package com.siya.epistemophile.recorder

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.core.content.ContextCompat
import eu.mrogalski.saidit.SaidItService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

@Singleton
class ServiceConnector @Inject constructor(
    @ApplicationContext private val appContext: Context
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _service = MutableStateFlow<SaidItService?>(null)
    val service = _service

    private var isBound = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val typed = binder as? SaidItService.BackgroundRecorderBinder
            _service.update { typed?.getService() }
        }
        override fun onServiceDisconnected(name: ComponentName?) {
            _service.update { null }
            isBound = false
        }
    }

    fun ensureBound() {
        if (isBound) return
        isBound = true
        val intent = Intent(appContext, SaidItService::class.java)
        // Make sure service is started as foreground; actual foreground is handled by service itself
        ContextCompat.startForegroundService(appContext, intent)
        appContext.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    suspend fun await(timeoutMs: Long = 15_000L): SaidItService? {
        ensureBound()
        return withTimeoutOrNull(timeoutMs) { service.first { it != null } }
    }
}

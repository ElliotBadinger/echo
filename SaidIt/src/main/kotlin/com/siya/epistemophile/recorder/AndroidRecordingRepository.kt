package com.siya.epistemophile.recorder

import com.siya.epistemophile.domain.model.RecorderState
import com.siya.epistemophile.domain.model.RecordingError
import com.siya.epistemophile.domain.repository.RecordingRepository
import eu.mrogalski.saidit.SaidItService
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

@Singleton
class AndroidRecordingRepository @Inject constructor(
    private val connector: ServiceConnector,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : RecordingRepository {

    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private val _state = MutableStateFlow(RecorderState())
    override val recorderState: StateFlow<RecorderState> = _state.asStateFlow()

    init {
        // Start observing service state lazily in background
        scope.launch {
            connector.ensureBound()
            while (true) {
                val service = connector.service.value
                if (service != null) {
                    tryReadState(service)?.let { snap ->
                        _state.value = _state.value.copy(
                            isListening = snap.listening,
                            isRecording = snap.recording,
                            memorizedSeconds = snap.memorized,
                            recordedSeconds = snap.recorded
                        )
                    }
                }
                delay(200)
            }
        }
    }

    override suspend fun enableListening(): Result<Unit> = runCatching {
        val service = connector.await() ?: error("Service not available")
        service.enableListening()
        _state.value = _state.value.copy(isListening = true)
    }

    override suspend fun disableListening(): Result<Unit> = runCatching {
        val service = connector.await() ?: error("Service not available")
        service.disableListening()
        _state.value = _state.value.copy(isListening = false, isRecording = false)
    }

    override suspend fun startRecording(prependedMemorySeconds: Float): Result<Unit> = runCatching {
        val service = connector.await() ?: error("Service not available")
        service.startRecording(prependedMemorySeconds)
        _state.value = _state.value.copy(isListening = true, isRecording = true)
    }.recoverCatching { throw RecordingError.Unknown(it) }

    override suspend fun stopRecording(): Result<Unit> = runCatching {
        val service = connector.await() ?: error("Service not available")
        service.stopRecording(null)
        _state.value = _state.value.copy(isRecording = false, hasUnsavedRecording = true)
    }.recoverCatching { throw RecordingError.Unknown(it) }

    override suspend fun dumpRecording(memorySeconds: Float, newFileName: String?): Result<Unit> =
        runCatching {
            val service = connector.await() ?: error("Service not available")
            // Suspend until the service completes saving
            suspendCancellableCoroutine<Unit> { cont ->
                service.dumpRecording(memorySeconds, object : SaidItService.WavFileReceiver {
                    override fun onSuccess(fileUri: android.net.Uri) {
                        if (!cont.isCompleted) cont.resume(Unit)
                    }
                    override fun onFailure(e: Exception) {
                        if (!cont.isCompleted) cont.resume(Unit) // Propagate error via state below
                    }
                }, newFileName ?: "")
            }
            _state.value = _state.value.copy(
                hasUnsavedRecording = false,
                lastSavedFileName = newFileName?.takeIf { it.isNotBlank() }
            )
        }.recoverCatching { throw RecordingError.Unknown(it) }

    private data class ServiceSnapshot(
        val listening: Boolean,
        val recording: Boolean,
        val memorized: Float,
        val recorded: Float
    )

    private suspend fun tryReadState(service: SaidItService): ServiceSnapshot? =
        suspendCancellableCoroutine { cont ->
            try {
                service.getState(object : SaidItService.StateCallback {
                    override fun state(
                        listeningEnabled: Boolean,
                        recording: Boolean,
                        memorized: Float,
                        totalMemory: Float,
                        recorded: Float
                    ) {
                        if (!cont.isCompleted) cont.resume(
                            ServiceSnapshot(
                                listening = listeningEnabled,
                                recording = recording,
                                memorized = memorized,
                                recorded = recorded
                            )
                        )
                    }
                })
            } catch (t: Throwable) {
                if (!cont.isCompleted) cont.resume(null)
            }
        }

    fun clear() { scope.cancel() }
}

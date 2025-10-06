package eu.mrogalski.saidit

import android.Manifest
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.siya.epistemophile.R

/**
 * Hosts the legacy SaidIt recorder experience and ensures the background
 * recording service is running once the user grants runtime permissions.
 */
class SaidItActivity : AppCompatActivity() {

    private var permissionDeniedDialog: AlertDialog? = null
    private var echoService: SaidItService? = null
    private var isBound = false
    private var isFragmentSet = false
    private var mainFragment: SaidItFragment? = null

    private val sharedPreferences: SharedPreferences by lazy {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, binder: IBinder) {
            val typedBinder = binder as SaidItService.BackgroundRecorderBinder
            echoService = typedBinder.getService()
            isBound = true
            mainFragment?.setService(echoService)
        }

        override fun onServiceDisconnected(name: ComponentName) {
            echoService = null
            isBound = false
        }
    }

    private val howToLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            mainFragment?.startTour()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_background_recorder)
    }

    override fun onStart() {
        super.onStart()
        permissionDeniedDialog?.dismiss()
        requestPermissions()
    }

    override fun onRestart() {
        super.onRestart()
        permissionDeniedDialog?.dismiss()
        requestPermissions()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
    }

    private fun requestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.FOREGROUND_SERVICE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions += Manifest.permission.POST_NOTIFICATIONS
        }
        ActivityCompat.requestPermissions(
            this,
            permissions.toTypedArray(),
            PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != PERMISSION_REQUEST_CODE) return

        val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        if (allGranted) {
            ensureServiceBound()
            showFragmentIfNeeded()
        } else {
            if (permissionDeniedDialog?.isShowing != true) {
                showPermissionDeniedDialog()
            }
        }
    }

    private fun ensureServiceBound() {
        if (isBound) return

        val serviceIntent = Intent(this, SaidItService::class.java)
        startService(serviceIntent)
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun showFragmentIfNeeded() {
        if (isFragmentSet) return

        isFragmentSet = true
        val prefs = sharedPreferences
        val isFirstRun = prefs.getBoolean(KEY_FIRST_RUN, true)

        mainFragment = SaidItFragment().also { fragment ->
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, fragment, MAIN_FRAGMENT_TAG)
                .commit()
        }

        when {
            isFirstRun -> {
                howToLauncher.launch(Intent(this, HowToActivity::class.java))
                prefs.edit().putBoolean(KEY_FIRST_RUN, false).apply()
            }

            prefs.getBoolean(KEY_TOUR_ON_NEXT_LAUNCH, false) -> {
                mainFragment?.startTour()
                prefs.edit().putBoolean(KEY_TOUR_ON_NEXT_LAUNCH, false).apply()
            }
        }
    }

    private fun showPermissionDeniedDialog() {
        permissionDeniedDialog = buildDialog(
            R.string.permission_required,
            R.string.permission_required_message
        )
            .setPositiveButton(R.string.allow) { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                }
                startActivity(intent)
            }
            .setNegativeButton(R.string.exit) { _, _ -> finish() }
            .setCancelable(false)
            .show()
    }

    private fun buildDialog(@StringRes titleRes: Int, @StringRes messageRes: Int): AlertDialog.Builder =
        AlertDialog.Builder(this)
            .setTitle(titleRes)
            .setMessage(messageRes)

    internal fun getEchoService(): SaidItService? = echoService

    companion object {
        private const val PERMISSION_REQUEST_CODE = 5465
        private const val PREFS_NAME = "eu.mrogalski.saidit"
        private const val KEY_FIRST_RUN = "is_first_run"
        private const val KEY_TOUR_ON_NEXT_LAUNCH = "show_tour_on_next_launch"
        private const val MAIN_FRAGMENT_TAG = "main-fragment"
    }
}

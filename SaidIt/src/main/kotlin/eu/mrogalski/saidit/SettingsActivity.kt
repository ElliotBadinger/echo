package eu.mrogalski.saidit

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.os.Bundle
import android.os.IBinder
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.slider.Slider
import com.google.android.material.switchmaterial.SwitchMaterial
import com.siya.epistemophile.R
import eu.mrogalski.StringFormat
import eu.mrogalski.android.TimeFormat

/**
 * Legacy settings screen for configuring memory, sample rate, and auto-save behaviour.
 */
class SettingsActivity : AppCompatActivity() {

    private var service: SaidItService? = null
    private var isBound = false

    private lateinit var historyLimitTextView: TextView
    private lateinit var memoryToggleGroup: MaterialButtonToggleGroup
    private lateinit var qualityToggleGroup: MaterialButtonToggleGroup
    private lateinit var memoryLowButton: Button
    private lateinit var memoryMediumButton: Button
    private lateinit var memoryHighButton: Button
    private lateinit var quality8kHzButton: Button
    private lateinit var quality16kHzButton: Button
    private lateinit var quality48kHzButton: Button
    private lateinit var autoSaveSwitch: SwitchMaterial
    private lateinit var autoSaveDurationSlider: Slider
    private lateinit var autoSaveDurationLabel: TextView
    private lateinit var sharedPreferences: SharedPreferences

    private val memoryToggleListener =
        MaterialButtonToggleGroup.OnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked || !isBound) return@OnButtonCheckedListener

            val maxMemory = Runtime.getRuntime().maxMemory()
            val memorySize = when (checkedId) {
                R.id.memory_medium -> maxMemory / 2
                R.id.memory_high -> (maxMemory * 0.90).toLong()
                else -> maxMemory / 4
            }
            service?.setMemorySize(memorySize)
            updateHistoryLimit()
        }

    private val qualityToggleListener =
        MaterialButtonToggleGroup.OnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked || !isBound) return@OnButtonCheckedListener

            val sampleRate = when (checkedId) {
                R.id.quality_16kHz -> 16_000
                R.id.quality_48kHz -> 48_000
                else -> 8_000
            }
            service?.setSampleRate(sampleRate)
            updateHistoryLimit()
        }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, binder: IBinder) {
            val typedBinder = binder as SaidItService.BackgroundRecorderBinder
            service = typedBinder.getService()
            isBound = true
            syncUi()
        }

        override fun onServiceDisconnected(name: ComponentName) {
            service = null
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        sharedPreferences = getSharedPreferences(SaidIt.PACKAGE_NAME, MODE_PRIVATE)

        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        historyLimitTextView = findViewById(R.id.history_limit)
        memoryToggleGroup = findViewById(R.id.memory_toggle_group)
        qualityToggleGroup = findViewById(R.id.quality_toggle_group)
        memoryLowButton = findViewById(R.id.memory_low)
        memoryMediumButton = findViewById(R.id.memory_medium)
        memoryHighButton = findViewById(R.id.memory_high)
        quality8kHzButton = findViewById(R.id.quality_8kHz)
        quality16kHzButton = findViewById(R.id.quality_16kHz)
        quality48kHzButton = findViewById(R.id.quality_48kHz)
        autoSaveSwitch = findViewById(R.id.auto_save_switch)
        autoSaveDurationSlider = findViewById(R.id.auto_save_duration_slider)
        autoSaveDurationLabel = findViewById(R.id.auto_save_duration_label)
        val howToButton: Button = findViewById(R.id.how_to_button)
        val showTourButton: Button = findViewById(R.id.show_tour_button)

        toolbar.setNavigationOnClickListener { finish() }

        howToButton.setOnClickListener {
            startActivity(Intent(this, HowToActivity::class.java))
        }
        showTourButton.setOnClickListener {
            sharedPreferences.edit().putBoolean(KEY_TOUR_ON_NEXT_LAUNCH, true).apply()
            finish()
        }

        memoryToggleGroup.addOnButtonCheckedListener(memoryToggleListener)
        qualityToggleGroup.addOnButtonCheckedListener(qualityToggleListener)

        autoSaveSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean(KEY_AUTO_SAVE_ENABLED, isChecked).apply()
            autoSaveDurationSlider.isEnabled = isChecked
            autoSaveDurationLabel.isEnabled = isChecked
            if (!isBound) return@setOnCheckedChangeListener
            if (isChecked) {
                service?.scheduleAutoSave()
            } else {
                service?.cancelAutoSave()
            }
        }

        autoSaveDurationSlider.addOnChangeListener { _, value, fromUser ->
            val minutes = value.toInt()
            updateAutoSaveLabel(minutes)
            if (fromUser) {
                sharedPreferences.edit().putInt(KEY_AUTO_SAVE_DURATION, minutes * 60).apply()
                if (isBound) {
                    service?.scheduleAutoSave()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val intent = Intent(this, SaidItService::class.java)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        if (isBound) {
            unbindService(connection)
            isBound = false
        }
    }

    private fun syncUi() {
        val currentService = service ?: return

        memoryToggleGroup.removeOnButtonCheckedListener(memoryToggleListener)
        qualityToggleGroup.removeOnButtonCheckedListener(qualityToggleListener)

        val maxMemory = Runtime.getRuntime().maxMemory()
        memoryLowButton.text = StringFormat.shortFileSize(maxMemory / 4)
        memoryMediumButton.text = StringFormat.shortFileSize(maxMemory / 2)
        memoryHighButton.text = StringFormat.shortFileSize((maxMemory * 0.90).toLong())

        val currentMemory = currentService.getMemorySize()
        when {
            currentMemory <= maxMemory / 4 -> memoryToggleGroup.check(R.id.memory_low)
            currentMemory <= maxMemory / 2 -> memoryToggleGroup.check(R.id.memory_medium)
            else -> memoryToggleGroup.check(R.id.memory_high)
        }

        val currentRate = currentService.getSamplingRate()
        when {
            currentRate >= 48_000 -> qualityToggleGroup.check(R.id.quality_48kHz)
            currentRate >= 16_000 -> qualityToggleGroup.check(R.id.quality_16kHz)
            else -> qualityToggleGroup.check(R.id.quality_8kHz)
        }

        val autoSaveEnabled = sharedPreferences.getBoolean(KEY_AUTO_SAVE_ENABLED, false)
        autoSaveSwitch.isChecked = autoSaveEnabled
        autoSaveDurationSlider.isEnabled = autoSaveEnabled
        autoSaveDurationLabel.isEnabled = autoSaveEnabled

        val autoSaveDurationSeconds = sharedPreferences.getInt(KEY_AUTO_SAVE_DURATION, DEFAULT_AUTO_SAVE_SECONDS)
        val autoSaveMinutes = autoSaveDurationSeconds / 60
        autoSaveDurationSlider.value = autoSaveMinutes.toFloat()
        updateAutoSaveLabel(autoSaveMinutes)

        updateHistoryLimit()

        memoryToggleGroup.addOnButtonCheckedListener(memoryToggleListener)
        qualityToggleGroup.addOnButtonCheckedListener(qualityToggleListener)
    }

    private fun updateHistoryLimit() {
        val currentService = service ?: return
        val historySeconds = currentService.getBytesToSeconds() * currentService.getMemorySize().toFloat()
        val result = TimeFormat.Result()
        TimeFormat.naturalLanguage(resources, historySeconds, result)
        historyLimitTextView.text = result.text
    }

    private fun updateAutoSaveLabel(totalMinutes: Int) {
        val res = resources
        if (totalMinutes < 60) {
            autoSaveDurationLabel.text =
                res.getQuantityString(R.plurals.minute_plural, totalMinutes, totalMinutes)
            return
        }

        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        val hourText = res.getQuantityString(R.plurals.hour_plural, hours, hours)
        autoSaveDurationLabel.text = if (minutes == 0) {
            hourText
        } else {
            val minuteText = res.getQuantityString(R.plurals.minute_plural, minutes, minutes)
            res.getString(R.string.time_join, hourText, minuteText)
        }
    }

    companion object {
        private const val KEY_TOUR_ON_NEXT_LAUNCH = "show_tour_on_next_launch"
        private const val KEY_AUTO_SAVE_ENABLED = "auto_save_enabled"
        private const val KEY_AUTO_SAVE_DURATION = "auto_save_duration"
        private const val DEFAULT_AUTO_SAVE_SECONDS = 600
    }
}

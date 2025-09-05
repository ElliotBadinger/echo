package eu.mrogalski.saidit

import android.os.SystemClock

/**
 * Production implementation of Clock interface that wraps Android's SystemClock.
 * Provides real system uptime for production use.
 */
class SystemClockWrapper : Clock {
    override fun uptimeMillis(): Long = SystemClock.uptimeMillis()
}
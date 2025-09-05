package eu.mrogalski.saidit

/**
 * Clock interface for providing system time in a testable way.
 * Allows for dependency injection and mocking in tests.
 */
interface Clock {
    /**
     * Returns the current system uptime in milliseconds.
     * @return Current uptime in milliseconds since system boot
     */
    fun uptimeMillis(): Long
}
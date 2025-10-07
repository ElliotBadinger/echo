package eu.mrogalski.saidit

/**
 * Test implementation of Clock interface for controlled time testing.
 * Allows tests to control the passage of time for deterministic testing.
 */
class FakeClock : Clock {
    private var currentTime = 0L

    override fun uptimeMillis(): Long = currentTime

    /**
     * Advances the fake clock by the specified number of milliseconds.
     * @param millis Number of milliseconds to advance the clock
     */
    fun advance(millis: Long) {
        currentTime += millis
    }

    /**
     * Sets the fake clock to a specific time.
     * @param time The time in milliseconds to set the clock to
     */
    fun setTime(time: Long) {
        currentTime = time
    }

    /**
     * Resets the fake clock back to zero.
     */
    fun reset() {
        currentTime = 0L
    }
}
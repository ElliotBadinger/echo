package eu.mrogalski.saidit

import java.io.Closeable
import java.io.IOException

/**
 * Abstraction for classes that persist PCM audio samples.
 * Allows SaidItService to swap implementations during tests.
 */
interface AudioSampleWriter : Closeable {
    @Throws(IOException::class)
    fun write(data: ByteArray, offset: Int, length: Int)

    fun getTotalSampleBytesWritten(): Int
}

package eu.mrogalski.saidit

import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.io.ByteArrayOutputStream
import java.io.IOException

class AudioMemoryTest {

    private lateinit var audioMemory: AudioMemory
    private lateinit var clock: FakeClock

    @Before
    fun setUp() {
        clock = FakeClock()
        audioMemory = AudioMemory(clock)
        audioMemory.allocate(1024 * 4)
    }

    private class TestFiller(private val dataToProvide: ByteArray) : AudioMemory.Consumer {
        private var readOffset = 0

        override fun consume(array: ByteArray, offset: Int, count: Int): Result<Int> {
            val toRead = minOf(count, dataToProvide.size - readOffset)
            if (toRead > 0) {
                System.arraycopy(dataToProvide, readOffset, array, offset, toRead)
                readOffset += toRead
            }
            return Result.success(toRead)
        }
    }

    private class CapturingConsumer : AudioMemory.Consumer {
        private val baos = ByteArrayOutputStream()

        override fun consume(array: ByteArray, offset: Int, count: Int): Result<Int> {
            baos.write(array, offset, count)
            return Result.success(count)
        }

        fun getCapturedData(): ByteArray = baos.toByteArray()
    }

    @Test
    @Throws(IOException::class)
    fun testWriteNoWrap() {
        val data = ByteArray(100) { it.toByte() }

        audioMemory.fill(TestFiller(data))

        val stats = audioMemory.getStats(0)
        assertEquals(100, stats.filled)
        assertFalse(stats.overwriting)
    }

    @Test
    @Throws(IOException::class)
    fun testWriteWithWrap() {
        val bufferSize = audioMemory.getAllocatedMemorySize().toInt()
        val data = ByteArray(bufferSize + 50) { it.toByte() }

        audioMemory.fill(TestFiller(data))

        val stats = audioMemory.getStats(0)
        assertEquals(bufferSize, stats.filled)
        assertTrue(stats.overwriting)
    }

    @Test
    @Throws(IOException::class)
    fun testDumpNoWrap() {
        val data = ByteArray(200) { it.toByte() }
        audioMemory.fill(TestFiller(data))

        val consumer = CapturingConsumer()
        audioMemory.dump(consumer, 100)

        val dumped = consumer.getCapturedData()
        assertEquals(100, dumped.size)
        for (i in 0 until 100) {
            assertEquals((i + 100).toByte(), dumped[i])
        }
    }

    @Test
    @Throws(IOException::class)
    fun testDumpWithWrap() {
        val bufferSize = audioMemory.getAllocatedMemorySize().toInt()
        val data = ByteArray(bufferSize + 50) { it.toByte() }

        // This will wrap around
        audioMemory.fill(TestFiller(data))

        val consumer = CapturingConsumer()
        audioMemory.dump(consumer, 100)

        val dumped = consumer.getCapturedData()
        assertEquals(100, dumped.size)

        // Check if the dumped data is the last 100 bytes of the input data
        val expected = ByteArray(100)
        System.arraycopy(data, data.size - 100, expected, 0, 100)
        assertArrayEquals(expected, dumped)
    }

    @Test
    @Throws(IOException::class)
    fun testDumpFullBuffer() {
        val bufferSize = audioMemory.getAllocatedMemorySize().toInt()
        val data = ByteArray(bufferSize) { it.toByte() }

        audioMemory.fill(TestFiller(data))

        val consumer = CapturingConsumer()
        audioMemory.dump(consumer, bufferSize)

        val dumped = consumer.getCapturedData()
        assertEquals(bufferSize, dumped.size)
        assertArrayEquals(data, dumped)
    }

    @Test
    @Throws(IOException::class)
    fun testDumpZeroBytes() {
        val data = ByteArray(100)
        audioMemory.fill(TestFiller(data))

        val consumer = CapturingConsumer()
        audioMemory.dump(consumer, 0)

        assertEquals(0, consumer.getCapturedData().size)
    }

    @Test
    @Throws(IOException::class)
    fun testStats() {
        audioMemory.allocate(1000)
        var stats = audioMemory.getStats(0)
        assertEquals(0, stats.filled)
        assertEquals(AudioMemory.CHUNK_SIZE, stats.total)
        assertFalse(stats.overwriting)

        audioMemory.fill(TestFiller(ByteArray(500)))
        stats = audioMemory.getStats(0)
        assertEquals(500, stats.filled)
        assertFalse(stats.overwriting)

        audioMemory.fill(TestFiller(ByteArray(AudioMemory.CHUNK_SIZE))) // This will overwrite
        stats = audioMemory.getStats(0)
        assertEquals(AudioMemory.CHUNK_SIZE, stats.filled)
        assertTrue(stats.overwriting)
    }
}
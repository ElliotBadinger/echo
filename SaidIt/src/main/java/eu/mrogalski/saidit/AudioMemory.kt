package eu.mrogalski.saidit

import com.siya.epistemophile.R
import java.io.IOException
import java.nio.ByteBuffer

/**
 * Modern Kotlin implementation of AudioMemory with Result<T> error handling.
 * Manages a ring buffer for audio data with improved error handling and thread safety.
 */
open class AudioMemory(private val clock: Clock) {

    companion object {
        // Keep chunk size as allocation granularity (20s @ 48kHz mono 16-bit)
        const val CHUNK_SIZE = 1920000 // bytes
    }

    // Ring buffer
    private var ring: ByteBuffer? = null // direct buffer
    private var capacity = 0 // bytes
    private var writePos = 0 // next write index [0..capacity)
    private var size = 0     // number of valid bytes stored (<= capacity)

    // Fill estimation
    private var fillingStartUptimeMillis: Long = 0
    private var filling = false
    private var overwriting = false

    // Reusable IO buffer to reduce allocations when interacting with AudioRecord/consumers
    private var ioBuffer = ByteArray(32 * 1024)

    /**
     * Consumer interface for audio data processing with Result<T> error handling
     */
    fun interface Consumer {
        /**
         * Consume audio data from the provided array
         * @return Result<Int> containing bytes consumed or error
         */
        fun consume(array: ByteArray, offset: Int, count: Int): Result<Int>
    }

    /**
     * Legacy Consumer interface for backward compatibility
     */
    fun interface LegacyConsumer {
        @Throws(IOException::class)
        fun consume(array: ByteArray, offset: Int, count: Int): Int
    }

    /**
     * Allocate ring buffer memory with the specified size
     * @param sizeToEnsure minimum size to ensure in bytes
     * @return Result<Unit> indicating success or failure
     */
    @Synchronized
    open fun allocate(sizeToEnsure: Long): Result<Unit> {
        return try {
            var required = 0
            while (required < sizeToEnsure) required += CHUNK_SIZE
            if (required == capacity) return Result.success(Unit) // no change

            // Allocate new ring; drop previous content to free memory pressure.
            ring = if (required > 0) ByteBuffer.allocateDirect(required) else null
            capacity = required
            writePos = 0
            size = 0
            overwriting = false
            
            Result.success(Unit)
        } catch (e: OutOfMemoryError) {
            Result.failure(AudioMemoryException("Failed to allocate $sizeToEnsure bytes", e))
        } catch (e: Exception) {
            Result.failure(AudioMemoryException("Unexpected error during allocation", e))
        }
    }

    /**
     * Get the currently allocated memory size
     * @return allocated memory size in bytes
     */
    @Synchronized
    fun getAllocatedMemorySize(): Long = capacity.toLong()

    /**
     * Count the number of filled bytes in the buffer
     * @return number of filled bytes
     */
    fun countFilled(): Int {
        synchronized(this) {
            return size
        }
    }

    /**
     * Ensure ioBuffer is at least min bytes
     */
    private fun ensureIoBuffer(min: Int) {
        if (ioBuffer.size < min) {
            var newLen = ioBuffer.size
            while (newLen < min) newLen = maxOf(newLen * 2, 4096)
            ioBuffer = ByteArray(newLen)
        }
    }

    /**
     * Fill ring buffer with newly recorded data using Result<T> pattern
     * @param filler Consumer that provides audio data
     * @return Result<Int> containing total bytes read or error
     */
    fun fill(filler: Consumer): Result<Int> {
        var totalRead = 0
        
        synchronized(this) {
            if (capacity == 0 || ring == null) return Result.success(0)
            filling = true
            fillingStartUptimeMillis = clock.uptimeMillis()
        }

        try {
            ensureIoBuffer(32 * 1024)

            // The filler might provide data in multiple chunks.
            var shouldContinue = true
            while (shouldContinue) {
                val readResult = filler.consume(ioBuffer, 0, ioBuffer.size)
                
                when {
                    readResult.isFailure -> {
                        synchronized(this) { filling = false }
                        return Result.failure(
                            AudioMemoryException("Error reading from consumer", readResult.exceptionOrNull())
                        )
                    }
                    readResult.getOrNull() == null || readResult.getOrNull()!! <= 0 -> {
                        shouldContinue = false
                    }
                    else -> {
                        val read = readResult.getOrNull()!!
                        synchronized(this) {
                            if (read > 0 && capacity > 0) { // check capacity again inside sync block
                                // Write into ring with wrap-around
                                val first = minOf(read, capacity - writePos)
                                if (first > 0) {
                                    val dup = ring!!.duplicate()
                                    dup.position(writePos)
                                    dup.put(ioBuffer, 0, first)
                                }
                                val remaining = read - first
                                if (remaining > 0) {
                                    val dup = ring!!.duplicate()
                                    dup.position(0)
                                    dup.put(ioBuffer, first, remaining)
                                }
                                writePos = (writePos + read) % capacity
                                val newSize = size + read
                                if (newSize > capacity) {
                                    overwriting = true
                                    size = capacity
                                } else {
                                    size = newSize
                                }
                                totalRead += read
                            } else {
                                // capacity became 0, stop filling
                                shouldContinue = false
                            }
                        }
                    }
                }
            }

            synchronized(this) {
                filling = false
            }
            return Result.success(totalRead)
            
        } catch (e: Exception) {
            synchronized(this) {
                filling = false
            }
            return Result.failure(AudioMemoryException("Unexpected error during fill operation", e))
        }
    }

    /**
     * Legacy fill method for backward compatibility
     * @param filler LegacyConsumer that provides audio data
     * @return total bytes read
     * @throws IOException if an error occurs
     */
    @Throws(IOException::class)
    fun fill(filler: LegacyConsumer): Int {
        val modernConsumer = Consumer { array, offset, count ->
            try {
                Result.success(filler.consume(array, offset, count))
            } catch (e: IOException) {
                Result.failure(e)
            }
        }
        
        return fill(modernConsumer).getOrElse { error ->
            throw when (error) {
                is IOException -> error
                is AudioMemoryException -> IOException(error.message, error.cause)
                else -> IOException("Audio memory operation failed", error)
            }
        }
    }

    /**
     * Dump audio data to consumer with Result<T> error handling
     * @param consumer Consumer to receive the audio data
     * @param bytesToDump number of bytes to dump
     * @return Result<Unit> indicating success or failure
     */
    @Synchronized
    open fun dump(consumer: Consumer, bytesToDump: Int): Result<Unit> {
        if (capacity == 0 || ring == null || size == 0 || bytesToDump <= 0) {
            return Result.success(Unit)
        }

        return try {
            val toCopy = minOf(bytesToDump, size)
            val skip = size - toCopy // skip older bytes beyond window

            val start = (writePos - size + capacity) % capacity // oldest
            var readPos = (start + skip) % capacity             // first byte to output

            var remaining = toCopy
            while (remaining > 0) {
                val chunk = minOf(remaining, capacity - readPos)
                // Copy out chunk into consumer via temporary array
                ensureIoBuffer(chunk)
                val dup = ring!!.duplicate()
                dup.position(readPos)
                dup.get(ioBuffer, 0, chunk)
                
                val consumeResult = consumer.consume(ioBuffer, 0, chunk)
                if (consumeResult.isFailure) {
                    return Result.failure(
                        AudioMemoryException("Error writing to consumer", consumeResult.exceptionOrNull())
                    )
                }
                
                remaining -= chunk
                readPos = (readPos + chunk) % capacity
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(AudioMemoryException("Unexpected error during dump operation", e))
        }
    }

    /**
     * Legacy dump method for backward compatibility
     * @param consumer LegacyConsumer to receive the audio data
     * @param bytesToDump number of bytes to dump
     * @throws IOException if an error occurs
     */
    @Synchronized
    @Throws(IOException::class)
    fun dump(consumer: LegacyConsumer, bytesToDump: Int) {
        val modernConsumer = Consumer { array, offset, count ->
            try {
                Result.success(consumer.consume(array, offset, count))
            } catch (e: IOException) {
                Result.failure(e)
            }
        }
        
        dump(modernConsumer, bytesToDump).getOrElse { error ->
            throw when (error) {
                is IOException -> error
                is AudioMemoryException -> IOException(error.message, error.cause)
                else -> IOException("Audio memory operation failed", error)
            }
        }
    }

    /**
     * Statistics about the audio memory buffer
     */
    data class Stats(
        val filled: Int,        // bytes stored
        val total: Int,         // capacity
        val estimation: Int,    // bytes assumed in flight since last fill started
        val overwriting: Boolean // whether we've wrapped at least once
    )

    /**
     * Get current buffer statistics
     * @param fillRate fill rate in bytes per second
     * @return current buffer statistics
     */
    @Synchronized
    fun getStats(fillRate: Int): Stats {
        return Stats(
            filled = size,
            total = capacity,
            estimation = if (filling) {
                ((clock.uptimeMillis() - fillingStartUptimeMillis) * fillRate / 1000).toInt()
            } else 0,
            overwriting = overwriting
        )
    }
}

/**
 * Custom exception for AudioMemory operations
 */
class AudioMemoryException(message: String, cause: Throwable? = null) : Exception(message, cause)
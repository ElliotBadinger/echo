package com.siya.epistemophile.audio.pcm

import java.io.Closeable
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import kotlin.math.min

internal object PcmByteUtils {

    fun toReducedBitIntArray(
        data: ByteArray,
        length: Int,
        bytesPerSample: Int,
        sampleSizeInBits: Int,
        bigEndian: Boolean
    ): IntArray {
        if (length == 0) return IntArray(0)
        require(length % bytesPerSample == 0) {
            "Byte count $length is not aligned to sample size $bytesPerSample"
        }
        val sampleCount = length / bytesPerSample
        val result = IntArray(sampleCount)
        val shift = 32 - sampleSizeInBits
        var offset = 0
        repeat(sampleCount) { index ->
            var value = 0
            if (bigEndian) {
                repeat(bytesPerSample) { step ->
                    value = (value shl 8) or (data[offset + step].toInt() and 0xFF)
                }
            } else {
                for (step in bytesPerSample - 1 downTo 0) {
                    value = (value shl 8) or (data[offset + step].toInt() and 0xFF)
                }
            }
            result[index] = (value shl shift) shr shift
            offset += bytesPerSample
        }
        return result
    }

    fun toByteArray(
        values: IntArray,
        length: Int,
        bytesPerSample: Int,
        bigEndian: Boolean
    ): ByteArray {
        val actualLength = min(length, values.size)
        val result = ByteArray(actualLength * bytesPerSample)
        var offset = 0
        repeat(actualLength) { index ->
            val value = values[index]
            if (bigEndian) {
                for (step in bytesPerSample - 1 downTo 0) {
                    result[offset + step] = (value shr ((bytesPerSample - 1 - step) * 8)).toByte()
                }
            } else {
                repeat(bytesPerSample) { step ->
                    result[offset + step] = (value shr (step * 8)).toByte()
                }
            }
            offset += bytesPerSample
        }
        return result
    }

    fun toByteArray(values: ShortArray, length: Int, bigEndian: Boolean): ByteArray {
        val actualLength = min(length, values.size)
        val result = ByteArray(actualLength * 2)
        var offset = 0
        repeat(actualLength) { index ->
            val value = values[index].toInt()
            if (bigEndian) {
                result[offset] = (value shr 8).toByte()
                result[offset + 1] = value.toByte()
            } else {
                result[offset] = value.toByte()
                result[offset + 1] = (value shr 8).toByte()
            }
            offset += 2
        }
        return result
    }

    fun toByteArray(value: Int, bigEndian: Boolean): ByteArray {
        val bytes = ByteArray(4)
        if (bigEndian) {
            bytes[0] = (value shr 24).toByte()
            bytes[1] = (value shr 16).toByte()
            bytes[2] = (value shr 8).toByte()
            bytes[3] = value.toByte()
        } else {
            bytes[0] = value.toByte()
            bytes[1] = (value shr 8).toByte()
            bytes[2] = (value shr 16).toByte()
            bytes[3] = (value shr 24).toByte()
        }
        return bytes
    }

    fun toByteArray(value: Short, bigEndian: Boolean): ByteArray {
        val bytes = ByteArray(2)
        if (bigEndian) {
            bytes[0] = (value.toInt() shr 8).toByte()
            bytes[1] = value.toByte()
        } else {
            bytes[0] = value.toByte()
            bytes[1] = (value.toInt() shr 8).toByte()
        }
        return bytes
    }

    fun readAll(input: InputStream): ByteArray = input.readBytes()

    fun copy(input: InputStream, output: OutputStream) {
        input.use { source ->
            output.use { target ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                while (true) {
                    val read = source.read(buffer)
                    if (read == -1) break
                    target.write(buffer, 0, read)
                }
            }
        }
    }

    fun closeQuietly(closeable: Closeable?) {
        try {
            closeable?.close()
        } catch (_: IOException) {
            // Ignore
        }
    }
}

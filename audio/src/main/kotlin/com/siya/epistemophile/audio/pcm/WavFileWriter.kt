package com.siya.epistemophile.audio.pcm

import java.io.Closeable
import java.io.File
import java.io.IOException

class WavFileWriter(
    private val wavAudioFormat: WavAudioFormat,
    private val file: File
) : Closeable {

    private val outputStream = PcmMonoOutputStream(wavAudioFormat, file)
    private var totalSampleBytesWritten: Int = 0

    init {
        require(!wavAudioFormat.bigEndian) { "Wav file cannot contain bigEndian sample data." }
        if (wavAudioFormat.sampleSizeInBits > 8) {
            require(wavAudioFormat.signed) { "Wav file cannot contain unsigned data for this sampleSize:${wavAudioFormat.sampleSizeInBits}" }
        }
        outputStream.write(RiffHeaderData(wavAudioFormat, 0).asByteArray())
    }

    @Throws(IOException::class)
    fun write(bytes: ByteArray): WavFileWriter {
        checkLimit(totalSampleBytesWritten, bytes.size)
        outputStream.write(bytes, 0, bytes.size)
        totalSampleBytesWritten += bytes.size
        return this
    }

    @Throws(IOException::class)
    fun write(bytes: ByteArray, offset: Int, count: Int): WavFileWriter {
        checkLimit(totalSampleBytesWritten, count)
        outputStream.write(bytes, offset, count)
        totalSampleBytesWritten += count
        return this
    }

    @Throws(IOException::class)
    fun write(samples: IntArray): WavFileWriter {
        val bytesToAdd = samples.size * wavAudioFormat.bytesRequiredPerSample
        checkLimit(totalSampleBytesWritten, bytesToAdd)
        outputStream.write(samples)
        totalSampleBytesWritten += bytesToAdd
        return this
    }

    @Throws(IOException::class)
    fun write(samples: ShortArray): WavFileWriter {
        val bytesToAdd = samples.size * 2
        checkLimit(totalSampleBytesWritten, bytesToAdd)
        outputStream.write(samples)
        totalSampleBytesWritten += bytesToAdd
        return this
    }

    private fun checkLimit(total: Int, toAdd: Int) {
        val result = total.toLong() + toAdd
        require(result < Int.MAX_VALUE) { "Size of bytes is too big:$result" }
    }

    override fun close() {
        PcmByteUtils.closeQuietly(outputStream)
        PcmAudioHelper.modifyRiffSizeData(file, totalSampleBytesWritten)
    }

    fun getWavFormat(): PcmAudioFormat = wavAudioFormat

    fun getTotalSampleBytesWritten(): Int = totalSampleBytesWritten
}

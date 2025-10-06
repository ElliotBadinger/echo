package com.siya.epistemophile.audio.pcm

import java.io.DataInputStream
import java.io.IOException
import java.io.InputStream

class PcmMonoInputStream(
    val format: PcmAudioFormat,
    inputStream: InputStream
) : InputStream() {

    private val dataInput = DataInputStream(inputStream)
    private val maxPositiveIntegerForSampleSize: Int = 0x7fffffff ushr (32 - format.sampleSizeInBits)

    init {
        require(format.channels == 1) { "Only mono streams are supported." }
    }

    override fun read(): Int = dataInput.read()

    @Throws(IOException::class)
    fun readSamplesAsIntArray(amount: Int): IntArray {
        val buffer = ByteArray(amount * format.bytesRequiredPerSample)
        val readAmount = dataInput.read(buffer)
        if (readAmount == -1) {
            return IntArray(0)
        }
        return PcmByteUtils.toReducedBitIntArray(
            buffer,
            readAmount,
            format.bytesRequiredPerSample,
            format.sampleSizeInBits,
            format.bigEndian
        )
    }

    @Throws(IOException::class)
    fun readAll(): IntArray {
        val all = PcmByteUtils.readAll(dataInput)
        return PcmByteUtils.toReducedBitIntArray(
            all,
            all.size,
            format.bytesRequiredPerSample,
            format.sampleSizeInBits,
            format.bigEndian
        )
    }

    @Throws(IOException::class)
    fun readSamplesAsByteArray(amount: Int): ByteArray {
        val buffer = ByteArray(amount * format.bytesRequiredPerSample)
        val readCount = dataInput.read(buffer)
        if (readCount == -1) {
            return ByteArray(0)
        }
        if (readCount != buffer.size) {
            validateReadCount(readCount)
            return buffer.copyOf(readCount)
        }
        return buffer
    }

    private fun validateReadCount(readCount: Int) {
        require(readCount % format.bytesRequiredPerSample == 0) {
            "unexpected amounts of bytes read from the input stream. Byte count must be an order of:${format.bytesRequiredPerSample}"
        }
    }

    @Throws(IOException::class)
    fun readSamplesAsIntArray(frameStart: Int, frameEnd: Int): IntArray {
        skipSamples(frameStart)
        return readSamplesAsIntArray(frameEnd - frameStart)
    }

    @Throws(IOException::class)
    fun skipSamples(skipAmount: Int): Int {
        val actualSkipped = dataInput.skipBytes(skipAmount * format.bytesRequiredPerSample)
        return actualSkipped / format.bytesRequiredPerSample
    }

    @Throws(IOException::class)
    fun readSamplesNormalized(amount: Int): DoubleArray = normalize(readSamplesAsIntArray(amount))

    @Throws(IOException::class)
    fun readSamplesNormalized(): DoubleArray = normalize(readAll())

    private fun normalize(original: IntArray): DoubleArray {
        if (original.isEmpty()) return DoubleArray(0)
        val normalized = DoubleArray(original.size)
        for (i in original.indices) {
            normalized[i] = original[i].toDouble() / maxPositiveIntegerForSampleSize
        }
        return normalized
    }

    override fun close() {
        PcmByteUtils.closeQuietly(dataInput)
    }

    fun calculateSampleByteIndex(second: Double): Int {
        require(second >= 0) { "Time information cannot be negative." }
        var location = (second * format.sampleRate * format.bytesRequiredPerSample).toInt()
        if (location % format.bytesRequiredPerSample != 0) {
            location += format.bytesRequiredPerSample - location % format.bytesRequiredPerSample
        }
        return location
    }

    fun calculateSampleTime(sampleIndex: Int): Double {
        require(sampleIndex >= 0) { "sampleIndex information cannot be negative:$sampleIndex" }
        return sampleIndex.toDouble() / format.sampleRate
    }

    fun skip(byteCount: Int): Long = dataInput.skipBytes(byteCount).toLong()
}

package com.siya.epistemophile.audio.pcm

import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException

internal class RiffHeaderData(
    val format: PcmAudioFormat,
    val totalSamplesInByte: Int
) {

    val sampleCount: Int
        get() = totalSamplesInByte / format.bytesRequiredPerSample

    fun timeSeconds(): Double {
        return totalSamplesInByte.toDouble() / format.bytesRequiredPerSample / format.sampleRate
    }

    constructor(pair: Pair<PcmAudioFormat, Int>) : this(pair.first, pair.second)

    constructor(inputStream: DataInputStream) : this(readHeader(inputStream))

    constructor(file: File) : this(DataInputStream(FileInputStream(file)))

    fun asByteArray(): ByteArray {
        val output = ByteArrayOutputStream()
        return try {
            output.write(PcmByteUtils.toByteArray(RIFF_CHUNK_ID, bigEndian = true))
            output.write(PcmByteUtils.toByteArray(36 + totalSamplesInByte, bigEndian = false))
            output.write(PcmByteUtils.toByteArray(WAVE_FORMAT_ID, bigEndian = true))

            output.write(PcmByteUtils.toByteArray(FMT_CHUNK_ID, bigEndian = true))
            output.write(PcmByteUtils.toByteArray(16, bigEndian = false))
            output.write(PcmByteUtils.toByteArray(PCM_AUDIO_FORMAT.toShort(), bigEndian = false))
            output.write(PcmByteUtils.toByteArray(format.channels.toShort(), bigEndian = false))
            output.write(PcmByteUtils.toByteArray(format.sampleRate, bigEndian = false))
            val byteRate = format.channels * format.sampleRate * format.bytesRequiredPerSample
            output.write(PcmByteUtils.toByteArray(byteRate, bigEndian = false))
            output.write(PcmByteUtils.toByteArray((format.channels * format.bytesRequiredPerSample).toShort(), bigEndian = false))
            output.write(PcmByteUtils.toByteArray(format.sampleSizeInBits.toShort(), bigEndian = false))

            output.write(PcmByteUtils.toByteArray(DATA_CHUNK_ID, bigEndian = true))
            output.write(PcmByteUtils.toByteArray(totalSamplesInByte, bigEndian = false))
            output.toByteArray()
        } catch (io: IOException) {
            ByteArray(0)
        } finally {
            PcmByteUtils.closeQuietly(output)
        }
    }

    companion object {
        const val PCM_RIFF_HEADER_SIZE = 44
        const val RIFF_CHUNK_SIZE_INDEX = 4
        const val RIFF_SUBCHUNK2_SIZE_INDEX = 40

        private const val RIFF_CHUNK_ID = 0x52494646
        private const val WAVE_FORMAT_ID = 0x57415645
        private const val FMT_CHUNK_ID = 0x666d7420
        private const val DATA_CHUNK_ID = 0x64617461
        private const val PCM_AUDIO_FORMAT = 1

        private fun readHeader(stream: DataInputStream): Pair<PcmAudioFormat, Int> {
            return try {
                val buffer4 = ByteArray(4)
                val buffer2 = ByteArray(2)

                stream.skipBytes(4 + 4 + 4 + 4 + 4 + 2)

                stream.readFully(buffer2)
                val channels = littleEndianShort(buffer2)

                stream.readFully(buffer4)
                val sampleRate = littleEndianInt(buffer4)

                stream.skipBytes(4 + 2)

                stream.readFully(buffer2)
                val sampleSizeInBits = littleEndianShort(buffer2)

                stream.skipBytes(4)

                stream.readFully(buffer4)
                val totalSamplesInByte = littleEndianInt(buffer4)

                val format = WavAudioFormat.Builder()
                    .channels(channels)
                    .sampleRate(sampleRate)
                    .sampleSizeInBits(sampleSizeInBits)
                    .build()
                format to totalSamplesInByte
            } finally {
                PcmByteUtils.closeQuietly(stream)
            }
        }

        private fun littleEndianShort(bytes: ByteArray): Int {
            require(bytes.size >= 2)
            return (bytes[1].toInt() shl 8) or (bytes[0].toInt() and 0xFF)
        }

        private fun littleEndianInt(bytes: ByteArray): Int {
            require(bytes.size >= 4)
            return (bytes[3].toInt() shl 24) or
                ((bytes[2].toInt() and 0xFF) shl 16) or
                ((bytes[1].toInt() and 0xFF) shl 8) or
                (bytes[0].toInt() and 0xFF)
        }
    }
}

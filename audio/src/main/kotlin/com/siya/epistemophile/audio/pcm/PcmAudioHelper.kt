package com.siya.epistemophile.audio.pcm

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.RandomAccessFile

object PcmAudioHelper {

    @Throws(IOException::class)
    fun convertRawToWav(format: WavAudioFormat, rawSource: File, wavTarget: File) {
        DataOutputStream(FileOutputStream(wavTarget)).use { dos ->
            dos.write(RiffHeaderData(format, 0).asByteArray())
            DataInputStream(FileInputStream(rawSource)).use { dis ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var read: Int
                var total = 0
                while (dis.read(buffer).also { read = it } != -1) {
                    total += read
                    dos.write(buffer, 0, read)
                }
                modifyRiffSizeData(wavTarget, total)
            }
        }
    }

    @Throws(IOException::class)
    fun convertWavToRaw(wavSource: File, rawTarget: File) {
        PcmByteUtils.copy(MonoWavFileReader(wavSource).newStream(), FileOutputStream(rawTarget))
    }

    @Throws(IOException::class)
    fun readAllFromWavNormalized(fileName: String): DoubleArray {
        return MonoWavFileReader(File(fileName)).newStream().use { it.readSamplesNormalized() }
    }

    @Throws(IOException::class)
    fun modifyRiffSizeData(wavFile: File, size: Int) {
        RandomAccessFile(wavFile, "rw").use { raf ->
            raf.seek(RiffHeaderData.RIFF_CHUNK_SIZE_INDEX.toLong())
            raf.write(PcmByteUtils.toByteArray(size + 36, bigEndian = false))
            raf.seek(RiffHeaderData.RIFF_SUBCHUNK2_SIZE_INDEX.toLong())
            raf.write(PcmByteUtils.toByteArray(size, bigEndian = false))
        }
    }

    @Throws(IOException::class)
    fun generateSilenceWavFile(wavAudioFormat: WavAudioFormat, file: File, seconds: Double) {
        WavFileWriter(wavAudioFormat, file).use { writer ->
            val empty = IntArray((seconds * wavAudioFormat.sampleRate).toInt())
            writer.write(empty)
        }
    }

    private const val DEFAULT_BUFFER_SIZE = 4096
}

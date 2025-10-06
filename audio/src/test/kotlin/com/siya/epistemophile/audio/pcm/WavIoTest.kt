package com.siya.epistemophile.audio.pcm

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files

class WavIoTest {

    @Test
    fun `write and read wav round trip`() {
        val format = WavAudioFormat.mono16Bit(44100)
        val samples = intArrayOf(0, 1000, -1000, 32767, -32768)
        val wavFile = Files.createTempFile("roundtrip", ".wav").toFile()
        try {
            WavFileWriter(format, wavFile).use { writer ->
                writer.write(samples)
            }

            val reader = MonoWavFileReader(wavFile)
            assertEquals(samples.size, reader.getSampleCount())
            assertEquals(format.sampleRate, reader.getFormat().sampleRate)
            val readSamples = reader.getAllSamples()
            assertArrayEquals(samples, readSamples)
        } finally {
            wavFile.delete()
        }
    }

    @Test
    fun `convert raw pcm to wav`() {
        val format = WavAudioFormat.mono16Bit(22050)
        val rawFile = Files.createTempFile("raw", ".pcm").toFile()
        val wavFile = Files.createTempFile("converted", ".wav").toFile()
        try {
            val rawSamples = intArrayOf(0, 2000, -2000, 1500)
            FileOutputStream(rawFile).use { output ->
                output.write(PcmByteUtils.toByteArray(rawSamples, rawSamples.size, format.bytesRequiredPerSample, format.bigEndian))
            }

            PcmAudioHelper.convertRawToWav(format, rawFile, wavFile)

            val reader = MonoWavFileReader(wavFile)
            assertEquals(rawSamples.size, reader.getSampleCount())
            assertArrayEquals(rawSamples, reader.getAllSamples())
        } finally {
            rawFile.delete()
            wavFile.delete()
        }
    }

    @Test
    fun `normalize samples produces expected amplitude`() {
        val format = PcmAudioFormat.mono16BitSignedLittleEndian(8000)
        val intSamples = intArrayOf(0, 32767, -32768)
        val byteArray = PcmByteUtils.toByteArray(intSamples, intSamples.size, format.bytesRequiredPerSample, format.bigEndian)
        val stream = PcmMonoInputStream(format, ByteArrayInputStream(byteArray))
        val normalized = stream.readSamplesNormalized(intSamples.size)
        assertEquals(3, normalized.size)
        assertEquals(0.0, normalized[0], 1e-6)
        assertEquals(1.0, normalized[1], 1e-3)
        assertEquals(-1.0, normalized[2], 1e-3)
    }
}

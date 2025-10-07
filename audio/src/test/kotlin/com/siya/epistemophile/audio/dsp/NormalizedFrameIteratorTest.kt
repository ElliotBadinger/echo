package com.siya.epistemophile.audio.dsp

import com.siya.epistemophile.audio.pcm.PcmAudioFormat
import com.siya.epistemophile.audio.pcm.PcmByteUtils
import com.siya.epistemophile.audio.pcm.PcmMonoInputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream

class NormalizedFrameIteratorTest {

    @Test
    fun `iterator yields overlapping frames`() {
        val format = PcmAudioFormat.mono16BitSignedLittleEndian(8000)
        val samples = intArrayOf(0, 1000, 2000, 3000, 4000, 5000)
        val bytes = PcmByteUtils.toByteArray(samples, samples.size, format.bytesRequiredPerSample, format.bigEndian)
        val stream = PcmMonoInputStream(format, ByteArrayInputStream(bytes))
        val iterator = NormalizedFrameIterator(stream, frameSize = 4, shiftAmount = 2, applyPadding = false)

        val frames = mutableListOf<DoubleArray>()
        while (iterator.hasNext()) {
            frames += iterator.next().data
        }

        assertEquals(2, frames.size)
        val normalization = 0x7fff.toDouble()
        val expectedFirst = doubleArrayOf(0.0, 1000 / normalization, 2000 / normalization, 3000 / normalization)
        val expectedSecond = doubleArrayOf(2000 / normalization, 3000 / normalization, 4000 / normalization, 5000 / normalization)
        frames[0].forEachIndexed { index, value ->
            assertEquals(expectedFirst[index], value, 1e-3)
        }
        frames[1].forEachIndexed { index, value ->
            assertEquals(expectedSecond[index], value, 1e-3)
        }
    }

    @Test
    fun `iterator pads final frame when requested`() {
        val format = PcmAudioFormat.mono16BitSignedLittleEndian(8000)
        val samples = intArrayOf(1000, 2000, 3000)
        val bytes = PcmByteUtils.toByteArray(samples, samples.size, format.bytesRequiredPerSample, format.bigEndian)
        val stream = PcmMonoInputStream(format, ByteArrayInputStream(bytes))
        val iterator = NormalizedFrameIterator(stream, frameSize = 4, shiftAmount = 2, applyPadding = true)

        assertTrue(iterator.hasNext())
        val first = iterator.next().data
        assertEquals(4, first.size)
        assertEquals(1000 / 0x7fff.toDouble(), first[0], 1e-3)
        assertEquals(2000 / 0x7fff.toDouble(), first[1], 1e-3)
        assertEquals(3000 / 0x7fff.toDouble(), first[2], 1e-3)
        assertEquals(0.0, first[3], 1e-6)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `iterator rejects shift larger than frame size`() {
        val format = PcmAudioFormat.mono16BitSignedLittleEndian(8000)
        val stream = PcmMonoInputStream(format, ByteArrayInputStream(ByteArray(0)))

        NormalizedFrameIterator(stream, frameSize = 2, shiftAmount = 3, applyPadding = false)
    }
}

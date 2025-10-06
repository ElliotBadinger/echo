package com.siya.epistemophile.audio.dsp

import org.junit.Assert.assertArrayEquals
import org.junit.Test
import kotlin.math.PI
import kotlin.math.cos

class WindowerFactoryTest {

    @Test
    fun `hamming window applies expected coefficients`() {
        val processor = WindowerFactory.newHammingWindower(4)
        val input = DoubleVector(DoubleArray(4) { 1.0 })
        val output = processor.process(input).data
        val expected = coefficients(alpha = 0.46, length = 4)
        assertArrayEquals(expected, output, 1e-6)
    }

    @Test
    fun `hanning window processes in place`() {
        val processor = WindowerFactory.newHanningWindower(4)
        val vector = DoubleVector(DoubleArray(4) { 1.0 })
        processor.processInPlace(vector)
        val expected = coefficients(alpha = 0.5, length = 4)
        assertArrayEquals(expected, vector.data, 1e-6)
    }

    @Test
    fun `triangular window returns unity when length is one`() {
        val processor = WindowerFactory.newTriangularWindower(1)
        val vector = DoubleVector(doubleArrayOf(2.0))
        val output = processor.process(vector)
        assertArrayEquals(doubleArrayOf(2.0), output.data, 1e-6)
    }

    private fun coefficients(alpha: Double, length: Int): DoubleArray {
        val result = DoubleArray(length)
        for (i in 0 until length) {
            result[i] = if (length == 1) {
                1.0
            } else {
                (1 - alpha) - alpha * cos(2 * PI * i / (length - 1.0))
            }
        }
        return result
    }
}

package com.siya.epistemophile.audio.dsp

import kotlin.math.PI
import kotlin.math.cos

object WindowerFactory {

    private class RaisedCosineWindower(private val alpha: Double, length: Int) : DoubleVectorProcessor {
        private val cosineWindow: DoubleArray = DoubleArray(length).also { window ->
            require(length > 0) { "Window length cannot be smaller than 1" }
            for (i in window.indices) {
                window[i] = if (length == 1) {
                    1.0
                } else {
                    (1 - alpha) - alpha * cos(2 * PI * i / (length - 1.0))
                }
            }
        }

        override fun process(input: DoubleVector): DoubleVector {
            val result = DoubleArray(input.data.size)
            for (i in input.data.indices) {
                result[i] = input.data[i] * cosineWindow[i]
            }
            return DoubleVector(result)
        }

        override fun processInPlace(input: DoubleVector) {
            for (i in input.data.indices) {
                input.data[i] *= cosineWindow[i]
            }
        }
    }

    fun newHammingWindower(length: Int): DoubleVectorProcessor = RaisedCosineWindower(0.46, length)

    fun newHanningWindower(length: Int): DoubleVectorProcessor = RaisedCosineWindower(0.5, length)

    fun newTriangularWindower(length: Int): DoubleVectorProcessor = RaisedCosineWindower(0.0, length)
}

package com.siya.epistemophile.audio.dsp

import com.siya.epistemophile.audio.pcm.PcmMonoInputStream
import java.io.IOException

class NormalizedFrameIterator(
    private val inputStream: PcmMonoInputStream,
    private val frameSize: Int,
    private val shiftAmount: Int,
    private val applyPadding: Boolean
) : Iterator<DoubleVector> {

    private var currentFrame: DoubleVector? = null
    private var frameCounter = 0

    init {
        require(frameSize > 0) { "Frame size must be larger than zero." }
        require(shiftAmount > 0) { "Shift size must be larger than zero." }
        require(shiftAmount <= frameSize) { "Shift size cannot exceed frame size." }
    }

    override fun hasNext(): Boolean {
        val data: DoubleArray = try {
            if (frameCounter == 0) {
                val first = inputStream.readSamplesNormalized(frameSize)
                if (first.size < frameSize) {
                    if (applyPadding && first.isNotEmpty()) {
                        padFrame(first, frameSize)
                    } else {
                        return false
                    }
                } else {
                    first
                }
            } else {
                val next = inputStream.readSamplesNormalized(shiftAmount)
                if (next.size < shiftAmount) {
                    if (applyPadding && next.isNotEmpty()) {
                        next + DoubleArray(shiftAmount - next.size)
                    } else {
                        return false
                    }
                } else {
                    next
                }
            }
        } catch (e: IOException) {
            return false
        }

        currentFrame = if (frameCounter == 0) {
            DoubleVector(data)
        } else {
            val previous = currentFrame!!.data.clone()
            val preserved = previous.size - shiftAmount
            if (preserved > 0) {
                System.arraycopy(previous, shiftAmount, previous, 0, preserved)
            }
            System.arraycopy(data, 0, previous, preserved, data.size)
            DoubleVector(previous)
        }
        frameCounter++
        return true
    }

    override fun next(): DoubleVector {
        return currentFrame ?: throw NoSuchElementException()
    }

    private fun padFrame(data: DoubleArray, targetSize: Int): DoubleArray {
        val result = DoubleArray(targetSize)
        System.arraycopy(data, 0, result, 0, data.size)
        return result
    }
}

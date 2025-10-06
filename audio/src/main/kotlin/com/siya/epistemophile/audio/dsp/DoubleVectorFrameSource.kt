package com.siya.epistemophile.audio.dsp

import com.siya.epistemophile.audio.pcm.PcmMonoInputStream

class DoubleVectorFrameSource private constructor(
    private val inputStream: PcmMonoInputStream,
    private val frameSize: Int,
    private val shiftAmount: Int,
    private val paddingApplied: Boolean
) {

    fun getIterableFrameReader(): Iterable<DoubleVector> = Iterable {
        NormalizedFrameIterator(inputStream, frameSize, shiftAmount, paddingApplied)
    }

    fun getNormalizedFrameIterator(): Iterator<DoubleVector> {
        return NormalizedFrameIterator(inputStream, frameSize, shiftAmount, paddingApplied)
    }

    companion object {
        fun fromSampleAmount(
            inputStream: PcmMonoInputStream,
            frameSize: Int,
            shiftAmount: Int
        ): DoubleVectorFrameSource = DoubleVectorFrameSource(inputStream, frameSize, shiftAmount, false)

        fun fromSampleAmountWithPadding(
            inputStream: PcmMonoInputStream,
            frameSize: Int,
            shiftAmount: Int
        ): DoubleVectorFrameSource = DoubleVectorFrameSource(inputStream, frameSize, shiftAmount, true)

        fun fromSizeInMilliseconds(
            inputStream: PcmMonoInputStream,
            frameSizeInMillis: Double,
            shiftAmountInMillis: Double
        ): DoubleVectorFrameSource {
            val frameSize = inputStream.format.sampleCountForMilliseconds(frameSizeInMillis)
            val shift = inputStream.format.sampleCountForMilliseconds(shiftAmountInMillis)
            return DoubleVectorFrameSource(inputStream, frameSize, shift, false)
        }

        fun fromSizeInMillisecondsWithPadding(
            inputStream: PcmMonoInputStream,
            frameSizeInMillis: Double,
            shiftAmountInMillis: Double
        ): DoubleVectorFrameSource {
            val frameSize = inputStream.format.sampleCountForMilliseconds(frameSizeInMillis)
            val shift = inputStream.format.sampleCountForMilliseconds(shiftAmountInMillis)
            return DoubleVectorFrameSource(inputStream, frameSize, shift, true)
        }
    }
}

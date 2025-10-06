package com.siya.epistemophile.audio.dsp

interface DoubleVectorProcessor {
    fun process(input: DoubleVector): DoubleVector

    fun processInPlace(input: DoubleVector)
}

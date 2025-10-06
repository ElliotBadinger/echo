package com.siya.epistemophile.audio.dsp

class DoubleVectorProcessingPipeline(
    private val vectorSource: Iterator<DoubleVector>,
    private val processors: List<DoubleVectorProcessor>
) : Iterator<DoubleVector> {

    override fun hasNext(): Boolean = vectorSource.hasNext()

    override fun next(): DoubleVector {
        var vector = vectorSource.next()
        processors.forEach { processor ->
            vector = processor.process(vector)
        }
        return vector
    }
}

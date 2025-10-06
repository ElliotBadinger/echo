package com.siya.epistemophile.audio.dsp

class DoubleVector(val data: DoubleArray) {

    init {
        require(data.isNotEmpty()) { "Data cannot be empty." }
    }

    fun size(): Int = data.size

    override fun toString(): String = data.joinToString(prefix = "[", postfix = "]")
}

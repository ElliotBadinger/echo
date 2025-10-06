package com.siya.epistemophile.audio.dsp

class MutableComplex(var real: Double, var imaginary: Double) {

    constructor(complex: Complex) : this(complex.real, complex.imaginary)

    fun toImmutable(): Complex = Complex(real, imaginary)
}

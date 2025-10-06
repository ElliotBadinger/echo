package com.siya.epistemophile.audio.pcm

open class PcmAudioFormat(
    val sampleRate: Int,
    val sampleSizeInBits: Int,
    val channels: Int,
    val bigEndian: Boolean,
    val signed: Boolean
) {

    val bytesRequiredPerSample: Int = if (sampleSizeInBits % 8 == 0) {
        sampleSizeInBits / 8
    } else {
        sampleSizeInBits / 8 + 1
    }

    init {
        require(sampleRate > 0) { "sampleRate cannot be less than one. But it is:$sampleRate" }
        require(sampleSizeInBits in 2..31) {
            "sampleSizeInBits must be between (including) 2-31. But it is:$sampleSizeInBits"
        }
        require(channels in 1..2) { "channels must be 1 or 2. But it is:$channels" }
    }

    fun sampleCountForMilliseconds(milliseconds: Double): Int {
        return (sampleRate * milliseconds / 1000.0).toInt()
    }

    override fun toString(): String {
        return "[ Sample Rate:$sampleRate , SampleSizeInBits:$sampleSizeInBits, channels:$channels, signed:$signed, bigEndian:$bigEndian ]"
    }

    class Builder(private val sampleRate: Int) {
        private var sampleSizeInBits: Int = 16
        private var channels: Int = 1
        private var bigEndian: Boolean = false
        private var signed: Boolean = true

        fun channels(channels: Int) = apply { this.channels = channels }

        fun bigEndian() = apply { this.bigEndian = true }

        fun unsigned() = apply { this.signed = false }

        fun sampleSizeInBits(bits: Int) = apply { this.sampleSizeInBits = bits }

        fun build(): PcmAudioFormat = PcmAudioFormat(sampleRate, sampleSizeInBits, channels, bigEndian, signed)
    }

    companion object {
        fun mono16BitSignedLittleEndian(sampleRate: Int): PcmAudioFormat =
            PcmAudioFormat(sampleRate, 16, 1, bigEndian = false, signed = true)
    }
}

package com.siya.epistemophile.audio.pcm

class WavAudioFormat private constructor(
    sampleRate: Int,
    sampleSizeInBits: Int,
    channels: Int,
    signed: Boolean
) : PcmAudioFormat(sampleRate, sampleSizeInBits, channels, bigEndian = false, signed = signed) {

    class Builder {
        private var sampleRate: Int = 0
        private var sampleSizeInBits: Int = 16
        private var channels: Int = 1

        fun sampleRate(sampleRate: Int) = apply { this.sampleRate = sampleRate }

        fun channels(channels: Int) = apply { this.channels = channels }

        fun sampleSizeInBits(bits: Int) = apply { this.sampleSizeInBits = bits }

        fun build(): WavAudioFormat {
            val isSigned = sampleSizeInBits != 8
            return WavAudioFormat(sampleRate, sampleSizeInBits, channels, isSigned)
        }
    }

    companion object {
        fun mono16Bit(sampleRate: Int): WavAudioFormat = WavAudioFormat(sampleRate, 16, 1, true)

        fun wavFormat(sampleRate: Int, sampleSizeInBits: Int, channels: Int): WavAudioFormat {
            val signed = sampleSizeInBits != 8
            return WavAudioFormat(sampleRate, sampleSizeInBits, channels, signed)
        }
    }
}

package com.siya.epistemophile.audio.pcm

import java.io.File
import java.io.FileInputStream
import java.io.IOException

class MonoWavFileReader(private val file: File) {

    private val riffHeaderData = RiffHeaderData(file)

    init {
        require(riffHeaderData.format.channels == 1) { "Wav file is not Mono." }
    }

    @Throws(IOException::class)
    fun newStream(): PcmMonoInputStream {
        val stream = PcmMonoInputStream(riffHeaderData.format, FileInputStream(file))
        val skipped = stream.skip(RiffHeaderData.PCM_RIFF_HEADER_SIZE)
        require(skipped >= RiffHeaderData.PCM_RIFF_HEADER_SIZE) {
            "cannot skip necessary amount of bytes from underlying stream."
        }
        return stream
    }

    @Throws(IOException::class)
    fun getAllSamples(): IntArray {
        return newStream().use { it.readAll() }
    }

    @Throws(IOException::class)
    fun getSamplesAsInts(frameStart: Int, frameEnd: Int): IntArray {
        require(frameStart >= 0) { "Start Frame cannot be negative:$frameStart" }
        require(frameEnd >= frameStart) { "Start Frame cannot be after end frame. Start:$frameStart, end:$frameEnd" }
        require(frameEnd <= riffHeaderData.sampleCount) {
            "Frame count out of bounds. Max sample count:${riffHeaderData.sampleCount} but frame is:$frameEnd"
        }
        return newStream().use { stream ->
            stream.skipSamples(frameStart)
            stream.readSamplesAsIntArray(frameEnd - frameStart)
        }
    }

    fun getFormat(): PcmAudioFormat = riffHeaderData.format

    fun getSampleCount(): Int = riffHeaderData.sampleCount

    fun getFile(): File = file
}

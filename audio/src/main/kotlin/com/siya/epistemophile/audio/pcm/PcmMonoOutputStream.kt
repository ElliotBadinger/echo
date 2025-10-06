package com.siya.epistemophile.audio.pcm

import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

class PcmMonoOutputStream(
    private val format: PcmAudioFormat,
    private val dataOutput: DataOutputStream
) : OutputStream() {

    constructor(format: PcmAudioFormat, file: File) : this(
        format,
        DataOutputStream(FileOutputStream(file))
    )

    @Throws(IOException::class)
    override fun write(b: Int) {
        dataOutput.write(b)
    }

    @Throws(IOException::class)
    override fun write(buffer: ByteArray, offset: Int, count: Int) {
        dataOutput.write(buffer, offset, count)
    }

    @Throws(IOException::class)
    fun write(values: ShortArray) {
        val bytes = PcmByteUtils.toByteArray(values, values.size, format.bigEndian)
        dataOutput.write(bytes)
    }

    @Throws(IOException::class)
    fun write(values: IntArray) {
        val bytes = PcmByteUtils.toByteArray(values, values.size, format.bytesRequiredPerSample, format.bigEndian)
        dataOutput.write(bytes)
    }

    override fun close() {
        PcmByteUtils.closeQuietly(dataOutput)
    }
}

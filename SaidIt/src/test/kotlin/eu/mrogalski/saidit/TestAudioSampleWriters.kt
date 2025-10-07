package eu.mrogalski.saidit

import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class FileBackedSampleWriter(private val target: File) : AudioSampleWriter {
    private val output = FileOutputStream(target)
    private var written = 0

    @Throws(IOException::class)
    override fun write(data: ByteArray, offset: Int, length: Int) {
        output.write(data, offset, length)
        written += length
    }

    override fun close() {
        output.flush()
        output.close()
    }

    override fun getTotalSampleBytesWritten(): Int = written
}

class FailingAudioSampleWriter(
    private val failure: IOException = IOException("Deliberate write failure")
) : AudioSampleWriter {

    @Throws(IOException::class)
    override fun write(data: ByteArray, offset: Int, length: Int) {
        throw failure
    }

    override fun close() {
        // no-op
    }

    override fun getTotalSampleBytesWritten(): Int = 0
}

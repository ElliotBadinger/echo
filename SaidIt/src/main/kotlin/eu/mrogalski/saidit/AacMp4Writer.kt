package eu.mrogalski.saidit

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.util.Log
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import javax.inject.Inject
import kotlin.math.min

/**
 * Factory interface for creating MediaCodec instances.
 * Enables dependency injection and proper testing.
 */
interface MediaCodecFactory {
    fun createEncoder(mimeType: String): MediaCodec
}

/**
 * Factory interface for creating MediaMuxer instances.
 * Enables dependency injection and proper testing.
 */
interface MediaMuxerFactory {
    fun createMuxer(outputPath: String, format: Int): MediaMuxer
}

/**
 * Default implementation of MediaCodecFactory.
 */
class DefaultMediaCodecFactory @Inject constructor() : MediaCodecFactory {
    override fun createEncoder(mimeType: String): MediaCodec {
        return MediaCodec.createEncoderByType(mimeType)
    }
}

/**
 * Default implementation of MediaMuxerFactory.
 */
class DefaultMediaMuxerFactory @Inject constructor() : MediaMuxerFactory {
    override fun createMuxer(outputPath: String, format: Int): MediaMuxer {
        return MediaMuxer(outputPath, format)
    }
}

/**
 * Encodes PCM 16-bit mono to AAC-LC and writes into an MP4 (.m4a) container.
 * Thread-safe for single-producer usage on an audio thread.
 * 
 * Modern Kotlin implementation with dependency injection for testability.
 * Uses proper resource management and error handling.
 * 
 * @param sampleRate Audio sample rate in Hz
 * @param channelCount Number of audio channels
 * @param bitRate Audio bit rate in bits per second
 * @param outFile Output MP4 file
 * @param codecFactory Factory for creating MediaCodec instances (injectable for testing)
 * @param muxerFactory Factory for creating MediaMuxer instances (injectable for testing)
 */
class AacMp4Writer(
    private val sampleRate: Int,
    private val channelCount: Int,
    bitRate: Int,
    outFile: File,
    private val codecFactory: MediaCodecFactory = DefaultMediaCodecFactory(),
    private val muxerFactory: MediaMuxerFactory = DefaultMediaMuxerFactory()
) : AudioSampleWriter {
    
    companion object {
        private const val TAG = "AacMp4Writer"
        private const val MIME_TYPE = MediaFormat.MIMETYPE_AUDIO_AAC // "audio/mp4a-latm"
        private const val PCM_BYTES_PER_SAMPLE = 2 // 16-bit PCM
        private const val TIMEOUT_US = 10000L
    }

    private val encoder: MediaCodec
    private val muxer: MediaMuxer
    private val bufferInfo = MediaCodec.BufferInfo()

    private var muxerStarted = false
    private var trackIndex = -1
    private var totalPcmBytesWritten = 0L
    private var ptsUs = 0L // Monotonic presentation time in microseconds for input samples

    init {
        try {
            val format = MediaFormat.createAudioFormat(MIME_TYPE, sampleRate, channelCount).apply {
                setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
                setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
                setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 16384)
            }

            encoder = codecFactory.createEncoder(MIME_TYPE).apply {
                configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
                start()
            }

            muxer = muxerFactory.createMuxer(outFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        } catch (e: Exception) {
            // Ensure cleanup if initialization fails
            runCatching { encoder.release() }
            runCatching { muxer.release() }
            throw IOException("Failed to initialize AacMp4Writer", e)
        }
    }

    /**
     * Writes PCM audio data to the encoder.
     * 
     * @param data PCM audio data buffer
     * @param offset Starting offset in the data buffer
     * @param length Number of bytes to write
     * @throws IOException if encoding or muxing fails
     */
    @Throws(IOException::class)
    override fun write(data: ByteArray, offset: Int, length: Int) {
        var remaining = length
        var off = offset
        
        while (remaining > 0) {
            val inIndex = encoder.dequeueInputBuffer(TIMEOUT_US)
            if (inIndex >= 0) {
                val inBuf = encoder.getInputBuffer(inIndex)
                if (inBuf != null) {
                    inBuf.clear()
                    val toCopy = min(remaining, inBuf.remaining())
                    inBuf.put(data, off, toCopy)
                    
                    val inputPts = ptsUs
                    val sampleCount = toCopy / PCM_BYTES_PER_SAMPLE / channelCount
                    ptsUs += (sampleCount * 1_000_000L) / sampleRate
                    
                    encoder.queueInputBuffer(inIndex, 0, toCopy, inputPts, 0)
                    off += toCopy
                    remaining -= toCopy
                    totalPcmBytesWritten += toCopy
                }
            } else {
                // No input buffer available, try draining and retry
                drainEncoder(endOfStream = false)
            }
        }
        drainEncoder(endOfStream = false)
    }

    /**
     * Drains the encoder output and writes to muxer.
     * 
     * @param endOfStream true if this is the final drain call
     * @throws IOException if muxing fails
     */
    @Throws(IOException::class)
    private fun drainEncoder(endOfStream: Boolean) {
        if (endOfStream) {
            val inIndex = encoder.dequeueInputBuffer(TIMEOUT_US)
            if (inIndex >= 0) {
                encoder.queueInputBuffer(inIndex, 0, 0, ptsUs, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
            }
            // If we couldn't queue EOS now, we'll retry on next drain
        }
        
        while (true) {
            val outIndex = encoder.dequeueOutputBuffer(bufferInfo, TIMEOUT_US)
            when {
                outIndex == MediaCodec.INFO_TRY_AGAIN_LATER -> {
                    if (!endOfStream) break
                }
                outIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                    if (muxerStarted) {
                        throw IllegalStateException("Format changed twice")
                    }
                    val newFormat = encoder.outputFormat
                    trackIndex = muxer.addTrack(newFormat)
                    muxer.start()
                    muxerStarted = true
                }
                outIndex >= 0 -> {
                    val outBuf = encoder.getOutputBuffer(outIndex)
                    if (outBuf != null && bufferInfo.size > 0) {
                        outBuf.position(bufferInfo.offset)
                        outBuf.limit(bufferInfo.offset + bufferInfo.size)
                        
                        if (!muxerStarted) {
                            // This should not happen, but guard anyway
                            Log.w(TAG, "Muxer not started when output available, dropping frame")
                        } else {
                            muxer.writeSampleData(trackIndex, outBuf, bufferInfo)
                        }
                    }
                    encoder.releaseOutputBuffer(outIndex, false)
                    
                    if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        break
                    }
                }
            }
        }
    }

    /**
     * Closes the encoder and muxer, finalizing the output file.
     * Safe to call multiple times.
     */
    override fun close() {
        if (totalPcmBytesWritten == 0L) {
            Log.w(TAG, "close() called without any data written. Muxer will not be finalized.")
        } else {
            runCatching {
                drainEncoder(endOfStream = true)
            }.onFailure { e ->
                Log.e(TAG, "Error finishing encoder", e)
            }
        }
        
        // Clean up encoder
        runCatching { encoder.stop() }
        runCatching { encoder.release() }
        
        // Clean up muxer
        runCatching {
            if (muxerStarted) {
                muxer.stop()
            }
        }
        runCatching { muxer.release() }
    }

    /**
     * Returns the total number of PCM sample bytes written.
     * Safe cast for typical audio file sizes.
     */
    override fun getTotalSampleBytesWritten(): Int {
        return minOf(Int.MAX_VALUE.toLong(), totalPcmBytesWritten).toInt()
    }
}
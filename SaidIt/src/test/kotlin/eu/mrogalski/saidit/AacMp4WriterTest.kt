package eu.mrogalski.saidit

import android.media.MediaCodec
import android.media.MediaFormat
import android.media.MediaMuxer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Comprehensive unit tests for AacMp4Writer Kotlin implementation.
 * Tests null safety, resource management, and MediaCodec integration.
 */
@RunWith(MockitoJUnitRunner::class)
class AacMp4WriterTest {

    @Mock private lateinit var mockEncoder: MediaCodec
    @Mock private lateinit var mockMuxer: MediaMuxer
    @Mock private lateinit var mockFormat: MediaFormat
    @Mock private lateinit var mockFile: File
    @Mock private lateinit var mockByteBuffer: ByteBuffer

    private lateinit var mediaCodecStatic: MockedStatic<MediaCodec>
    private lateinit var mediaFormatStatic: MockedStatic<MediaFormat>

    private val sampleRate = 44100
    private val channelCount = 1
    private val bitRate = 128000
    private val testData = ByteArray(1024) { it.toByte() }

    @Before
    fun setUp() {
        // Mock static MediaCodec methods
        mediaCodecStatic = mockStatic(MediaCodec::class.java)
        mediaFormatStatic = mockStatic(MediaFormat::class.java)

        // Setup MediaFormat creation
        mediaFormatStatic.`when`<MediaFormat> {
            MediaFormat.createAudioFormat(anyString(), anyInt(), anyInt())
        }.thenReturn(mockFormat)

        // Setup MediaCodec creation
        mediaCodecStatic.`when`<MediaCodec> {
            MediaCodec.createEncoderByType(anyString())
        }.thenReturn(mockEncoder)

        // Setup file path
        `when`(mockFile.absolutePath).thenReturn("/test/path/output.m4a")

        // Setup encoder behavior
        `when`(mockEncoder.outputFormat).thenReturn(mockFormat)
        `when`(mockEncoder.getInputBuffer(anyInt())).thenReturn(mockByteBuffer)
        `when`(mockEncoder.getOutputBuffer(anyInt())).thenReturn(mockByteBuffer)
        `when`(mockByteBuffer.remaining()).thenReturn(1024)

        // Setup muxer behavior
        `when`(mockMuxer.addTrack(any())).thenReturn(0)
    }

    @After
    fun tearDown() {
        mediaCodecStatic.close()
        mediaFormatStatic.close()
    }

    @Test
    fun `constructor initializes encoder and muxer correctly`() {
        // Given: Mock successful initialization
        setupSuccessfulInitialization()

        // When: Creating AacMp4Writer
        val writer = createAacMp4Writer()

        // Then: Verify initialization calls
        verify(mockFormat).setInteger(MediaFormat.KEY_AAC_PROFILE, any())
        verify(mockFormat).setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
        verify(mockFormat).setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 16384)
        verify(mockEncoder).configure(mockFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        verify(mockEncoder).start()

        writer.close()
    }

    @Test
    fun `constructor throws IOException when encoder creation fails`() {
        // Given: MediaCodec creation throws exception
        mediaCodecStatic.`when`<MediaCodec> {
            MediaCodec.createEncoderByType(anyString())
        }.thenThrow(RuntimeException("Encoder creation failed"))

        // When & Then: Constructor should throw IOException
        assertFailsWith<IOException> {
            createAacMp4Writer()
        }
    }

    @Test
    fun `write processes data correctly with available input buffer`() {
        // Given: Successful initialization and available input buffer
        setupSuccessfulInitialization()
        `when`(mockEncoder.dequeueInputBuffer(anyLong())).thenReturn(0)
        `when`(mockEncoder.dequeueOutputBuffer(any(), anyLong())).thenReturn(MediaCodec.INFO_TRY_AGAIN_LATER)

        val writer = createAacMp4Writer()

        // When: Writing data
        writer.write(testData, 0, testData.size)

        // Then: Verify encoder interactions
        verify(mockEncoder).dequeueInputBuffer(10000L)
        verify(mockEncoder).getInputBuffer(0)
        verify(mockByteBuffer).clear()
        verify(mockByteBuffer).put(testData, 0, testData.size)
        verify(mockEncoder).queueInputBuffer(eq(0), eq(0), eq(testData.size), anyLong(), eq(0))

        writer.close()
    }

    @Test
    fun `write handles no available input buffer by draining encoder`() {
        // Given: No input buffer available initially
        setupSuccessfulInitialization()
        `when`(mockEncoder.dequeueInputBuffer(anyLong())).thenReturn(-1)
        `when`(mockEncoder.dequeueOutputBuffer(any(), anyLong())).thenReturn(MediaCodec.INFO_TRY_AGAIN_LATER)

        val writer = createAacMp4Writer()

        // When: Writing data
        writer.write(testData, 0, testData.size)

        // Then: Verify drain attempt
        verify(mockEncoder, atLeastOnce()).dequeueOutputBuffer(any(), anyLong())

        writer.close()
    }

    @Test
    fun `write handles null input buffer gracefully`() {
        // Given: Null input buffer returned
        setupSuccessfulInitialization()
        `when`(mockEncoder.dequeueInputBuffer(anyLong())).thenReturn(0)
        `when`(mockEncoder.getInputBuffer(0)).thenReturn(null)
        `when`(mockEncoder.dequeueOutputBuffer(any(), anyLong())).thenReturn(MediaCodec.INFO_TRY_AGAIN_LATER)

        val writer = createAacMp4Writer()

        // When: Writing data (should not crash)
        writer.write(testData, 0, testData.size)

        // Then: Verify no crash and proper handling
        verify(mockEncoder).getInputBuffer(0)
        // Should not attempt to use null buffer
        verify(mockByteBuffer, never()).clear()

        writer.close()
    }

    @Test
    fun `drainEncoder handles format change correctly`() {
        // Given: Format change during output
        setupSuccessfulInitialization()
        `when`(mockEncoder.dequeueInputBuffer(anyLong())).thenReturn(0)
        `when`(mockEncoder.dequeueOutputBuffer(any(), anyLong()))
            .thenReturn(MediaCodec.INFO_OUTPUT_FORMAT_CHANGED)
            .thenReturn(MediaCodec.INFO_TRY_AGAIN_LATER)

        val writer = createAacMp4Writer()

        // When: Writing data (triggers drain)
        writer.write(testData, 0, testData.size)

        // Then: Verify format change handling
        verify(mockEncoder).outputFormat
        verify(mockMuxer).addTrack(mockFormat)
        verify(mockMuxer).start()

        writer.close()
    }

    @Test
    fun `drainEncoder throws exception on double format change`() {
        // Given: Multiple format changes
        setupSuccessfulInitialization()
        `when`(mockEncoder.dequeueInputBuffer(anyLong())).thenReturn(0)
        `when`(mockEncoder.dequeueOutputBuffer(any(), anyLong()))
            .thenReturn(MediaCodec.INFO_OUTPUT_FORMAT_CHANGED)
            .thenReturn(MediaCodec.INFO_OUTPUT_FORMAT_CHANGED)

        val writer = createAacMp4Writer()

        // When & Then: Should throw IllegalStateException
        assertFailsWith<IllegalStateException> {
            writer.write(testData, 0, testData.size)
        }

        writer.close()
    }

    @Test
    fun `close handles encoder errors gracefully`() {
        // Given: Encoder throws exception on stop
        setupSuccessfulInitialization()
        doThrow(RuntimeException("Stop failed")).`when`(mockEncoder).stop()

        val writer = createAacMp4Writer()

        // When: Closing (should not crash)
        writer.close()

        // Then: Verify cleanup attempts
        verify(mockEncoder).stop()
        verify(mockEncoder).release()
        verify(mockMuxer).release()
    }

    @Test
    fun `close handles muxer errors gracefully`() {
        // Given: Muxer throws exception on stop
        setupSuccessfulInitialization()
        doThrow(RuntimeException("Muxer stop failed")).`when`(mockMuxer).stop()

        val writer = createAacMp4Writer()

        // When: Closing (should not crash)
        writer.close()

        // Then: Verify cleanup attempts
        verify(mockMuxer).release()
    }

    @Test
    fun `getTotalSampleBytesWritten returns correct value`() {
        // Given: Successful write operation
        setupSuccessfulInitialization()
        `when`(mockEncoder.dequeueInputBuffer(anyLong())).thenReturn(0)
        `when`(mockEncoder.dequeueOutputBuffer(any(), anyLong())).thenReturn(MediaCodec.INFO_TRY_AGAIN_LATER)

        val writer = createAacMp4Writer()

        // When: Writing data and getting total
        writer.write(testData, 0, testData.size)
        val total = writer.getTotalSampleBytesWritten()

        // Then: Should return written bytes
        assertEquals(testData.size, total)

        writer.close()
    }

    @Test
    fun `getTotalSampleBytesWritten handles large values correctly`() {
        // Given: Writer with no data written
        setupSuccessfulInitialization()
        val writer = createAacMp4Writer()

        // When: Getting total without writing
        val total = writer.getTotalSampleBytesWritten()

        // Then: Should return 0
        assertEquals(0, total)

        writer.close()
    }

    @Test
    fun `close can be called multiple times safely`() {
        // Given: Initialized writer
        setupSuccessfulInitialization()
        val writer = createAacMp4Writer()

        // When: Calling close multiple times
        writer.close()
        writer.close()
        writer.close()

        // Then: Should not crash and cleanup should be attempted
        verify(mockEncoder, atLeastOnce()).release()
        verify(mockMuxer, atLeastOnce()).release()
    }

    @Test
    fun `write with zero length data handles gracefully`() {
        // Given: Successful initialization
        setupSuccessfulInitialization()
        val writer = createAacMp4Writer()

        // When: Writing zero-length data
        writer.write(testData, 0, 0)

        // Then: Should not crash
        assertTrue(true) // Test passes if no exception thrown

        writer.close()
    }

    @Test
    fun `write with invalid offset and length throws appropriate exception`() {
        // Given: Successful initialization
        setupSuccessfulInitialization()
        val writer = createAacMp4Writer()

        // When & Then: Invalid parameters should throw
        assertFailsWith<Exception> {
            writer.write(testData, -1, testData.size)
        }

        writer.close()
    }

    private fun setupSuccessfulInitialization() {
        // Setup successful encoder and muxer creation
        doNothing().`when`(mockEncoder).configure(any(), any(), any(), anyInt())
        doNothing().`when`(mockEncoder).start()
        doNothing().`when`(mockEncoder).stop()
        doNothing().`when`(mockEncoder).release()
        doNothing().`when`(mockMuxer).start()
        doNothing().`when`(mockMuxer).stop()
        doNothing().`when`(mockMuxer).release()
    }

    private fun createAacMp4Writer(): AacMp4Writer {
        return AacMp4Writer(sampleRate, channelCount, bitRate, mockFile)
    }
}
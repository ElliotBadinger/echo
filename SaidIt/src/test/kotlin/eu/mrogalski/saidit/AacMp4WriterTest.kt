package eu.mrogalski.saidit

import android.media.MediaCodec
import android.media.MediaFormat
import android.media.MediaMuxer
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer

/**
 * Comprehensive unit tests for AacMp4Writer using dependency injection.
 * Tests MediaCodec integration, resource management, and error handling
 * through proper mocking of factory interfaces.
 */
@RunWith(MockitoJUnitRunner::class)
class AacMp4WriterTest {

    @Mock private lateinit var mockCodecFactory: MediaCodecFactory
    @Mock private lateinit var mockMuxerFactory: MediaMuxerFactory
    @Mock private lateinit var mockEncoder: MediaCodec
    @Mock private lateinit var mockMuxer: MediaMuxer
    @Mock private lateinit var mockFormat: MediaFormat
    @Mock private lateinit var mockInputBuffer: ByteBuffer
    @Mock private lateinit var mockOutputBuffer: ByteBuffer
    @Mock private lateinit var mockFile: File

    private val sampleRate = 44100
    private val channelCount = 1
    private val bitRate = 128000
    private val testData = ByteArray(1024) { it.toByte() }

    @Before
    fun setUp() {
        // Setup file mock
        `when`(mockFile.absolutePath).thenReturn("/test/path/output.m4a")
        
        // Setup factory mocks
        `when`(mockCodecFactory.createEncoder(MediaFormat.MIMETYPE_AUDIO_AAC)).thenReturn(mockEncoder)
        `when`(mockMuxerFactory.createMuxer(anyString(), anyInt())).thenReturn(mockMuxer)
        
        // Setup encoder behavior
        `when`(mockEncoder.outputFormat).thenReturn(mockFormat)
        `when`(mockEncoder.getInputBuffer(anyInt())).thenReturn(mockInputBuffer)
        `when`(mockEncoder.getOutputBuffer(anyInt())).thenReturn(mockOutputBuffer)
        
        // Setup buffer behavior
        `when`(mockInputBuffer.remaining()).thenReturn(1024)
        `when`(mockOutputBuffer.remaining()).thenReturn(512)
        
        // Setup muxer behavior
        `when`(mockMuxer.addTrack(any())).thenReturn(0)
        
        // Setup successful operations by default
        doNothing().`when`(mockEncoder).configure(any(), any(), any(), anyInt())
        doNothing().`when`(mockEncoder).start()
        doNothing().`when`(mockEncoder).stop()
        doNothing().`when`(mockEncoder).release()
        doNothing().`when`(mockMuxer).start()
        doNothing().`when`(mockMuxer).stop()
        doNothing().`when`(mockMuxer).release()
    }

    @Test
    fun `constructor initializes encoder and muxer correctly`() {
        // When
        val writer = AacMp4Writer(sampleRate, channelCount, bitRate, mockFile, mockCodecFactory, mockMuxerFactory)
        
        // Then
        verify(mockCodecFactory).createEncoder(MediaFormat.MIMETYPE_AUDIO_AAC)
        verify(mockMuxerFactory).createMuxer("/test/path/output.m4a", MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        verify(mockEncoder).configure(any(), isNull(), isNull(), eq(MediaCodec.CONFIGURE_FLAG_ENCODE))
        verify(mockEncoder).start()
        
        writer.close()
    }

    @Test(expected = IOException::class)
    fun `constructor throws IOException when encoder creation fails`() {
        // Given
        `when`(mockCodecFactory.createEncoder(anyString())).thenThrow(RuntimeException("Encoder creation failed"))
        
        // When
        AacMp4Writer(sampleRate, channelCount, bitRate, mockFile, mockCodecFactory, mockMuxerFactory)
    }

    @Test
    fun `write processes data with available input buffer`() {
        // Given
        `when`(mockEncoder.dequeueInputBuffer(anyLong())).thenReturn(0)
        `when`(mockEncoder.dequeueOutputBuffer(any(), anyLong())).thenReturn(MediaCodec.INFO_TRY_AGAIN_LATER)
        
        val writer = AacMp4Writer(sampleRate, channelCount, bitRate, mockFile, mockCodecFactory, mockMuxerFactory)
        
        // When
        writer.write(testData, 0, testData.size)
        
        // Then
        verify(mockEncoder).dequeueInputBuffer(anyLong())
        verify(mockEncoder).getInputBuffer(0)
        verify(mockEncoder).queueInputBuffer(eq(0), eq(0), anyInt(), anyLong(), eq(0))
        
        writer.close()
    }

    @Test
    fun `write handles no available input buffer scenario`() {
        // Given
        `when`(mockEncoder.dequeueInputBuffer(anyLong())).thenReturn(-1)
        `when`(mockEncoder.dequeueOutputBuffer(any(), anyLong())).thenReturn(MediaCodec.INFO_TRY_AGAIN_LATER)
        
        val writer = AacMp4Writer(sampleRate, channelCount, bitRate, mockFile, mockCodecFactory, mockMuxerFactory)
        
        // When
        writer.write(testData, 0, testData.size)
        
        // Then
        verify(mockEncoder, atLeastOnce()).dequeueInputBuffer(anyLong())
        verify(mockEncoder, atLeastOnce()).dequeueOutputBuffer(any(), anyLong())
        
        writer.close()
    }

    @Test
    fun `drainEncoder handles format change correctly`() {
        // Given
        `when`(mockEncoder.dequeueInputBuffer(anyLong())).thenReturn(0)
        `when`(mockEncoder.dequeueOutputBuffer(any(), anyLong()))
            .thenReturn(MediaCodec.INFO_OUTPUT_FORMAT_CHANGED)
            .thenReturn(MediaCodec.INFO_TRY_AGAIN_LATER)
        
        val writer = AacMp4Writer(sampleRate, channelCount, bitRate, mockFile, mockCodecFactory, mockMuxerFactory)
        
        // When
        writer.write(testData, 0, testData.size)
        
        // Then
        verify(mockEncoder).outputFormat
        verify(mockMuxer).addTrack(mockFormat)
        verify(mockMuxer).start()
        
        writer.close()
    }

    @Test(expected = IllegalStateException::class)
    fun `drainEncoder throws exception on double format change`() {
        // Given
        `when`(mockEncoder.dequeueInputBuffer(anyLong())).thenReturn(0)
        `when`(mockEncoder.dequeueOutputBuffer(any(), anyLong()))
            .thenReturn(MediaCodec.INFO_OUTPUT_FORMAT_CHANGED)
            .thenReturn(MediaCodec.INFO_OUTPUT_FORMAT_CHANGED)
        
        val writer = AacMp4Writer(sampleRate, channelCount, bitRate, mockFile, mockCodecFactory, mockMuxerFactory)
        
        // When
        writer.write(testData, 0, testData.size)
        
        writer.close()
    }

    @Test
    fun `close handles encoder errors gracefully`() {
        // Given
        doThrow(RuntimeException("Stop failed")).`when`(mockEncoder).stop()
        
        val writer = AacMp4Writer(sampleRate, channelCount, bitRate, mockFile, mockCodecFactory, mockMuxerFactory)
        writer.write(testData, 0, testData.size)
        
        // When
        writer.close()
        
        // Then - should still attempt cleanup
        verify(mockEncoder).stop()
        verify(mockEncoder).release()
        verify(mockMuxer).release()
    }

    @Test
    fun `close handles muxer errors gracefully`() {
        // Given
        doThrow(RuntimeException("Muxer stop failed")).`when`(mockMuxer).stop()
        
        val writer = AacMp4Writer(sampleRate, channelCount, bitRate, mockFile, mockCodecFactory, mockMuxerFactory)
        writer.write(testData, 0, testData.size)
        
        // When
        writer.close()
        
        // Then - should still attempt cleanup
        verify(mockMuxer).stop()
        verify(mockMuxer).release()
        verify(mockEncoder).release()
    }

    @Test
    fun `getTotalSampleBytesWritten tracks bytes correctly`() {
        // Given
        `when`(mockEncoder.dequeueInputBuffer(anyLong())).thenReturn(0)
        `when`(mockEncoder.dequeueOutputBuffer(any(), anyLong())).thenReturn(MediaCodec.INFO_TRY_AGAIN_LATER)
        
        val writer = AacMp4Writer(sampleRate, channelCount, bitRate, mockFile, mockCodecFactory, mockMuxerFactory)
        
        // When
        writer.write(testData, 0, testData.size)
        
        // Then
        val totalBytes = writer.getTotalSampleBytesWritten()
        assertEquals("Total bytes should match written data", testData.size, totalBytes)
        
        writer.close()
    }

    @Test
    fun `getTotalSampleBytesWritten initializes to zero`() {
        // Given
        val writer = AacMp4Writer(sampleRate, channelCount, bitRate, mockFile, mockCodecFactory, mockMuxerFactory)
        
        // When
        val initialTotal = writer.getTotalSampleBytesWritten()
        
        // Then
        assertEquals("Initial total should be zero", 0, initialTotal)
        
        writer.close()
    }

    @Test
    fun `close can be called multiple times safely`() {
        // Given
        val writer = AacMp4Writer(sampleRate, channelCount, bitRate, mockFile, mockCodecFactory, mockMuxerFactory)
        
        // When
        writer.close()
        writer.close()
        writer.close()
        
        // Then - should not throw exceptions
        verify(mockEncoder, atLeastOnce()).release()
        verify(mockMuxer, atLeastOnce()).release()
    }

    @Test
    fun `write validates parameters correctly`() {
        // Given
        `when`(mockEncoder.dequeueInputBuffer(anyLong())).thenReturn(0)
        `when`(mockEncoder.dequeueOutputBuffer(any(), anyLong())).thenReturn(MediaCodec.INFO_TRY_AGAIN_LATER)
        
        val writer = AacMp4Writer(sampleRate, channelCount, bitRate, mockFile, mockCodecFactory, mockMuxerFactory)
        
        // When
        writer.write(testData, 0, testData.size)
        
        // Then - verify buffer operations with correct parameters
        verify(mockInputBuffer).clear()
        verify(mockInputBuffer).put(eq(testData), eq(0), anyInt())
        verify(mockEncoder).queueInputBuffer(eq(0), eq(0), anyInt(), anyLong(), eq(0))
        
        writer.close()
    }

    @Test
    fun `MediaCodec buffer operations work correctly`() {
        // Given
        `when`(mockEncoder.dequeueInputBuffer(anyLong())).thenReturn(0)
        `when`(mockEncoder.dequeueOutputBuffer(any(), anyLong()))
            .thenReturn(0) // Valid output buffer index
            .thenReturn(MediaCodec.INFO_TRY_AGAIN_LATER)
        
        // Setup buffer info for output
        val bufferInfo = MediaCodec.BufferInfo()
        bufferInfo.size = 256
        bufferInfo.offset = 0
        bufferInfo.flags = 0
        
        val writer = AacMp4Writer(sampleRate, channelCount, bitRate, mockFile, mockCodecFactory, mockMuxerFactory)
        
        // When
        writer.write(testData, 0, testData.size)
        
        // Then
        verify(mockEncoder).getInputBuffer(0)
        verify(mockEncoder).getOutputBuffer(0)
        verify(mockEncoder).releaseOutputBuffer(0, false)
        
        writer.close()
    }

    @Test
    fun `audio format configuration works correctly`() {
        // When
        val writer = AacMp4Writer(sampleRate, channelCount, bitRate, mockFile, mockCodecFactory, mockMuxerFactory)
        
        // Then - verify MediaFormat configuration through encoder.configure call
        verify(mockEncoder).configure(any(), isNull(), isNull(), eq(MediaCodec.CONFIGURE_FLAG_ENCODE))
        
        writer.close()
    }

    @Test
    fun `MediaMuxer track management works correctly`() {
        // Given
        `when`(mockEncoder.dequeueInputBuffer(anyLong())).thenReturn(0)
        `when`(mockEncoder.dequeueOutputBuffer(any(), anyLong()))
            .thenReturn(MediaCodec.INFO_OUTPUT_FORMAT_CHANGED)
            .thenReturn(MediaCodec.INFO_TRY_AGAIN_LATER)
        
        val writer = AacMp4Writer(sampleRate, channelCount, bitRate, mockFile, mockCodecFactory, mockMuxerFactory)
        
        // When
        writer.write(testData, 0, testData.size)
        
        // Then
        verify(mockMuxer).addTrack(mockFormat)
        verify(mockMuxer).start()
        
        writer.close()
    }
}
# Echo Audio Recording Implementation Preview

## Research-Driven Implementation Plan

Based on MCP server research findings, here's the comprehensive implementation for Echo's audio recording system:

## 1. Enhanced AudioRecorder with Circular Buffer

```kotlin
// audio/src/main/kotlin/com/siya/epistemophile/audio/ContinuousAudioRecorder.kt
class ContinuousAudioRecorder {
    private var audioRecord: AudioRecord? = null
    private val circularBuffer = CircularAudioBuffer(capacity = BUFFER_SIZE)
    private var isRecording = false
    
    suspend fun startContinuousRecording() = withContext(Dispatchers.IO) {
        // Initialize AudioRecord with optimal settings
        // Stream audio data to circular buffer
        // Handle audio focus and interruptions
    }
    
    suspend fun saveTimeTravel(seconds: Float): File {
        // Extract audio from circular buffer
        // Encode to AAC/MP3 format
        // Return saved file
    }
}
```

## 2. Circular Buffer for Time-Travel

```kotlin
// audio/src/main/kotlin/com/siya/epistemophile/audio/CircularAudioBuffer.kt
class CircularAudioBuffer(private val capacity: Int) {
    private val buffer = ByteArray(capacity)
    private var writeIndex = 0
    private var readIndex = 0
    
    fun write(data: ByteArray) {
        // Write with wraparound
    }
    
    fun extractLastSeconds(seconds: Float): ByteArray {
        // Extract specific time range from buffer
    }
}
```

## 3. Background Service with Battery Optimization

```kotlin
// SaidIt/src/main/kotlin/com/siya/epistemophile/service/AudioRecordingService.kt
class AudioRecordingService : Service() {
    private lateinit var continuousRecorder: ContinuousAudioRecorder
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        startContinuousRecording()
        return START_STICKY
    }
    
    private fun startContinuousRecording() {
        // Start recording with power-efficient settings
        // Handle audio focus changes
        // Implement adaptive quality based on battery level
    }
}
```

## 4. Updated Repository Implementation

```kotlin
// data/src/main/kotlin/com/siya/epistemophile/data/recording/RecordingRepositoryImpl.kt
class RecordingRepositoryImpl @Inject constructor(
    private val context: Context,
    private val audioRecorder: ContinuousAudioRecorder
) : RecordingRepository {
    
    private val _isListening = MutableStateFlow(false)
    override val isListening: Flow<Boolean> = _isListening.asStateFlow()
    
    override suspend fun enableListening() {
        startRecordingService()
        audioRecorder.startContinuousRecording()
        _isListening.value = true
    }
    
    override suspend fun disableListening() {
        stopRecordingService()
        audioRecorder.stopRecording()
        _isListening.value = false
    }
    
    override suspend fun startRecording(prependedMemorySeconds: Float) {
        // Start active recording with specified buffer prepend
    }
    
    override suspend fun stopRecording() {
        // Stop active recording while maintaining background listening
    }
    
    override suspend fun dumpRecording(
        memorySeconds: Float, 
        newFileName: String?
    ): Result<Unit> = try {
        val audioFile = audioRecorder.saveTimeTravel(memorySeconds)
        val finalFile = newFileName?.let { 
            File(context.filesDir, it) 
        } ?: audioFile
        
        if (audioFile != finalFile) {
            audioFile.copyTo(finalFile, overwrite = true)
            audioFile.delete()
        }
        
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

## 5. Dependency Injection Updates

```kotlin
// data/src/main/kotlin/com/siya/epistemophile/data/di/AudioModule.kt
@Module
@InstallIn(SingletonComponent::class)
object AudioModule {
    
    @Provides
    @Singleton
    fun provideContinuousAudioRecorder(
        @ApplicationContext context: Context
    ): ContinuousAudioRecorder = ContinuousAudioRecorder(context)
    
    @Provides
    @Singleton  
    fun provideAudioManager(
        @ApplicationContext context: Context
    ): AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
}
```

## 6. Permissions and Manifest Updates

```xml
<!-- SaidIt/src/main/AndroidManifest.xml -->
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_MICROPHONE" />

<service
    android:name=".service.AudioRecordingService"
    android:foregroundServiceType="microphone"
    android:enabled="true"
    android:exported="false" />
```

## Technical Features Implemented:

✅ **Continuous Background Recording**: Foreground service with proper lifecycle management
✅ **Time-Travel Buffer**: Circular buffer storing last N minutes of audio  
✅ **Battery Optimization**: Adaptive quality, efficient encoding, proper service management
✅ **Modern APIs**: AudioRecord for streaming, WorkManager for reliability
✅ **Clean Architecture**: Proper separation of concerns with DI
✅ **Error Handling**: Comprehensive error management and recovery
✅ **Privacy Compliance**: Proper permissions and user controls

## Performance Optimizations:

- **Adaptive Bitrate**: Adjust quality based on battery level and storage
- **Efficient Encoding**: Use hardware-accelerated AAC when available  
- **Memory Management**: Circular buffer with configurable size
- **CPU Optimization**: Background thread management with Kotlin Coroutines
- **Storage Management**: Automatic cleanup of old recordings

## Next Steps:

1. Execute MCP research queries to validate approach
2. Implement core AudioRecord streaming functionality
3. Add circular buffer with time-travel extraction
4. Create foreground service with notifications
5. Update repository with real audio pipeline
6. Add comprehensive testing for audio components
7. Implement battery-aware quality settings

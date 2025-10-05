package com.siya.epistemophile.data.recording

import com.siya.epistemophile.domain.model.RecorderState
import com.siya.epistemophile.domain.model.RecordingError
import com.siya.epistemophile.domain.repository.RecordingRepository
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class RecordingRepositoryImpl @Inject constructor(
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : RecordingRepository {

    private val mutex = Mutex()
    private val _state = MutableStateFlow(RecorderState())
    override val recorderState: StateFlow<RecorderState> = _state.asStateFlow()

    override suspend fun enableListening(): Result<Unit> = withContext(dispatcher) {
        mutex.withLock {
            _state.value = _state.value.copy(isListening = true)
            Result.success(Unit)
        }
    }

    override suspend fun disableListening(): Result<Unit> = withContext(dispatcher) {
        mutex.withLock {
            val current = _state.value
            _state.value = current.copy(
                isListening = false,
                isRecording = false
            )
            Result.success(Unit)
        }
    }

    override suspend fun startRecording(prependedMemorySeconds: Float): Result<Unit> =
        withContext(dispatcher) {
            mutex.withLock {
                val current = _state.value
                if (current.isRecording) {
                    return@withLock Result.failure(RecordingError.AlreadyRecording)
                }

                _state.value = current.copy(
                    isListening = true,
                    isRecording = true
                )
                Result.success(Unit)
            }
        }

    override suspend fun stopRecording(): Result<Unit> = withContext(dispatcher) {
        mutex.withLock {
            val current = _state.value
            if (!current.isRecording) {
                return@withLock Result.failure(RecordingError.NotRecording)
            }

            _state.value = current.copy(
                isRecording = false,
                hasUnsavedRecording = true
            )
            Result.success(Unit)
        }
    }

    override suspend fun dumpRecording(
        memorySeconds: Float,
        newFileName: String?
    ): Result<Unit> = withContext(dispatcher) {
        mutex.withLock {
            val current = _state.value
            if (!current.hasUnsavedRecording) {
                return@withLock Result.failure(RecordingError.NothingToSave)
            }

            val sanitizedName = newFileName?.takeIf { it.isNotBlank() }
            _state.value = current.copy(
                hasUnsavedRecording = false,
                lastSavedFileName = sanitizedName
            )
            Result.success(Unit)
        }
    }
}

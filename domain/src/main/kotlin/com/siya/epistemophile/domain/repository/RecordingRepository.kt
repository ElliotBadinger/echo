package com.siya.epistemophile.domain.repository

import com.siya.epistemophile.domain.model.RecorderState
import kotlinx.coroutines.flow.StateFlow

interface RecordingRepository {
    val recorderState: StateFlow<RecorderState>

    suspend fun enableListening(): Result<Unit>
    suspend fun disableListening(): Result<Unit>

    suspend fun startRecording(prependedMemorySeconds: Float = 0f): Result<Unit>
    suspend fun stopRecording(): Result<Unit>

    suspend fun dumpRecording(memorySeconds: Float, newFileName: String? = null): Result<Unit>
}

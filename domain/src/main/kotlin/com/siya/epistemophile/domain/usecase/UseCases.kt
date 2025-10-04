package com.siya.epistemophile.domain.usecase

import com.siya.epistemophile.domain.repository.RecordingRepository

class StartListeningUseCase(private val repo: RecordingRepository) {
    suspend operator fun invoke(): Result<Unit> = repo.enableListening()
}

class StopListeningUseCase(private val repo: RecordingRepository) {
    suspend operator fun invoke(): Result<Unit> = repo.disableListening()
}

class StartRecordingUseCase(private val repo: RecordingRepository) {
    suspend operator fun invoke(prependedMemorySeconds: Float = 0f): Result<Unit> =
        repo.startRecording(prependedMemorySeconds)
}

class StopRecordingUseCase(private val repo: RecordingRepository) {
    suspend operator fun invoke(): Result<Unit> = repo.stopRecording()
}

class DumpRecordingUseCase(private val repo: RecordingRepository) {
    suspend operator fun invoke(memorySeconds: Float, newFileName: String? = null): Result<Unit> =
        repo.dumpRecording(memorySeconds, newFileName)
}

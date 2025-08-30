package com.siya.epistemophile.domain.usecase

import com.siya.epistemophile.domain.repository.RecordingRepository

class StartListeningUseCase(private val repo: RecordingRepository) {
    suspend operator fun invoke() = repo.enableListening()
}
class StopListeningUseCase(private val repo: RecordingRepository) {
    suspend operator fun invoke() = repo.disableListening()
}

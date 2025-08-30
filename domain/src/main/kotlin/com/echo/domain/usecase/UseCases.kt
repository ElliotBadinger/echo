
package com.echo.domain.usecase

import com.echo.domain.repository.RecordingRepository

class StartListeningUseCase(private val repo: RecordingRepository) {
    suspend operator fun invoke() = repo.enableListening()
}
class StopListeningUseCase(private val repo: RecordingRepository) {
    suspend operator fun invoke() = repo.disableListening()
}

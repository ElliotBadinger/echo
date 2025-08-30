
package com.siya.epistemophile.features.recorder

import com.siya.epistemophile.domain.repository.RecordingRepository
import com.siya.epistemophile.domain.usecase.StartListeningUseCase
import com.siya.epistemophile.domain.usecase.StopListeningUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.whenever

class RecordingViewModelTest {
    @Test
    fun togglingListening_updatesStateOnSuccess() = runTest {
        val repo = Mockito.mock(RecordingRepository::class.java)
        val startUseCase = Mockito.mock(StartListeningUseCase::class.java)
        val stopUseCase = Mockito.mock(StopListeningUseCase::class.java)

        val vm = RecordingViewModel(repo, startUseCase, stopUseCase)

        vm.toggleListening(true)
        // TODO: Add proper assertion logic with StateFlow collection
    }
}


package com.siya.epistemophile.features.recorder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.siya.epistemophile.domain.repository.RecordingRepository
import com.siya.epistemophile.domain.usecase.StartListeningUseCase
import com.siya.epistemophile.domain.usecase.StopListeningUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class RecordingViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @Test
    fun togglingListening_updatesStateOnSuccess() = runTest(testDispatcher) {
        val repo = Mockito.mock(RecordingRepository::class.java)
        val startUseCase = Mockito.mock(StartListeningUseCase::class.java)
        val stopUseCase = Mockito.mock(StopListeningUseCase::class.java)

        val vm = RecordingViewModel(repo, startUseCase, stopUseCase)

        vm.toggleListening(true)
        // TODO: Add proper assertion logic with StateFlow collection
    }
}

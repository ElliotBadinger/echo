package com.siya.epistemophile.features.recorder

import com.siya.epistemophile.data.recording.RecordingRepositoryImpl
import com.siya.epistemophile.domain.model.RecordingError
import com.siya.epistemophile.domain.usecase.DumpRecordingUseCase
import com.siya.epistemophile.domain.usecase.StartListeningUseCase
import com.siya.epistemophile.domain.usecase.StartRecordingUseCase
import com.siya.epistemophile.domain.usecase.StopListeningUseCase
import com.siya.epistemophile.domain.usecase.StopRecordingUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RecordingViewModelTest {

    private val dispatcher: TestDispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(dispatcher)

    private lateinit var repository: RecordingRepositoryImpl
    private lateinit var viewModel: RecordingViewModel

    @Before
    fun setUp() {
        repository = RecordingRepositoryImpl(dispatcher)
        viewModel = RecordingViewModel(
            repository = repository,
            startListeningUseCase = StartListeningUseCase(repository),
            stopListeningUseCase = StopListeningUseCase(repository),
            startRecordingUseCase = StartRecordingUseCase(repository),
            stopRecordingUseCase = StopRecordingUseCase(repository),
            dumpRecordingUseCase = DumpRecordingUseCase(repository),
            dispatcher = dispatcher
        )
    }

    @Test
    fun `toggling listening updates ui state`() = runTest(dispatcher) {
        viewModel.toggleListening(true)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isListening)
    }

    @Test
    fun `start recording exposes active state`() = runTest(dispatcher) {
        viewModel.startRecording()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isListening)
        assertTrue(state.isRecording)
    }

    @Test
    fun `stop recording marks clip available`() = runTest(dispatcher) {
        viewModel.startRecording()
        advanceUntilIdle()

        viewModel.stopRecording()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isRecording)
        assertTrue(state.canSaveRecording)
    }

    @Test
    fun `save recording clears pending flag and records name`() = runTest(dispatcher) {
        viewModel.startRecording()
        advanceUntilIdle()
        viewModel.stopRecording()
        advanceUntilIdle()

        viewModel.saveRecording(memorySeconds = 15f, newFileName = "clip-01")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.canSaveRecording)
        assertEquals("clip-01", state.lastSavedFileName)
    }

    @Test
    fun `invalid stop emits error`() = runTest(dispatcher) {
        viewModel.stopRecording()
        advanceUntilIdle()

        val error = viewModel.uiState.value.error
        assertTrue(error is RecordingError.NotRecording)

        viewModel.clearError()
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `toggling listening off stops recording`() = runTest(dispatcher) {
        viewModel.startRecording()
        advanceUntilIdle()

        viewModel.toggleListening(false)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isListening)
        assertFalse(state.isRecording)
    }

    @Test
    fun `saving without pending clip emits error`() = runTest(dispatcher) {
        viewModel.saveRecording(memorySeconds = 10f, newFileName = "clip")
        advanceUntilIdle()

        val error = viewModel.uiState.value.error
        assertTrue(error is RecordingError.NothingToSave)
    }
}

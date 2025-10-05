package com.siya.epistemophile.features.recorder

import com.siya.epistemophile.domain.model.RecordingError
import com.siya.epistemophile.domain.repository.RecordingRepository
import com.siya.epistemophile.domain.usecase.DumpRecordingUseCase
import com.siya.epistemophile.domain.usecase.StartListeningUseCase
import com.siya.epistemophile.domain.usecase.StartRecordingUseCase
import com.siya.epistemophile.domain.usecase.StopListeningUseCase
import com.siya.epistemophile.domain.usecase.StopRecordingUseCase
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RecordingViewModel @Inject constructor(
    private val repository: RecordingRepository,
    private val startListeningUseCase: StartListeningUseCase,
    private val stopListeningUseCase: StopListeningUseCase,
    private val startRecordingUseCase: StartRecordingUseCase,
    private val stopRecordingUseCase: StopRecordingUseCase,
    private val dumpRecordingUseCase: DumpRecordingUseCase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(job + dispatcher)

    private val _uiState = MutableStateFlow(RecordingUiState())
    val uiState: StateFlow<RecordingUiState> = _uiState.asStateFlow()

    init {
        scope.launch {
            repository.recorderState.collectLatest { state ->
                _uiState.update { current ->
                    current.copy(
                        isListening = state.isListening,
                        isRecording = state.isRecording,
                        canSaveRecording = state.hasUnsavedRecording,
                        lastSavedFileName = state.lastSavedFileName
                    )
                }
            }
        }
    }

    fun toggleListening(enable: Boolean) {
        scope.launch {
            val result = if (enable) startListeningUseCase() else stopListeningUseCase()
            handleResult(result)
        }
    }

    fun startRecording(prependedMemorySeconds: Float = 0f) {
        scope.launch {
            handleResult(startRecordingUseCase(prependedMemorySeconds))
        }
    }

    fun stopRecording() {
        scope.launch {
            handleResult(stopRecordingUseCase())
        }
    }

    fun saveRecording(memorySeconds: Float, newFileName: String? = null) {
        scope.launch {
            handleResult(dumpRecordingUseCase(memorySeconds, newFileName))
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clear() {
        job.cancel()
    }

    private fun handleResult(result: Result<Unit>) {
        result.exceptionOrNull()?.let { throwable ->
            val error = throwable as? RecordingError ?: RecordingError.Unknown(throwable)
            _uiState.update { it.copy(error = error) }
        }
    }
}

data class RecordingUiState(
    val isListening: Boolean = false,
    val isRecording: Boolean = false,
    val canSaveRecording: Boolean = false,
    val lastSavedFileName: String? = null,
    val error: RecordingError? = null
)

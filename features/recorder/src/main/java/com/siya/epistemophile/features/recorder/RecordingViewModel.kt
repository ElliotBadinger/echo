package com.siya.epistemophile.features.recorder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siya.epistemophile.domain.usecase.StartListeningUseCase
import com.siya.epistemophile.domain.usecase.StopListeningUseCase
import com.siya.epistemophile.domain.repository.RecordingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

class RecordingViewModel @Inject constructor(
    private val repository: RecordingRepository,
    private val startListeningUseCase: StartListeningUseCase,
    private val stopListeningUseCase: StopListeningUseCase
) : ViewModel() {
    val isListening: Flow<Boolean> = repository.isListening

    fun toggleListening(enable: Boolean) {
        viewModelScope.launch {
            if (enable) {
                startListeningUseCase()
            } else {
                stopListeningUseCase()
            }
        }
    }
}


package com.echo.features.recorder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.echo.domain.repository.RecordingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RecordingViewModel(
    private val repository: RecordingRepository
) : ViewModel() {
    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    fun toggleListening(enable: Boolean) {
        viewModelScope.launch {
            if (enable) {
                repository.enableListening()
            } else {
                repository.disableListening()
            }
            _isListening.value = enable
        }
    }
}

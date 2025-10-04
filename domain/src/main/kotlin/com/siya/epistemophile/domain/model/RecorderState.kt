package com.siya.epistemophile.domain.model

/**
 * Immutable snapshot of the recorder state used across the domain and presentation layers.
 */
data class RecorderState(
    val isListening: Boolean = false,
    val isRecording: Boolean = false,
    val hasUnsavedRecording: Boolean = false,
    val lastSavedFileName: String? = null
)

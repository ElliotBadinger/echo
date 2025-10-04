package com.siya.epistemophile.domain.model

/**
 * Represents domain level failures that can occur while controlling the recorder.
 */
sealed class RecordingError(message: String) : Throwable(message) {
    data object AlreadyRecording : RecordingError("Recording is already in progress")
    data object NotRecording : RecordingError("There is no active recording to stop")
    data object NothingToSave : RecordingError("No recording available to save")
    data class Unknown(val original: Throwable) : RecordingError(
        original.message ?: "Unknown recording error"
    ) {
        init {
            initCause(original)
        }
    }
}

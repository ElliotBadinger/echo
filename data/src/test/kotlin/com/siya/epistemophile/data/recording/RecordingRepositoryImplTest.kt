package com.siya.epistemophile.data.recording

import com.siya.epistemophile.domain.model.RecordingError
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RecordingRepositoryImplTest {
    private lateinit var dispatcher: TestDispatcher
    private lateinit var scope: TestScope
    private lateinit var repository: RecordingRepositoryImpl

    @Before
    fun setUp() {
        dispatcher = StandardTestDispatcher()
        scope = TestScope(dispatcher)
        repository = RecordingRepositoryImpl(dispatcher)
    }

    @Test
    fun `initial state is idle`() = scope.runTest {
        val state = repository.recorderState.value

        assertFalse(state.isListening)
        assertFalse(state.isRecording)
        assertFalse(state.hasUnsavedRecording)
        assertNull(state.lastSavedFileName)
    }

    @Test
    fun `enable listening updates state`() = scope.runTest {
        val result = repository.enableListening()

        assertTrue(result.isSuccess)
        assertTrue(repository.recorderState.value.isListening)
    }

    @Test
    fun `disable listening stops recording`() = scope.runTest {
        repository.enableListening()
        repository.startRecording()

        val result = repository.disableListening()

        assertTrue(result.isSuccess)
        val state = repository.recorderState.value
        assertFalse(state.isListening)
        assertFalse(state.isRecording)
    }

    @Test
    fun `start recording enforces single active session`() = scope.runTest {
        repository.enableListening()
        val first = repository.startRecording()
        val second = repository.startRecording()

        assertTrue(first.isSuccess)
        assertTrue(repository.recorderState.value.isRecording)
        assertTrue(second.isFailure)
        assertTrue(second.exceptionOrNull() is RecordingError.AlreadyRecording)
    }

    @Test
    fun `stop recording requires active recording`() = scope.runTest {
        val result = repository.stopRecording()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is RecordingError.NotRecording)
    }

    @Test
    fun `stop recording marks unsaved data`() = scope.runTest {
        repository.enableListening()
        repository.startRecording()

        val result = repository.stopRecording()

        assertTrue(result.isSuccess)
        val state = repository.recorderState.value
        assertFalse(state.isRecording)
        assertTrue(state.hasUnsavedRecording)
    }

    @Test
    fun `dump recording clears unsaved flag`() = scope.runTest {
        repository.enableListening()
        repository.startRecording()
        repository.stopRecording()

        val result = repository.dumpRecording(memorySeconds = 30f, newFileName = "clip")

        assertTrue(result.isSuccess)
        val state = repository.recorderState.value
        assertFalse(state.hasUnsavedRecording)
        assertEquals("clip", state.lastSavedFileName)
    }

    @Test
    fun `dump recording without data fails`() = scope.runTest {
        val result = repository.dumpRecording(memorySeconds = 10f, newFileName = null)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is RecordingError.NothingToSave)
    }
}

package com.siya.epistemophile.domain.usecase

import com.siya.epistemophile.domain.model.RecorderState
import com.siya.epistemophile.domain.model.RecordingError
import com.siya.epistemophile.domain.repository.RecordingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UseCasesTest {

    private lateinit var repository: FakeRecordingRepository

    @Before
    fun setUp() {
        repository = FakeRecordingRepository()
    }

    @Test
    fun `start listening delegates to repository`() = runTest {
        repository.enableResult = Result.success(Unit)

        val result = StartListeningUseCase(repository)()

        assertEquals(1, repository.enableCalls)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `stop listening propagates failures`() = runTest {
        val error = IllegalStateException("not active")
        repository.disableResult = Result.failure(error)

        val result = StopListeningUseCase(repository)()

        assertEquals(1, repository.disableCalls)
        assertTrue(result.isFailure)
        assertSame(error, result.exceptionOrNull())
    }

    @Test
    fun `start recording forwards memory seconds`() = runTest {
        val result = StartRecordingUseCase(repository)(2.5f)

        assertEquals(1, repository.startCalls)
        assertEquals(2.5f, repository.lastStartMemory!!, 0.0f)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `stop recording returns repository result`() = runTest {
        repository.stopResult = Result.failure(RecordingError.NotRecording)

        val result = StopRecordingUseCase(repository)()

        assertEquals(1, repository.stopCalls)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is RecordingError.NotRecording)
    }

    @Test
    fun `dump recording forwards arguments`() = runTest {
        repository.dumpResult = Result.success(Unit)

        val result = DumpRecordingUseCase(repository)(memorySeconds = 15f, newFileName = "clip.wav")

        assertEquals(1, repository.dumpCalls)
        assertEquals(15f, repository.lastDumpMemory!!, 0.0f)
        assertEquals("clip.wav", repository.lastDumpFileName)
        assertTrue(result.isSuccess)
    }

    private class FakeRecordingRepository : RecordingRepository {
        override val recorderState: StateFlow<RecorderState> = MutableStateFlow(RecorderState())

        var enableCalls = 0
        var disableCalls = 0
        var startCalls = 0
        var stopCalls = 0
        var dumpCalls = 0

        var lastStartMemory: Float? = null
        var lastDumpMemory: Float? = null
        var lastDumpFileName: String? = null

        var enableResult: Result<Unit> = Result.success(Unit)
        var disableResult: Result<Unit> = Result.success(Unit)
        var startResult: Result<Unit> = Result.success(Unit)
        var stopResult: Result<Unit> = Result.success(Unit)
        var dumpResult: Result<Unit> = Result.success(Unit)

        override suspend fun enableListening(): Result<Unit> {
            enableCalls += 1
            return enableResult
        }

        override suspend fun disableListening(): Result<Unit> {
            disableCalls += 1
            return disableResult
        }

        override suspend fun startRecording(prependedMemorySeconds: Float): Result<Unit> {
            startCalls += 1
            lastStartMemory = prependedMemorySeconds
            return startResult
        }

        override suspend fun stopRecording(): Result<Unit> {
            stopCalls += 1
            return stopResult
        }

        override suspend fun dumpRecording(memorySeconds: Float, newFileName: String?): Result<Unit> {
            dumpCalls += 1
            lastDumpMemory = memorySeconds
            lastDumpFileName = newFileName
            return dumpResult
        }
    }
}

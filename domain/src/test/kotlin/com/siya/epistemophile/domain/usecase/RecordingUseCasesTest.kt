package com.siya.epistemophile.domain.usecase

import com.siya.epistemophile.domain.model.RecorderState
import com.siya.epistemophile.domain.repository.RecordingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecordingUseCasesTest {
    private val fakeRepository = FakeRecordingRepository()

    @Test
    fun `start listening delegates to repository`() = runTest {
        val useCase = StartListeningUseCase(fakeRepository)

        val result = useCase()

        assertEquals(1, fakeRepository.enableInvocations)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `stop listening delegates to repository`() = runTest {
        val useCase = StopListeningUseCase(fakeRepository)

        val result = useCase()

        assertEquals(1, fakeRepository.disableInvocations)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `start recording delegates to repository`() = runTest {
        val useCase = StartRecordingUseCase(fakeRepository)

        val result = useCase(5f)

        assertEquals(1, fakeRepository.startRecordingInvocations)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `stop recording delegates to repository`() = runTest {
        val useCase = StopRecordingUseCase(fakeRepository)

        val result = useCase()

        assertEquals(1, fakeRepository.stopRecordingInvocations)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `dump recording delegates to repository`() = runTest {
        val useCase = DumpRecordingUseCase(fakeRepository)

        val result = useCase(10f, "test")

        assertEquals(1, fakeRepository.dumpRecordingInvocations)
        assertTrue(result.isSuccess)
    }

    private class FakeRecordingRepository : RecordingRepository {
        private val state = MutableStateFlow(RecorderState())

        var enableInvocations = 0
            private set
        var disableInvocations = 0
            private set
        var startRecordingInvocations = 0
            private set
        var stopRecordingInvocations = 0
            private set
        var dumpRecordingInvocations = 0
            private set

        override val recorderState: StateFlow<RecorderState> = state

        override suspend fun enableListening(): Result<Unit> {
            enableInvocations++
            return Result.success(Unit)
        }

        override suspend fun disableListening(): Result<Unit> {
            disableInvocations++
            return Result.success(Unit)
        }

        override suspend fun startRecording(prependedMemorySeconds: Float): Result<Unit> {
            startRecordingInvocations++
            return Result.success(Unit)
        }

        override suspend fun stopRecording(): Result<Unit> {
            stopRecordingInvocations++
            return Result.success(Unit)
        }

        override suspend fun dumpRecording(memorySeconds: Float, newFileName: String?): Result<Unit> {
            dumpRecordingInvocations++
            return Result.success(Unit)
        }
    }
}

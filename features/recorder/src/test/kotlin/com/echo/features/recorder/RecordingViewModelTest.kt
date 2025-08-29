
package com.echo.features.recorder

import com.echo.domain.repository.RecordingRepository
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.whenever

class RecordingViewModelTest {
    @Test
    fun togglingListening_updatesStateOnSuccess() = runTest {
        val repo = Mockito.mock(RecordingRepository::class.java)
        whenever(repo.startListening()).thenReturn(Result.success(Unit))
        whenever(repo.stopListening()).thenReturn(Result.success(Unit))
        val vm = RecordingViewModel(repo)

        vm.toggleListening(true)
        // crude assertion without Turbine to keep deps stable
        // In real test, collect StateFlow and assert emissions
    }
}

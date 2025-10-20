package eu.mrogalski.saidit.di

import com.siya.epistemophile.domain.repository.RecordingRepository
import com.siya.epistemophile.domain.usecase.DumpRecordingUseCase
import com.siya.epistemophile.domain.usecase.StartListeningUseCase
import com.siya.epistemophile.domain.usecase.StartRecordingUseCase
import com.siya.epistemophile.domain.usecase.StopListeningUseCase
import com.siya.epistemophile.domain.usecase.StopRecordingUseCase
import com.siya.epistemophile.recorder.AndroidRecordingRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RecorderBindingsModule {
    @Binds
    @Singleton
    abstract fun bindRecordingRepository(
        impl: AndroidRecordingRepository
    ): RecordingRepository
}

@Module
@InstallIn(SingletonComponent::class)
object RecorderUseCasesModule {
    @Provides fun provideDispatcher(): CoroutineDispatcher = Dispatchers.Default
    @Provides fun provideStartListening(repo: RecordingRepository) = StartListeningUseCase(repo)
    @Provides fun provideStopListening(repo: RecordingRepository) = StopListeningUseCase(repo)
    @Provides fun provideStartRecording(repo: RecordingRepository) = StartRecordingUseCase(repo)
    @Provides fun provideStopRecording(repo: RecordingRepository) = StopRecordingUseCase(repo)
    @Provides fun provideDumpRecording(repo: RecordingRepository) = DumpRecordingUseCase(repo)
}

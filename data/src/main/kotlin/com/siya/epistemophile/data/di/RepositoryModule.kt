package com.siya.epistemophile.data.di

import com.siya.epistemophile.data.recording.RecordingRepositoryImpl
import com.siya.epistemophile.domain.repository.RecordingRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindRecordingRepository(impl: RecordingRepositoryImpl): RecordingRepository
}

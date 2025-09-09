package eu.mrogalski.saidit.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAudioConfig(): AudioConfig {
        return AudioConfig(sampleRate = 48000, channels = 1)
    }

    data class AudioConfig(
        val sampleRate: Int,
        val channels: Int
    )
}

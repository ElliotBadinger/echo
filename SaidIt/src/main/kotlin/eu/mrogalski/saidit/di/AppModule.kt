package eu.mrogalski.saidit.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Application-level dependency injection module.
 * 
 * Note: Audio configuration is handled via SharedPreferences in SaidItService
 * because it's user-configurable through SettingsActivity (8kHz/16kHz/48kHz options).
 * See docs/architecture/audio-config-decision.md for architectural rationale.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    // Currently empty - audio configuration is user-managed via SharedPreferences
    // Future application-level dependencies should be added here
}

# Audio Configuration Architecture Decision

## Issue
AppModule provides a hardcoded AudioConfig via dependency injection, but SaidItService reads audio configuration directly from SharedPreferences, creating an architectural disconnect.

## Analysis

### Current Implementation
- **AppModule.provideAudioConfig()**: Returns hardcoded AudioConfig(sampleRate = 48000, channels = 1)
- **SaidItService.onCreate()**: Reads sample rate from SharedPreferences using SaidIt.SAMPLE_RATE_KEY
- **SettingsActivity**: Allows users to configure audio quality (8kHz, 16kHz, 48kHz) and stores in SharedPreferences

### User Configuration Evidence
From SettingsActivity.java (lines 60-66):
```java
int sampleRate = 8000; // Default to 8kHz
if (checkedId == R.id.quality_16kHz) {
    sampleRate = 16000;
} else if (checkedId == R.id.quality_48kHz) {
    sampleRate = 48000;
}
service.setSampleRate(sampleRate);
```

## Architectural Decision

**DECISION: Remove AudioConfig from AppModule and keep SharedPreferences-based configuration**

### Rationale
1. **User Configurability**: Audio quality settings are user-configurable through SettingsActivity UI
2. **Dynamic Configuration**: Sample rate can be changed at runtime (8kHz/16kHz/48kHz options)
3. **Persistence**: User preferences must persist across app restarts
4. **Service Independence**: SaidItService should not depend on DI for basic configuration that users control

### Implementation Changes Required

1. **Remove AudioConfig from AppModule**:
   - Delete `provideAudioConfig()` method
   - Delete `AudioConfig` data class
   - This eliminates the unused hardcoded configuration

2. **Update AppModule tests**:
   - Remove tests related to AudioConfig
   - Focus tests on actual DI bindings if any are added in the future
   - Currently AppModule should be empty or have minimal content

3. **Document the decision**:
   - Update architecture documentation to clarify that audio configuration is user-managed
   - Explain why SharedPreferences is the correct approach for this use case

## Benefits
- **Eliminates Architectural Confusion**: No more disconnect between DI and actual usage
- **Maintains User Control**: Users retain ability to configure audio quality
- **Simplifies DI**: Removes unnecessary dependency injection for user-configurable values
- **Clear Separation of Concerns**: DI for services, SharedPreferences for user configuration

## Impact
- **Low Risk**: AudioConfig was not being used by SaidItService anyway
- **Code Simplification**: Removes unused DI configuration
- **Improved Architecture**: Aligns DI with actual usage patterns

## Alternative Considered
**Alternative**: Inject SharedPreferences into SaidItService and use DI to provide preference access.

**Rejected because**: 
- SaidItService is a Service, not an Activity/Fragment, making Hilt injection more complex
- Audio configuration changes need immediate effect, which SharedPreferences provides naturally
- The current pattern is simple and works well for user preferences

## Conclusion
The existing SharedPreferences-based approach in SaidItService is architecturally correct. The AudioConfig in AppModule was the architectural mistake and should be removed.

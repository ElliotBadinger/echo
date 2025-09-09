# WARP.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

## Project Overview

Echo is a modern Android application for continuous background audio recording with a "time-travel" feature - allowing users to save clips from moments that already happened. Built using Clean Architecture with MVVM pattern, Kotlin Coroutines, and Hilt dependency injection.

**Package Structure:**
- Main package: `com.siya.epistemophile`
- Legacy package: `eu.mrogalski.saidit` (gradually migrating)

## Agent Onboarding System

**🚨 CRITICAL FOR NEW AGENTS: Run health check FIRST before any development work!**

### Quick Start (< 2 minutes)
```bash
# Essential first command - validates environment and core functionality
bash scripts/agent/healthcheck.sh

# Or just environment validation if time critical
bash scripts/agent/healthcheck.sh --tier 0
```

### Health Check Tiers
- **Tier 0**: Environment validation (Java, Gradle, Android SDK, network)
- **Tier 1**: Quick compile check (fail-fast feedback)
- **Tier 2**: Core unit tests (domain, data, core modules)
- **Tier 3**: Android tests (optional with `--with-android`)
- **Tier 4**: Coverage and lint (optional with `--with-full`)

### Key Agent Resources
- `docs/agent-workflow/quick-start-guide.md`: Immediate steps for new agents
- `docs/project-state/health-dashboard.md`: Real-time project status
- `scripts/agent/healthcheck.sh`: Tiered validation tool

### Agent Success Pattern
1. **Validate**: `bash scripts/agent/healthcheck.sh --tier 0-1` (30s)
2. **Develop**: Make focused changes
3. **Test**: `bash scripts/agent/healthcheck.sh --tier 2` (2 min)
4. **Commit**: Manual git commands only
5. **Monitor**: Check CI results

## Essential Development Commands

### Build Commands
```bash
# Clean and build entire project
./gradlew clean build

# Build with error continuation (useful for debugging)
./gradlew clean build --continue

# Install debug version on device
./gradlew installDebug

# Run all tests
./gradlew test

# Run tests for specific module
./gradlew :features:recorder:test
./gradlew :SaidIt:test
```

### Testing Commands
```bash
# Run unit tests with coverage
./gradlew jacocoAll

# Run specific test class
./gradlew test --tests "RecordingViewModelTest"

# Check test compilation only
./gradlew testClasses
```

### Project Health Commands
```bash
# 🚨 PREFERRED: Agent health check (comprehensive validation)
bash scripts/agent/healthcheck.sh

# Quick environment check only
bash scripts/agent/healthcheck.sh --tier 0

# Traditional Gradle verification
./gradlew --version

# Check dependencies
./gradlew dependencies

# Find Kotlin/Java source files
find . -name "*.kt" -o -name "*.java" | grep -v build | head -20
```

## Architecture Overview

### Module Structure
```
echo/
├── SaidIt/                    # Main app module (legacy name)
├── domain/                    # Pure Kotlin domain layer
├── data/                     # Data layer with repositories
├── features/recorder/        # Recording feature module
├── audio/                    # Audio processing module
└── core/                     # Core utilities and shared code
```

### Clean Architecture Layers

**Domain Layer** (`domain/`)
- Contains business logic and interfaces
- Pure Kotlin module (no Android dependencies)
- Key interfaces: `RecordingRepository`
- Use cases: `StartListeningUseCase`, `StopListeningUseCase`

**Data Layer** (`data/`)
- Repository implementations
- Dependency injection modules
- Currently has stub implementation for audio recording

**Presentation Layer** (`features/recorder/`, `SaidIt/`)
- ViewModels using Kotlin Coroutines and StateFlow
- UI components (currently Java-based, migrating to Kotlin)
- Dependency injection with Hilt

### Key Design Patterns
- **MVVM with ViewModels**: `RecordingViewModel` for UI state management
- **Repository Pattern**: `RecordingRepository` interface with `RecordingRepositoryImpl`
- **Use Cases**: Clean separation of business logic
- **Dependency Injection**: Hilt for managing dependencies
- **Coroutines**: For asynchronous operations and reactive programming

## Build Configuration

### Technology Stack
- **Android SDK**: Target SDK 34, Min SDK 30
- **Kotlin**: 1.9.25 with Coroutines 1.8.1
- **Java**: Version 17 compatibility
- **Build Tool**: Gradle 8.13 with Kotlin DSL
- **Testing**: JUnit 4.13.2, Mockito 5.11.0, Robolectric 4.11.1
- **DI**: Hilt 2.51.1

### Version Catalog (`gradle/libs.versions.toml`)
Uses Gradle version catalog for dependency management. Key versions:
- AGP: 8.7.0
- Kotlin: 1.9.25
- Hilt: 2.51.1
- AndroidX Lifecycle: 2.8.6

### Build Features
- ProGuard enabled for release builds
- Jacoco test coverage reports
- Kapt annotation processing for Hilt
- Build config generation enabled

## Development Workflow

### Current Project State
- **Build Status**: 100% operational, all tests passing
- **Architecture**: Migrating from monolithic Java to modular Kotlin
- **Testing**: Comprehensive test suite with high coverage
- **CI/CD**: GitHub Actions pipeline fully functional

### Critical Development Rules

**TIER 1 Priority (Fix First):**
- Build failures, compilation errors, test failures
- Runtime crashes, dependency issues

**TIER 2 Priority (Incremental Improvements):**
- Code quality, architecture improvements
- Small feature additions, bug fixes

**TIER 3 Priority (Major Features):**
- Large architectural changes, new major functionality

### Testing Strategy
- Unit tests for all ViewModels and Use Cases
- Mockito for mocking dependencies
- Robolectric for Android-specific testing
- Coroutine testing with `StandardTestDispatcher`
- Architecture components testing with `InstantTaskExecutorRule`

### Migration Strategy
Project is actively migrating Java classes to Kotlin:
- **Impact-First Selection**: Choose files with meaningful business logic (50+ lines)
- **Comprehensive Testing Mandatory**: Integration tests, not just annotation validation
- **Architectural Investigation**: Identify and fix design issues during conversion
- **Backward Compatibility**: Maintain during transition
- **Modern Patterns**: Use coroutines, data classes, extension functions

### 🚨 Migration Quality Requirements
**Each Kotlin migration MUST include:**
1. **Integration Tests**: Verify actual framework integration (Hilt, Android, etc.)
2. **Behavioral Testing**: Test real use cases, not just method signatures
3. **Dependency Verification**: Confirm injected dependencies are actually used
4. **Error Scenarios**: Edge cases, null handling, invalid inputs
5. **Architectural Audit**: Document any discovered design issues

**FORBIDDEN Migration Patterns:**
- Converting only trivial files (< 30 lines)
- Tests that only check annotations exist
- Skipping integration with actual Android framework
- Ignoring discovered architectural problems

## Common Development Tasks

### Adding a New Feature Module
1. Create module in `settings.gradle`
2. Add `build.gradle.kts` with Android library plugin
3. Set up Hilt dependency injection
4. Create domain interfaces in `domain/` module
5. Implement in data layer
6. Add ViewModels and UI components

### Testing Patterns
```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class MyViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    private val testDispatcher = StandardTestDispatcher()
    
    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }
    
    @Test
    fun testSomething() = runTest(testDispatcher) {
        // Test implementation with mocked dependencies
    }
}
```

### Dependency Injection Setup
**Repository Binding:**
```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindRecordingRepository(impl: RecordingRepositoryImpl): RecordingRepository
}
```

**ViewModel Injection:**
```kotlin
class RecordingViewModel @Inject constructor(
    private val repository: RecordingRepository,
    private val startListeningUseCase: StartListeningUseCase
) : ViewModel()
```

## Troubleshooting

### Build Issues
- **Kapt failures**: Check annotation processing setup, disable build cache if needed
- **Memory issues**: Increase Gradle JVM heap size
- **Version conflicts**: Check `gradle/libs.versions.toml` for consistent versioning

### Testing Issues
- **Coroutine tests**: Ensure proper `TestDispatcher` setup
- **Android mocking**: Use Robolectric or avoid Android framework dependencies in unit tests
- **Timing issues**: Use `runTest` and proper coroutine testing patterns

### Git Workflow
- Always use manual git commands (avoid automated tooling that may cause conflicts)
- Commit frequently with descriptive messages
- Format: `"Agent Session [DATE]: Description"`

## Documentation System

The project has extensive documentation in `docs/` directory:
- **Agent Workflow**: Guidelines for AI development
- **Frameworks**: Technical implementation strategies  
- **Project State**: Current status and priorities
- **Templates**: Standardized documentation formats

### Key Documentation Files
- `docs/agent-workflow/core-principles.md`: Non-negotiable development rules
- `docs/project-state/current-status.md`: Live project state
- `docs/agent-workflow/session-checklist.md`: Development workflow

### For AI Agents
- **FIRST STEP**: Run `bash scripts/agent/healthcheck.sh` for validation
- Check `docs/project-state/health-dashboard.md` for current status
- Read core principles before making changes
- Use manual git commands only
- Focus on small, testable changes
- Document significant changes using provided templates
- Verify all tests pass before claiming work complete

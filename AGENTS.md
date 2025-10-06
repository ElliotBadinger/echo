# Repository Guidelines

## Project Structure & Module Organization
- `SaidIt/`: legacy app shell with manifests, Hilt entry points, UI wiring.
- `features/recorder/`: recorder screens, ViewModels, and StateFlow models.
- `domain/`: pure Kotlin use cases and repository contracts; tests in `domain/src/test`.
- `data/`: repository implementations, DI modules, and fakes under `data/src/test`.
- `audio/`: low-level capture and buffering utilities.
- `core/`: shared extensions, dispatchers, and error handling.
Documentation lives in `docs/`; automation scripts (health check, releases) sit in `scripts/`.

## Build, Test, and Development Commands
Run `bash scripts/agent/healthcheck.sh --tier 0` after setup, `--tier 1` for compile smoke, and `--tier 2` before submitting. Full build: `./gradlew clean build`. Assemble recorder only with `./gradlew :features:recorder:assembleDebug`. JVM tests: `./gradlew test` or module-specific (`./gradlew :domain:test`). Coverage: `./gradlew jacocoAll`. Install on device: `./gradlew installDebug`.

## Coding Style & Naming Conventions
Use Kotlin official style with 4-space indentation and explicit visibility for shared APIs. Prefer immutable data classes, sealed hierarchies, and coroutine-based flows. Package names are feature-first (`com.siya.epistemophile.recorder.ui`). Functions and vars are lowerCamelCase; constants in companion objects are UPPER_SNAKE_CASE. Reformat in Android Studio and run `./gradlew lint` before pushing.

## Testing Guidelines
Frameworks: JUnit4, Mockito 5, and Robolectric for Android-facing code. Name tests by behavior (`RecordingViewModelTest_onStart_emitsListening`). Use `runTest` with `StandardTestDispatcher` for coroutine logic. JVM tests live in `src/test`, instrumented suites in `src/androidTest`. Extend or add tests whenever behavior shifts and rerun the health check tier 2 gate.

## Migration & Architecture Expectations
Migration from `eu.mrogalski.saidit` to `com.siya.epistemophile` targets high-impact Kotlin conversions (>50 LOC). Each migration must validate DI wiring, cover happy/error paths, and document issues uncovered. Maintain Clean Architecture: ViewModels talk to use cases, repositories wrap data sources, and asynchronous work remains inside coroutines.

## Commit & Pull Request Guidelines
Commit in focused slices with messages `Agent Session YYYY-MM-DD: imperative summary`; reference issues in the body when relevant. Pull requests must state intent, list validation commands, attach screenshots for UI tweaks, and tag responsible reviewers. Rebase onto `main`, ensure CI is green, and leave TODOs only when linked to open tickets.

## Agent Workflow Checklist
1. Validate: run the tiered health check (`--tier 0/1/2`) before coding, testing, and submitting.
2. Develop: keep scope tight, avoid unrelated file churn.
3. Test: rerun tier 2 or targeted Gradle tasks to catch regressions early.
4. Commit: use manual git commands and follow the message template.
5. Monitor: log risks in `docs/project-state/health-dashboard.md` and track CI outcomes.

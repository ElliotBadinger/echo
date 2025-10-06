# Kotlin Conversion Roadmap

## Current Status
- All modules outside `SaidIt/` are already written in Kotlin (domain, data, core, audio, features/recorder).
- Legacy `SaidIt/` module still contains the app shell, presentation wiring, and DSP helpers in Java.
- Robolectric, JVM, and health-check tiers 0–3 pass after a clean build, so the remaining migration can proceed incrementally.

## Remaining Java Surface
- **UI Shell (`eu.mrogalski.saidit`)** – 7 activity/adapter classes (~825 LOC) drive legacy screens and navigation.
- **DSP & PCM helpers (`simplesound`)** – 16 classes (~1 050 LOC) provide audio framing and WAV support.
- **Instrumentation Tests** – 4 Robolectric/espresso suites plus 1 unit test (~250 LOC) under `src/androidTest` and `src/test`.

## Workstreams
1. **Presentation Layer Rewrite**
   - Convert `SaidItActivity`, `SettingsActivity`, `RecordingsAdapter`, and onboarding UI classes to Kotlin.
   - Align with new architecture (ViewModels, DI) and replace deprecated Android APIs while porting.
2. **Audio Utility Migration**
   - Port `simplesound` DSP/PCM classes to Kotlin and relocate into `audio/` if practical.
   - Add JVM tests that exercise framing, windowing, and WAV IO edge cases.
3. **Instrumentation & Test Suite Refresh**
   - Recreate the remaining Java tests in Kotlin, expanding scenarios to cover error states, permission flows, and autosave.
   - Standardize on coroutines test utilities and shared `MainDispatcherRule`.
4. **Clean-up & Tooling**
   - Remove Java-only build flags, shrink unused kapt configuration, and enable lint checks once Kotlin-only.

## Proposed Tickets
1. **ECHO-201 – Convert Legacy Activities to Kotlin**
   - Scope: `SaidItActivity`, `SettingsActivity`, onboarding fragments/pager, adapter wiring.
   - Definition of done: Kotlin replacements with equivalent behaviour, tests updated, Java files removed.
2. **ECHO-202 – Port Recording Recycler Adapter & UI Helpers**
   - Scope: `RecordingsAdapter`, related view holders/utilities, plus migration to Kotlin data classes.
   - DoD: Kotlin adapter with unit/UI tests covering empty/error states.
3. **ECHO-203 – Migrate DSP/PCM Library**
   - Scope: all `simplesound` classes; optional relocation into `audio` module.
   - DoD: Kotlin implementations, new unit tests for numerical accuracy and IO edge cases.
4. **ECHO-204 – Rewrite Instrumentation Suite in Kotlin**
   - Scope: espresso/Robolectric tests under `src/androidTest/java` and `src/test/java`.
   - DoD: Kotlin tests using shared rules, expanded coverage for autosave, background service, and fragment flows.
5. **ECHO-205 – Final Java Removal & Tooling Cleanup**
   - Scope: remove residual Java configs, update Gradle options (e.g., drop kapt duplicates), enable strict Kotlin compiler flags.
   - DoD: repository contains no `.java` sources, health-check tier 4 (coverage + lint) passes, documentation updated.

## Validation Plan
- Run `bash scripts/agent/healthcheck.sh --tier 0-3 --with-android` after each ticket; tier 4 before merge of ECHO-205.
- Extend JVM tests with property-based or table-driven cases for audio helpers.
- Capture migration risks and mitigations in `docs/project-state/health-dashboard.md` during the effort.

## Dependencies & Risk Notes
- DSP migration (ECHO-203) should land before adapter refactors rely on Kotlin-only audio APIs.
- Ensure Hilt modules are updated when activities move to Kotlin to avoid classpath mismatches.
- Watch for behavioural drift in autosave/background service; keep instrumentation tests green during each step.

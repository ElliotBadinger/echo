# Kotlin Conversion Roadmap

## Current Status
- All modules outside `SaidIt/` are already written in Kotlin (domain, data, core, audio, features/recorder).
- Legacy `SaidIt/` presentation shell has been converted to Kotlin; remaining Java sources are limited to generated stubs and legacy instrumentation/unit tests.
- Robolectric, JVM, and health-check tiers 0–3 pass after a clean build, so the remaining migration can proceed incrementally.

## Remaining Java Surface
- **Instrumentation & Legacy Unit Tests** – 4 Robolectric/espresso suites plus 1 unit test (~250 LOC) under `src/androidTest` and `src/test`.

## Workstreams
1. **Presentation Layer Rewrite** *(partially complete)*
   - `SaidIt` onboarding flow, toolbar pager, and recordings list adapter now run in Kotlin with new Robolectric coverage for pager titles and adapter grouping behaviour.
   - Remaining scope: service wiring and settings screens should adopt shared ViewModel patterns.
2. **Audio Utility Migration** *(in progress)*
   - `simplesound` PCM/DSP helpers relocated to `audio` as Kotlin implementations with new JVM tests covering WAV IO, windowing coefficients, and normalized frame iteration.
   - Next step: ensure downstream call sites (service save path) adopt the new APIs and remove transient compatibility shims.
3. **Instrumentation & Test Suite Refresh**
   - Recreate the remaining Java tests in Kotlin, expanding scenarios to cover error states, permission flows, and autosave.
   - Standardize on coroutines test utilities and shared `MainDispatcherRule`.
4. **Clean-up & Tooling**
   - Remove Java-only build flags, shrink unused kapt configuration, and enable lint checks once Kotlin-only.

## Proposed Tickets
1. **ECHO-201 – Convert Legacy Activities to Kotlin** *(In progress)*
   - Scope: `SaidItActivity`, `SettingsActivity`, onboarding fragments/pager, adapter wiring.
   - Definition of done: Kotlin replacements with equivalent behaviour, tests updated, Java files removed.
2. **ECHO-202 – Port Recording Recycler Adapter & UI Helpers**
   - Scope: `RecordingsAdapter`, related view holders/utilities, plus migration to Kotlin data classes.
   - DoD: Kotlin adapter with unit/UI tests covering empty/error states.
3. **ECHO-203 – Migrate DSP/PCM Library** *(actively executing)*
   - Scope: all `simplesound` classes; relocation into `audio` module complete with WAV/windowing/frame iterator tests in place.
   - DoD: ensure service consumers switch to the Kotlin APIs, then delete deprecated references and update documentation.
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
- DSP migration (ECHO-203) should land before adapter refactors rely on Kotlin-only audio APIs. New PCM utilities now live in `audio`; verify background service playback still compiles once wired in.
- Ensure Hilt modules are updated when activities move to Kotlin to avoid classpath mismatches.
- Normalization math now reimplements what the `jcaki` JAR previously handled; added JVM tests mitigate regressions, but monitor for edge cases with unsigned 8‑bit WAVs.
- Watch for behavioural drift in autosave/background service; keep instrumentation tests green during each step.

## ECHO-203 Detailed Plan
- **Inventory & Ownership**: Catalogue every class under `SaidIt/src/main/java/simplesound/**` with notes on current consumers (service, tests, adapters). Confirm whether any code in other modules relies on Java-specific APIs.
- **Modulo Conversion Batches**: Port helpers in logical batches (e.g., DSP math, PCM streams, WAV IO) to keep reviews tight and allow incremental verification.
- **API Surface Cleanup**: Kotlin versions now expose strongly-typed helpers in `com.siya.epistemophile.audio`; continue tightening visibility (e.g., sealed list items, factory functions) as consumers adopt them.
- **Test Strategy**: Added deterministic JVM tests for Hamming/Hanning windows, WAV round trips, and normalized frame iteration. Consider property-based expansions (edge sample sizes) in follow-up tickets.
- **Adoption Steps**: Once Kotlin utilities land, update repositories/adapters to consume the new APIs, then delete the legacy Java package and update documentation.

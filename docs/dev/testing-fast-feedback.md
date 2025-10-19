# Testing Fast Feedback Guide

## Quick Start

- `./gradlew fastTests -PskipInstrumentation=true`
  - Runs JVM + Robolectric suites in parallel across `domain`, `data`, `features/recorder`, `audio`, and `SaidIt`.
  - Uses configuration cache, Gradle build cache, and parallel forks tuned to half your logical cores.
  - Skips managed-device instrumentation – perfect for edit→test loops.

- `./gradlew fullTests`
  - Extends `fastTests` and, by default, includes the managed `mediumApi30` emulator (API 30, Google ATD).
  - Pass `-PskipInstrumentation=true` or use `scripts/agent/healthcheck.sh --tier 2` without `--with-android` when virtualization is unavailable.

- `scripts/agent/changed-tests.sh --run`
  - Reads `git diff` against `origin/master` and invokes `fastTests` with `-PincludeTestTasks=…` so only affected modules execute.
  - Use `--base <ref>` and `--head <ref>` to customise the diff range.

## Managed Device Instrumentation

1. Ensure host virtualization is enabled (BIOS/VM settings) and GPU acceleration is supported.
2. Install the SDK components (handled automatically by `scripts/agent/healthcheck.sh`). Manual bootstrap:
   ```bash
   yes | .android-sdk/cmdline-tools/latest/bin/sdkmanager \
     --sdk_root=.android-sdk \
     "platforms;android-33" "platform-tools" \
     "system-images;android-33;google_apis;x86_64" "emulator"
   ```
3. First run: `./gradlew :SaidIt:mediumApi30Setup`. This creates a quick-boot snapshot. Subsequent runs reuse it.
4. If the emulator fails to launch on this laptop (exit code 137), rerun with `-PskipInstrumentation=true` and file the environment issue in `docs/project-state/health-dashboard.md`.

## Health Check Integration

- Tier 1 (`--tier 1`): `fastTests -PskipInstrumentation=true`
- Tier 2 (`--tier 2`): `fullTests` (adds managed device unless `--with-android` is omitted)
- Tier 3 (`--tier 3 --with-android`): explicit managed-device rerun for debugging
- Tier 4 (`--tier 4 --with-full`): `jacocoAll` + lint (unchanged)

## Tips & Flags

- `-PincludeTestTasks=:domain:test,:data:test` narrows `fastTests` to the listed tasks.
- `-PskipInstrumentation=true` forces `fullTests` to behave like Tier 1 (useful on CI plan nodes without virtualization).
- `--stacktrace --scan` remain available for diagnostics; configuration cache is on by default.

## Known Limitations

- Managed devices require ≥8 GB free RAM and hardware virtualization; low-power machines may need to rely on Tier 1 until a physical device is connected.
- `SaidIt` instrumentation still exercises legacy UI flows; additional pruning is tracked in `docs/project-state/health-dashboard.md` under “Testing”.
- ATD images deny microphone/storage permissions even with `grantAllPermissions`; expect to use a physical device (`connectedDebugAndroidTest`) for complete coverage until tests are refactored.

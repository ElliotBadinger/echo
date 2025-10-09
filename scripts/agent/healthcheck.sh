#!/usr/bin/env bash
set -euo pipefail

# Echo Agent Health Check
# Tiers:
#  0 - Environment + SDK + cache sanity
#  1 - Quick compile (fail-fast)
#  2 - Core unit tests (fast modules)
#  3 - Android/Robolectric tests (optional)
#  4 - Coverage and lint (optional)
#
# Usage examples:
#   bash scripts/agent/healthcheck.sh --tier 0
#   bash scripts/agent/healthcheck.sh --tier 0-2
#   bash scripts/agent/healthcheck.sh --all
#   bash scripts/agent/healthcheck.sh --tier 1 --no-cache-restore
#
# Notes:
# - Never prints secrets. Avoid echoing env values that may contain credentials.
# - Designed to be idempotent and fast.

ROOT_DIR=$(cd "$(dirname "$0")/../.." && pwd)
SDK_DIR="$ROOT_DIR/.android-sdk"
CMDLINE_TOOLS_ZIP="commandlinetools-linux-11076708_latest.zip"
CMDLINE_TOOLS_URL="https://dl.google.com/android/repository/${CMDLINE_TOOLS_ZIP}"
REQUIRED_SDK_PACKAGES=(
  "platforms;android-34"
  "platforms;android-33"
  "build-tools;34.0.0"
  "platform-tools"
)
cd "$ROOT_DIR"

TIER_RANGE=""
RUN_ALL=false
RESTORE_CACHE=true
INCLUDE_ANDROID_T3=false
INCLUDE_FULL_T4=false
GRADLEW="./gradlew"

log() { printf "[health] %s\n" "$*"; }
warn() { printf "[health][warn] %s\n" "$*"; }
err() { printf "[health][error] %s\n" "$*"; }

have_cmd() { command -v "$1" >/dev/null 2>&1; }

ensure_local_properties() {
  local sdk_path="$1"
  local props_file="$ROOT_DIR/local.properties"
  local escaped_path="$sdk_path"
  if [[ -f "$props_file" ]]; then
    if grep -q '^sdk.dir=' "$props_file"; then
      if ! grep -q "^sdk.dir=${escaped_path//\//\/}$" "$props_file"; then
        local tmp_file
        tmp_file=$(mktemp)
        sed "s#^sdk.dir=.*#sdk.dir=${escaped_path//\//\/}#" "$props_file" > "$tmp_file"
        mv "$tmp_file" "$props_file"
        log "Updated sdk.dir in local.properties -> $escaped_path"
      fi
    else
      printf '\n%s\n' "sdk.dir=${escaped_path//\//\/}" >> "$props_file"
      log "Appended sdk.dir to existing local.properties -> $escaped_path"
    fi
  else
    printf 'sdk.dir=%s\n' "$escaped_path" > "$props_file"
    log "Generated local.properties with sdk.dir -> $escaped_path"
  fi
}

ensure_android_licenses() {
  local sdk_path="$1"
  mkdir -p "$sdk_path/licenses"
  printf 'd56f5187479451eabf01fb78af6dfcb131a6481e\n' > "$sdk_path/licenses/android-sdk-license"
  printf '84831b9409646a918e30573bab4c9c91346d8abd\n' > "$sdk_path/licenses/android-sdk-preview-license"
}

bootstrap_cmdline_tools() {
  local sdk_path="$1"
  local tools_root="$sdk_path/cmdline-tools"
  local archive_path="$tools_root/$CMDLINE_TOOLS_ZIP"

  mkdir -p "$tools_root"
  if [[ ! -x "$tools_root/latest/bin/sdkmanager" ]]; then
    if [[ ! -f "$archive_path" ]]; then
      log "Downloading Android command line tools..."
      curl -sSL "$CMDLINE_TOOLS_URL" -o "$archive_path"
    else
      log "Reusing cached command line tools archive"
    fi
    rm -rf "$tools_root/latest" "$tools_root/cmdline-tools"
    unzip -q "$archive_path" -d "$tools_root"
    mv "$tools_root/cmdline-tools" "$tools_root/latest"
  fi
}

install_minimal_android_sdk() {
  local sdk_path="$1"
  bootstrap_cmdline_tools "$sdk_path"
  ensure_android_licenses "$sdk_path"
  local sdkmanager="$sdk_path/cmdline-tools/latest/bin/sdkmanager"
  if [[ ! -x "$sdkmanager" ]]; then
    err "sdkmanager not found after extracting command line tools"
    return 1
  fi
  log "Installing minimal Android SDK components into $sdk_path"
  yes | "$sdkmanager" --sdk_root="$sdk_path" "${REQUIRED_SDK_PACKAGES[@]}" >/dev/null
}

ensure_android_sdk() {
  local sdk_root
  if [[ -n "${ANDROID_SDK_ROOT:-}" ]]; then
    sdk_root="$ANDROID_SDK_ROOT"
  else
    sdk_root="$SDK_DIR"
    export ANDROID_SDK_ROOT="$sdk_root"
    export ANDROID_HOME="$sdk_root"
  fi

  local needs_install=false
  for pkg in "${REQUIRED_SDK_PACKAGES[@]}"; do
    local pkg_path="${pkg//;/\/}"
    if [[ ! -d "$sdk_root/$pkg_path" ]]; then
      needs_install=true
      break
    fi
  done

  if [[ "$needs_install" == true ]]; then
    log "Android SDK components missing; bootstrapping local SDK at $sdk_root"
    install_minimal_android_sdk "$sdk_root"
  fi

  if [[ -d "$sdk_root" ]]; then
    log "Android SDK: $sdk_root"
    ensure_android_licenses "$sdk_root"
    ensure_local_properties "$sdk_root"
  else
    warn "Android SDK not found and automatic bootstrap failed"
  fi
}

parse_args() {
  while [[ $# -gt 0 ]]; do
    case "$1" in
      --tier)
        shift
        TIER_RANGE="$1"
        ;;
      --all)
        RUN_ALL=true
        ;;
      --no-cache-restore)
        RESTORE_CACHE=false
        ;;
      --with-android)
        INCLUDE_ANDROID_T3=true
        ;;
      --with-full)
        INCLUDE_FULL_T4=true
        ;;
      -h|--help)
        usage
        exit 0
        ;;
      *)
        warn "Unknown arg: $1"
        ;;
    esac
    shift
  done
}

usage() {
  cat <<EOF
Echo Agent Health Check

Usage: bash scripts/agent/healthcheck.sh [--tier N | --tier N-M | --all] [--no-cache-restore] [--with-android] [--with-full]

Tiers:
  0  Environment checks (Java/Kotlin/Gradle), Android SDK + licenses, network sanity, project layout
  1  Quick compile tasks (fail-fast): :SaidIt:compileDebugKotlin and a small assemble in :domain
  2  Core unit tests (fast): :domain:test :data:test :core:test
  3  Android/Robolectric (optional): :features:recorder:test and :SaidIt:test
  4  Coverage + lint (optional): jacocoAll, lint where available

Examples:
  bash scripts/agent/healthcheck.sh --tier 0
  bash scripts/agent/healthcheck.sh --tier 0-2
  bash scripts/agent/healthcheck.sh --all
  bash scripts/agent/healthcheck.sh --tier 3 --with-android
EOF
}

header() {
  echo ""
  echo "=================================================="
  echo "$1"
  echo "=================================================="
}

check_env() {
  header "Tier 0: Environment and SDK checks"

  log "OS: $(uname -a)"
  if have_cmd java; then
    log "Java: $(java -version 2>&1 | head -n1)"
  else
    err "Java not found. Install JDK 17 as per WARP.md"
    return 1
  fi

  if have_cmd $GRADLEW; then
    log "Gradle wrapper present"
    $GRADLEW --version || { err "gradle --version failed"; return 1; }
  else
    err "Gradle wrapper not found. Ensure ./gradlew exists and is executable"
    return 1
  fi

  # Android SDK checks (best-effort, non-fatal if missing but warned)
  ensure_android_sdk

  # Network sanity (best-effort, short timeouts)
  if have_cmd curl; then
    curl -sSfL --max-time 5 https://repo.maven.apache.org/maven2/ >/dev/null && log "Network to Maven Central: OK" || warn "Cannot reach Maven Central quickly"
    curl -sSfL --max-time 5 https://services.gradle.org/ >/dev/null && log "Network to Gradle services: OK" || warn "Cannot reach Gradle services quickly"
  else
    warn "curl not available; skipping network checks"
  fi

  # Project layout sanity
  for p in "SaidIt" "domain" "data" "features/recorder" "core" ".github/workflows"; do
    if [[ -d "$p" ]]; then
      log "Found module/path: $p"
    else
      warn "Missing expected path: $p"
    fi
  done
}

quick_compile() {
  header "Tier 1: Quick compile"
  local args=("--no-daemon" "--stacktrace" "-q")
  if [[ "$RESTORE_CACHE" == true ]]; then
    log "Caches enabled (default Gradle caches)"
  else
    args+=("--no-build-cache")
    warn "Gradle build cache disabled by flag"
  fi
  log "Running compile tasks: :SaidIt:compileDebugKotlin and :domain:assemble"
  $GRADLEW "${args[@]}" :SaidIt:compileDebugKotlin :domain:assemble
}

core_unit_tests() {
  header "Tier 2: Core unit tests (fast)"
  local args=("--no-daemon" "--stacktrace" "--continue")
  $GRADLEW "${args[@]}" :domain:test :data:test :core:test
}

android_tests() {
  header "Tier 3: Android/Robolectric tests (optional)"
  if [[ "$INCLUDE_ANDROID_T3" != true ]]; then
    warn "Skipped. Enable with --with-android"
    return 0
  fi
  local args=("--no-daemon" "--stacktrace" "--continue")
  local primary_tasks=(":features:recorder:test")
  log "Running Android unit tests: ${primary_tasks[*]}"
  if ! $GRADLEW "${args[@]}" "${primary_tasks[@]}"; then
    return 1
  fi
  log "Primary Android unit tests passed; executing :SaidIt:test"
  $GRADLEW "${args[@]}" :SaidIt:test
}

coverage_and_lint() {
  header "Tier 4: Coverage and lint (optional)"
  if [[ "$INCLUDE_FULL_T4" != true ]]; then
    warn "Skipped. Enable with --with-full"
    return 0
  fi
  local args=("--no-daemon" "--stacktrace" "--continue")
  $GRADLEW "${args[@]}" jacocoAll || warn "jacocoAll reported issues"
  # Run lint where available; do not fail the script if lint fails
  $GRADLEW "${args[@]}" :SaidIt:lintDebug || warn ":SaidIt:lintDebug reported issues"
}

run_tiers() {
  local from=0 to=0
  if $RUN_ALL; then
    from=0; to=4
  elif [[ -n "$TIER_RANGE" ]]; then
    if [[ "$TIER_RANGE" =~ ^([0-4])$ ]]; then
      from=${BASH_REMATCH[1]}
      to=$from
    elif [[ "$TIER_RANGE" =~ ^([0-4])-([0-4])$ ]]; then
      from=${BASH_REMATCH[1]}
      to=${BASH_REMATCH[2]}
      if (( to < from )); then
        err "Invalid tier range: $TIER_RANGE"; exit 1
      fi
    else
      err "Invalid --tier value: $TIER_RANGE"; exit 1
    fi
  else
    # Default to Tier 0-2 for fast signal
    from=0; to=2
  fi

  local overall_rc=0

  for tier in $(seq $from $to); do
    case $tier in
      0) check_env || overall_rc=1 ;;
      1) quick_compile || overall_rc=1 ;;
      2) core_unit_tests || overall_rc=1 ;;
      3) android_tests || overall_rc=1 ;;
      4) coverage_and_lint || overall_rc=1 ;;
    esac
  done

  echo ""
  echo "==================== Summary ====================="
  if [[ $overall_rc -eq 0 ]]; then
    echo "All selected tiers completed successfully."
  else
    echo "One or more tiers reported issues. See logs above."
  fi
  echo "Selected tiers: $from-$to"
  echo "Flags: cache_restore=$RESTORE_CACHE, android_t3=$INCLUDE_ANDROID_T3, full_t4=$INCLUDE_FULL_T4"
  echo "=================================================="

  return $overall_rc
}

parse_args "$@"
run_tiers


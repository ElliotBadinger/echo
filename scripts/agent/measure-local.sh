#!/usr/bin/env bash
set -euo pipefail

usage() {
  cat <<'EOF'
Usage: scripts/agent/measure-local.sh [options]

Options:
  --runs N                 Number of iterations per command (default: 3)
  --output PATH            Append CSV results to PATH (header auto-added)
  --skip-instrumentation   Skip connected instrumentation suite
  --help                   Show this help

This script measures wall-clock durations for key Gradle workflows to
establish before/after baselines during local optimization.
EOF
}

runs=3
output=""
skip_instrumentation=0

while [[ $# -gt 0 ]]; do
  case "$1" in
    --runs)
      runs="$2"
      shift 2
      ;;
    --output)
      output="$2"
      shift 2
      ;;
    --skip-instrumentation)
      skip_instrumentation=1
      shift 1
      ;;
    --help)
      usage
      exit 0
      ;;
    *)
      echo "Unknown option: $1" >&2
      usage >&2
      exit 1
      ;;
  esac
done

if ! [[ $runs =~ ^[0-9]+$ ]] || [[ $runs -lt 1 ]]; then
  echo "--runs must be a positive integer" >&2
  exit 1
fi

declare -a COMMAND_KEYS
declare -A COMMANDS

COMMAND_KEYS+=("clean_build")
COMMANDS["clean_build"]="./gradlew clean build"

COMMAND_KEYS+=("unit_all")
COMMANDS["unit_all"]="./gradlew test"

COMMAND_KEYS+=("unit_modules")
COMMANDS["unit_modules"]="./gradlew :domain:test :data:test :features:recorder:test"

if [[ $skip_instrumentation -eq 0 ]]; then
  COMMAND_KEYS+=("connected_debug_android_test")
  COMMANDS["connected_debug_android_test"]="./gradlew connectedDebugAndroidTest"
fi

timestamp() {
  date +"%Y-%m-%dT%H:%M:%S%z"
}

record_csv() {
  local key=$1
  local median=$2
  local runs_array=$3

  if [[ -z $output ]]; then
    return
  fi

  local header="timestamp,command,median_seconds,all_runs_seconds"
  if [[ ! -f $output ]]; then
    echo "$header" >"$output"
  fi

  echo "$(timestamp),$key,$median,$runs_array" >>"$output"
}

median_from() {
  local -n values=$1
  local sorted
  IFS=$'\n' sorted=($(printf '%s\n' "${values[@]}" | sort -n))
  local mid=$(( ${#sorted[@]} / 2 ))
  if (( ${#sorted[@]} % 2 == 1 )); then
    printf '%s' "${sorted[$mid]}"
  else
    awk -v a="${sorted[$((mid-1))]}" -v b="${sorted[$mid]}" 'BEGIN { printf "%.3f", (a + b) / 2 }'
  fi
}

run_command() {
  local key=$1
  local cmd=$2
  local -a durations=()
  echo "==> Measuring $key : $cmd" >&2
  for ((i = 1; i <= runs; i++)); do
    echo "  -> Run $i/$runs" >&2
    local start_ns end_ns
    start_ns=$(date +%s.%N)
    if ! eval "$cmd" >/dev/null; then
      echo "Command failed: $cmd" >&2
      exit 1
    fi
    end_ns=$(date +%s.%N)
    local elapsed
    elapsed=$(awk -v start="$start_ns" -v end="$end_ns" 'BEGIN { printf "%.3f", (end - start) }')
    durations+=("$elapsed")
    echo "     duration=${elapsed}s" >&2
  done

  local median
  median=$(median_from durations)
  local joined
  joined=$(IFS=';'; echo "${durations[*]}")
  record_csv "$key" "$median" "$joined"
  echo "<= $key median=${median}s (runs=${joined})" >&2
}

for key in "${COMMAND_KEYS[@]}"; do
  run_command "$key" "${COMMANDS[$key]}"
done

echo "All measurements complete." >&2

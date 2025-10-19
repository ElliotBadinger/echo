#!/usr/bin/env bash
set -euo pipefail

BASE=""
HEAD="HEAD"
EXECUTE=false

usage() {
  cat <<'EOF'
Usage: scripts/agent/changed-tests.sh [--base REF] [--head REF] [--run]

Scans git changes between REF ranges (default: origin/master..HEAD) and maps
them to the smallest set of Gradle test tasks. Intended to pair with
`./gradlew fastTests -PincludeTestTasks=... -PskipInstrumentation=true` for
fast feedback loops.

Options:
  --base REF   Git ref/hash to diff against (default: origin/master)
  --head REF   Git ref/hash for the upper bound (default: HEAD)
  --run        Execute the recommended Gradle command instead of printing it
  --help       Show this message
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --base)
      BASE="$2"
      shift 2
      ;;
    --head)
      HEAD="$2"
      shift 2
      ;;
    --run)
      EXECUTE=true
      shift
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

if [[ -z $BASE ]]; then
  if git rev-parse --verify origin/master >/dev/null 2>&1; then
    BASE="origin/master"
  else
    BASE="$(git merge-base HEAD main 2>/dev/null || git merge-base HEAD master 2>/dev/null || echo HEAD^ )"
  fi
fi

if ! git rev-parse --verify "$BASE" >/dev/null 2>&1; then
  echo "Base ref $BASE is not valid" >&2
  exit 1
fi

if ! git rev-parse --verify "$HEAD" >/dev/null 2>&1; then
  echo "Head ref $HEAD is not valid" >&2
  exit 1
fi

changed_files=$(git diff --name-only "$BASE" "$HEAD")

if [[ -z $changed_files ]]; then
  echo "No changes detected between $BASE and $HEAD." >&2
  cmd="./gradlew fastTests -PskipInstrumentation=true"
  if $EXECUTE; then
    eval "$cmd"
  else
    echo "$cmd"
  fi
  exit 0
fi

declare -A task_map=(
  [":domain:test"]=0
  [":data:test"]=0
  [":features:recorder:test"]=0
  [":audio:testDebugUnitTest"]=0
  [":core:test"]=0
  [":SaidIt:testDebugUnitTest"]=0
)

while IFS= read -r file; do
  case "$file" in
    domain/*) task_map[":domain:test"]=1 ;;
    data/*) task_map[":data:test"]=1 ;;
    features/recorder/*) task_map[":features:recorder:test"]=1 ;;
    audio/*) task_map[":audio:testDebugUnitTest"]=1 ;;
    core/*) task_map[":core:test"]=1 ;;
    SaidIt/*) task_map[":SaidIt:testDebugUnitTest"]=1 ;;
    docs/*|scripts/*|build.gradle.kts|settings.gradle|gradle/*|gradle.properties)
      # Non-source tweaks still benefit from fastTests default
      ;;
    *)
      task_map[":SaidIt:testDebugUnitTest"]=1
      task_map[":domain:test"]=1
      ;;
  esac
done <<< "$changed_files"

selected=()
for task in "${!task_map[@]}"; do
  if [[ ${task_map[$task]} -eq 1 ]]; then
    selected+=("$task")
  fi
done

if [[ ${#selected[@]} -eq 0 ]]; then
  cmd="./gradlew fastTests -PskipInstrumentation=true"
else
  IFS=',' read -r -a include <<< "${selected[*]}"
  include_arg=$(printf '%s,' "${selected[@]}")
  include_arg=${include_arg%,}
  cmd="./gradlew fastTests -PincludeTestTasks=${include_arg} -PskipInstrumentation=true"
fi

if $EXECUTE; then
  eval "$cmd"
else
  echo "$cmd"
fi

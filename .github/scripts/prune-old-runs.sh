#!/usr/bin/env bash
set -euo pipefail

ALLURE_ROOT="${1:?pages/allure-reports path required}"
MAX_RUNS="${2:-10}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=allure-pages-lib.sh
source "${SCRIPT_DIR}/allure-pages-lib.sh"

for branch_dir in "${ALLURE_ROOT}"/*/; do
  [ -d "${branch_dir}" ] || continue
  slug="$(basename "${branch_dir}")"
  is_reserved_allure_dir "${slug}" && continue

  runs=()
  while IFS= read -r run_dir; do
    runs+=("${run_dir}")
  done < <(list_run_dirs "${branch_dir}")

  count="${#runs[@]}"
  if [ "${count}" -le "${MAX_RUNS}" ]; then
    continue
  fi

  to_delete=$((count - MAX_RUNS))
  for ((i = 0; i < to_delete; i++)); do
    echo "Pruning old run: ${runs[i]}"
    rm -rf "${runs[i]}"
  done
done

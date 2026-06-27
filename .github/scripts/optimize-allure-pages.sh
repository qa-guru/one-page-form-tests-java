#!/usr/bin/env bash
set -euo pipefail

ALLURE_ROOT="${1:?pages/allure-reports path required}"
MAX_RUNS="${2:-10}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=allure-pages-lib.sh
source "${SCRIPT_DIR}/allure-pages-lib.sh"

STATIC_ROOT="${ALLURE_ROOT}/_static"

bash "${SCRIPT_DIR}/prune-old-runs.sh" "${ALLURE_ROOT}" "${MAX_RUNS}"

for branch_dir in "${ALLURE_ROOT}"/*/; do
  [ -d "${branch_dir}" ] || continue
  slug="$(basename "${branch_dir}")"
  is_reserved_allure_dir "${slug}" && continue

  if [ -d "${branch_dir}/dashboard" ]; then
    bash "${SCRIPT_DIR}/optimize-allure-plugin.sh" \
      "${branch_dir}/dashboard" "${STATIC_ROOT}/dashboard"
  fi

  while IFS= read -r run_dir; do
    bash "${SCRIPT_DIR}/optimize-allure-run.sh" "${run_dir}" "${STATIC_ROOT}"
  done < <(list_run_dirs "${branch_dir}")
done

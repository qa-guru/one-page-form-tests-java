#!/usr/bin/env bash
set -euo pipefail

ALLURE_BIN="${1:?allure binary path required}"
RESULTS_DIR="${2:?allure results directory required}"
OUTPUT_DIR="${3:?dashboard-en output directory required}"
CONFIG="${4:-allurerc.dashboard-en.json}"

[ -d "${RESULTS_DIR}" ] || exit 0
[ -f "${CONFIG}" ] || exit 0

mkdir -p "${OUTPUT_DIR}"
rm -rf "${OUTPUT_DIR:?}/"*

"${ALLURE_BIN}" dashboard "${RESULTS_DIR}" \
  --config "${CONFIG}" \
  --output "${OUTPUT_DIR}"

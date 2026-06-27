#!/usr/bin/env bash
set -euo pipefail

RUN_DIR="${1:?run directory required}"
STATIC_ROOT="${2:?static root directory required}"

[ -d "${RUN_DIR}" ] || exit 0

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

rm -rf "${RUN_DIR}/dashboard"

if [ -d "${RUN_DIR}/awesome" ]; then
  bash "${SCRIPT_DIR}/optimize-allure-plugin.sh" \
    "${RUN_DIR}/awesome" "${STATIC_ROOT}/awesome"

  cat > "${RUN_DIR}/index.html" <<'EOF'
<!DOCTYPE html>
<html lang="ru">
<head>
  <meta charset="UTF-8">
  <meta http-equiv="refresh" content="0;url=awesome/index.html">
  <title>Allure Report</title>
</head>
<body>
  <p><a href="awesome/index.html">Открыть Allure Report</a></p>
</body>
</html>
EOF
  exit 0
fi

if [ -f "${RUN_DIR}/index.html" ]; then
  bash "${SCRIPT_DIR}/optimize-allure-plugin.sh" \
    "${RUN_DIR}" "${STATIC_ROOT}/awesome"
fi

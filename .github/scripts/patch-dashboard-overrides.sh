#!/usr/bin/env bash
set -euo pipefail

ALLURE_ROOT="${1:?pages/allure-reports path required}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
STATIC_ROOT="${ALLURE_ROOT}/_static/dashboard"
OVERRIDES_SRC="${REPO_ROOT}/dashboard-overrides.css"
LINK_TAG='<link rel="stylesheet" type="text/css" href="../../_static/dashboard/dashboard-overrides.css">'

[ -f "${OVERRIDES_SRC}" ] || exit 0

mkdir -p "${STATIC_ROOT}"
cp "${OVERRIDES_SRC}" "${STATIC_ROOT}/dashboard-overrides.css"

# shellcheck source=allure-pages-lib.sh
source "${SCRIPT_DIR}/allure-pages-lib.sh"

for branch_dir in "${ALLURE_ROOT}"/*/; do
  [ -d "${branch_dir}" ] || continue
  slug="$(basename "${branch_dir}")"
  is_reserved_allure_dir "${slug}" && continue

  for dashboard_name in dashboard dashboard-en; do
    html="${branch_dir}/${dashboard_name}/index.html"
    [ -f "${html}" ] || continue

    HTML_FILE="${html}" LINK_TAG="${LINK_TAG}" python <<'PY'
import os
import pathlib

html = pathlib.Path(os.environ["HTML_FILE"])
link = os.environ["LINK_TAG"]
text = html.read_text(encoding="utf-8")

if "dashboard-overrides.css" in text:
    raise SystemExit(0)

marker = "</head>"
if marker not in text:
    raise SystemExit(0)

html.write_text(text.replace(marker, f"    {link}\n{marker}", 1), encoding="utf-8")
PY
  done
done

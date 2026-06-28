#!/usr/bin/env bash
set -euo pipefail

BRANCH_DIR="${1:?branch directory required}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=allure-pages-lib.sh
source "${SCRIPT_DIR}/allure-pages-lib.sh"
# shellcheck source=render-pages-template.sh
source "${SCRIPT_DIR}/render-pages-template.sh"

branch_name="$(basename "${BRANCH_DIR}")"
latest_run=""

report_entry() {
  local run_id="${1}"
  if [ -f "${BRANCH_DIR}/${run_id}/awesome/index.html" ]; then
    printf '%s' "${run_id}/awesome/index.html"
  elif [ -f "${BRANCH_DIR}/${run_id}/index.html" ]; then
    printf '%s' "${run_id}/index.html"
  else
    printf '%s' ""
  fi
}

if [ -f "${BRANCH_DIR}/latest-run-id.txt" ]; then
  latest_run="$(tr -d '[:space:]' < "${BRANCH_DIR}/latest-run-id.txt")"
fi

if [ -z "${latest_run}" ] || [ ! -d "${BRANCH_DIR}/${latest_run}" ]; then
  while IFS= read -r run_dir; do
    latest_run="$(basename "${run_dir}")"
  done < <(list_run_dirs "${BRANCH_DIR}" | tail -1)
fi

run_links=""
while IFS= read -r run_dir; do
  run_id="$(basename "${run_dir}")"
  entry="$(report_entry "${run_id}")"
  [ -n "${entry}" ] || continue
  run_links="<li><a href=\"${entry}\">Run ${run_id}</a></li>${run_links}"
done < <(list_run_dirs "${BRANCH_DIR}" reverse)

dashboard_frame=""
if [ -f "${BRANCH_DIR}/dashboard/index.html" ]; then
  dashboard_frame='<iframe class="dashboard-frame" src="dashboard/index.html" title="Dashboard"></iframe>'
elif [ -f "${BRANCH_DIR}/index.html" ] && [ ! -f "${BRANCH_DIR}/latest-run-id.txt" ]; then
  dashboard_frame='<iframe class="dashboard-frame" src="index.html" title="Dashboard"></iframe>'
fi

report_cta=""
latest_entry=""
if [ -n "${latest_run}" ]; then
  latest_entry="$(report_entry "${latest_run}")"
fi
if [ -n "${latest_entry}" ]; then
  report_cta="<a class=\"btn primary\" href=\"${latest_entry}\">Открыть полный отчёт (последний прогон)</a>"
else
  report_cta='<p class="empty-state">Полный отчёт пока не опубликован</p>'
fi

if [ -z "${run_links}" ]; then
  run_links="<li>Нет сохранённых прогонов</li>"
fi

if [ -z "${dashboard_frame}" ]; then
  dashboard_frame='<p class="empty-state">Dashboard не сгенерирован</p>'
fi

shell_header="$(render_partial header.html "BRAND_HREF=../")"
shell_footer="$(render_partial footer.html)"

render_template branch.html "${BRANCH_DIR}/index.html" \
  "HEADER=${shell_header}" \
  "FOOTER=${shell_footer}" \
  "BRANCH_NAME=${branch_name}" \
  "RUN_LINKS=${run_links}" \
  "REPORT_CTA=${report_cta}" \
  "DASHBOARD_FRAME=${dashboard_frame}"

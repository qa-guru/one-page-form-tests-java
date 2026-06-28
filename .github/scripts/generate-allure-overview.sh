#!/usr/bin/env bash
set -euo pipefail

PAGES_ALLURE="${1:?pages/allure-reports path required}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=allure-pages-lib.sh
source "${SCRIPT_DIR}/allure-pages-lib.sh"
# shellcheck source=render-pages-template.sh
source "${SCRIPT_DIR}/render-pages-template.sh"

GLOBAL_LANDING="${PAGES_ALLURE}"

rm -rf "${GLOBAL_LANDING}/_other-branches"

declare -a branches=()
while IFS= read -r dir; do
  branches+=("$(basename "${dir}")")
done < <(
  find "${GLOBAL_LANDING}" -mindepth 1 -maxdepth 1 -type d \
    ! -name '_other-branches' \
    -exec test -f '{}/history.jsonl' ';' \
    \( -exec test -f '{}/dashboard/index.html' ';' -o -exec test -d '{}/dashboard' ';' -o -exec test -f '{}/latest-run-id.txt' ';' \) \
    -print 2>/dev/null | sort
)

for branch_dir in "${GLOBAL_LANDING}"/*/; do
  [ -d "${branch_dir}" ] || continue
  slug="$(basename "${branch_dir}")"
  case "${slug}" in _other-branches) continue ;; esac
  if [ -f "${branch_dir}/history.jsonl" ]; then
    bash "${SCRIPT_DIR}/generate-branch-landing.sh" "${branch_dir}"
  fi
done

sorted_branches=()
if printf '%s\n' "${branches[@]:-}" | grep -qx 'main'; then
  sorted_branches+=('main')
fi
for branch in "${branches[@]:-}"; do
  [ "${branch}" = 'main' ] && continue
  sorted_branches+=("${branch}")
done

branch_panels=""
branch_links=""
for branch in "${sorted_branches[@]:-}"; do
  latest_run=""
  if [ -f "${GLOBAL_LANDING}/${branch}/latest-run-id.txt" ]; then
    latest_run="$(tr -d '[:space:]' < "${GLOBAL_LANDING}/${branch}/latest-run-id.txt")"
  fi
  if [ -z "${latest_run}" ]; then
    latest_run="$(list_run_dirs "${GLOBAL_LANDING}/${branch}" | tail -1 | xargs -r basename)"
  fi

  report_link=""
  if [ -n "${latest_run}" ]; then
    if [ -f "${GLOBAL_LANDING}/${branch}/${latest_run}/awesome/index.html" ]; then
      report_link="<a class=\"report-link\" href=\"${branch}/${latest_run}/awesome/index.html\">полный отчёт →</a>"
    elif [ -f "${GLOBAL_LANDING}/${branch}/${latest_run}/index.html" ]; then
      report_link="<a class=\"report-link\" href=\"${branch}/${latest_run}/index.html\">полный отчёт →</a>"
    fi
  fi

  dashboard_src="${branch}/"
  if [ -f "${GLOBAL_LANDING}/${branch}/dashboard/index.html" ]; then
    dashboard_src="${branch}/dashboard/index.html"
  fi

  branch_panels="${branch_panels}$(render_fragment branch-panel.html \
    "BRANCH=${branch}" \
    "REPORT_LINK=${report_link}" \
    "DASHBOARD_SRC=${dashboard_src}")"
  branch_links="${branch_links}<li><a href=\"${branch}/\">${branch}</a></li>"
  if [ -n "${latest_run}" ]; then
    if [ -f "${GLOBAL_LANDING}/${branch}/${latest_run}/awesome/index.html" ]; then
      branch_links="${branch_links}<li><a href=\"${branch}/${latest_run}/awesome/index.html\">${branch} — последний run</a></li>"
    elif [ -f "${GLOBAL_LANDING}/${branch}/${latest_run}/index.html" ]; then
      branch_links="${branch_links}<li><a href=\"${branch}/${latest_run}/index.html\">${branch} — последний run</a></li>"
    fi
  fi
done

if [ -z "${branch_panels}" ]; then
  branch_panels='      <section class="panel-card"><div class="card-body"><p class="empty-state">Пока нет опубликованных отчётов по веткам</p></div></section>'
  branch_links='<li>Нет опубликованных веток</li>'
fi

find "${GLOBAL_LANDING}" -maxdepth 1 -type f ! -name 'index.html' -delete

shell_header="$(render_partial header.html "BRAND_HREF=./")"
shell_footer="$(render_partial footer.html)"

render_template overview.html "${GLOBAL_LANDING}/index.html" \
  "HEADER=${shell_header}" \
  "FOOTER=${shell_footer}" \
  "BRANCH_LINKS=${branch_links}" \
  "BRANCH_PANELS=${branch_panels}"

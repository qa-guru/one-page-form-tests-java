#!/usr/bin/env bash
set -euo pipefail

PAGES_ALLURE="${1:?pages/allure-reports path required}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=allure-pages-lib.sh
source "${SCRIPT_DIR}/allure-pages-lib.sh"

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

  branch_panels="${branch_panels}
    <section class=\"card\">
      <div class=\"card-header\">
        <h2><a href=\"${branch}/\">${branch}</a></h2>
        ${report_link}
      </div>
      <div class=\"card-body\">
        <iframe class=\"dashboard-frame\" src=\"${dashboard_src}\" title=\"Dashboard: ${branch}\"></iframe>
      </div>
    </section>"
  branch_links="${branch_links}<li><a href=\"${branch}/\">${branch}</a></li>"
  if [ -n "${latest_run}" ]; then
    if [ -f "${GLOBAL_LANDING}/${branch}/${latest_run}/awesome/index.html" ]; then
      branch_links="${branch_links} <li><a href=\"${branch}/${latest_run}/awesome/index.html\">${branch} — последний run</a></li>"
    elif [ -f "${GLOBAL_LANDING}/${branch}/${latest_run}/index.html" ]; then
      branch_links="${branch_links} <li><a href=\"${branch}/${latest_run}/index.html\">${branch} — последний run</a></li>"
    fi
  fi
done

if [ -z "${branch_panels}" ]; then
  branch_panels='<section class="card"><div class="card-body"><p class="empty-state">Пока нет опубликованных отчётов по веткам</p></div></section>'
  branch_links='<li>Нет опубликованных веток</li>'
fi

find "${GLOBAL_LANDING}" -maxdepth 1 -type f ! -name 'index.html' -delete

cat > "${GLOBAL_LANDING}/index.html" <<EOF
<!DOCTYPE html>
<html lang="ru">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>UI Tests Dashboard</title>
  <link rel="stylesheet" href="../allure-shell.css">
</head>
<body>
  <div class="shell">
    <div class="layout">
      <header class="page-header">
        <h1>UI Tests Dashboard</h1>
        <p>Тренды по веткам. Для дерева тестов откройте полный отчёт.</p>
      </header>

      <p class="note">Dashboard показывает только аналитику. Чтобы кликнуть по тестам, откройте ссылку «полный отчёт» у нужной ветки.</p>

      <section class="card branch-nav">
        <div class="card-header">
          <h2>Навигация</h2>
        </div>
        <div class="card-body">
          <ul>
            ${branch_links}
          </ul>
        </div>
      </section>

      ${branch_panels}
    </div>
  </div>
  <script src="../allure-shell.js"></script>
</body>
</html>
EOF

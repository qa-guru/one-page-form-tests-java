#!/usr/bin/env bash
set -euo pipefail

BRANCH_DIR="${1:?branch directory required}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=allure-pages-lib.sh
source "${SCRIPT_DIR}/allure-pages-lib.sh"

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

cat > "${BRANCH_DIR}/index.html" <<EOF
<!DOCTYPE html>
<html lang="ru">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>UI Tests — ${branch_name}</title>
  <link rel="stylesheet" href="../../allure-shell.css">
</head>
<body>
  <div class="shell">
    <div class="layout">
      <header class="page-header">
        <a class="back-link" href="../">← Все ветки</a>
        <h1>${branch_name}</h1>
        <p>Dashboard — только тренды. Для дерева тестов откройте полный отчёт.</p>
      </header>

      <section class="card">
        <div class="card-header">
          <h2>Полный отчёт</h2>
        </div>
        <div class="card-body">
          <p class="note">Из dashboard нельзя провалиться в тесты — используйте полный Allure Report.</p>
          <div class="actions">
            ${report_cta}
            <a class="btn secondary" href="../">Overview всех веток</a>
          </div>
        </div>
      </section>

      <section class="card">
        <div class="card-header">
          <h2>Прогоны</h2>
        </div>
        <div class="card-body">
          <ul class="runs">
            ${run_links}
          </ul>
        </div>
      </section>

      <section class="card">
        <div class="card-header">
          <h2>Trends</h2>
        </div>
        <div class="card-body">
          ${dashboard_frame:-<p class="empty-state">Dashboard не сгенерирован</p>}
        </div>
      </section>
    </div>
  </div>
  <script src="../../allure-shell.js"></script>
</body>
</html>
EOF

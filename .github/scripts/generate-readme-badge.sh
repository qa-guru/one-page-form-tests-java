#!/usr/bin/env bash
set -euo pipefail

RESULTS_DIR="${1:?allure-results directory required}"
OUTPUT_DIR="${2:?output directory required}"
BRANCH="${3:-main}"
BUILD="${4:-}"

mkdir -p "${OUTPUT_DIR}"

passed=0
failed=0
broken=0
skipped=0

while IFS= read -r status; do
  case "${status}" in
    passed) passed=$((passed + 1)) ;;
    failed) failed=$((failed + 1)) ;;
    broken) broken=$((broken + 1)) ;;
    skipped) skipped=$((skipped + 1)) ;;
  esac
done < <(
  find "${RESULTS_DIR}" -name '*-result.json' -print0 2>/dev/null \
    | xargs -0 jq -r '.status // empty' 2>/dev/null || true
)

total=$((passed + failed + broken + skipped))

if [ "${total}" -eq 0 ]; then
  passed=0
  failed=0
  broken=0
  skipped=0
  status_label="no data"
  status_bg="#64748b"
  badge_right="awaiting run"
else
  if [ "${failed}" -gt 0 ] || [ "${broken}" -gt 0 ]; then
    status_label="failing"
    status_bg="#dc2626"
    badge_right="${failed} failed"
    if [ "${broken}" -gt 0 ]; then
      badge_right="${badge_right}, ${broken} broken"
    fi
  else
    status_label="passing"
    status_bg="#008a56"
    badge_right="${passed} passed"
  fi
fi

build_line=""
if [ -n "${BUILD}" ]; then
  build_line=" · build ${BUILD}"
fi

cat > "${OUTPUT_DIR}/badge.svg" <<EOF
<svg xmlns="http://www.w3.org/2000/svg" width="220" height="20" role="img" aria-label="UI Tests: ${badge_right}">
  <title>UI Tests: ${badge_right}</title>
  <linearGradient id="b" x2="0" y2="100%">
    <stop offset="0" stop-color="#f8fafc" stop-opacity=".7"/>
    <stop offset=".1" stop-color="#eef2f7" stop-opacity=".3"/>
    <stop offset=".9" stop-color="#eef2f7" stop-opacity=".3"/>
    <stop offset="1" stop-color="#f8fafc" stop-opacity=".7"/>
  </linearGradient>
  <mask id="m"><rect width="220" height="20" rx="3" fill="#fff"/></mask>
  <g mask="url(#m)">
    <rect width="92" height="20" fill="#7e22ce"/>
    <rect x="92" width="128" height="20" fill="${status_bg}"/>
    <rect width="220" height="20" fill="url(#b)"/>
  </g>
  <g fill="#fff" text-anchor="middle" font-family="ui-sans-serif,system-ui,sans-serif" font-size="11">
    <text x="46" y="14" font-weight="600">UI Tests</text>
    <text x="156" y="14">${badge_right}</text>
  </g>
</svg>
EOF

bar_width=280
segment_total="${total}"
[ "${segment_total}" -gt 0 ] || segment_total=1

passed_w=$((passed * bar_width / segment_total))
failed_w=$((failed * bar_width / segment_total))
broken_w=$((broken * bar_width / segment_total))
skipped_w=$((skipped * bar_width / segment_total))

x=40
passed_x="${x}"
failed_x=$((x + passed_w))
broken_x=$((failed_x + failed_w))
skipped_x=$((broken_x + broken_w))

cat > "${OUTPUT_DIR}/stats.svg" <<EOF
<svg xmlns="http://www.w3.org/2000/svg" width="360" height="92" viewBox="0 0 360 92" role="img" aria-label="UI Tests stats: ${passed} passed, ${failed} failed">
  <title>UI Tests on ${BRANCH}${build_line}</title>
  <rect width="360" height="92" rx="12" fill="#ffffff" stroke="rgba(11,48,86,0.12)"/>
  <text x="20" y="28" fill="rgba(1,10,24,0.83)" font-family="ui-sans-serif,system-ui,sans-serif" font-size="14" font-weight="700">UI Tests</text>
  <text x="20" y="46" fill="rgba(2,19,44,0.6)" font-family="ui-sans-serif,system-ui,sans-serif" font-size="11">${BRANCH}${build_line} · ${status_label}</text>
  <rect x="40" y="58" width="${bar_width}" height="10" rx="5" fill="#e2e8f0"/>
EOF

if [ "${passed_w}" -gt 0 ]; then
  echo "  <rect x=\"${passed_x}\" y=\"58\" width=\"${passed_w}\" height=\"10\" rx=\"5\" fill=\"#008a56\"/>" >> "${OUTPUT_DIR}/stats.svg"
fi
if [ "${failed_w}" -gt 0 ]; then
  echo "  <rect x=\"${failed_x}\" y=\"58\" width=\"${failed_w}\" height=\"10\" fill=\"#dc2626\"/>" >> "${OUTPUT_DIR}/stats.svg"
fi
if [ "${broken_w}" -gt 0 ]; then
  echo "  <rect x=\"${broken_x}\" y=\"58\" width=\"${broken_w}\" height=\"10\" fill=\"#ea580c\"/>" >> "${OUTPUT_DIR}/stats.svg"
fi
if [ "${skipped_w}" -gt 0 ]; then
  echo "  <rect x=\"${skipped_x}\" y=\"58\" width=\"${skipped_w}\" height=\"10\" fill=\"#94a3b8\"/>" >> "${OUTPUT_DIR}/stats.svg"
fi

cat >> "${OUTPUT_DIR}/stats.svg" <<EOF
  <g font-family="ui-sans-serif,system-ui,sans-serif" font-size="11" fill="rgba(1,18,40,0.68)">
    <circle cx="52" cy="82" r="4" fill="#008a56"/>
    <text x="62" y="86">${passed} passed</text>
    <circle cx="132" cy="82" r="4" fill="#dc2626"/>
    <text x="142" y="86">${failed} failed</text>
    <circle cx="210" cy="82" r="4" fill="#ea580c"/>
    <text x="220" y="86">${broken} broken</text>
    <circle cx="296" cy="82" r="4" fill="#94a3b8"/>
    <text x="306" y="86">${skipped} skipped</text>
  </g>
</svg>
EOF

echo "Generated ${OUTPUT_DIR}/badge.svg and ${OUTPUT_DIR}/stats.svg (${passed}/${total} passed)"

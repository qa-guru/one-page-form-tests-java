#!/usr/bin/env bash
set -euo pipefail

PLUGIN_DIR="${1:?plugin directory required}"
STATIC_DIR="${2:?static assets directory required}"

[ -d "${PLUGIN_DIR}" ] || exit 0
mkdir -p "${STATIC_DIR}"

shopt -s nullglob
for file in \
  "${PLUGIN_DIR}"/*.js \
  "${PLUGIN_DIR}"/*.css \
  "${PLUGIN_DIR}"/*.woff \
  "${PLUGIN_DIR}"/*.woff2 \
  "${PLUGIN_DIR}"/*.LICENSE.txt
do
  [ -f "${file}" ] || continue
  name="$(basename "${file}")"
  if [ ! -f "${STATIC_DIR}/${name}" ]; then
    mv "${file}" "${STATIC_DIR}/${name}"
  else
    rm -f "${file}"
  fi
done

if rel="$(realpath --relative-to="${PLUGIN_DIR}" "${STATIC_DIR}" 2>/dev/null)"; then
  :
else
  rel="$(python3 -c "import os,sys; print(os.path.relpath(sys.argv[1], sys.argv[2]))" "${STATIC_DIR}" "${PLUGIN_DIR}")"
fi

for html in "${PLUGIN_DIR}"/*.html; do
  [ -f "${html}" ] || continue
  for asset in "${STATIC_DIR}"/*; do
    [ -f "${asset}" ] || continue
    base="$(basename "${asset}")"
    HTML_FILE="${html}" ASSET_BASE="${base}" ASSET_REL="${rel}" python3 <<'PY'
import os
import pathlib
import re

html = pathlib.Path(os.environ["HTML_FILE"])
base = os.environ["ASSET_BASE"]
rel = os.environ["ASSET_REL"]
text = html.read_text(encoding="utf-8")
pattern = re.compile(rf'(src|href)=(["\']){re.escape(base)}\2')
updated = pattern.sub(rf"\1=\2{rel}/{base}\2", text)
if updated != text:
    html.write_text(updated, encoding="utf-8")
PY
  done
done

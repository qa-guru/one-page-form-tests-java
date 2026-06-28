#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TEMPLATE_DIR="$(cd "${SCRIPT_DIR}/../../pages-templates" && pwd)"

apply_template() {
  local template_path="$1"
  local output_path="$2"
  shift 2

  python - "${template_path}" "${output_path}" "$@" <<'PY'
import sys
from pathlib import Path

template_path = Path(sys.argv[1])
output_path = Path(sys.argv[2])
content = template_path.read_text(encoding="utf-8")

for arg in sys.argv[3:]:
    key, sep, value = arg.partition("=")
    if not sep:
        raise SystemExit(f"Invalid placeholder argument: {arg!r}")
    content = content.replace(f"{{{{{key}}}}}", value)

output_path.write_text(content, encoding="utf-8")
PY
}

render_partial() {
  local partial="$1"
  shift
  apply_template "${TEMPLATE_DIR}/partials/${partial}" /dev/stdout "$@"
}

render_fragment() {
  local fragment="$1"
  shift
  apply_template "${TEMPLATE_DIR}/fragments/${fragment}" /dev/stdout "$@"
}

render_template() {
  local template="$1"
  local output="$2"
  shift 2
  apply_template "${TEMPLATE_DIR}/${template}" "${output}" "$@"
}

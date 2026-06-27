#!/usr/bin/env bash

list_run_dirs() {
  local branch_dir="${1:?branch directory required}"
  local sort_args=(-V)
  if [ "${2:-}" = "reverse" ]; then
    sort_args=(-Vr)
  fi

  find "${branch_dir}" -mindepth 1 -maxdepth 1 -type d 2>/dev/null \
    | while IFS= read -r run_dir; do
        [[ "$(basename "${run_dir}")" =~ ^[0-9]+$ ]] || continue
        printf '%s\n' "${run_dir}"
      done \
    | sort "${sort_args[@]}"
}

is_reserved_allure_dir() {
  case "${1}" in
    _static | _other-branches) return 0 ;;
    *) return 1 ;;
  esac
}

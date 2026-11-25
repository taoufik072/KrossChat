#!/usr/bin/env bash
# Prints one field of the hook JSON read from stdin.
# Usage: read-input.sh <field>   (field: file_path | command)
field="$1"
input="$(cat)"
if command -v jq >/dev/null 2>&1; then
  printf '%s' "$input" | jq -r ".tool_input.${field} // empty"
elif command -v python3 >/dev/null 2>&1; then
  printf '%s' "$input" | python3 -c "import json,sys; print(json.load(sys.stdin).get('tool_input',{}).get('$field') or '')"
fi

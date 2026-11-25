#!/usr/bin/env bash
# PreToolUse hook (Edit|Write|MultiEdit): blocks changes to secret and build-identity files.
# Exit code 2 blocks the tool call and sends stderr back to Claude.
file_path="$("$(dirname "$0")/read-input.sh" file_path)"
[ -z "$file_path" ] && exit 0

case "$file_path" in
  *local.properties|*google-services.json|*GoogleService-Info.plist|*.jks|*.keystore|*.p12|*.mobileprovision|*.xcodeproj/*)
    echo "Blocked by .claude/hooks/protect-secrets.sh: '$file_path' is a protected file (secrets, signing or Xcode project). Do not modify it; ask the user to change it manually if needed." >&2
    exit 2
    ;;
esac
exit 0

#!/usr/bin/env bash
# PreToolUse hook (Bash): refuses project-wide Gradle tasks so Claude builds only the modules it touched.
# Exit code 2 blocks the command and sends stderr back to Claude.
cmd="$("$(dirname "$0")/read-input.sh" command)"
case "$cmd" in
  *gradlew*) ;;
  *) exit 0 ;;
esac

# Tasks that are too expensive when run on the whole project (no ":module:" prefix).
heavy=" build assemble assembleDebug assembleRelease check test allTests testAndroidHostTest lint clean connectedAndroidTest connectedCheck "

# Allowed exception: full unit tests + coverage before a PR.
if [[ "$cmd" == *koverXmlReport* || "$cmd" == *koverHtmlReport* ]]; then
  exit 0
fi

# Always refused, even with a module prefix: clean builds and device tests.
for t in clean connectedAndroidTest connectedCheck; do
  if [[ "$cmd" =~ (^|[[:space:]:])$t([[:space:]]|$) ]]; then
    echo "Blocked by .claude/hooks/guard-gradle.sh: '$t' is not allowed (clean build or device tests). Ask the user if it is really needed." >&2
    exit 2
  fi
done

# Refuse unprefixed heavy tasks after gradlew.
after="${cmd#*gradlew}"
for token in $after; do
  case "$token" in
    -*|*:*) continue ;;
  esac
  if [[ "$heavy" == *" $token "* ]]; then
    echo "Blocked by .claude/hooks/guard-gradle.sh: './gradlew $token' runs on the whole project. Run it only on the modules you touched, in a single call, e.g. './gradlew :feature:chat:data:compileAndroidMain :feature:chat:data:testAndroidHostTest --offline -q'. Full tests + coverage are allowed only as './gradlew testAndroidHostTest koverXmlReport'." >&2
    exit 2
  fi
done
exit 0

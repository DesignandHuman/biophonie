#!/usr/bin/env bash

module="$1"
OUTPUT="/tmp/detekt_$RANDOM"
path="${module//://}/"
changed_files="$(git diff --staged --name-only "$path" |grep -e "\.kts" -e "\.kt")"
if [[ -z $changed_files ]]; then
  exit 0
fi
echo "detekt: Inspecting module $module"
./gradlew --quiet "$module":detekt >> "$OUTPUT"
EXIT_CODE=$?
if [ $EXIT_CODE -ne 0 ]; then
  cat "$OUTPUT"
  rm "$OUTPUT"
  echo "*******************************************************"
  echo " detekt failed in module $module"
  echo " Please fix the above issues before committing"
  echo "*******************************************************"
  exit $EXIT_CODE
fi
rm "$OUTPUT"

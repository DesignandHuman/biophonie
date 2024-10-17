#!/usr/bin/env bash

module="$1"
OUTPUT="/tmp/sortDependencies_$RANDOM"
path="${module//://}/"
changed_files="$(git diff --staged --name-only "$path" |grep -e "\.kts")"
if [[ -z $changed_files ]]; then
  exit 0
fi
./gradlew --quiet "$module":checkSortDependencies >> "$OUTPUT"
EXIT_CODE=$?
if [ $EXIT_CODE -ne 0 ]; then
  cat "$OUTPUT"
  rm "$OUTPUT"
  echo "*******************************************************"
  echo " sort-dependencies failed in module $module"
  echo " Please fix the above issues before committing"
  echo " Tip: run the formatter with './gradlew $module:sortDependencies'"
  echo "*******************************************************"
  exit $EXIT_CODE
fi
rm "$OUTPUT"

#!/usr/bin/env bash

module="$1"
OUTPUT="/tmp/ktfmt_$RANDOM"
path="${module//://}/"
changed_files="$(git diff --staged --name-only "$path" |grep -e "\.kts" -e "\.kt")"
if [[ -z $changed_files ]]; then
  exit 0
fi
echo "ktfmt: Inspecting module $module"
relative_files="$(echo "$changed_files" | sed "s#^$path##g" | tr '\n' ':')"
./gradlew --quiet "$module":ktfmtPreCommit --include-only="$relative_files" >> "$OUTPUT"
EXIT_CODE=$?
if [ $EXIT_CODE -ne 0 ]; then
  cat "$OUTPUT"
  rm "$OUTPUT"
  echo "*******************************************************"
  echo " ktfmt failed in module $module"
  echo " Please fix the above issues before committing"
  echo " Tip: run the formatter with './gradlew ktfmtFormat'"
  echo "*******************************************************"
  exit $EXIT_CODE
fi
rm "$OUTPUT"

#!/usr/bin/env bash

module="$1"
OUTPUT="/tmp/buildHealth_$RANDOM"
echo "buildHealth: Inspecting module $module"
./gradlew --quiet "$module":projectHealth >> "$OUTPUT"
EXIT_CODE=$?
if [ $EXIT_CODE -ne 0 ]; then
  cat "$OUTPUT"
  rm "$OUTPUT"
  echo "*******************************************************"
  echo " buildHealth failed for module $module"
  echo " Please fix the issues above before committing"
  echo " Tip: run the formatter with: ./gradlew fixDependencies"
  echo "*******************************************************"
  exit $EXIT_CODE
fi
rm "$OUTPUT"

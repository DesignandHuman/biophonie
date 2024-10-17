#!/usr/bin/env bash

echo "Running ktfmt check on staged files..."
MODULES="$(./gradlew -q modules)"
pids=()
isSuccess=true
for module in $MODULES; do
  "$(dirname "$0")/dep/ktfmtModule.sh" "$module" &
  pids+=($!)
done

for pid in ${pids[*]}; do
  wait "$pid"
  if [ "$?" -ne 0 ]; then
    isSuccess=false
  fi
done

if [ $isSuccess = true ]; then
  echo "ktfmt ended successfully"
  exit 0
else
  exit 1
fi

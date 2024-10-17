#!/usr/bin/env bash

echo "Running sort-dependencies check on staged files..."
MODULES="$(./gradlew -q modules)"
pids=()
isSuccess=true
for module in $MODULES; do
  "$(dirname "$0")/dep/sortDependenciesModule.sh" "$module" &
  pids+=($!)
done

for pid in ${pids[*]}; do
  wait "$pid"
  if [ "$?" -ne 0 ]; then
    isSuccess=false
  fi
done

if [ $isSuccess = true ]; then
  echo "sort-dependencies ended successfully"
  exit 0
else
  exit 1
fi

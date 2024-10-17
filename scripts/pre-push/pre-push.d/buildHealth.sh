#!/usr/bin/env bash

echo "Running buildHealth check..."

MODULES="$(./gradlew -q modules)"
pids=()
isSuccess=true
for module in $MODULES; do
  path="${module//://}/"
  changed_files="$(git diff @'{push}' @ --name-only "$path" |grep -e "\.kts")"
  if [[ -z $changed_files ]]; then
    continue
  fi
  "$(dirname "$0")/dep/buildHealthModule.sh" "$module" &
  pids+=($!)
done

for pid in ${pids[*]}; do
  wait "$pid"
  if [ "$?" -ne 0 ]; then
    isSuccess=false
  fi
done

if [ $isSuccess = true ]; then
  echo "dependency-analysis ended successfully"
  exit 0
else
  exit 1
fi

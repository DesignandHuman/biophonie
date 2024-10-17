#!/usr/bin/env bash

echo "Running moduleGraph check..."
changed_files=$(git diff @'{push}' @ --name-only |grep "/" |grep "\.kts")
for file in $changed_files; do
  build_modification_date=$(git log -1 --format="%ct" -- "$file")
  graph_name=$(sed -e 's#/#_#g' -e 's/_build.gradle.kts//g' <<< "$file")
  file_graph=docs/images/graphs/dep_graph_"$graph_name".svg
  graph_modification_date=$(git log -1 --format="%ct" -- "$file_graph")
  if [[ "$build_modification_date" -gt "$graph_modification_date" ]]; then
      echo "*******************************************************"
      echo " moduleGraph check failed for build: $file"
      echo " Please generate the graphs and commit using the command:"
      echo " ./scripts/generateModuleGraphs.sh"
      echo "*******************************************************"
      exit 1
  fi
done
echo "moduleGraph check ended successfully"

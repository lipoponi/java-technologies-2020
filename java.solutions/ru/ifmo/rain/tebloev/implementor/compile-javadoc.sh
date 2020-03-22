#!/bin/bash

workingdir="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"
cd "$workingdir" || exit 1

cd ../../../../../../

modulepath="../java-advanced-2020/lib/"
output="$workingdir/_javadoc"
sourcepath="../java-advanced-2020/modules/:."

javadoc --module java.solutions -d "$output" -p "$modulepath" --module-source-path "$sourcepath" \
  -private -link https://docs.oracle.com/en/java/javase/11/docs/api

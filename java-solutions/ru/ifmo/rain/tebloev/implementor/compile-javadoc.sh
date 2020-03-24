#!/bin/bash

workingdir="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"
cd "$workingdir" || exit 1

rm -rf "$workingdir/_javadoc"

cd ../../../../../../
modulepath="../java-advanced-2020/lib/:../java-advanced-2020/artifacts/"
output="$workingdir/_javadoc"

# shellcheck disable=SC2046
javadoc $(find . -name "*.java") -d "$output" -p "$modulepath" \
  -private -link https://docs.oracle.com/en/java/javase/11/docs/api

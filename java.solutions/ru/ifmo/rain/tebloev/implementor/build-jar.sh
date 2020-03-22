#!/bin/bash

workingdir="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"
cd "$workingdir" || exit 1

rm -rf "$workingdir/_build"

cd ../../../../../../
modulepath="../java-advanced-2020/lib/:../java-advanced-2020/artifacts/"
output="$workingdir/_build"
javac --module java.solutions --module-source-path "." -p "$modulepath" -d "$output"

cd "$workingdir" || exit 1
jar --create --file=_implementor.jar \
  --module-version=1.0 \
  --main-class=ru.ifmo.rain.tebloev.implementor.JarImplementor \
  -C _build/java.solutions .

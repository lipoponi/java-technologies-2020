#!/bin/bash

workingdir="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"
./build.sh
cd "$workingdir" || exit 1
cd ../../../../../../

classpath="../java-advanced-2020/lib/junit-4.11.jar:../java-advanced-2020/lib/hamcrest-core-1.3.jar:$workingdir/_build"
java -cp "$classpath" org.junit.runner.JUnitCore ServerTest ClientTest

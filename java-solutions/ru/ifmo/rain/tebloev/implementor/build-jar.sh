#!/bin/bash

tmppackageroot="_build/ru.ifmo.rain.tebloev.implementor/ru/ifmo/rain/tebloev/implementor"
mkdir -p $tmppackageroot

cp ./*.java $tmppackageroot
mv $tmppackageroot/module-info.java _build/ru.ifmo.rain.tebloev.implementor

cd ../../../../../../../

packageroot="./java-advanced-2020-solutions/java-solutions/ru/ifmo/rain/tebloev/implementor"
output="$packageroot/_build/out"
modulepath="./java-advanced-2020/lib/:./java-advanced-2020/artifacts/"
modulesourcepath="$packageroot/_build"
javac --module ru.ifmo.rain.tebloev.implementor --module-source-path $modulesourcepath -p $modulepath -d $output

cd $packageroot || exit 1
jar --create --file=_implementor.jar --module-version=1.0 -C _build/out/ru.ifmo.rain.tebloev.implementor .
#!/bin/bash

cd ../../../../../../../

output="./java-advanced-2020-solutions/java-solutions/ru/ifmo/rain/tebloev/implementor/_build"
classpath="./java-advanced-2020/modules/info.kgeorgiy.java.advanced.implementor"
sources="./java-advanced-2020-solutions/java-solutions/ru/ifmo/rain/tebloev/implementor/*.java"

javac -d $output -cp "$classpath" $sources

cd ./java-advanced-2020-solutions/java-solutions/ru/ifmo/rain/tebloev/implementor/_build || exit
jar --create --file ../_implementor.jar ./ru
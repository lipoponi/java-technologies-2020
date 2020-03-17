#!/bin/bash

cd ../../../../../../../

output="./java-advanced-2020-solutions/java-solutions/ru/ifmo/rain/tebloev/implementor/_javadoc"
modulepath="./java-advanced-2020/lib/:./java-advanced-2020/artifacts/"
sourcepath="./java-advanced-2020/modules/:./java-advanced-2020-solutions/java-solutions/ru/ifmo/rain/tebloev/implementor/_build"

javadoc --module ru.ifmo.rain.tebloev.implementor -d $output -p "$modulepath" --module-source-path "$sourcepath" -private -link https://docs.oracle.com/en/java/javase/11/docs/api

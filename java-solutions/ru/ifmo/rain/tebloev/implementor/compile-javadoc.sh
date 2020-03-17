#!/bin/bash

cd ../../../../../../../

output="./java-advanced-2020-solutions/java-solutions/ru/ifmo/rain/tebloev/implementor/_javadoc"
classpath="./java-advanced-2020/lib/*:./java-advanced-2020/modules/info.kgeorgiy.java.advanced.implementor/:./java-advanced-2020/modules/info.kgeorgiy.java.advanced.base/"
sources="./java-advanced-2020-solutions/java-solutions/ru/ifmo/rain/tebloev/implementor/*.java"

javadoc -d $output -cp "$classpath" -private -link https://docs.oracle.com/en/java/javase/11/docs/api/ $sources
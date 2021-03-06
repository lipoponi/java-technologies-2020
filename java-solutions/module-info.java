/**
 * Module that contains homework solutions for
 * <a href="http://www.kgeorgiy.info/courses/java-advanced/index.html">java-advanced-2020</a> course.
 */
module java.solutions {
    requires java.compiler;
    requires java.rmi;

    requires info.kgeorgiy.java.advanced.walk;
    requires info.kgeorgiy.java.advanced.arrayset;
    requires info.kgeorgiy.java.advanced.student;
    requires info.kgeorgiy.java.advanced.implementor;
    requires info.kgeorgiy.java.advanced.concurrent;
    requires info.kgeorgiy.java.advanced.mapper;
    requires info.kgeorgiy.java.advanced.crawler;
    requires info.kgeorgiy.java.advanced.hello;

    exports ru.ifmo.rain.tebloev.walk;
    exports ru.ifmo.rain.tebloev.arrayset;
    exports ru.ifmo.rain.tebloev.student;
    exports ru.ifmo.rain.tebloev.implementor;
    exports ru.ifmo.rain.tebloev.concurrent;
    exports ru.ifmo.rain.tebloev.crawler;
    exports ru.ifmo.rain.tebloev.hello;
    exports ru.ifmo.rain.tebloev.bank.common to java.rmi;
    exports ru.ifmo.rain.tebloev.bank.server;
    exports ru.ifmo.rain.tebloev.bank.client;
    exports ru.ifmo.rain.tebloev.bank.test;
}
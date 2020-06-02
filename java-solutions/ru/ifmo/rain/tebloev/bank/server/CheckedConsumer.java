package ru.ifmo.rain.tebloev.bank.server;

@FunctionalInterface
interface CheckedConsumer<T, E extends Exception> {
    void accept(T t) throws E;
}

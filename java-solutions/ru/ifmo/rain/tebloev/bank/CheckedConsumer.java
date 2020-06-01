package ru.ifmo.rain.tebloev.bank;

@FunctionalInterface
interface CheckedConsumer<T, E extends Exception> {
    void accept(T t) throws E;
}

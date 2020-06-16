package ru.ifmo.rain.tebloev.hello;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.Consumer;

@FunctionalInterface
public interface IOConsumer<T> {
    static <T> Consumer<T> unchecked(IOConsumer<T> consumer) {
        return (T value) -> {
            try {
                consumer.accept(value);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        };
    }

    void accept(T value) throws IOException;
}

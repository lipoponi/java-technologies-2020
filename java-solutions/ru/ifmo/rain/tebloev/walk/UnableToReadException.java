package ru.ifmo.rain.tebloev.walk;

import java.io.IOException;

class UnableToReadException extends IOException {
    public UnableToReadException(final Throwable cause) {
        super(cause.getMessage(), cause);
    }
}

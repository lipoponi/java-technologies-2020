package ru.ifmo.rain.tebloev.walk;

import java.io.IOException;

class CannotOpenReaderException extends IOException {
    public CannotOpenReaderException(final Throwable cause) {
        super(cause.getMessage(), cause);
    }
}

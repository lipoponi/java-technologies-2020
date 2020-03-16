package ru.ifmo.rain.tebloev.walk;

import java.io.IOException;

class CannotOpenWriterException extends IOException {
    public CannotOpenWriterException(final Throwable cause) {
        super(cause.getMessage(), cause);
    }
}

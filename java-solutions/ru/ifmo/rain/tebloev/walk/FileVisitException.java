package ru.ifmo.rain.tebloev.walk;

import java.io.IOException;

class FileVisitException extends IOException {
    public FileVisitException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}

package ru.ifmo.rain.tebloev.walk;

import java.io.IOException;

class ParentDirectoriesException extends IOException {
    public ParentDirectoriesException(final Throwable cause) {
        super(cause.getMessage(), cause);
    }
}

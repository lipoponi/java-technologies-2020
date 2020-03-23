package ru.ifmo.rain.tebloev.walk;

class PathLineException extends Exception {
    public PathLineException(final int line, final Throwable cause) {
        super(String.format("Input line [%d] %s", line, cause), cause);
    }
}

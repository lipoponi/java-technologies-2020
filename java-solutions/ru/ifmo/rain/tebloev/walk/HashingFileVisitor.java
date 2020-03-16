package ru.ifmo.rain.tebloev.walk;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

class HashingFileVisitor extends SimpleFileVisitor<Path> {
    private final Writer out;
    private final List<IOException> exceptions = new ArrayList<>();

    public HashingFileVisitor(final Writer out) {
        this.out = out;
    }

    public FileVisitResult visitFile(final String filepath) throws FileVisitException {
        final int hash = FileHasher.getHash(filepath);
        final String result = String.format("%08x %s%n", hash, filepath);

        try {
            out.write(result);
        } catch (final IOException e) {
            throw new FileVisitException(filepath, e);
        }

        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(final Path file, final BasicFileAttributes ignored) throws FileVisitException {
        final String filepath = file.toString();

        return visitFile(filepath);
    }

    @Override
    public FileVisitResult visitFileFailed(final Path ignored, final IOException exc) {
        exceptions.add(exc);

        return FileVisitResult.CONTINUE;
    }

    public List<IOException> getExceptions() {
        return exceptions;
    }
}

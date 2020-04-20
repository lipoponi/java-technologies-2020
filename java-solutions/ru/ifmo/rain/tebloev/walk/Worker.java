package ru.ifmo.rain.tebloev.walk;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;

class Worker {
    final BufferedReader in;
    final Writer out;
    final PrintStream err;
    final HashingFileVisitor visitor;

    public Worker(final Reader in, final Writer out, final PrintStream err) {
        this.in = new BufferedReader(in);
        this.out = out;
        this.err = err;
        visitor = new HashingFileVisitor(out);
    }

    public void work() throws UnableToReadException {
        try {
            String line;
            for (int i = 1; (line = in.readLine()) != null; i++) {
                try {
                    boolean isDirectory = new File(line).isDirectory();

                    if (isDirectory) {
                        Files.walkFileTree(Paths.get(line), visitor);
                    } else {
                        visitor.visitFile(line);
                    }
                } catch (final SecurityException | InvalidPathException | FileVisitException e) {
                    err.println(new PathLineException(i, e));
                }
            }
        } catch (final IOException e) {
            throw new UnableToReadException(e);
        }
    }
}
package ru.ifmo.rain.tebloev.walk;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Walk {
    private static final PrintStream err = System.err;

    private static void runWrapper(final String inputPath, final String outputPath) throws IOException {
        try {
            Path dir = Paths.get(outputPath).getParent();
            if (dir != null) {
                Files.createDirectories(dir);
            }
        } catch (final IOException | InvalidPathException e) {
            throw new ParentDirectoriesException(e);
        }

        try (Reader input = new FileReader(inputPath, StandardCharsets.UTF_8)) {
            try (Writer output = new FileWriter(outputPath, StandardCharsets.UTF_8)) {
                Worker worker = new Worker(input, output, err);
                worker.work();
            } catch (final UnableToReadException e) {
                throw e;
            } catch (final IOException e) {
                throw new CannotOpenWriterException(e);
            }
        } catch (final UnableToReadException | CannotOpenWriterException e) {
            throw e;
        } catch (final IOException e) {
            throw new CannotOpenReaderException(e);
        }
    }

    public static void main(String[] args) {
        try {
            if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
                throw new ParametersFormatException("usage: java ru.ifmo.rain.tebloev.walk.Walk <входной файл> <выходной файл>");
            }

            runWrapper(args[0], args[1]);
        } catch (final Exception e) {
            err.println(e);
        }
    }
}

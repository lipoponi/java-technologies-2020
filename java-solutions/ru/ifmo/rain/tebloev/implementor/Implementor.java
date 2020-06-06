package ru.ifmo.rain.tebloev.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

/**
 * Implements specified base token.
 *
 * @author Stanislav Tebloev
 */
public class Implementor implements Impler {
    /**
     * Generates implementation for specified command line arguments. Required
     * arguments are {@code classname} and {@code output path}. Usage:
     * {@code <classname> <output path>}
     *
     * @param args two command line arguments
     */
    public static void main(String[] args) {
        try {
            if (args == null || args.length != 2) {
                throw new ImplerException("usage: <classname> <output path>");
            }

            try {
                new Implementor().implement(Class.forName(args[0]), Path.of(args[1]));
            } catch (ClassNotFoundException e) {
                throw new ImplerException("class not found", e);
            } catch (InvalidPathException e) {
                throw new ImplerException("invalid path", e);
            }
        } catch (ImplerException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * Returns package {@link Path} for specified token.
     *
     * @return {@link Path} object of package containing specified token
     */
    protected Path getPackagePath(final Class<?> token) {
        return Path.of(token.getPackageName().replace(".", File.separator));
    }

    /**
     * Returns implementation name for specified {@link Class} token.
     *
     * @param token {@link Class} object
     * @return implementation name for token
     */
    protected String getResultName(final Class<?> token) {
        return token.getSimpleName() + "Impl";
    }

    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        try {
            Path packageRoot = root.resolve(getPackagePath(token));
            Path filepath = packageRoot.resolve(getResultName(token) + ".java");
            try {
                Files.createDirectories(packageRoot);
            } catch (IOException e) {
                throw new ImplerException("cannot create directories for result", e);
            }

            CodeGenerator generator = new CodeGenerator();
            try (Writer writer = new UnicodeEscapingWriter(Files.newBufferedWriter(filepath, StandardCharsets.UTF_8))) {
                generator.writeTokenImplementation(writer, token);
            } catch (IOException e) {
                throw new ImplerException("cannot write result", e);
            }
        } catch (ImplerException e) {
            throw e;
        } catch (Exception e) {
            throw new ImplerException("unknown error", e);
        }
    }
}

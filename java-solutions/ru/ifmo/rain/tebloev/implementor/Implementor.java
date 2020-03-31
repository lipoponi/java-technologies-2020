package ru.ifmo.rain.tebloev.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Implements specified base token.
 *
 * @author Stanislav Tebloev
 */
public class Implementor implements Impler {
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
    protected String getImplName(final Class<?> token) {
        return token.getSimpleName() + "Impl";
    }

    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        try {
            Path packageRoot = root.resolve(getPackagePath(token));
            Files.createDirectories(packageRoot);
            File filepath = packageRoot.resolve(getImplName(token) + ".java").toFile();

            CodeGenerator generator = new CodeGenerator();
            try (Writer writer = new UnicodeEscapingWriter(new BufferedWriter(new FileWriter(filepath, StandardCharsets.UTF_8)))) {
                generator.writeTokenImplementation(writer, token);
            }
        } catch (ImplerException e) {
            throw e;
        } catch (IOException e) {
            throw new ImplerException("io operation cannot be done", e);
        } catch (Exception e) {
            throw new ImplerException(e);
        }
    }

    /**
     * Generates implementation for specified command line arguments. Required
     * arguments are {@code classname} and {@code output path}. Usage:
     * {@code <classname> <output path>}
     *
     * @param args two command line arguments
     */
    public static void main(String[] args) {
        if (args == null || args.length != 2) {
            System.err.println("usage: <classname> <output path>");
            return;
        }

        try {
            Impler implementor = new Implementor();
            try {
                implementor.implement(Class.forName(args[0]), Path.of(args[1]));
            } catch (ImplerException e) {
                throw e;
            } catch (Exception e) {
                throw new ImplerException("arguments are invalid", e);
            }
        } catch (ImplerException e) {
            System.err.println(e.getMessage());
        }
    }
}

package ru.ifmo.rain.tebloev.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

/**
 * Implements specified base token and produces jar file with result.
 *
 * @author Stanislav Tebloev
 */
public class JarImplementor extends Implementor implements JarImpler {
    /**
     * Converts {@link Path} object to string with correct filepath related to
     * jar.
     *
     * @param filepath specified {@link Path} object
     * @return filepath with correct jar separators
     */
    private String toJarEntryName(final Path filepath) {
        return filepath.toString().replace(File.separator, "/");
    }

    /**
     * Returns classpath for specified token.
     *
     * @param token {@link Class} object
     * @return classpath for specified token
     */
    private String getTokenClasspath(final Class<?> token) {
        try {
            return Path.of(token.getProtectionDomain().getCodeSource().getLocation().toURI()).toString();
        } catch (final URISyntaxException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {
        try {
            Path root = Path.of(System.getProperty("java.io.tmpdir")).resolve("implementor");
            Path packageRoot = root.resolve(getPackagePath(token));
            File sourceFile = packageRoot.resolve(getImplName(token) + ".java").toFile();
            File classFile = packageRoot.resolve(getImplName(token) + ".class").toFile();

            implement(token, root);

            String classpath = getTokenClasspath(token);
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

            if (compiler.run(null, null, null, "-cp", classpath, sourceFile.toString()) != 0) {
                throw new ImplerException("compilation error");
            }

            Manifest manifest = new Manifest();
            manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");

            Files.createDirectories(jarFile.getParent());
            try (InputStream is = new FileInputStream(classFile);
                 JarOutputStream jos = new JarOutputStream(new FileOutputStream(jarFile.toFile()), manifest)) {
                String entryName = toJarEntryName(getPackagePath(token).resolve(getImplName(token) + ".class"));

                jos.putNextEntry(new JarEntry(entryName));
                is.transferTo(jos);
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
     * arguments are {@code classname} and {@code output file}. Usage:
     * {@code -jar <classname> <output file>}
     *
     * @param args three command line arguments
     */
    public static void main(String[] args) {
        if (args == null || args.length != 3 || !"-jar".equals(args[0])) {
            System.err.println("usage: -jar <classname> <output file>");
            return;
        }

        try {
            JarImpler implementor = new JarImplementor();
            try {
                implementor.implementJar(Class.forName(args[1]), Path.of(args[2]));
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

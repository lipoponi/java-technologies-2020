package ru.ifmo.rain.tebloev.walk;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;

class FileHasher {
    private static final int FNV_PRIME = 0x01000193;
    private static final int HASH_INITIAL = 0x811c9dc5;

    public static int getHash(final String pathname) {
        try (
                final BufferedInputStream stream = new BufferedInputStream(new FileInputStream(pathname))
        ) {
            int hash = HASH_INITIAL;
            int tmp;

            while ((tmp = stream.read()) != -1) {
                hash = (hash * FNV_PRIME) ^ tmp;
            }

            return hash;
        } catch (final IOException ignored) {
            return 0;
        }
    }
}
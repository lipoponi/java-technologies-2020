package ru.ifmo.rain.tebloev.implementor;

import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * Writer that converts Unicode to ASCII with escape sequences.
 *
 * @author Stanislav Tebloev
 */
public class UnicodeEscapingWriter extends FilterWriter {
    /**
     * Create a new {@link UnicodeEscapingWriter}.
     *
     * @param out a Writer object to provide the underlying stream.
     * @throws NullPointerException if {@code out} is {@code null}
     */
    public UnicodeEscapingWriter(final Writer out) {
        super(out);
    }

    /**
     * Checks if character codepoint belongs to ASCII charset.
     *
     * @param codepoint character Unicode codepoint
     * @return {@code true} if it belongs to ASCII, otherwise {@code false}
     */
    protected boolean isAsciiCharacter(int codepoint) {
        return 0 <= codepoint && codepoint < 128;
    }

    /**
     * Converts Unicode codepoint to it's escape sequence.
     *
     * @param codepoint character Unicode codepoint
     * @return {@link String} that contains escape sequence
     */
    protected String getEscaped(int codepoint) {
        return String.format("\\u%04x", codepoint);
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        for (int i = 0; i < len; i++) {
            write(cbuf[i + off]);
        }
    }

    @Override
    public void write(String str, int off, int len) throws IOException {
        for (int i = 0; i < len; i++) {
            write(str.charAt(i + off));
        }
    }

    @Override
    public void write(int c) throws IOException {
        if (isAsciiCharacter(c)) {
            out.write(c);
        } else {
            out.write(getEscaped(c));
        }
    }
}

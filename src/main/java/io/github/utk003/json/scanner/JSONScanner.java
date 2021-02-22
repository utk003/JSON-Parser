/*
MIT License

Copyright (c) 2020 Utkarsh Priyam

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */

package io.github.utk003.json.scanner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * A default implementation of {@link Scanner}
 * <p>
 * This {@code Scanner} takes input as an {@link InputStream}.
 *
 * @author Utkarsh Priyam (<a href="https://github.com/utk003" target="_top">utk003</a>)
 * @version February 2, 2021
 * @see Scanner
 * @see InputStream
 */
public class JSONScanner implements Scanner {
    private final BufferedReader bf;
    private final boolean parseWhiteSpace;

    private char[] nextLine = {};
    private int index = 0, lineNum = 0;

    private long numTokens = 0L;
    @Override
    public long tokensPassed() {
        return numTokens;
    }

    private char currentChar = '\0';
    private String currentToken = "";

    private boolean eof = false;
    @Override
    public boolean hasMore() {
        return !eof;
    }

    /**
     * Creates a new {@code JSONScanner} bound to the given {@link InputStream}
     * <p>
     * This calls {@link #JSONScanner(InputStream, boolean, boolean)} with the
     * arguments {@code (source, true, false)}.
     *
     * @param source The JSON input
     * @see #JSONScanner(InputStream, boolean, boolean)
     * @see InputStream
     */
    public JSONScanner(InputStream source) {
        this(source, true, false);
    }

    /**
     * Creates a new {@code JSONScanner} bound to the given {@link InputStream} and
     * configured with the given configuration arguments
     * <p>
     *
     * @param source          The JSON input
     * @param advanceFirst    Whether or not the first token should be
     *                        loaded immediately (see {@link Scanner#advance()}
     * @param parseWhiteSpace Whether or not white space should be parsed as a token
     * @see Scanner#advance()
     * @see InputStream
     */
    public JSONScanner(InputStream source, boolean advanceFirst, boolean parseWhiteSpace) {
        bf = new BufferedReader(new InputStreamReader(source));
        this.parseWhiteSpace = parseWhiteSpace;

        nextChar();
        if (advanceFirst)
            advance();
    }

    /**
     * Returns the next character in the JSON input
     * <p>
     * This method is for internal use only
     *
     * @return The next {@code char} in the JSON input
     */
    private char nextChar() {
        try {
            while (index >= nextLine.length) {
                String temp = bf.readLine();
                if (eof = temp == null)
                    return currentChar = '\0';
                nextLine = temp.toCharArray();
                lineNum++;
                index = 0;
            }
            return currentChar = nextLine[index++];
        } catch (IOException e) {
            throw new IllegalStateException("Unexpected error while parsing JSON");
        }
    }

    @Override
    public String current() {
        return currentToken;
    }
    @Override
    public String advance() {
        numTokens++;
        StringBuilder builder = new StringBuilder();
        if (isWhiteSpace(currentChar)) {
            if (parseWhiteSpace) {
                builder.append(currentChar);
                while (isWhiteSpace(nextChar()))
                    builder.append(currentChar);
                return currentToken = builder.toString();
            } else
                //noinspection StatementWithEmptyBody
                while (isWhiteSpace(nextChar())) /* do nothing */ ;
        }

        if (currentChar == '"') {
            builder.append(currentChar);
            while (nextChar() != '"') {
                builder.append(currentChar);
                if (currentChar == '\\')
                    builder.append(nextChar());
            }
            builder.append(currentChar);
            nextChar();
            return currentToken = builder.toString();
        }

        if (isNumberOrValueChar(currentChar)) {
            builder.append(currentChar);
            while (isNumberOrValueChar(nextChar()))
                builder.append(currentChar);
            return currentToken = builder.toString();
        }

        builder.append(currentChar);
        nextChar();
        return currentToken = builder.toString();
    }

    /**
     * Returns whether or not given character is part
     * of a valid JSON number or primitive value
     * <p>
     * This method is for internal use only
     *
     * @param c The {@code char} to check
     * @return {@code true}, if the character is valid in a JSON number or primitive value; otherwise, {@code false}
     */
    private boolean isNumberOrValueChar(char c) {
        return '0' <= c && c <= '9' ||
                'a' <= c && c <= 'z' ||
                'A' <= c && c <= 'Z' ||
                '+' == c || '-' == c || '.' == c;
    }

    /**
     * Returns whether or not given character is a whitespace character
     * <p>
     * This method is for internal use only
     *
     * @param c The {@code char} to check
     * @return {@code true}, if the character is a whitespace character; otherwise, {@code false}
     */
    private boolean isWhiteSpace(char c) {
        return c == ' ' || c == '\n' || c == '\r' || c == '\t';
    }

    /**
     * Returns the current line number and column of this {@code JSONScanner}
     * <p>
     * This value is calculated from the new lines in the JSON input and the
     * number of characters parsed between consecutive lines
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return lineNum + " " + index;
    }
}

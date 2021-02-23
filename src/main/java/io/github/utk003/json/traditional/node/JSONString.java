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

package io.github.utk003.json.traditional.node;

import io.github.utk003.json.scanner.Scanner;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

/**
 * A {@link JSONValue} that represents a JSON string.
 *
 * @author Utkarsh Priyam (<a href="https://github.com/utk003" target="_top">utk003</a>)
 * @version February 23, 2021
 * @see JSONValue
 */
public class JSONString extends JSONValue {
    /**
     * A publicly accessible reference to the {@code String} this {@code JSONString} represents.
     */
    public final String STRING;
    /**
     * A private copy of the original string (without escaped character substitutions, etc.)
     */
    private final String ORIGINAL;

    /**
     * Creates a {@code JSONString} from the given {@code String} and with the given path
     *
     * @param str  The {@code String} this {@code JSONString} represents
     * @param path This node's path in the JSON tree
     */
    public JSONString(String str, String path) {
        super(ValueType.STRING, path);

        ORIGINAL = str;
        STRING = preprocess(str);
    }

    /**
     * Processes the input {@code String} to replace
     * escaped characters, unicode characters, and more
     *
     * @param s The original string
     * @return The processed string
     * @throws RuntimeException If the string has invalid escaped characters
     */
    private static String preprocess(String s) {
        Iterator<Integer> chars = s.chars().iterator();
        StringBuilder builder = new StringBuilder();

        while (chars.hasNext()) {
            char c = (char) (int) chars.next();
            if (c == '\\') {
                switch (c = (char) (int) chars.next()) {
                    case '\"':
                    case '\\':
                    case '/':
                        builder.append(c);
                        break;

                    case 'b':
                        builder.append("\b");
                        break;
                    case 'f':
                        builder.append("\f");
                        break;
                    case 'n':
                        builder.append("\n");
                        break;
                    case 'r':
                        builder.append("\r");
                        break;
                    case 't':
                        builder.append("\t");
                        break;

                    case 'u':
                        builder.append(
                                hexChar(
                                        (char) (int) chars.next(),
                                        (char) (int) chars.next(),
                                        (char) (int) chars.next(),
                                        (char) (int) chars.next()
                                )
                        );
                        break;

                    default:
                        throw new RuntimeException("Invalid character escape encountered while parsing JSON");
                }
            } else
                builder.append(c);
        }
        return builder.toString();
    }

    /**
     * Converts the given 4 characters into a single unicode character.
     *
     * @param c1 Character 1
     * @param c2 Character 2
     * @param c3 Character 3
     * @param c4 Character 4
     * @return The single unicode character equivalent to {@code \\u<c1><c2><c3><c4>}
     */
    private static char hexChar(char c1, char c2, char c3, char c4) {
        return (char) (4096 * hex(c1) + 256 * hex(c2) + 16 * hex(c3) + hex(c4));
    }

    /**
     * Converts the given character into an integer between 0 and 15, inclusive.
     *
     * @param c The input character
     * @return The integer equivalent from the hexadecimal to decimal translation
     * @throws IllegalArgumentException If the argument is not an alphanumeric character
     */
    private static int hex(char c) {
        if ('0' <= c && c <= '9')
            return c - '0';
        if ('A' <= c && c <= 'Z')
            return c - 'A' + 10;
        if ('a' <= c && c <= 'z')
            return c - 'a' + 10;
        throw new IllegalArgumentException("'" + c + "' is not a valid hex character");
    }

    /**
     * Parses a {@code JSONString} from the given {@link Scanner}.
     * <p>
     * The created {@code JSONString} will have the specified path.
     *
     * @param s    The input source {@code Scanner}
     * @param path The {@code JSONString}'s path in the JSON tree
     * @return The newly created {@code JSONString}
     * @see JSONValue#parseJSON(Scanner)
     * @see JSONValue#parseJSON(Scanner, String)
     */
    static JSONString parseString(Scanner s, String path) {
        String token = s.current();
        return new JSONString(token.substring(1, token.length() - 1), path);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<JSONValue> findElements(PathTrace[] tokenizedPath, int index) {
        return index == tokenizedPath.length ? Collections.singleton(this) : Collections.emptySet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void print(PrintStream out, int depth) {
        outputString(out, "\"");
        outputString(out, ORIGINAL);
        outputString(out, "\"");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return STRING.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof JSONString && STRING.equals(((JSONString) obj).STRING);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "\"" + STRING + "\"";
    }
}

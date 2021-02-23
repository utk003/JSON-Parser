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
import java.util.Objects;

/**
 * A {@link JSONValue} that represents a JSON primitive (a boolean or {@code null}).
 *
 * @author Utkarsh Priyam (<a href="https://github.com/utk003" target="_top">utk003</a>)
 * @version February 23, 2021
 * @see JSONValue
 */
public class JSONPrimitive extends JSONValue {
    /**
     * A publicly accessible reference to this primitive.
     */
    public final Boolean VALUE;

    /**
     * Creates a {@code JSONPrimitive} from the given {@code Boolean} and with the given path
     *
     * @param val  The {@code Boolean} this {@code JSONPrimitive} represents
     * @param path This node's path in the JSON tree
     */
    public JSONPrimitive(Boolean val, String path) {
        super(ValueType.PRIMITIVE, path);
        VALUE = val;
    }
    /**
     * Creates a {@code JSONPrimitive} from the given {@code String} and with the given path
     *
     * @param s    The {@code String} form of the primitive this {@code JSONPrimitive} represents
     * @param path This node's path in the JSON tree
     */
    public JSONPrimitive(String s, String path) {
        this("true".equals(s) ? (Boolean) true : "false".equals(s) ? false : null, path);
    }

    /**
     * Parses a {@code JSONPrimitive} from the given {@link Scanner}.
     * <p>
     * The created {@code JSONPrimitive} will have the specified path.
     *
     * @param s    The input source {@code Scanner}
     * @param path The {@code JSONPrimitive}'s path in the JSON tree
     * @return The newly created {@code JSONPrimitive}
     * @see JSONValue#parseJSON(Scanner)
     * @see JSONValue#parseJSON(Scanner, String)
     */
    static JSONPrimitive parsePrimitive(Scanner s, String path) {
        return new JSONPrimitive(s.current(), path);
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
        outputString(out, "" + VALUE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return VALUE == null ? 0 : VALUE.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof JSONPrimitive && Objects.equals(VALUE, ((JSONPrimitive) obj).VALUE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "" + VALUE;
    }
}

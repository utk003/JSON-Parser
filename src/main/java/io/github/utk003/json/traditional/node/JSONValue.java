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
import io.github.utk003.util.misc.Verify;

import java.io.PrintStream;
import java.util.*;

/**
 * An abstract class that represents a node for a JSON tree.
 * <p>
 * A {@code JSONValue} can represent an object, an array, a string,
 * a number, a boolean (primitive), or {@code null} (primitive).
 * The specific type is stored as a {@link ValueType}.
 * <p>
 * A {@code JSONValue} stores its element type, its path in the JSON
 * tree, and any children elements depending on its specific JSON type.
 * Specifically, this element will have children if and only if it is also
 * a {@link JSONStorageElement}, with the parameterized type being either
 * a {@code String} or an {@code Integer}.
 * <p>
 * A {@code JSONValue} also provides utility methods for printing out
 * the JSON it represents, both in a nicely formatted form as well as
 * in a condensed form that has no whitespace.
 * <p>
 * Lastly, the {@link #findElements(String)} method can be used to find all
 * elements in the tree whose paths match a specific path expression.
 *
 * @author Utkarsh Priyam (<a href="https://github.com/utk003" target="_top">utk003</a>)
 * @version February 23, 2021
 * @see ValueType
 * @see JSONStorageElement
 */
public abstract class JSONValue {
    /**
     * The default path to the root element of a JSON tree.
     * <p>
     * This path can be used to perform an anchored search over
     * the JSON tree or to properly interpret the {@link #PATH}
     * of a {@code JSONValue}.
     */
    public static final String ROOT_PATH = "<root>";

    /**
     * An enum for all the different possible types of {@link JSONValue}.
     * <p>
     * These enum values correspond as follows:
     * <ul>
     * <li>{@code OBJECT} for {@link JSONObject}s
     * <li>{@code ARRAY} for {@link JSONArray}s
     * <li>{@code NUMBER} for {@link JSONNumber}s
     * <li>{@code STRING} for {@link JSONString}s
     * <li>{@code PRIMITIVE} for {@link JSONPrimitive}s
     * </ul>
     *
     * @author Utkarsh Priyam (<a href="https://github.com/utk003" target="_top">utk003</a>)
     * @version February 23, 2021
     */
    public enum ValueType {
        OBJECT, ARRAY, NUMBER, STRING, PRIMITIVE
    }

    /**
     * This {@code JSONValue}'s type (see {@link ValueType})
     */
    public final ValueType TYPE;
    /**
     * This {@code JSONValue}'s path in its JSON tree
     */
    public final String PATH;

    /**
     * Creates a new {@code JSONValue} with the given type and path.
     *
     * @param type The type of this {@code JSONValue} (see {@link #TYPE})
     * @param path The path of this {@code JSONValue} (see {@link #PATH})
     * @see #TYPE
     * @see #PATH
     */
    protected JSONValue(ValueType type, String path) {
        TYPE = type;
        PATH = path;
    }

    /**
     * An internal utility class for tracking whether or not
     * a {@link JSONValue}'s path matches the search expression.
     * <p>
     * A {@code PathTrace} can represent either an index in a
     * JSON array or a key for a JSON object. If it represents an
     * index, then {@link #KEY} will be {@code null}. Otherwise,
     * it will not be {@code null}, and additionally {@link #INDEX}
     * will always be {@code -1}. In other words, whether or not this
     * {@code PathTrace} represents a key for a JSON object or an index
     * for a JSON array can be uniquely determined from the nullity
     * of the {@code String} key variable.
     *
     * @author Utkarsh Priyam (<a href="https://github.com/utk003" target="_top">utk003</a>)
     * @version February 23, 2021
     * @see #INDEX
     * @see #KEY
     * @see #findElements(String)
     */
    protected static final class PathTrace {
        /**
         * The (potential) index this {@code PathTrace} represents in a JSON array.
         * <p>
         * If this index is {@code -1}, then it means that either this index can be
         * any valid index or this {@code PathTrace} represents an object key instead,
         * depending on the nullity of {@link #KEY}.
         * <p>
         * If this index is not {@code -1}, then this {@code PathTrace} must necessarily
         * represent an index, and {@link #KEY} will be {@code null}.
         */
        public final int INDEX;
        /**
         * The (potential) {@code String} key this {@code PathTrace} represents for a JSON object.
         * <p>
         * If this key is {@code null}, then this {@code PathTrace} necessarily represents an
         * index for a JSON array.
         * <p>
         * Otherwise, if this key is not {@code null}, then {@link #INDEX} will necessarily
         * be {@code -1}. If this key is {@code "*"}, then any child of the object matches.
         * Otherwise, the child must specifically match the value of this key.
         */
        public final String KEY;

        /**
         * A private constructor to create a {@code PathTrace}
         * with the given key or index.
         *
         * @param key   The key or index in string form
         * @param isKey If {@code true}, then the {@code key} argument is taken as is into {@link #KEY};
         *              otherwise, the argument is parsed into an {@code int} first
         */
        private PathTrace(String key, boolean isKey) {
            Verify.requireNotNull(key);

            if (isKey) {
                INDEX = -1;
                KEY = key;
            } else {
                INDEX = key.equals("*") ? -1 : Integer.parseInt(key);
                KEY = null;
            }
        }
    }

    /**
     * Parses a JSON tree from the input scanner and returns
     * a {@code JSONValue} corresponding to the root of the tree.
     * <p>
     * This method provides the implementation of the recursive parsing
     * methods {@code parseRecursive(...)} from the
     * {@link io.github.utk003.json.traditional.JSONParser} class.
     *
     * @param s The input scanner
     * @return The root of the parsed JSON tree
     * @see io.github.utk003.json.traditional.JSONParser
     */
    public static JSONValue parseJSON(Scanner s) {
        return parseJSON(s, ROOT_PATH);
    }
    /**
     * A package-private helper for JSON parsing that additionally
     * tracks the path of the currently-parsed element in the tree.
     *
     * @param s    The input source scanner
     * @param path The path of the current element in the JSON tree
     * @return The root of a JSON tree parsed from the input {@link Scanner}
     */
    static JSONValue parseJSON(Scanner s, String path) {
        char c = s.current().charAt(0);
        switch (c) {
            case '{':
                return JSONObject.parseObject(s, path);

            case '[':
                return JSONArray.parseArray(s, path);

            case '"':
                return JSONString.parseString(s, path);

            default:
                if (c == '-' || '0' <= c && c <= '9')
                    return JSONNumber.parseNumber(s, path);
                else
                    return JSONPrimitive.parsePrimitive(s, path);
        }
    }

    /**
     * Returns a {@code Collection} of {@code JSONValue}s corresponding
     * to all elements in the JSON tree rooted at this {@code JSONValue}
     * whose path in the tree matches the targt path specified by the
     * {@code path} argument.
     * <p>
     * The path consists of JSON object keys and array indices separated by
     * periods ({@code .}) for the keys and surrounded by brackets ({@code []})
     * for the indices.
     * Additionally, an asterisk ({@code *}) can be used to
     * designate a wildcard element, which means any child at that level will
     * match that specific part of the path.
     * Lastly, if {@link #ROOT_PATH} is provided as the first step in the path,
     * then the path will be treated as "anchored". In other words, the path
     * will be treated as an absolute path from the root of the tree. If the
     * root path is not included, then any element whose path matches the
     * target path (from any point along the path of the element) will match
     * with the target path.
     *
     * @param path The target path along the JSON tree
     * @return A {@code Collection} of all {@code JSONValue}s whose path matches the target path
     */
    public final Collection<JSONValue> findElements(String path) {
        String[] splitPath = path.split("[.\\[]");
        ArrayList<PathTrace> pathList = new ArrayList<>();

        for (String element : splitPath) {
            int lenMin1 = element.length() - 1;
            if (element.charAt(lenMin1) == ']')
                pathList.add(new PathTrace(element.substring(0, lenMin1), false));
            else
                pathList.add(new PathTrace(element, true));
        }

        PathTrace[] trace = pathList.toArray(new PathTrace[0]);
        if (trace.length == 0)
            return Collections.singleton(this);
        return findElements(trace, ROOT_PATH.equals(trace[0].KEY) ? 1 : 0);
    }
    /**
     * A protected helper for {@link #findElements(String)} that must
     * be implemented by any subclass.
     *
     * @param tokenizedPath An array of all {@link PathTrace} elements in the path
     * @param index         The current index of the search in the array
     * @return A {@code Collection} of all {@code JSONValue}s in the subtree rooted at this {@code JSONValue} whose path matches the target path
     */
    protected abstract Collection<JSONValue> findElements(PathTrace[] tokenizedPath, int index);

    /**
     * Prints this {@code JSONValue} to the specified {@link PrintStream}
     * in a nicely formatted way.
     *
     * @param out The target output stream
     */
    public final void print(PrintStream out) {
        print(out, 0);
    }
    /**
     * Prints this {@code JSONValue} to the specified {@link PrintStream}
     * in a nicely formatted way.
     * <p>
     * This method is equivalent to
     * <pre>
     * this.print(out);
     * out.println();
     * </pre>
     *
     * @param out The target output stream
     * @see #print(PrintStream)
     */
    public final void println(PrintStream out) {
        print(out);
        out.println();
    }

    /**
     * A protected utility method for printing the specified {@code String}
     * to the specified {@link PrintStream} with a depth of {@code 0}.
     * <p>
     * This method is equivalent to
     * <pre>
     * this.outputString(out, toWrite, 0);
     * </pre>
     *
     * @param out     The target output stream
     * @param toWrite The string to output
     * @see #outputString(PrintStream, String, int)
     */
    protected final void outputString(PrintStream out, String toWrite) {
        outputString(out, toWrite, 0);
    }
    /**
     * A protected utility method for printing the specified {@code String}
     * to the specified {@link PrintStream} with the specified depth.
     * <p>
     * The output depth refers to the number of indents that must be included
     * before the {@code toWrite} argument is printed.
     *
     * @param out     The target output stream
     * @param toWrite The string to output
     * @param depth   The depth to print at
     */
    protected final void outputString(PrintStream out, String toWrite, int depth) {
        for (int i = 0; i < depth; i++)
            out.print("  ");
        out.print(toWrite);
    }

    /**
     * A protected utility method for printing a new line to the specified {@link PrintStream}.
     * <p>
     * This method is equivalent to
     * <pre>
     * out.println();
     * </pre>
     *
     * @param out The target output stream
     */
    protected final void outputNewLine(PrintStream out) {
        out.println();
    }

    /**
     * A protected utility method for printing the specified {@code String}
     * to the specified {@link PrintStream} followed by a new line.
     * <p>
     * This method is equivalent to
     * <pre>
     * this.outputString(out, toWrite);
     * out.println();
     * </pre>
     *
     * @param out     The target output stream
     * @param toWrite The string to output
     * @see #outputString(PrintStream, String)
     */
    protected final void outputStringWithNewLine(PrintStream out, String toWrite) {
        outputString(out, toWrite);
        out.println();
    }

    /**
     * Prints the children of this {@code JSONValue} in a nicely formatted
     * way by using the {@code outputString(...)} methods provided by this class.
     * <p>
     * This method should be implemented by all subclasses.
     *
     * @param out   The output stream to print to
     * @param depth The current depth of the nested children (for formatting purposes)
     */
    protected abstract void print(PrintStream out, int depth);

    /**
     * This method should be implemented by all subclasses.
     * {@inheritDoc}
     */
    @Override
    public abstract int hashCode();

    /**
     * This method should be implemented by all subclasses.
     * {@inheritDoc}
     */
    @Override
    public abstract boolean equals(Object obj);

    /**
     * This method should be implemented by all subclasses.
     * {@inheritDoc}
     */
    @Override
    public abstract String toString();
}
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
import io.github.utk003.util.data.tuple.immutable.ImmutablePair;
import io.github.utk003.util.misc.Verifier;

import java.io.PrintStream;
import java.util.*;

/**
 * A {@link JSONValue} that represents a JSON object.
 * <p>
 * This class also extends {@link JSONStorageElement} with
 * a parameter type of {@code String}.
 *
 * @author Utkarsh Priyam (<a href="https://github.com/utk003" target="_top">utk003</a>)
 * @version February 23, 2021
 * @see JSONValue
 * @see JSONStorageElement
 */
public class JSONObject extends JSONValue implements JSONStorageElement<String> {
    private final Map<String, JSONValue> ELEMENTS;

    /**
     * Creates a new {@code JSONObject} with the specified path in the JSON tree.
     *
     * @param path This node's path in the JSON tree
     */
    public JSONObject(String path) {
        super(ValueType.OBJECT, path);
        ELEMENTS = new HashMap<>();
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public int numElements() {
        return ELEMENTS.size();
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return ELEMENTS.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void modifyElement(String key, JSONValue val) {
        ELEMENTS.put(key, val);
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public JSONValue getElement(String key) {
        return ELEMENTS.get(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<JSONValue> getElements() {
        return Collections.unmodifiableCollection(ELEMENTS.values());
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public LinkedList<ImmutablePair<String, JSONValue>> getElementsPaired() {
        LinkedList<ImmutablePair<String, JSONValue>> list = new LinkedList<>();
        for (Map.Entry<String, JSONValue> e : ELEMENTS.entrySet())
            list.addLast(new ImmutablePair<>(e.getKey(), e.getValue()));
        return list;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public ImmutablePair<LinkedList<String>, LinkedList<JSONValue>> getElementsAsPairedLists() {
        ImmutablePair<LinkedList<String>, LinkedList<JSONValue>> pair = new ImmutablePair<>(new LinkedList<>(), new LinkedList<>());
        for (Map.Entry<String, JSONValue> e : ELEMENTS.entrySet()) {
            pair.FIRST.addLast(e.getKey());
            pair.SECOND.addLast(e.getValue());
        }
        return pair;
    }

    /**
     * Parses a {@code JSONObject} from the given {@link Scanner}.
     * <p>
     * The created {@code JSONObject} will have the specified path.
     *
     * @param s    The input source {@code Scanner}
     * @param path The {@code JSONObject}'s path in the JSON tree
     * @return The newly created {@code JSONObject}
     * @see JSONValue#parseJSON(Scanner)
     * @see JSONValue#parseJSON(Scanner, String)
     */
    static JSONObject parseObject(Scanner s, String path) {
        JSONObject obj = new JSONObject(path);

        String token;
        do {
            token = s.advance();
            if (token.equals("}"))
                break;

            // skip colon (:)
            Verifier.requireEqual(s.advance(), ":", "Malformed JSON Object: key should be followed by a colon (:)");

            s.advance(); // load first token of value
            obj.ELEMENTS.put(
                    token = token.substring(1, token.length() - 1), // remove quotes from key
                    JSONValue.parseJSON(s, path + "." + token)
            );
        } while (s.advance().equals(","));
        return obj;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<JSONValue> findElements(PathTrace[] tokenizedPath, int index) {
        if (index == tokenizedPath.length)
            return Collections.singleton(this);

        PathTrace trace = tokenizedPath[index];
        if (trace.KEY == null)
            return Collections.emptySet();

        index++;

        Collection<JSONValue> elements;
        if (trace.KEY.equals("*")) {
            elements = new LinkedList<>();
            for (JSONValue element : ELEMENTS.values())
                elements.addAll(element.findElements(tokenizedPath, index));
        } else
            elements = getElement(trace.KEY).findElements(tokenizedPath, index);
        return elements;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void print(PrintStream out, int depth) {
        depth++;
        outputStringWithNewLine(out, "{");

        int count = 0, total = ELEMENTS.size();
        for (Map.Entry<String, JSONValue> entry : ELEMENTS.entrySet()) {
            outputString(out, "", depth);
            outputString(out, "\"");
            outputString(out, entry.getKey());
            outputString(out, "\"");
            outputString(out, ": ");

            entry.getValue().print(out, depth);

            if (++count != total)
                outputStringWithNewLine(out, ",");
            else
                outputNewLine(out);
        }

        depth--;
        outputString(out, "}", depth);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return ELEMENTS.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof JSONObject && ELEMENTS.equals(((JSONObject) obj).ELEMENTS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, JSONValue> element : ELEMENTS.entrySet())
            builder.append(",\"").append(element.getKey()).append("\":").append(element.getValue());
        return "{" + (builder.length() == 0 ? "" : builder.substring(1)) + "}";
    }
}

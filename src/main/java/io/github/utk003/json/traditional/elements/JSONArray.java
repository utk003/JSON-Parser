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

package io.github.utk003.json.traditional.elements;

import io.github.utk003.json.scanner.Scanner;
import io.github.utk003.util.data.immutable.ImmutablePair;

import java.io.PrintStream;
import java.util.*;

public class JSONArray extends JSONValue implements JSONStorageElement<Integer> {
    private final List<JSONValue> ELEMENTS = new ArrayList<>();

    public JSONArray(String path) {
        super(ValueType.ARRAY, path);
    }
    public JSONArray(JSONValue[] elements, String path) {
        this(path);
        ELEMENTS.addAll(Arrays.asList(elements));
    }
    public JSONArray(List<JSONValue> elements, String path) {
        this(path);
        ELEMENTS.addAll(elements);
    }

    @Override
    public final int numElements() {
        return ELEMENTS.size();
    }
    @Override
    public final boolean isEmpty() {
        return ELEMENTS.isEmpty();
    }

    @Override
    public void modifyElement(Integer index, JSONValue obj) {
        if (index == null || index == numElements()) ELEMENTS.add(obj);
        else ELEMENTS.set(index, obj);
    }
    @Override
    public JSONValue getElement(Integer index) {
        return ELEMENTS.get(index);
    }
    @Override
    public Collection<JSONValue> getElements() {
        return Collections.unmodifiableList(ELEMENTS);
    }
    @Override
    public ImmutablePair<LinkedList<String>, LinkedList<JSONValue>> getElementsPaired() {
        ImmutablePair<LinkedList<String>, LinkedList<JSONValue>> pair = new ImmutablePair<>(new LinkedList<>(), new LinkedList<>());
        for (JSONValue e : ELEMENTS) {
            pair.FIRST.addLast(null);
            pair.SECOND.addLast(e);
        }
        return pair;
    }

    static JSONArray parseArray(Scanner s, String path) {
        JSONArray obj = new JSONArray(path);
        int i = 0;
        do {
            if (s.advance().equals("]"))
                break;

            obj.ELEMENTS.add(JSONValue.parseJSON(s, path + "[" + i++ + "]"));
        } while (s.advance().equals(","));
        return obj;
    }

    @Override
    public Collection<JSONValue> findElements(PathTrace[] tokenizedPath, int index) {
        if (index == tokenizedPath.length)
            return Collections.singleton(this);

        PathTrace trace = tokenizedPath[index];
        if (trace.KEY != null)
            return Collections.emptySet();

        index++;

        Collection<JSONValue> elements;
        if (trace.INDEX < 0) {
            elements = new LinkedList<>();
            for (JSONValue element : ELEMENTS)
                elements.addAll(element.findElements(tokenizedPath, index));
        } else
            elements = getElement(trace.INDEX).findElements(tokenizedPath, index);
        return elements;
    }

    @Override
    protected void print(PrintStream out, int depth) {
        depth++;
        outputStringWithNewLine(out, "[");

        int count = 0, total = ELEMENTS.size();
        for (JSONValue jsonValue : ELEMENTS) {
            outputString(out, "", depth);
            jsonValue.print(out, depth);

            if (++count != total)
                outputStringWithNewLine(out, ",");
            else
                outputStringWithNewLine(out);
        }

        depth--;
        outputString(out, "]", depth);
    }


    @Override
    public int hashCode() {
        return ELEMENTS.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof JSONArray && ELEMENTS.equals(((JSONArray) obj).ELEMENTS);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (JSONValue element : ELEMENTS)
            builder.append(",").append(element);
        return "[" + (builder.length() == 0 ? "" : builder.substring(1)) + "]";
    }
}

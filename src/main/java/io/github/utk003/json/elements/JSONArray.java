////////////////////////////////////////////////////////////////////////////////////
// MIT License                                                                    //
//                                                                                //
// Copyright (c) 2020 Utkarsh Priyam                                              //
//                                                                                //
// Permission is hereby granted, free of charge, to any person obtaining a copy   //
// of this software and associated documentation files (the "Software"), to deal  //
// in the Software without restriction, including without limitation the rights   //
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell      //
// copies of the Software, and to permit persons to whom the Software is          //
// furnished to do so, subject to the following conditions:                       //
//                                                                                //
// The above copyright notice and this permission notice shall be included in all //
// copies or substantial portions of the Software.                                //
//                                                                                //
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR     //
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,       //
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE    //
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER         //
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,  //
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE  //
// SOFTWARE.                                                                      //
////////////////////////////////////////////////////////////////////////////////////

package io.github.utk003.json.elements;

import io.github.utk003.json.JSONParser;
import io.github.utk003.json.Scanner;
import io.github.utk003.util.misc.Verify;

import java.io.PrintStream;
import java.util.*;

public class JSONArray extends JSONValue implements JSONStorageElement<Integer> {
    private final List<JSONValue> ELEMENTS = new ArrayList<>();

    private JSONArray() {
        super(ValueType.ARRAY);
    }
    public JSONArray(JSONValue... elements) {
        this();
        ELEMENTS.addAll(Arrays.asList(elements));
    }
    public JSONArray(List<JSONValue> elements) {
        this();
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
        if (index == numElements()) ELEMENTS.add(obj);
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

    public static JSONArray parseArray(Scanner s) {
        JSONArray obj = new JSONArray();
        do {
            if (s.advance().equals("]"))
                break;

            obj.ELEMENTS.add(JSONParser.parseRecursive(s));
        } while (s.advance().equals(","));
        return obj;
    }

    @Override
    public Collection<JSONValue> findElements(PathTrace[] tokenizedPath, int index) {
        PathTrace trace = tokenizedPath[index];
        Verify.requireNull(trace.KEY);

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
}

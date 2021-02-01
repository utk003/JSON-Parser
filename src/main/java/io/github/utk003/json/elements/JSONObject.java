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

public class JSONObject extends JSONValue implements JSONStorageElement<String> {
    private final Map<String, JSONValue> ELEMENTS;

    public JSONObject() {
        super(ValueType.OBJECT);
        ELEMENTS = new HashMap<>();
    }

    @Override
    public int numElements() {
        return ELEMENTS.size();
    }

    @Override
    public boolean isEmpty() {
        return ELEMENTS.isEmpty();
    }

    @Override
    public void modifyElement(String key, JSONValue val) {
        ELEMENTS.put(key, val);
    }
    @Override
    public JSONValue getElement(String key) {
        return ELEMENTS.get(key);
    }
    @Override
    public Collection<JSONValue> getElements() {
        return Collections.unmodifiableCollection(ELEMENTS.values());
    }

    public static JSONObject parseObject(Scanner s) {
        JSONObject obj = new JSONObject();

        String token;
        do {
            token = s.advance();
            if (token.equals("}"))
                break;

            // skip colon (:)
            Verify.requireTrue(s.advance().equals(":"));

            s.advance(); // load first token of value
            obj.ELEMENTS.put(token.substring(1, token.length() - 1), JSONParser.parseRecursive(s));
        } while (s.advance().equals(","));
        return obj;
    }

    @Override
    public Collection<JSONValue> findElements(PathTrace[] tokenizedPath, int index) {
        PathTrace trace = tokenizedPath[index];
        Verify.requireNotNull(trace.KEY);

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

    @Override
    protected void print(PrintStream out, int depth) {
        depth++;
        outputStringWithNewLine(out, "{");

        int count = 0, total = ELEMENTS.size();
        for (Map.Entry<String, JSONValue> entry: ELEMENTS.entrySet()) {
            outputString(out, "", depth);
            outputString(out, "\"");
            outputString(out, entry.getKey());
            outputString(out, "\"");
            outputString(out, ": ");

            entry.getValue().print(out, depth);

            if (++count != total)
                outputStringWithNewLine(out, ",");
            else
                outputStringWithNewLine(out);
        }

        depth--;
        outputString(out, "}", depth);
    }
}

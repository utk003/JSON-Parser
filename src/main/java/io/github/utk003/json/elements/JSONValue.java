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

import com.sun.istack.internal.NotNull;
import io.github.utk003.json.Scanner;
import io.github.utk003.util.misc.Verify;

import java.io.PrintStream;
import java.util.*;

public abstract class JSONValue {
    public enum ValueType {
        OBJECT, ARRAY, NUMBER, STRING, PRIMITIVE
    }

    public final ValueType TYPE;
    protected JSONValue(ValueType type) {
        TYPE = type;
    }

    public static final class PathTrace {
        public final int INDEX;
        public final String KEY;

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

    public static JSONValue parseJSON(Scanner s) {
        char c = s.current().charAt(0);
        switch (c) {
            case '{':
                return JSONObject.parseObject(s);

            case '[':
                return JSONArray.parseArray(s);

            case '"':
                return JSONString.parseString(s);

            default:
                if (c == '-' || '0' <= c && c <= '9')
                    return JSONNumber.parseNumber(s);
                else
                    return JSONPrimitive.parsePrimitive(s);
        }
    }

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

        return findElements(pathList.toArray(new PathTrace[0]), 0);
    }
    protected abstract Collection<JSONValue> findElements(PathTrace[] tokenizedPath, int index);

    protected static final String PRINT_INDENT = "  ";

    public final void print(PrintStream out) {
        print(out, 0);
    }
    public final void println(PrintStream out) {
        print(out);
        out.println();
    }

    protected final void outputString(PrintStream out, String toWrite) {
        outputString(out, toWrite, 0);
    }
    protected final void outputString(PrintStream out, String toWrite, int depth) {
        for (int i = 0; i < depth; i++)
            out.print(PRINT_INDENT);
        out.print(toWrite);
    }

    protected final void outputStringWithNewLine(PrintStream out) {
        outputStringWithNewLine(out, "");
    }
    protected final void outputStringWithNewLine(PrintStream out, String toWrite) {
        outputString(out, toWrite);
        out.println();
    }

    protected abstract void print(PrintStream out, int depth);

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);

    @Override
    public abstract String toString();
}
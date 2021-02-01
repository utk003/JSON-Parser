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

import io.github.utk003.json.Scanner;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

public class JSONPrimitive extends JSONValue {
    private final Boolean VALUE;

    public JSONPrimitive(Boolean val) {
        super(ValueType.PRIMITIVE);
        VALUE = val;
    }
    public JSONPrimitive(String s) {
        this("true".equals(s) ? (Boolean) true : "false".equals(s) ? false : null);
    }

    public Boolean getValue() {
        return VALUE;
    }

    static JSONPrimitive parsePrimitive(Scanner s) {
        return new JSONPrimitive(s.current());
    }

    @Override
    public Collection<JSONValue> findElements(PathTrace[] tokenizedPath, int index) {
        return index == tokenizedPath.length ? Collections.singleton(this) : Collections.emptySet();
    }

    @Override
    protected void print(PrintStream out, int depth) {
        outputString(out, "" + VALUE);
    }

    @Override
    public int hashCode() {
        return VALUE == null ? 0 : VALUE.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof JSONPrimitive && Objects.equals(VALUE, ((JSONPrimitive) obj).VALUE);
    }

    @Override
    public String toString() {
        return "" + VALUE;
    }
}

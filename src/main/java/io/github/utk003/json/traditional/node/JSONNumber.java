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

public class JSONNumber extends JSONValue {
    public final Number NUMBER;

    public JSONNumber(String s, String path) {
        this(s.contains("e") || s.contains("E") || s.contains(".") ? (Number) Double.parseDouble(s) : (Number) Long.parseLong(s), path);
    }
    public JSONNumber(Number val, String path) {
        super(ValueType.NUMBER, path);
        NUMBER = val;
    }

    public Number getNumber() {
        return NUMBER;
    }

    static JSONNumber parseNumber(Scanner s, String path) {
        return new JSONNumber(s.current(), path);
    }

    @Override
    public Collection<JSONValue> findElements(PathTrace[] tokenizedPath, int index) {
        return index == tokenizedPath.length ? Collections.singleton(this) : Collections.emptySet();
    }

    @Override
    protected void print(PrintStream out, int depth) {
        outputString(out, "" + NUMBER);
    }

    @Override
    public int hashCode() {
        return NUMBER.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof JSONNumber && NUMBER.equals(((JSONNumber) obj).NUMBER);
    }

    @Override
    public String toString() {
        return "" + NUMBER;
    }
}

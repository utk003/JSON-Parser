package io.github.utk003.json.scanner;/*
MIT License

Copyright (c) 2020-2021 Utkarsh Priyam

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

import io.github.utk003.json.traditional.JSONParser;
import io.github.utk003.json.traditional.node.JSONValue;

import java.io.*;

public class NonRecursiveParserTest {
    private static final String IN = "test/in/", OUT = "test/out/non-recursive/";
    public static void main(String[] args) throws IOException {
        String fileName = "test.json";

        System.out.println("Starting Non-Recursive JSON Parser Test on \"" + fileName + "\"");

        int numIterations = 1000;
        long start = System.nanoTime();
        for (int i = 0; i < numIterations; i++) {
            JSONValue json = JSONParser.parseNonRecursive(new FileInputStream(IN + fileName));
            json.println(new PrintStream(new FileOutputStream(OUT + fileName)));
        }
        long end = System.nanoTime();

        System.out.println("Parsing Duration: " + (end - start) / 1_000_000.0 / numIterations + " ms");
        System.out.println("Parsing Duration: " + (end - start) / 1_000_000_000.0 / numIterations + " s");

        System.out.println("JSON parsing output in \"" + OUT + fileName + "\"");
    }
}

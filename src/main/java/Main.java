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

import io.github.utk003.json.JSONParser;
import io.github.utk003.json.elements.*;
import io.github.utk003.json.Scanner;

import java.io.*;

public class Main {
    private static long time;
    private static void markStartTime() {
        time = System.nanoTime();
    }
    private static long readAndPrintTime(String message) {
        long delta = System.nanoTime() - time;
        if (message != null) {
            System.out.println(message + ": " + delta / 1_000_000.0 + " ms");
            System.out.println(message + ": " + delta / 1_000_000_000.0 + " s");
        }
        return delta;
    }

    public static void main(String[] args) throws IOException {
        String fileName = "test.json";
        JSONValue json = JSONParser.parse(new FileInputStream(fileName));
    }

    private static void scanningBenchmark(String file) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        String s;
        char[] arr;
        markStartTime();
        while ((s = br.readLine()) != null) {
            arr = s.toCharArray();
            for (char c : arr) ;
        }
        readAndPrintTime("Raw BufferedReader Input");

        System.out.println();

        Scanner sc = new Scanner(new FileInputStream(file));
        markStartTime();
        while (sc.hasMore())
            sc.advance();
        long t = readAndPrintTime("Scanner-Tokenized Input");

        System.out.println();

        System.out.println("Number of tokens: " + sc.tokensPassed());
        System.out.println("Time per token: " + t / 1000.0 / sc.tokensPassed() + " μs (microseconds)");
    }
}

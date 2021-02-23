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

package io.github.utk003.json.traditional;

import io.github.utk003.json.scanner.JSONScanner;
import io.github.utk003.json.scanner.Scanner;
import io.github.utk003.json.traditional.node.*;
import io.github.utk003.util.misc.Verify;

import java.io.InputStream;
import java.util.Stack;

import static io.github.utk003.json.traditional.node.JSONValue.ROOT_PATH;

/**
 * A parser for creating JSON trees with {@link JSONValue} nodes
 * from some form of {@link Scanner} or {@code Scanner}-accepted
 * input format.
 * <p>
 *
 */
public class JSONParser {
    public static JSONValue parseRecursive(InputStream source) {
        return JSONValue.parseJSON(new JSONScanner(source));
    }
    public static JSONValue parseRecursive(Scanner scanner) {
        if (scanner.tokensPassed() == 0) scanner.advance();
        Verify.requireTrue(scanner.tokensPassed() == 1);
        return JSONValue.parseJSON(scanner);
    }

    public static JSONValue parseNonRecursive(InputStream source) {
        return parseNonRecursiveHelper(new JSONScanner(source));
    }
    public static JSONValue parseNonRecursive(Scanner scanner) {
        if (scanner.tokensPassed() == 0) scanner.advance();
        Verify.requireTrue(scanner.tokensPassed() == 1);
        return parseNonRecursiveHelper(scanner);
    }

    private static JSONValue parseNonRecursiveHelper(Scanner scanner) {
        Stack<JSONValue> stack = new Stack<>();
        Stack<String> strStack = new Stack<>(), pathTracker = new Stack<>();
        Stack<Integer> numElementsStack = new Stack<>();

        stack.push(getElement(scanner.current(), ROOT_PATH));
        strStack.push(null);
        pathTracker.push(ROOT_PATH);
        numElementsStack.push(1);

        JSONValue.ValueType type = stack.peek().TYPE;
        boolean justAddedStorageElement = type == JSONValue.ValueType.OBJECT || type == JSONValue.ValueType.ARRAY;
        numElementsStack.push(0);

        while (scanner.hasMore()) {
            String str = scanner.advance(), key;
            char c = str.charAt(0);

            if (c == '}' || c == ']' || c == ',') {
                if (justAddedStorageElement)
                    justAddedStorageElement = false;
                else {
                    JSONValue element = stack.pop();
                    //noinspection unchecked
                    ((JSONStorageElement<Object>) stack.peek()).modifyElement(strStack.pop(), element);
                    pathTracker.pop(); // remove path from stack
                    numElementsStack.pop(); // clear num elements counter
                }
                continue;
            }

            if (stack.peek().TYPE == JSONValue.ValueType.OBJECT) {
                key = str.substring(1, str.length() - 1); // str contains key for JSONValue
                scanner.advance(); // skip colon
                str = scanner.advance(); // s.advance() gets first token of nested element
            } else {
                key = null;
                // str contains first token of nested element
            }

            int currIndex = numElementsStack.pop();
            numElementsStack.push(currIndex + 1);

            String path = pathTracker.peek() + (key == null ? "[" + currIndex + "]" : "." + key);
            JSONValue element = getElement(str, path);

            strStack.push(key);
            stack.push(element);

            pathTracker.push(path);
            numElementsStack.push(0);

            justAddedStorageElement = element.TYPE == JSONValue.ValueType.OBJECT || element.TYPE == JSONValue.ValueType.ARRAY;
        }
        return stack.peek();
    }

    private static JSONValue getElement(String token, String path) {
        char c = token.charAt(0);
        switch (c) {
            case '{':
                return new JSONObject(path);

            case '[':
                return new JSONArray(path);

            case '"':
                return new JSONString(token.substring(1, token.length() - 1), path);

            default:
                if (c == '-' || '0' <= c && c <= '9')
                    return new JSONNumber(token, path);
                else
                    return new JSONPrimitive(token, path);
        }
    }
}

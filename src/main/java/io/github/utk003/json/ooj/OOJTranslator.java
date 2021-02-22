/*
MIT License

Copyright (c) 2021 Utkarsh Priyam

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

package io.github.utk003.json.ooj;

import io.github.utk003.json.scanner.Scanner;
import io.github.utk003.json.traditional.elements.JSONStorageElement;
import io.github.utk003.json.traditional.elements.JSONValue;
import io.github.utk003.util.data.immutable.ImmutablePair;
import io.github.utk003.util.misc.Verify;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class OOJTranslator {
    public static <T> T translateRecursive(OOJParser parser, JSONValue root, Class<T> clazz)
            throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchFieldException, NoSuchMethodException {
        return parser.parseRecursive(new JSONValueStreamer(root), clazz);
    }
    public <T> T parseNonRecursive(OOJParser parser, JSONValue root, Class<T> clazz)
            throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchFieldException, NoSuchMethodException {
        return parser.parseNonRecursive(new JSONValueStreamer(root), clazz);
    }

    static class JSONValueStreamer implements Scanner {
        private LinkedList<JSONValue> jsonTop;
        private LinkedList<String> keysTop;

        private final Stack<LinkedList<JSONValue>> JSON = new Stack<>();
        private final Stack<LinkedList<String>> KEYS = new Stack<>();

        private static <K> LinkedList<K> getSingletonLinkedList(K element) {
            LinkedList<K> list = new LinkedList<>();
            list.add(element);
            return list;
        }

        public JSONValueStreamer(JSONValue root) {
            JSON.push(jsonTop = getSingletonLinkedList(root));
            KEYS.push(keysTop = getSingletonLinkedList(JSONValue.ROOT_PATH));

            currentToken = advance0();
            nextToken = advance0();
        }

        private String currentToken, nextToken;
        private long tokensPassed = 1;

        // 0 = nothing so far
        // 1 = returned key
        // 2 = returned color - ready for recursion into obj
        // 3 = done with prev - return comma
        private int keyState = 2;
        private final Stack<Integer> KEY_STATES = new Stack<>();

        private String advance0() {
            if (jsonTop.isEmpty()) {
                JSON.pop();
                KEYS.pop();

                if (JSON.isEmpty())
                    return null;

                jsonTop = JSON.peek();
                keysTop = KEYS.peek();
                keyState = KEY_STATES.pop();

                JSONValue json = jsonTop.removeFirst();
                keysTop.removeFirst();

                return json.TYPE == JSONValue.ValueType.ARRAY ? "]" : "}";
            }

            switch (keyState) {
                case 0:
                    keyState = 1;
                    return "\"" + keysTop.getFirst() + "\"";
                case 1:
                    keyState = 2;
                    return ":";
                case 2:
                    keyState = 3;
                    break;
                case 3:
                    keyState = keysTop.getFirst() != null ? 0 : 2; // !null -> object = 0, null -> array = 2
                    return ",";

                default:
                    throw new IllegalStateException("unexpected error while translating JSON");
            }

            // if here, we need to recurse to the next layer
            JSONValue json = jsonTop.getFirst();
            if (json.TYPE == JSONValue.ValueType.OBJECT || json.TYPE == JSONValue.ValueType.ARRAY) {
                ImmutablePair<LinkedList<String>, LinkedList<JSONValue>> keyJSONPair = ((JSONStorageElement<?>) json).getElementsPaired();
                Verify.requireTrue(keyJSONPair.FIRST.size() == keyJSONPair.SECOND.size());

                jsonTop = keyJSONPair.SECOND;
                keysTop = keyJSONPair.FIRST;

                JSON.push(jsonTop);
                KEYS.push(keysTop);
                KEY_STATES.push(keyState);

                if (json.TYPE == JSONValue.ValueType.OBJECT) {
                    keyState = 0;
                    return "{";
                } else {
                    keyState = 2;
                    return "[";
                }

            } else {
                jsonTop.removeFirst();
                keysTop.removeFirst();

                return json.toString();
            }
        }

        @Override
        public boolean hasMore() {
            return nextToken != null;
        }
        @Override
        public long tokensPassed() {
            return tokensPassed;
        }
        @Override
        public String current() {
            return currentToken;
        }
        @Override
        public String advance() {
            if (nextToken == null)
                return null;

            currentToken = nextToken;
            nextToken = advance0();

            tokensPassed++;
            return currentToken;
        }
    }
}

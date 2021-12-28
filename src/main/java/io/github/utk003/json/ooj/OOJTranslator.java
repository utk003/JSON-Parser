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
import io.github.utk003.json.traditional.node.JSONStorageElement;
import io.github.utk003.json.traditional.node.JSONValue;
import io.github.utk003.util.data.tuple.immutable.ImmutablePair;
import io.github.utk003.util.misc.Verifier;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * A translator for converting traditional JSON trees
 * ({@link JSONValue}s) into POJOs using a {@link OOJParser}.
 * <p>
 * This translate can translate both recursively and non-recursively.
 * Both are roughly equivalent in terms of speed.
 *
 * @author Utkarsh Priyam (<a href="https://github.com/utk003" target="_top">utk003</a>)
 * @version February 23, 2021
 * @see JSONValue
 * @see OOJParser
 */
public class OOJTranslator {
    /**
     * Recursively translates the given JSON tree rooted at the
     * {@code root} argument using the given {@link OOJParser}.
     *
     * @param parser The parser that will do the OOJ parsing
     * @param root   The root of the traditional parsing output
     * @param clazz  The class of the root of the JSON tree
     * @param <T>    The class type of the {@code clazz} argument and this method's return type
     * @return The translated object of type {@code T}
     * @throws IllegalAccessException    If one arises while using Java reflection to translate the JSON
     * @throws InstantiationException    If one arises while using Java reflection to translate the JSON
     * @throws InvocationTargetException If one arises while using Java reflection to translate the JSON
     * @throws NoSuchFieldException      If one arises while using Java reflection to translate the JSON
     * @see OOJParser#parseRecursive(Scanner, Class)
     */
    public static <T> T translateRecursive(OOJParser parser, JSONValue root, Class<T> clazz)
            throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchFieldException {
        return parser.parseRecursive(new JSONValueStreamer(root), clazz);
    }
    /**
     * Non-recursively translates the given JSON tree rooted at the
     * {@code root} argument using the given {@link OOJParser}.
     *
     * @param parser The parser that will do the OOJ parsing
     * @param root   The root of the traditional parsing output
     * @param clazz  The class of the root of the JSON tree
     * @param <T>    The class type of the {@code clazz} argument and this method's return type
     * @return The translated object of type {@code T}
     * @throws IllegalAccessException    If one arises while using Java reflection to translate the JSON
     * @throws InstantiationException    If one arises while using Java reflection to translate the JSON
     * @throws InvocationTargetException If one arises while using Java reflection to translate the JSON
     * @throws NoSuchFieldException      If one arises while using Java reflection to translate the JSON
     * @see OOJParser#parseNonRecursive(Scanner, Class)
     */
    public <T> T parseNonRecursive(OOJParser parser, JSONValue root, Class<T> clazz)
            throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchFieldException {
        return parser.parseNonRecursive(new JSONValueStreamer(root), clazz);
    }

    /**
     * A utility {@link Scanner} that streams a JSON tree on-the-fly
     * for use in re-parsing the JSON or in outputting the original JSON.
     *
     * @author Utkarsh Priyam (<a href="https://github.com/utk003" target="_top">utk003</a>)
     * @version February 23, 2021
     * @see Scanner
     * @see JSONValue
     */
    public static class JSONValueStreamer implements Scanner {
        private LinkedList<JSONValue> topJSON;
        private LinkedList<?> topKeysOrInds;
        boolean topIsKeyNotIndex;

        private final Stack<LinkedList<JSONValue>> JSON = new Stack<>();
        private final Stack<LinkedList<?>> KEYS_OR_INDS = new Stack<>();
        private final Stack<Boolean> IS_KEY_NOT_INDEX = new Stack<>();

        private static <K> LinkedList<K> getSingletonLinkedList(K element) {
            LinkedList<K> list = new LinkedList<>();
            list.add(element);
            return list;
        }

        /**
         * Creates a new {@code JSONValueStreamer} that will stream
         * the JSON of the tree rooted at the specified root.
         *
         * @param root The root of the JSON tree to stream
         */
        public JSONValueStreamer(JSONValue root) {
            JSON.push(topJSON = getSingletonLinkedList(root));
            KEYS_OR_INDS.push(topKeysOrInds = getSingletonLinkedList(JSONValue.ROOT_PATH));
            IS_KEY_NOT_INDEX.push(topIsKeyNotIndex = true);

            currentToken = advance0();
            nextToken = advance0();
        }

        private String currentToken, nextToken;
        private long tokensPassed = 1;

        // 0 = nothing so far
        // 1 = returned key
        // 2 = returned colon - ready for recursion into obj
        // 3 = done with prev - return comma
        private int keyState = 2;
        private final Stack<Integer> KEY_STATES = new Stack<>();

        /**
         * Retrieves the next token from the JSON tree.
         * <p>
         * This method is for internal use, solely in the {@link #advance()} method
         * (and in the constructor for initialization purposes).
         *
         * @return The next token to be streamed
         */
        private String advance0() {
            if (topJSON.isEmpty()) {
                JSON.pop();
                KEYS_OR_INDS.pop();
                IS_KEY_NOT_INDEX.pop();

                if (JSON.isEmpty())
                    return null;

                boolean oldTopIsKeyNotIndex = topIsKeyNotIndex;

                topJSON = JSON.peek();
                topKeysOrInds = KEYS_OR_INDS.peek();
                topIsKeyNotIndex = IS_KEY_NOT_INDEX.peek();
                keyState = KEY_STATES.pop();

                topJSON.removeFirst();
                topKeysOrInds.removeFirst();

                return oldTopIsKeyNotIndex ? "}" : "]";
            }

            switch (keyState) {
                case 0:
                    keyState = 1;
                    return "\"" + topKeysOrInds.getFirst() + "\"";
                case 1:
                    keyState = 2;
                    return ":";
                case 2:
                    keyState = 3;
                    break;
                case 3:
                    keyState = topIsKeyNotIndex ? 0 : 2; // !null -> object = 0, null -> array = 2
                    return ",";

                default:
                    throw new IllegalStateException("unexpected error while translating JSON");
            }

            // if here, we need to recurse to the next layer
            JSONValue json = topJSON.getFirst();
            if (json.TYPE == JSONValue.ValueType.OBJECT || json.TYPE == JSONValue.ValueType.ARRAY) {
                ImmutablePair<? extends LinkedList<?>, LinkedList<JSONValue>> keyJSONPair =
                        ((JSONStorageElement<?>) json).getElementsAsPairedLists();
                Verifier.requireEqual(
                        keyJSONPair.FIRST.size(), keyJSONPair.SECOND.size(),
                        "JSONStorageElement data mismatch: number of keys doesn't match number of JSON child nodes"
                );

                JSON.push(topJSON = keyJSONPair.SECOND);
                KEYS_OR_INDS.push(topKeysOrInds = keyJSONPair.FIRST);
                IS_KEY_NOT_INDEX.push(topIsKeyNotIndex = json.TYPE == JSONValue.ValueType.OBJECT);

                KEY_STATES.push(keyState);

                if (topIsKeyNotIndex) {
                    keyState = 0;
                    return "{";
                } else {
                    keyState = 2;
                    return "[";
                }

            } else {
                topJSON.removeFirst();
                topKeysOrInds.removeFirst();

                return json.toString();
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasMore() {
            return nextToken != null;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public long tokensPassed() {
            return tokensPassed;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String current() {
            return currentToken;
        }
        /**
         * {@inheritDoc}
         */
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

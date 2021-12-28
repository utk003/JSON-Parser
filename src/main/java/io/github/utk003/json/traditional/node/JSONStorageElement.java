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

import io.github.utk003.util.data.tuple.immutable.ImmutablePair;

import java.util.Collection;
import java.util.LinkedList;

/**
 * An interface that represents the two JSON element types that can
 * hold other elements as children in a JSON tree: objects and arrays.
 * <p>
 * Arrays can be differentiated from objects based on the type of the
 * parameter: JSON arrays will use {@code Integer}s as their index,
 * and JSON objects will use {@code String}s.
 * <p>
 * Of the default implementations for {@code JSONValue} node provided,
 * only {@link JSONArray} and {@link JSONObject}, which correspond to
 * JSON arrays and JSON objects respectively, implement {@code JSONStorageElement}.
 *
 * @param <E> The key/index type for this {@code JSONStorageElement}
 * @author Utkarsh Priyam (<a href="https://github.com/utk003" target="_top">utk003</a>)
 * @version February 23, 2021
 */
public interface JSONStorageElement<E> {
    /**
     * Returns how many children this {@code JSONStorageElement} has.
     *
     * @return How many children this {@code JSONStorageElement} has
     */
    int numElements();
    /**
     * Returns true if and only if this {@code JSONStorageElement} has no children.
     * <p>
     * This method is equivalent to
     * <pre>
     * this.numElements() == 0
     * </pre>
     *
     * @return {@code true} if this {@code JSONStorageElement} has no children; otherwise, {@code false}
     */
    boolean isEmpty();

    /**
     * Modifies the child with the specified key/index to the newly specified {@code JSONValue} element.
     *
     * @param key The key/index to replace at
     * @param obj The new child element
     */
    void modifyElement(E key, JSONValue obj);
    /**
     * Gets the child {@code JSONValue} element indentified by the specified key/index
     *
     * @param key The key/index to get from
     * @return The child with that key/index, if it exists; otherwise, {@code null}
     */
    JSONValue getElement(E key);

    /**
     * Returns an immutable {@code Collection} of all {@code JSONValue} children this {@code JSONStorageElement} has.
     *
     * @return An immutable {@code Collection} of all {@code JSONValue} children this {@code JSONStorageElement} has.
     */
    Collection<JSONValue> getElements();

    /**
     * Returns an {@link ImmutablePair} of {@code LinkedList}s that
     * hold all of this {@code JSONStorageElement}'s {@code JSONValue}
     * children as well as their corresponding keys/indices.
     * <p>
     * This method is particularly useful in transforming a JSON tree
     * back into a {@link io.github.utk003.json.scanner.Scanner} in
     * a single pass. Check out {@link io.github.utk003.json.ooj.OOJTranslator.JSONValueStreamer}
     * for more details.
     *
     * @return This {@code JSONStorageElement}'s children and their keys/indices as a pair of {@code LinkedList}s
     */
    ImmutablePair<LinkedList<E>, LinkedList<JSONValue>> getElementsAsPairedLists();
    /**
     * Returns an {@link ImmutablePair} of {@code LinkedList}s that
     * hold all of this {@code JSONStorageElement}'s {@code JSONValue}
     * children as well as their corresponding keys/indices.
     * <p>
     * This method is particularly useful in transforming a JSON tree
     * back into a {@link io.github.utk003.json.scanner.Scanner} in
     * a single pass. Check out {@link io.github.utk003.json.ooj.OOJTranslator.JSONValueStreamer}
     * for more details.
     *
     * @return This {@code JSONStorageElement}'s children and their keys/indices paired in a {@code LinkedList}
     */
    LinkedList<ImmutablePair<E, JSONValue>> getElementsPaired();
}

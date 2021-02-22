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

package io.github.utk003.json.scanner;

/**
 * An interface for tokenizing a given input into valid JSON tokens
 * <p>
 * {@code Scanner}s can take JSON input in any form, whether as a
 * {@link java.util.stream.Stream}, a {@link java.io.Reader}, or
 * in another way.
 * <p>
 * {@link JSONScanner} is a default implementation of {@code Scanner},
 * and it is also used by this library to tokenize JSON inputs.
 *
 * @author Utkarsh Priyam (<a href="https://github.com/utk003" target="_top">utk003</a>)
 * @version February 2, 2021
 * @see JSONScanner
 * @see java.io.Reader
 * @see java.util.stream.Stream
 */
public interface Scanner {
    /**
     * Returns whether or not there are any more tokens to be parsed
     *
     * @return {@code true}, if there are more tokens to parse; otherwise, {@code false}
     */
    boolean hasMore();
    /**
     * Returns the number of tokens that have been returned so far
     * <p>
     * A token counts as being returned every time a new token is
     * returned by this {@code Scanner}. In other words, this value
     * should represent the number of times that {@link #advance()}
     * is called.
     *
     * @return The number of tokens that have been returned
     * @see #advance()
     */
    long tokensPassed();

    /**
     * Returns the current token associated with this {@code Scanner}
     * <p>
     * The current token for this {@code Scanner} is defined as the most
     * recent token returned by {@link #advance()}. If {@code advance()}
     * has never been called before, then the return value of this method
     * is undefined and may not be consistent across implementations.
     *
     * @return This {@code Scanner}'s current token, if one exists
     * @see #advance()
     */
    String current();
    /**
     * Advances to and returns the next token for this {@code Scanner}
     * <p>
     * If {@link #hasMore()} returns {@code false}, then the return value
     * for this method is undefined. This method can return {@code null},
     * the previous token, or even throw an exception.
     *
     * @return This {@code Scanner}'s next token, if one exists
     * @see #hasMore()
     */
    String advance();
}

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

/**
 * The "Object-Oriented JSON" package provides utilities for
 * converting JSON into custom Java objects.
 * <p>
 * Both JSON objects and arrays can be modeled with Java objects,
 * and the primitives like {@code boolean}s, {@code null}, numbers,
 * and strings can be stored either as primitives or their respective
 * Java classes. Use the {@link io.github.utk003.json.ooj.OOJParser}
 * to utilize this adaptive JSON parsing.
 * <p>
 * For traditional JSON parsing involving dedicated JSON tree nodes
 * and paths, check out the {@link io.github.utk003.json.traditional} package.
 * To translate from traditional parsing to OOJ, use
 * {@link io.github.utk003.json.ooj.OOJTranslator}.
 *
 * @see io.github.utk003.json.ooj.OOJParser
 * @see io.github.utk003.json.ooj.OOJTranslator
 * @see io.github.utk003.json.traditional
 */
package io.github.utk003.json.ooj;
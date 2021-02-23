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

import io.github.utk003.json.scanner.JSONScanner;
import io.github.utk003.json.scanner.Scanner;
import io.github.utk003.util.data.immutable.ImmutablePair;
import io.github.utk003.util.misc.Verify;

import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A parser for converting JSON into POJO classes from some form
 * of {@link Scanner} or {@code Scanner}-accepted input format.
 * <p>
 * This parser provides both recursive and non-recursive implementations
 * for parsing JSON. However, non-recursive parsing is currently not implemented,
 * and it throws an {@link UnsupportedOperationException} if called.
 * <p>
 * For traditional tree-based parsing rather than class-based OOJ parsing,
 * check out the {@link io.github.utk003.json.traditional} package.
 *
 * @author Utkarsh Priyam (<a href="https://github.com/utk003" target="_top">utk003</a>)
 * @version February 23, 2021
 * @see Scanner
 * @see io.github.utk003.json.ooj
 */
public class OOJParser {
    /**
     * Parses JSON into an object of type {@code T} recursively from the given {@link InputStream}.
     *
     * @param source The input source for the JSON
     * @param clazz  The class of the root of the JSON tree
     * @param <T>    The class type of the {@code clazz} argument and this method's return type
     * @return The parsed object of type {@code T}
     * @throws IllegalAccessException    If one arises while using Java reflection to parse the JSON
     * @throws InstantiationException    If one arises while using Java reflection to parse the JSON
     * @throws InvocationTargetException If one arises while using Java reflection to parse the JSON
     * @throws NoSuchFieldException      If one arises while using Java reflection to parse the JSON
     * @see io.github.utk003.json.traditional.JSONParser#parseRecursive(InputStream)
     */
    public <T> T parseRecursive(InputStream source, Class<T> clazz)
            throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchFieldException {
        return parseJSONRecursive(new JSONScanner(source), clazz);
    }
    /**
     * Parses JSON into an object of type {@code T} recursively from the given {@link Scanner}.
     *
     * @param scanner The input source for the JSON as a {@code Scanner}
     * @param clazz   The class of the root of the JSON tree
     * @param <T>     The class type of the {@code clazz} argument and this method's return type
     * @return The parsed object of type {@code T}
     * @throws IllegalAccessException    If one arises while using Java reflection to parse the JSON
     * @throws InstantiationException    If one arises while using Java reflection to parse the JSON
     * @throws InvocationTargetException If one arises while using Java reflection to parse the JSON
     * @throws NoSuchFieldException      If one arises while using Java reflection to parse the JSON
     * @see io.github.utk003.json.traditional.JSONParser#parseRecursive(Scanner)
     */
    public <T> T parseRecursive(Scanner scanner, Class<T> clazz)
            throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchFieldException {
        Verify.requireTrue(scanner.tokensPassed() > 0);
        Verify.requireTrue(scanner.hasMore());
        return parseJSONRecursive(scanner, clazz);
    }

    /**
     * Parses JSON into an object of type {@code T} non-recursively from the given {@link InputStream}.
     *
     * @param source The input source for the JSON
     * @param clazz  The class of the root of the JSON tree
     * @param <T>    The class type of the {@code clazz} argument and this method's return type
     * @return The parsed object of type {@code T}
     * @throws IllegalAccessException    If one arises while using Java reflection to parse the JSON
     * @throws InstantiationException    If one arises while using Java reflection to parse the JSON
     * @throws InvocationTargetException If one arises while using Java reflection to parse the JSON
     * @throws NoSuchFieldException      If one arises while using Java reflection to parse the JSON
     * @see io.github.utk003.json.traditional.JSONParser#parseNonRecursive(InputStream)
     */
    public <T> T parseNonRecursive(InputStream source, Class<T> clazz)
            throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchFieldException {
        return parseJSONNonRecursive(new JSONScanner(source), clazz);
    }
    /**
     * Parses JSON into an object of type {@code T} non-recursively from the given {@link Scanner}.
     *
     * @param scanner The input source for the JSON as a {@code Scanner}
     * @param clazz   The class of the root of the JSON tree
     * @param <T>     The class type of the {@code clazz} argument and this method's return type
     * @return The parsed object of type {@code T}
     * @throws IllegalAccessException    If one arises while using Java reflection to parse the JSON
     * @throws InstantiationException    If one arises while using Java reflection to parse the JSON
     * @throws InvocationTargetException If one arises while using Java reflection to parse the JSON
     * @throws NoSuchFieldException      If one arises while using Java reflection to parse the JSON
     * @see io.github.utk003.json.traditional.JSONParser#parseNonRecursive(Scanner)
     */
    public <T> T parseNonRecursive(Scanner scanner, Class<T> clazz)
            throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchFieldException {
        Verify.requireTrue(scanner.tokensPassed() > 0);
        Verify.requireTrue(scanner.hasMore());
        return parseJSONNonRecursive(scanner, clazz);
    }

    private final Map<Class<?>, ImmutablePair<Method, Class<?>[]>> TRANSFORMATION_MAP = new HashMap<>();
    /**
     * Stores the specified JSON-array-to-class transformer.
     * <p>
     * A transformer consists of the target class and the details
     * necessary to locate the transformer method during runtime.
     *
     * @param clazz                    The target class
     * @param methodHolder             The class where the transformer method is defined
     * @param methodName               The method id for the transformer method
     * @param transformationParamTypes An array of the class types of the arguments of the transformer method
     * @throws NoSuchMethodException If the specified transformer method cannot be found
     */
    public void storeArrayTransformer(Class<?> clazz, Class<?> methodHolder, String methodName,
                                      Class<?>... transformationParamTypes) throws NoSuchMethodException {
        Method method = methodHolder.getDeclaredMethod(methodName, transformationParamTypes);
        TRANSFORMATION_MAP.put(clazz, new ImmutablePair<>(method, transformationParamTypes));
    }
    /**
     * Removes the specified JSON-array-to-class transformer.
     *
     * @param clazz The target class of the transformer to remove
     * @see #storeArrayTransformer(Class, Class, String, Class[])
     */
    public void removeArrayTransformer(Class<?> clazz) {
        TRANSFORMATION_MAP.remove(clazz);
    }

    // --------------------------------------------------------------------------------------------- //
    // --------------------------------------------------------------------------------------------- //
    // --------------------------------------------------------------------------------------------- //
    // --------------------------------------------------------------------------------------------- //
    // --------------------------------------------------------------------------------------------- //

    /**
     * Parses JSON into an object of type {@code T} recursively.
     *
     * @param scanner The input source as a {@link Scanner}
     * @param clazz   The class type of the resulting object
     * @param <T>     The class type of the {@code clazz} argument and this method's return type
     * @return The OOJ form of the input JSON
     * @throws IllegalAccessException    If one arises while using Java reflection to parse the JSON
     * @throws InstantiationException    If one arises while using Java reflection to parse the JSON
     * @throws InvocationTargetException If one arises while using Java reflection to parse the JSON
     * @throws NoSuchFieldException      If one arises while using Java reflection to parse the JSON
     */
    @SuppressWarnings("unchecked")
    private <T> T parseJSONRecursive(Scanner scanner, Class<T> clazz)
            throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchFieldException {
        String curr;
        switch (curr = scanner.current()) {
            case "{":
                return parseObject(scanner, clazz);

            case "[":
                return parseArray(scanner, clazz);

            case "null":
                return null;

            default:
                if (clazz == String.class)
                    return (T) checkAndTrimString(curr);

                if (clazz == Integer.class || clazz == int.class)
                    return (T) (Integer) Integer.parseInt(curr);
                if (clazz == Long.class || clazz == long.class)
                    return (T) (Long) Long.parseLong(curr);

                if (clazz == Double.class || clazz == double.class)
                    return (T) (Double) Double.parseDouble(curr);
                if (clazz == Float.class || clazz == float.class)
                    return (T) (Float) Float.parseFloat(curr);

                if (clazz == Boolean.class || clazz == boolean.class)
                    switch (curr) {
                        case "true":
                            return (T) (Boolean) true;
                        case "false":
                            return (T) (Boolean) false;

                        default:
                            throw new IllegalStateException("Invalid boolean value");
                    }

                if (clazz == Byte.class || clazz == byte.class)
                    return (T) (Byte) Byte.parseByte(curr);
                if (clazz == Short.class || clazz == short.class)
                    return (T) (Short) Short.parseShort(curr);

                throw new IllegalStateException("Illegal JSON value");
        }
    }

    /**
     * Verifies that the given string starts and ends with an escaped quote ({@code "})
     * and strips off those quotes, returning the raw text stored within the string.
     *
     * @param str The original string
     * @return The verified and stripped string
     */
    private static String checkAndTrimString(String str) {
        int len = str.length();
        Verify.requireTrue(str.charAt(0) == '"');
        Verify.requireTrue(str.charAt(len - 1) == '"');
        return str.substring(1, len - 1);
    }

    /**
     * Parses a JSON object of type {@code T} recursively.
     *
     * @param scanner The input source as a {@link Scanner}
     * @param clazz   The class type of the resulting object
     * @param <T>     The class type of the {@code clazz} argument and this method's return type
     * @return The OOJ form of the JSON object in the input JSON
     * @throws IllegalAccessException    If one arises while using Java reflection to parse the JSON
     * @throws InstantiationException    If one arises while using Java reflection to parse the JSON
     * @throws InvocationTargetException If one arises while using Java reflection to parse the JSON
     * @throws NoSuchFieldException      If one arises while using Java reflection to parse the JSON
     */
    private <T> T parseObject(Scanner scanner, Class<T> clazz)
            throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchFieldException {
        Verify.requireTrue(scanner.current().equals("{"));
        T instance = clazz.newInstance();

        boolean continueLooping;
        do {
            String key = checkAndTrimString(scanner.advance());
            Verify.requireTrue(scanner.advance().equals(":"));
            scanner.advance(); // load first token of element

            Field field = clazz.getDeclaredField(key);
            field.set(instance, parseJSONRecursive(scanner, field.getType()));

            continueLooping = scanner.advance().equals(",");
        } while (continueLooping);

        Verify.requireTrue(scanner.current().equals("}"));
        return instance;
    }

    /**
     * Parses a JSON array of type {@code T} recursively.
     * <p>
     * This helper method makes use of the transformer methods in {@link #TRANSFORMATION_MAP}
     * in order to more accurately translate JSON arrays into POJOs. Additionally,
     * {@link OOJArray}s can be use to aid this {@code OOJParser} in correctly identifying
     * how to translate the input JSON array into a POJO.
     *
     * @param scanner The input source as a {@link Scanner}
     * @param clazz   The class type of the resulting object
     * @param <T>     The class type of the {@code clazz} argument and this method's return type
     * @return The OOJ form of the JSON array in the input JSON
     * @throws IllegalAccessException    If one arises while using Java reflection to parse the JSON
     * @throws InstantiationException    If one arises while using Java reflection to parse the JSON
     * @throws InvocationTargetException If one arises while using Java reflection to parse the JSON
     * @throws NoSuchFieldException      If one arises while using Java reflection to parse the JSON
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private <T> T parseArray(Scanner scanner, Class<T> clazz)
            throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchFieldException {
        Verify.requireTrue(scanner.current().equals("["));
        T instance;

        if (OOJArray.class.isAssignableFrom(clazz)) {
            instance = clazz.newInstance();
            int index = 0;

            boolean continueLooping;
            do {
                scanner.advance(); // load first token of element

                Field field = clazz.getDeclaredField("element" + index++);
                field.set(instance, parseJSONRecursive(scanner, field.getType()));

                continueLooping = scanner.advance().equals(",");
            } while (continueLooping);
        } else if (clazz.isArray()) {
            Class<?> arrayType = clazz.getComponentType();
            List list = new LinkedList();

            boolean continueLooping;
            do {
                scanner.advance(); // load first token of element
                list.add(parseJSONRecursive(scanner, arrayType));
                continueLooping = scanner.advance().equals(",");
            } while (continueLooping);

            instance = (T) Array.newInstance(arrayType, list.size());
            int index = 0;
            for (Object o : list)
                Array.set(instance, index++, o);
        } else {
            ImmutablePair<Method, Class<?>[]> transformation = TRANSFORMATION_MAP.get(clazz);
            Method transformer = transformation.FIRST;
            Object[] params = new Object[transformation.SECOND.length];

            int index = 0;
            boolean continueLooping;
            do {
                scanner.advance(); // load first token of element

                params[index] = parseJSONRecursive(scanner, transformation.SECOND[index]);
                index++;

                continueLooping = scanner.advance().equals(",");
            } while (continueLooping);

            if (transformer.isAccessible())
                instance = (T) transformer.invoke(null, params);
            else {
                transformer.setAccessible(true);
                instance = (T) transformer.invoke(null, params);
                transformer.setAccessible(false);
            }
        }

        Verify.requireTrue(scanner.current().equals("]"));
        return instance;
    }

    // --------------------------------------------------------------------------------------------- //
    // --------------------------------------------------------------------------------------------- //
    // --------------------------------------------------------------------------------------------- //
    // --------------------------------------------------------------------------------------------- //
    // --------------------------------------------------------------------------------------------- //

    /**
     * Parses JSON into an object of type {@code T} non-recursively.
     * <p>
     * This is currently unimplemented, instead immediately
     * throwing an {@link UnsupportedOperationException}.
     *
     * @param scanner The input source as a {@link Scanner}
     * @param clazz   The class type of the resulting object
     * @param <T>     The class type of the {@code clazz} argument and this method's return type
     * @return The OOJ form of the input JSON
     * @throws IllegalAccessException        If one arises while using Java reflection to parse the JSON
     * @throws InstantiationException        If one arises while using Java reflection to parse the JSON
     * @throws InvocationTargetException     If one arises while using Java reflection to parse the JSON
     * @throws NoSuchFieldException          If one arises while using Java reflection to parse the JSON
     * @throws UnsupportedOperationException Always
     */
    private <T> T parseJSONNonRecursive(Scanner scanner, Class<T> clazz)
            throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchFieldException {
        throw new UnsupportedOperationException("OOJParser.parseJSONNonRecursive(...) is currently not implemented");
    }
}

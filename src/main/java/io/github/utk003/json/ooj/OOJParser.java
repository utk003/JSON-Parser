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
import io.github.utk003.util.data.immutable.ImmutableTriple;
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

public class OOJParser {
    public <T> T parseRecursive(InputStream source, Class<T> clazz)
            throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchFieldException, NoSuchMethodException {
        return parseJSONRecursive(new JSONScanner(source), clazz);
    }
    public <T> T parseRecursive(Scanner scanner, Class<T> clazz)
            throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchFieldException, NoSuchMethodException {
        if (scanner.tokensPassed() == 0) scanner.advance();
        Verify.requireTrue(scanner.tokensPassed() == 1);
        return parseJSONRecursive(scanner, clazz);
    }

    public <T> T parseNonRecursive(InputStream source, Class<T> clazz)
            throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchFieldException, NoSuchMethodException {
        return parseJSONNonRecursive(new JSONScanner(source), clazz);
    }
    public <T> T parseNonRecursive(Scanner scanner, Class<T> clazz)
            throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchFieldException, NoSuchMethodException {
        if (scanner.tokensPassed() == 0) scanner.advance();
        Verify.requireTrue(scanner.tokensPassed() == 1);
        return parseJSONNonRecursive(scanner, clazz);
    }

    private final Map<Class<?>, ImmutableTriple<Class<?>, String, Class<?>[]>> TRANSFORMATION_MAP = new HashMap<>();
    public <T> void storeArrayTransformer(Class<T> clazz, Class<?> methodHolder, String methodName, Class<?>... transformationParamTypes) {
        TRANSFORMATION_MAP.put(clazz, new ImmutableTriple<>(methodHolder, methodName, transformationParamTypes));
    }
    public void removeArrayTransformer(Class<?> clazz) {
        TRANSFORMATION_MAP.remove(clazz);
    }

    // --------------------------------------------------------------------------------------------- //
    // --------------------------------------------------------------------------------------------- //
    // --------------------------------------------------------------------------------------------- //
    // --------------------------------------------------------------------------------------------- //
    // --------------------------------------------------------------------------------------------- //

    @SuppressWarnings("unchecked")
    private <T> T parseJSONRecursive(Scanner scanner, Class<T> clazz)
            throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchFieldException, NoSuchMethodException {
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

    private static String checkAndTrimString(String str) {
        int len = str.length();
        Verify.requireTrue(str.charAt(0) == '"');
        Verify.requireTrue(str.charAt(len - 1) == '"');
        return str.substring(1, len - 1);
    }

    private <T> T parseObject(Scanner scanner, Class<T> clazz)
            throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchFieldException, NoSuchMethodException {
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

    @SuppressWarnings({"unchecked", "rawtypes"})
    private <T> T parseArray(Scanner scanner, Class<T> clazz)
            throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchFieldException, NoSuchMethodException {
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
            ImmutableTriple<Class<?>, String, Class<?>[]> transformation = TRANSFORMATION_MAP.get(clazz);
            Method transformer = transformation.FIRST.getDeclaredMethod(transformation.SECOND, transformation.THIRD);
            Object[] params = new Object[transformation.THIRD.length];

            int index = 0;
            boolean continueLooping;
            do {
                scanner.advance(); // load first token of element

                params[index] = parseJSONRecursive(scanner, transformation.THIRD[index]);
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

    private <T> T parseJSONNonRecursive(Scanner scanner, Class<T> clazz)
            throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchFieldException, NoSuchMethodException {
        throw new UnsupportedOperationException("OOJParser.parseJSONNonRecursive(...) is currently not implemented");
    }
}

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
import io.github.utk003.util.misc.Verifier;

import java.io.InputStream;
import java.lang.reflect.*;
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
        if (scanner.tokensPassed() == 0) scanner.advance();
        Verifier.requireTrue(scanner.hasMore(), "The given scanner cannot be empty");
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
        if (scanner.tokensPassed() == 0) scanner.advance();
        Verifier.requireTrue(scanner.hasMore(), "The given scanner cannot be empty");
        return parseJSONNonRecursive(scanner, clazz);
    }

    private final Map<Class<?>, ImmutableTriple<Executable, Class<?>[], Boolean>> ARRAY_TRANSFORMATION_MAP = new HashMap<>();
    private final Map<Class<?>, ImmutableTriple<Executable, Map<String, Class<?>>, String[]>> OBJECT_TRANSFORMATION_MAP = new HashMap<>();

    /**
     * Stores the specified JSON-array-to-class transformer.
     * <p>
     * A transformer consists of the target class and the details
     * necessary to locate the transformer method.
     * <p>
     * If the provided method name (the third argument {@code methodName})
     * is {@code null}, then the equality of the first and second class
     * arguments will be verified, and that class's constructor will be
     * used instead of a transformation method.
     * <p>
     * The {@code giveAsSingleArray} argument (the final one) tells the
     * parser how to provide the final values for the elements of the array
     * to the transformation method. If this boolean is {@code true}, then
     * the values will be provided as an array of {@code Object}s. Otherwise,
     * the values will be passed in as unique arguments in the exact order
     * prescribed by the second-last argument {@code arrayElementTypes},
     * which is an array of {@code Class<?>} objects.
     *
     * @param clazz             The target class (to create)
     * @param methodHolder      The class where the transformer method is defined
     * @param methodName        The method id for the transformer method
     * @param arrayElementTypes An array of the class types for the transformation
     * @param giveAsSingleArray A boolean flag that specifies whether the transformation
     *                          method takes its arguments as an array of {@code Object}s
     *                          or as individual arguments
     * @throws Verifier.VerificationException If the method name is {@code null} and the created
     *                                        class type does not equal the method holder class type
     * @throws NoSuchMethodException          If the specified transformer method or constructor cannot be found
     */
    public void storeArrayTransformer(Class<?> clazz, Class<?> methodHolder, String methodName,
                                      Class<?>[] arrayElementTypes, boolean giveAsSingleArray) throws NoSuchMethodException {
        Executable executable;
        if (methodName == null) {
            Verifier.requireExactMatch(clazz, methodHolder, "Created class and constructor holder do not match");
            if (giveAsSingleArray)
                executable = methodHolder.getDeclaredConstructor(Object[].class);
            else
                executable = methodHolder.getDeclaredConstructor(arrayElementTypes);
        } else {
            if (giveAsSingleArray)
                executable = methodHolder.getDeclaredMethod(methodName, Object[].class);
            else
                executable = methodHolder.getDeclaredMethod(methodName, arrayElementTypes);
        }
        ARRAY_TRANSFORMATION_MAP.put(clazz, new ImmutableTriple<>(executable, arrayElementTypes, giveAsSingleArray));
    }
    /**
     * Stores the specified JSON-array-to-class constructor transformer.
     * <p>
     * This method exists solely to provide a simpler way to use constructors
     * as the array transformation method.
     * <p>
     * For details on how array transformers work, check out
     * {@link #storeArrayTransformer(Class, Class, String, Class[], boolean)}.
     *
     * @param clazz             The target class (to create)
     * @param arrayElementTypes An array of the class types for the transformation
     * @param giveAsSingleArray A boolean flag that specifies whether the transformation
     *                          method takes its arguments as an array of {@code Object}s
     *                          or as individual arguments
     * @throws NoSuchMethodException If the specified transformer constructor cannot be found
     * @see #storeArrayTransformer(Class, Class, String, Class[], boolean)
     */
    public void storeArrayTransformerConstructor(Class<?> clazz, Class<?>[] arrayElementTypes,
                                                 boolean giveAsSingleArray) throws NoSuchMethodException {
        storeArrayTransformer(clazz, clazz, null, arrayElementTypes, giveAsSingleArray);
    }
    /**
     * Stores the specified JSON-object-to-class transformer.
     * <p>
     * A transformer consists of the target class and the details
     * necessary to locate the transformer method.
     * <p>
     * The {@code args} argument (the final one) tells the parser how
     * to provide the final values for the fields to the transformation
     * method. If this array is {@code null}, the values will be provided
     * in a single map declared as {@code Map<String, Object>}. Otherwise,
     * the values will be provided as {@code N} unique arguments, where
     * {@code N} is the length of the given array {@code args}. The specific
     * value of each argument will be the parsed value of the field whose
     * identifier corresponds to the {@code String} at that exact index
     * in the given array {@code args}.
     *
     * @param clazz            The target class (to create)
     * @param methodHolder     The class where the transformer method is defined
     * @param methodName       The method id for the transformer method
     * @param jsonObjectFields All of the field identifiers and class types for the transformation
     * @param args             A (possibly {@code null}) array of {@code String}s delineating
     *                         the precise argument order for the transformation method
     * @throws Verifier.VerificationException If the method name is {@code null} and the created
     *                                        class type does not equal the method holder class type
     * @throws NoSuchMethodException          If the specified transformer method or constructor cannot be found
     */
    public void storeObjectTransformer(Class<?> clazz, Class<?> methodHolder, String methodName,
                                       Map<String, Class<?>> jsonObjectFields, String[] args) throws NoSuchMethodException {
        if (methodName == null)
            Verifier.requireExactMatch(clazz, methodHolder, "Created class and constructor holder do not match");

        Executable executable;
        if (args == null) {
            if (methodName == null)
                executable = methodHolder.getDeclaredConstructor(Map.class);
            else
                executable = methodHolder.getDeclaredMethod(methodName, Map.class);
        } else {
            Class<?>[] argTypes = new Class[args.length];
            for (int i = 0; i < args.length; i++)
                argTypes[i] = jsonObjectFields.get(args[i]);

            if (methodName == null)
                executable = methodHolder.getDeclaredConstructor(argTypes);
            else
                executable = methodHolder.getDeclaredMethod(methodName, argTypes);
        }
        OBJECT_TRANSFORMATION_MAP.put(clazz, new ImmutableTriple<>(executable, jsonObjectFields, args));
    }
    /**
     * Stores the specified JSON-object-to-class constructor transformer.
     * <p>
     * This method exists solely to provide a simpler way to use constructors
     * as the object transformation method.
     * <p>
     * For details on how object transformers work, check out
     * {@link #storeObjectTransformer(Class, Class, String, Map, String[])}.
     *
     * @param clazz            The target class (to create)
     * @param jsonObjectFields All of the field identifiers and class types for the transformation
     * @param args             A (possibly {@code null}) array of {@code String}s delineating
     *                         the precise argument order for the transformation method
     * @throws NoSuchMethodException If the specified transformer method or constructor cannot be found
     */
    public void storeObjectTransformerConstructor(Class<?> clazz, Map<String, Class<?>> jsonObjectFields,
                                                  String[] args) throws NoSuchMethodException {
        storeObjectTransformer(clazz, clazz, null, jsonObjectFields, args);
    }

    // --------------------------------------------------------------------------------------------- //
    // --------------------------------------------------------------------------------------------- //
    // --------------------------------------------------------------------------------------------- //
    // --------------------------------------------------------------------------------------------- //
    // --------------------------------------------------------------------------------------------- //

    /**
     * Verifies that the given string starts and ends with an escaped quote ({@code "})
     * and strips off those quotes, returning the raw text stored within the string.
     *
     * @param str The original string
     * @return The verified and stripped string
     */
    private static String checkAndTrimString(String str) {
        int len = str.length();
        Verifier.requireMatch(str.charAt(0), '"', "Malformed JSON String: should begin with a quote (\")");
        Verifier.requireMatch(str.charAt(len - 1), '"', "Malformed JSON String: should end with a quote (\")");
        return str.substring(1, len - 1);
    }

    /**
     * Invokes the specified method or constructor with the given arguments.
     *
     * @param executable The method or constructor to invoke
     * @param args       The invocation arguments
     * @param <T>        The return type of the method
     * @return The return value of the invocation
     * @throws IllegalAccessException    If one arises while executing the method or constructor
     * @throws InstantiationException    If one arises while executing the method or constructor
     * @throws InvocationTargetException If one arises while executing the method or constructor
     * @see #executeMethod(Executable, Object[])
     * @see Constructor#newInstance(Object...)
     * @see Method#invoke(Object, Object...)
     */
    private static <T> T invokeTransformationMethod(Executable executable, Object... args)
            throws IllegalAccessException, InstantiationException, InvocationTargetException {
        T returnVal;
        if (executable.isAccessible())
            returnVal = executeMethod(executable, args);
        else {
            executable.setAccessible(true);
            returnVal = executeMethod(executable, args);
            executable.setAccessible(false);
        }
        return returnVal;
    }
    /**
     * Actually invokes the given executable with the given arguments.
     * <p>
     * This is a helper method for {@link #invokeTransformationMethod(Executable, Object...)}.
     *
     * @param executable The method or constructor to invoke
     * @param args       The invocation arguments
     * @param <T>        The return type of the method
     * @return The return value of the invocation
     * @throws IllegalAccessException    If one arises while executing the method or constructor
     * @throws InstantiationException    If one arises while executing the method or constructor
     * @throws InvocationTargetException If one arises while executing the method or constructor
     * @see #invokeTransformationMethod(Executable, Object...)
     * @see Constructor#newInstance(Object...)
     * @see Method#invoke(Object, Object...)
     */
    @SuppressWarnings("unchecked")
    private static <T> T executeMethod(Executable executable, Object[] args)
            throws IllegalAccessException, InstantiationException, InvocationTargetException {
        return executable instanceof Method ?
                (T) ((Method) executable).invoke(null, args) :
                (T) ((Constructor<?>) executable).newInstance(args);
    }

    /**
     * Sets the value of the given field for the given object instance to the given value.
     *
     * @param field    The field whose value needs to be set
     * @param instance The instance for which the field should be set
     * @param value    The new value of the field
     * @throws IllegalAccessException If one arises while setting the new value to the field
     * @see Field#set(Object, Object)
     */
    private static void setField(Field field, Object instance, Object value) throws IllegalAccessException {
        if (field.isAccessible())
            field.set(instance, value);
        else {
            field.setAccessible(true);
            field.set(instance, value);
            field.setAccessible(false);
        }
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
     * Parses a JSON object of type {@code T} recursively.
     * <p>
     * This method prioritizes custom transformation methods defined via the
     * {@link #storeObjectTransformer(Class, Class, String, Map, String[])} method. If
     * no such transformation exists, then this method instead simply locates
     * a field with the corresponding identifier and sets its value to the
     * value represented by the JSON child.
     *
     * @param scanner The input source as a {@link Scanner}
     * @param clazz   The class type of the resulting object
     * @param <T>     The class type of the {@code clazz} argument and this method's return type
     * @return The OOJ form of the JSON object in the input JSON
     * @throws IllegalAccessException    If one arises while using Java reflection to parse the JSON
     * @throws InstantiationException    If one arises while using Java reflection to parse the JSON
     * @throws InvocationTargetException If one arises while using Java reflection to parse the JSON
     * @throws NoSuchFieldException      If one arises while using Java reflection to parse the JSON
     * @see #storeObjectTransformer(Class, Class, String, Map, String[])
     */
    private <T> T parseObject(Scanner scanner, Class<T> clazz)
            throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchFieldException {
        Verifier.requireMatch(scanner.current(), "{", "Malformed JSON Object: should start with a curly brace ({)");
        T instance;

        ImmutableTriple<Executable, Map<String, Class<?>>, String[]> transformation = OBJECT_TRANSFORMATION_MAP.get(clazz);
        if (transformation != null) {
            Map<String, Object> fieldValuesMap = new HashMap<>(transformation.SECOND.size());

            boolean continueLooping;
            do {
                String key = checkAndTrimString(scanner.advance());
                Verifier.requireMatch(scanner.advance(), ":", "Malformed JSON Object: key should be followed by a colon (:)");
                scanner.advance(); // load first token of element

                fieldValuesMap.put(key, parseJSONRecursive(scanner, transformation.SECOND.get(key)));

                continueLooping = scanner.advance().equals(",");
            } while (continueLooping);

            if (transformation.THIRD == null)
                // invoke the method with all arguments given in a single map
                instance = invokeTransformationMethod(transformation.FIRST, fieldValuesMap);
            else {
                int len = transformation.THIRD.length;
                Object[] arguments = new Object[len];
                for (int i = 0; i < len; i++)
                    arguments[i] = fieldValuesMap.get(transformation.THIRD[i]);

                // invoke the method with all arguments given separately
                instance = invokeTransformationMethod(transformation.FIRST, arguments);
            }
        } else {
            instance = clazz.newInstance();

            boolean continueLooping;
            do {
                String key = checkAndTrimString(scanner.advance());
                Verifier.requireMatch(scanner.advance(), ":", "Malformed JSON Object: key should be followed by a colon (:)");
                scanner.advance(); // load first token of element

                Field field = clazz.getDeclaredField(key);
                setField(field, instance, parseJSONRecursive(scanner, field.getType()));

                continueLooping = scanner.advance().equals(",");
            } while (continueLooping);
        }

        Verifier.requireMatch(scanner.current(), "}", "Malformed JSON Object: should end with a curly brace (})");
        return instance;
    }

    /**
     * Parses a JSON array of type {@code T} recursively.
     * <p>
     * This method prioritizes custom transformation methods defined via the
     * {@link #storeArrayTransformer(Class, Class, String, Class[], boolean)} method. If
     * no such transformation exists, then this method attempts to translate the
     * JSON into POJOs via the {@link OOJArray} class. If the target class does
     * not extend {@code OOJArray}, then this method assumes the target class is an
     * array and translates the JSON by creating elements of the array's stored type.
     *
     * @param scanner The input source as a {@link Scanner}
     * @param clazz   The class type of the resulting object
     * @param <T>     The class type of the {@code clazz} argument and this method's return type
     * @return The OOJ form of the JSON array in the input JSON
     * @throws IllegalAccessException    If one arises while using Java reflection to parse the JSON
     * @throws InstantiationException    If one arises while using Java reflection to parse the JSON
     * @throws InvocationTargetException If one arises while using Java reflection to parse the JSON
     * @throws NoSuchFieldException      If one arises while using Java reflection to parse the JSON
     * @see #storeArrayTransformer(Class, Class, String, Class[], boolean)
     * @see OOJArray
     */
    private <T> T parseArray(Scanner scanner, Class<T> clazz)
            throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchFieldException {
        Verifier.requireMatch(scanner.current(), "[", "Malformed JSON Array: should start with a curly brace ([)");
        T instance;

        ImmutableTriple<Executable, Class<?>[], Boolean> transformation = ARRAY_TRANSFORMATION_MAP.get(clazz);
        if (transformation != null) {
            Object[] args = new Object[transformation.SECOND.length];

            int index = 0;
            boolean continueLooping;
            do {
                scanner.advance(); // load first token of element

                args[index] = parseJSONRecursive(scanner, transformation.SECOND[index]);
                index++;

                continueLooping = scanner.advance().equals(",");
            } while (continueLooping);

            instance = transformation.THIRD ?
                    // if transformation.THIRD, give arguments as a single array
                    invokeTransformationMethod(transformation.FIRST, (Object) args) :
                    // if not, give arguments individually
                    invokeTransformationMethod(transformation.FIRST, args);
        } else if (OOJArray.class.isAssignableFrom(clazz)) {
            instance = clazz.newInstance();

            int index = 0;
            boolean continueLooping;
            do {
                scanner.advance(); // load first token of element

                Field field = clazz.getDeclaredField("element" + index++);
                setField(field, instance, parseJSONRecursive(scanner, field.getType()));

                continueLooping = scanner.advance().equals(",");
            } while (continueLooping);
        } else {
            Verifier.requireTrue(clazz.isArray(), "Unable to locate JSON Array transformation for " + clazz);
            Class<?> arrayType = clazz.getComponentType();
            List<Object> list = new LinkedList<>();

            boolean continueLooping;
            do {
                scanner.advance(); // load first token of element
                list.add(parseJSONRecursive(scanner, arrayType));
                continueLooping = scanner.advance().equals(",");
            } while (continueLooping);

            //noinspection unchecked
            instance = (T) Array.newInstance(arrayType, list.size());
            int index = 0;
            for (Object o : list)
                Array.set(instance, index++, o);
        }

        Verifier.requireMatch(scanner.current(), "]", "Malformed JSON Array: should end with a curly brace (])");
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

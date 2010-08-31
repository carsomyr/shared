/**
 * <p>
 * Copyright (c) 2008 Roy Liu<br>
 * All rights reserved.
 * </p>
 * <p>
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * </p>
 * <ul>
 * <li>Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.</li>
 * <li>Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution.</li>
 * <li>Neither the name of the author nor the names of any contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.</li>
 * </ul>
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * </p>
 */

package shared.util;

import java.lang.reflect.Array;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A static utility class for manipulating arrays.
 * 
 * @author Roy Liu
 */
public class Arrays {

    /**
     * Creates a new array initialized to the given value.
     */
    final public static int[] newArray(int length, int value) {

        int[] res = new int[length];
        java.util.Arrays.fill(res, value);

        return res;
    }

    /**
     * Creates a new array initialized to the given value.
     */
    final public static double[] newArray(int length, double value) {

        double[] res = new double[length];
        java.util.Arrays.fill(res, value);

        return res;
    }

    /**
     * Creates a new array initialized to the given value.
     */
    final public static Object[] newArray(int length, Object value) {

        Object[] res = new Object[length];
        java.util.Arrays.fill(res, value);

        return res;
    }

    /**
     * Creates a new array initialized to the given value.
     * 
     * @param <T>
     *            the component type.
     */
    @SuppressWarnings("unchecked")
    final public static <T> T[] newArray(Class<? extends T> clazz, int length, T value) {

        T[] res = (T[]) Array.newInstance(clazz, length);
        java.util.Arrays.fill(res, value);

        return res;
    }

    /**
     * A convenience array slicing method.
     * 
     * @param values
     *            the values.
     * @param indices
     *            the slice indices.
     * @return the sliced values.
     */
    final public static double[] slice(double[] values, int[] indices) {

        int len = indices.length;

        double[] res = new double[len];

        for (int i = 0; i < len; i++) {
            res[i] = values[indices[i]];
        }

        return res;
    }

    /**
     * A convenience array slicing method.
     * 
     * @param values
     *            the values.
     * @param indices
     *            the slice indices.
     * @return the sliced values.
     */
    final public static int[] slice(int[] values, int[] indices) {

        int len = indices.length;

        int[] res = new int[len];

        for (int i = 0; i < len; i++) {
            res[i] = values[indices[i]];
        }

        return res;
    }

    /**
     * A convenience array slicing method.
     * 
     * @param values
     *            the values.
     * @param indices
     *            the slice indices.
     * @param clazz
     *            the component class.
     * @param <T>
     *            the component type.
     * @return the sliced values.
     */
    @SuppressWarnings("unchecked")
    final public static <T> T[] slice(T[] values, int[] indices, Class<? extends T> clazz) {

        int len = indices.length;

        T[] res = (T[]) Array.newInstance(clazz, len);

        for (int i = 0; i < len; i++) {
            res[i] = values[indices[i]];
        }

        return res;
    }

    /**
     * Boxes the given array of {@code double}s.
     * 
     * @param values
     *            the values.
     * @return the boxed values.
     */
    final public static Double[] box(double[] values) {

        int len = values.length;

        Double[] res = new Double[len];

        for (int i = 0; i < len; i++) {
            res[i] = values[i];
        }

        return res;
    }

    /**
     * Boxes the given array of {@code int}s.
     * 
     * @param values
     *            the values.
     * @return the boxed values.
     */
    final public static Integer[] box(int[] values) {

        int len = values.length;

        Integer[] res = new Integer[len];

        for (int i = 0; i < len; i++) {
            res[i] = values[i];
        }

        return res;
    }

    /**
     * Unboxes the given array of {@link Double}s.
     * 
     * @param values
     *            the values.
     * @return the unboxed values.
     */
    final public static double[] unbox(Double[] values) {

        int len = values.length;

        double[] res = new double[len];

        for (int i = 0; i < len; i++) {
            res[i] = values[i];
        }

        return res;
    }

    /**
     * Unboxes the given array of {@link Integer}s.
     * 
     * @param values
     *            the values.
     * @return the unboxed values.
     */
    final public static int[] unbox(Integer[] values) {

        int len = values.length;

        int[] res = new int[len];

        for (int i = 0; i < len; i++) {
            res[i] = values[i];
        }

        return res;
    }

    /**
     * Wraps the given value and trailing values as an array.
     * 
     * @param <T>
     *            the component type.
     * @param value
     *            the head value.
     * @param values
     *            the trailing values.
     * @return the array.
     */
    @SuppressWarnings("unchecked")
    final public static <T> T[] wrap(Class<T> clazz, T value, T... values) {

        T[] res = (T[]) Array.newInstance(clazz, 1 + values.length);

        res[0] = value;

        System.arraycopy(values, 0, res, 1, values.length);

        return res;
    }

    /**
     * A facade for {@link #wrap(Class, Object, Object...)}.
     */
    @SuppressWarnings("unchecked")
    final public static <T> T[] wrap(T value, T... values) {
        return wrap((Class<T>) value.getClass(), value, values);
    }

    /**
     * Compares two arrays of {@code double}s on the basis of lexicographic order.
     * 
     * @param lhs
     *            the left hand side.
     * @param rhs
     *            the right hand side.
     */
    final public static int compare(double[] lhs, double[] rhs) {

        for (int i = 0, n = Math.min(lhs.length, rhs.length); i < n; i++) {

            int cmp = Double.compare(lhs[i], rhs[i]);

            if (cmp != 0) {
                return cmp;
            }
        }

        return lhs.length - rhs.length;
    }

    /**
     * Compares two arrays of {@code int}s on the basis of lexicographic order.
     * 
     * @param lhs
     *            the left hand side.
     * @param rhs
     *            the right hand side.
     */
    final public static int compare(int[] lhs, int[] rhs) {

        for (int i = 0, n = Math.min(lhs.length, rhs.length); i < n; i++) {

            int cmp = lhs[i] - rhs[i];

            if (cmp != 0) {
                return cmp;
            }
        }

        return lhs.length - rhs.length;
    }

    /**
     * Compares two arrays of objects on the basis of lexicographic order.
     * 
     * @param lhs
     *            the left hand side.
     * @param rhs
     *            the right hand side.
     * @param <T>
     *            the component type.
     */
    final public static <T extends Comparable<? super T>> int compare(T[] lhs, T[] rhs) {

        for (int i = 0, n = Math.min(lhs.length, rhs.length); i < n; i++) {

            int cmp = lhs[i].compareTo(rhs[i]);

            if (cmp != 0) {
                return cmp;
            }
        }

        return lhs.length - rhs.length;
    }

    /**
     * Compares two arrays of objects on the basis of lexicographic order.
     * 
     * @param lhs
     *            the left hand side.
     * @param rhs
     *            the right hand side.
     * @param c
     *            the {@link Comparator} to use.
     * @param <T>
     *            the component type.
     */
    final public static <T> int compare(T[] lhs, T[] rhs, Comparator<? super T> c) {

        for (int i = 0, n = Math.min(lhs.length, rhs.length); i < n; i++) {

            int cmp = c.compare(lhs[i], rhs[i]);

            if (cmp != 0) {
                return cmp;
            }
        }

        return lhs.length - rhs.length;
    }

    /**
     * A variant of {@link java.util.Arrays#binarySearch(int[], int, int, int)} that finds the array index with value
     * nearest to the given key, as determined by the provided {@link RoundingMode}.
     * 
     * @param values
     *            the values.
     * @param from
     *            the search start index.
     * @param to
     *            the search end index.
     * @param key
     *            the key.
     * @param rm
     *            the {@link RoundingMode}.
     * @return the nearest index.
     */
    final public static int binarySearchNearest(int[] values, int from, int to, int key, RoundingMode rm) {

        Control.checkTrue(values.length > 0, //
                "Array size must be positive");

        int index = java.util.Arrays.binarySearch(values, from, to, key);

        if (index >= 0) {

            return index;

        } else {

            index = -index - 1;

            int lower = Math.max(index - 1, 0);
            int upper = Math.min(index, values.length - 1);

            int diffLower = key - values[lower];
            int diffUpper = values[upper] - key;

            switch (rm) {

            case DOWN:
                return lower;

            case UP:
                return upper;

            case HALF_DOWN:
                return diffLower <= diffUpper ? lower : upper;

            case HALF_UP:
                return diffUpper <= diffLower ? upper : lower;

            default:
                throw new IllegalArgumentException("Invalid rounding mode");
            }
        }
    }

    /**
     * A facade for {@link #binarySearchNearest(int[], int, int, int, RoundingMode)}.
     */
    final public static int binarySearchNearest(int[] values, int to, int from, int key) {
        return binarySearchNearest(values, to, from, key, RoundingMode.HALF_DOWN);
    }

    /**
     * A facade for {@link #binarySearchNearest(int[], int, int, int, RoundingMode)}.
     */
    final public static int binarySearchNearest(int[] values, int key, RoundingMode rm) {
        return binarySearchNearest(values, 0, values.length, key, rm);
    }

    /**
     * A facade for {@link #binarySearchNearest(int[], int, int, int, RoundingMode)}.
     */
    final public static int binarySearchNearest(int[] values, int key) {
        return binarySearchNearest(values, 0, values.length, key, RoundingMode.HALF_DOWN);
    }

    /**
     * A variant of {@link java.util.Arrays#binarySearch(double[], int, int, double)} that finds the array index with
     * value nearest to the given key, as determined by the provided {@link RoundingMode}.
     * 
     * @param values
     *            the values.
     * @param from
     *            the search start index.
     * @param to
     *            the search end index.
     * @param key
     *            the key.
     * @param rm
     *            the {@link RoundingMode}.
     * @return the nearest index.
     */
    final public static int binarySearchNearest(double[] values, int from, int to, double key, RoundingMode rm) {

        Control.checkTrue(values.length > 0, //
                "Array size must be positive");

        int index = java.util.Arrays.binarySearch(values, from, to, key);

        if (index >= 0) {

            return index;

        } else {

            index = -index - 1;

            int lower = Math.max(index - 1, 0);
            int upper = Math.min(index, values.length - 1);

            double diffLower = key - values[lower];
            double diffUpper = values[upper] - key;

            switch (rm) {

            case DOWN:
                return lower;

            case UP:
                return upper;

            case HALF_DOWN:
                return diffLower <= diffUpper ? lower : upper;

            case HALF_UP:
                return diffUpper <= diffLower ? upper : lower;

            default:
                throw new IllegalArgumentException("Invalid rounding mode");
            }
        }
    }

    /**
     * A facade for {@link #binarySearchNearest(double[], int, int, double, RoundingMode)}.
     */
    final public static int binarySearchNearest(double[] values, int to, int from, double key) {
        return binarySearchNearest(values, to, from, key, RoundingMode.HALF_DOWN);
    }

    /**
     * A facade for {@link #binarySearchNearest(double[], int, int, double, RoundingMode)}.
     */
    final public static int binarySearchNearest(double[] values, double key, RoundingMode rm) {
        return binarySearchNearest(values, 0, values.length, key, rm);
    }

    /**
     * A facade for {@link #binarySearchNearest(double[], int, int, double, RoundingMode)}.
     */
    final public static int binarySearchNearest(double[] values, double key) {
        return binarySearchNearest(values, 0, values.length, key, RoundingMode.HALF_DOWN);
    }

    /**
     * A variant of {@link java.util.Arrays#binarySearch(Object[], int, int, Object)} that finds the array index with
     * value nearest to the given key, as determined by the provided {@link RoundingMode}.
     * 
     * @param values
     *            the values.
     * @param from
     *            the search start index.
     * @param to
     *            the search end index.
     * @param key
     *            the key.
     * @param rm
     *            the {@link RoundingMode}.
     * @param <T>
     *            the component type.
     * @return the nearest index.
     */
    final public static <T extends Comparable<? super T>> int binarySearchNearest(T[] values, int from, int to, //
            T key, RoundingMode rm) {

        Control.checkTrue(values.length > 0, //
                "Array size must be positive");

        int index = java.util.Arrays.binarySearch(values, from, to, key);

        if (index >= 0) {

            return index;

        } else {

            index = -index - 1;

            int lower = Math.max(index - 1, 0);
            int upper = Math.min(index, values.length - 1);

            switch (rm) {

            case DOWN:
                return lower;

            case UP:
                return upper;

            default:
                throw new IllegalArgumentException("Invalid rounding mode");
            }
        }
    }

    /**
     * A facade for {@link #binarySearchNearest(Comparable[], int, int, Comparable, RoundingMode)}.
     * 
     * @param <T>
     *            the component type.
     */
    final public static <T extends Comparable<? super T>> int binarySearchNearest(T[] values, int to, int from, //
            T key) {
        return binarySearchNearest(values, to, from, key, RoundingMode.DOWN);
    }

    /**
     * A facade for {@link #binarySearchNearest(Comparable[], int, int, Comparable, RoundingMode)}.
     * 
     * @param <T>
     *            the component type.
     */
    final public static <T extends Comparable<? super T>> int binarySearchNearest(T[] values, T key, RoundingMode rm) {
        return binarySearchNearest(values, 0, values.length, key, rm);
    }

    /**
     * A facade for {@link #binarySearchNearest(Comparable[], int, int, Comparable, RoundingMode)}.
     * 
     * @param <T>
     *            the component type.
     */
    final public static <T extends Comparable<? super T>> int binarySearchNearest(T[] values, T key) {
        return binarySearchNearest(values, 0, values.length, key, RoundingMode.DOWN);
    }

    /**
     * A variant of {@link java.util.Arrays#binarySearch(Object[], Object, Comparator)} that finds the array index with
     * value nearest to the given key, as determined by the provided {@link RoundingMode}.
     * 
     * @param values
     *            the values.
     * @param key
     *            the key.
     * @param rm
     *            the {@link RoundingMode}.
     * @param c
     *            the {@link Comparator} to use.
     * @param <T>
     *            the component type.
     * @return the nearest index.
     */
    final public static <T> int binarySearchNearest(T[] values, T key, Comparator<? super T> c, RoundingMode rm) {

        Control.checkTrue(values.length > 0, //
                "Array size must be positive");

        int index = java.util.Arrays.binarySearch(values, key, c);

        if (index >= 0) {

            return index;

        } else {

            index = -index - 1;

            int lower = Math.max(index - 1, 0);
            int upper = Math.min(index, values.length - 1);

            switch (rm) {

            case DOWN:
                return lower;

            case UP:
                return upper;

            default:
                throw new IllegalArgumentException("Invalid rounding mode");
            }
        }
    }

    /**
     * A facade for {@link #binarySearchNearest(Object[], Object, Comparator)}.
     * 
     * @param <T>
     *            the component type.
     */
    final public static <T> int binarySearchNearest(T[] values, T key, Comparator<? super T> c) {
        return binarySearchNearest(values, key, c, RoundingMode.DOWN);
    }

    /**
     * A variant of {@link Collections#binarySearch(List, Object)} that finds the array index with value nearest to the
     * given key, as determined by the provided {@link RoundingMode}.
     * 
     * @param values
     *            the values.
     * @param key
     *            the key.
     * @param rm
     *            the {@link RoundingMode}.
     * @param <T>
     *            the component type.
     * @return the nearest index.
     */
    final public static <T extends Comparable<? super T>> int binarySearchNearest(List<? extends T> values, T key, //
            RoundingMode rm) {

        Control.checkTrue(values.size() > 0, //
                "List size must be positive");

        int index = Collections.binarySearch(values, key);

        if (index >= 0) {

            return index;

        } else {

            index = -index - 1;

            int lower = Math.max(index - 1, 0);
            int upper = Math.min(index, values.size() - 1);

            switch (rm) {

            case DOWN:
                return lower;

            case UP:
                return upper;

            default:
                throw new IllegalArgumentException("Invalid rounding mode");
            }
        }
    }

    /**
     * A facade for {@link #binarySearchNearest(List, Comparable, RoundingMode)}.
     * 
     * @param <T>
     *            the component type.
     */
    final public static <T extends Comparable<? super T>> int binarySearchNearest(List<? extends T> values, T key) {
        return binarySearchNearest(values, key, RoundingMode.DOWN);
    }

    /**
     * A variant of {@link Collections#binarySearch(List, Object, Comparator)} that finds the array index with value
     * nearest to the given key, as determined by the provided {@link RoundingMode}.
     * 
     * @param values
     *            the values.
     * @param key
     *            the key.
     * @param rm
     *            the {@link RoundingMode}.
     * @param c
     *            the {@link Comparator} to use.
     * @param <T>
     *            the component type.
     * @return the nearest index.
     */
    final public static <T> int binarySearchNearest(List<? extends T> values, T key, Comparator<? super T> c, //
            RoundingMode rm) {

        Control.checkTrue(values.size() > 0, //
                "List size must be positive");

        int index = Collections.binarySearch(values, key, c);

        if (index >= 0) {

            return index;

        } else {

            index = -index - 1;

            int lower = Math.max(index - 1, 0);
            int upper = Math.min(index, values.size() - 1);

            switch (rm) {

            case DOWN:
                return lower;

            case UP:
                return upper;

            default:
                throw new IllegalArgumentException("Invalid rounding mode");
            }
        }
    }

    /**
     * A facade for {@link #binarySearchNearest(List, Object, Comparator, RoundingMode)}.
     * 
     * @param <T>
     *            the component type.
     */
    final public static <T> int binarySearchNearest(List<? extends T> values, T key, Comparator<? super T> c) {
        return binarySearchNearest(values, key, c, RoundingMode.DOWN);
    }

    // Dummy constructor.
    Arrays() {
    }
}

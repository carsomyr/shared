/**
 * This file is part of the Shared Scientific Toolbox in Java ("this library"). <br />
 * <br />
 * Copyright (C) 2007 Roy Liu <br />
 * <br />
 * This library is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation, either version 2.1 of the License, or (at your option)
 * any later version. <br />
 * <br />
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details. <br />
 * <br />
 * You should have received a copy of the GNU Lesser General Public License along with this library. If not, see <a
 * href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 */

package shared.util;

import java.util.Random;

/**
 * A collection of very basic arithmetic operations on arrays.
 * 
 * @author Roy Liu
 */
public class Arithmetic {

    /**
     * The underlying, shared source of randomness.
     */
    final protected static Random RandomKernel = new Random(0xdeadbeefcacabeadL);

    /**
     * Computes the maximum.
     * 
     * @param values
     *            the array.
     * @return the maximum.
     */
    final public static double max(double... values) {

        double acc = -Double.MAX_VALUE;

        for (double v : values) {
            acc = Math.max(acc, v);
        }

        return acc;
    }

    /**
     * Computes the minimum.
     * 
     * @param values
     *            the array.
     * @return the minimum.
     */
    final public static double min(double... values) {

        double acc = Double.MAX_VALUE;

        for (double v : values) {
            acc = Math.min(acc, v);
        }

        return acc;
    }

    /**
     * Computes the sum.
     * 
     * @param values
     *            the array.
     * @return the sum.
     */
    final public static double sum(double... values) {

        double acc = 0.0;

        for (double v : values) {
            acc += v;
        }

        return acc;
    }

    /**
     * Computes the product.
     * 
     * @param values
     *            the array.
     * @return the product.
     */
    final public static double product(double... values) {

        double acc = 1.0;

        for (double v : values) {
            acc *= v;
        }

        return acc;
    }

    /**
     * Computes the variance.
     * 
     * @param values
     *            the array.
     * @return the variance.
     */
    final public static double variance(double... values) {

        double len = values.length;
        double mean = sum(values) / len;
        double ssd = 0.0;

        for (double v : values) {
            ssd += ((v - mean) * (v - mean)) / len;
        }

        return ssd;
    }

    /**
     * Computes the entropy.
     * 
     * @param values
     *            the array.
     * @return the entropy.
     */
    final public static double entropy(double... values) {

        double sum = Math.max(0.0, sum(values)) + 1e-64;
        double en = 0.0;

        for (double v : values) {

            double val = v / sum;
            en += (val >= 1e-64) ? (val * Math.log(val)) : 0.0;
        }

        return -en;
    }

    /**
     * Shuffles the given array.
     * 
     * @param values
     *            the array.
     * @return the array.
     */
    final public static int[] shuffle(int[] values) {

        for (int i = values.length; i > 1; i--) {

            int index = Arithmetic.nextInt(i);

            int tmp = values[i - 1];
            values[i - 1] = values[index];
            values[index] = tmp;
        }

        return values;
    }

    /**
     * Shuffles the given array.
     * 
     * @param values
     *            the array.
     * @return the array.
     */
    final public static double[] shuffle(double[] values) {

        for (int i = values.length; i > 1; i--) {

            int index = Arithmetic.nextInt(i);

            double tmp = values[i - 1];
            values[i - 1] = values[index];
            values[index] = tmp;
        }

        return values;
    }

    /**
     * Computes the product.
     * 
     * @param values
     *            the array.
     * @return the product.
     */
    final public static int product(int... values) {

        int acc = 1;

        for (int value : values) {
            acc *= value;
        }

        return acc;
    }

    /**
     * Computes the sum.
     * 
     * @param values
     *            the array.
     * @return the sum.
     */
    final public static int sum(int... values) {

        int acc = 0;

        for (int value : values) {
            acc += value;
        }

        return acc;
    }

    /**
     * Computes the maximum.
     * 
     * @param values
     *            the array.
     * @return the maximum.
     */
    final public static int max(int... values) {

        int acc = Integer.MIN_VALUE;

        for (int value : values) {
            acc = Math.max(acc, value);
        }

        return acc;
    }

    /**
     * Computes the minimum.
     * 
     * @param values
     *            the array.
     * @return the minimum.
     */
    final public static int min(int... values) {

        int acc = Integer.MAX_VALUE;

        for (int value : values) {
            acc = Math.min(acc, value);
        }

        return acc;
    }

    /**
     * Creates a <tt>[m, n)</tt> range of {@code int}s with step increment <tt>k</tt>.
     * 
     * @param m
     *            the (inclusive) start value.
     * @param n
     *            the (exclusive) end value.
     * @param k
     *            the step increment.
     * @return the range.
     */
    final public static int[] range(int m, int n, int k) {

        Control.checkTrue((n >= m && k > 0) || (n <= m && k < 0), //
                "Invalid range specification");

        final int[] res;

        if (k > 0) {

            res = new int[(n - m + k - 1) / k];

            for (int i = m, ii = 0; i < n; i += k, ii++) {
                res[ii] = i;
            }

        } else {

            res = new int[(n - m + k + 1) / k];

            for (int i = m, ii = 0; i > n; i += k, ii++) {
                res[ii] = i;
            }
        }

        return res;
    }

    /**
     * Creates a <tt>[m, n)</tt> range of {@code int}s.
     * 
     * @param m
     *            the (inclusive) lower bound.
     * @param n
     *            the (exclusive) upper bound.
     * @return the range.
     */
    final public static int[] range(int m, int n) {
        return range(m, n, 1);
    }

    /**
     * Creates a <tt>[0, n)</tt> range of {@code int}s.
     * 
     * @param n
     *            the size of the range.
     * @return the range.
     */
    final public static int[] range(int n) {
        return range(0, n, 1);
    }

    /**
     * Creates a <tt>[m, n)</tt> range of {@code double}s with step increment <tt>k</tt>.
     * 
     * @param m
     *            the (inclusive) start value.
     * @param n
     *            the (exclusive) end value.
     * @param k
     *            the step increment.
     * @return the range.
     */
    final public static double[] doubleRange(int m, int n, int k) {

        Control.checkTrue((n >= m && k > 0) || (n <= m && k < 0), //
                "Invalid range specification");

        final double[] res;

        if (k > 0) {

            res = new double[(n - m + k - 1) / k];

            for (int i = m, ii = 0; i < n; i += k, ii++) {
                res[ii] = i;
            }

        } else {

            res = new double[(n - m + k + 1) / k];

            for (int i = m, ii = 0; i > n; i += k, ii++) {
                res[ii] = i;
            }
        }

        return res;
    }

    /**
     * Creates a <tt>[m, n)</tt> range of {@code double}s.
     * 
     * @param m
     *            the (inclusive) lower bound.
     * @param n
     *            the (exclusive) upper bound.
     * @return the range.
     */
    final public static double[] doubleRange(int m, int n) {
        return doubleRange(m, n, 1);
    }

    /**
     * Creates a <tt>[0, n)</tt> range of {@code double}s.
     * 
     * @param n
     *            the size of the range.
     * @return the range.
     */
    final public static double[] doubleRange(int n) {
        return doubleRange(0, n, 1);
    }

    /**
     * Creates a <tt>[m, n)</tt> range of {@code long}s with step increment <tt>k</tt>.
     * 
     * @param m
     *            the (inclusive) start value.
     * @param n
     *            the (exclusive) end value.
     * @param k
     *            the step increment.
     * @return the range.
     */
    final public static long[] longRange(int m, int n, int k) {

        Control.checkTrue((n >= m && k > 0) || (n <= m && k < 0), //
                "Invalid range specification");

        final long[] res;

        if (k > 0) {

            res = new long[(n - m + k - 1) / k];

            for (int i = m, ii = 0; i < n; i += k, ii++) {
                res[ii] = i;
            }

        } else {

            res = new long[(n - m + k + 1) / k];

            for (int i = m, ii = 0; i > n; i += k, ii++) {
                res[ii] = i;
            }
        }

        return res;
    }

    /**
     * Creates a <tt>[m, n)</tt> range of {@code long}s.
     * 
     * @param m
     *            the (inclusive) lower bound.
     * @param n
     *            the (exclusive) upper bound.
     * @return the range.
     */
    final public static long[] longRange(int m, int n) {
        return longRange(m, n, 1);
    }

    /**
     * Creates a <tt>[0, n)</tt> range of {@code long}s.
     * 
     * @param n
     *            the size of the range.
     * @return the range.
     */
    final public static long[] longRange(int n) {
        return longRange(0, n, 1);
    }

    /**
     * Finds the index of the given value.
     * 
     * @param values
     *            the array.
     * @param value
     *            the value to search for.
     * @return the index, or the array length if not found.
     */
    final public static int indexOf(int[] values, int value) {

        for (int i = 0, n = values.length; i < n; i++) {

            if (values[i] == value) {
                return i;
            }
        }

        return values.length;
    }

    /**
     * Counts the number of times the given value appears.
     * 
     * @param values
     *            the array.
     * @param value
     *            the value to count.
     * @return the count.
     */
    final public static int count(int[] values, int value) {

        int count = 0;

        for (int i = 0, n = values.length; i < n; i++) {

            if (values[i] == value) {
                count++;
            }
        }

        return count;
    }

    /**
     * Retrieves the source of randomness behind the static methods of {@link Arithmetic}.
     * 
     * @return the random source.
     */
    final public static Random getRandomSource() {
        return RandomKernel;
    }

    /**
     * A wrapper for {@link Random#nextInt(int)}.
     * 
     * @param n
     *            the upper bound.
     * @return an integer in <tt>[0, n)</tt>.
     */
    final public static int nextInt(int n) {
        return RandomKernel.nextInt(n);
    }

    /**
     * A wrapper for {@link Random#nextInt()}.
     * 
     * @return an integer chosen uniformly at random.
     */
    final public static int nextInt() {
        return RandomKernel.nextInt();
    }

    /**
     * A wrapper for {@link Random#nextLong()}.
     * 
     * @return a long chosen uniformly at random.
     */
    final public static long nextLong() {
        return RandomKernel.nextLong();
    }

    /**
     * A wrapper for {@link Random#nextDouble()}.
     * 
     * @param a
     *            the upper bound.
     * @return a {@code double} in <tt>[0, a)</tt>.
     */
    final public static double nextDouble(double a) {
        return a * RandomKernel.nextDouble();
    }

    /**
     * A wrapper for {@link Random#nextGaussian()}.
     * 
     * @param a
     *            the standard deviation.
     * @return a sample drawn from a Gaussian with mean {@code 0} and standard deviation {@code a}.
     */
    final public static double nextGaussian(double a) {
        return a * RandomKernel.nextGaussian();
    }

    /**
     * A wrapper for {@link Random#nextBytes(byte[])}.
     * 
     * @param n
     *            the size of the random {@code byte} array.
     * @return a randomly generated {@code byte} array of length <tt>n</tt>.
     */
    final public static byte[] nextBytes(int n) {

        byte[] res = new byte[n];

        RandomKernel.nextBytes(res);

        return res;
    }

    /**
     * Seeds the underlying source of randomness with {@link System#nanoTime()}.
     */
    final public static void randomize() {
        RandomKernel.setSeed(System.nanoTime());
    }

    /**
     * Seeds the underlying source of randomness with {@code 0}.
     */
    final public static void derandomize() {
        RandomKernel.setSeed(0);
    }

    // Dummy constructor.
    Arithmetic() {
    }
}

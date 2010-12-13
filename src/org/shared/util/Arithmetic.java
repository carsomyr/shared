/**
 * <p>
 * Copyright (c) 2007-2010 Roy Liu<br>
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

package org.shared.util;

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
    final protected static Random randomKernel = new Random(0xdeadbeefcacabeadL);

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
     * Computes the greatest common divisor of the given two numbers.
     * 
     * @param a
     *            the first number.
     * @param b
     *            the second number.
     * @return the greatest common divisor (gcd).
     */
    final public static int gcd(int a, int b) {

        for (int c; b != 0;) {

            c = a % b;
            a = b;
            b = c;
        }

        return a;
    }

    /**
     * Creates a <code>[{@code m}, {@code n})</code> range of {@code int}s with step increment {@code k}.
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
     * Creates a <code>[{@code m}, {@code n})</code> range of {@code int}s.
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
     * Creates a <code>[{@code 0}, {@code n})</code> range of {@code int}s.
     * 
     * @param n
     *            the size of the range.
     * @return the range.
     */
    final public static int[] range(int n) {
        return range(0, n, 1);
    }

    /**
     * Creates a <code>[{@code m}, {@code n})</code> range of {@code double}s with step increment {@code k}.
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
     * Creates a <code>[{@code m}, {@code n})</code> range of {@code double}s.
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
     * Creates a <code>[{@code 0}, {@code n})</code> range of {@code double}s.
     * 
     * @param n
     *            the size of the range.
     * @return the range.
     */
    final public static double[] doubleRange(int n) {
        return doubleRange(0, n, 1);
    }

    /**
     * Creates a <code>[{@code m}, {@code n})</code> range of {@code long}s with step increment {@code k}.
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
     * Creates a <code>[{@code m}, {@code n})</code> range of {@code long}s.
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
     * Creates a <code>[{@code 0}, {@code n})</code> range of {@code long}s.
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
     * @param target
     *            the value to count.
     * @return the count.
     */
    final public static int count(int[] values, int target) {

        int count = 0;

        for (int value : values) {

            if (value == target) {
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
        return randomKernel;
    }

    /**
     * A wrapper for {@link Random#nextInt(int)}.
     * 
     * @param n
     *            the upper bound.
     * @return an {@code int} in <code>[{@code 0}, {@code n})</code>.
     */
    final public static int nextInt(int n) {
        return randomKernel.nextInt(n);
    }

    /**
     * A wrapper for {@link Random#nextInt()}.
     * 
     * @return an {@code int} chosen uniformly at random.
     */
    final public static int nextInt() {
        return randomKernel.nextInt();
    }

    /**
     * A wrapper for {@link Random#nextLong()}.
     * 
     * @return a {@code long} chosen uniformly at random.
     */
    final public static long nextLong() {
        return randomKernel.nextLong();
    }

    /**
     * A wrapper for {@link Random#nextDouble()}.
     * 
     * @param a
     *            the upper bound.
     * @return a {@code double} in <code>[{@code 0}, {@code a})</code>.
     */
    final public static double nextDouble(double a) {
        return a * randomKernel.nextDouble();
    }

    /**
     * A wrapper for {@link Random#nextGaussian()}.
     * 
     * @param a
     *            the standard deviation.
     * @return a sample drawn from a Gaussian with mean {@code 0} and standard deviation {@code a}.
     */
    final public static double nextGaussian(double a) {
        return a * randomKernel.nextGaussian();
    }

    /**
     * A wrapper for {@link Random#nextBytes(byte[])}.
     * 
     * @param n
     *            the size of the random {@code byte} array.
     * @return a randomly generated {@code byte} array of length {@code n}.
     */
    final public static byte[] nextBytes(int n) {

        byte[] res = new byte[n];

        randomKernel.nextBytes(res);

        return res;
    }

    /**
     * Seeds the underlying source of randomness with {@link System#nanoTime()}.
     */
    final public static void randomize() {
        randomKernel.setSeed(System.nanoTime());
    }

    /**
     * Seeds the underlying source of randomness with {@code 0}.
     */
    final public static void derandomize() {
        randomKernel.setSeed(0);
    }

    // Dummy constructor.
    Arithmetic() {
    }
}

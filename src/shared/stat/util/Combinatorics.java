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

package shared.stat.util;

import shared.util.Control;
import shared.util.DynamicObjectArray;

/**
 * A collection of useful static methods for combinatorics.
 * 
 * @author Roy Liu
 */
public class Combinatorics {

    /**
     * A lookup table of coefficients in support of {@link #gammaLn(double)}.
     */
    final protected static double[] GammaLnCoefficients = new double[] {
    //
            76.18009172947146, -86.50532032941677, //
            24.01409824083091, -1.231739572450155, //
            0.1208650973866179e-2, -0.5395239384953e-5 //
    };

    /**
     * A facade for {@link #partition(int, int, int)}.
     * 
     * @param n
     *            the number of elements.
     * @return the partitions.
     */
    final public static int[][] partition(int n) {
        return partition(n, 1, n + 1);
    }

    /**
     * Calculates all partitions of an {@code n} element set into at least {@code npartsLower} parts and at most
     * (exclusive) {@code npartsUpper} parts.
     * 
     * @param n
     *            the number of elements.
     * @param npartsLower
     *            the lower bound on the number of parts.
     * @param npartsUpper
     *            the upper bound (exclusive) on the number of parts.
     * @return the partitions.
     */
    final public static int[][] partition(int n, int npartsLower, int npartsUpper) {

        Control.checkTrue(npartsLower >= 1 && npartsUpper <= n + 1, //
                "Invalid arguments");

        DynamicObjectArray<int[]> acc = new DynamicObjectArray<int[]>(int[].class);

        for (int nparts = npartsLower; nparts < npartsUpper; nparts++) {
            partition(acc, new int[nparts], 0, 1, n);
        }

        return acc.values();
    }

    /**
     * A helper method in support of {@link #partition(int, int, int)}.
     * 
     * @param acc
     *            the partition accumulator.
     * @param sizes
     *            the partition sizes.
     * @param nsizes
     *            the number of parts so far.
     * @param currentSize
     *            the part size so far.
     * @param nremaining
     *            the number of remaining elements.
     */
    final protected static void partition(DynamicObjectArray<int[]> acc, //
            int[] sizes, int nsizes, int currentSize, int nremaining) {

        if (nremaining == 0 && sizes.length == nsizes) {
            acc.push(sizes.clone());
        }

        if (sizes.length == nsizes) {
            return;
        }

        for (int size = currentSize, maxSize = nremaining / (sizes.length - nsizes); size <= maxSize; size++) {

            sizes[nsizes] = size;
            partition(acc, sizes, nsizes + 1, size, nremaining - size);
        }
    }

    /**
     * A facade for {@link #orderedPartition(int, int, int)}.
     * 
     * @param n
     *            the number of elements.
     * @return the ordered partitions.
     */
    final public static int[][] orderedPartition(int n) {
        return orderedPartition(n, 1, n + 1);
    }

    /**
     * Calculates all ordered partitions of an {@code n} element set into at least {@code npartsLower} parts and at most
     * (exclusive) {@code npartsUpper} parts.
     * 
     * @param n
     *            the number of elements.
     * @param npartsLower
     *            the lower bound on the number of parts.
     * @param npartsUpper
     *            the upper bound (exclusive) on the number of parts.
     * @return the ordered partitions.
     */
    final public static int[][] orderedPartition(int n, int npartsLower, int npartsUpper) {

        Control.checkTrue(npartsLower < npartsUpper, //
                "Invalid arguments");

        DynamicObjectArray<int[]> acc = new DynamicObjectArray<int[]>(int[].class);

        for (int nparts = npartsLower; nparts < npartsUpper; nparts++) {
            orderedPartition(acc, new int[nparts], 0, n);
        }

        return acc.values();
    }

    /**
     * A helper method in support of {@link #orderedPartition(int, int, int)}.
     * 
     * @param acc
     *            the ordered partition accumulator.
     * @param sizes
     *            the ordered partition sizes.
     * @param nsizes
     *            the number of parts so far.
     * @param nremaining
     *            the number of remaining elements.
     */
    final protected static void orderedPartition(DynamicObjectArray<int[]> acc, //
            int[] sizes, int nsizes, int nremaining) {

        if (nremaining == 0 && sizes.length == nsizes) {
            acc.push(sizes.clone());
        }

        if (sizes.length == nsizes) {
            return;
        }

        for (int size = 0, maxSize = nremaining; size <= maxSize; size++) {

            sizes[nsizes] = size;
            orderedPartition(acc, sizes, nsizes + 1, nremaining - size);
        }
    }

    /**
     * The <a href="http://en.wikipedia.org/wiki/Gamma_function">gamma function</a>.
     * 
     * @param x
     *            the input value.
     * @return the gamma function evaluation.
     */
    final public static double gamma(double x) {
        return Math.exp(gammaLn(x));
    }

    /**
     * The log-gamma function.
     * 
     * @param x
     *            the input value.
     * @return the log-gamma function evaluation.
     */
    final public static double gammaLn(double x) {

        double t = x + 4.5 - (x - 0.5) * Math.log(x + 4.5);
        double sum = 1.000000000190015;

        for (int j = 0, n = GammaLnCoefficients.length; j < n; j++, x++) {
            sum += GammaLnCoefficients[j] / x;
        }

        return -t + Math.log(2.5066282746310005 * sum);
    }

    // Dummy constructor.
    Combinatorics() {
    }
}

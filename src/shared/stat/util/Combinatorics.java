/**
 * <p>
 * Copyright (C) 2007 The Regents of the University of California<br />
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

/**
 * <p>
 * Copyright (c) 2008 The Regents of the University of California<br>
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

package shared.fft;

import java.util.ArrayList;

import shared.util.Arithmetic;

/**
 * A class of mixed-radix FFT operations in support of {@link JavaFFTService}.
 * 
 * @author Roy Liu
 */
public class FFTOps {

    /**
     * An FFT operation in support of {@link JavaFFTService#fft(int[], double[], double[])}. Very slow and unoptimized;
     * for demonstration purposes only.
     * 
     * @param direction
     *            the transform direction.
     * @param dims
     *            the array dimensions.
     * @param in
     *            the in array.
     * @param out
     *            the out array.
     */
    final public static void fft(int direction, int[] dims, double[] in, double[] out) {

        int nDims = dims.length;
        int len = Arithmetic.product(dims);

        double[] outTmp = new double[len << 1];
        double[] scratch = new double[len << 1];

        System.arraycopy(in, 0, out, 0, len << 1);

        for (int dim = 0; dim < nDims; dim++) {

            int dimSize = dims[dim];
            int dimStride = len / dimSize;

            int[] factors = createFactors(dimSize);
            double[] twiddles = createTwiddles(dimSize, direction);

            int srcOffsetIncr = 2;
            int dstOffsetIncr = dimSize << 1;

            for (int j = 0, srcOffset = 0, dstOffset = 0; j < dimStride; //
            j++, srcOffset += srcOffsetIncr, dstOffset += dstOffsetIncr) {
                fft(out, srcOffset, outTmp, dstOffset, scratch, //
                        factors, 0, twiddles, dimSize, dimStride, 1);
            }

            System.arraycopy(outTmp, 0, out, 0, len << 1);
        }

        switch (direction) {

        case +1:
            break;

        case -1:

            double factor = 1.0 / len;

            for (int i = 0, offset = 0; i < len; i++, offset += 2) {

                out[offset] *= factor;
                out[offset + 1] *= factor;
            }

            break;

        default:
            throw new IllegalArgumentException("Invalid transform direction");
        }
    }

    /**
     * Creates a prime factorization and stride modifiers array.
     */
    final protected static int[] createFactors(int num) {

        ArrayList<Integer> factors = new ArrayList<Integer>();

        int p = 2;
        int upper = (int) Math.floor(Math.sqrt(num));

        do {

            for (; num % p != 0;) {

                switch (p) {

                case 1:
                    p = 1;
                    break;

                default:
                    p += 2;
                    break;
                }

                p = p > upper ? num : p;
            }

            num /= p;

            factors.add(p);
            factors.add(num);

        } while (num > 1);

        int[] res = new int[factors.size()];

        for (int i = 0, n = res.length; i < n; i++) {
            res[i] = factors.get(i);
        }

        return res;
    }

    /**
     * Creates an array of twiddle factors.
     */
    final protected static double[] createTwiddles(int n, int direction) {

        double[] res = new double[n << 1];

        for (int i = 0, offset = 0; i < n; i++, offset += 2) {

            double phase = (-2 * direction * Math.PI * i) / n;

            res[offset] = Math.cos(phase);
            res[offset + 1] = Math.sin(phase);
        }

        return res;
    }

    /**
     * The main mixed-radix FFT procedure.
     */
    final protected static void fft(double[] src, int srcOffset, double[] dst, int dstOffset, double[] scratch, //
            int[] factors, int factorIndexCurrent, double[] twiddles, //
            int size, int stride, int strideCurrent) {

        int p = factors[factorIndexCurrent];
        int m = factors[factorIndexCurrent + 1];

        int srcOffsetIncr = (strideCurrent * stride) << 1;
        int dstOffsetIncr = m << 1;
        int upper = dstOffset + ((p * m) << 1);

        if (m == 1) {

            for (int srcOffsetCurrent = srcOffset, dstOffsetCurrent = dstOffset; dstOffsetCurrent < upper; //
            srcOffsetCurrent += srcOffsetIncr, dstOffsetCurrent += 2) {

                dst[dstOffsetCurrent] = src[srcOffsetCurrent];
                dst[dstOffsetCurrent + 1] = src[srcOffsetCurrent + 1];
            }

        } else {

            for (int srcOffsetCurrent = srcOffset, dstOffsetCurrent = dstOffset; dstOffsetCurrent < upper; //
            srcOffsetCurrent += srcOffsetIncr, dstOffsetCurrent += dstOffsetIncr) {
                fft(src, srcOffsetCurrent, dst, dstOffsetCurrent, scratch, //
                        factors, factorIndexCurrent + 2, twiddles, //
                        size, stride, strideCurrent * p);
            }
        }

        fft(dst, dstOffset, scratch, p, m, twiddles, size, strideCurrent);
    }

    /**
     * A recursive FFT subroutine for a specific radix.
     */
    final protected static void fft(double[] dst, int dstOffsetCurrent, double[] scratch, //
            int p, int n, double[] twiddles, int size, int strideCurrent) {

        int dstOffsetIncr = n << 1;
        int scratchOffsetIncr = 2;
        int twiddleOffsetIncr = (strideCurrent * n) << 1;

        int modulus = size << 1;

        for (int i = 0; i < n; i++) {

            for (int j = 0, dstOffset = (i << 1) + dstOffsetCurrent, scratchOffset = 0; j < p; //
            j++, dstOffset += dstOffsetIncr, scratchOffset += scratchOffsetIncr) {

                scratch[scratchOffset] = dst[dstOffset];
                scratch[scratchOffset + 1] = dst[dstOffset + 1];
            }

            for (int j = 0, dstOffset = (i << 1) + dstOffsetCurrent, twiddleOffset = (strideCurrent * i) << 1; j < p; //
            j++, dstOffset += dstOffsetIncr, twiddleOffset += twiddleOffsetIncr) {

                dst[dstOffset] = scratch[0];
                dst[dstOffset + 1] = scratch[1];

                for (int k = 1, scratchOffset = 2, modOffset = twiddleOffset; k < p; //
                k++, scratchOffset += scratchOffsetIncr, //
                modOffset = (modOffset + twiddleOffset) % modulus) {

                    dst[dstOffset] += scratch[scratchOffset] * twiddles[modOffset] //
                            - scratch[scratchOffset + 1] * twiddles[modOffset + 1];
                    dst[dstOffset + 1] += scratch[scratchOffset] * twiddles[modOffset + 1] //
                            + scratch[scratchOffset + 1] * twiddles[modOffset];
                }
            }
        }
    }

    // Dummy constructor.
    FFTOps() {
    }
}

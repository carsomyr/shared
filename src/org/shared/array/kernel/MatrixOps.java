/**
 * <p>
 * Copyright (c) 2007 Roy Liu<br>
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

package org.shared.array.kernel;

import org.shared.util.Control;

/**
 * A class for matrix operations in pure Java.
 * 
 * @author Roy Liu
 */
public class MatrixOps {

    /**
     * A matrix multiply operation in support of
     * {@link JavaArrayKernel#mul(double[], double[], int, int, double[], boolean)}.
     */
    final public static void mul(double[] lhsV, double[] rhsV, int lr, int rc, double[] dstV, boolean complex) {

        int factor = (complex ? 2 : 1);
        int lc = (lr != 0) ? lhsV.length / (factor * lr) : 0;
        int rr = (rc != 0) ? rhsV.length / (factor * rc) : 0;
        int inner = Control.checkEquals(lc, rr);

        Control.checkTrue(lr >= 0 && rc >= 0 //
                && (lhsV.length == factor * lr * lc) //
                && (rhsV.length == factor * rr * rc) //
                && (dstV.length == factor * lr * rc), //
                "Invalid array lengths");

        if (complex) {

            for (int i = 0; i < lr; i++) {

                for (int j = 0; j < rc; j++) {

                    double sumRe = 0.0;
                    double sumIm = 0.0;

                    for (int k = 0; k < inner; k++) {

                        int lIndex = 2 * (i * lc + k);
                        int rIndex = 2 * (k * rc + j);

                        sumRe += lhsV[lIndex] * rhsV[rIndex] //
                                - lhsV[lIndex + 1] * rhsV[rIndex + 1];
                        sumIm += lhsV[lIndex] * rhsV[rIndex + 1] //
                                + lhsV[lIndex + 1] * rhsV[rIndex];
                    }

                    int outIndex = 2 * (i * rc + j);

                    dstV[outIndex] = sumRe;
                    dstV[outIndex + 1] = sumIm;
                }
            }

        } else {

            for (int i = 0; i < lr; i++) {

                for (int j = 0; j < rc; j++) {

                    double sum = 0.0;

                    for (int k = 0; k < inner; k++) {
                        sum += lhsV[i * lc + k] * rhsV[k * rc + j];
                    }

                    dstV[i * rc + j] = sum;
                }
            }
        }
    }

    /**
     * A matrix diagonal operation in support of {@link JavaArrayKernel#diag(double[], double[], int, boolean)}.
     */
    final public static void diag(double[] srcV, double[] dstV, int size, boolean complex) {

        int factor = (complex ? 2 : 1);

        Control.checkTrue((srcV.length == factor * size * size) && (dstV.length == factor * size), //
                "Invalid array lengths");

        if (complex) {

            for (int j = 0, m = 2 * size; j < m; j += 2) {

                dstV[j] = srcV[j * size + j];
                dstV[j + 1] = srcV[j * size + j + 1];
            }

        } else {

            for (int i = 0; i < size; i++) {
                dstV[i] = srcV[i * size + i];
            }
        }
    }

    // Dummy constructor.
    MatrixOps() {
    }
}

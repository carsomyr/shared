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

package shared.array.kernel;

import shared.util.Control;

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
    final public static void mul(double[] lhsV, double[] rhsV, int lr, int rc, double[] dstV, boolean isComplex) {

        int factor = (isComplex ? 2 : 1);
        int lc = (lr != 0) ? lhsV.length / (factor * lr) : 0;
        int rr = (rc != 0) ? rhsV.length / (factor * rc) : 0;
        int inner = Control.checkEquals(lc, rr);

        Control.checkTrue(lr >= 0 && rc >= 0 //
                && (lhsV.length == factor * lr * lc) //
                && (rhsV.length == factor * rr * rc) //
                && (dstV.length == factor * lr * rc), //
                "Invalid array lengths");

        if (isComplex) {

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
    final public static void diag(double[] srcV, double[] dstV, int size, boolean isComplex) {

        int factor = (isComplex ? 2 : 1);

        Control.checkTrue((srcV.length == factor * size * size) && (dstV.length == factor * size), //
                "Invalid array lengths");

        if (isComplex) {

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

/**
 * <p>
 * Copyright (C) 2009 Roy Liu<br />
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

package shared.array.kernel;

import static shared.array.kernel.ElementOps.CEDivOp;
import static shared.array.kernel.ElementOps.CTORAbsOp;
import shared.util.Arithmetic;
import shared.util.Control;

/**
 * A class for linear algebra operations in pure Java.
 * 
 * @author Roy Liu
 */
public class LinearAlgebraOps {

    /**
     * A singular value decomposition operation in support of
     * {@link JavaArrayKernel#svd(double[], int, int, double[], double[], double[], int, int)}.
     */
    final public static void svd(double[] srcV, int srcStrideRow, int srcStrideCol, //
            double[] uV, double[] sV, double[] vV, //
            int nRows, int nCols) {

        Control.checkTrue(nRows >= nCols //
                && srcV.length == nRows * nCols //
                && uV.length == nRows * nCols //
                && sV.length == nCols //
                && vV.length == nCols * nCols //
                && ((srcStrideRow == nCols && srcStrideCol == 1) //
                || (srcStrideRow == 1 && srcStrideCol == nRows)), //
                "Invalid arguments");

        int uStrideRow = nCols;
        int vStrideRow = nCols;

        double[] e = new double[nCols];
        double[] work = new double[nRows];
        double[] a = srcV.clone();

        // Reduce A to bidiagonal form, storing the diagonal elements
        // in s and the super-diagonal elements in e.

        int nc = Math.min(nRows - 1, nCols);
        int nr = Math.max(0, Math.min(nCols - 2, nRows));
        for (int k = 0, l = Math.max(nc, nr); k < l; k++) {
            if (k < nc) {

                // Compute the transformation for the k-th column and
                // place the k-th diagonal in s[k].
                // Compute 2-norm of k-th column without under/overflow.
                sV[k] = 0;
                for (int i = k; i < nRows; i++) {
                    sV[k] = CTORAbsOp.op(sV[k], a[srcStrideRow * (i) + srcStrideCol * (k)]);
                }
                if (sV[k] != 0.0) {
                    if (a[srcStrideRow * (k) + srcStrideCol * (k)] < 0.0) {
                        sV[k] = -sV[k];
                    }
                    for (int i = k; i < nRows; i++) {
                        a[srcStrideRow * (i) + srcStrideCol * (k)] /= sV[k];
                    }
                    a[srcStrideRow * (k) + srcStrideCol * (k)] += 1.0;
                }
                sV[k] = -sV[k];
            }
            for (int j = k + 1; j < nCols; j++) {
                if ((k < nc) & (sV[k] != 0.0)) {

                    // Apply the transformation.

                    double t = 0;
                    for (int i = k; i < nRows; i++) {
                        t += a[srcStrideRow * (i) + srcStrideCol * (k)] * a[srcStrideRow * (i) + srcStrideCol * (j)];
                    }
                    t = -t / a[srcStrideRow * (k) + srcStrideCol * (k)];
                    for (int i = k; i < nRows; i++) {
                        a[srcStrideRow * (i) + srcStrideCol * (j)] += t * a[srcStrideRow * (i) + srcStrideCol * (k)];
                    }
                }

                // Place the k-th row of A into e for the
                // subsequent calculation of the row transformation.

                e[j] = a[srcStrideRow * (k) + srcStrideCol * (j)];
            }
            if (k < nc) {

                // Place the transformation in U for subsequent back
                // multiplication.

                for (int i = k; i < nRows; i++) {
                    uV[uStrideRow * (i) + (k)] = a[srcStrideRow * (i) + srcStrideCol * (k)];
                }
            }
            if (k < nr) {

                // Compute the k-th row transformation and place the
                // k-th super-diagonal in e[k].
                // Compute 2-norm without under/overflow.
                e[k] = 0;
                for (int i = k + 1; i < nCols; i++) {
                    e[k] = CTORAbsOp.op(e[k], e[i]);
                }
                if (e[k] != 0.0) {
                    if (e[k + 1] < 0.0) {
                        e[k] = -e[k];
                    }
                    for (int i = k + 1; i < nCols; i++) {
                        e[i] /= e[k];
                    }
                    e[k + 1] += 1.0;
                }
                e[k] = -e[k];
                if ((k + 1 < nRows) & (e[k] != 0.0)) {

                    // Apply the transformation.

                    for (int i = k + 1; i < nRows; i++) {
                        work[i] = 0.0;
                    }
                    for (int j = k + 1; j < nCols; j++) {
                        for (int i = k + 1; i < nRows; i++) {
                            work[i] += e[j] * a[srcStrideRow * (i) + srcStrideCol * (j)];
                        }
                    }
                    for (int j = k + 1; j < nCols; j++) {
                        double t = -e[j] / e[k + 1];
                        for (int i = k + 1; i < nRows; i++) {
                            a[srcStrideRow * (i) + srcStrideCol * (j)] += t * work[i];
                        }
                    }
                }

                // Place the transformation in V for subsequent
                // back multiplication.

                for (int i = k + 1; i < nCols; i++) {
                    vV[vStrideRow * (i) + (k)] = e[i];
                }
            }
        }

        // Set up the final bidiagonal matrix or order p.

        int p = nCols;
        if (nc < nCols) {
            sV[nc] = a[srcStrideRow * (nc) + srcStrideCol * (nc)];
        }
        if (nRows < p) {
            sV[p - 1] = 0.0;
        }
        if (nr + 1 < p) {
            e[nr] = a[srcStrideRow * (nr) + srcStrideCol * (p - 1)];
        }
        e[p - 1] = 0.0;

        // If required, generate U.

        for (int j = nc; j < nCols; j++) {
            for (int i = 0; i < nRows; i++) {
                uV[uStrideRow * (i) + (j)] = 0.0;
            }
            uV[uStrideRow * (j) + (j)] = 1.0;
        }
        for (int k = nc - 1; k >= 0; k--) {
            if (sV[k] != 0.0) {
                for (int j = k + 1; j < nCols; j++) {
                    double t = 0;
                    for (int i = k; i < nRows; i++) {
                        t += uV[uStrideRow * (i) + (k)] * uV[uStrideRow * (i) + (j)];
                    }
                    t = -t / uV[uStrideRow * (k) + (k)];
                    for (int i = k; i < nRows; i++) {
                        uV[uStrideRow * (i) + (j)] += t * uV[uStrideRow * (i) + (k)];
                    }
                }
                for (int i = k; i < nRows; i++) {
                    uV[uStrideRow * (i) + (k)] = -uV[uStrideRow * (i) + (k)];
                }
                uV[uStrideRow * (k) + (k)] = 1.0 + uV[uStrideRow * (k) + (k)];
                for (int i = 0; i < k - 1; i++) {
                    uV[uStrideRow * (i) + (k)] = 0.0;
                }
            } else {
                for (int i = 0; i < nRows; i++) {
                    uV[uStrideRow * (i) + (k)] = 0.0;
                }
                uV[uStrideRow * (k) + (k)] = 1.0;
            }
        }

        // If required, generate V.

        for (int k = nCols - 1; k >= 0; k--) {
            if ((k < nr) & (e[k] != 0.0)) {
                for (int j = k + 1; j < nCols; j++) {
                    double t = 0;
                    for (int i = k + 1; i < nCols; i++) {
                        t += vV[vStrideRow * (i) + (k)] * vV[vStrideRow * (i) + (j)];
                    }
                    t = -t / vV[vStrideRow * (k + 1) + (k)];
                    for (int i = k + 1; i < nCols; i++) {
                        vV[vStrideRow * (i) + (j)] += t * vV[vStrideRow * (i) + (k)];
                    }
                }
            }
            for (int i = 0; i < nCols; i++) {
                vV[vStrideRow * (i) + (k)] = 0.0;
            }
            vV[vStrideRow * (k) + (k)] = 1.0;
        }

        // Main iteration loop for the singular values.

        int pp = p - 1;
        int iter = 0;
        double eps = Math.pow(2.0, -52.0);
        double tiny = Math.pow(2.0, -966.0);
        while (p > 0) {
            int k, kase;

            // Here is where a test for too many iterations would go.

            // This section of the program inspects for
            // negligible elements in the s and e arrays. On
            // completion the variables kase and k are set as follows.

            // kase = 1 if s(p) and e[k-1] are negligible and k<p
            // kase = 2 if s(k) is negligible and k<p
            // kase = 3 if e[k-1] is negligible, k<p, and
            // s(k), ..., s(p) are not negligible (qr step).
            // kase = 4 if e(p-1) is negligible (convergence).

            for (k = p - 2; k >= -1; k--) {
                if (k == -1) {
                    break;
                }
                if (Math.abs(e[k]) <= tiny + eps * (Math.abs(sV[k]) + Math.abs(sV[k + 1]))) {
                    e[k] = 0.0;
                    break;
                }
            }
            if (k == p - 2) {
                kase = 4;
            } else {
                int ks;
                for (ks = p - 1; ks >= k; ks--) {
                    if (ks == k) {
                        break;
                    }
                    double t = (ks != p ? Math.abs(e[ks]) : 0.) + (ks != k + 1 ? Math.abs(e[ks - 1]) : 0.);
                    if (Math.abs(sV[ks]) <= tiny + eps * t) {
                        sV[ks] = 0.0;
                        break;
                    }
                }
                if (ks == k) {
                    kase = 3;
                } else if (ks == p - 1) {
                    kase = 1;
                } else {
                    kase = 2;
                    k = ks;
                }
            }
            k++;

            // Perform the task indicated by kase.

            switch (kase) {

            // Deflate negligible s(p).

            case 1: {
                double f = e[p - 2];
                e[p - 2] = 0.0;
                for (int j = p - 2; j >= k; j--) {
                    double t = CTORAbsOp.op(sV[j], f);
                    double cs = sV[j] / t;
                    double sn = f / t;
                    sV[j] = t;
                    if (j != k) {
                        f = -sn * e[j - 1];
                        e[j - 1] = cs * e[j - 1];
                    }
                    for (int i = 0; i < nCols; i++) {
                        t = cs * vV[vStrideRow * (i) + (j)] + sn * vV[vStrideRow * (i) + (p - 1)];
                        vV[vStrideRow * (i) + (p - 1)] = -sn * vV[vStrideRow * (i) + (j)] + cs
                                * vV[vStrideRow * (i) + (p - 1)];
                        vV[vStrideRow * (i) + (j)] = t;
                    }
                }
            }
                break;

            // Split at negligible s(k).

            case 2: {
                double f = e[k - 1];
                e[k - 1] = 0.0;
                for (int j = k; j < p; j++) {
                    double t = CTORAbsOp.op(sV[j], f);
                    double cs = sV[j] / t;
                    double sn = f / t;
                    sV[j] = t;
                    f = -sn * e[j];
                    e[j] = cs * e[j];
                    for (int i = 0; i < nRows; i++) {
                        t = cs * uV[uStrideRow * (i) + (j)] + sn * uV[uStrideRow * (i) + (k - 1)];
                        uV[uStrideRow * (i) + (k - 1)] = -sn * uV[uStrideRow * (i) + (j)] + cs
                                * uV[uStrideRow * (i) + (k - 1)];
                        uV[uStrideRow * (i) + (j)] = t;
                    }
                }
            }
                break;

            // Perform one qr step.

            case 3: {

                // Calculate the shift.

                double scale = Math.max(Math.max(Math.max(Math.max(Math.abs(sV[p - 1]), Math.abs(sV[p - 2])), Math
                        .abs(e[p - 2])), Math.abs(sV[k])), Math.abs(e[k]));
                double sp = sV[p - 1] / scale;
                double spm1 = sV[p - 2] / scale;
                double epm1 = e[p - 2] / scale;
                double sk = sV[k] / scale;
                double ek = e[k] / scale;
                double b = ((spm1 + sp) * (spm1 - sp) + epm1 * epm1) / 2.0;
                double c = (sp * epm1) * (sp * epm1);
                double shift = 0.0;
                if ((b != 0.0) | (c != 0.0)) {
                    shift = Math.sqrt(b * b + c);
                    if (b < 0.0) {
                        shift = -shift;
                    }
                    shift = c / (b + shift);
                }
                double f = (sk + sp) * (sk - sp) + shift;
                double g = sk * ek;

                // Chase zeros.

                for (int j = k; j < p - 1; j++) {
                    double t = CTORAbsOp.op(f, g);
                    double cs = f / t;
                    double sn = g / t;
                    if (j != k) {
                        e[j - 1] = t;
                    }
                    f = cs * sV[j] + sn * e[j];
                    e[j] = cs * e[j] - sn * sV[j];
                    g = sn * sV[j + 1];
                    sV[j + 1] = cs * sV[j + 1];
                    for (int i = 0; i < nCols; i++) {
                        t = cs * vV[vStrideRow * (i) + (j)] + sn * vV[vStrideRow * (i) + (j + 1)];
                        vV[vStrideRow * (i) + (j + 1)] = -sn * vV[vStrideRow * (i) + (j)] + cs
                                * vV[vStrideRow * (i) + (j + 1)];
                        vV[vStrideRow * (i) + (j)] = t;
                    }
                    t = CTORAbsOp.op(f, g);
                    cs = f / t;
                    sn = g / t;
                    sV[j] = t;
                    f = cs * e[j] + sn * sV[j + 1];
                    sV[j + 1] = -sn * e[j] + cs * sV[j + 1];
                    g = sn * e[j + 1];
                    e[j + 1] = cs * e[j + 1];
                    if (j < nRows - 1) {
                        for (int i = 0; i < nRows; i++) {
                            t = cs * uV[uStrideRow * (i) + (j)] + sn * uV[uStrideRow * (i) + (j + 1)];
                            uV[uStrideRow * (i) + (j + 1)] = -sn * uV[uStrideRow * (i) + (j)] + cs
                                    * uV[uStrideRow * (i) + (j + 1)];
                            uV[uStrideRow * (i) + (j)] = t;
                        }
                    }
                }
                e[p - 2] = f;
                iter = iter + 1;
            }
                break;

            // Convergence.

            case 4: {

                // Make the singular values positive.

                if (sV[k] <= 0.0) {
                    sV[k] = (sV[k] < 0.0 ? -sV[k] : 0.0);
                    for (int i = 0; i <= pp; i++) {
                        vV[vStrideRow * (i) + (k)] = -vV[vStrideRow * (i) + (k)];
                    }
                }

                // Order the singular values.

                while (k < pp) {
                    if (sV[k] >= sV[k + 1]) {
                        break;
                    }
                    double t = sV[k];
                    sV[k] = sV[k + 1];
                    sV[k + 1] = t;
                    if (k < nCols - 1) {
                        for (int i = 0; i < nCols; i++) {
                            t = vV[vStrideRow * (i) + (k + 1)];
                            vV[vStrideRow * (i) + (k + 1)] = vV[vStrideRow * (i) + (k)];
                            vV[vStrideRow * (i) + (k)] = t;
                        }
                    }
                    if (k < nRows - 1) {
                        for (int i = 0; i < nRows; i++) {
                            t = uV[uStrideRow * (i) + (k + 1)];
                            uV[uStrideRow * (i) + (k + 1)] = uV[uStrideRow * (i) + (k)];
                            uV[uStrideRow * (i) + (k)] = t;
                        }
                    }
                    k++;
                }
                iter = 0;
                p--;
            }
                break;
            }
        }
    }

    /**
     * An eigenvector and eigenvalue operation in support of
     * {@link JavaArrayKernel#eigs(double[], double[], double[], int)}.
     */
    final public static void eigs(double[] srcV, double[] vecV, double[] valV, int size) {

        Control.checkTrue(srcV.length == size * size //
                && vecV.length == size * size //
                && valV.length == 2 * size, //
                "Invalid arguments");

        double[] h = srcV.clone();

        hessenberg(h, vecV, size);
        hessenbergToSchur(h, vecV, valV, size);
    }

    /**
     * Computes the reduction to Hessenberg form.
     * 
     * @param h
     *            the working Hessenberg matrix.
     * @param vecV
     *            the eigenvectors.
     * @param size
     *            the matrix size.
     */
    final protected static void hessenberg(double[] h, double[] vecV, int size) {

        int hStrideRow = size;
        int vStrideRow = size;

        double[] ort = new double[size];

        // This is derived from the Algol procedures orthes and ortran,
        // by Martin and Wilkinson, Handbook for Auto. Comp.,
        // Vol.ii-Linear Algebra, and the corresponding
        // Fortran subroutines in EISPACK.

        int low = 0;
        int high = size - 1;

        for (int m = low + 1; m <= high - 1; m++) {

            // Scale column.

            double scale = 0.0;
            for (int i = m; i <= high; i++) {
                scale = scale + Math.abs(h[hStrideRow * (i) + (m - 1)]);
            }
            if (scale != 0.0) {

                // Compute Householder transformation.

                double hAcc = 0.0;
                for (int i = high; i >= m; i--) {
                    ort[i] = h[hStrideRow * (i) + (m - 1)] / scale;
                    hAcc += ort[i] * ort[i];
                }
                double g = Math.sqrt(hAcc);
                if (ort[m] > 0) {
                    g = -g;
                }
                hAcc = hAcc - ort[m] * g;
                ort[m] = ort[m] - g;

                // Apply Householder similarity transformation
                // H = (I-u*u'/h)*H*(I-u*u')/h)

                for (int j = m; j < size; j++) {
                    double f = 0.0;
                    for (int i = high; i >= m; i--) {
                        f += ort[i] * h[hStrideRow * (i) + (j)];
                    }
                    f = f / hAcc;
                    for (int i = m; i <= high; i++) {
                        h[hStrideRow * (i) + (j)] -= f * ort[i];
                    }
                }

                for (int i = 0; i <= high; i++) {
                    double f = 0.0;
                    for (int j = high; j >= m; j--) {
                        f += ort[j] * h[hStrideRow * (i) + (j)];
                    }
                    f = f / hAcc;
                    for (int j = m; j <= high; j++) {
                        h[hStrideRow * (i) + (j)] -= f * ort[j];
                    }
                }
                ort[m] = scale * ort[m];
                h[hStrideRow * (m) + (m - 1)] = scale * g;
            }
        }

        // Accumulate transformations (Algol's ortran).

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                vecV[vStrideRow * (i) + (j)] = (i == j ? 1.0 : 0.0);
            }
        }

        for (int m = high - 1; m >= low + 1; m--) {
            if (h[hStrideRow * (m) + (m - 1)] != 0.0) {
                for (int i = m + 1; i <= high; i++) {
                    ort[i] = h[hStrideRow * (i) + (m - 1)];
                }
                for (int j = m; j <= high; j++) {
                    double g = 0.0;
                    for (int i = m; i <= high; i++) {
                        g += ort[i] * vecV[vStrideRow * (i) + (j)];
                    }
                    // Double division avoids possible underflow
                    g = (g / ort[m]) / h[hStrideRow * (m) + (m - 1)];
                    for (int i = m; i <= high; i++) {
                        vecV[vStrideRow * (i) + (j)] += g * ort[i];
                    }
                }
            }
        }
    }

    /**
     * Computes the reduction from Hessenberg form to real Schur form.
     * 
     * @param h
     *            the working Hessenberg matrix.
     * @param vecV
     *            the eigenvectors.
     * @param valV
     *            the eigenvalues.
     * @param size
     *            the matrix size.
     */
    final protected static void hessenbergToSchur(double[] h, double[] vecV, double[] valV, int size) {

        int hStrideRow = size;
        int vStrideRow = size;

        double[] cdiv = new double[2];

        // This is derived from the Algol procedure hqr2,
        // by Martin and Wilkinson, Handbook for Auto. Comp.,
        // Vol.ii-Linear Algebra, and the corresponding
        // Fortran subroutine in EISPACK.

        // Initialize

        int nn = size;
        int n = nn - 1;
        int low = 0;
        int high = nn - 1;
        double eps = Math.pow(2.0, -52.0);
        double exShift = 0.0;
        double p = 0, q = 0, r = 0, s = 0, z = 0, t, w, x, y;

        // Store roots isolated by balanc and compute matrix norm

        double norm = 0.0;
        for (int i = 0; i < nn; i++) {
            if (i < low || i > high) {
                valV[2 * (i)] = h[hStrideRow * (i) + (i)];
                valV[2 * (i) + 1] = 0.0;
            }
            for (int j = Math.max(i - 1, 0); j < nn; j++) {
                norm = norm + Math.abs(h[hStrideRow * (i) + (j)]);
            }
        }

        // Outer loop over eigenvalue index

        int iter = 0;
        while (n >= low) {

            // Look for single small sub-diagonal element

            int l = n;
            while (l > low) {
                s = Math.abs(h[hStrideRow * (l - 1) + (l - 1)]) + Math.abs(h[hStrideRow * (l) + (l)]);
                if (s == 0.0) {
                    s = norm;
                }
                if (Math.abs(h[hStrideRow * (l) + (l - 1)]) < eps * s) {
                    break;
                }
                l--;
            }

            // Check for convergence
            // One root found

            if (l == n) {
                h[hStrideRow * (n) + (n)] = h[hStrideRow * (n) + (n)] + exShift;
                valV[2 * (n)] = h[hStrideRow * (n) + (n)];
                valV[2 * (n) + 1] = 0.0;
                n--;
                iter = 0;

                // Two roots found

            } else if (l == n - 1) {
                w = h[hStrideRow * (n) + (n - 1)] * h[hStrideRow * (n - 1) + (n)];
                p = (h[hStrideRow * (n - 1) + (n - 1)] - h[hStrideRow * (n) + (n)]) / 2.0;
                q = p * p + w;
                z = Math.sqrt(Math.abs(q));
                h[hStrideRow * (n) + (n)] = h[hStrideRow * (n) + (n)] + exShift;
                h[hStrideRow * (n - 1) + (n - 1)] = h[hStrideRow * (n - 1) + (n - 1)] + exShift;
                x = h[hStrideRow * (n) + (n)];

                // Real pair

                if (q >= 0) {
                    if (p >= 0) {
                        z = p + z;
                    } else {
                        z = p - z;
                    }
                    valV[2 * (n - 1)] = x + z;
                    valV[2 * (n)] = valV[2 * (n - 1)];
                    if (z != 0.0) {
                        valV[2 * (n)] = x - w / z;
                    }
                    valV[2 * (n - 1) + 1] = 0.0;
                    valV[2 * (n) + 1] = 0.0;
                    x = h[hStrideRow * (n) + (n - 1)];
                    s = Math.abs(x) + Math.abs(z);
                    p = x / s;
                    q = z / s;
                    r = Math.sqrt(p * p + q * q);
                    p = p / r;
                    q = q / r;

                    // Row modification

                    for (int j = n - 1; j < nn; j++) {
                        z = h[hStrideRow * (n - 1) + (j)];
                        h[hStrideRow * (n - 1) + (j)] = q * z + p * h[hStrideRow * (n) + (j)];
                        h[hStrideRow * (n) + (j)] = q * h[hStrideRow * (n) + (j)] - p * z;
                    }

                    // Column modification

                    for (int i = 0; i <= n; i++) {
                        z = h[hStrideRow * (i) + (n - 1)];
                        h[hStrideRow * (i) + (n - 1)] = q * z + p * h[hStrideRow * (i) + (n)];
                        h[hStrideRow * (i) + (n)] = q * h[hStrideRow * (i) + (n)] - p * z;
                    }

                    // Accumulate transformations

                    for (int i = low; i <= high; i++) {
                        z = vecV[vStrideRow * (i) + (n - 1)];
                        vecV[vStrideRow * (i) + (n - 1)] = q * z + p * vecV[vStrideRow * (i) + (n)];
                        vecV[vStrideRow * (i) + (n)] = q * vecV[vStrideRow * (i) + (n)] - p * z;
                    }

                    // Complex pair

                } else {
                    valV[2 * (n - 1)] = x + p;
                    valV[2 * (n)] = x + p;
                    valV[2 * (n - 1) + 1] = z;
                    valV[2 * (n) + 1] = -z;
                }
                n = n - 2;
                iter = 0;

                // No convergence yet

            } else {

                // Form shift

                x = h[hStrideRow * (n) + (n)];
                y = 0.0;
                w = 0.0;
                if (l < n) {
                    y = h[hStrideRow * (n - 1) + (n - 1)];
                    w = h[hStrideRow * (n) + (n - 1)] * h[hStrideRow * (n - 1) + (n)];
                }

                // Wilkinson's original ad hoc shift

                if (iter == 10) {
                    exShift += x;
                    for (int i = low; i <= n; i++) {
                        h[hStrideRow * (i) + (i)] -= x;
                    }
                    s = Math.abs(h[hStrideRow * (n) + (n - 1)]) + Math.abs(h[hStrideRow * (n - 1) + (n - 2)]);
                    x = y = 0.75 * s;
                    w = -0.4375 * s * s;
                }

                // MATLAB's new ad hoc shift

                if (iter == 30) {
                    s = (y - x) / 2.0;
                    s = s * s + w;
                    if (s > 0) {
                        s = Math.sqrt(s);
                        if (y < x) {
                            s = -s;
                        }
                        s = x - w / ((y - x) / 2.0 + s);
                        for (int i = low; i <= n; i++) {
                            h[hStrideRow * (i) + (i)] -= s;
                        }
                        exShift += s;
                        x = y = w = 0.964;
                    }
                }

                iter = iter + 1; // (Could check iteration count here.)

                // Look for two consecutive small sub-diagonal elements

                int m = n - 2;
                while (m >= l) {
                    z = h[hStrideRow * (m) + (m)];
                    r = x - z;
                    s = y - z;
                    p = (r * s - w) / h[hStrideRow * (m + 1) + (m)] + h[hStrideRow * (m) + (m + 1)];
                    q = h[hStrideRow * (m + 1) + (m + 1)] - z - r - s;
                    r = h[hStrideRow * (m + 2) + (m + 1)];
                    s = Math.abs(p) + Math.abs(q) + Math.abs(r);
                    p = p / s;
                    q = q / s;
                    r = r / s;
                    if (m == l) {
                        break;
                    }
                    if (Math.abs(h[hStrideRow * (m) + (m - 1)]) * (Math.abs(q) + Math.abs(r)) < eps
                            * (Math.abs(p) * (Math.abs(h[hStrideRow * (m - 1) + (m - 1)]) + Math.abs(z) + Math
                                    .abs(h[hStrideRow * (m + 1) + (m + 1)])))) {
                        break;
                    }
                    m--;
                }

                for (int i = m + 2; i <= n; i++) {
                    h[hStrideRow * (i) + (i - 2)] = 0.0;
                    if (i > m + 2) {
                        h[hStrideRow * (i) + (i - 3)] = 0.0;
                    }
                }

                // Double QR step involving rows l:n and columns m:n

                for (int k = m; k <= n - 1; k++) {
                    boolean notLast = (k != n - 1);
                    if (k != m) {
                        p = h[hStrideRow * (k) + (k - 1)];
                        q = h[hStrideRow * (k + 1) + (k - 1)];
                        r = (notLast ? h[hStrideRow * (k + 2) + (k - 1)] : 0.0);
                        x = Math.abs(p) + Math.abs(q) + Math.abs(r);
                        if (x != 0.0) {
                            p = p / x;
                            q = q / x;
                            r = r / x;
                        }
                    }
                    if (x == 0.0) {
                        break;
                    }
                    s = Math.sqrt(p * p + q * q + r * r);
                    if (p < 0) {
                        s = -s;
                    }
                    if (s != 0) {
                        if (k != m) {
                            h[hStrideRow * (k) + (k - 1)] = -s * x;
                        } else if (l != m) {
                            h[hStrideRow * (k) + (k - 1)] = -h[hStrideRow * (k) + (k - 1)];
                        }
                        p = p + s;
                        x = p / s;
                        y = q / s;
                        z = r / s;
                        q = q / p;
                        r = r / p;

                        // Row modification

                        for (int j = k; j < nn; j++) {
                            p = h[hStrideRow * (k) + (j)] + q * h[hStrideRow * (k + 1) + (j)];
                            if (notLast) {
                                p = p + r * h[hStrideRow * (k + 2) + (j)];
                                h[hStrideRow * (k + 2) + (j)] = h[hStrideRow * (k + 2) + (j)] - p * z;
                            }
                            h[hStrideRow * (k) + (j)] = h[hStrideRow * (k) + (j)] - p * x;
                            h[hStrideRow * (k + 1) + (j)] = h[hStrideRow * (k + 1) + (j)] - p * y;
                        }

                        // Column modification

                        for (int i = 0; i <= Math.min(n, k + 3); i++) {
                            p = x * h[hStrideRow * (i) + (k)] + y * h[hStrideRow * (i) + (k + 1)];
                            if (notLast) {
                                p = p + z * h[hStrideRow * (i) + (k + 2)];
                                h[hStrideRow * (i) + (k + 2)] = h[hStrideRow * (i) + (k + 2)] - p * r;
                            }
                            h[hStrideRow * (i) + (k)] = h[hStrideRow * (i) + (k)] - p;
                            h[hStrideRow * (i) + (k + 1)] = h[hStrideRow * (i) + (k + 1)] - p * q;
                        }

                        // Accumulate transformations

                        for (int i = low; i <= high; i++) {
                            p = x * vecV[vStrideRow * (i) + (k)] + y * vecV[vStrideRow * (i) + (k + 1)];
                            if (notLast) {
                                p = p + z * vecV[vStrideRow * (i) + (k + 2)];
                                vecV[vStrideRow * (i) + (k + 2)] = vecV[vStrideRow * (i) + (k + 2)] - p * r;
                            }
                            vecV[vStrideRow * (i) + (k)] = vecV[vStrideRow * (i) + (k)] - p;
                            vecV[vStrideRow * (i) + (k + 1)] = vecV[vStrideRow * (i) + (k + 1)] - p * q;
                        }
                    } // (s != 0)
                } // k loop
            } // check convergence
        } // while (n >= low)

        // Backsubstitute to find vectors of upper triangular form

        if (norm == 0.0) {
            return;
        }

        for (n = nn - 1; n >= 0; n--) {
            p = valV[2 * (n)];
            q = valV[2 * (n) + 1];

            // Real vector

            if (q == 0) {
                int l = n;
                h[hStrideRow * (n) + (n)] = 1.0;
                for (int i = n - 1; i >= 0; i--) {
                    w = h[hStrideRow * (i) + (i)] - p;
                    r = 0.0;
                    for (int j = l; j <= n; j++) {
                        r = r + h[hStrideRow * (i) + (j)] * h[hStrideRow * (j) + (n)];
                    }
                    if (valV[2 * (i) + 1] < 0.0) {
                        z = w;
                        s = r;
                    } else {
                        l = i;
                        if (valV[2 * (i) + 1] == 0.0) {
                            if (w != 0.0) {
                                h[hStrideRow * (i) + (n)] = -r / w;
                            } else {
                                h[hStrideRow * (i) + (n)] = -r / (eps * norm);
                            }

                            // Solve real equations

                        } else {
                            x = h[hStrideRow * (i) + (i + 1)];
                            y = h[hStrideRow * (i + 1) + (i)];
                            q = (valV[2 * (i)] - p) * (valV[2 * (i)] - p) + valV[2 * (i) + 1] * valV[2 * (i) + 1];
                            t = (x * s - z * r) / q;
                            h[hStrideRow * (i) + (n)] = t;
                            if (Math.abs(x) > Math.abs(z)) {
                                h[hStrideRow * (i + 1) + (n)] = (-r - w * t) / x;
                            } else {
                                h[hStrideRow * (i + 1) + (n)] = (-s - y * t) / z;
                            }
                        }

                        // Overflow control

                        t = Math.abs(h[hStrideRow * (i) + (n)]);
                        if ((eps * t) * t > 1) {
                            for (int j = i; j <= n; j++) {
                                h[hStrideRow * (j) + (n)] = h[hStrideRow * (j) + (n)] / t;
                            }
                        }
                    }
                }

                // Complex vector

            } else if (q < 0) {
                int l = n - 1;

                // Last vector component imaginary so matrix is triangular

                if (Math.abs(h[hStrideRow * (n) + (n - 1)]) > Math.abs(h[hStrideRow * (n - 1) + (n)])) {
                    h[hStrideRow * (n - 1) + (n - 1)] = q / h[hStrideRow * (n) + (n - 1)];
                    h[hStrideRow * (n - 1) + (n)] = -(h[hStrideRow * (n) + (n)] - p) / h[hStrideRow * (n) + (n - 1)];
                } else {
                    CEDivOp.op(cdiv, //
                            0.0, -h[hStrideRow * (n - 1) + (n)], //
                            h[hStrideRow * (n - 1) + (n - 1)] - p, q);
                    h[hStrideRow * (n - 1) + (n - 1)] = cdiv[0];
                    h[hStrideRow * (n - 1) + (n)] = cdiv[1];
                }
                h[hStrideRow * (n) + (n - 1)] = 0.0;
                h[hStrideRow * (n) + (n)] = 1.0;
                for (int i = n - 2; i >= 0; i--) {
                    double ra, sa, vr, vi;
                    ra = 0.0;
                    sa = 0.0;
                    for (int j = l; j <= n; j++) {
                        ra = ra + h[hStrideRow * (i) + (j)] * h[hStrideRow * (j) + (n - 1)];
                        sa = sa + h[hStrideRow * (i) + (j)] * h[hStrideRow * (j) + (n)];
                    }
                    w = h[hStrideRow * (i) + (i)] - p;

                    if (valV[2 * (i) + 1] < 0.0) {
                        z = w;
                        r = ra;
                        s = sa;
                    } else {
                        l = i;
                        if (valV[2 * (i) + 1] == 0) {
                            CEDivOp.op(cdiv, //
                                    -ra, -sa, w, q);
                            h[hStrideRow * (i) + (n - 1)] = cdiv[0];
                            h[hStrideRow * (i) + (n)] = cdiv[1];
                        } else {

                            // Solve complex equations

                            x = h[hStrideRow * (i) + (i + 1)];
                            y = h[hStrideRow * (i + 1) + (i)];
                            vr = (valV[2 * (i)] - p) * (valV[2 * (i)] - p) + valV[2 * (i) + 1] * valV[2 * (i) + 1] - q
                                    * q;
                            vi = (valV[2 * (i)] - p) * 2.0 * q;
                            if (vr == 0.0 && vi == 0.0) {
                                vr = eps * norm * (Math.abs(w) + Math.abs(q) + Math.abs(x) + Math.abs(y) + Math.abs(z));
                            }
                            CEDivOp.op(cdiv, //
                                    x * r - z * ra + q * sa, //
                                    x * s - z * sa - q * ra, //
                                    vr, vi);
                            h[hStrideRow * (i) + (n - 1)] = cdiv[0];
                            h[hStrideRow * (i) + (n)] = cdiv[1];
                            if (Math.abs(x) > (Math.abs(z) + Math.abs(q))) {
                                h[hStrideRow * (i + 1) + (n - 1)] = (-ra - w * h[hStrideRow * (i) + (n - 1)] + q
                                        * h[hStrideRow * (i) + (n)])
                                        / x;
                                h[hStrideRow * (i + 1) + (n)] = (-sa - w * h[hStrideRow * (i) + (n)] - q
                                        * h[hStrideRow * (i) + (n - 1)])
                                        / x;
                            } else {
                                CEDivOp.op(cdiv, //
                                        -r - y * h[hStrideRow * (i) + (n - 1)], //
                                        -s - y * h[hStrideRow * (i) + (n)], //
                                        z, q);
                                h[hStrideRow * (i + 1) + (n - 1)] = cdiv[0];
                                h[hStrideRow * (i + 1) + (n)] = cdiv[1];
                            }
                        }

                        // Overflow control

                        t = Math.max(Math.abs(h[hStrideRow * (i) + (n - 1)]), Math.abs(h[hStrideRow * (i) + (n)]));
                        if ((eps * t) * t > 1) {
                            for (int j = i; j <= n; j++) {
                                h[hStrideRow * (j) + (n - 1)] = h[hStrideRow * (j) + (n - 1)] / t;
                                h[hStrideRow * (j) + (n)] = h[hStrideRow * (j) + (n)] / t;
                            }
                        }
                    }
                }
            }
        }

        // Vectors of isolated roots

        for (int i = 0; i < nn; i++) {
            if (i < low || i > high) {
                for (int j = i; j < nn; j++) {
                    vecV[vStrideRow * (i) + (j)] = h[hStrideRow * (i) + (j)];
                }
            }
        }

        // Back transformation to get eigenvectors of original matrix

        for (int j = nn - 1; j >= low; j--) {
            for (int i = low; i <= high; i++) {
                z = 0.0;
                for (int k = low; k <= Math.min(j, high); k++) {
                    z = z + vecV[vStrideRow * (i) + (k)] * h[hStrideRow * (k) + (j)];
                }
                vecV[vStrideRow * (i) + (j)] = z;
            }
        }
    }

    /**
     * A matrix inversion operation in support of {@link JavaArrayKernel#invert(double[], double[], int)}.
     */
    final public static void invert(double[] srcV, double[] dstV, int size) {

        Control.checkTrue(srcV.length == size * size //
                && dstV.length == size * size, //
                "Invalid arguments");

        double[] lu = srcV.clone();
        int[] pivots = Arithmetic.range(size);

        lup(lu, pivots, size, size);

        for (int i = 0; i < size; i++) {

            Control.checkTrue(lu[size * i + i] != 0, //
                    "Matrix is singular");

            dstV[size * i + pivots[i]] = 1.0;
        }

        luSolve(lu, size, dstV, size);
    }

    /**
     * Computes the LU decomposition along with row pivots.
     * 
     * @param lu
     *            the LU matrix.
     * @param pivots
     *            the row pivots.
     * @param nRows
     *            the number of rows.
     * @param nCols
     *            the number of columns.
     */
    final protected static void lup(double[] lu, int[] pivots, int nRows, int nCols) {

        int luStrideRow = nCols;

        // Use a "left-looking", dot-product, Crout/Doolittle algorithm.

        double[] luColJ = new double[nRows];

        // Outer loop.

        for (int j = 0; j < nCols; j++) {

            // Make a copy of the j-th column to localize references.

            for (int i = 0; i < nRows; i++) {
                luColJ[i] = lu[luStrideRow * (i) + (j)];
            }

            // Apply previous transformations.

            for (int i = 0; i < nRows; i++) {

                // Most of the time is spent in the following dot product.

                int kmax = Math.min(i, j);
                double s = 0.0;
                for (int k = 0; k < kmax; k++) {
                    s += lu[luStrideRow * (i) + (k)] * luColJ[k];
                }

                lu[luStrideRow * (i) + (j)] = luColJ[i] -= s;
            }

            // Find pivot and exchange if necessary.

            int p = j;
            for (int i = j + 1; i < nRows; i++) {
                if (Math.abs(luColJ[i]) > Math.abs(luColJ[p])) {
                    p = i;
                }
            }
            if (p != j) {
                for (int k = 0; k < nCols; k++) {
                    double t = lu[luStrideRow * (p) + (k)];
                    lu[luStrideRow * (p) + (k)] = lu[luStrideRow * (j) + (k)];
                    lu[luStrideRow * (j) + (k)] = t;
                }
                int k = pivots[p];
                pivots[p] = pivots[j];
                pivots[j] = k;
            }

            // Compute multipliers.

            if (j < nRows && lu[luStrideRow * (j) + (j)] != 0.0) {
                for (int i = j + 1; i < nRows; i++) {
                    lu[luStrideRow * (i) + (j)] /= lu[luStrideRow * (j) + (j)];
                }
            }
        }
    }

    /**
     * Uses an existing LU decomposition to solve a system of linear equations.
     * 
     * @param lu
     *            the LU matrix.
     * @param nLUCols
     *            the number of columns in the LU matrix.
     * @param dstV
     *            the destination matrix.
     * @param nDstVCols
     *            the number of columns in the destination matrix.
     */
    final protected static void luSolve(double[] lu, int nLUCols, double[] dstV, int nDstVCols) {

        int luStrideRow = nLUCols;
        int vStrideRow = nDstVCols;

        // Solve L*Y = B(piv,:)
        for (int k = 0; k < nLUCols; k++) {
            for (int i = k + 1; i < nLUCols; i++) {
                for (int j = 0; j < nDstVCols; j++) {
                    dstV[vStrideRow * (i) + (j)] -= dstV[vStrideRow * (k) + (j)] * lu[luStrideRow * (i) + (k)];
                }
            }
        }
        // Solve U*X = Y;
        for (int k = nLUCols - 1; k >= 0; k--) {
            for (int j = 0; j < nLUCols; j++) {
                dstV[vStrideRow * (k) + (j)] /= lu[luStrideRow * (k) + (k)];
            }
            for (int i = 0; i < k; i++) {
                for (int j = 0; j < nDstVCols; j++) {
                    dstV[vStrideRow * (i) + (j)] -= dstV[vStrideRow * (k) + (j)] * lu[luStrideRow * (i) + (k)];
                }
            }
        }
    }

    // Dummy constructor.
    LinearAlgebraOps() {
    }
}

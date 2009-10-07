/*
 * This file is part of the Shared Scientific Toolbox in Java ("this library").
 * 
 * Copyright (C) 2009 Roy Liu
 * 
 * This library is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation, either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library. If not, see
 * http://www.gnu.org/licenses/.
 */

#include <LinearAlgebraOps.hpp>

void LinearAlgebraOps::svd(JNIEnv *env, jobject thisObj, //
        jdoubleArray srcV, jint srcStrideRow, jint srcStrideCol, //
        jdoubleArray uV, jdoubleArray sV, jdoubleArray vV, //
        jint nrows, jint ncols) {

    try {

        if (!srcV || !uV || !sV || !vV) {
            throw std::runtime_error("Invalid arguments");
        }

        jint srcLen = env->GetArrayLength(srcV);
        jint uLen = env->GetArrayLength(uV);
        jint sLen = env->GetArrayLength(sV);
        jint vLen = env->GetArrayLength(vV);

        if ((nrows < ncols) //
                || (srcLen != nrows * ncols) //
                || (uLen != nrows * ncols) //
                || (sLen != ncols) //
                || (vLen != ncols * ncols) //
                || !((srcStrideRow == ncols && srcStrideCol == 1) //
                        || (srcStrideRow == 1 && srcStrideCol == nrows))) {
            throw std::runtime_error("Invalid arguments");
        }

        ArrayPinHandler srcVH(env, srcV, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        ArrayPinHandler uVH(env, uV, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_WRITE);
        ArrayPinHandler sVH(env, sV, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_WRITE);
        ArrayPinHandler vVH(env, vV, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_WRITE);

        jdouble *srcVArr = (jdouble *) srcVH.get();
        jdouble *uVArr = (jdouble *) uVH.get();
        jdouble *sVArr = (jdouble *) sVH.get();
        jdouble *vVArr = (jdouble *) vVH.get();

        svd(srcVArr, srcStrideRow, srcStrideCol, srcLen, uVArr, sVArr, vVArr, nrows, ncols);

    } catch (std::exception &e) {

        Common::throwNew(env, e);
    }
}

void LinearAlgebraOps::svd(jdouble *srcVArr, jint srcStrideRow, jint srcStrideCol, jint srcLen, //
        jdouble *uVArr, jdouble *sVArr, jdouble *vVArr, //
        jint nrows, jint ncols) {

    jint uStrideRow = ncols;
    jint vStrideRow = ncols;

    MallocHandler allH(sizeof(jdouble) * (ncols + nrows + srcLen));
    jdouble *all = (jdouble *) allH.get();
    jdouble *e = all;
    jdouble *work = all + ncols;
    jdouble *a = all + ncols + nrows;

    memset(e, 0, sizeof(jdouble) * ncols);
    memset(work, 0, sizeof(jdouble) * nrows);

    memcpy(a, srcVArr, sizeof(jdouble) * srcLen);

    // Reduce A to bidiagonal form, storing the diagonal elements
    // in s and the super-diagonal elements in e.

    jint nct = std::min(nrows - 1, ncols);
    jint nrt = std::max((jint) 0, std::min(ncols - 2, nrows));
    for (jint k = 0, l = std::max(nct, nrt); k < l; k++) {
        if (k < nct) {

            // Compute the transformation for the k-th column and
            // place the k-th diagonal in s[k].
            // Compute 2-norm of k-th column without under/overflow.
            sVArr[k] = 0;
            for (jint i = k; i < nrows; i++) {
                sVArr[k] = jcomplex(sVArr[k], a[srcStrideRow * (i) + srcStrideCol * (k)]).abs();
            }
            if (sVArr[k] != 0.0) {
                if (a[srcStrideRow * (k) + srcStrideCol * (k)] < 0.0) {
                    sVArr[k] = -sVArr[k];
                }
                for (jint i = k; i < nrows; i++) {
                    a[srcStrideRow * (i) + srcStrideCol * (k)] /= sVArr[k];
                }
                a[srcStrideRow * (k) + srcStrideCol * (k)] += 1.0;
            }
            sVArr[k] = -sVArr[k];
        }
        for (jint j = k + 1; j < ncols; j++) {
            if ((k < nct) & (sVArr[k] != 0.0)) {

                // Apply the transformation.

                jdouble t = 0;
                for (jint i = k; i < nrows; i++) {
                    t += a[srcStrideRow * (i) + srcStrideCol * (k)] * a[srcStrideRow * (i) + srcStrideCol * (j)];
                }
                t = -t / a[srcStrideRow * (k) + srcStrideCol * (k)];
                for (jint i = k; i < nrows; i++) {
                    a[srcStrideRow * (i) + srcStrideCol * (j)] += t * a[srcStrideRow * (i) + srcStrideCol * (k)];
                }
            }

            // Place the k-th row of A jinto e for the
            // subsequent calculation of the row transformation.

            e[j] = a[srcStrideRow * (k) + srcStrideCol * (j)];
        }
        if (k < nct) {

            // Place the transformation in U for subsequent back
            // multiplication.

            for (jint i = k; i < nrows; i++) {
                uVArr[uStrideRow * (i) + (k)] = a[srcStrideRow * (i) + srcStrideCol * (k)];
            }
        }
        if (k < nrt) {

            // Compute the k-th row transformation and place the
            // k-th super-diagonal in e[k].
            // Compute 2-norm without under/overflow.
            e[k] = 0;
            for (jint i = k + 1; i < ncols; i++) {
                e[k] = jcomplex(e[k], e[i]).abs();
            }
            if (e[k] != 0.0) {
                if (e[k + 1] < 0.0) {
                    e[k] = -e[k];
                }
                for (jint i = k + 1; i < ncols; i++) {
                    e[i] /= e[k];
                }
                e[k + 1] += 1.0;
            }
            e[k] = -e[k];
            if ((k + 1 < nrows) & (e[k] != 0.0)) {

                // Apply the transformation.

                for (jint i = k + 1; i < nrows; i++) {
                    work[i] = 0.0;
                }
                for (jint j = k + 1; j < ncols; j++) {
                    for (jint i = k + 1; i < nrows; i++) {
                        work[i] += e[j] * a[srcStrideRow * (i) + srcStrideCol * (j)];
                    }
                }
                for (jint j = k + 1; j < ncols; j++) {
                    jdouble t = -e[j] / e[k + 1];
                    for (jint i = k + 1; i < nrows; i++) {
                        a[srcStrideRow * (i) + srcStrideCol * (j)] += t * work[i];
                    }
                }
            }

            // Place the transformation in V for subsequent
            // back multiplication.

            for (jint i = k + 1; i < ncols; i++) {
                vVArr[vStrideRow * (i) + (k)] = e[i];
            }
        }
    }

    // Set up the final bidiagonal matrix or order p.

    jint p = ncols;
    if (nct < ncols) {
        sVArr[nct] = a[srcStrideRow * (nct) + srcStrideCol * (nct)];
    }
    if (nrows < p) {
        sVArr[p - 1] = 0.0;
    }
    if (nrt + 1 < p) {
        e[nrt] = a[srcStrideRow * (nrt) + srcStrideCol * (p - 1)];
    }
    e[p - 1] = 0.0;

    // If required, generate U.

    for (jint j = nct; j < ncols; j++) {
        for (jint i = 0; i < nrows; i++) {
            uVArr[uStrideRow * (i) + (j)] = 0.0;
        }
        uVArr[uStrideRow * (j) + (j)] = 1.0;
    }
    for (jint k = nct - 1; k >= 0; k--) {
        if (sVArr[k] != 0.0) {
            for (jint j = k + 1; j < ncols; j++) {
                jdouble t = 0;
                for (jint i = k; i < nrows; i++) {
                    t += uVArr[uStrideRow * (i) + (k)] * uVArr[uStrideRow * (i) + (j)];
                }
                t = -t / uVArr[uStrideRow * (k) + (k)];
                for (jint i = k; i < nrows; i++) {
                    uVArr[uStrideRow * (i) + (j)] += t * uVArr[uStrideRow * (i) + (k)];
                }
            }
            for (jint i = k; i < nrows; i++) {
                uVArr[uStrideRow * (i) + (k)] = -uVArr[uStrideRow * (i) + (k)];
            }
            uVArr[uStrideRow * (k) + (k)] = 1.0 + uVArr[uStrideRow * (k) + (k)];
            for (jint i = 0; i < k - 1; i++) {
                uVArr[uStrideRow * (i) + (k)] = 0.0;
            }
        } else {
            for (jint i = 0; i < nrows; i++) {
                uVArr[uStrideRow * (i) + (k)] = 0.0;
            }
            uVArr[uStrideRow * (k) + (k)] = 1.0;
        }
    }

    // If required, generate V.

    for (jint k = ncols - 1; k >= 0; k--) {
        if ((k < nrt) & (e[k] != 0.0)) {
            for (jint j = k + 1; j < ncols; j++) {
                jdouble t = 0;
                for (jint i = k + 1; i < ncols; i++) {
                    t += vVArr[vStrideRow * (i) + (k)] * vVArr[vStrideRow * (i) + (j)];
                }
                t = -t / vVArr[vStrideRow * (k + 1) + (k)];
                for (jint i = k + 1; i < ncols; i++) {
                    vVArr[vStrideRow * (i) + (j)] += t * vVArr[vStrideRow * (i) + (k)];
                }
            }
        }
        for (jint i = 0; i < ncols; i++) {
            vVArr[vStrideRow * (i) + (k)] = 0.0;
        }
        vVArr[vStrideRow * (k) + (k)] = 1.0;
    }

    // Main iteration loop for the singular values.

    jint pp = p - 1;
    jint iter = 0;
    jdouble eps = pow(2.0, -52.0);
    jdouble tiny = pow(2.0, -966.0);
    while (p > 0) {
        jint k, kase;

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
            if (fabs(e[k]) <= tiny + eps * (fabs(sVArr[k]) + fabs(sVArr[k + 1]))) {
                e[k] = 0.0;
                break;
            }
        }
        if (k == p - 2) {
            kase = 4;
        } else {
            jint ks;
            for (ks = p - 1; ks >= k; ks--) {
                if (ks == k) {
                    break;
                }
                jdouble t = (ks != p ? fabs(e[ks]) : 0.) + (ks != k + 1 ? fabs(e[ks - 1]) : 0.);
                if (fabs(sVArr[ks]) <= tiny + eps * t) {
                    sVArr[ks] = 0.0;
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
            jdouble f = e[p - 2];
            e[p - 2] = 0.0;
            for (jint j = p - 2; j >= k; j--) {
                jdouble t = jcomplex(sVArr[j], f).abs();
                jdouble cs = sVArr[j] / t;
                jdouble sn = f / t;
                sVArr[j] = t;
                if (j != k) {
                    f = -sn * e[j - 1];
                    e[j - 1] = cs * e[j - 1];
                }
                for (jint i = 0; i < ncols; i++) {
                    t = cs * vVArr[vStrideRow * (i) + (j)] + sn * vVArr[vStrideRow * (i) + (p - 1)];
                    vVArr[vStrideRow * (i) + (p - 1)] = -sn * vVArr[vStrideRow * (i) + (j)] + cs * vVArr[vStrideRow
                            * (i) + (p - 1)];
                    vVArr[vStrideRow * (i) + (j)] = t;
                }
            }
        }
            break;

            // Split at negligible s(k).

        case 2: {
            jdouble f = e[k - 1];
            e[k - 1] = 0.0;
            for (jint j = k; j < p; j++) {
                jdouble t = jcomplex(sVArr[j], f).abs();
                jdouble cs = sVArr[j] / t;
                jdouble sn = f / t;
                sVArr[j] = t;
                f = -sn * e[j];
                e[j] = cs * e[j];
                for (jint i = 0; i < nrows; i++) {
                    t = cs * uVArr[uStrideRow * (i) + (j)] + sn * uVArr[uStrideRow * (i) + (k - 1)];
                    uVArr[uStrideRow * (i) + (k - 1)] = -sn * uVArr[uStrideRow * (i) + (j)] + cs * uVArr[uStrideRow
                            * (i) + (k - 1)];
                    uVArr[uStrideRow * (i) + (j)] = t;
                }
            }
        }
            break;

            // Perform one qr step.

        case 3: {

            // Calculate the shift.

            jdouble scale = std::max(std::max(
                    std::max(std::max(fabs(sVArr[p - 1]), fabs(sVArr[p - 2])), fabs(e[p - 2])), fabs(sVArr[k])), fabs(
                    e[k]));
            jdouble sp = sVArr[p - 1] / scale;
            jdouble spm1 = sVArr[p - 2] / scale;
            jdouble epm1 = e[p - 2] / scale;
            jdouble sk = sVArr[k] / scale;
            jdouble ek = e[k] / scale;
            jdouble b = ((spm1 + sp) * (spm1 - sp) + epm1 * epm1) / 2.0;
            jdouble c = (sp * epm1) * (sp * epm1);
            jdouble shift = 0.0;
            if ((b != 0.0) | (c != 0.0)) {
                shift = sqrt(b * b + c);
                if (b < 0.0) {
                    shift = -shift;
                }
                shift = c / (b + shift);
            }
            jdouble f = (sk + sp) * (sk - sp) + shift;
            jdouble g = sk * ek;

            // Chase zeros.

            for (jint j = k; j < p - 1; j++) {
                jdouble t = jcomplex(f, g).abs();
                jdouble cs = f / t;
                jdouble sn = g / t;
                if (j != k) {
                    e[j - 1] = t;
                }
                f = cs * sVArr[j] + sn * e[j];
                e[j] = cs * e[j] - sn * sVArr[j];
                g = sn * sVArr[j + 1];
                sVArr[j + 1] = cs * sVArr[j + 1];
                for (jint i = 0; i < ncols; i++) {
                    t = cs * vVArr[vStrideRow * (i) + (j)] + sn * vVArr[vStrideRow * (i) + (j + 1)];
                    vVArr[vStrideRow * (i) + (j + 1)] = -sn * vVArr[vStrideRow * (i) + (j)] + cs * vVArr[vStrideRow
                            * (i) + (j + 1)];
                    vVArr[vStrideRow * (i) + (j)] = t;
                }
                t = jcomplex(f, g).abs();
                cs = f / t;
                sn = g / t;
                sVArr[j] = t;
                f = cs * e[j] + sn * sVArr[j + 1];
                sVArr[j + 1] = -sn * e[j] + cs * sVArr[j + 1];
                g = sn * e[j + 1];
                e[j + 1] = cs * e[j + 1];
                if (j < nrows - 1) {
                    for (jint i = 0; i < nrows; i++) {
                        t = cs * uVArr[uStrideRow * (i) + (j)] + sn * uVArr[uStrideRow * (i) + (j + 1)];
                        uVArr[uStrideRow * (i) + (j + 1)] = -sn * uVArr[uStrideRow * (i) + (j)] + cs * uVArr[uStrideRow
                                * (i) + (j + 1)];
                        uVArr[uStrideRow * (i) + (j)] = t;
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

            if (sVArr[k] <= 0.0) {
                sVArr[k] = (sVArr[k] < 0.0 ? -sVArr[k] : 0.0);
                for (jint i = 0; i <= pp; i++) {
                    vVArr[vStrideRow * (i) + (k)] = -vVArr[vStrideRow * (i) + (k)];
                }
            }

            // Order the singular values.

            while (k < pp) {
                if (sVArr[k] >= sVArr[k + 1]) {
                    break;
                }
                jdouble t = sVArr[k];
                sVArr[k] = sVArr[k + 1];
                sVArr[k + 1] = t;
                if (k < ncols - 1) {
                    for (jint i = 0; i < ncols; i++) {
                        t = vVArr[vStrideRow * (i) + (k + 1)];
                        vVArr[vStrideRow * (i) + (k + 1)] = vVArr[vStrideRow * (i) + (k)];
                        vVArr[vStrideRow * (i) + (k)] = t;
                    }
                }
                if (k < nrows - 1) {
                    for (jint i = 0; i < nrows; i++) {
                        t = uVArr[uStrideRow * (i) + (k + 1)];
                        uVArr[uStrideRow * (i) + (k + 1)] = uVArr[uStrideRow * (i) + (k)];
                        uVArr[uStrideRow * (i) + (k)] = t;
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
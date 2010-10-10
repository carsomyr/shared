/*
 * Copyright (c) 2009 Roy Liu
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * 
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 *     disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 *     following disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the author nor the names of any contributors may be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

#include <LinearAlgebraOps.hpp>

void LinearAlgebraOps::eigs(JNIEnv *env, jobject thisObj, //
        jdoubleArray srcV, jdoubleArray vecV, jdoubleArray valV, jint size) {

    try {

        if (!srcV || !vecV || !valV) {
            throw std::runtime_error("Invalid arguments");
        }

        jint srcLen = env->GetArrayLength(srcV);
        jint vecLen = env->GetArrayLength(vecV);
        jint valLen = env->GetArrayLength(valV);

        if ((srcLen != size * size) || (vecLen != size * size) || (valLen != 2 * size)) {
            throw std::runtime_error("Invalid arguments");
        }

        ArrayPinHandler srcVH(env, srcV, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        ArrayPinHandler vecVH(env, vecV, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_WRITE);
        ArrayPinHandler valVH(env, valV, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_WRITE);

        jdouble *srcVArr = (jdouble *) srcVH.get();
        jdouble *vecVArr = (jdouble *) vecVH.get();
        jdouble *valVArr = (jdouble *) valVH.get();

        MallocHandler allH(sizeof(jdouble) * srcLen);
        jdouble *all = (jdouble *) allH.get();
        jdouble *h = all;

        memcpy(h, srcVArr, sizeof(jdouble) * srcLen);

        hessenberg(h, vecVArr, size);
        hessenbergToSchur(h, vecVArr, valVArr, size);

    } catch (std::exception &e) {

        Common::throwNew(env, e);
    }
}

void LinearAlgebraOps::hessenberg(jdouble *h, jdouble *vecVArr, jint size) {

    jint hStrideRow = size;
    jint vStrideRow = size;

    MallocHandler allH(sizeof(jdouble) * size);
    jdouble *all = (jdouble *) allH.get();
    jdouble *ort = all;

    memset(ort, 0, sizeof(jdouble) * size);

    // This is derived from the Algol procedures orthes and ortran,
    // by Martin and Wilkinson, Handbook for Auto. Comp.,
    // Vol.ii-Linear Algebra, and the corresponding
    // Fortran subroutines in EISPACK.

    jint low = 0;
    jint high = size - 1;

    for (jint m = low + 1; m <= high - 1; m++) {

        // Scale column.

        jdouble scale = 0.0;
        for (jint i = m; i <= high; i++) {
            scale = scale + fabs(h[hStrideRow * (i) + (m - 1)]);
        }
        if (scale != 0.0) {

            // Compute Householder transformation.

            jdouble hAcc = 0.0;
            for (jint i = high; i >= m; i--) {
                ort[i] = h[hStrideRow * (i) + (m - 1)] / scale;
                hAcc += ort[i] * ort[i];
            }
            jdouble g = sqrt(hAcc);
            if (ort[m] > 0) {
                g = -g;
            }
            hAcc = hAcc - ort[m] * g;
            ort[m] = ort[m] - g;

            // Apply Householder similarity transformation
            // H = (I-u*u'/h)*H*(I-u*u')/h)

            for (jint j = m; j < size; j++) {
                jdouble f = 0.0;
                for (jint i = high; i >= m; i--) {
                    f += ort[i] * h[hStrideRow * (i) + (j)];
                }
                f = f / hAcc;
                for (jint i = m; i <= high; i++) {
                    h[hStrideRow * (i) + (j)] -= f * ort[i];
                }
            }

            for (jint i = 0; i <= high; i++) {
                jdouble f = 0.0;
                for (jint j = high; j >= m; j--) {
                    f += ort[j] * h[hStrideRow * (i) + (j)];
                }
                f = f / hAcc;
                for (jint j = m; j <= high; j++) {
                    h[hStrideRow * (i) + (j)] -= f * ort[j];
                }
            }
            ort[m] = scale * ort[m];
            h[hStrideRow * (m) + (m - 1)] = scale * g;
        }
    }

    // Accumulate transformations (Algol's ortran).

    for (jint i = 0; i < size; i++) {
        for (jint j = 0; j < size; j++) {
            vecVArr[vStrideRow * (i) + (j)] = (i == j ? 1.0 : 0.0);
        }
    }

    for (jint m = high - 1; m >= low + 1; m--) {
        if (h[hStrideRow * (m) + (m - 1)] != 0.0) {
            for (jint i = m + 1; i <= high; i++) {
                ort[i] = h[hStrideRow * (i) + (m - 1)];
            }
            for (jint j = m; j <= high; j++) {
                jdouble g = 0.0;
                for (jint i = m; i <= high; i++) {
                    g += ort[i] * vecVArr[vStrideRow * (i) + (j)];
                }
                // Double division avoids possible underflow
                g = (g / ort[m]) / h[hStrideRow * (m) + (m - 1)];
                for (jint i = m; i <= high; i++) {
                    vecVArr[vStrideRow * (i) + (j)] += g * ort[i];
                }
            }
        }
    }
}

void LinearAlgebraOps::hessenbergToSchur(jdouble *h, jdouble *vecVArr, jdouble *valVArr, jint size) {

    jint hStrideRow = size;
    jint vStrideRow = size;

    jcomplex cDiv = jcomplex(0.0, 0.0);

    // This is derived from the Algol procedure hqr2,
    // by Martin and Wilkinson, Handbook for Auto. Comp.,
    // Vol.ii-Linear Algebra, and the corresponding
    // Fortran subroutine in EISPACK.

    // Initialize

    jint nn = size;
    jint n = nn - 1;
    jint low = 0;
    jint high = nn - 1;
    jdouble eps = pow(2.0, -52.0);
    jdouble exShift = 0.0;
    jdouble p = 0, q = 0, r = 0, s = 0, z = 0, t, w, x, y;

    // Store roots isolated by balanc and compute matrix norm

    jdouble norm = 0.0;
    for (jint i = 0; i < nn; i++) {
        if (i < low || i > high) {
            valVArr[2 * (i)] = h[hStrideRow * (i) + (i)];
            valVArr[2 * (i) + 1] = 0.0;
        }
        for (jint j = std::max(i - 1, (jint) 0); j < nn; j++) {
            norm = norm + fabs(h[hStrideRow * (i) + (j)]);
        }
    }

    // Outer loop over eigenvalue index

    jint iter = 0;
    while (n >= low) {

        // Look for single small sub-diagonal element

        jint l = n;
        while (l > low) {
            s = fabs(h[hStrideRow * (l - 1) + (l - 1)]) + fabs(h[hStrideRow * (l) + (l)]);
            if (s == 0.0) {
                s = norm;
            }
            if (fabs(h[hStrideRow * (l) + (l - 1)]) < eps * s) {
                break;
            }
            l--;
        }

        // Check for convergence
        // One root found

        if (l == n) {
            h[hStrideRow * (n) + (n)] = h[hStrideRow * (n) + (n)] + exShift;
            valVArr[2 * (n)] = h[hStrideRow * (n) + (n)];
            valVArr[2 * (n) + 1] = 0.0;
            n--;
            iter = 0;

            // Two roots found

        } else if (l == n - 1) {
            w = h[hStrideRow * (n) + (n - 1)] * h[hStrideRow * (n - 1) + (n)];
            p = (h[hStrideRow * (n - 1) + (n - 1)] - h[hStrideRow * (n) + (n)]) / 2.0;
            q = p * p + w;
            z = sqrt(fabs(q));
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
                valVArr[2 * (n - 1)] = x + z;
                valVArr[2 * (n)] = valVArr[2 * (n - 1)];
                if (z != 0.0) {
                    valVArr[2 * (n)] = x - w / z;
                }
                valVArr[2 * (n - 1) + 1] = 0.0;
                valVArr[2 * (n) + 1] = 0.0;
                x = h[hStrideRow * (n) + (n - 1)];
                s = fabs(x) + fabs(z);
                p = x / s;
                q = z / s;
                r = sqrt(p * p + q * q);
                p = p / r;
                q = q / r;

                // Row modification

                for (jint j = n - 1; j < nn; j++) {
                    z = h[hStrideRow * (n - 1) + (j)];
                    h[hStrideRow * (n - 1) + (j)] = q * z + p * h[hStrideRow * (n) + (j)];
                    h[hStrideRow * (n) + (j)] = q * h[hStrideRow * (n) + (j)] - p * z;
                }

                // Column modification

                for (jint i = 0; i <= n; i++) {
                    z = h[hStrideRow * (i) + (n - 1)];
                    h[hStrideRow * (i) + (n - 1)] = q * z + p * h[hStrideRow * (i) + (n)];
                    h[hStrideRow * (i) + (n)] = q * h[hStrideRow * (i) + (n)] - p * z;
                }

                // Accumulate transformations

                for (jint i = low; i <= high; i++) {
                    z = vecVArr[vStrideRow * (i) + (n - 1)];
                    vecVArr[vStrideRow * (i) + (n - 1)] = q * z + p * vecVArr[vStrideRow * (i) + (n)];
                    vecVArr[vStrideRow * (i) + (n)] = q * vecVArr[vStrideRow * (i) + (n)] - p * z;
                }

                // Complex pair

            } else {
                valVArr[2 * (n - 1)] = x + p;
                valVArr[2 * (n)] = x + p;
                valVArr[2 * (n - 1) + 1] = z;
                valVArr[2 * (n) + 1] = -z;
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
                for (jint i = low; i <= n; i++) {
                    h[hStrideRow * (i) + (i)] -= x;
                }
                s = fabs(h[hStrideRow * (n) + (n - 1)]) + fabs(h[hStrideRow * (n - 1) + (n - 2)]);
                x = y = 0.75 * s;
                w = -0.4375 * s * s;
            }

            // MATLAB's new ad hoc shift

            if (iter == 30) {
                s = (y - x) / 2.0;
                s = s * s + w;
                if (s > 0) {
                    s = sqrt(s);
                    if (y < x) {
                        s = -s;
                    }
                    s = x - w / ((y - x) / 2.0 + s);
                    for (jint i = low; i <= n; i++) {
                        h[hStrideRow * (i) + (i)] -= s;
                    }
                    exShift += s;
                    x = y = w = 0.964;
                }
            }

            iter = iter + 1; // (Could check iteration count here.)

            // Look for two consecutive small sub-diagonal elements

            jint m = n - 2;
            while (m >= l) {
                z = h[hStrideRow * (m) + (m)];
                r = x - z;
                s = y - z;
                p = (r * s - w) / h[hStrideRow * (m + 1) + (m)] + h[hStrideRow * (m) + (m + 1)];
                q = h[hStrideRow * (m + 1) + (m + 1)] - z - r - s;
                r = h[hStrideRow * (m + 2) + (m + 1)];
                s = fabs(p) + fabs(q) + fabs(r);
                p = p / s;
                q = q / s;
                r = r / s;
                if (m == l) {
                    break;
                }
                if (fabs(h[hStrideRow * (m) + (m - 1)]) * (fabs(q) + fabs(r))
                        < eps * (fabs(p) * (fabs(h[hStrideRow * (m - 1) + (m - 1)])
                                + fabs(z)
                                + fabs(h[hStrideRow * (m + 1) + (m + 1)])))) {
                    break;
                }
                m--;
            }

            for (jint i = m + 2; i <= n; i++) {
                h[hStrideRow * (i) + (i - 2)] = 0.0;
                if (i > m + 2) {
                    h[hStrideRow * (i) + (i - 3)] = 0.0;
                }
            }

            // Double QR step involving rows l:n and columns m:n

            for (jint k = m; k <= n - 1; k++) {
                jboolean notLast = (k != n - 1);
                if (k != m) {
                    p = h[hStrideRow * (k) + (k - 1)];
                    q = h[hStrideRow * (k + 1) + (k - 1)];
                    r = (notLast ? h[hStrideRow * (k + 2) + (k - 1)] : 0.0);
                    x = fabs(p) + fabs(q) + fabs(r);
                    if (x != 0.0) {
                        p = p / x;
                        q = q / x;
                        r = r / x;
                    }
                }
                if (x == 0.0) {
                    break;
                }
                s = sqrt(p * p + q * q + r * r);
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

                    for (jint j = k; j < nn; j++) {
                        p = h[hStrideRow * (k) + (j)] + q * h[hStrideRow * (k + 1) + (j)];
                        if (notLast) {
                            p = p + r * h[hStrideRow * (k + 2) + (j)];
                            h[hStrideRow * (k + 2) + (j)] = h[hStrideRow * (k + 2) + (j)] - p * z;
                        }
                        h[hStrideRow * (k) + (j)] = h[hStrideRow * (k) + (j)] - p * x;
                        h[hStrideRow * (k + 1) + (j)] = h[hStrideRow * (k + 1) + (j)] - p * y;
                    }

                    // Column modification

                    for (jint i = 0; i <= std::min(n, k + 3); i++) {
                        p = x * h[hStrideRow * (i) + (k)] + y * h[hStrideRow * (i) + (k + 1)];
                        if (notLast) {
                            p = p + z * h[hStrideRow * (i) + (k + 2)];
                            h[hStrideRow * (i) + (k + 2)] = h[hStrideRow * (i) + (k + 2)] - p * r;
                        }
                        h[hStrideRow * (i) + (k)] = h[hStrideRow * (i) + (k)] - p;
                        h[hStrideRow * (i) + (k + 1)] = h[hStrideRow * (i) + (k + 1)] - p * q;
                    }

                    // Accumulate transformations

                    for (jint i = low; i <= high; i++) {
                        p = x * vecVArr[vStrideRow * (i) + (k)] + y * vecVArr[vStrideRow * (i) + (k + 1)];
                        if (notLast) {
                            p = p + z * vecVArr[vStrideRow * (i) + (k + 2)];
                            vecVArr[vStrideRow * (i) + (k + 2)] = vecVArr[vStrideRow * (i) + (k + 2)] - p * r;
                        }
                        vecVArr[vStrideRow * (i) + (k)] = vecVArr[vStrideRow * (i) + (k)] - p;
                        vecVArr[vStrideRow * (i) + (k + 1)] = vecVArr[vStrideRow * (i) + (k + 1)] - p * q;
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
        p = valVArr[2 * (n)];
        q = valVArr[2 * (n) + 1];

        // Real vector

        if (q == 0) {
            jint l = n;
            h[hStrideRow * (n) + (n)] = 1.0;
            for (jint i = n - 1; i >= 0; i--) {
                w = h[hStrideRow * (i) + (i)] - p;
                r = 0.0;
                for (jint j = l; j <= n; j++) {
                    r = r + h[hStrideRow * (i) + (j)] * h[hStrideRow * (j) + (n)];
                }
                if (valVArr[2 * (i) + 1] < 0.0) {
                    z = w;
                    s = r;
                } else {
                    l = i;
                    if (valVArr[2 * (i) + 1] == 0.0) {
                        if (w != 0.0) {
                            h[hStrideRow * (i) + (n)] = -r / w;
                        } else {
                            h[hStrideRow * (i) + (n)] = -r / (eps * norm);
                        }

                        // Solve real equations

                    } else {
                        x = h[hStrideRow * (i) + (i + 1)];
                        y = h[hStrideRow * (i + 1) + (i)];
                        q = (valVArr[2 * (i)] - p) * (valVArr[2 * (i)] - p)
                                + valVArr[2 * (i) + 1] * valVArr[2 * (i) + 1];
                        t = (x * s - z * r) / q;
                        h[hStrideRow * (i) + (n)] = t;
                        if (fabs(x) > fabs(z)) {
                            h[hStrideRow * (i + 1) + (n)] = (-r - w * t) / x;
                        } else {
                            h[hStrideRow * (i + 1) + (n)] = (-s - y * t) / z;
                        }
                    }

                    // Overflow control

                    t = fabs(h[hStrideRow * (i) + (n)]);
                    if ((eps * t) * t > 1) {
                        for (jint j = i; j <= n; j++) {
                            h[hStrideRow * (j) + (n)] = h[hStrideRow * (j) + (n)] / t;
                        }
                    }
                }
            }

            // Complex vector

        } else if (q < 0) {
            jint l = n - 1;

            // Last vector component imaginary so matrix is triangular

            if (fabs(h[hStrideRow * (n) + (n - 1)]) > fabs(h[hStrideRow * (n - 1) + (n)])) {
                h[hStrideRow * (n - 1) + (n - 1)] = q / h[hStrideRow * (n) + (n - 1)];
                h[hStrideRow * (n - 1) + (n)] = -(h[hStrideRow * (n) + (n)] - p) / h[hStrideRow * (n) + (n - 1)];
            } else {
                cDiv = jcomplex(0.0, -h[hStrideRow * (n - 1) + (n)])
                        / jcomplex(h[hStrideRow * (n - 1) + (n - 1)] - p, q);
                h[hStrideRow * (n - 1) + (n - 1)] = cDiv.re;
                h[hStrideRow * (n - 1) + (n)] = cDiv.im;
            }
            h[hStrideRow * (n) + (n - 1)] = 0.0;
            h[hStrideRow * (n) + (n)] = 1.0;
            for (jint i = n - 2; i >= 0; i--) {
                jdouble ra, sa, vr, vi;
                ra = 0.0;
                sa = 0.0;
                for (jint j = l; j <= n; j++) {
                    ra = ra + h[hStrideRow * (i) + (j)] * h[hStrideRow * (j) + (n - 1)];
                    sa = sa + h[hStrideRow * (i) + (j)] * h[hStrideRow * (j) + (n)];
                }
                w = h[hStrideRow * (i) + (i)] - p;

                if (valVArr[2 * (i) + 1] < 0.0) {
                    z = w;
                    r = ra;
                    s = sa;
                } else {
                    l = i;
                    if (valVArr[2 * (i) + 1] == 0) {
                        cDiv = jcomplex(-ra, -sa) / jcomplex(w, q);
                        h[hStrideRow * (i) + (n - 1)] = cDiv.re;
                        h[hStrideRow * (i) + (n)] = cDiv.im;
                    } else {

                        // Solve complex equations

                        x = h[hStrideRow * (i) + (i + 1)];
                        y = h[hStrideRow * (i + 1) + (i)];
                        vr = (valVArr[2 * (i)] - p) * (valVArr[2 * (i)] - p)
                                + valVArr[2 * (i) + 1] * valVArr[2 * (i) + 1] - q * q;
                        vi = (valVArr[2 * (i)] - p) * 2.0 * q;
                        if (vr == 0.0 && vi == 0.0) {
                            vr = eps * norm * (fabs(w) + fabs(q) + fabs(x) + fabs(y) + fabs(z));
                        }
                        cDiv = jcomplex(x * r - z * ra + q * sa, x * s - z * sa - q * ra) / jcomplex(vr, vi);
                        h[hStrideRow * (i) + (n - 1)] = cDiv.re;
                        h[hStrideRow * (i) + (n)] = cDiv.im;
                        if (fabs(x) > (fabs(z) + fabs(q))) {
                            h[hStrideRow * (i + 1) + (n - 1)] = (-ra - w * h[hStrideRow * (i) + (n - 1)]
                                    + q * h[hStrideRow * (i) + (n)]) / x;
                            h[hStrideRow * (i + 1) + (n)] = (-sa - w * h[hStrideRow * (i) + (n)]
                                    - q * h[hStrideRow * (i) + (n - 1)]) / x;
                        } else {
                            cDiv = jcomplex(-r - y * h[hStrideRow * (i) + (n - 1)], -s - y * h[hStrideRow * (i) + (n)])
                                    / jcomplex(z, q);
                            h[hStrideRow * (i + 1) + (n - 1)] = cDiv.re;
                            h[hStrideRow * (i + 1) + (n)] = cDiv.im;
                        }
                    }

                    // Overflow control

                    t = std::max(fabs(h[hStrideRow * (i) + (n - 1)]), fabs(h[hStrideRow * (i) + (n)]));
                    if ((eps * t) * t > 1) {
                        for (jint j = i; j <= n; j++) {
                            h[hStrideRow * (j) + (n - 1)] = h[hStrideRow * (j) + (n - 1)] / t;
                            h[hStrideRow * (j) + (n)] = h[hStrideRow * (j) + (n)] / t;
                        }
                    }
                }
            }
        }
    }

    // Vectors of isolated roots

    for (jint i = 0; i < nn; i++) {
        if (i < low || i > high) {
            for (jint j = i; j < nn; j++) {
                vecVArr[vStrideRow * (i) + (j)] = h[hStrideRow * (i) + (j)];
            }
        }
    }

    // Back transformation to get eigenvectors of original matrix

    for (jint j = nn - 1; j >= low; j--) {
        for (jint i = low; i <= high; i++) {
            z = 0.0;
            for (jint k = low; k <= std::min(j, high); k++) {
                z = z + vecVArr[vStrideRow * (i) + (k)] * h[hStrideRow * (k) + (j)];
            }
            vecVArr[vStrideRow * (i) + (j)] = z;
        }
    }
}

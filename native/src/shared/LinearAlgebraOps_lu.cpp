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

void LinearAlgebraOps::invert(JNIEnv *env, jobject thisObj, //
        jdoubleArray srcV, jdoubleArray dstV, jint size) {

    try {

        if (!srcV || !dstV) {
            throw std::runtime_error("Invalid arguments");
        }

        jint srcLen = env->GetArrayLength(srcV);
        jint dstLen = env->GetArrayLength(dstV);

        if ((srcLen != size * size) || (dstLen != size * size)) {
            throw std::runtime_error("Invalid arguments");
        }

        ArrayPinHandler srcVH(env, srcV, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        ArrayPinHandler dstVH(env, dstV, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_WRITE);

        jdouble *srcVArr = (jdouble *) srcVH.get();
        jdouble *dstVArr = (jdouble *) dstVH.get();

        MallocHandler allH(sizeof(jdouble) * srcLen + sizeof(jint) * size);
        void *all = (jdouble *) allH.get();
        jdouble *lu = (jdouble *) all;
        jint *pivots = (jint *) ((jdouble *) all + srcLen);

        memcpy(lu, srcVArr, sizeof(jdouble) * srcLen);

        for (jint i = 0; i < size; i++) {
            pivots[i] = i;
        }

        lup(lu, pivots, size, size);

        for (jint i = 0; i < size; i++) {

            if (lu[size * i + i] == 0) {
                throw std::runtime_error("Matrix is singular");
            }

            dstVArr[size * i + pivots[i]] = 1.0;
        }

        luSolve(lu, size, dstVArr, size);

    } catch (std::exception &e) {

        Common::throwNew(env, e);
    }
}

void LinearAlgebraOps::lup(jdouble *lu, jint *pivots, jint nRows, jint nCols) {

    MallocHandler allH(sizeof(jdouble) * nRows);
    jdouble *all = (jdouble *) allH.get();
    jdouble *luColJ = all;

    jint luStrideRow = nCols;

    // Use a "left-looking", dot-product, Crout/Doolittle algorithm.

    // Outer loop.

    for (jint j = 0; j < nCols; j++) {

        // Make a copy of the j-th column to localize references.

        for (jint i = 0; i < nRows; i++) {
            luColJ[i] = lu[luStrideRow * (i) + (j)];
        }

        // Apply previous transformations.

        for (jint i = 0; i < nRows; i++) {

            // Most of the time is spent in the following dot product.

            jint kmax = std::min(i, j);
            jdouble s = 0.0;
            for (jint k = 0; k < kmax; k++) {
                s += lu[luStrideRow * (i) + (k)] * luColJ[k];
            }

            lu[luStrideRow * (i) + (j)] = luColJ[i] -= s;
        }

        // Find pivot and exchange if necessary.

        jint p = j;
        for (jint i = j + 1; i < nRows; i++) {
            if (fabs(luColJ[i]) > fabs(luColJ[p])) {
                p = i;
            }
        }
        if (p != j) {
            for (jint k = 0; k < nCols; k++) {
                jdouble t = lu[luStrideRow * (p) + (k)];
                lu[luStrideRow * (p) + (k)] = lu[luStrideRow * (j) + (k)];
                lu[luStrideRow * (j) + (k)] = t;
            }
            jint k = pivots[p];
            pivots[p] = pivots[j];
            pivots[j] = k;
        }

        // Compute multipliers.

        if (j < nRows && lu[luStrideRow * (j) + (j)] != 0.0) {
            for (jint i = j + 1; i < nRows; i++) {
                lu[luStrideRow * (i) + (j)] /= lu[luStrideRow * (j) + (j)];
            }
        }
    }
}

void LinearAlgebraOps::luSolve(jdouble *lu, jint nLUCols, jdouble *dstVArr, jint nDstVCols) {

    jint luStrideRow = nLUCols;
    jint vStrideRow = nDstVCols;

    // Solve L*Y = B(piv,:)
    for (jint k = 0; k < nLUCols; k++) {
        for (jint i = k + 1; i < nLUCols; i++) {
            for (jint j = 0; j < nDstVCols; j++) {
                dstVArr[vStrideRow * (i) + (j)] -= dstVArr[vStrideRow * (k) + (j)] * lu[luStrideRow * (i) + (k)];
            }
        }
    }
    // Solve U*X = Y;
    for (jint k = nLUCols - 1; k >= 0; k--) {
        for (jint j = 0; j < nLUCols; j++) {
            dstVArr[vStrideRow * (k) + (j)] /= lu[luStrideRow * (k) + (k)];
        }
        for (jint i = 0; i < k; i++) {
            for (jint j = 0; j < nDstVCols; j++) {
                dstVArr[vStrideRow * (i) + (j)] -= dstVArr[vStrideRow * (k) + (j)] * lu[luStrideRow * (i) + (k)];
            }
        }
    }
}

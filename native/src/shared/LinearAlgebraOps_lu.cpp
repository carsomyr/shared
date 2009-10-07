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

void LinearAlgebraOps::lup(jdouble *lu, jint *pivots, jint nrows, jint ncols) {

    MallocHandler allH(sizeof(jdouble) * nrows);
    jdouble *all = (jdouble *) allH.get();
    jdouble *luColJ = all;

    jint luStrideRow = ncols;

    // Use a "left-looking", dot-product, Crout/Doolittle algorithm.

    // Outer loop.

    for (jint j = 0; j < ncols; j++) {

        // Make a copy of the j-th column to localize references.

        for (jint i = 0; i < nrows; i++) {
            luColJ[i] = lu[luStrideRow * (i) + (j)];
        }

        // Apply previous transformations.

        for (jint i = 0; i < nrows; i++) {

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
        for (jint i = j + 1; i < nrows; i++) {
            if (fabs(luColJ[i]) > fabs(luColJ[p])) {
                p = i;
            }
        }
        if (p != j) {
            for (jint k = 0; k < ncols; k++) {
                jdouble t = lu[luStrideRow * (p) + (k)];
                lu[luStrideRow * (p) + (k)] = lu[luStrideRow * (j) + (k)];
                lu[luStrideRow * (j) + (k)] = t;
            }
            jint k = pivots[p];
            pivots[p] = pivots[j];
            pivots[j] = k;
        }

        // Compute multipliers.

        if (j < nrows && lu[luStrideRow * (j) + (j)] != 0.0) {
            for (jint i = j + 1; i < nrows; i++) {
                lu[luStrideRow * (i) + (j)] /= lu[luStrideRow * (j) + (j)];
            }
        }
    }
}

void LinearAlgebraOps::luSolve(jdouble *lu, jint nluCols, jdouble *dstVArr, jint ndstVCols) {

    jint luStrideRow = nluCols;
    jint vStrideRow = ndstVCols;

    // Solve L*Y = B(piv,:)
    for (jint k = 0; k < nluCols; k++) {
        for (jint i = k + 1; i < nluCols; i++) {
            for (jint j = 0; j < ndstVCols; j++) {
                dstVArr[vStrideRow * (i) + (j)] -= dstVArr[vStrideRow * (k) + (j)] * lu[luStrideRow * (i) + (k)];
            }
        }
    }
    // Solve U*X = Y;
    for (jint k = nluCols - 1; k >= 0; k--) {
        for (jint j = 0; j < nluCols; j++) {
            dstVArr[vStrideRow * (k) + (j)] /= lu[luStrideRow * (k) + (k)];
        }
        for (jint i = 0; i < k; i++) {
            for (jint j = 0; j < ndstVCols; j++) {
                dstVArr[vStrideRow * (i) + (j)] -= dstVArr[vStrideRow * (k) + (j)] * lu[luStrideRow * (i) + (k)];
            }
        }
    }
}

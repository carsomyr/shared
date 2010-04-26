/*
 * Copyright (C) 2009 Roy Liu
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

#include <Common.hpp>

#ifndef _Included_LinearAlgebraOps
#define _Included_LinearAlgebraOps

/**
 * A class for linear algebra operations.
 */
class LinearAlgebraOps {

public:

    /**
     * Computes the singular value decomposition of a matrix.
     * 
     * @param env
     *      the JNI environment.
     * @param thisObj
     *      this object.
     * @param srcV
     *      the source values.
     * @param srcStrideRow
     *      the source row stride.
     * @param srcStrideCol
     *      the source column stride.
     * @param uV
     *      the input vectors.
     * @param sV
     *      the gain controls.
     * @param vV
     *      the output vectors.
     * @param nrows
     *      the number of rows.
     * @param ncols
     *      the number of columns.
     */
    static void svd(JNIEnv *env, jobject thisObj, //
            jdoubleArray srcV, jint srcStrideRow, jint srcStrideCol, //
            jdoubleArray uV, jdoubleArray sV, jdoubleArray vV, //
            jint nrows, jint ncols);

    /**
     * Computes the eigenvectors and eigenvalues of a matrix.
     * 
     * @param env
     *      the JNI environment.
     * @param thisObj
     *      this object.
     * @param srcV
     *      the source values.
     * @param vecV
     *      the eigenvectors.
     * @param valV
     *      the eigenvalues.
     * @param size
     *      the matrix size.
     */
    static void eigs(JNIEnv *env, jobject thisObj, //
            jdoubleArray srcV, jdoubleArray vecV, jdoubleArray valV, jint size);

    /**
     * Computes the inverse of a matrix.
     * 
     * @param env
     *      the JNI environment.
     * @param thisObj
     *      this object.
     * @param srcV
     *      the source values.
     * @param dstV
     *      the destination values.
     * @param size
     *      the matrix size.
     */
    static void invert(JNIEnv *env, jobject thisObj, //
            jdoubleArray srcV, jdoubleArray dstV, jint size);

private:

    static void svd(jdouble *, jint, jint, jint, //
            jdouble *, jdouble *, jdouble *, //
            jint, jint);

    static void hessenberg(jdouble *, jdouble *, jint);

    static void hessenbergToSchur(jdouble *, jdouble *, jdouble *, jint);

    static void lup(jdouble *, jint *, jint, jint);

    static void luSolve(jdouble *, jint, jdouble *, jint);
};

#endif

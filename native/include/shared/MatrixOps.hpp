/*
 * Copyright (c) 2007 Roy Liu
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
#include <ElementOps.hpp>

#ifndef _Included_MatrixOps
#define _Included_MatrixOps

/**
 * A class for matrix operations.
 */
class MatrixOps {

public:

    /**
     * Multiplies two matrices.
     * 
     * @param env
     *      the JNI environment.
     * @param thisObj
     *      this object.
     * @param lhsV
     *      the left hand side values.
     * @param rhsV
     *      the right hand side values.
     * @param lhsR
     *      the row count of the result.
     * @param rhsC
     *      the column count of the result.
     * @param dstV
     *      the destination values.
     * @param isComplex
     *      whether the operation is complex-valued.
     */
    static void mul(JNIEnv *env, jobject thisObj, jdoubleArray lhsV, jdoubleArray rhsV, jint lhsR, jint rhsC,
            jdoubleArray dstV, jboolean isComplex);

    /**
     * Computes the product of two matrices.
     * 
     * @param lArr
     *      the left hand side array.
     * @param rArr
     *      the right hand side array.
     * @param inner
     *      the inner dimension size.
     * @param outArr
     *      the output array.
     * @param lr
     *      the left hand side row count.
     * @param rc
     *      the right hand side column count.
     * @param zero
     *      the representation of 0.
     */
    template<class T> inline static void mul(const T *lArr, const T *rArr, jint inner, //
            T *outArr, jint lr, jint rc, T zero);

    /**
     * Gets the diagonal of a matrix.
     * 
     * @param env
     *      the JNI environment.
     * @param thisObj
     *      this object.
     * @param srcV
     *      source values.
     * @param dstV
     *      destination values.
     * @param size
     *      the matrix size.
     * @param isComplex
     *      whether the operation is complex-valued.
     */
    static void diag(JNIEnv *env, jobject thisObj, jdoubleArray srcV, jdoubleArray dstV, //
            jint size, jboolean isComplex);

    /**
     * Computes the diagonal of a matrix.
     * 
     * @param srcArr
     *      the source array.
     * @param dstArr
     *      the destination array.
     * @param size
     *      the matrix size.
     */
    template<class T> inline static void diag(const T *srcArr, T *dstArr, jint size);

private:

    template<class T> inline static void mulProxy(JNIEnv *, //
            jarray, jarray, jint, jint, jarray, //
            T, jboolean);

    template<class T> inline static void diagProxy(JNIEnv *, //
            jarray, jarray, jint, jboolean);
};

#endif

/*
 * This file is part of the Shared Scientific Toolbox in Java ("this library").
 * 
 * Copyright (C) 2007 Roy Liu
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
     *      the concept of '0'.
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

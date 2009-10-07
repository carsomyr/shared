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

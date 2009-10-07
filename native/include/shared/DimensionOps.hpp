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
#include <MappingOps.hpp>

#include <JNIHeadersWrap.hpp>

#ifndef _Included_DimensionOps
#define _Included_DimensionOps

/**
 * A class for dimension-based operations.
 */
class DimensionOps {

public:

    /**
     * Performs a real dimension operation.
     * 
     * @param env
     *      the JNI environment.
     * @param thisObj
     *      this object.
     * @param type
     *      the operation type.
     * @param srcV
     *      the source values.
     * @param srcD
     *      the source dimensions.
     * @param srcS
     *      the source strides.
     * @param dstV
     *      the destination values.
     * @param selectedDims
     *      the dimensions of interest.
     */
    static void rdOp(JNIEnv *env, jobject thisObj, jint type, //
            jdoubleArray srcV, jintArray srcD, jintArray srcS, jdoubleArray dstV, //
            jintArray selectedDims);

    /**
     * Performs a real index operation.
     * 
     * @param env
     *      the JNI environment.
     * @param thisObj
     *      this object.
     * @param type
     *      the operation type.
     * @param srcV
     *      the source values.
     * @param srcD
     *      the source dimensions.
     * @param srcS
     *      the source strides.
     * @param dstV
     *      the destination values.
     * @param dim
     *      the dimension of interest.
     */
    static void riOp(JNIEnv *env, jobject thisObj, jint type, //
            jdoubleArray srcV, jintArray srcD, jintArray srcS, //
            jintArray dstV, //
            jint dim);

    /**
     * Performs a real reduce operation.
     * 
     * @param env
     *      the JNI environment.
     * @param thisObj
     *      this object.
     * @param type
     *      the operation type.
     * @param srcV
     *      the source values.
     * @param srcD
     *      the source dimensions.
     * @param srcS
     *      the source strides.
     * @param dstV
     *      the destination values.
     * @param dstD
     *      the destination dimensions.
     * @param dstS
     *      the destination strides.
     * @param selectedDims
     *      the dimensions of interest.
     */
    static void rrOp(JNIEnv *env, jobject thisObj, jint type, //
            jdoubleArray srcV, jintArray srcD, jintArray srcS, //
            jdoubleArray dstV, jintArray dstD, jintArray dstS, //
            jintArray selectedDims);

    /**
     * Assigns base indices when excluding a dimension.
     * 
     * @param srcIndices
     *      the source indices.
     * @param srcDArr
     *      the dimensions.
     * @param srcDArrModified
     *      the modified dimensions.
     * @param srcSArr
     *      the strides.
     * @param srcSArrModified
     *      the modified strides.
     * @param ndims
     *      the number of dimensions.
     * @param dim
     *      the dimension to exclude.
     */
    static void assignBaseIndices( //
            jint *srcIndices, //
            const jint *srcDArr, jint *srcDArrModified, const jint *srcSArr, jint *srcSArrModified, //
            jint ndims, jint dim);

    /**
     * Defines a real reduce operation.
     */
    typedef void rrOp_t(jdouble *, const jint *, jint, jint, jint);

    /**
     * Real reduce sum.
     */
    inline static rrOp_t rrSum;

    /**
     * Real reduce product.
     */
    inline static rrOp_t rrProd;

    /**
     * Real reduce maximum.
     */
    inline static rrOp_t rrMax;

    /**
     * Real reduce minimum.
     */
    inline static rrOp_t rrMin;

    /**
     * Real reduce variance.
     */
    inline static rrOp_t rrVar;

    /**
     * Real index maximum.
     */
    inline static void riMax(jdouble *, const jint *, jint *, jint, jint, jint);

    /**
     * Real index minimum.
     */
    inline static void riMin(jdouble *, const jint *, jint *, jint, jint, jint);

    /**
     * Real index find zeroes.
     */
    inline static void riZero(jdouble *, const jint *, jint *, jint, jint, jint);

    /**
     * Real index find greater-than-zeroes.
     */
    inline static void riGZero(jdouble *, const jint *, jint *, jint, jint, jint);

    /**
     * Real index find less-than-zeroes.
     */
    inline static void riLZero(jdouble *, const jint *, jint *, jint, jint, jint);

    /**
     * Real index sort.
     */
    inline static void riSort(jdouble *, const jint *, jint *, jint, jint, jint);

    /**
     * Real dimension sum.
     */
    inline static void rdSum(jdouble *, const jint *, const jint *, jdouble *, const jint *, //
            jint, jint, jint);

    /**
     * Real dimension product.
     */
    inline static void rdProd(jdouble *, const jint *, const jint *, jdouble *, const jint *, //
            jint, jint, jint);
};

#endif

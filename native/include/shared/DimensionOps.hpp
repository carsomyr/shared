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
     * @param opDims
     *      the dimensions of interest.
     */
    static void rdOp(JNIEnv *env, jobject thisObj, jint type, //
            jdoubleArray srcV, jintArray srcD, jintArray srcS, jdoubleArray dstV, //
            jintArray opDims);

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
     * @param opDims
     *      the dimensions of interest.
     */
    static void rrOp(JNIEnv *env, jobject thisObj, jint type, //
            jdoubleArray srcV, jintArray srcD, jintArray srcS, //
            jdoubleArray dstV, jintArray dstD, jintArray dstS, //
            jintArray opDims);

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
     * @param nDims
     *      the number of dimensions.
     * @param dim
     *      the dimension to exclude.
     */
    static void assignBaseIndices( //
            jint *srcIndices, //
            const jint *srcDArr, jint *srcDArrModified, const jint *srcSArr, jint *srcSArrModified, //
            jint nDims, jint dim);

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

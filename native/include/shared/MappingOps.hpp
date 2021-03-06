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
#include <NativeArrayKernel.hpp>

#ifndef _Included_MappingOps
#define _Included_MappingOps

/**
 * A container class for conveying mapping information.
 */
class MappingResult {

public:

    explicit MappingResult() {

        this->nIndices = 0;
        this->indices = NULL;
        this->srcIndices = NULL;
        this->dstIndices = NULL;
    }

    /**
     * Allocates space for source and destination indices.
     * 
     * @param nIndices
     *      the number of indices.
     */
    virtual void createIndices(jint nIndices) {

        if (this->indices) {
            throw std::runtime_error("Indices already created");
        }

        void *indices = malloc(sizeof(jint) * 2 * nIndices);

        if (!indices) {
            throw std::runtime_error("Allocation failed");
        }

        this->nIndices = nIndices;
        this->indices = indices;
        this->srcIndices = (jint *) indices;
        this->dstIndices = (jint *) indices + nIndices;
    }

    virtual ~MappingResult() {

        if (this->indices) {
            free(this->indices);
        }
    }

    /**
     * The mapping size.
     */
    jint nIndices;

    /**
     * The source indices.
     */
    jint *srcIndices;

    /**
     * The destination indices.
     */
    jint *dstIndices;

private:

    MappingResult(const MappingResult &);

    MappingResult &operator=(const MappingResult &);

    void *indices;
};

/**
 * A class for mapping and slicing operations.
 */
class MappingOps {

public:

    /**
     * Creates an array of physical indices.
     * 
     * @param indices
     *      the result array.
     * @param dims
     *      the mapping dimensions.
     * @param strides
     *      the strides.
     * @param nDims
     *      the number of dimensions.
     */
    static void assignMappingIndices( //
            jint *indices, const jint *dims, const jint *strides, //
            jint nDims);

    /**
     * Creates an array of physical slicing indices.
     * 
     * @param indices
     *      the result array.
     * @param dims
     *      the slicing dimensions.
     * @param strides
     *      the strides.
     * @param nDims
     *      the number of dimensions.
     * @param sliceIndices
     *      the indices to slice on arranged by dimension.
     */
    static void assignSlicingIndices( //
            jint *indices, const jint *dims, const jint *strides, //
            jint nDims, //
            jint **sliceIndices);

    /**
     * Checks an array's dimensions and strides.
     * 
     * @param len
     *      the array length.
     * @param dims
     *      the dimensions.
     * @param strides
     *      the strides.
     * @param nDims
     *      the number of dimensions.
     */
    static void checkDimensions( //
            const jint *dims, const jint *strides, //
            jint nDims, jint len);

    /**
     * Assigns source values to destination values based on arrays of physical indices.
     * 
     * @param env
     *      the JNI environment.
     * @param type
     *      the array type.
     * @param srcV
     *      the source array.
     * @param srcIndices
     *      the source indices.
     * @param dstV
     *      the destination array.
     * @param dstIndices
     *      the destination indices.
     * @param nIndices
     *      the number of indices.
     */
    static void assign(JNIEnv *env, ArrayPinHandler::jarray_type type, //
            jarray srcV, jint *srcIndices, //
            jarray dstV, jint *dstIndices, //
            jint nIndices);

    //

    /**
     * Performs a mapping operation.
     * 
     * @param env
     *      the JNI environment.
     * @param thisObj
     *      this object.
     * @param bounds
     *      the mapping bounds.
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
     */
    static void map(JNIEnv *env, jobject thisObj, //
            jintArray bounds, //
            jobject srcV, jintArray srcD, jintArray srcS, //
            jobject dstV, jintArray dstD, jintArray dstS);

    /**
     * Computes source and destination mapping indices.
     * 
     * @param boundsArr
     *      the mapping bounds.
     * @param srcDArr
     *      the source dimensions.
     * @param srcSArr
     *      the source strides.
     * @param srcLen
     *      the number of source values.
     * @param dstDArr
     *      the destination dimensions.
     * @param dstSArr
     *      the destination strides.
     * @param dstLen
     *      the number of destination values.
     * @param nDims
     *      the number of dimensions.
     * @return the MappingResult.
     */
    static MappingResult *map( //
            const jint *boundsArr, //
            const jint *srcDArr, const jint *srcSArr, jint srcLen, //
            const jint *dstDArr, const jint *dstSArr, jint dstLen, //
            jint nDims);

    //

    /**
     * Performs a slicing operation.
     * 
     * @param env
     *      the JNI environment.
     * @param thisObj
     *      this object.
     * @param slices
     *      the slicing specification.
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
     */
    static void slice(JNIEnv *env, jobject thisObj, //
            jintArray slices, //
            jobject srcV, jintArray srcD, jintArray srcS, //
            jobject dstV, jintArray dstD, jintArray dstS);

    /**
     * Computes source and destination slicing indices.
     * 
     * @param slicesArr
     *      the slicing specification.
     * @param nSlices
     *      the number of slices.
     * @param srcDArr
     *      the source dimensions.
     * @param srcSArr
     *      the source strides.
     * @param srcLen
     *      the number of source values.
     * @param dstDArr
     *      the destination dimensions.
     * @param dstSArr
     *      the destination strides.
     * @param dstLen
     *      the number of destination values.
     * @param nDims
     *      the number of dimensions.
     * @return the MappingResult.
     */
    static MappingResult *slice( //
            const jint *slicesArr, jint nSlices, //
            const jint *srcDArr, const jint *srcSArr, jint srcLen, //
            const jint *dstDArr, const jint *dstSArr, jint dstLen, //
            jint nDims);

private:

    static MappingResult *mapProxy(JNIEnv *, //
            jintArray, //
            jarray, jintArray, jintArray, //
            jarray, jintArray, jintArray);

    static MappingResult *sliceProxy(JNIEnv *, //
            jintArray, //
            jarray, jintArray, jintArray, //
            jarray, jintArray, jintArray);
};

#endif

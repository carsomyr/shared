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
#include <NativeArrayKernel.hpp>

#ifndef _Included_MappingOps
#define _Included_MappingOps

/**
 * A container class for conveying mapping information.
 */
class MappingResult {

public:

    explicit MappingResult() {

        this->nindices = 0;
        this->indices = NULL;
        this->srcIndices = NULL;
        this->dstIndices = NULL;
    }

    /**
     * Allocates space for source and destination indices.
     * 
     * @param nindices
     *      the number of indices.
     */
    virtual void createIndices(jint nindices) {

        if (this->indices) {
            throw std::runtime_error("Indices already created");
        }

        void *indices = malloc(sizeof(jint) * 2 * nindices);

        if (!indices) {
            throw std::runtime_error("Allocation failed");
        }

        this->nindices = nindices;
        this->indices = indices;
        this->srcIndices = (jint *) indices;
        this->dstIndices = (jint *) indices + nindices;
    }

    virtual ~MappingResult() {

        if (this->indices) {
            free(this->indices);
        }
    }

    /**
     * The mapping size.
     */
    jint nindices;

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
     * @param ndims
     *      the number of dimensions.
     */
    static void assignMappingIndices( //
            jint *indices, const jint *dims, const jint *strides, //
            jint ndims);

    /**
     * Creates an array of physical slicing indices.
     * 
     * @param indices
     *      the result array.
     * @param dims
     *      the slicing dimensions.
     * @param strides
     *      the strides.
     * @param ndims
     *      the number of dimensions.
     * @param sliceIndices
     *      the indices to slice on arranged by dimension.
     */
    static void assignSlicingIndices( //
            jint *indices, const jint *dims, const jint *strides, //
            jint ndims, //
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
     * @param ndims
     *      the number of dimensions.
     */
    static void checkDimensions( //
            const jint *dims, const jint *strides, //
            jint ndims, jint len);

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
     * @param nindices
     *      the number of indices.
     */
    static void assign(JNIEnv *env, ArrayPinHandler::jarray_type type, //
            jarray srcV, jint *srcIndices, //
            jarray dstV, jint *dstIndices, //
            jint nindices);

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
     * @param ndims
     *      the number of dimensions.
     * @return the MappingResult.
     */
    static MappingResult *map( //
            const jint *boundsArr, //
            const jint *srcDArr, const jint *srcSArr, jint srcLen, //
            const jint *dstDArr, const jint *dstSArr, jint dstLen, //
            jint ndims);

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
     * @param nslices
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
     * @param ndims
     *      the number of dimensions.
     * @return the MappingResult.
     */
    static MappingResult *slice( //
            const jint *slicesArr, jint nslices, //
            const jint *srcDArr, const jint *srcSArr, jint srcLen, //
            const jint *dstDArr, const jint *dstSArr, jint dstLen, //
            jint ndims);

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

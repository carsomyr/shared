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
#include <MappingOps.hpp>
#include <NativeArrayKernel.hpp>

#include <JNIHeadersWrap.hpp>

#ifndef _Included_SparseOps
#define _Included_SparseOps

/**
 * A container class for conveying sparse array merge information.
 */
class MergeResult {

public:

    explicit MergeResult() {

        this->assignments = NULL;
        this->oldAssignments = NULL;
        this->oldIndirections = NULL;
        this->newAssignments = NULL;
        this->newIndirections = NULL;

        this->oldLen = 0;
        this->newLen = 0;

        this->metadata = NULL;
        this->indices = NULL;
        this->indirectionOffsets = NULL;
        this->indirections = NULL;

        this->count = 0;
        this->dims = NULL;
        this->nDims = 0;
    }

    /**
     * Allocates space for old and new array assignments.
     * 
     * @param oldLen
     *      the number of old array assignments.
     * @param newLen
     *      the number of new array assignments.
     */
    virtual void createAssignments(jint oldLen, jint newLen) {

        if (this->assignments) {
            throw std::runtime_error("Assignments already created");
        }

        void *assignments = malloc(sizeof(jint) * (2 * oldLen + 2 * newLen));

        if (!assignments) {
            throw std::runtime_error("Allocation failed");
        }

        this->assignments = assignments;
        this->oldAssignments = (jint *) assignments;
        this->oldIndirections = (jint *) assignments + oldLen;
        this->newAssignments = (jint *) assignments + 2 * oldLen;
        this->newIndirections = (jint *) assignments + 2 * oldLen + newLen;

        this->oldLen = oldLen;
        this->newLen = newLen;
    }

    /**
     * Allocates space for metadata.
     * 
     * @param count
     *      the number of elements.
     * @param dims
     *      the dimensions.
     * @param nDims
     *      the number of dimensions.
     */
    virtual void createMetadata(jint count, jint *dims, jint nDims) {

        if (this->metadata) {
            throw std::runtime_error("Metadata already created");
        }

        jint sumD = Common::sum(dims, nDims, (jint) 0);

        void *metadata = malloc(sizeof(jint) * (count //
                + (sumD + nDims) //
                + nDims * count //
                + nDims));

        if (!metadata) {
            throw std::runtime_error("Allocation failed");
        }

        this->metadata = metadata;
        this->indices = (jint *) metadata;
        this->indirectionOffsets = (jint *) metadata + count;
        this->indirections = (jint *) metadata + count + (sumD + nDims);

        this->count = count;
        this->dims = (jint *) metadata + count + (sumD + nDims) + (nDims * count);
        this->nDims = nDims;

        memcpy(this->dims, dims, sizeof(jint) * nDims);
    }

    virtual ~MergeResult() {

        if (this->assignments) {
            free(this->assignments);
        }

        if (this->metadata) {
            free(this->metadata);
        }
    }

    /**
     * The assignments from the old array.
     */
    jint *oldAssignments;

    /**
     * The indirection indices into the old array.
     */
    jint *oldIndirections;

    /**
     * The assignments from the new array.
     */
    jint *newAssignments;

    /**
     * The indirection indices into the new array.
     */
    jint *newIndirections;

    /**
     * The number of old array assignments.
     */
    jint oldLen;

    /**
     * The number of new array assignments.
     */
    jint newLen;

    /**
     * The physical indices.
     */
    jint *indices;

    /**
     * The offsets into MergeResult#indirections.
     */
    jint *indirectionOffsets;

    /**
     * The indirection indices into the storage array and MergeResult#indices.
     */
    jint *indirections;

    /**
     * The number of elements.
     */
    jint count;

    /**
     * The dimensions.
     */
    jint *dims;

    /**
     * The number of dimensions.
     */
    jint nDims;

private:

    MergeResult(const MergeResult &);

    MergeResult &operator=(const MergeResult &);

    void *assignments;

    void *metadata;
};

/**
 * A class for sparse array operations.
 */
class SparseOps {

public:

    /**
     * Creates a sparse array by assigning old and new array values into it.
     * 
     * @param env
     *      the JNI environment.
     * @param type
     *      the common array type.
     * @param res
     *      the MergeResult.
     * @param oldV
     *      the old array.
     * @param newV
     *      the new array.
     * @return the sparse array.
     */
    static jobject createSparseArrayState(JNIEnv *env, //
            ArrayPinHandler::jarray_type type, //
            MergeResult *res, jarray oldV, jarray newV);

    /**
     * Merges old and new array metadata.
     * 
     * @param oldIndices
     *      the old physical indices. Invariant: Sorted in ascending order, and does not contain duplicates.
     * @param oldIndirections
     *      the indirections on old values.
     * @param oldLen
     *      the old array length.
     * @param newIndices
     *      the new physical indices. Invariant: Sorted in ascending order, and does not contain duplicates.
     * @param newIndirections
     *      the indirections on new values.
     * @param newLen
     *      the new array length.
     * @param dims
     *      the dimensions.
     * @param strides
     *      the strides.
     * @param dimOffsets
     *      the dimension offsets.
     * @param nDims
     *      the number of dimensions.
     * @return the MergeResult.
     */
    static MergeResult *merge( //
            jint *oldIndices, jint *oldIndirections, jint oldLen, //
            jint *newIndices, jint *newIndirections, jint newLen, //
            jint *dims, jint *strides, jint *dimOffsets, jint nDims);

    /**
     * Normalizes a range of sorted values such that duplicates are removed.
     * 
     * @param values
     *      the array of values.
     * @param start
     *      the start index.
     * @param end
     *      the end index.
     * @return the number of resulting unique values.
     */
    static jint normalize(jint *values, jint start, jint end);

    /**
     * Gets the sliced indirections.
     * 
     * @param sliceOffsets
     *      the slice offsets.
     * @param sliceCounts
     *      the slice counts.
     * @param slices
     *      the slices.
     * @param dimOffsets
     *      the dimension offsets.
     * @param indirectionOffsets
     *      the indirection offsets.
     * @param indirections
     *      the indirections.
     * @param nIndirections
     *      the number of indirections.
     * @param dims
     *      the dimensions.
     * @param nDims
     *      the number of dimensions.
     * @param result
     *      the result array.
     * @param len
     *      the result length.
     */
    static void getSlicedIndirections( //
            jint *sliceOffsets, jint *sliceCounts, jint *slices, //
            jint *dimOffsets, jint *indirectionOffsets, jint *indirections, jint nIndirections, //
            jint *dims, jint nDims, //
            jint *result, jint &len);

    /**
     * Merges sliced indirections by physical index.
     * 
     * @param indices
     *      the physical indices.
     * @param indirections
     *      the sliced indirections.
     * @param len
     *      the number of elements.
     * @param resIndices
     *      the resulting physical indices.
     * @param resIndirections
     *      the resulting sliced indirections.
     * @param resLen
     *      the resulting number of elements.
     */
    static void merge(jint *indices, jint *indirections, jint len, //
            jint *resIndices, jint *resIndirections, jint &resLen);

    //

    /**
     * Slices one sparse array into another.
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
     * @param srcDO
     *      the source dimension offsets.
     * @param srcI
     *      the source physical indices. Invariant: Sorted in ascending order, and does not contain duplicates.
     * @param srcIO
     *      the source indirection offsets.
     * @param srcII
     *      the source indirections.
     * @param dstV
     *      the destination values.
     * @param dstD
     *      the destination dimensions.
     * @param dstS
     *      the destination strides.
     * @param dstDO
     *      the destination dimension offsets.
     * @param dstI
     *      the destination physical indices. Invariant: Sorted in ascending order, and does not contain duplicates.
     * @param dstIO
     *      the destination indirection offsets.
     * @param dstII
     *      the destination indirections.
     * @return the sparse array.
     */
    static jobject slice(JNIEnv *env, jobject thisObj, //
            jintArray slices, //
            jobject srcV, jintArray srcD, jintArray srcS, jintArray srcDO, //
            jintArray srcI, jintArray srcIO, jintArray srcII, //
            jobject dstV, jintArray dstD, jintArray dstS, jintArray dstDO, //
            jintArray dstI, jintArray dstIO, jintArray dstII);

    /**
     * Inserts elements into a sparse array.
     * 
     * @param env
     *      the JNI environment.
     * @param thisObj
     *      this object.
     * @param oldV
     *      the old values.
     * @param oldD
     *      the old dimensions.
     * @param oldS
     *      the old strides.
     * @param oldDO
     *      the old dimension offsets.
     * @param oldI
     *      the old physical indices. Invariant: Sorted in ascending order, and does not contain duplicates.
     * @param newV
     *      the new values.
     * @param newI
     *      the new physical indices, which need not be sorted in ascending order.
     * @return the sparse array.
     */
    static jobject insert(JNIEnv *env, jobject thisObj, //
            jobject oldV, jintArray oldD, jintArray oldS, jintArray oldDO, jintArray oldI, //
            jobject newV, jintArray newI);

private:

    static MergeResult *mergeProxy(JNIEnv *, //
            jintArray, //
            jarray, jintArray, jintArray, jintArray, //
            jintArray, jintArray, jintArray, //
            jarray, jintArray, jintArray, jintArray, //
            jintArray, jintArray, jintArray);

    static MergeResult *mergeProxy(JNIEnv *, //
            jarray, jintArray, jintArray, jintArray, jintArray, //
            jarray, jintArray);
};

#endif

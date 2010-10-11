/*
 * Copyright (c) 2009 Roy Liu
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

#include <SparseOps.hpp>

jobject SparseOps::createSparseArrayState(JNIEnv *env, //
        ArrayPinHandler::jarray_type type, //
        MergeResult *mergeResult, jarray oldV, jarray newV) {

    jarray values;

    jint *oldAssignments = mergeResult->oldAssignments;
    jint *newAssignments = mergeResult->newAssignments;
    jint *newIndirections = mergeResult->newIndirections;
    jint *oldIndirections = mergeResult->oldIndirections;

    switch (type) {

    case ArrayPinHandler::DOUBLE:

    {
        jdoubleArray dstV = Common::newDoubleArray(env, mergeResult->count);

        ArrayPinHandler oldVh(env, oldV, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        ArrayPinHandler newVh(env, newV, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        ArrayPinHandler dstVh(env, dstV, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_WRITE);

        jdouble *oldVArr = (jdouble *) oldVh.get();
        jdouble *newVArr = (jdouble *) newVh.get();
        jdouble *dstVArr = (jdouble *) dstVh.get();

        for (jint i = 0, n = mergeResult->oldLen; i < n; i++) {
            dstVArr[oldAssignments[i]] = oldVArr[oldIndirections[i]];
        }

        for (jint i = 0, n = mergeResult->newLen; i < n; i++) {
            dstVArr[newAssignments[i]] = newVArr[newIndirections[i]];
        }

        values = dstV;
    }

        break;

    case ArrayPinHandler::INT:

    {
        jintArray dstV = Common::newIntArray(env, mergeResult->count);

        ArrayPinHandler oldVh(env, oldV, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        ArrayPinHandler newVh(env, newV, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        ArrayPinHandler dstVh(env, dstV, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_WRITE);

        jint *oldVArr = (jint *) oldVh.get();
        jint *newVArr = (jint *) newVh.get();
        jint *dstVArr = (jint *) dstVh.get();

        for (jint i = 0, n = mergeResult->oldLen; i < n; i++) {
            dstVArr[oldAssignments[i]] = oldVArr[oldIndirections[i]];
        }

        for (jint i = 0, n = mergeResult->newLen; i < n; i++) {
            dstVArr[newAssignments[i]] = newVArr[newIndirections[i]];
        }

        values = dstV;
    }

        break;

    case ArrayPinHandler::OBJECT:

    {
        jobjectArray dstV = NativeArrayKernel::newArray(env, //
                NativeArrayKernel::getComponentType(env, env->GetObjectClass(oldV)), //
                mergeResult->count);

        for (jint i = 0, n = mergeResult->oldLen; i < n; i++) {
            env->SetObjectArrayElement((jobjectArray) dstV, oldAssignments[i], //
                    env->GetObjectArrayElement((jobjectArray) oldV, oldIndirections[i]));
        }

        for (jint i = 0, n = mergeResult->newLen; i < n; i++) {
            env->SetObjectArrayElement((jobjectArray) dstV, newAssignments[i], //
                    env->GetObjectArrayElement((jobjectArray) newV, newIndirections[i]));
        }

        values = dstV;
    }

        break;

    default:
        throw std::runtime_error("Invalid array type");
    }

    jint count = mergeResult->count;
    jint *dims = mergeResult->dims;
    jint nDims = mergeResult->nDims;

    jint sumD = Common::sum(dims, nDims, (jint) 0);

    jintArray indices = Common::newIntArray(env, count);
    jintArray indirectionOffsets = Common::newIntArray(env, sumD + nDims);
    jintArray indirections = Common::newIntArray(env, nDims * count);

    {
        ArrayPinHandler indicesH(env, indices, //
                ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_WRITE);
        ArrayPinHandler indirectionOffsetsH(env, indirectionOffsets, //
                ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_WRITE);
        ArrayPinHandler indirectionsH(env, indirections, //
                ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_WRITE);

        memcpy((jint *) indicesH.get(), //
                mergeResult->indices, sizeof(jint) * count);
        memcpy((jint *) indirectionOffsetsH.get(), //
                mergeResult->indirectionOffsets, sizeof(jint) * (sumD + nDims));
        memcpy((jint *) indirectionsH.get(), //
                mergeResult->indirections, sizeof(jint) * (nDims * count));
    }

    return NativeArrayKernel::newSparseArrayState(env, values, indices, indirectionOffsets, indirections);
}

MergeResult *SparseOps::merge( //
        jint *oldIndices, jint *oldIndirections, jint oldLen, //
        jint *newIndices, jint *newIndirections, jint newLen, //
        jint *dims, jint *strides, jint *dimOffsets, jint nDims) {

    MergeResult *res = NULL;

    try {

        res = new MergeResult();

        //

        res->createAssignments(oldLen, newLen);

        jint *oldAssignments = res->oldAssignments;
        jint *newAssignments = res->newAssignments;

        jint count = 0;
        jint oldCount = 0;
        jint newCount = 0;

        for (; oldCount < oldLen && newCount < newLen;) {

            if (oldIndices[oldCount] < newIndices[newCount]) {

                oldAssignments[oldCount++] = count++;

            } else if (oldIndices[oldCount] > newIndices[newCount]) {

                newAssignments[newCount++] = count++;

            } else {

                oldAssignments[oldCount++] = count;
                newAssignments[newCount++] = count;
                count++;
            }
        }

        for (; oldCount < oldLen; oldCount++, count++) {
            oldAssignments[oldCount] = count;
        }

        for (; newCount < newLen; newCount++, count++) {
            newAssignments[newCount] = count;
        }

        res->createMetadata(count, dims, nDims);

        jint *indices = res->indices;
        jint *indirectionOffsets = res->indirectionOffsets;
        jint *indirections = res->indirections;
        jint *oldIndirectionsCopy = res->oldIndirections;
        jint *newIndirectionsCopy = res->newIndirections;

        //

        for (jint i = 0; i < oldCount; i++) {
            indices[oldAssignments[i]] = oldIndices[i];
        }

        for (jint i = 0; i < newCount; i++) {
            indices[newAssignments[i]] = newIndices[i];
        }

        memcpy(oldIndirectionsCopy, oldIndirections, sizeof(jint) * oldLen);
        memcpy(newIndirectionsCopy, newIndirections, sizeof(jint) * newLen);

        //

        jint sumD = Common::sum(dims, nDims, (jint) 0);
        jint prodD = Common::product(dims, nDims, (jint) 1);

        MallocHandler mallocH(sizeof(jint) * (sumD + nDims));
        jint *dimCounts = (jint *) mallocH.get();

        //

        memset(dimCounts, 0, sizeof(jint) * (sumD + nDims));

        for (jint i = 0, acc; i < count; i++) {

            acc = indices[i];

            if (!(acc >= 0 && acc < prodD)) {
                throw std::runtime_error("Invalid physical index");
            }

            for (jint dim = 0; dim < nDims; dim++) {

                jint dimOffset = dimOffsets[dim] + acc / strides[dim];

                dimCounts[dimOffset]++;
                acc %= strides[dim];
            }
        }

        //

        for (jint dim = 0, acc; dim < nDims; dim++) {

            acc = 0;

            jint dimOffset = dimOffsets[dim];
            jint dimSize = dims[dim];

            for (jint dimIndex = 0; dimIndex < dimSize; dimIndex++) {

                indirectionOffsets[dimOffset + dimIndex] = acc;
                acc += dimCounts[dimOffsets[dim] + dimIndex];
            }

            indirectionOffsets[dimOffset + dimSize] = count;
        }

        //

        memset(dimCounts, 0, sizeof(jint) * (sumD + nDims));

        for (jint i = 0, acc; i < count; i++) {

            acc = indices[i];

            for (jint dim = 0; dim < nDims; dim++) {

                jint dimOffset = dimOffsets[dim] + acc / strides[dim];

                indirections[count * dim + indirectionOffsets[dimOffset] + dimCounts[dimOffset]] = i;
                dimCounts[dimOffset]++;
                acc %= strides[dim];
            }
        }

        return res;

    } catch (...) {

        if (res) {
            delete res;
        }

        throw;
    }
}

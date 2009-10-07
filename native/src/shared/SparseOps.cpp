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

        ArrayPinHandler oldVH(env, oldV, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        ArrayPinHandler newVH(env, newV, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        ArrayPinHandler dstVH(env, dstV, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_WRITE);

        jdouble *oldVArr = (jdouble *) oldVH.get();
        jdouble *newVArr = (jdouble *) newVH.get();
        jdouble *dstVArr = (jdouble *) dstVH.get();

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

        ArrayPinHandler oldVH(env, oldV, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        ArrayPinHandler newVH(env, newV, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        ArrayPinHandler dstVH(env, dstV, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_WRITE);

        jint *oldVArr = (jint *) oldVH.get();
        jint *newVArr = (jint *) newVH.get();
        jint *dstVArr = (jint *) dstVH.get();

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
    jint ndims = mergeResult->ndims;

    jint sumD = Common::sum(dims, ndims, (jint) 0);

    jintArray indices = Common::newIntArray(env, count);
    jintArray indirectionOffsets = Common::newIntArray(env, sumD + ndims);
    jintArray indirections = Common::newIntArray(env, ndims * count);

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
                mergeResult->indirectionOffsets, sizeof(jint) * (sumD + ndims));
        memcpy((jint *) indirectionsH.get(), //
                mergeResult->indirections, sizeof(jint) * (ndims * count));
    }

    return NativeArrayKernel::newSparseArrayState(env, values, indices, indirectionOffsets, indirections);
}

MergeResult *SparseOps::merge( //
        jint *oldIndices, jint *oldIndirections, jint oldLen, //
        jint *newIndices, jint *newIndirections, jint newLen, //
        jint *dims, jint *strides, jint *dimOffsets, jint ndims) {

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

        res->createMetadata(count, dims, ndims);

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

        jint sumD = Common::sum(dims, ndims, (jint) 0);
        jint prodD = Common::product(dims, ndims, (jint) 1);

        MallocHandler mallocH(sizeof(jint) * (sumD + ndims));
        jint *dimCounts = (jint *) mallocH.get();

        //

        memset(dimCounts, 0, sizeof(jint) * (sumD + ndims));

        for (jint i = 0, acc; i < count; i++) {

            acc = indices[i];

            if (!(acc >= 0 && acc < prodD)) {
                throw std::runtime_error("Invalid physical index");
            }

            for (jint dim = 0; dim < ndims; dim++) {

                jint dimOffset = dimOffsets[dim] + acc / strides[dim];

                dimCounts[dimOffset]++;
                acc %= strides[dim];
            }
        }

        //

        for (jint dim = 0, acc; dim < ndims; dim++) {

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

        memset(dimCounts, 0, sizeof(jint) * (sumD + ndims));

        for (jint i = 0, acc; i < count; i++) {

            acc = indices[i];

            for (jint dim = 0; dim < ndims; dim++) {

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

        throw ;
    }
}

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

jobject SparseOps::insert(JNIEnv *env, jobject thisObj, //
        jobject oldV, jintArray oldD, jintArray oldS, jintArray oldDO, jintArray oldI, //
        jobject newV, jintArray newI) {

    jobject res = NULL;

    MergeResult *mergeResult = NULL;

    try {

        if (!oldV || !oldD || !oldS || !oldDO || !oldI || !newV || !newI) {
            throw std::runtime_error("Invalid arguments");
        }

        //

        ArrayPinHandler::jarray_type type = NativeArrayKernel::getArrayType(env, newV, oldV);

        //

        mergeResult = mergeProxy(env, //
                (jarray) oldV, oldD, oldS, oldDO, oldI, //
                (jarray) newV, newI);

        //

        res = createSparseArrayState(env, type, mergeResult, (jarray) oldV, (jarray) newV);

    } catch (std::exception &e) {

        Common::throwNew(env, e);
    }

    if (mergeResult) {
        delete mergeResult;
    }

    return res;
}

MergeResult *SparseOps::mergeProxy(JNIEnv *env, //
        jarray oldV, jintArray oldD, jintArray oldS, jintArray oldDO, jintArray oldI, //
        jarray newV, jintArray newI) {

    MergeResult *mergeResult = NULL;

    try {

        jint ndims = env->GetArrayLength(oldD);
        jint oldLen = env->GetArrayLength(oldV);
        jint newLen = env->GetArrayLength(newV);

        if ((ndims != env->GetArrayLength(oldS)) //
                || (oldLen != env->GetArrayLength(oldI)) //
                || (newLen != env->GetArrayLength(newI)) //
                || (ndims + 1 != env->GetArrayLength(oldDO))) {
            throw std::runtime_error("Invalid arguments");
        }

        ArrayPinHandler oldDH(env, oldD, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        ArrayPinHandler oldSH(env, oldS, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        ArrayPinHandler oldDOH(env, oldDO, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        ArrayPinHandler oldIH(env, oldI, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        ArrayPinHandler newIH(env, newI, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);

        jint *oldDArr = (jint *) oldDH.get();
        jint *oldSArr = (jint *) oldSH.get();
        jint *oldDOArr = (jint *) oldDOH.get();
        jint *oldIArr = (jint *) oldIH.get();
        jint *newIArr = (jint *) newIH.get();

        jint prodD = Common::product(oldDArr, ndims, (jint) 1);

        MappingOps::checkDimensions(oldDArr, oldSArr, ndims, prodD);

        for (jint dim = 0; dim < ndims; dim++) {

            if (oldDOArr[dim + 1] - oldDOArr[dim] - 1 != oldDArr[dim]) {
                throw std::runtime_error("Invalid arguments");
            }
        }

        //

        MallocHandler mallocH(sizeof(permutation_entry<jint, jint> ) * newLen //
                + sizeof(jint) * (2 * newLen + oldLen));

        void *all = (void *) mallocH.get();
        permutation_entry<jint, jint> *entries = (permutation_entry<jint, jint> *) all;
        jint *perm = (jint *) ((permutation_entry<jint, jint> *) all + newLen);
        jint *newIndirections = (jint *) ((permutation_entry<jint, jint> *) all + newLen) + newLen;
        jint *oldIndirections = (jint *) ((permutation_entry<jint, jint> *) all + newLen) + 2 * newLen;

        for (jint i = 0; i < newLen; i++) {
            entries[i] = permutation_entry<jint, jint> (newIArr[i], i);
        }

        std::sort(entries, entries + newLen);

        for (jint i = 0; i < newLen; i++) {

            newIArr[i] = entries[i].value;
            perm[i] = entries[i].payload;

            newIndirections[i] = i;
        }

        for (jint i = 0; i < oldLen; i++) {
            oldIndirections[i] = i;
        }

        for (jint i = 1; i < newLen; i++) {

            if (entries[i - 1].value == entries[i].value) {
                throw std::runtime_error("Duplicate values are not allowed");
            }
        }

        mergeResult = merge( //
                oldIArr, oldIndirections, oldLen, //
                newIArr, newIndirections, newLen, //
                oldDArr, oldSArr, oldDOArr, ndims);

        // Copy the new assignments into the permutation and then copy back.

        newIndirections = mergeResult->newIndirections;

        for (jint i = 0; i < newLen; i++) {
            perm[i] = newIndirections[perm[i]];
        }

        memcpy(newIndirections, perm, sizeof(jint) * newLen);

        return mergeResult;

    } catch (...) {

        if (mergeResult) {
            delete mergeResult;
        }

        throw;
    }
}

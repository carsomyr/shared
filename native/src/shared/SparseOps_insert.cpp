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

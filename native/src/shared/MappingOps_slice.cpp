/*
 * Copyright (C) 2007 Roy Liu
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

#include <MappingOps.hpp>

void MappingOps::slice(JNIEnv *env, jobject thisObj, //
        jintArray slices, //
        jobject srcV, jintArray srcD, jintArray srcS, //
        jobject dstV, jintArray dstD, jintArray dstS) {

    MappingResult *mappingResult = NULL;

    try {

        if (!srcV || !srcD || !srcS || !dstV || !dstD || !dstS || !slices) {
            throw std::runtime_error("Invalid arguments");
        }

        //

        ArrayPinHandler::jarray_type type = NativeArrayKernel::getArrayType(env, srcV, dstV);

        //

        mappingResult = sliceProxy(env, slices, //
                (jarray) srcV, srcD, srcS, //
                (jarray) dstV, dstD, dstS);

        //

        assign(env, type, //
                (jarray) srcV, mappingResult->srcIndices, //
                (jarray) dstV, mappingResult->dstIndices, //
                mappingResult->nindices);

    } catch (std::exception &e) {

        Common::throwNew(env, e);
    }

    if (mappingResult) {
        delete mappingResult;
    }
}

MappingResult *MappingOps::sliceProxy(JNIEnv *env, //
        jintArray slices, //
        jarray srcV, jintArray srcD, jintArray srcS, //
        jarray dstV, jintArray dstD, jintArray dstS) {

    jint srcLen = env->GetArrayLength(srcV);
    jint dstLen = env->GetArrayLength(dstV);
    jint ndims = env->GetArrayLength(srcD);
    jint nslices = env->GetArrayLength(slices);

    if ((ndims != env->GetArrayLength(srcS)) //
            || (ndims != env->GetArrayLength(dstD)) //
            || (ndims != env->GetArrayLength(dstS)) //
            || (nslices % 3)) {
        throw std::runtime_error("Invalid arguments");
    }

    ArrayPinHandler slicesH(env, slices, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
    ArrayPinHandler srcDH(env, srcD, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
    ArrayPinHandler srcSH(env, srcS, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
    ArrayPinHandler dstDH(env, dstD, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
    ArrayPinHandler dstSH(env, dstS, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);

    jint *slicesArr = (jint *) slicesH.get();
    jint *srcDArr = (jint *) srcDH.get();
    jint *srcSArr = (jint *) srcSH.get();
    jint *dstDArr = (jint *) dstDH.get();
    jint *dstSArr = (jint *) dstSH.get();

    return slice(slicesArr, nslices / 3, //
            srcDArr, srcSArr, srcLen, //
            dstDArr, dstSArr, dstLen, //
            ndims);
}

MappingResult *MappingOps::slice( //
        const jint *slicesArr, jint nslices, //
        const jint *srcDArr, const jint *srcSArr, jint srcLen, //
        const jint *dstDArr, const jint *dstSArr, jint dstLen, //
        jint ndims) {

    MappingResult *res = NULL;

    try {

        // Perform checks.

        MappingOps::checkDimensions(srcDArr, srcSArr, ndims, srcLen);
        MappingOps::checkDimensions(dstDArr, dstSArr, ndims, dstLen);

        for (jint i = 0, n = 3 * nslices; i < n; i += 3) {

            jint srcIndex = slicesArr[i];
            jint dstIndex = slicesArr[i + 1];
            jint dim = slicesArr[i + 2];

            if (!(dim >= 0 && dim < ndims)) {
                throw std::runtime_error("Invalid dimension");
            }

            if (!(srcIndex >= 0 && srcIndex < srcDArr[dim]) //
                    || !(dstIndex >= 0 && dstIndex < dstDArr[dim])) {
                throw std::runtime_error("Invalid index");
            }
        }

        // Execute the slice operation.

        MallocHandler allH(sizeof(jint) * (2 * ndims + 2 * nslices) + sizeof(jint *) * (2 * ndims));
        void *all = (void *) allH.get();

        jint *dimCounts = (jint *) all;
        jint *dimAcc = (jint *) all + ndims;
        jint *ssiBacking = (jint *) all + 2 * ndims;
        jint *dsiBacking = (jint *) all + 2 * ndims + nslices;
        jint **ssiArr = (jint **) ((jint *) all + 2 * ndims + 2 * nslices);
        jint **dsiArr = (jint **) ((jint *) all + 2 * ndims + 2 * nslices) + ndims;

        // Prepare the dimension counts array.
        memset(dimCounts, 0, sizeof(jint) * ndims);
        memset(dimAcc, 0, sizeof(jint) * ndims);

        // Count the number of slices for each dimension.

        for (jint i = 0, n = 3 * nslices; i < n; i += 3) {
            dimCounts[slicesArr[i + 2]]++;
        }

        // Assign the slice array pointers.

        jint nindices = 1;

        for (jint dim = 0, acc = 0; dim < ndims; acc += dimCounts[dim++]) {

            ssiArr[dim] = ssiBacking + acc;
            dsiArr[dim] = dsiBacking + acc;

            nindices *= dimCounts[dim];
        }

        // Assign the slice array values.

        for (jint i = 0, n = 3 * nslices; i < n; i += 3) {

            jint dim = slicesArr[i + 2];
            jint idx = dimAcc[dim]++;

            ssiArr[dim][idx] = slicesArr[i];
            dsiArr[dim][idx] = slicesArr[i + 1];
        }

        res = new MappingResult();

        // Proceed only if there's something to slice.
        if (!nindices) {
            return res;
        }

        res->createIndices(nindices);

        MappingOps::assignSlicingIndices(res->srcIndices, dimCounts, srcSArr, ndims, ssiArr);
        MappingOps::assignSlicingIndices(res->dstIndices, dimCounts, dstSArr, ndims, dsiArr);

        return res;

    } catch (...) {

        if (res) {
            delete res;
        }

        throw;
    }
}

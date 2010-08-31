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

#include <MappingOps.hpp>

void MappingOps::map(JNIEnv *env, jobject thisObj, //
        jintArray bounds, //
        jobject srcV, jintArray srcD, jintArray srcS, //
        jobject dstV, jintArray dstD, jintArray dstS) {

    MappingResult *mappingResult = NULL;

    try {

        if (!srcV || !srcD || !srcS || !dstV || !dstD || !dstS || !bounds) {
            throw std::runtime_error("Invalid arguments");
        }

        //

        ArrayPinHandler::jarray_type type = NativeArrayKernel::getArrayType(env, srcV, dstV);

        //

        mappingResult = mapProxy(env, bounds, //
                (jarray) srcV, srcD, srcS, //
                (jarray) dstV, dstD, dstS);

        //

        assign(env, type, //
                (jarray) srcV, mappingResult->srcIndices, //
                (jarray) dstV, mappingResult->dstIndices, //
                mappingResult->nIndices);

    } catch (std::exception &e) {

        Common::throwNew(env, e);
    }

    if (mappingResult) {
        delete mappingResult;
    }
}

MappingResult *MappingOps::mapProxy(JNIEnv *env, //
        jintArray bounds, //
        jarray srcV, jintArray srcD, jintArray srcS, //
        jarray dstV, jintArray dstD, jintArray dstS) {

    jint srcLen = env->GetArrayLength(srcV);
    jint dstLen = env->GetArrayLength(dstV);
    jint nDims = env->GetArrayLength(srcD);

    if ((nDims != env->GetArrayLength(srcS)) //
            || (nDims != env->GetArrayLength(dstD)) //
            || (nDims != env->GetArrayLength(dstS)) //
            || (3 * nDims != env->GetArrayLength(bounds))) {
        throw std::runtime_error("Invalid arguments");
    }

    ArrayPinHandler boundsH(env, bounds, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
    ArrayPinHandler srcDH(env, srcD, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
    ArrayPinHandler srcSH(env, srcS, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
    ArrayPinHandler dstDH(env, dstD, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
    ArrayPinHandler dstSH(env, dstS, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
    // NO JNI AFTER THIS POINT!

    jint *boundsArr = (jint *) boundsH.get();
    jint *srcDArr = (jint *) srcDH.get();
    jint *srcSArr = (jint *) srcSH.get();
    jint *dstDArr = (jint *) dstDH.get();
    jint *dstSArr = (jint *) dstSH.get();

    return map(boundsArr, //
            srcDArr, srcSArr, srcLen, //
            dstDArr, dstSArr, dstLen, //
            nDims);
}

MappingResult *MappingOps::map( //
        const jint *boundsArr, //
        const jint *srcDArr, const jint *srcSArr, jint srcLen, //
        const jint *dstDArr, const jint *dstSArr, jint dstLen, //
        jint nDims) {

    MappingResult *res = NULL;

    try {

        // Perform checks.

        MappingOps::checkDimensions(srcDArr, srcSArr, nDims, srcLen);
        MappingOps::checkDimensions(dstDArr, dstSArr, nDims, dstLen);

        //

        jint nSlices = 0;
        jint mapLen = 1;

        for (jint dim = 0, offset = 0; dim < nDims; dim++, offset += 3) {

            jint size = boundsArr[offset + 2];

            if (size < 0) {
                throw std::runtime_error("Invalid mapping parameters");
            }

            nSlices += size;
            mapLen *= size;
        }

        res = new MappingResult();

        // Proceed only if nonzero length.
        if (!srcLen || !dstLen) {
            return res;
        }

        // Execute the map operation.

        MallocHandler allH(sizeof(jint) * (2 * nSlices + nDims) + sizeof(jint *) * (2 * nDims));
        void *all = (void *) allH.get();

        jint *ssiBacking = (jint *) all;
        jint *dsiBacking = (jint *) all + nSlices;
        jint *mapDimsArr = (jint *) all + 2 * nSlices;
        jint **ssiArr = (jint **) ((jint *) all + 2 * nSlices + nDims);
        jint **dsiArr = (jint **) ((jint *) all + 2 * nSlices + nDims) + nDims;

        // Assign slice array values.

        for (jint dim = 0, acc = 0, offset = 0; dim < nDims; dim++, acc += boundsArr[offset + 2], offset += 3) {

            jint mapSize = boundsArr[offset + 2];

            mapDimsArr[dim] = mapSize;

            jint *srcSlices = (ssiArr[dim] = ssiBacking + acc);
            jint *dstSlices = (dsiArr[dim] = dsiBacking + acc);

            for (jint j = 0, //
                    srcSize = srcDArr[dim], //
                    srcOffset = (((boundsArr[offset]) % srcSize) + srcSize) % srcSize, //
                    dstSize = dstDArr[dim], //
                    dstOffset = (((boundsArr[offset + 1]) % dstSize) + dstSize) % dstSize; //
            j < mapSize; //
            j++, //
            srcOffset = (srcOffset + 1) % srcSize, //
            dstOffset = (dstOffset + 1) % dstSize) {

                srcSlices[j] = srcOffset;
                dstSlices[j] = dstOffset;
            }
        }

        // Proceed only if there's something to map.
        if (!mapLen) {
            return res;
        }

        res->createIndices(mapLen);

        MappingOps::assignSlicingIndices(res->srcIndices, mapDimsArr, srcSArr, nDims, ssiArr);
        MappingOps::assignSlicingIndices(res->dstIndices, mapDimsArr, dstSArr, nDims, dsiArr);

        return res;

    } catch (...) {

        if (res) {
            delete res;
        }

        throw;
    }
}

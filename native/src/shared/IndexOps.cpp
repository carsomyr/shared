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

#include <IndexOps.hpp>

jintArray IndexOps::find(JNIEnv *env, jobject thisObj, //
        jintArray srcV, jintArray srcD, jintArray srcS, jintArray logical) {

    jintArray res = NULL;

    jint *indicesArr = NULL;

    try {

        indicesArr = IndexOps::findProxy(env, srcV, srcD, srcS, logical);

        jint size = indicesArr[0];

        res = Common::newIntArray(env, size);

        ArrayPinHandler resH(env, res, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_WRITE);
        // NO JNI AFTER THIS POINT!

        memcpy((jint *) resH.get(), indicesArr + 1, sizeof(jint) * size);

    } catch (std::exception &e) {

        Common::throwNew(env, e);
    }

    if (indicesArr) {
        delete[] indicesArr;
    }

    return res;
}

inline jint *IndexOps::findProxy(JNIEnv *env, //
        jintArray srcV, jintArray srcD, jintArray srcS, jintArray logical) {

    jint *resArr = NULL;

    try {

        if (!srcV || !srcD || !srcS || !logical) {
            throw std::runtime_error("Invalid arguments");
        }

        jint srcLen = env->GetArrayLength(srcV);
        jint nDims = env->GetArrayLength(srcD);

        if ((nDims != env->GetArrayLength(srcS)) || (nDims != env->GetArrayLength(logical))) {
            throw std::runtime_error("Invalid arguments");
        }

        // Initialize pinned arrays.

        ArrayPinHandler srcVH(env, srcV, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        ArrayPinHandler srcDH(env, srcD, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        ArrayPinHandler srcSH(env, srcS, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        ArrayPinHandler indicesH(env, logical, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        // NO JNI AFTER THIS POINT!

        jint *srcVArr = (jint *) srcVH.get();
        jint *srcDArr = (jint *) srcDH.get();
        jint *srcSArr = (jint *) srcSH.get();
        jint *logicalArr = (jint *) indicesH.get();

        MappingOps::checkDimensions(srcDArr, srcSArr, nDims, srcLen);

        jint activeDim = -1;
        jint count = 0;

        for (jint dim = 0; dim < nDims; dim++) {

            if (logicalArr[dim] == -1) {

                activeDim = dim;
                count++;
            }
        }

        if (count != 1) {
            throw std::runtime_error("Invalid arguments");
        }

        jint offset = 0;

        for (jint dim = 0; dim < nDims; dim++) {

            if (dim != activeDim) {

                jint index = logicalArr[dim];

                offset += index * srcSArr[dim];

                if (!(index >= 0 && index < srcDArr[dim])) {
                    throw std::runtime_error("Invalid index");
                }
            }
        }

        jint upper = 0;
        jint size = srcDArr[activeDim];
        jint stride = srcSArr[activeDim];

        for (jint i = 0, physical = offset; i < size; i++, physical += stride) {

            if (srcVArr[physical] >= 0) {
                upper++;
            }
        }

        jint *resArr = new jint[1 + upper];

        resArr[0] = upper;

        for (jint i = 1, physical = offset; i <= upper; i++, physical += stride) {
            resArr[i] = srcVArr[physical];
        }

        return resArr;

    } catch (...) {

        if (resArr) {
            delete[] resArr;
        }

        throw;
    }
}

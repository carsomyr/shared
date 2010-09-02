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

#include <NativeImageKernel.hpp>

void NativeImageKernel::init(JNIEnv *env) {
}

void NativeImageKernel::destroy(JNIEnv *env) {
}

void NativeImageKernel::createIntegralImage(JNIEnv *env, jobject thisObj, //
        jdoubleArray srcV, jintArray srcD, jintArray srcS, //
        jdoubleArray dstV, jintArray dstD, jintArray dstS) {

    try {

        if (!srcV || !srcD || !srcS || !dstV || !dstD || !dstS) {
            throw std::runtime_error("Invalid arguments");
        }

        jint srcLen = env->GetArrayLength(srcV);
        jint dstLen = env->GetArrayLength(dstV);
        jint nDims = env->GetArrayLength(srcD);

        if ((nDims != env->GetArrayLength(srcS))
                || (nDims != env->GetArrayLength(dstD))
                || (nDims != env->GetArrayLength(dstS))) {
            throw std::runtime_error("Invalid arguments");
        }

        ArrayPinHandler srcVH(env, srcV, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        ArrayPinHandler srcDH(env, srcD, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        ArrayPinHandler srcSH(env, srcS, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        ArrayPinHandler dstVH(env, dstV, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_WRITE);
        ArrayPinHandler dstDH(env, dstD, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        ArrayPinHandler dstSH(env, dstS, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        // NO JNI AFTER THIS POINT!

        jdouble *srcVArr = (jdouble *) srcVH.get();
        jint *srcDArr = (jint *) srcDH.get();
        jint *srcSArr = (jint *) srcSH.get();
        jdouble *dstVArr = (jdouble *) dstVH.get();
        jint *dstDArr = (jint *) dstDH.get();
        jint *dstSArr = (jint *) dstSH.get();

        MappingOps::checkDimensions(srcDArr, srcSArr, nDims, srcLen);
        MappingOps::checkDimensions(dstDArr, dstSArr, nDims, dstLen);

        jint dstOffset = 0;

        for (jint dim = 0; dim < nDims; dim++) {

            if (srcDArr[dim] + 1 != dstDArr[dim]) {
                throw std::runtime_error("Dimension mismatch");
            }

            dstOffset += dstSArr[dim];
        }

        if (!srcLen) {
            return;
        }

        MallocHandler mallocH(sizeof(jint) * (srcLen + dstLen));
        void *all = mallocH.get();

        jint *srcIndices = (jint *) all;
        jint *dstIndices = ((jint *) all) + srcLen;

        MappingOps::assignMappingIndices(srcIndices, srcDArr, srcSArr, nDims);
        MappingOps::assignMappingIndices(dstIndices, srcDArr, dstSArr, nDims);

        for (jint i = 0; i < srcLen; i++) {
            dstVArr[dstIndices[i] + dstOffset] = srcVArr[srcIndices[i]];
        }

        //

        MappingOps::assignMappingIndices(dstIndices, dstDArr, dstSArr, nDims);

        for (jint dim = 0, indexBlockIncrement = dstLen; dim < nDims; indexBlockIncrement /= dstDArr[dim++]) {

            jint size = dstDArr[dim];
            jint stride = dstSArr[dim];

            for (jint lower = 0, upper = indexBlockIncrement / size;
                    lower < dstLen;
                    lower += indexBlockIncrement, upper += indexBlockIncrement) {

                for (jint indexIndex = lower; indexIndex < upper; indexIndex++) {

                    jdouble acc = 0.0;

                    for (jint k = 0, physical = dstIndices[indexIndex]; k < size; k++, physical += stride) {

                        acc += dstVArr[physical];
                        dstVArr[physical] = acc;
                    }
                }
            }
        }

    } catch (std::exception &e) {

        Common::throwNew(env, e);
    }
}

void NativeImageKernel::createIntegralHistogram(JNIEnv *env, jobject thisObj, //
        jdoubleArray srcV, jintArray srcD, jintArray srcS, jintArray memV, //
        jdoubleArray dstV, jintArray dstD, jintArray dstS) {

    try {

        if (!srcV || !srcD || !srcS || !memV || !dstV || !dstD || !dstS) {
            throw std::runtime_error("Invalid arguments");
        }

        jint srcLen = env->GetArrayLength(srcV);
        jint memLen = env->GetArrayLength(memV);
        jint dstLen = env->GetArrayLength(dstV);
        jint nDims = env->GetArrayLength(srcD);

        if ((nDims != env->GetArrayLength(srcS))
                || (nDims + 1 != env->GetArrayLength(dstD))
                || (nDims + 1 != env->GetArrayLength(dstS))
                || (srcLen != memLen)) {
            throw std::runtime_error("Invalid arguments");
        }

        ArrayPinHandler srcVH(env, srcV, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        ArrayPinHandler srcDH(env, srcD, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        ArrayPinHandler srcSH(env, srcS, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        ArrayPinHandler memVH(env, memV, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        ArrayPinHandler dstVH(env, dstV, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_WRITE);
        ArrayPinHandler dstDH(env, dstD, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        ArrayPinHandler dstSH(env, dstS, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        // NO JNI AFTER THIS POINT!

        jdouble *srcVArr = (jdouble *) srcVH.get();
        jint *srcDArr = (jint *) srcDH.get();
        jint *srcSArr = (jint *) srcSH.get();
        jint *memVArr = (jint *) memVH.get();
        jdouble *dstVArr = (jdouble *) dstVH.get();
        jint *dstDArr = (jint *) dstDH.get();
        jint *dstSArr = (jint *) dstSH.get();

        MappingOps::checkDimensions(srcDArr, srcSArr, nDims, srcLen);
        MappingOps::checkDimensions(dstDArr, dstSArr, nDims + 1, dstLen);

        jint dstOffset = 0;

        for (jint dim = 0; dim < nDims; dim++) {

            if (srcDArr[dim] + 1 != dstDArr[dim]) {
                throw std::runtime_error("Dimension mismatch");
            }

            dstOffset += dstSArr[dim];
        }

        if (!srcLen) {
            return;
        }

        jint nBins = dstDArr[nDims];
        jint binStride = dstSArr[nDims];
        jint dstLenModified = dstLen / nBins;

        MallocHandler mallocH(sizeof(jint) * (srcLen + dstLenModified));
        void *all = mallocH.get();

        jint *srcIndices = (jint *) all;
        jint *dstIndices = ((jint *) all) + srcLen;

        MappingOps::assignMappingIndices(srcIndices, srcDArr, srcSArr, nDims);
        MappingOps::assignMappingIndices(dstIndices, srcDArr, dstSArr, nDims);

        for (jint i = 0; i < srcLen; i++) {

            jint index = memVArr[srcIndices[i]];

            if (!(index >= 0 && index < nBins)) {
                throw std::runtime_error("Invalid membership index");
            }

            dstVArr[dstIndices[i] + dstOffset + index * binStride] = srcVArr[srcIndices[i]];
        }

        //

        MappingOps::assignMappingIndices(dstIndices, dstDArr, dstSArr, nDims);

        for (jint dim = 0, indexBlockIncrement = dstLenModified;
                dim < nDims;
                indexBlockIncrement /= dstDArr[dim++]) {

            jint size = dstDArr[dim];
            jint stride = dstSArr[dim];

            for (jint lower = 0, upper = indexBlockIncrement / size;
                    lower < dstLenModified;
                    lower += indexBlockIncrement, upper += indexBlockIncrement) {

                for (jint indexIndex = lower; indexIndex < upper; indexIndex++) {

                    for (jint binIndex = 0, binOffset = 0; binIndex < nBins; binIndex++, binOffset += binStride) {

                        jdouble acc = 0.0;

                        for (jint k = 0, physical = dstIndices[indexIndex] + binOffset; k < size; k++, physical
                                += stride) {

                            acc += dstVArr[physical];
                            dstVArr[physical] = acc;
                        }
                    }
                }
            }
        }

    } catch (std::exception &e) {

        Common::throwNew(env, e);
    }
}

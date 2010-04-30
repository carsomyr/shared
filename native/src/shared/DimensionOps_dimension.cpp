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

#include <DimensionOps.hpp>

void DimensionOps::rdOp(JNIEnv *env, jobject thisObj, jint type, //
        jdoubleArray srcV, jintArray srcD, jintArray srcS, jdoubleArray dstV, //
        jintArray opDims) {

    try {

        void (*op)(jdouble *, const jint *, const jint *, jdouble *, const jint *, //
                jint, jint, jint) = NULL;

        switch (type) {

        case shared_array_kernel_ArrayKernel_RD_SUM:
            op = DimensionOps::rdSum;
            break;

        case shared_array_kernel_ArrayKernel_RD_PROD:
            op = DimensionOps::rdProd;
            break;

        default:
            throw std::runtime_error("Operation type not recognized");
        }

        if (!srcV || !srcD || !srcS || !dstV || !opDims) {
            throw std::runtime_error("Invalid arguments");
        }

        jint srcLen = env->GetArrayLength(srcV);
        jint dstLen = env->GetArrayLength(dstV);
        jint nDims = env->GetArrayLength(srcD);
        jint nOpDims = env->GetArrayLength(opDims);

        if ((nDims != env->GetArrayLength(srcS)) //
                || (srcLen != dstLen)) {
            throw std::runtime_error("Invalid arguments");
        }

        // Initialize pinned arrays.

        ArrayPinHandler srcVH(env, srcV, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        ArrayPinHandler srcDH(env, srcD, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        ArrayPinHandler srcSH(env, srcS, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        ArrayPinHandler dstVH(env, dstV, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_WRITE);
        ArrayPinHandler opDimsH(env, opDims, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        // NO JNI AFTER THIS POINT!

        jdouble *srcVArr = (jdouble *) srcVH.get();
        jint *srcDArr = (jint *) srcDH.get();
        jint *srcSArr = (jint *) srcSH.get();
        jdouble *dstVArr = (jdouble *) dstVH.get();
        jint *opDimsArr = (jint *) opDimsH.get();

        MappingOps::checkDimensions(srcDArr, srcSArr, nDims, srcLen);

        for (jint i = 0; i < nOpDims; i++) {

            jint dim = opDimsArr[i];

            if (!(dim >= 0 && dim < nDims)) {
                throw std::runtime_error("Invalid dimension");
            }
        }

        // Proceed only if non-zero length.
        if (!srcLen) {
            return;
        }

        // Execute the dimension operation.

        op(srcVArr, srcDArr, srcSArr, dstVArr, opDimsArr, srcLen, nDims, nOpDims);

    } catch (std::exception &e) {

        Common::throwNew(env, e);
    }
}

inline void DimensionOps::rdSum(jdouble *srcVArr, const jint *srcDArr, const jint *srcSArr, //
        jdouble *dstVArr, const jint *opDimsArr, //
        jint len, jint nDims, jint nOpDims) {

    MallocHandler mallocH(sizeof(jint) * (len + nDims));
    void *all = mallocH.get();

    jint *srcIndices = (jint *) all;
    jint *indicator = ((jint *) all) + len;

    memset(indicator, 0, sizeof(jint) * nDims);

    // Assign indicator values.

    for (jint i = 0; i < nOpDims; i++) {
        indicator[opDimsArr[i]] = 1;
    }

    // Take the sum along each dimension.

    memcpy(dstVArr, srcVArr, sizeof(jdouble) * len);

    MappingOps::assignMappingIndices(srcIndices, srcDArr, srcSArr, nDims);

    //

    for (jint dim = 0, indexBlockIncrement = len; dim < nDims; indexBlockIncrement /= srcDArr[dim++]) {

        if (!indicator[dim]) {
            continue;
        }

        jint size = srcDArr[dim];
        jint stride = srcSArr[dim];

        for (jint lower = 0, upper = indexBlockIncrement / size; lower < len; //
        lower += indexBlockIncrement, upper += indexBlockIncrement) {

            for (jint indexIndex = lower; indexIndex < upper; indexIndex++) {

                jdouble acc = 0.0;

                for (jint k = 0, physical = srcIndices[indexIndex]; k < size; k++, physical += stride) {

                    acc += dstVArr[physical];
                    dstVArr[physical] = acc;
                }
            }
        }
    }
}

inline void DimensionOps::rdProd(jdouble *srcVArr, const jint *srcDArr, const jint *srcSArr, //
        jdouble *dstVArr, const jint *opDimsArr, //
        jint len, jint nDims, jint nOpDims) {

    MallocHandler mallocH(sizeof(jint) * (len + nDims));
    void *all = mallocH.get();

    jint *srcIndices = (jint *) all;
    jint *indicator = ((jint *) all) + len;

    memset(indicator, 0, sizeof(jint) * nDims);

    // Assign indicator values.

    for (jint i = 0; i < nOpDims; i++) {
        indicator[opDimsArr[i]] = 1;
    }

    // Take the product along each dimension.

    memcpy(dstVArr, srcVArr, sizeof(jdouble) * len);

    MappingOps::assignMappingIndices(srcIndices, srcDArr, srcSArr, nDims);

    //

    for (jint dim = 0, indexBlockIncrement = len; dim < nDims; indexBlockIncrement /= srcDArr[dim++]) {

        if (!indicator[dim]) {
            continue;
        }

        jint size = srcDArr[dim];
        jint stride = srcSArr[dim];

        for (jint lower = 0, upper = indexBlockIncrement / size; lower < len; //
        lower += indexBlockIncrement, upper += indexBlockIncrement) {

            for (jint indexIndex = lower; indexIndex < upper; indexIndex++) {

                jdouble acc = 1.0;

                for (jint k = 0, physical = srcIndices[indexIndex]; k < size; k++, physical += stride) {

                    acc *= dstVArr[physical];
                    dstVArr[physical] = acc;
                }
            }
        }
    }
}

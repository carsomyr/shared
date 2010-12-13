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

#include <DimensionOps.hpp>

void DimensionOps::rrOp(JNIEnv *env, jobject thisObj, jint type, //
        jdoubleArray srcV, jintArray srcD, jintArray srcS, //
        jdoubleArray dstV, jintArray dstD, jintArray dstS, //
        jintArray opDims) {

    try {

        rrOp_t *op = NULL;

        switch (type) {

        case org_shared_array_kernel_ArrayKernel_RR_SUM:
            op = DimensionOps::rrSum;
            break;

        case org_shared_array_kernel_ArrayKernel_RR_PROD:
            op = DimensionOps::rrProd;
            break;

        case org_shared_array_kernel_ArrayKernel_RR_MAX:
            op = DimensionOps::rrMax;
            break;

        case org_shared_array_kernel_ArrayKernel_RR_MIN:
            op = DimensionOps::rrMin;
            break;

        case org_shared_array_kernel_ArrayKernel_RR_VAR:
            op = DimensionOps::rrVar;
            break;

        default:
            throw std::runtime_error("Operation type not recognized");
        }

        if (!srcV || !srcD || !srcS || !dstV || !dstD || !dstS || !opDims) {
            throw std::runtime_error("Invalid arguments");
        }

        jint srcLen = env->GetArrayLength(srcV);
        jint dstLen = env->GetArrayLength(dstV);
        jint nDims = env->GetArrayLength(srcD);
        jint nOpDims = env->GetArrayLength(opDims);

        if ((nDims != env->GetArrayLength(srcS))
                || (nDims != env->GetArrayLength(dstD))
                || (nDims != env->GetArrayLength(dstS))) {
            throw std::runtime_error("Invalid arguments");
        }

        // Initialize pinned arrays.

        ArrayPinHandler srcVh(env, srcV, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        ArrayPinHandler srcDh(env, srcD, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        ArrayPinHandler srcSh(env, srcS, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        ArrayPinHandler dstVh(env, dstV, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_WRITE);
        ArrayPinHandler dstDh(env, dstD, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        ArrayPinHandler dstSh(env, dstS, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        ArrayPinHandler opDimsH(env, opDims, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        // NO JNI AFTER THIS POINT!

        jdouble *srcVArr = (jdouble *) srcVh.get();
        jint *srcDArr = (jint *) srcDh.get();
        jint *srcSArr = (jint *) srcSh.get();
        jdouble *dstVArr = (jdouble *) dstVh.get();
        jint *dstDArr = (jint *) dstDh.get();
        jint *dstSArr = (jint *) dstSh.get();
        jint *opDimsArr = (jint *) opDimsH.get();

        MappingOps::checkDimensions(srcDArr, srcSArr, nDims, srcLen);
        MappingOps::checkDimensions(dstDArr, dstSArr, nDims, dstLen);

        std::sort(opDimsArr, opDimsArr + nOpDims);

        for (jint i = 1; i < nOpDims; i++) {

            if (opDimsArr[i - 1] == opDimsArr[i]) {
                throw std::runtime_error("Duplicate operating dimensions are not allowed");
            }
        }

        jint acc = dstLen;

        for (jint i = 0; i < nOpDims; i++) {

            jint dim = opDimsArr[i];

            if (!(dim >= 0 && dim < nDims)) {
                throw std::runtime_error("Invalid dimension");
            }

            if (dstDArr[dim] > 1) {
                throw std::runtime_error("Operating dimensions must have singleton or zero length");
            }

            acc *= srcDArr[dim];
        }

        if (acc != srcLen) {
            throw std::runtime_error("Invalid arguments");
        }

        // Proceed only if nonzero length.
        if (!srcLen) {
            return;
        }

        MallocHandler mallocH(sizeof(jdouble) * srcLen + sizeof(jint) * (srcLen + nDims + 2 * (nDims - 1) + dstLen));
        void *all = mallocH.get();

        jdouble *workingV = (jdouble *) all;
        jint *workingIndices = (jint *) ((jdouble *) all + srcLen);
        jint *workingD = (jint *) ((jdouble *) all + srcLen) + srcLen;
        jint *workingDModified = (jint *) ((jdouble *) all + srcLen) + srcLen + nDims;
        jint *srcSArrModified = (jint *) ((jdouble *) all + srcLen) + srcLen + nDims + (nDims - 1);
        jint *dstIndices = (jint *) ((jdouble *) all + srcLen) + srcLen + nDims + 2 * (nDims - 1);

        memcpy(workingV, srcVArr, sizeof(jdouble) * srcLen);
        memcpy(workingD, srcDArr, sizeof(jint) * nDims);

        acc = srcLen;

        for (jint i = 0; i < nOpDims; i++) {

            jint dim = opDimsArr[i];

            acc /= srcDArr[dim];

            // Assign indices while pretending that the dimension of interest doesn't exist.
            DimensionOps::assignBaseIndices(workingIndices, //
                    workingD, workingDModified, //
                    srcSArr, srcSArrModified, //
                    nDims, dim);

            // Execute the reduce operation.
            op(workingV, workingIndices, acc, workingD[dim], srcSArr[dim]);

            workingD[dim] = 1;
        }

        MappingOps::assignMappingIndices(workingIndices, dstDArr, srcSArr, nDims);
        MappingOps::assignMappingIndices(dstIndices, dstDArr, dstSArr, nDims);

        for (jint i = 0; i < dstLen; i++) {
            dstVArr[dstIndices[i]] = workingV[workingIndices[i]];
        }

    } catch (std::exception &e) {

        Common::throwNew(env, e);
    }
}

inline void DimensionOps::rrSum(jdouble *working, const jint *workingIndices, //
        jint nIndices, jint size, jint stride) {

    for (jint i = 0; i < nIndices; i++) {

        jint workingIndex = workingIndices[i];

        for (jint j = 1, offset = workingIndex + stride; j < size; j++, offset += stride) {
            working[workingIndex] += working[offset];
        }
    }
}

inline void DimensionOps::rrProd(jdouble *working, const jint *workingIndices, //
        jint nIndices, jint size, jint stride) {

    for (jint i = 0; i < nIndices; i++) {

        jint workingIndex = workingIndices[i];

        for (jint j = 1, offset = workingIndex + stride; j < size; j++, offset += stride) {
            working[workingIndex] *= working[offset];
        }
    }
}

inline void DimensionOps::rrMax(jdouble *working, const jint *workingIndices, //
        jint nIndices, jint size, jint stride) {

    for (jint i = 0; i < nIndices; i++) {

        jint workingIndex = workingIndices[i];

        for (jint j = 1, offset = workingIndex + stride; j < size; j++, offset += stride) {
            working[workingIndex] = std::max<jdouble>(working[offset], working[workingIndex]);
        }
    }
}

inline void DimensionOps::rrMin(jdouble *working, const jint *workingIndices, //
        jint nIndices, jint size, jint stride) {

    for (jint i = 0; i < nIndices; i++) {

        jint workingIndex = workingIndices[i];

        for (jint j = 1, offset = workingIndex + stride; j < size; j++, offset += stride) {
            working[workingIndex] = std::min<jdouble>(working[offset], working[workingIndex]);
        }
    }
}

inline void DimensionOps::rrVar(jdouble *working, const jint *workingIndices, //
        jint nIndices, jint size, jint stride) {

    for (jint i = 0; i < nIndices; i++) {

        jint workingIndex = workingIndices[i];

        jdouble mean = 0.0;

        for (jint j = 0, offset = workingIndex; j < size; j++, offset += stride) {
            mean += working[offset];
        }

        mean /= size;

        for (jint j = 0, offset = workingIndex; j < size; j++, offset += stride) {

            jdouble diff = working[offset] - mean;
            working[offset] = diff * diff;
        }

        for (jint j = 1, offset = workingIndex + stride; j < size; j++, offset += stride) {
            working[workingIndex] += working[offset];
        }

        working[workingIndex] /= size;
    }
}

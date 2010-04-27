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

void MappingOps::assignMappingIndices( //
        jint *indices, const jint *dims, const jint *strides, //
        jint nDims) {

    indices[0] = 0;

    for (jint k = nDims - 1, blockSize = 1, stride, size; k >= 0; blockSize *= size, k--) {

        stride = strides[k];
        size = dims[k];

        for (jint offset = blockSize, m = blockSize * size; offset < m; offset += blockSize) {

            for (jint i = offset - blockSize, j = offset; i < offset; i++, j++) {
                indices[j] = indices[i] + stride;
            }
        }
    }
}

void MappingOps::assignSlicingIndices( //
        jint *indices, const jint *dims, const jint *strides, //
        jint nDims, //
        jint **sliceIndices) {

    indices[0] = 0;

    for (jint dim = 0; dim < nDims; dim++) {
        indices[0] += strides[dim] * sliceIndices[dim][0];
    }

    for (jint k = nDims - 1, blockSize = 1, strideOffset, size; k >= 0; blockSize *= size, k--) {

        jint *dimSlices = sliceIndices[k];
        size = dims[k];

        for (jint offset = blockSize, m = blockSize * size, n = 1; offset < m; offset += blockSize, n++) {

            strideOffset = strides[k] * (dimSlices[n] - dimSlices[n - 1]);

            for (jint i = offset - blockSize, j = offset; i < offset; i++, j++) {
                indices[j] = indices[i] + strideOffset;
            }
        }
    }
}

void MappingOps::checkDimensions( //
        const jint *dims, const jint *strides, //
        jint nDims, jint len) {

    jint acc = 0;

    for (jint dim = 0; dim < nDims; dim++) {

        if (dims[dim] < 0 || strides[dim] < 0) {
            throw std::runtime_error("Invalid dimensions and/or strides");
        }

        acc += (dims[dim] - 1) * strides[dim];
    }

    if (acc != len - 1) {
        throw std::runtime_error("Invalid dimensions and/or strides");
    }
}

void MappingOps::assign(JNIEnv *env, ArrayPinHandler::jarray_type type, //
        jarray srcV, jint *srcIndices, //
        jarray dstV, jint *dstIndices, //
        jint nIndices) {

    switch (type) {

    case ArrayPinHandler::DOUBLE:

    {
        ArrayPinHandler srcVH(env, srcV, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        ArrayPinHandler dstVH(env, dstV, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_WRITE);

        jdouble *srcVArr = (jdouble *) srcVH.get();
        jdouble *dstVArr = (jdouble *) dstVH.get();

        for (jint i = 0; i < nIndices; i++) {
            dstVArr[dstIndices[i]] = srcVArr[srcIndices[i]];
        }
    }

        break;

    case ArrayPinHandler::INT:

    {
        ArrayPinHandler srcVH(env, srcV, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        ArrayPinHandler dstVH(env, dstV, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_WRITE);

        jint *srcVArr = (jint *) srcVH.get();
        jint *dstVArr = (jint *) dstVH.get();

        for (jint i = 0; i < nIndices; i++) {
            dstVArr[dstIndices[i]] = srcVArr[srcIndices[i]];
        }
    }

        break;

    case ArrayPinHandler::OBJECT:

    {
        for (jint i = 0; i < nIndices; i++) {
            env->SetObjectArrayElement((jobjectArray) dstV, dstIndices[i], //
                    env->GetObjectArrayElement((jobjectArray) srcV, srcIndices[i]));
        }
    }

        break;

    default:
        throw std::runtime_error("Invalid array type");
    }
}

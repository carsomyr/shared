/*
 * This file is part of the Shared Scientific Toolbox in Java ("this library").
 * 
 * Copyright (C) 2007 Roy Liu
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

#include <MappingOps.hpp>

void MappingOps::assignMappingIndices( //
        jint *indices, const jint *dims, const jint *strides, //
        jint ndims) {

    indices[0] = 0;

    for (jint k = ndims - 1, blockSize = 1, stride, size; k >= 0; blockSize *= size, k--) {

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
        jint ndims, //
        jint **sliceIndices) {

    indices[0] = 0;

    for (jint dim = 0; dim < ndims; dim++) {
        indices[0] += strides[dim] * sliceIndices[dim][0];
    }

    for (jint k = ndims - 1, blockSize = 1, strideOffset, size; k >= 0; blockSize *= size, k--) {

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
        jint ndims, jint len) {

    jint acc = 0;

    for (jint dim = 0; dim < ndims; dim++) {

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
        jint nindices) {

    switch (type) {

    case ArrayPinHandler::DOUBLE:

    {
        ArrayPinHandler srcVH(env, srcV, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        ArrayPinHandler dstVH(env, dstV, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_WRITE);

        jdouble *srcVArr = (jdouble *) srcVH.get();
        jdouble *dstVArr = (jdouble *) dstVH.get();

        for (jint i = 0; i < nindices; i++) {
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

        for (jint i = 0; i < nindices; i++) {
            dstVArr[dstIndices[i]] = srcVArr[srcIndices[i]];
        }
    }

        break;

    case ArrayPinHandler::OBJECT:

    {
        for (jint i = 0; i < nindices; i++) {
            env->SetObjectArrayElement((jobjectArray) dstV, dstIndices[i], //
                    env->GetObjectArrayElement((jobjectArray) srcV, srcIndices[i]));
        }
    }

        break;

    default:
        throw std::runtime_error("Invalid array type");
    }
}

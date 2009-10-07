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

#include <DimensionOps.hpp>

void DimensionOps::rdOp(JNIEnv *env, jobject thisObj, jint type, //
        jdoubleArray srcV, jintArray srcD, jintArray srcS, jdoubleArray dstV, //
        jintArray selectedDims) {

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

        if (!srcV || !srcD || !srcS || !dstV || !selectedDims) {
            throw std::runtime_error("Invalid arguments");
        }

        jint srcLen = env->GetArrayLength(srcV);
        jint dstLen = env->GetArrayLength(dstV);
        jint ndims = env->GetArrayLength(srcD);
        jint nselectedDims = env->GetArrayLength(selectedDims);

        if ((ndims != env->GetArrayLength(srcS)) //
                || (srcLen != dstLen)) {
            throw std::runtime_error("Invalid arguments");
        }

        // Initialize pinned arrays.

        ArrayPinHandler srcVH(env, srcV, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        ArrayPinHandler srcDH(env, srcD, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        ArrayPinHandler srcSH(env, srcS, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        ArrayPinHandler dstVH(env, dstV, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_WRITE);
        ArrayPinHandler selectedDimsH(env, //
                selectedDims, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        // NO JNI AFTER THIS POINT!

        jdouble *srcVArr = (jdouble *) srcVH.get();
        jint *srcDArr = (jint *) srcDH.get();
        jint *srcSArr = (jint *) srcSH.get();
        jdouble *dstVArr = (jdouble *) dstVH.get();
        jint *selectedDimsArr = (jint *) selectedDimsH.get();

        MappingOps::checkDimensions(srcDArr, srcSArr, ndims, srcLen);

        for (jint i = 0; i < nselectedDims; i++) {

            jint dim = selectedDimsArr[i];

            if (!(dim >= 0 && dim < ndims)) {
                throw std::runtime_error("Invalid dimension");
            }
        }

        // Proceed only if non-zero length.
        if (!srcLen) {
            return;
        }

        // Execute the dimension operation.

        op(srcVArr, srcDArr, srcSArr, dstVArr, selectedDimsArr, srcLen, ndims, nselectedDims);

    } catch (std::exception &e) {

        Common::throwNew(env, e);
    }
}

inline void DimensionOps::rdSum(jdouble *srcVArr, const jint *srcDArr, const jint *srcSArr, //
        jdouble *dstVArr, const jint *selectedDimsArr, //
        jint len, jint ndims, jint nselectedDims) {

    MallocHandler mallocH(sizeof(jint) * (len + ndims));
    void *all = mallocH.get();

    jint *srcIndices = (jint *) all;
    jint *indicator = ((jint *) all) + len;

    memset(indicator, 0, sizeof(jint) * ndims);

    // Assign indicator values.

    for (jint i = 0; i < nselectedDims; i++) {
        indicator[selectedDimsArr[i]] = 1;
    }

    // Take the sum along each dimension.

    memcpy(dstVArr, srcVArr, sizeof(jdouble) * len);

    MappingOps::assignMappingIndices(srcIndices, srcDArr, srcSArr, ndims);

    //

    for (jint dim = 0, indexBlockIncrement = len; dim < ndims; indexBlockIncrement /= srcDArr[dim++]) {

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
        jdouble *dstVArr, const jint *selectedDimsArr, //
        jint len, jint ndims, jint nselectedDims) {

    MallocHandler mallocH(sizeof(jint) * (len + ndims));
    void *all = mallocH.get();

    jint *srcIndices = (jint *) all;
    jint *indicator = ((jint *) all) + len;

    memset(indicator, 0, sizeof(jint) * ndims);

    // Assign indicator values.

    for (jint i = 0; i < nselectedDims; i++) {
        indicator[selectedDimsArr[i]] = 1;
    }

    // Take the product along each dimension.

    memcpy(dstVArr, srcVArr, sizeof(jdouble) * len);

    MappingOps::assignMappingIndices(srcIndices, srcDArr, srcSArr, ndims);

    //

    for (jint dim = 0, indexBlockIncrement = len; dim < ndims; indexBlockIncrement /= srcDArr[dim++]) {

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

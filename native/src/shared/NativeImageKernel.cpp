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
        jint ndims = env->GetArrayLength(srcD);

        if ((ndims != env->GetArrayLength(srcS)) //
                || (ndims != env->GetArrayLength(dstD)) //
                || (ndims != env->GetArrayLength(dstS))) {
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

        MappingOps::checkDimensions(srcDArr, srcSArr, ndims, srcLen);
        MappingOps::checkDimensions(dstDArr, dstSArr, ndims, dstLen);

        jint dstOffset = 0;

        for (jint dim = 0; dim < ndims; dim++) {

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

        MappingOps::assignMappingIndices(srcIndices, srcDArr, srcSArr, ndims);
        MappingOps::assignMappingIndices(dstIndices, srcDArr, dstSArr, ndims);

        for (jint i = 0; i < srcLen; i++) {
            dstVArr[dstIndices[i] + dstOffset] = srcVArr[srcIndices[i]];
        }

        //

        MappingOps::assignMappingIndices(dstIndices, dstDArr, dstSArr, ndims);

        for (jint dim = 0, indexBlockIncrement = dstLen; dim < ndims; indexBlockIncrement /= dstDArr[dim++]) {

            jint size = dstDArr[dim];
            jint stride = dstSArr[dim];

            for (jint lower = 0, upper = indexBlockIncrement / size; //
            lower < dstLen; //
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
        jint ndims = env->GetArrayLength(srcD);

        if ((ndims != env->GetArrayLength(srcS)) //
                || (ndims + 1 != env->GetArrayLength(dstD)) //
                || (ndims + 1 != env->GetArrayLength(dstS)) //
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

        MappingOps::checkDimensions(srcDArr, srcSArr, ndims, srcLen);
        MappingOps::checkDimensions(dstDArr, dstSArr, ndims + 1, dstLen);

        jint dstOffset = 0;

        for (jint dim = 0; dim < ndims; dim++) {

            if (srcDArr[dim] + 1 != dstDArr[dim]) {
                throw std::runtime_error("Dimension mismatch");
            }

            dstOffset += dstSArr[dim];
        }

        if (!srcLen) {
            return;
        }

        jint nbins = dstDArr[ndims];
        jint binStride = dstSArr[ndims];
        jint dstLenModified = dstLen / nbins;

        MallocHandler mallocH(sizeof(jint) * (srcLen + dstLenModified));
        void *all = mallocH.get();

        jint *srcIndices = (jint *) all;
        jint *dstIndices = ((jint *) all) + srcLen;

        MappingOps::assignMappingIndices(srcIndices, srcDArr, srcSArr, ndims);
        MappingOps::assignMappingIndices(dstIndices, srcDArr, dstSArr, ndims);

        for (jint i = 0; i < srcLen; i++) {

            jint index = memVArr[srcIndices[i]];

            if (!(index >= 0 && index < nbins)) {
                throw std::runtime_error("Invalid membership index");
            }

            dstVArr[dstIndices[i] + dstOffset + index * binStride] = srcVArr[srcIndices[i]];
        }

        //

        MappingOps::assignMappingIndices(dstIndices, dstDArr, dstSArr, ndims);

        for (jint dim = 0, indexBlockIncrement = dstLenModified; dim < ndims; //
        indexBlockIncrement /= dstDArr[dim++]) {

            jint size = dstDArr[dim];
            jint stride = dstSArr[dim];

            for (jint lower = 0, upper = indexBlockIncrement / size; //
            lower < dstLenModified; //
            lower += indexBlockIncrement, upper += indexBlockIncrement) {

                for (jint indexIndex = lower; indexIndex < upper; indexIndex++) {

                    for (jint binIndex = 0, binOffset = 0; binIndex < nbins; binIndex++, binOffset += binStride) {

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

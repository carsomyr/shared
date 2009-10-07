/*
 * This file is part of the Shared Scientific Toolbox in Java ("this library").
 * 
 * Copyright (C) 2009 Roy Liu
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
        jint ndims = env->GetArrayLength(srcD);

        if ((ndims != env->GetArrayLength(srcS)) //
                || (ndims != env->GetArrayLength(logical))) {
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

        MappingOps::checkDimensions(srcDArr, srcSArr, ndims, srcLen);

        jint activeDim = -1;
        jint count = 0;

        for (jint dim = 0; dim < ndims; dim++) {

            if (logicalArr[dim] == -1) {

                activeDim = dim;
                count++;
            }
        }

        if (count != 1) {
            throw std::runtime_error("Invalid arguments");
        }

        jint offset = 0;

        for (jint dim = 0; dim < ndims; dim++) {

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

        throw ;
    }
}

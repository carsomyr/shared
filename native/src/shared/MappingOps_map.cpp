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
                mappingResult->nindices);

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
    jint ndims = env->GetArrayLength(srcD);

    if ((ndims != env->GetArrayLength(srcS)) //
            || (ndims != env->GetArrayLength(dstD)) //
            || (ndims != env->GetArrayLength(dstS)) //
            || (3 * ndims != env->GetArrayLength(bounds))) {
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
            ndims);
}

MappingResult *MappingOps::map( //
        const jint *boundsArr, //
        const jint *srcDArr, const jint *srcSArr, jint srcLen, //
        const jint *dstDArr, const jint *dstSArr, jint dstLen, //
        jint ndims) {

    MappingResult *res = NULL;

    try {

        // Perform checks.

        MappingOps::checkDimensions(srcDArr, srcSArr, ndims, srcLen);
        MappingOps::checkDimensions(dstDArr, dstSArr, ndims, dstLen);

        //

        jint nslices = 0;
        jint mapLen = 1;

        for (jint dim = 0, offset = 0; dim < ndims; dim++, offset += 3) {

            jint size = boundsArr[offset + 2];

            if (size < 0) {
                throw std::runtime_error("Invalid mapping parameters");
            }

            nslices += size;
            mapLen *= size;
        }

        res = new MappingResult();

        // Proceed only if non-zero length.
        if (!srcLen || !dstLen) {
            return res;
        }

        // Execute the map operation.

        MallocHandler allH(sizeof(jint) * (2 * nslices + ndims) + sizeof(jint *) * (2 * ndims));
        void *all = (void *) allH.get();

        jint *ssiBacking = (jint *) all;
        jint *dsiBacking = (jint *) all + nslices;
        jint *mapDimsArr = (jint *) all + 2 * nslices;
        jint **ssiArr = (jint **) ((jint *) all + 2 * nslices + ndims);
        jint **dsiArr = (jint **) ((jint *) all + 2 * nslices + ndims) + ndims;

        // Assign slice array values.

        for (jint dim = 0, acc = 0, offset = 0; dim < ndims; dim++, acc += boundsArr[offset + 2], offset += 3) {

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

        MappingOps::assignSlicingIndices(res->srcIndices, mapDimsArr, srcSArr, ndims, ssiArr);
        MappingOps::assignSlicingIndices(res->dstIndices, mapDimsArr, dstSArr, ndims, dsiArr);

        return res;

    } catch (...) {

        if (res) {
            delete res;
        }

        throw ;
    }
}

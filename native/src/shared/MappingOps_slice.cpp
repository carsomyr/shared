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

void MappingOps::slice(JNIEnv *env, jobject thisObj, //
        jintArray slices, //
        jobject srcV, jintArray srcD, jintArray srcS, //
        jobject dstV, jintArray dstD, jintArray dstS) {

    MappingResult *mappingResult = NULL;

    try {

        if (!srcV || !srcD || !srcS || !dstV || !dstD || !dstS || !slices) {
            throw std::runtime_error("Invalid arguments");
        }

        //

        ArrayPinHandler::jarray_type type = NativeArrayKernel::getArrayType(env, srcV, dstV);

        //

        mappingResult = sliceProxy(env, slices, //
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

MappingResult *MappingOps::sliceProxy(JNIEnv *env, //
        jintArray slices, //
        jarray srcV, jintArray srcD, jintArray srcS, //
        jarray dstV, jintArray dstD, jintArray dstS) {

    jint srcLen = env->GetArrayLength(srcV);
    jint dstLen = env->GetArrayLength(dstV);
    jint ndims = env->GetArrayLength(srcD);
    jint nslices = env->GetArrayLength(slices);

    if ((ndims != env->GetArrayLength(srcS)) //
            || (ndims != env->GetArrayLength(dstD)) //
            || (ndims != env->GetArrayLength(dstS)) //
            || (nslices % 3)) {
        throw std::runtime_error("Invalid arguments");
    }

    ArrayPinHandler slicesH(env, slices, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
    ArrayPinHandler srcDH(env, srcD, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
    ArrayPinHandler srcSH(env, srcS, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
    ArrayPinHandler dstDH(env, dstD, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
    ArrayPinHandler dstSH(env, dstS, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);

    jint *slicesArr = (jint *) slicesH.get();
    jint *srcDArr = (jint *) srcDH.get();
    jint *srcSArr = (jint *) srcSH.get();
    jint *dstDArr = (jint *) dstDH.get();
    jint *dstSArr = (jint *) dstSH.get();

    return slice(slicesArr, nslices / 3, //
            srcDArr, srcSArr, srcLen, //
            dstDArr, dstSArr, dstLen, //
            ndims);
}

MappingResult *MappingOps::slice( //
        const jint *slicesArr, jint nslices, //
        const jint *srcDArr, const jint *srcSArr, jint srcLen, //
        const jint *dstDArr, const jint *dstSArr, jint dstLen, //
        jint ndims) {

    MappingResult *res = NULL;

    try {

        // Perform checks.

        MappingOps::checkDimensions(srcDArr, srcSArr, ndims, srcLen);
        MappingOps::checkDimensions(dstDArr, dstSArr, ndims, dstLen);

        for (jint i = 0, n = 3 * nslices; i < n; i += 3) {

            jint srcIndex = slicesArr[i];
            jint dstIndex = slicesArr[i + 1];
            jint dim = slicesArr[i + 2];

            if (!(dim >= 0 && dim < ndims)) {
                throw std::runtime_error("Invalid dimension");
            }

            if (!(srcIndex >= 0 && srcIndex < srcDArr[dim]) //
                    || !(dstIndex >= 0 && dstIndex < dstDArr[dim])) {
                throw std::runtime_error("Invalid index");
            }
        }

        // Execute the slice operation.

        MallocHandler allH(sizeof(jint) * (2 * ndims + 2 * nslices) + sizeof(jint *) * (2 * ndims));
        void *all = (void *) allH.get();

        jint *dimCounts = (jint *) all;
        jint *dimAcc = (jint *) all + ndims;
        jint *ssiBacking = (jint *) all + 2 * ndims;
        jint *dsiBacking = (jint *) all + 2 * ndims + nslices;
        jint **ssiArr = (jint **) ((jint *) all + 2 * ndims + 2 * nslices);
        jint **dsiArr = (jint **) ((jint *) all + 2 * ndims + 2 * nslices) + ndims;

        // Prepare the dimension counts array.
        memset(dimCounts, 0, sizeof(jint) * ndims);
        memset(dimAcc, 0, sizeof(jint) * ndims);

        // Count the number of slices for each dimension.

        for (jint i = 0, n = 3 * nslices; i < n; i += 3) {
            dimCounts[slicesArr[i + 2]]++;
        }

        // Assign the slice array pointers.

        jint nindices = 1;

        for (jint dim = 0, acc = 0; dim < ndims; acc += dimCounts[dim++]) {

            ssiArr[dim] = ssiBacking + acc;
            dsiArr[dim] = dsiBacking + acc;

            nindices *= dimCounts[dim];
        }

        // Assign the slice array values.

        for (jint i = 0, n = 3 * nslices; i < n; i += 3) {

            jint dim = slicesArr[i + 2];
            jint idx = dimAcc[dim]++;

            ssiArr[dim][idx] = slicesArr[i];
            dsiArr[dim][idx] = slicesArr[i + 1];
        }

        res = new MappingResult();

        // Proceed only if there's something to slice.
        if (!nindices) {
            return res;
        }

        res->createIndices(nindices);

        MappingOps::assignSlicingIndices(res->srcIndices, dimCounts, srcSArr, ndims, ssiArr);
        MappingOps::assignSlicingIndices(res->dstIndices, dimCounts, dstSArr, ndims, dsiArr);

        return res;

    } catch (...) {

        if (res) {
            delete res;
        }

        throw;
    }
}

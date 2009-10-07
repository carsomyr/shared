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

#include <SparseOps.hpp>

jobject SparseOps::slice(JNIEnv *env, jobject thisObj, //
        jintArray slices, //
        jobject srcV, jintArray srcD, jintArray srcS, jintArray srcDO, //
        jintArray srcI, jintArray srcIO, jintArray srcII, //
        jobject dstV, jintArray dstD, jintArray dstS, jintArray dstDO, //
        jintArray dstI, jintArray dstIO, jintArray dstII) {

    jobject res = NULL;

    MergeResult *mergeResult = NULL;

    try {

        if (!slices //
                || !srcV || !srcD || !srcS || !srcDO //
                || !srcI || !srcIO || !srcII //
                || !dstV || !dstD || !dstS || !dstDO //
                || !dstI || !dstIO || !dstII) {
            throw std::runtime_error("Invalid arguments");
        }

        //

        ArrayPinHandler::jarray_type type = NativeArrayKernel::getArrayType(env, srcV, dstV);

        //

        mergeResult = mergeProxy(env, slices, //
                (jarray) srcV, srcD, srcS, srcDO, //
                srcI, srcIO, srcII, //
                (jarray) dstV, dstD, dstS, dstDO, //
                dstI, dstIO, dstII);

        //

        res = createSparseArrayState(env, type, mergeResult, (jarray) dstV, (jarray) srcV);

    } catch (std::exception &e) {

        Common::throwNew(env, e);
    }

    if (mergeResult) {
        delete mergeResult;
    }

    return res;
}

MergeResult *SparseOps::mergeProxy(JNIEnv *env, //
        jintArray slices, //
        jarray srcV, jintArray srcD, jintArray srcS, jintArray srcDO, //
        jintArray srcI, jintArray srcIO, jintArray srcII, //
        jarray dstV, jintArray dstD, jintArray dstS, jintArray dstDO, //
        jintArray dstI, jintArray dstIO, jintArray dstII) {

    MergeResult *mergeResult = NULL;

    try {

        jint srcLen = env->GetArrayLength(srcV);
        jint dstLen = env->GetArrayLength(dstV);
        jint ndims = env->GetArrayLength(srcD);
        jint srcIOLen = env->GetArrayLength(srcIO);
        jint dstIOLen = env->GetArrayLength(dstIO);

        jint nslices = env->GetArrayLength(slices) / 3;

        if ((ndims != env->GetArrayLength(srcS)) //
                || (ndims != env->GetArrayLength(dstD)) //
                || (ndims != env->GetArrayLength(dstS)) //
                || (env->GetArrayLength(slices) % 3) //
                || (srcLen != env->GetArrayLength(srcI)) //
                || (dstLen != env->GetArrayLength(dstI)) //
                || (ndims + 1 != env->GetArrayLength(srcDO)) //
                || (ndims * srcLen != env->GetArrayLength(srcII)) //
                || (ndims + 1 != env->GetArrayLength(dstDO)) //
                || (ndims * dstLen != env->GetArrayLength(dstII))) {
            throw std::runtime_error("Invalid arguments");
        }

        ArrayPinHandler slicesH(env, slices, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        ArrayPinHandler srcDH(env, srcD, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        ArrayPinHandler srcSH(env, srcS, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        ArrayPinHandler srcIH(env, srcI, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        ArrayPinHandler srcDOH(env, srcDO, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        ArrayPinHandler srcIOH(env, srcIO, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        ArrayPinHandler srcIIH(env, srcII, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        ArrayPinHandler dstDH(env, dstD, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        ArrayPinHandler dstSH(env, dstS, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        ArrayPinHandler dstIH(env, dstI, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        ArrayPinHandler dstDOH(env, dstDO, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        ArrayPinHandler dstIOH(env, dstIO, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        ArrayPinHandler dstIIH(env, dstII, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);

        jint *slicesArr = (jint *) slicesH.get();
        jint *srcDArr = (jint *) srcDH.get();
        jint *srcSArr = (jint *) srcSH.get();
        jint *srcIArr = (jint *) srcIH.get();
        jint *srcDOArr = (jint *) srcDOH.get();
        jint *srcIOArr = (jint *) srcIOH.get();
        jint *srcIIArr = (jint *) srcIIH.get();
        jint *dstDArr = (jint *) dstDH.get();
        jint *dstSArr = (jint *) dstSH.get();
        jint *dstIArr = (jint *) dstIH.get();
        jint *dstDOArr = (jint *) dstDOH.get();
        jint *dstIOArr = (jint *) dstIOH.get();
        jint *dstIIArr = (jint *) dstIIH.get();

        if ((srcIOLen != Common::sum(srcDArr, ndims, (jint) 0) + ndims) //
                || (dstIOLen != Common::sum(dstDArr, ndims, (jint) 0) + ndims)) {
            throw std::runtime_error("Invalid arguments");
        }

        MappingOps::checkDimensions(srcDArr, srcSArr, ndims, Common::product(srcDArr, ndims, (jint) 1));
        MappingOps::checkDimensions(dstDArr, dstSArr, ndims, Common::product(dstDArr, ndims, (jint) 1));

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

        for (jint dim = 0; dim < ndims; dim++) {

            if ((srcDOArr[dim + 1] - srcDOArr[dim] - 1 != srcDArr[dim]) //
                    || (dstDOArr[dim + 1] - dstDOArr[dim] - 1 != dstDArr[dim])) {
                throw std::runtime_error("Invalid arguments");
            }
        }

        //

        jint offsetArrayLen = srcDOArr[ndims];

        MallocHandler all0H(sizeof(jint) * (2 * ndims + 2 * offsetArrayLen + 1 + srcLen + dstLen));
        jint *all0 = (jint *) all0H.get();
        jint *srcSliceCounts = all0;
        jint *lookupCounts = all0 + ndims;
        jint *sliceOffsets = all0 + ndims + offsetArrayLen;
        jint *lookupOffsets = all0 + 2 * ndims + offsetArrayLen + 1;
        jint *srcIndirections = all0 + 2 * ndims + 2 * offsetArrayLen + 1;
        jint *dstIndirections = all0 + 2 * ndims + 2 * offsetArrayLen + 1 + srcLen;

        memset(srcSliceCounts, 0, sizeof(jint) * ndims);
        memset(lookupCounts, 0, sizeof(jint) * offsetArrayLen);

        for (jint i = 0, n = 3 * nslices; i < n; i += 3) {

            jint srcIndex = slicesArr[i];
            jint dim = slicesArr[i + 2];

            srcSliceCounts[dim]++;
            lookupCounts[srcDOArr[dim] + srcIndex]++;
        }

        //

        jint sliceOffset = 0;
        jint lookupOffset = 0;

        for (jint dim = 0; dim < ndims; dim++) {

            sliceOffsets[dim] = sliceOffset;
            sliceOffset += srcSliceCounts[dim];

            jint dimSize = srcDArr[dim];

            for (jint dimIndex = 0; dimIndex < dimSize; dimIndex++) {

                lookupOffsets[srcDOArr[dim] + dimIndex] = lookupOffset;
                lookupOffset += lookupCounts[srcDOArr[dim] + dimIndex];
            }

            lookupOffsets[srcDOArr[dim] + dimSize] = lookupOffset;
        }

        sliceOffsets[ndims] = sliceOffset;

        //

        MallocHandler all1H(sizeof(jint) * (2 * sliceOffset + ndims + lookupOffset));
        jint *all1 = (jint *) all1H.get();
        jint *srcSlices = all1;
        jint *dstSlices = all1 + sliceOffset;
        jint *dstSliceCounts = all1 + 2 * sliceOffset;
        jint *dstLookups = all1 + 2 * sliceOffset + ndims;

        memset(srcSliceCounts, 0, sizeof(jint) * ndims);
        memset(lookupCounts, 0, sizeof(jint) * offsetArrayLen);

        for (jint i = 0, n = 3 * nslices; i < n; i += 3) {

            jint srcIndex = slicesArr[i];
            jint dstIndex = slicesArr[i + 1];
            jint dim = slicesArr[i + 2];

            srcSlices[sliceOffsets[dim] + srcSliceCounts[dim]] = srcIndex;
            dstSlices[sliceOffsets[dim] + srcSliceCounts[dim]] = dstIndex;
            dstLookups[lookupOffsets[srcDOArr[dim] + srcIndex] //
                    + lookupCounts[srcDOArr[dim] + srcIndex]] = dstIndex;

            srcSliceCounts[dim]++;
            lookupCounts[srcDOArr[dim] + srcIndex]++;
        }

        //

        memset(srcSliceCounts, 0, sizeof(jint) * ndims);
        memset(lookupCounts, 0, sizeof(jint) * offsetArrayLen);

        for (jint dim = 0; dim < ndims; dim++) {

            srcSliceCounts[dim] = normalize(srcSlices, sliceOffsets[dim], sliceOffsets[dim + 1]);
            dstSliceCounts[dim] = normalize(dstSlices, sliceOffsets[dim], sliceOffsets[dim + 1]);

            for (jint dimIndex = 0, dimSize = srcDArr[dim]; dimIndex < dimSize; dimIndex++) {
                lookupCounts[srcDOArr[dim] + dimIndex] = normalize(dstLookups, //
                        lookupOffsets[srcDOArr[dim] + dimIndex], //
                        lookupOffsets[srcDOArr[dim] + dimIndex + 1]);
            }
        }

        //

        jint nsrcIndirections;

        getSlicedIndirections(sliceOffsets, srcSliceCounts, srcSlices, //
                srcDOArr, srcIOArr, srcIIArr, srcLen, //
                srcDArr, ndims, //
                srcIndirections, nsrcIndirections);

        jint ndstIndirections;

        getSlicedIndirections(sliceOffsets, dstSliceCounts, dstSlices, //
                dstDOArr, dstIOArr, dstIIArr, dstLen, //
                dstDArr, ndims, //
                dstIndirections, ndstIndirections);

        //

        MallocHandler all2H(sizeof(jint) * (nsrcIndirections + 1 + 2 * dstLen - 2 * ndstIndirections));
        jint *all2 = (jint *) all2H.get();
        jint *indirectionOffsets = all2;
        jint *oldIndices = all2 + nsrcIndirections + 1;
        jint *oldIndirections = all2 + nsrcIndirections + 1 + dstLen - ndstIndirections;

        jint indirectionOffset = 0;

        for (jint i = 0, prodD = Common::product(srcDArr, ndims, (jint) 1); i < nsrcIndirections; i++) {

            jint indirection = srcIndirections[i];

            if (!(indirection >= 0 && indirection < srcLen)) {
                throw std::runtime_error("Invalid indirection index");
            }

            jint physical = srcIArr[indirection];

            if (!(physical >= 0 && physical < prodD)) {
                throw std::runtime_error("Invalid physical index");
            }

            jint mapLen = 1;

            for (jint dim = 0; dim < ndims; dim++) {

                jint logicalIndex = physical / srcSArr[dim];

                mapLen *= lookupCounts[srcDOArr[dim] + logicalIndex];

                physical %= srcSArr[dim];
            }

            indirectionOffsets[i] = indirectionOffset;
            indirectionOffset += mapLen;
        }

        indirectionOffsets[nsrcIndirections] = indirectionOffset;

        //

        MallocHandler all3H(sizeof(jint) * (4 * indirectionOffset + ndims));
        jint *all3 = (jint *) all3H.get();
        jint *newIndirections = all3;
        jint *newIndices = all3 + indirectionOffset;
        jint *resNewIndirections = all3 + 2 * indirectionOffset;
        jint *resNewIndices = all3 + 3 * indirectionOffset;
        jint *logical = all3 + 4 * indirectionOffset;

        for (jint i = 0; i < nsrcIndirections; i++) {

            jint indirection = srcIndirections[i];
            jint physical = srcIArr[indirection];

            indirectionOffset = indirectionOffsets[i];

            newIndices[indirectionOffset] = 0;

            for (jint dim = 0; dim < ndims; dim++) {

                logical[dim] = physical / srcSArr[dim];

                newIndices[indirectionOffset] += dstSArr[dim] //
                        * dstLookups[lookupOffsets[srcDOArr[dim] + logical[dim]]];

                physical %= srcSArr[dim];
            }

            std::fill( //
                    newIndirections + indirectionOffsets[i], //
                    newIndirections + indirectionOffsets[i + 1], //
                    indirection);

            for (jint dim = ndims - 1, blockSize = 1, size; dim >= 0; blockSize *= size, dim--) {

                jint start = lookupOffsets[srcDOArr[dim] + logical[dim]];
                size = lookupCounts[srcDOArr[dim] + logical[dim]];

                for (jint offset = indirectionOffset + blockSize, //
                        offsetEnd = indirectionOffset + blockSize * size, n = start + 1; //
                offset < offsetEnd; offset += blockSize, n++) {

                    jint strideOffset = dstSArr[dim] * (dstLookups[n] - dstLookups[n - 1]);

                    for (jint j = offset - blockSize, k = offset; j < offset; j++, k++) {
                        newIndices[k] = newIndices[j] + strideOffset;
                    }
                }
            }
        }

        //

        if (ndstIndirections > 0) {

            jint count = 0;

            for (jint i = 0, m = dstIndirections[0]; i < m; i++, count++) {

                oldIndices[count] = dstIArr[i];
                oldIndirections[count] = i;
            }

            for (jint i = 0, n = ndstIndirections - 1; i < n; i++) {

                for (jint j = dstIndirections[i] + 1, m = dstIndirections[i + 1]; j < m; j++, count++) {

                    oldIndices[count] = dstIArr[j];
                    oldIndirections[count] = j;
                }
            }

            for (jint i = dstIndirections[ndstIndirections - 1] + 1, m = dstLen; i < m; i++, count++) {

                oldIndices[count] = dstIArr[i];
                oldIndirections[count] = i;
            }

        } else {

            memcpy(oldIndices, dstIArr, sizeof(jint) * dstLen);

            for (jint i = 0; i < dstLen; i++) {
                oldIndirections[i] = i;
            }
        }

        //

        jint resLen;

        merge(newIndices, newIndirections, indirectionOffsets[nsrcIndirections], //
                resNewIndices, resNewIndirections, resLen);

        mergeResult = merge(oldIndices, oldIndirections, dstLen - ndstIndirections, //
                resNewIndices, resNewIndirections, resLen, //
                dstDArr, dstSArr, dstDOArr, ndims);

        return mergeResult;

    } catch (...) {

        if (mergeResult) {
            delete mergeResult;
        }

        throw ;
    }
}

jint SparseOps::normalize(jint *values, jint start, jint end) {

    std::sort(values + start, values + end);

    jint propagate = 0;

    for (jint i = start, current = -1; i < end; i++) {

        values[i - propagate] = values[i];

        if (current != values[i]) {

            current = values[i];

        } else {

            propagate++;
        }
    }

    return end - start - propagate;
}

void SparseOps::getSlicedIndirections( //
        jint *sliceOffsets, jint *sliceCounts, jint *slices, //
        jint *dimOffsets, jint *indirectionOffsets, jint *indirections, jint nindirections, //
        jint *dims, jint ndims, //
        jint *result, jint &len) {

    jint *intersection = NULL;
    len = 0;

    for (jint dim = 0; dim < ndims; dim++) {

        jint dimOffset = dimOffsets[dim];

        jint sliceStart = sliceOffsets[dim];
        jint sliceEnd = sliceOffsets[dim] + sliceCounts[dim];

        jint nslices = sliceEnd - sliceStart;

        MallocHandler all0H(sizeof(jint) * (2 * nslices));
        jint *all0 = (jint *) all0H.get();
        jint *indirectionLowers = all0;
        jint *indirectionUppers = all0 + nslices;

        jint nelts = 0;

        for (jint i = 0; i < nslices; i++) {

            jint index = slices[i + sliceStart];

            jint start = indirectionOffsets[dimOffset + index];
            jint end = indirectionOffsets[dimOffset + index + 1];

            if (!(start >= 0 //
                    && start <= end //
                    && end >= 0 //
                    && end <= nindirections)) {
                throw std::runtime_error("Invalid arguments");
            }

            indirectionLowers[i] = start;
            indirectionUppers[i] = end;

            nelts += end - start;
        }

        MallocHandler all1H(sizeof(jint) * nelts);
        jint *all1 = (jint *) all1H.get();
        jint *res = all1;

        for (jint i = 0, resCount = 0; i < nslices; i++) {

            jint index = slices[i + sliceStart];

            jint start = indirectionOffsets[dimOffset + index];
            jint end = indirectionOffsets[dimOffset + index + 1];

            for (jint j = start; j < end; j++) {
                res[resCount++] = indirections[nindirections * dim + j];
            }
        }

        std::sort(res, res + nelts);

        if (intersection != NULL) {

            jint intersectionLower = 0;
            jint intersectionUpper = len;

            jint resLower = 0;
            jint resUpper = nelts;

            jint propagate = 0;

            for (; intersectionLower < intersectionUpper && resLower < resUpper;) {

                if (intersection[intersectionLower] < res[resLower]) {

                    intersection[intersectionLower - propagate] = intersection[intersectionLower];
                    propagate++;

                    intersectionLower++;

                } else if (intersection[intersectionLower] > res[resLower]) {

                    resLower++;

                } else {

                    intersection[intersectionLower - propagate] = intersection[intersectionLower];

                    intersectionLower++;
                    resLower++;
                }
            }

            len = intersectionLower - propagate;

        } else {

            intersection = result;
            len = nelts;

            memcpy(intersection, res, sizeof(jint) * len);
        }
    }
}

void SparseOps::merge(jint *indices, jint *indirections, jint len, //
        jint *resIndices, jint *resIndirections, jint &resLen) {

    MallocHandler allH(sizeof(permutation_entry<jint, jint> ) * len);
    permutation_entry<jint, jint> *all = (permutation_entry<jint, jint> *) allH.get();
    permutation_entry<jint, jint> *entries = all;

    for (jint i = 0; i < len; i++) {
        entries[i] = permutation_entry<jint, jint> (indices[i], indirections[i]);
    }

    std::stable_sort(entries, entries + len);

    jint propagate = 0;

    for (jint i = 0, current = -1; i < len; i++) {

        entries[i - propagate] = entries[i];

        if (current != entries[i].value) {

            current = entries[i].value;

        } else {

            propagate++;
        }
    }

    resLen = len - propagate;

    for (jint i = 0; i < resLen; i++) {

        resIndices[i] = entries[i].value;
        resIndirections[i] = entries[i].payload;
    }
}

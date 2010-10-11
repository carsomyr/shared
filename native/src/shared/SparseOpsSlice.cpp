/*
 * Copyright (c) 2009 Roy Liu
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

#include <SparseOps.hpp>

jobject SparseOps::slice(JNIEnv *env, jobject thisObj, //
        jintArray slices, //
        jobject srcV, jintArray srcD, jintArray srcS, jintArray srcDo, //
        jintArray srcI, jintArray srcIo, jintArray srcIi, //
        jobject dstV, jintArray dstD, jintArray dstS, jintArray dstDo, //
        jintArray dstI, jintArray dstIo, jintArray dstIi) {

    jobject res = NULL;

    MergeResult *mergeResult = NULL;

    try {

        if (!slices
                || !srcV || !srcD || !srcS || !srcDo
                || !srcI || !srcIo || !srcIi
                || !dstV || !dstD || !dstS || !dstDo
                || !dstI || !dstIo || !dstIi) {
            throw std::runtime_error("Invalid arguments");
        }

        //

        ArrayPinHandler::jarray_type type = NativeArrayKernel::getArrayType(env, srcV, dstV);

        //

        mergeResult = mergeProxy(env, slices, //
                (jarray) srcV, srcD, srcS, srcDo, //
                srcI, srcIo, srcIi, //
                (jarray) dstV, dstD, dstS, dstDo, //
                dstI, dstIo, dstIi);

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
        jarray srcV, jintArray srcD, jintArray srcS, jintArray srcDo, //
        jintArray srcI, jintArray srcIo, jintArray srcIi, //
        jarray dstV, jintArray dstD, jintArray dstS, jintArray dstDo, //
        jintArray dstI, jintArray dstIo, jintArray dstIi) {

    MergeResult *mergeResult = NULL;

    try {

        jint srcLen = env->GetArrayLength(srcV);
        jint dstLen = env->GetArrayLength(dstV);
        jint nDims = env->GetArrayLength(srcD);
        jint srcIoLen = env->GetArrayLength(srcIo);
        jint dstIoLen = env->GetArrayLength(dstIo);

        jint nSlices = env->GetArrayLength(slices) / 3;

        if ((nDims != env->GetArrayLength(srcS))
                || (nDims != env->GetArrayLength(dstD))
                || (nDims != env->GetArrayLength(dstS))
                || (env->GetArrayLength(slices) % 3)
                || (srcLen != env->GetArrayLength(srcI))
                || (dstLen != env->GetArrayLength(dstI))
                || (nDims + 1 != env->GetArrayLength(srcDo))
                || (nDims * srcLen != env->GetArrayLength(srcIi))
                || (nDims + 1 != env->GetArrayLength(dstDo))
                || (nDims * dstLen != env->GetArrayLength(dstIi))) {
            throw std::runtime_error("Invalid arguments");
        }

        ArrayPinHandler slicesH(env, slices, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        ArrayPinHandler srcDh(env, srcD, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        ArrayPinHandler srcSh(env, srcS, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        ArrayPinHandler srcIh(env, srcI, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        ArrayPinHandler srcDoh(env, srcDo, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        ArrayPinHandler srcIoh(env, srcIo, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        ArrayPinHandler srcIih(env, srcIi, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        ArrayPinHandler dstDh(env, dstD, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        ArrayPinHandler dstSh(env, dstS, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        ArrayPinHandler dstIh(env, dstI, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        ArrayPinHandler dstDoh(env, dstDo, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        ArrayPinHandler dstIoh(env, dstIo, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        ArrayPinHandler dstIih(env, dstIi, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);

        jint *slicesArr = (jint *) slicesH.get();
        jint *srcDArr = (jint *) srcDh.get();
        jint *srcSArr = (jint *) srcSh.get();
        jint *srcIArr = (jint *) srcIh.get();
        jint *srcDoArr = (jint *) srcDoh.get();
        jint *srcIoArr = (jint *) srcIoh.get();
        jint *srcIiArr = (jint *) srcIih.get();
        jint *dstDArr = (jint *) dstDh.get();
        jint *dstSArr = (jint *) dstSh.get();
        jint *dstIArr = (jint *) dstIh.get();
        jint *dstDoArr = (jint *) dstDoh.get();
        jint *dstIoArr = (jint *) dstIoh.get();
        jint *dstIiArr = (jint *) dstIih.get();

        if ((srcIoLen != Common::sum(srcDArr, nDims, (jint) 0) + nDims)
                || (dstIoLen != Common::sum(dstDArr, nDims, (jint) 0) + nDims)) {
            throw std::runtime_error("Invalid arguments");
        }

        MappingOps::checkDimensions(srcDArr, srcSArr, nDims, Common::product(srcDArr, nDims, (jint) 1));
        MappingOps::checkDimensions(dstDArr, dstSArr, nDims, Common::product(dstDArr, nDims, (jint) 1));

        for (jint i = 0, n = 3 * nSlices; i < n; i += 3) {

            jint srcIndex = slicesArr[i];
            jint dstIndex = slicesArr[i + 1];
            jint dim = slicesArr[i + 2];

            if (!(dim >= 0 && dim < nDims)) {
                throw std::runtime_error("Invalid dimension");
            }

            if (!(srcIndex >= 0 && srcIndex < srcDArr[dim])
                    || !(dstIndex >= 0 && dstIndex < dstDArr[dim])) {
                throw std::runtime_error("Invalid index");
            }
        }

        for (jint dim = 0; dim < nDims; dim++) {

            if ((srcDoArr[dim + 1] - srcDoArr[dim] - 1 != srcDArr[dim])
                    || (dstDoArr[dim + 1] - dstDoArr[dim] - 1 != dstDArr[dim])) {
                throw std::runtime_error("Invalid arguments");
            }
        }

        //

        jint offsetArrayLen = srcDoArr[nDims];

        MallocHandler all0H(sizeof(jint) * (2 * nDims + 2 * offsetArrayLen + 1 + srcLen + dstLen));
        jint *all0 = (jint *) all0H.get();
        jint *srcSliceCounts = all0;
        jint *lookupCounts = all0 + nDims;
        jint *sliceOffsets = all0 + nDims + offsetArrayLen;
        jint *lookupOffsets = all0 + 2 * nDims + offsetArrayLen + 1;
        jint *srcIndirections = all0 + 2 * nDims + 2 * offsetArrayLen + 1;
        jint *dstIndirections = all0 + 2 * nDims + 2 * offsetArrayLen + 1 + srcLen;

        memset(srcSliceCounts, 0, sizeof(jint) * nDims);
        memset(lookupCounts, 0, sizeof(jint) * offsetArrayLen);

        for (jint i = 0, n = 3 * nSlices; i < n; i += 3) {

            jint srcIndex = slicesArr[i];
            jint dim = slicesArr[i + 2];

            srcSliceCounts[dim]++;
            lookupCounts[srcDoArr[dim] + srcIndex]++;
        }

        //

        jint sliceOffset = 0;
        jint lookupOffset = 0;

        for (jint dim = 0; dim < nDims; dim++) {

            sliceOffsets[dim] = sliceOffset;
            sliceOffset += srcSliceCounts[dim];

            jint dimSize = srcDArr[dim];

            for (jint dimIndex = 0; dimIndex < dimSize; dimIndex++) {

                lookupOffsets[srcDoArr[dim] + dimIndex] = lookupOffset;
                lookupOffset += lookupCounts[srcDoArr[dim] + dimIndex];
            }

            lookupOffsets[srcDoArr[dim] + dimSize] = lookupOffset;
        }

        sliceOffsets[nDims] = sliceOffset;

        //

        MallocHandler all1H(sizeof(jint) * (2 * sliceOffset + nDims + lookupOffset));
        jint *all1 = (jint *) all1H.get();
        jint *srcSlices = all1;
        jint *dstSlices = all1 + sliceOffset;
        jint *dstSliceCounts = all1 + 2 * sliceOffset;
        jint *dstLookups = all1 + 2 * sliceOffset + nDims;

        memset(srcSliceCounts, 0, sizeof(jint) * nDims);
        memset(lookupCounts, 0, sizeof(jint) * offsetArrayLen);

        for (jint i = 0, n = 3 * nSlices; i < n; i += 3) {

            jint srcIndex = slicesArr[i];
            jint dstIndex = slicesArr[i + 1];
            jint dim = slicesArr[i + 2];

            srcSlices[sliceOffsets[dim] + srcSliceCounts[dim]] = srcIndex;
            dstSlices[sliceOffsets[dim] + srcSliceCounts[dim]] = dstIndex;
            dstLookups[lookupOffsets[srcDoArr[dim] + srcIndex] + lookupCounts[srcDoArr[dim] + srcIndex]] = dstIndex;

            srcSliceCounts[dim]++;
            lookupCounts[srcDoArr[dim] + srcIndex]++;
        }

        //

        memset(srcSliceCounts, 0, sizeof(jint) * nDims);
        memset(lookupCounts, 0, sizeof(jint) * offsetArrayLen);

        for (jint dim = 0; dim < nDims; dim++) {

            srcSliceCounts[dim] = normalize(srcSlices, sliceOffsets[dim], sliceOffsets[dim + 1]);
            dstSliceCounts[dim] = normalize(dstSlices, sliceOffsets[dim], sliceOffsets[dim + 1]);

            for (jint dimIndex = 0, dimSize = srcDArr[dim]; dimIndex < dimSize; dimIndex++) {
                lookupCounts[srcDoArr[dim] + dimIndex] = normalize(dstLookups, //
                        lookupOffsets[srcDoArr[dim] + dimIndex], //
                        lookupOffsets[srcDoArr[dim] + dimIndex + 1]);
            }
        }

        //

        jint nSrcIndirections;

        getSlicedIndirections(sliceOffsets, srcSliceCounts, srcSlices, //
                srcDoArr, srcIoArr, srcIiArr, srcLen, //
                srcDArr, nDims, //
                srcIndirections, nSrcIndirections);

        jint nDstIndirections;

        getSlicedIndirections(sliceOffsets, dstSliceCounts, dstSlices, //
                dstDoArr, dstIoArr, dstIiArr, dstLen, //
                dstDArr, nDims, //
                dstIndirections, nDstIndirections);

        //

        MallocHandler all2H(sizeof(jint) * (nSrcIndirections + 1 + 2 * dstLen - 2 * nDstIndirections));
        jint *all2 = (jint *) all2H.get();
        jint *indirectionOffsets = all2;
        jint *oldIndices = all2 + nSrcIndirections + 1;
        jint *oldIndirections = all2 + nSrcIndirections + 1 + dstLen - nDstIndirections;

        jint indirectionOffset = 0;

        for (jint i = 0, prodD = Common::product(srcDArr, nDims, (jint) 1); i < nSrcIndirections; i++) {

            jint indirection = srcIndirections[i];

            if (!(indirection >= 0 && indirection < srcLen)) {
                throw std::runtime_error("Invalid indirection index");
            }

            jint physical = srcIArr[indirection];

            if (!(physical >= 0 && physical < prodD)) {
                throw std::runtime_error("Invalid physical index");
            }

            jint mapLen = 1;

            for (jint dim = 0; dim < nDims; dim++) {

                jint logicalIndex = physical / srcSArr[dim];

                mapLen *= lookupCounts[srcDoArr[dim] + logicalIndex];

                physical %= srcSArr[dim];
            }

            indirectionOffsets[i] = indirectionOffset;
            indirectionOffset += mapLen;
        }

        indirectionOffsets[nSrcIndirections] = indirectionOffset;

        //

        MallocHandler all3H(sizeof(jint) * (4 * indirectionOffset + nDims));
        jint *all3 = (jint *) all3H.get();
        jint *newIndirections = all3;
        jint *newIndices = all3 + indirectionOffset;
        jint *resNewIndirections = all3 + 2 * indirectionOffset;
        jint *resNewIndices = all3 + 3 * indirectionOffset;
        jint *logical = all3 + 4 * indirectionOffset;

        for (jint i = 0; i < nSrcIndirections; i++) {

            jint indirection = srcIndirections[i];
            jint physical = srcIArr[indirection];

            indirectionOffset = indirectionOffsets[i];

            newIndices[indirectionOffset] = 0;

            for (jint dim = 0; dim < nDims; dim++) {

                logical[dim] = physical / srcSArr[dim];

                newIndices[indirectionOffset] += dstSArr[dim] * dstLookups[lookupOffsets[srcDoArr[dim] + logical[dim]]];

                physical %= srcSArr[dim];
            }

            std::fill( //
                    newIndirections + indirectionOffsets[i], //
                    newIndirections + indirectionOffsets[i + 1], //
                    indirection);

            for (jint dim = nDims - 1, blockSize = 1, size; dim >= 0; blockSize *= size, dim--) {

                jint start = lookupOffsets[srcDoArr[dim] + logical[dim]];
                size = lookupCounts[srcDoArr[dim] + logical[dim]];

                for (jint offset = indirectionOffset + blockSize,
                        offsetEnd = indirectionOffset + blockSize * size, n = start + 1;
                        offset < offsetEnd;
                        offset += blockSize, n++) {

                    jint strideOffset = dstSArr[dim] * (dstLookups[n] - dstLookups[n - 1]);

                    for (jint j = offset - blockSize, k = offset; j < offset; j++, k++) {
                        newIndices[k] = newIndices[j] + strideOffset;
                    }
                }
            }
        }

        //

        if (nDstIndirections > 0) {

            jint count = 0;

            for (jint i = 0, n = dstIndirections[0]; i < n; i++, count++) {

                oldIndices[count] = dstIArr[i];
                oldIndirections[count] = i;
            }

            for (jint i = 0, n = nDstIndirections - 1; i < n; i++) {

                for (jint j = dstIndirections[i] + 1, m = dstIndirections[i + 1]; j < m; j++, count++) {

                    oldIndices[count] = dstIArr[j];
                    oldIndirections[count] = j;
                }
            }

            for (jint i = dstIndirections[nDstIndirections - 1] + 1, n = dstLen; i < n; i++, count++) {

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

        merge(newIndices, newIndirections, indirectionOffsets[nSrcIndirections], //
                resNewIndices, resNewIndirections, resLen);

        mergeResult = merge(oldIndices, oldIndirections, dstLen - nDstIndirections,
                resNewIndices, resNewIndirections, resLen,
                dstDArr, dstSArr, dstDoArr, nDims);

        return mergeResult;

    } catch (...) {

        if (mergeResult) {
            delete mergeResult;
        }

        throw;
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
        jint *dimOffsets, jint *indirectionOffsets, jint *indirections, jint nIndirections, //
        jint *dims, jint nDims, //
        jint *result, jint &len) {

    jint *intersection = NULL;
    len = 0;

    for (jint dim = 0; dim < nDims; dim++) {

        jint dimOffset = dimOffsets[dim];

        jint sliceStart = sliceOffsets[dim];
        jint sliceEnd = sliceOffsets[dim] + sliceCounts[dim];

        jint nSlices = sliceEnd - sliceStart;

        MallocHandler all0H(sizeof(jint) * (2 * nSlices));
        jint *all0 = (jint *) all0H.get();
        jint *indirectionLowers = all0;
        jint *indirectionUppers = all0 + nSlices;

        jint nElts = 0;

        for (jint i = 0; i < nSlices; i++) {

            jint index = slices[i + sliceStart];

            jint start = indirectionOffsets[dimOffset + index];
            jint end = indirectionOffsets[dimOffset + index + 1];

            if (!(start >= 0 && start <= end && end >= 0 && end <= nIndirections)) {
                throw std::runtime_error("Invalid arguments");
            }

            indirectionLowers[i] = start;
            indirectionUppers[i] = end;

            nElts += end - start;
        }

        MallocHandler all1H(sizeof(jint) * nElts);
        jint *all1 = (jint *) all1H.get();
        jint *res = all1;

        for (jint i = 0, resCount = 0; i < nSlices; i++) {

            jint index = slices[i + sliceStart];

            jint start = indirectionOffsets[dimOffset + index];
            jint end = indirectionOffsets[dimOffset + index + 1];

            for (jint j = start; j < end; j++) {
                res[resCount++] = indirections[nIndirections * dim + j];
            }
        }

        std::sort(res, res + nElts);

        if (intersection != NULL) {

            jint intersectionLower = 0;
            jint intersectionUpper = len;

            jint resLower = 0;
            jint resUpper = nElts;

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
            len = nElts;

            memcpy(intersection, res, sizeof(jint) * len);
        }
    }
}

void SparseOps::merge(jint *indices, jint *indirections, jint len, //
        jint *resIndices, jint *resIndirections, jint &resLen) {

    MallocHandler allH(sizeof(permutation_entry<jint, jint>) * len);
    permutation_entry<jint, jint> *all = (permutation_entry<jint, jint> *) allH.get();
    permutation_entry<jint, jint> *entries = all;

    for (jint i = 0; i < len; i++) {
        entries[i] = permutation_entry<jint, jint>(indices[i], indirections[i]);
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

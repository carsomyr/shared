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

void DimensionOps::riOp(JNIEnv *env, jobject thisObj, jint type, //
        jdoubleArray srcV, jintArray srcD, jintArray srcS, //
        jintArray dstV, //
        jint dim) {

    try {

        void (*op)(jdouble *, const jint *, jint *, jint, jint, jint) = NULL;

        switch (type) {

        case shared_array_kernel_ArrayKernel_RI_MAX:
            op = DimensionOps::riMax;
            break;

        case shared_array_kernel_ArrayKernel_RI_MIN:
            op = DimensionOps::riMin;
            break;

        case shared_array_kernel_ArrayKernel_RI_ZERO:
            op = DimensionOps::riZero;
            break;

        case shared_array_kernel_ArrayKernel_RI_GZERO:
            op = DimensionOps::riGZero;
            break;

        case shared_array_kernel_ArrayKernel_RI_LZERO:
            op = DimensionOps::riLZero;
            break;

        case shared_array_kernel_ArrayKernel_RI_SORT:
            op = DimensionOps::riSort;
            break;

        default:
            throw std::runtime_error("Operation type not recognized");
        }

        if (!srcV || !srcD || !srcS || !dstV) {
            throw std::runtime_error("Invalid arguments");
        }

        jint srcLen = env->GetArrayLength(srcV);
        jint dstLen = env->GetArrayLength(dstV);
        jint nDims = env->GetArrayLength(srcD);

        if (!(dim >= -1 && dim < nDims) || (nDims != env->GetArrayLength(srcS)) || (srcLen != dstLen)) {
            throw std::runtime_error("Invalid arguments");
        }

        // Initialize pinned arrays.

        // Remember that the source array is modifiable, too!
        ArrayPinHandler srcVh(env, srcV, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_WRITE);
        ArrayPinHandler srcDh(env, srcD, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        ArrayPinHandler srcSh(env, srcS, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        ArrayPinHandler dstVh(env, dstV, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_WRITE);
        // NO JNI AFTER THIS POINT!

        jdouble *srcVArr = (jdouble *) srcVh.get();
        jint *srcDArr = (jint *) srcDh.get();
        jint *srcSArr = (jint *) srcSh.get();
        jint *dstVArr = (jint *) dstVh.get();

        MappingOps::checkDimensions(srcDArr, srcSArr, nDims, srcLen);

        // Proceed only if nonzero length.
        if (!srcLen) {
            return;
        }

        if (dim != -1) {

            jint nIndices = dstLen / srcDArr[dim];

            MallocHandler mallocH(sizeof(jint) * (nIndices + 2 * (nDims - 1)));
            void *all = mallocH.get();

            jint *srcIndices = (jint *) all;
            jint *srcDArrModified = (jint *) all + nIndices;
            jint *srcSArrModified = (jint *) all + nIndices + (nDims - 1);

            // Assign indices while pretending that the dimension of interest doesn't exist.
            DimensionOps::assignBaseIndices(srcIndices, srcDArr, srcDArrModified, srcSArr, srcSArrModified, //
                    nDims, dim);

            // Execute the index operation.

            op(srcVArr, srcIndices, dstVArr, nIndices, srcDArr[dim], srcSArr[dim]);

        } else {

            op(srcVArr, NULL, dstVArr, srcLen, -1, -1);
        }

    } catch (std::exception &e) {

        Common::throwNew(env, e);
    }
}

inline void DimensionOps::riMax(jdouble *src, const jint *srcIndices, jint *dst, //
        jint nIndices, jint size, jint stride) {

    if (srcIndices) {

        jint maxStride = stride * size;

        for (jint i = 0; i < nIndices; i++) {

            jdouble acc = -java_lang_Double_MAX_VALUE;

            for (jint offset = 0; offset < maxStride; offset += stride) {
                acc = std::max<jdouble>(acc, src[srcIndices[i] + offset]);
            }

            jint count = 0;

            for (jint offset = 0; offset < maxStride; offset += stride) {

                if (src[srcIndices[i] + offset] == acc) {

                    dst[srcIndices[i] + count] = offset / stride;
                    count += stride;
                }
            }

            for (jint offset = count; offset < maxStride; offset += stride) {
                dst[srcIndices[i] + offset] = -1;
            }
        }

    } else {

        jdouble maxValue = *std::max_element(src, src + nIndices);

        for (jint i = 0; i < nIndices; i++) {
            dst[i] = (src[i] == maxValue) ? 1 : 0;
        }
    }
}

inline void DimensionOps::riMin(jdouble *src, const jint *srcIndices, jint *dst, //
        jint nIndices, jint size, jint stride) {

    if (srcIndices) {

        jint maxStride = stride * size;

        for (jint i = 0; i < nIndices; i++) {

            jdouble acc = java_lang_Double_MAX_VALUE;

            for (jint offset = 0; offset < maxStride; offset += stride) {
                acc = std::min<jdouble>(acc, src[srcIndices[i] + offset]);
            }

            jint count = 0;

            for (jint offset = 0; offset < maxStride; offset += stride) {

                if (src[srcIndices[i] + offset] == acc) {

                    dst[srcIndices[i] + count] = offset / stride;
                    count += stride;
                }
            }

            for (jint offset = count; offset < maxStride; offset += stride) {
                dst[srcIndices[i] + offset] = -1;
            }
        }

    } else {

        jdouble minValue = *std::min_element(src, src + nIndices);

        for (jint i = 0; i < nIndices; i++) {
            dst[i] = (src[i] == minValue) ? 1 : 0;
        }
    }
}

inline void DimensionOps::riZero(jdouble *src, const jint *srcIndices, jint *dst, //
        jint nIndices, jint size, jint stride) {

    if (srcIndices) {

        jint maxStride = stride * size;

        for (jint i = 0; i < nIndices; i++) {

            jint count = 0;

            for (jint offset = 0; offset < maxStride; offset += stride) {

                if (src[srcIndices[i] + offset] == 0.0) {

                    dst[srcIndices[i] + count] = offset / stride;
                    count += stride;
                }
            }

            for (jint offset = count; offset < maxStride; offset += stride) {
                dst[srcIndices[i] + offset] = -1;
            }
        }

    } else {

        for (jint i = 0; i < nIndices; i++) {
            dst[i] = (src[i] == 0.0) ? 1 : 0;
        }
    }
}

inline void DimensionOps::riGZero(jdouble *src, const jint *srcIndices, jint *dst, //
        jint nIndices, jint size, jint stride) {

    if (srcIndices) {

        jint maxStride = stride * size;

        for (jint i = 0; i < nIndices; i++) {

            jint count = 0;

            for (jint offset = 0; offset < maxStride; offset += stride) {

                if (src[srcIndices[i] + offset] > 0.0) {

                    dst[srcIndices[i] + count] = offset / stride;
                    count += stride;
                }
            }

            for (jint offset = count; offset < maxStride; offset += stride) {
                dst[srcIndices[i] + offset] = -1;
            }
        }

    } else {

        for (jint i = 0; i < nIndices; i++) {
            dst[i] = (src[i] > 0.0) ? 1 : 0;
        }
    }
}

inline void DimensionOps::riLZero(jdouble *src, const jint *srcIndices, jint *dst, //
        jint nIndices, jint size, jint stride) {

    if (srcIndices) {

        jint maxStride = stride * size;

        for (jint i = 0; i < nIndices; i++) {

            jint count = 0;

            for (jint offset = 0; offset < maxStride; offset += stride) {

                if (src[srcIndices[i] + offset] < 0.0) {

                    dst[srcIndices[i] + count] = offset / stride;
                    count += stride;
                }
            }

            for (jint offset = count; offset < maxStride; offset += stride) {
                dst[srcIndices[i] + offset] = -1;
            }
        }

    } else {

        for (jint i = 0; i < nIndices; i++) {
            dst[i] = (src[i] < 0.0) ? 1 : 0;
        }
    }
}

inline void DimensionOps::riSort(jdouble *src, const jint *srcIndices, jint *dst, //
        jint nIndices, jint size, jint stride) {

    MallocHandler mallocH(sizeof(permutation_entry<jdouble, jint> ) * (srcIndices ? size : nIndices));
    permutation_entry<jdouble, jint> *entries = (permutation_entry<jdouble, jint> *) mallocH.get();

    if (srcIndices) {

        for (jint i = 0; i < nIndices; i++) {

            for (jint offset = 0, j = 0; j < size; offset += stride, j++) {

                entries[j].value = src[srcIndices[i] + offset];
                entries[j].payload = j;
            }

            std::sort(entries, entries + size);

            for (jint offset = 0, j = 0; j < size; offset += stride, j++) {

                src[srcIndices[i] + offset] = entries[j].value;
                dst[srcIndices[i] + offset] = entries[j].payload;
            }
        }

    } else {

        for (jint i = 0; i < nIndices; i++) {

            entries[i].value = src[i];
            entries[i].payload = i;
        }

        std::sort(entries, entries + nIndices);

        for (jint i = 0; i < nIndices; i++) {

            src[i] = entries[i].value;
            dst[i] = entries[i].payload;
        }
    }
}

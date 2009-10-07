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
        jint ndims = env->GetArrayLength(srcD);

        if (!(dim >= -1 && dim < ndims) //
                || (ndims != env->GetArrayLength(srcS)) //
                || (srcLen != dstLen)) {
            throw std::runtime_error("Invalid arguments");
        }

        // Initialize pinned arrays.

        // Remember that the source array is modifiable, too!
        ArrayPinHandler srcVH(env, srcV, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_WRITE);
        ArrayPinHandler srcDH(env, srcD, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        ArrayPinHandler srcSH(env, srcS, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        ArrayPinHandler dstVH(env, dstV, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_WRITE);
        // NO JNI AFTER THIS POINT!

        jdouble *srcVArr = (jdouble *) srcVH.get();
        jint *srcDArr = (jint *) srcDH.get();
        jint *srcSArr = (jint *) srcSH.get();
        jint *dstVArr = (jint *) dstVH.get();

        MappingOps::checkDimensions(srcDArr, srcSArr, ndims, srcLen);

        // Proceed only if non-zero length.
        if (!srcLen) {
            return;
        }

        if (dim != -1) {

            jint nindices = dstLen / srcDArr[dim];

            MallocHandler mallocH(sizeof(jint) * (nindices + 2 * (ndims - 1)));
            void *all = mallocH.get();

            jint *srcIndices = (jint *) all;
            jint *srcDArrModified = (jint *) all + nindices;
            jint *srcSArrModified = (jint *) all + nindices + (ndims - 1);

            // Assign indices while pretending that the dimension of interest doesn't exist.
            DimensionOps::assignBaseIndices(srcIndices, srcDArr, srcDArrModified, srcSArr, srcSArrModified, //
                    ndims, dim);

            // Execute the index operation.

            op(srcVArr, srcIndices, dstVArr, nindices, srcDArr[dim], srcSArr[dim]);

        } else {

            op(srcVArr, NULL, dstVArr, srcLen, -1, -1);
        }

    } catch (std::exception &e) {

        Common::throwNew(env, e);
    }
}

inline void DimensionOps::riMax(jdouble *src, const jint *srcIndices, jint *dst, //
        jint nindices, jint size, jint stride) {

    if (srcIndices) {

        jint maxStride = stride * size;

        for (jint i = 0; i < nindices; i++) {

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

        jdouble maxValue = *std::max_element(src, src + nindices);

        for (jint i = 0; i < nindices; i++) {
            dst[i] = (src[i] == maxValue) ? 1 : 0;
        }
    }
}

inline void DimensionOps::riMin(jdouble *src, const jint *srcIndices, jint *dst, //
        jint nindices, jint size, jint stride) {

    if (srcIndices) {

        jint maxStride = stride * size;

        for (jint i = 0; i < nindices; i++) {

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

        jdouble minValue = *std::min_element(src, src + nindices);

        for (jint i = 0; i < nindices; i++) {
            dst[i] = (src[i] == minValue) ? 1 : 0;
        }
    }
}

inline void DimensionOps::riZero(jdouble *src, const jint *srcIndices, jint *dst, //
        jint nindices, jint size, jint stride) {

    if (srcIndices) {

        jint maxStride = stride * size;

        for (jint i = 0; i < nindices; i++) {

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

        for (jint i = 0; i < nindices; i++) {
            dst[i] = (src[i] == 0.0) ? 1 : 0;
        }
    }
}

inline void DimensionOps::riGZero(jdouble *src, const jint *srcIndices, jint *dst, //
        jint nindices, jint size, jint stride) {

    if (srcIndices) {

        jint maxStride = stride * size;

        for (jint i = 0; i < nindices; i++) {

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

        for (jint i = 0; i < nindices; i++) {
            dst[i] = (src[i] > 0.0) ? 1 : 0;
        }
    }
}

inline void DimensionOps::riLZero(jdouble *src, const jint *srcIndices, jint *dst, //
        jint nindices, jint size, jint stride) {

    if (srcIndices) {

        jint maxStride = stride * size;

        for (jint i = 0; i < nindices; i++) {

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

        for (jint i = 0; i < nindices; i++) {
            dst[i] = (src[i] < 0.0) ? 1 : 0;
        }
    }
}

inline void DimensionOps::riSort(jdouble *src, const jint *srcIndices, jint *dst, //
        jint nindices, jint size, jint stride) {

    MallocHandler mallocH(sizeof(permutation_entry<jdouble, jint> ) * (srcIndices ? size : nindices));
    permutation_entry<jdouble, jint> *entries = (permutation_entry<jdouble, jint> *) mallocH.get();

    if (srcIndices) {

        for (jint i = 0; i < nindices; i++) {

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

        for (jint i = 0; i < nindices; i++) {

            entries[i].value = src[i];
            entries[i].payload = i;
        }

        std::sort(entries, entries + nindices);

        for (jint i = 0; i < nindices; i++) {

            src[i] = entries[i].value;
            dst[i] = entries[i].payload;
        }
    }
}

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

#include <MatrixOps.hpp>

void MatrixOps::mul(JNIEnv *env, jobject thisObj, jdoubleArray lhsV, jdoubleArray rhsV, jint lhsR, jint rhsC,
        jdoubleArray dstV, jboolean isComplex) {

    try {

        if (isComplex) {

            MatrixOps::mulProxy<jcomplex>(env, lhsV, rhsV, lhsR, rhsC, dstV, jcomplex(0, 0), JNI_TRUE);

        } else {

            MatrixOps::mulProxy<jdouble>(env, lhsV, rhsV, lhsR, rhsC, dstV, 0, JNI_FALSE);
        }

    } catch (std::exception &e) {

        Common::throwNew(env, e);
    }
}

template<class T> inline void MatrixOps::mulProxy(JNIEnv *env, //
        jarray lhsV, jarray rhsV, jint lhsR, jint rhsC, jarray dstV, //
        T zero, jboolean isComplex) {

    if (!lhsV || !rhsV || !dstV) {
        throw std::runtime_error("Invalid arguments");
    }

    jint lhsLen = env->GetArrayLength(lhsV);
    jint rhsLen = env->GetArrayLength(rhsV);
    jint dstLen = env->GetArrayLength(dstV);

    jint factor = (isComplex ? 2 : 1);
    jint lhsC = lhsR ? lhsLen / (factor * lhsR) : 0;
    jint rhsR = rhsC ? rhsLen / (factor * rhsC) : 0;
    jint inner = lhsC;

    if (lhsR < 0 || rhsC < 0 //
            || (lhsLen != factor * lhsR * lhsC) //
            || (rhsLen != factor * rhsR * rhsC) //
            || (dstLen != factor * lhsR * rhsC) //
            || (inner != rhsR)) {
        throw std::runtime_error("Invalid array lengths");
    }

    ArrayPinHandler lhsVH(env, lhsV, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
    ArrayPinHandler rhsVH(env, rhsV, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
    ArrayPinHandler dstVH(env, dstV, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_WRITE);
    // NO JNI AFTER THIS POINT!

    T *lhsVArr = (T *) lhsVH.get();
    T *rhsVArr = (T *) rhsVH.get();
    T *dstVArr = (T *) dstVH.get();

    MatrixOps::mul<T>(lhsVArr, rhsVArr, inner, dstVArr, lhsR, rhsC, zero);
}

template<class T> inline void MatrixOps::mul(const T *lArr, const T *rArr, jint inner, //
        T *outArr, jint lr, jint rc, T zero) {

    for (jint i = 0; i < lr; i++) {

        for (jint j = 0; j < rc; j++) {

            T sum = zero;

            for (jint k = 0; k < inner; k++) {
                sum = sum + lArr[i * inner + k] * rArr[k * rc + j];
            }

            outArr[i * rc + j] = sum;
        }
    }
}

void MatrixOps::diag(JNIEnv *env, jobject thisObj, jdoubleArray srcV, jdoubleArray dstV, jint size, jboolean isComplex) {

    try {

        if (isComplex) {

            MatrixOps::diagProxy<jcomplex>(env, srcV, dstV, size, JNI_TRUE);

        } else {

            MatrixOps::diagProxy<jdouble>(env, srcV, dstV, size, JNI_FALSE);
        }

    } catch (std::exception &e) {

        Common::throwNew(env, e);
    }
}

template<class T> inline void MatrixOps::diagProxy(JNIEnv *env, //
        jarray srcV, jarray dstV, jint size, jboolean isComplex) {

    if (!srcV || !dstV) {
        throw std::runtime_error("Invalid arguments");
    }

    jint srcLen = env->GetArrayLength(srcV);
    jint dstLen = env->GetArrayLength(dstV);

    jint factor = (isComplex ? 2 : 1);

    if ((srcLen != factor * size * size) || (dstLen != factor * size)) {
        throw std::runtime_error("Invalid array lengths");
    }

    ArrayPinHandler srcVH(env, srcV, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
    ArrayPinHandler dstVH(env, dstV, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_WRITE);
    // NO JNI AFTER THIS POINT!

    T *srcVArr = (T *) srcVH.get();
    T *dstVArr = (T *) dstVH.get();

    MatrixOps::diag<T>(srcVArr, dstVArr, size);
}

template<class T> inline void MatrixOps::diag(const T *srcArr, T *dstArr, jint size) {

    for (jint i = 0; i < size; i++) {
        dstArr[i] = srcArr[i * size + i];
    }
}

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

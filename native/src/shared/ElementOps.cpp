/*
 * Copyright (C) 2007 Roy Liu
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

#include <ElementOps.hpp>

jdouble ElementOps::raOp(JNIEnv *env, jobject thisObj, jint type, jdoubleArray srcV) {

    jdouble res = 0.0;

    try {

        raOp_t *op = NULL;

        switch (type) {

        case shared_array_kernel_ArrayKernel_RA_SUM:
            op = ElementOps::raSum;
            break;

        case shared_array_kernel_ArrayKernel_RA_PROD:
            op = ElementOps::raProd;
            break;

        case shared_array_kernel_ArrayKernel_RA_VAR:
            op = ElementOps::raVar;
            break;

        case shared_array_kernel_ArrayKernel_RA_MAX:
            op = ElementOps::raMax;
            break;

        case shared_array_kernel_ArrayKernel_RA_MIN:
            op = ElementOps::raMin;
            break;

        case shared_array_kernel_ArrayKernel_RA_ENT:
            op = ElementOps::raEnt;
            break;

        default:
            throw std::runtime_error("Operation type not recognized");
        }

        res = ElementOps::accumulatorOpProxy<jdouble>(env, op, srcV, JNI_FALSE);

    } catch (std::exception &e) {

        Common::throwNew(env, e);
    }

    return res;
}

jdoubleArray ElementOps::caOp(JNIEnv *env, jobject thisObj, jint type, jdoubleArray srcV) {

    jdoubleArray res = NULL;

    try {

        caOp_t *op = NULL;

        switch (type) {

        case shared_array_kernel_ArrayKernel_CA_SUM:
            op = ElementOps::caSum;
            break;

        case shared_array_kernel_ArrayKernel_CA_PROD:
            op = ElementOps::caProd;
            break;

        default:
            throw std::runtime_error("Operation type not recognized");
        }

        jcomplex a = ElementOps::accumulatorOpProxy<jcomplex>(env, op, srcV, JNI_TRUE);

        res = Common::newDoubleArray(env, 2);

        ArrayPinHandler resH(env, res, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_WRITE);
        // NO JNI AFTER THIS POINT!

        *((jcomplex *) resH.get()) = a;

    } catch (std::exception &e) {

        Common::throwNew(env, e);
    }

    return res;
}

void ElementOps::ruOp(JNIEnv *env, jobject thisObj, jint type, jdouble a, jdoubleArray srcV) {

    try {

        ruOp_t *op = NULL;

        switch (type) {

        case shared_array_kernel_ArrayKernel_RU_ADD:
            op = ElementOps::uAdd<jdouble>;
            break;

        case shared_array_kernel_ArrayKernel_RU_MUL:
            op = ElementOps::uMul<jdouble>;
            break;

        case shared_array_kernel_ArrayKernel_RU_SQR:
            op = ElementOps::uSqr<jdouble>;
            break;

        case shared_array_kernel_ArrayKernel_RU_INV:
            op = ElementOps::uInv<jdouble>;
            break;

        case shared_array_kernel_ArrayKernel_RU_FILL:
            op = ElementOps::uFill<jdouble>;
            break;

        case shared_array_kernel_ArrayKernel_RU_SHUFFLE:
            op = ElementOps::uShuffle<jdouble>;
            break;

        case shared_array_kernel_ArrayKernel_RU_POW:
            op = ElementOps::ruPow;
            break;

        case shared_array_kernel_ArrayKernel_RU_EXP:
            op = ElementOps::ruExp;
            break;

        case shared_array_kernel_ArrayKernel_RU_ABS:
            op = ElementOps::ruAbs;
            break;

        case shared_array_kernel_ArrayKernel_RU_RND:
            op = ElementOps::ruRnd;
            break;

        case shared_array_kernel_ArrayKernel_RU_LOG:
            op = ElementOps::ruLog;
            break;

        case shared_array_kernel_ArrayKernel_RU_SQRT:
            op = ElementOps::ruSqrt;
            break;

        case shared_array_kernel_ArrayKernel_RU_COS:
            op = ElementOps::ruCos;
            break;

        case shared_array_kernel_ArrayKernel_RU_SIN:
            op = ElementOps::ruSin;
            break;

        case shared_array_kernel_ArrayKernel_RU_ATAN:
            op = ElementOps::ruAtan;
            break;

        default:
            throw std::runtime_error("Operation type not recognized");
        }

        ElementOps::unaryOpProxy<jdouble>(env, op, srcV, a, JNI_FALSE);

    } catch (std::exception &e) {

        Common::throwNew(env, e);
    }
}

void ElementOps::cuOp(JNIEnv *env, jobject thisObj, jint type, jdouble aRe, jdouble aIm, jdoubleArray srcV) {

    try {

        cuOp_t *op = NULL;

        switch (type) {

        case shared_array_kernel_ArrayKernel_CU_ADD:
            op = ElementOps::uAdd<jcomplex>;
            break;

        case shared_array_kernel_ArrayKernel_CU_MUL:
            op = ElementOps::uMul<jcomplex>;
            break;

        case shared_array_kernel_ArrayKernel_CU_FILL:
            op = ElementOps::uFill<jcomplex>;
            break;

        case shared_array_kernel_ArrayKernel_CU_SHUFFLE:
            op = ElementOps::uShuffle<jcomplex>;
            break;

        case shared_array_kernel_ArrayKernel_CU_EXP:
            op = ElementOps::cuExp;
            break;

        case shared_array_kernel_ArrayKernel_CU_RND:
            op = ElementOps::cuRnd;
            break;

        case shared_array_kernel_ArrayKernel_CU_CONJ:
            op = ElementOps::cuConj;
            break;

        case shared_array_kernel_ArrayKernel_CU_COS:
            op = ElementOps::cuCos;
            break;

        case shared_array_kernel_ArrayKernel_CU_SIN:
            op = ElementOps::cuSin;
            break;

        default:
            throw std::runtime_error("Operation type not recognized");
        }

        ElementOps::unaryOpProxy<jcomplex>(env, op, srcV, jcomplex(aRe, aIm), JNI_TRUE);

    } catch (std::exception &e) {

        Common::throwNew(env, e);
    }
}

void ElementOps::iuOp(JNIEnv *env, jobject thisObj, jint type, jint a, jintArray srcV) {

    try {

        iuOp_t *op = NULL;

        switch (type) {

        case shared_array_kernel_ArrayKernel_IU_ADD:
            op = ElementOps::uAdd<jint>;
            break;

        case shared_array_kernel_ArrayKernel_IU_MUL:
            op = ElementOps::uMul<jint>;
            break;

        case shared_array_kernel_ArrayKernel_IU_FILL:
            op = ElementOps::uFill<jint>;
            break;

        case shared_array_kernel_ArrayKernel_IU_SHUFFLE:
            op = ElementOps::uShuffle<jint>;
            break;

        default:
            throw std::runtime_error("Operation type not recognized");
        }

        ElementOps::unaryOpProxy<jint>(env, op, srcV, a, JNI_FALSE);

    } catch (std::exception &e) {

        Common::throwNew(env, e);
    }
}

void ElementOps::eOp(JNIEnv *env, jobject thisObj, jint type, jobject lhsV, jobject rhsV, jobject dstV,
        jboolean isComplex) {

    try {

        if (NativeArrayKernel::isJdoubleArray(env, lhsV) //
                && NativeArrayKernel::isJdoubleArray(env, rhsV) //
                && NativeArrayKernel::isJdoubleArray(env, dstV)) {

            if (isComplex) {

                ElementOps::binaryOpProxy<jcomplex>(env, type, (jarray) lhsV, (jarray) rhsV, (jarray) dstV, //
                        ElementOps::COMPLEX);

            } else {

                ElementOps::binaryOpProxy<jdouble>(env, type, (jarray) lhsV, (jarray) rhsV, (jarray) dstV, //
                        ElementOps::REAL);
            }

        } else if (NativeArrayKernel::isJintArray(env, lhsV) //
                && NativeArrayKernel::isJintArray(env, rhsV) //
                && NativeArrayKernel::isJintArray(env, dstV)) {

            ElementOps::binaryOpProxy<jint>(env, type, (jarray) lhsV, (jarray) rhsV, (jarray) dstV, //
                    ElementOps::INTEGER);

        } else {

            throw std::runtime_error("Invalid array types");
        }

    } catch (std::exception &e) {

        Common::throwNew(env, e);
    }
}

void ElementOps::convert(JNIEnv *env, jobject thisObj, jint type, //
        jobject srcV, jboolean isSrcComplex, //
        jobject dstV, jboolean isDstComplex) {

    try {

        if (NativeArrayKernel::isJdoubleArray(env, srcV) && !isSrcComplex //
                && NativeArrayKernel::isJdoubleArray(env, dstV) && isDstComplex) {

            rtocOp_t *op = NULL;

            switch (type) {

            case shared_array_kernel_ArrayKernel_RTOC_RE:
                op = ElementOps::rtocRe;
                break;

            case shared_array_kernel_ArrayKernel_RTOC_IM:
                op = ElementOps::rtocIm;
                break;

            default:
                throw std::runtime_error("Operation type not recognized");
            }

            ElementOps::convertProxy<jdouble, jcomplex>(env, op, //
                    (jarray) srcV, isSrcComplex, //
                    (jarray) dstV, isDstComplex);

        } else if (NativeArrayKernel::isJdoubleArray(env, srcV) && isSrcComplex //
                && NativeArrayKernel::isJdoubleArray(env, dstV) && !isDstComplex) {

            ctorOp_t *op = NULL;

            switch (type) {

            case shared_array_kernel_ArrayKernel_CTOR_RE:
                op = ElementOps::ctorRe;
                break;

            case shared_array_kernel_ArrayKernel_CTOR_IM:
                op = ElementOps::ctorIm;
                break;

            case shared_array_kernel_ArrayKernel_CTOR_ABS:
                op = ElementOps::ctorAbs;
                break;

            default:
                throw std::runtime_error("Operation type not recognized");
            }

            ElementOps::convertProxy<jcomplex, jdouble>(env, op, //
                    (jarray) srcV, isSrcComplex, //
                    (jarray) dstV, isDstComplex);

        } else if (NativeArrayKernel::isJintArray(env, srcV) && !isSrcComplex //
                && NativeArrayKernel::isJdoubleArray(env, dstV) && !isDstComplex) {

            itorOp_t *op = NULL;

            switch (type) {

            case shared_array_kernel_ArrayKernel_ITOR:
                op = ElementOps::itor;
                break;

            default:
                throw std::runtime_error("Operation type not recognized");
            }

            ElementOps::convertProxy<jint, jdouble>(env, op, //
                    (jarray) srcV, isSrcComplex, //
                    (jarray) dstV, isDstComplex);

        } else {

            throw std::runtime_error("Invalid arguments");
        }

    } catch (std::exception &e) {

        Common::throwNew(env, e);
    }
}

template<class T> inline T ElementOps::accumulatorOpProxy(JNIEnv *env, T(*op)(const T *, jint), //
        jarray srcV, jboolean isComplex) {

    if (!srcV) {
        throw std::runtime_error("Invalid arguments");
    }

    jint srcLen = env->GetArrayLength(srcV);

    if (isComplex && (srcLen % 2) != 0) {
        throw std::runtime_error("Invalid array length");
    }

    jint logicalLen = srcLen / (isComplex ? 2 : 1);

    ArrayPinHandler srcVH(env, srcV, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
    // NO JNI AFTER THIS POINT!

    return op((T *) srcVH.get(), logicalLen);
}

template<class T> inline void ElementOps::unaryOpProxy(JNIEnv *env, void(*op)(T, T *, jint), //
        jarray srcV, T argument, jboolean isComplex) {

    if (!srcV) {
        throw std::runtime_error("Invalid arguments");
    }

    jint srcLen = env->GetArrayLength(srcV);

    if (isComplex && (srcLen % 2) != 0) {
        throw std::runtime_error("Invalid array length");
    }

    jint logicalLen = srcLen / (isComplex ? 2 : 1);

    ArrayPinHandler srcVH(env, srcV, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_WRITE);
    // NO JNI AFTER THIS POINT!

    op(argument, (T *) srcVH.get(), logicalLen);
}

template<class T> inline void ElementOps::binaryOpProxy(JNIEnv *env, jint type, jarray lhsV, jarray rhsV, jarray dstV,
        ElementOps::element_type elementType) {

    try {

        void (*op)(const T *, const T *, T *, jint) = NULL;

        jboolean isComplex;

        switch (elementType) {

        case COMPLEX:

            switch (type) {

            case shared_array_kernel_ArrayKernel_CE_ADD:
                op = ElementOps::eAdd<T>;
                break;

            case shared_array_kernel_ArrayKernel_CE_SUB:
                op = ElementOps::eSub<T>;
                break;

            case shared_array_kernel_ArrayKernel_CE_MUL:
                op = ElementOps::eMul<T>;
                break;

            case shared_array_kernel_ArrayKernel_CE_DIV:
                op = ElementOps::eDiv<T>;
                break;

            default:
                throw std::runtime_error("Operation type not recognized");
            }

            isComplex = JNI_TRUE;

            break;

        case REAL:

            switch (type) {

            case shared_array_kernel_ArrayKernel_RE_ADD:
                op = ElementOps::eAdd<T>;
                break;

            case shared_array_kernel_ArrayKernel_RE_SUB:
                op = ElementOps::eSub<T>;
                break;

            case shared_array_kernel_ArrayKernel_RE_MUL:
                op = ElementOps::eMul<T>;
                break;

            case shared_array_kernel_ArrayKernel_RE_DIV:
                op = ElementOps::eDiv<T>;
                break;

            case shared_array_kernel_ArrayKernel_RE_MAX:
                op = ElementOps::eMax<T>;
                break;

            case shared_array_kernel_ArrayKernel_RE_MIN:
                op = ElementOps::eMin<T>;
                break;

            default:
                throw std::runtime_error("Operation type not recognized");
            }

            isComplex = JNI_FALSE;

            break;

        case INTEGER:

            switch (type) {

            case shared_array_kernel_ArrayKernel_IE_ADD:
                op = ElementOps::eAdd<T>;
                break;

            case shared_array_kernel_ArrayKernel_IE_SUB:
                op = ElementOps::eSub<T>;
                break;

            case shared_array_kernel_ArrayKernel_IE_MUL:
                op = ElementOps::eMul<T>;
                break;

            case shared_array_kernel_ArrayKernel_IE_MAX:
                op = ElementOps::eMax<T>;
                break;

            case shared_array_kernel_ArrayKernel_IE_MIN:
                op = ElementOps::eMin<T>;
                break;

            default:
                throw std::runtime_error("Operation type not recognized");
            }

            isComplex = JNI_FALSE;

            break;

        default:
            throw std::runtime_error("Array type not recognized");
        }

        if (!lhsV || !rhsV || !dstV) {
            throw std::runtime_error("Invalid arguments");
        }

        jint lhsLen = env->GetArrayLength(lhsV);
        jint rhsLen = env->GetArrayLength(rhsV);
        jint dstLen = env->GetArrayLength(dstV);

        if (dstLen != lhsLen || dstLen != rhsLen || (isComplex && (dstLen % 2) != 0)) {
            throw std::runtime_error("Invalid array lengths");
        }

        jint logicalLen = dstLen / (isComplex ? 2 : 1);

        ArrayPinHandler lhsVH(env, lhsV, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        ArrayPinHandler rhsVH(env, rhsV, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        ArrayPinHandler dstVH(env, dstV, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_WRITE);
        // NO JNI AFTER THIS POINT!

        op((T *) lhsVH.get(), (T *) rhsVH.get(), (T *) dstVH.get(), logicalLen);

    } catch (std::exception &e) {

        Common::throwNew(env, e);
    }
}

template<class S, class T> inline void ElementOps::convertProxy(JNIEnv *env, void(*op)(const S *, T *, jint), //
        jarray srcV, jboolean srcIsComplex, //
        jarray dstV, jboolean dstIsComplex) {

    if (!srcV || !dstV) {
        throw std::runtime_error("Invalid arguments");
    }

    jint srcLen = env->GetArrayLength(srcV);
    jint dstLen = env->GetArrayLength(dstV);
    jint logicalLen = srcLen / (srcIsComplex ? 2 : 1);

    if ((srcIsComplex && (srcLen % 2) != 0) //
            || (dstIsComplex && (dstLen % 2) != 0) //
            || (logicalLen != dstLen / (dstIsComplex ? 2 : 1))) {
        throw std::runtime_error("Invalid array lengths");
    }

    ArrayPinHandler srcVH(env, srcV, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
    ArrayPinHandler dstVH(env, dstV, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_WRITE);
    // NO JNI AFTER THIS POINT!

    op((S *) srcVH.get(), (T *) dstVH.get(), logicalLen);
}

template<class T> inline void ElementOps::eAdd(const T *a, const T *b, T *r, jint len) {

    for (jint i = 0; i < len; i++) {
        r[i] = a[i] + b[i];
    }
}

template<class T> inline void ElementOps::eSub(const T *a, const T *b, T *r, jint len) {

    for (jint i = 0; i < len; i++) {
        r[i] = a[i] - b[i];
    }
}

template<class T> inline void ElementOps::eMul(const T *a, const T *b, T *r, jint len) {

    for (jint i = 0; i < len; i++) {
        r[i] = a[i] * b[i];
    }
}

template<class T> inline void ElementOps::eDiv(const T *a, const T *b, T *r, jint len) {

    for (jint i = 0; i < len; i++) {
        r[i] = a[i] / b[i];
    }
}

template<class T> inline void ElementOps::eMax(const T *a, const T *b, T *r, jint len) {

    for (jint i = 0; i < len; i++) {
        r[i] = std::max<T>(a[i], b[i]);
    }
}

template<class T> inline void ElementOps::eMin(const T *a, const T *b, T *r, jint len) {

    for (jint i = 0; i < len; i++) {
        r[i] = std::min<T>(a[i], b[i]);
    }
}

//

template<class T> inline void ElementOps::uSqr(T a, T *v, jint len) {

    for (jint i = 0; i < len; i++) {
        v[i] *= v[i];
    }
}

template<class T> inline void ElementOps::uInv(T a, T *v, jint len) {

    for (jint i = 0; i < len; i++) {
        v[i] = a / v[i];
    }
}

template<class T> inline void ElementOps::uAdd(T a, T *v, jint len) {

    for (jint i = 0; i < len; i++) {
        v[i] += a;
    }
}

template<class T> inline void ElementOps::uMul(T a, T *v, jint len) {

    for (jint i = 0; i < len; i++) {
        v[i] *= a;
    }
}

template<class T> inline void ElementOps::uFill(T a, T *v, jint len) {

    for (jint i = 0; i < len; i++) {
        v[i] = a;
    }
}

template<class T> inline void ElementOps::uShuffle(T a, T *v, jint len) {

    for (jint i = len; i > 1; i--) {

        jint index = rand() % i;

        T tmp = v[i - 1];
        v[i - 1] = v[index];
        v[index] = tmp;
    }
}

//

inline void ElementOps::ruAbs(jdouble a, jdouble *v, jint len) {

    for (jint i = 0; i < len; i++) {
        v[i] = fabs(v[i]);
    }
}

inline void ElementOps::ruExp(jdouble a, jdouble *v, jint len) {

    for (jint i = 0; i < len; i++) {
        v[i] = exp(v[i]);
    }
}

inline void ElementOps::ruRnd(jdouble a, jdouble *v, jint len) {

    for (jint i = 0; i < len; i++) {
        v[i] = a * (rand() / (jdouble) RAND_MAX);
    }
}

inline void ElementOps::ruLog(jdouble a, jdouble *v, jint len) {

    for (jint i = 0; i < len; i++) {
        v[i] = log(v[i]);
    }
}

inline void ElementOps::ruPow(jdouble a, jdouble *v, jint len) {

    for (jint i = 0; i < len; i++) {
        v[i] = pow(v[i], a);
    }
}

inline void ElementOps::ruSqrt(jdouble a, jdouble *v, jint len) {

    for (jint i = 0; i < len; i++) {
        v[i] = sqrt(v[i]);
    }
}

inline void ElementOps::ruCos(jdouble a, jdouble *v, jint len) {

    for (jint i = 0; i < len; i++) {
        v[i] = cos(v[i]);
    }
}

inline void ElementOps::ruSin(jdouble a, jdouble *v, jint len) {

    for (jint i = 0; i < len; i++) {
        v[i] = sin(v[i]);
    }
}

inline void ElementOps::ruAtan(jdouble a, jdouble *v, jint len) {

    for (jint i = 0; i < len; i++) {
        v[i] = atan(v[i]);
    }
}

//

inline void ElementOps::cuExp(jcomplex a, jcomplex *v, jint len) {

    for (jint i = 0; i < len; i++) {
        v[i] = jcomplex(cos(v[i].im) * exp(v[i].re), sin(v[i].im) * exp(v[i].re));
    }
}

inline void ElementOps::cuRnd(jcomplex a, jcomplex *v, jint len) {

    for (jint i = 0; i < len; i++) {
        v[i] = jcomplex(a.re * (rand() / (jdouble) RAND_MAX), a.im * (rand() / (jdouble) RAND_MAX));
    }
}

inline void ElementOps::cuConj(jcomplex a, jcomplex *v, jint len) {

    for (jint i = 0; i < len; i++) {
        v[i] = jcomplex(v[i].re, -v[i].im);
    }
}

inline void ElementOps::cuCos(jcomplex a, jcomplex *v, jint len) {

    jcomplex factor_i = jcomplex(0.0, 1.0);
    jcomplex factor_minus_i = jcomplex(0.0, -1.0);
    jcomplex denominator = jcomplex(2.0, 0.0);

    for (jint i = 0; i < len; i++) {

        jcomplex exp1 = factor_i * v[i];
        jcomplex exp2 = factor_minus_i * v[i];

        v[i] = jcomplex(cos(exp1.im) * exp(exp1.re) + cos(exp2.im) * exp(exp2.re), //
                sin(exp1.im) * exp(exp1.re) + sin(exp2.im) * exp(exp2.re)) / denominator;
    }
}

inline void ElementOps::cuSin(jcomplex a, jcomplex *v, jint len) {

    jcomplex factor_i = jcomplex(0.0, 1.0);
    jcomplex factor_minus_i = jcomplex(0.0, -1.0);
    jcomplex denominator = jcomplex(0.0, 2.0);

    for (jint i = 0; i < len; i++) {

        jcomplex exp1 = factor_i * v[i];
        jcomplex exp2 = factor_minus_i * v[i];

        v[i] = jcomplex(cos(exp1.im) * exp(exp1.re) - cos(exp2.im) * exp(exp2.re), //
                sin(exp1.im) * exp(exp1.re) - sin(exp2.im) * exp(exp2.re)) / denominator;
    }
}

//

inline void ElementOps::ctorAbs(const jcomplex *src, jdouble *dst, jint len) {

    for (jint i = 0; i < len; i++) {
        dst[i] = sqrt(src[i].re * src[i].re + src[i].im * src[i].im);
    }
}

inline void ElementOps::ctorRe(const jcomplex *src, jdouble *dst, jint len) {

    for (jint i = 0; i < len; i++) {
        dst[i] = src[i].re;
    }
}

inline void ElementOps::ctorIm(const jcomplex *src, jdouble *dst, jint len) {

    for (jint i = 0; i < len; i++) {
        dst[i] = src[i].im;
    }
}

//

inline void ElementOps::rtocRe(const jdouble *src, jcomplex *dst, jint len) {

    for (jint i = 0; i < len; i++) {
        dst[i] = jcomplex(src[i], 0.0);
    }
}

inline void ElementOps::rtocIm(const jdouble *src, jcomplex *dst, jint len) {

    for (jint i = 0; i < len; i++) {
        dst[i] = jcomplex(0.0, src[i]);
    }
}

//

inline void ElementOps::itor(const jint *src, jdouble *dst, jint len) {

    for (jint i = 0; i < len; i++) {
        dst[i] = src[i];
    }
}

//

inline jdouble ElementOps::raSum(const jdouble *a, jint len) {

    jdouble acc = 0.0;

    for (jint i = 0; i < len; i++) {
        acc += a[i];
    }

    return acc;
}

inline jdouble ElementOps::raProd(const jdouble *a, jint len) {

    jdouble acc = 1.0;

    for (jint i = 0; i < len; i++) {
        acc *= a[i];
    }

    return acc;
}

inline jdouble ElementOps::raMax(const jdouble *a, jint len) {

    jdouble acc = -java_lang_Double_MAX_VALUE;

    for (jint i = 0; i < len; i++) {
        acc = std::max<jdouble>(acc, a[i]);
    }

    return acc;
}

inline jdouble ElementOps::raMin(const jdouble *a, jint len) {

    jdouble acc = java_lang_Double_MAX_VALUE;

    for (jint i = 0; i < len; i++) {
        acc = std::min<jdouble>(acc, a[i]);
    }

    return acc;
}

inline jdouble ElementOps::raVar(const jdouble *a, jint len) {

    jdouble mean = raSum(a, len) / len;
    jdouble var = 0.0;

    for (jint i = 0; i < len; i++) {
        var += ((a[i] - mean) * (a[i] - mean)) / len;
    }

    return var;
}

inline jdouble ElementOps::raEnt(const jdouble *a, jint len) {

    jdouble sum = std::max<jdouble>(0.0, raSum(a, len)) + 1e-64;
    jdouble en = 0.0;

    for (jint i = 0; i < len; i++) {

        jdouble val = a[i] / sum;
        en += (val >= 1e-64) ? (val * log(val)) : 0.0;
    }

    return -en;
}

//

inline jcomplex ElementOps::caSum(const jcomplex *a, jint len) {

    jcomplex acc = jcomplex(0.0, 0.0);

    for (jint i = 0; i < len; i++) {
        acc += a[i];
    }

    return acc;
}

inline jcomplex ElementOps::caProd(const jcomplex *a, jint len) {

    jcomplex acc = jcomplex(1.0, 0.0);

    for (jint i = 0; i < len; i++) {
        acc *= a[i];
    }

    return acc;
}

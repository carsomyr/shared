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

#include <Common.hpp>
#include <NativeArrayKernel.hpp>

#include <JNIHeadersWrap.hpp>

#ifndef _Included_ElementOps
#define _Included_ElementOps

/**
 * A class for element-based operations.
 */
class ElementOps {

public:

    /**
     * An enumeration of element types.
     */
    enum element_type {

        /**
         * Indicates real values.
         */
        REAL, //

        /**
         * Indicates complex values.
         */
        COMPLEX, //

        /**
         * Indicates integral values.
         */
        INTEGER
    };

    /**
     * Performs a real accumulator operation.
     * 
     * @param env
     *      the JNI environment.
     * @param thisObj
     *      this object.
     * @param type
     *      the operation type.
     * @param srcV
     *      the array.
     * @return the accumulated result.
     */
    static jdouble raOp(JNIEnv *env, jobject thisObj, jint type, jdoubleArray srcV);

    /**
     * Performs a complex accumulator operation.
     * 
     * @param env
     *      the JNI environment.
     * @param thisObj
     *      this object.
     * @param type
     *      the operation type.
     * @param srcV
     *      the array.
     * @return the accumulated result.
     */
    static jdoubleArray caOp(JNIEnv *env, jobject thisObj, jint type, jdoubleArray srcV);

    /**
     * Applies a real unary operation.
     * 
     * @param env
     *      the JNI environment.
     * @param thisObj
     *      this object.
     * @param type
     *      the operation type.
     * @param a
     *      the argument, if any.
     * @param srcV
     *      the array.
     */
    static void ruOp(JNIEnv *env, jobject thisObj, jint type, jdouble a, jdoubleArray srcV);

    /**
     * Applies a complex unary operation.
     * 
     * @param env
     *      the JNI environment.
     * @param thisObj
     *      this object.
     * @param type
     *      the operation type.
     * @param aRe
     *      the real part of the argument, if any.
     * @param aIm
     *      the imaginary part of the argument, if any.
     * @param srcV
     *      the array.
     */
    static void cuOp(JNIEnv *env, jobject thisObj, jint type, jdouble aRe, jdouble aIm, jdoubleArray srcV);

    /**
     * Applies an integer unary operation.
     * 
     * @param env
     *      the JNI environment.
     * @param thisObj
     *      this object.
     * @param type
     *      the operation type.
     * @param a
     *      the argument, if any.
     * @param srcV
     *      the array.
     */
    static void iuOp(JNIEnv *env, jobject thisObj, jint type, jint a, jintArray srcV);

    /**
     * Applies a binary operation.
     * 
     * @param env
     *      the JNI environment.
     * @param thisObj
     *      this object.
     * @param type
     *      the operation type.
     * @param lhsV
     *      the left hand side values.
     * @param rhsV
     *      the right hand side values.
     * @param dstV
     *      the destination values.
     * @param isComplex
     *      whether the operation is complex-valued.
     */
    static void eOp(JNIEnv *env, jobject thisObj, jint type, jobject lhsV, jobject rhsV, jobject dstV,
            jboolean isComplex);

    /**
     * Performs a conversion operation.
     * 
     * @param env
     *      the JNI environment.
     * @param thisObj
     *      this object.
     * @param type
     *      the operation type.
     * @param srcV
     *      the source values.
     * @param isSrcComplex
     *      whether the source is complex-valued.
     * @param dstV
     *      the destination values.
     * @param isDstComplex
     *      whether the destination is complex-valued.
     */
    static void convert(JNIEnv *env, jobject thisObj, jint type, //
            jobject srcV, jboolean isSrcComplex, //
            jobject dstV, jboolean isDstComplex);

    /**
     * Defines a real accumulator operation.
     */
    typedef jdouble raOp_t(const jdouble *, jint);

    /**
     * Real accumulator sum.
     */
    inline static raOp_t raSum;

    /**
     * Real accumulator product.
     */
    inline static raOp_t raProd;

    /**
     * Real accumulator maximum.
     */
    inline static raOp_t raMax;

    /**
     * Real accumulator minimum.
     */
    inline static raOp_t raMin;

    /**
     * Real accumulator variance.
     */
    inline static raOp_t raVar;

    /**
     * Real accumulator entropy.
     */
    inline static raOp_t raEnt;

    /**
     * Defines a complex accumulator operation.
     */
    typedef jcomplex caOp_t(const jcomplex *, jint);

    /**
     * Complex accumulator sum.
     */
    inline static caOp_t caSum;

    /**
     * Complex accumulator product.
     */
    inline static caOp_t caProd;

    /**
     * Templatized unary addition.
     */
    template<class T> inline static void uAdd(T, T *, jint);

    /**
     * Templatized unary multiplication.
     */
    template<class T> inline static void uMul(T, T *, jint);

    /**
     * Templatized unary square.
     */
    template<class T> inline static void uSqr(T, T *, jint);

    /**
     * Templatized unary inverse.
     */
    template<class T> inline static void uInv(T, T *, jint);

    /**
     * Templatized unary fill.
     */
    template<class T> inline static void uFill(T, T *, jint);

    /**
     * Templatized unary shuffle.
     */
    template<class T> inline static void uShuffle(T, T *, jint);

    /**
     * Defines a real unary operation.
     */
    typedef void ruOp_t(jdouble, jdouble *, jint);

    /**
     * Real unary absolute value.
     */
    inline static ruOp_t ruAbs;

    /**
     * Real unary power.
     */
    inline static ruOp_t ruPow;

    /**
     * Real unary exponentiation.
     */
    inline static ruOp_t ruExp;

    /**
     * Real unary randomization.
     */
    inline static ruOp_t ruRnd;

    /**
     * Real unary natural logarithm.
     */
    inline static ruOp_t ruLog;

    /**
     * Real unary square root.
     */
    inline static ruOp_t ruSqrt;

    /**
     * Real unary cosine.
     */
    inline static ruOp_t ruCos;

    /**
     * Real unary sine.
     */
    inline static ruOp_t ruSin;

    /**
     * Real unary arctangent.
     */
    inline static ruOp_t ruAtan;

    /**
     * Defines a complex unary operation.
     */
    typedef void cuOp_t(jcomplex, jcomplex *, jint);

    /**
     * Complex unary exponentiation.
     */
    inline static cuOp_t cuExp;

    /**
     * Complex unary randomization.
     */
    inline static cuOp_t cuRnd;

    /**
     * Complex unary conjugation.
     */
    inline static cuOp_t cuConj;

    /**
     * Complex unary cosine.
     */
    inline static cuOp_t cuCos;

    /**
     * Complex unary sine.
     */
    inline static cuOp_t cuSin;

    /**
     * Defines an integer unary operation.
     */
    typedef void iuOp_t(jint, jint *, jint);

    /**
     * Templatized binary addition.
     */
    template<class T> inline static void eAdd(const T *, const T *, T *, jint);

    /**
     * Templatized binary subtraction.
     */
    template<class T> inline static void eSub(const T *, const T *, T *, jint);

    /**
     * Templatized binary multiplication.
     */
    template<class T> inline static void eMul(const T *, const T *, T *, jint);

    /**
     * Templatized binary division.
     */
    template<class T> inline static void eDiv(const T *, const T *, T *, jint);

    /**
     * Templatized binary maximum.
     */
    template<class T> inline static void eMax(const T *, const T *, T *, jint);

    /**
     * Templatized binary minimum.
     */
    template<class T> inline static void eMin(const T *, const T *, T *, jint);

    /**
     * Defines a complex-to-real operation.
     */
    typedef void ctorOp_t(const jcomplex *, jdouble *, jint);

    /**
     * Complex to real conversion by complex magnitudes.
     */
    inline static ctorOp_t ctorAbs;

    /**
     * Complex to real conversion by real part.
     */
    inline static ctorOp_t ctorRe;

    /**
     * Complex to real conversion by imaginary part.
     */
    inline static ctorOp_t ctorIm;

    /**
     * Defines a real-to-complex operation.
     */
    typedef void rtocOp_t(const jdouble *, jcomplex *, jint);

    /**
     * Real to complex conversion by real part.
     */
    inline static rtocOp_t rtocRe;

    /**
     * Real to complex conversion by imaginary part.
     */
    inline static rtocOp_t rtocIm;

    /**
     * Defines an integer-to-real operation.
     */
    typedef void itorOp_t(const jint *, jdouble *, jint);

    /**
     * Integer to real conversion by up-casting.
     */
    inline static itorOp_t itor;

private:

    template<class T> inline static T accumulatorOpProxy(JNIEnv *, T(*op)(const T *, jint), //
            jarray, jboolean);

    template<class T> inline static void unaryOpProxy(JNIEnv *, void(*op)(T, T *, jint), //
            jarray, T, jboolean);

    template<class T> inline static void binaryOpProxy(JNIEnv *, jint, //
            jarray, jarray, jarray, element_type);

    template<class S, class T> inline static void convertProxy(JNIEnv *, void(*op)(const S *, T *, jint), //
            jarray, jboolean, //
            jarray, jboolean);
};

#endif

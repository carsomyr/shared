/*
 * This file is part of the Shared Scientific Toolbox in Java ("this library").
 * 
 * Copyright (C) 2006 The Regents of the University of California
 * 
 * This library is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License, version 2, as published by the Free Software Foundation.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this library. If not, see
 * http://www.gnu.org/licenses/.
 */

#include <Common.hpp>

#include <JNIHeadersXWrap.hpp>

#include <fftw3.h>

#ifndef _Included_Plan
#define _Included_Plan

/**
 * A class for executing and manipulating <a href="http://www.fftw.org/">FFTW3</a> plans.
 */
class Plan {

public:

    /**
     * Performs an out-of-place transform.
     * 
     * @param env
     *      the JNI environment.
     * @param thisObj
     *      this object.
     * @param in
     *      the input array.
     * @param out
     *      the output array.
     */
    static void transform(JNIEnv *env, jobject thisObj, jdoubleArray in, jdoubleArray out);

    /**
     * Creates a pointer to the native peer.
     * 
     * @param env
     *      the JNI environment.
     * @param thisObj
     *      this object.
     * @param type
     *      the transform type.
     * @param dims
     *      the dimensions.
     * @param logicalMode
     *      the transform mode.
     * @return a pointer to the native peer.
     */
    static jbyteArray create(JNIEnv *env, jobject thisObj, jint type, jintArray dims, jint logicalMode);

    /**
     * Destroys the native peer.
     * 
     * @param env
     *      the JNI environment.
     * @param thisObj
     *      this object.
     */
    static void destroy(JNIEnv *env, jobject thisObj);

    /**
     * Exports learned wisdom to a string.
     * 
     * @param env
     *      the JNI environment.
     * @return the wisdom.
     */
    static jstring exportWisdom(JNIEnv *env);

    /**
     * Imports wisdom from a string.
     * 
     * @param env
     *      the JNI environment.
     * @param wisdom
     *      the source of wisdom.
     */
    static void importWisdom(JNIEnv *env, jstring wisdom);

    /**
     * Gets the transform parameters.
     * 
     * @param inLen
     *      the determined input array size.
     * @param outLen
     *      the determined output array size.
     * @param scalingFactor
     *      the determined scaling factor.
     * @param type
     *      the transform type.
     * @param dimsArr
     *      the dimensions.
     * @param ndims
     *      the number of dimensions.
     */
    inline static void getTransformParameters( //
            jint &inLen, jint &outLen, //
            jdouble &scalingFactor, jint type, const jint *dimsArr, jint ndims);

    /**
     * Creates a native plan.
     * 
     * @param type
     *      the transform type.
     * @param dimsArr
     *      the dimensions.
     * @param ndims
     *      the number of dimensions.
     * @param logicalMode
     *      the transform mode.
     * @param inLen
     *      the input array length.
     * @param outLen
     *      the output array length.
     * @return the native plan.
     */
    inline static fftw_plan createPlan( //
            jint type, const jint *dimsArr, jint ndims, //
            jint logicalMode, jint inLen, jint outLen);

    /**
     * Executes a native plan.
     * 
     * @param type
     *      the transform type.
     * @param plan
     *      the native plan.
     * @param inArr
     *      the input array.
     * @param inLen
     *      the input array length.
     * @param outArr
     *      the output array.
     * @param outLen
     *      the output array length.
     * @param scalingFactor
     *      the scaling factor.
     */
    inline static void executePlan( //
            jint type, fftw_plan plan, //
            jdouble *inArr, jint inLen, //
            jdouble *outArr, jint outLen, //
            jdouble scalingFactor);

    /**
     * Executes a complex-to-real transform, which requires special handling because it destroys its input.
     * 
     * @param plan
     *      the native plan.
     * @param inArr
     *      the input array.
     * @param outArr
     *      the output array.
     * @param inLen
     *      the input array length.
     */
    static void executePlan_C2R( //
            fftw_plan plan, jdouble *inArr, jdouble *outArr, jint inLen);

    /**
     * Initializes the Plan class.
     * 
     * @param env
     *      the JNI environment.
     */
    static void init(JNIEnv *env);

    /**
     * Destroys the Plan class.
     * 
     * @param env
     *      the JNI environment.
     */
    static void destroy(JNIEnv *env);
};

#endif

/*
 * Copyright (c) 2006 The Regents of the University of California
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
     * @param nDims
     *      the number of dimensions.
     */
    inline static void getTransformParameters( //
            jint &inLen, jint &outLen, //
            jdouble &scalingFactor, jint type, const jint *dimsArr, jint nDims);

    /**
     * Creates a native plan.
     * 
     * @param type
     *      the transform type.
     * @param dimsArr
     *      the dimensions.
     * @param nDims
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
            jint type, const jint *dimsArr, jint nDims, //
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
    static void executePlanCToR( //
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

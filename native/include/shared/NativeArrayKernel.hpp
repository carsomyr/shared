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

#include <Common.hpp>

#ifndef _Included_NativeArrayKernel
#define _Included_NativeArrayKernel

/**
 * A computational kernel for multidimensional array operations.
 */
class NativeArrayKernel {

public:

    /**
     * Checks if the given object is of type 'int[]'.
     * 
     * @param env
     *      the JNI environment.
     * @param obj
     *      the object.
     */
    static jboolean isJintArray(JNIEnv *env, jobject obj);

    /**
     * Checks if the given object is of type 'double[]'.
     * 
     * @param env
     *      the JNI environment.
     * @param obj
     *      the object.
     */
    static jboolean isJdoubleArray(JNIEnv *env, jobject obj);

    /**
     * Checks whether one type is assignable from another.
     * 
     * @param env
     *      the JNI environment.
     * @param to
     *      the assignee.
     * @param from
     *      the assigner.
     */
    static jboolean isAssignableFrom(JNIEnv *env, jclass to, jclass from);

    /**
     * Gets the component type of the given array type.
     * 
     * @param env
     *      the JNI environment.
     * @param clazz
     *      the array type.
     * @return the component type.
     */
    static jclass getComponentType(JNIEnv *env, jclass clazz);

    /**
     * Creates a Java array of type 'Object[]'.
     * 
     * @param env
     *      the JNI environment.
     * @param clazz
     *      the component type.
     * @param len
     *      the length.
     */
    static jobjectArray newArray(JNIEnv *env, jclass clazz, jint len);

    /**
     * Creates a sparse array.
     * 
     * @param env
     *      the JNI environment.
     * @param values
     *      the storage array.
     * @param indices
     *      the physical indices.
     * @param indirectionOffsets
     *      the offsets into the indirections.
     * @param indirections
     *      the indirection indices.
     * @return the sparse array.
     */
    static jobject newSparseArrayState(JNIEnv *env, //
            jarray values, jintArray indices, //
            jintArray indirectionOffsets, jintArray indirections);

    /**
     * Extracts the common array type.
     * 
     * @param env
     *      the JNI environment.
     * @param srcV
     *      the nominal source values.
     * @param dstV
     *      the nominal destination values.
     * @return the common array type.
     */
    static ArrayPinHandler::jarray_type getArrayType(JNIEnv *env, //
            jobject srcV, jobject dstV);

    /**
     * Initializes the kernel.
     * 
     * @param env
     *      the JNI environment.
     */
    static void init(JNIEnv *env);

    /**
     * Destroys the kernel.
     * 
     * @param env
     *      the JNI environment.
     */
    static void destroy(JNIEnv *env);
};

#endif

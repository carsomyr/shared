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

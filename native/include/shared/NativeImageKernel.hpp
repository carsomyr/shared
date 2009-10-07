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
#include <MappingOps.hpp>

#ifndef _Included_NativeImageKernel
#define _Included_NativeImageKernel

/**
 * A computational kernel for image processing.
 */
class NativeImageKernel {

public:

    /**
     * Creates an integral image.
     * 
     * @param env
     *      the JNI environment.
     * @param thisObj
     *      this object.
     * @param srcV
     *      the source values.
     * @param srcD
     *      the source dimensions.
     * @param srcS
     *      the source strides.
     * @param dstV
     *      the destination values.
     * @param dstD
     *      the destination dimensions.
     * @param dstS
     *      the destination strides.
     */
    static void createIntegralImage(JNIEnv *env, jobject thisObj, //
            jdoubleArray srcV, jintArray srcD, jintArray srcS, //
            jdoubleArray dstV, jintArray dstD, jintArray dstS);

    /**
     * Creates an integral histogram.
     * 
     * @param env
     *      the JNI environment.
     * @param thisObj
     *      this object.
     * @param srcV
     *      the source values.
     * @param srcD
     *      the source dimensions.
     * @param srcS
     *      the source strides.
     * @param memV
     *      the class memberships.
     * @param dstV
     *      the destination values.
     * @param dstD
     *      the destination dimensions.
     * @param dstS
     *      the destination strides.
     */
    static void createIntegralHistogram(JNIEnv *env, jobject thisObj, //
            jdoubleArray srcV, jintArray srcD, jintArray srcS, jintArray memV, //
            jdoubleArray dstV, jintArray dstD, jintArray dstS);

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

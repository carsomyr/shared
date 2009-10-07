/*
 * This file is part of the Shared Scientific Toolbox in Java ("this library").
 * 
 * Copyright (C) 2008 Roy Liu
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

#include <NativeArrayKernel.hpp>
#include <NativeImageKernel.hpp>

#ifndef _Included_Library
#define _Included_Library

/**
 * A class for setting up and tearing down the native library.
 */
class Library {

public:

    /**
     * On library load.
     * 
     * @param jvm
     *      the pointer to the Java Virtual Machine.
     * @param reserved
     *      the extra data.
     */
    static jint JNI_OnLoad(JavaVM *jvm, void *reserved);

    /**
     * On library unload.
     * 
     * @param jvm
     *      the pointer to the Java Virtual Machine.
     * @param reserved
     *      the extra data.
     */
    static void JNI_OnUnload(JavaVM *jvm, void *reserved);

    /**
     * Registers a native service provider.
     * 
     * @param env
     *      the JNI environment.
     * @param specClassName
     *      the specification class name.
     * @param implClassName
     *      the implementation class name.
     */
    static void registerService(JNIEnv *env, //
            const char *specClassName, const char *implClassName);

    /**
     * Initializes the native library.
     * 
     * @param env
     *      the JNI environment.
     */
    static void init(JNIEnv *env);

    /**
     * Destroys the native library.
     * 
     * @param env
     *      the JNI environment.
     */
    static void destroy(JNIEnv *env);
};

#endif

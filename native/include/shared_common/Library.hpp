/*
 * Copyright (C) 2008 Roy Liu
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

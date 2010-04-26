/*
 * Copyright (C) 2008-2010 Roy Liu
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

#include <Library.hpp>

static jclass libraryClass = NULL;
static jclass servicesClass = NULL;

static jmethodID registerServiceMethodID;

static jfieldID initializedFieldID;

jint Library::JNI_OnLoad(JavaVM *jvm, void *reserved) {

    void *env_p;

    if (jvm->GetEnv(&env_p, JNI_VERSION_1_4)) {
        return JNI_ERR;
    }

    JNIEnv *env = (JNIEnv *) env_p;

    //

    try {

        libraryClass = (jclass) Common::newWeakGlobalRef(env, //
                Common::findClass(env, "shared/metaclass/Library"));
        servicesClass = (jclass) Common::newWeakGlobalRef(env, //
                Common::findClass(env, "shared/util/Services"));

        initializedFieldID = Common::getStaticFieldID(env, libraryClass, //
                "INITIALIZED", "Z");

        registerServiceMethodID = Common::getStaticMethodID(env, servicesClass, //
                "registerService", "(Ljava/lang/Class;Ljava/lang/Class;)V");

        //

        NativeArrayKernel::init(env);
        Library::registerService(env, //
                "shared/array/kernel/ArrayKernel", "shared/array/jni/NativeArrayKernel");

        NativeImageKernel::init(env);
        Library::registerService(env, //
                "shared/image/kernel/ImageKernel", "shared/image/jni/NativeImageKernel");

#ifdef sstx_EXPORTS

        Library::init(env);
        Library::registerService(env, //
                "shared/fft/FFTService", "sharedx/fftw/FFTWService");

#endif

        // Set the initialization flag field to be true.
        env->SetStaticBooleanField(libraryClass, initializedFieldID, JNI_TRUE);

    } catch (std::exception &e) {

        Common::throwNew(env, e);
    }

    return JNI_VERSION_1_4;
}

void Library::JNI_OnUnload(JavaVM *jvm, void *reserved) {

    void *env_p;

    if (jvm->GetEnv(&env_p, JNI_VERSION_1_4)) {
        return;
    }

    JNIEnv *env = (JNIEnv *) env_p;

    NativeArrayKernel::destroy(env);
    NativeImageKernel::destroy(env);

#ifdef sstx_EXPORTS

    Library::destroy(env);

#endif

    //

    // Set the initialization flag field to be false.
    env->SetStaticBooleanField(libraryClass, initializedFieldID, JNI_FALSE);

    Common::deleteWeakGlobalRef(env, servicesClass);
    Common::deleteWeakGlobalRef(env, libraryClass);
}

void Library::registerService(JNIEnv *env, //
        const char *specClassName, const char *implClassName) {

    jclass specClass = Common::findClass(env, specClassName);
    jclass implClass = Common::findClass(env, implClassName);

    env->CallStaticVoidMethod(servicesClass, registerServiceMethodID, specClass, implClass);

    if (env->ExceptionCheck()) {
        throw std::runtime_error("shared.util.Services#registerService invocation failed");
    }
}

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

    Common::deleteWeakGlobalRef(env, libraryClass);
    Common::deleteWeakGlobalRef(env, servicesClass);

    // Set the initialization flag field to be false.
    env->SetStaticBooleanField(libraryClass, initializedFieldID, JNI_FALSE);
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

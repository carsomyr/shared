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

#include <NativeArrayKernel.hpp>

static jclass jclassClass = NULL;
static jmethodID isAssignableFromMethodID;
static jmethodID getComponentTypeMethodID;

static jclass sparseArrayStateClass = NULL;
static jmethodID sparseArrayStateMethodID;

static jclass jintArrayClass = NULL;
static jclass jdoubleArrayClass = NULL;
static jclass jobjectArrayClass = NULL;

void NativeArrayKernel::init(JNIEnv *env) {

    jclassClass = (jclass) Common::newWeakGlobalRef(env, //
            Common::findClass(env, "java/lang/Class"));
    isAssignableFromMethodID = Common::getMethodID(env, jclassClass, //
            "isAssignableFrom", "(Ljava/lang/Class;)Z");
    getComponentTypeMethodID = Common::getMethodID(env, jclassClass, //
            "getComponentType", "()Ljava/lang/Class;");

    jintArrayClass = (jclass) Common::newWeakGlobalRef(env, //
            Common::findClass(env, "[I"));
    jdoubleArrayClass = (jclass) Common::newWeakGlobalRef(env, //
            Common::findClass(env, "[D"));
    jobjectArrayClass = (jclass) Common::newWeakGlobalRef(env, //
            Common::findClass(env, "[Ljava/lang/Object;"));

    sparseArrayStateClass = (jclass) Common::newWeakGlobalRef(env, //
            Common::findClass(env, "shared/array/sparse/SparseArrayState"));
    sparseArrayStateMethodID = Common::getMethodID(env, sparseArrayStateClass, //
            "<init>", "(Ljava/lang/Object;[I[I[I)V");
}

void NativeArrayKernel::destroy(JNIEnv *env) {

    Common::deleteWeakGlobalRef(env, jclassClass);
    Common::deleteWeakGlobalRef(env, jintArrayClass);
    Common::deleteWeakGlobalRef(env, jdoubleArrayClass);
    Common::deleteWeakGlobalRef(env, jobjectArrayClass);

    Common::deleteWeakGlobalRef(env, sparseArrayStateClass);
}

jboolean NativeArrayKernel::isJintArray(JNIEnv *env, jobject obj) {
    return obj && env->IsInstanceOf(obj, jintArrayClass);
}

jboolean NativeArrayKernel::isJdoubleArray(JNIEnv *env, jobject obj) {
    return obj && env->IsInstanceOf(obj, jdoubleArrayClass);
}

jboolean NativeArrayKernel::isAssignableFrom(JNIEnv *env, jclass to, jclass from) {

    jboolean res = env->CallBooleanMethod(to, isAssignableFromMethodID, from);

    if (env->ExceptionCheck()) {
        throw std::runtime_error("java.lang.Class#isAssignableFrom invocation failed");
    }

    return res;
}

jclass NativeArrayKernel::getComponentType(JNIEnv *env, jclass clazz) {

    jclass res = (jclass) env->CallObjectMethod(clazz, getComponentTypeMethodID);

    if (env->ExceptionCheck()) {
        throw std::runtime_error("java.lang.Class#getComponentType invocation failed");
    }

    return res;
}

jobjectArray NativeArrayKernel::newArray(JNIEnv *env, jclass clazz, jint len) {
    return Common::newObjectArray(env, len, clazz);
}

jobject NativeArrayKernel::newSparseArrayState(JNIEnv *env, //
        jarray values, jintArray indices, //
        jintArray indirectionOffsets, jintArray indirections) {

    jobject res = env->NewObject(sparseArrayStateClass, sparseArrayStateMethodID, //
            values, indices, indirectionOffsets, indirections);

    if (env->ExceptionCheck()) {
        throw std::runtime_error("shared.array.sparse.SparseArrayState#<init> invocation failed");
    }

    return res;
}

ArrayPinHandler::jarray_type NativeArrayKernel::getArrayType(JNIEnv *env, //
        jobject srcV, jobject dstV) {

    if (isJdoubleArray(env, srcV) && isJdoubleArray(env, dstV)) {

        return ArrayPinHandler::DOUBLE;

    } else if (isJintArray(env, srcV) && isJintArray(env, dstV)) {

        return ArrayPinHandler::INT;

    } else if (isAssignableFrom(env, env->GetObjectClass(dstV), env->GetObjectClass(srcV))) {

        return ArrayPinHandler::OBJECT;

    } else {

        throw std::runtime_error("Invalid array types");
    }
}

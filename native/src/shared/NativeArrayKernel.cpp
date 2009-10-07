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

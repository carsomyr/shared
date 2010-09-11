/*
 * Copyright (c) 2007 Roy Liu
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

template<class T> CleanupHandler<T>::~CleanupHandler() {
}

MallocHandler::MallocHandler(jint nBytes) {

    this->ptr = malloc(nBytes);

    if (!this->ptr) {
        throw std::runtime_error("Allocation failed");
    }
}

void *MallocHandler::get() {
    return this->ptr;
}

MallocHandler::~MallocHandler() {
    free(this->ptr);
}

ArrayPinHandler::ArrayPinHandler(JNIEnv *env, const jarray array, //
        ArrayPinHandler::jarray_type type, ArrayPinHandler::release_mode mode) {

    jboolean isCopy = JNI_FALSE;

    this->env = env;
    this->array = array;
    this->type = type;
    this->mode = mode;
    this->pointer = NULL;

    switch (type) {

    case DOUBLE:
        this->pointer = env->GetDoubleArrayElements((jdoubleArray) array, &isCopy);
        break;

    case INT:
        this->pointer = env->GetIntArrayElements((jintArray) array, &isCopy);
        break;

    case BYTE:
        this->pointer = env->GetByteArrayElements((jbyteArray) array, &isCopy);
        break;

    case PRIMITIVE:
        this->pointer = env->GetPrimitiveArrayCritical((jarray) array, &isCopy);
        break;

    case STRING_UTF:
        this->pointer = (void *) env->GetStringUTFChars((jstring) array, &isCopy);
        break;

    case STRING:
        this->pointer = (void *) env->GetStringChars((jstring) array, &isCopy);
        break;

    case STRING_PRIMITIVE:
        this->pointer = (void *) env->GetStringCritical((jstring) array, &isCopy);
        break;

    default:
        throw std::runtime_error("Array type not recognized");
    }

    // Throw an exception on failure.
    if (!this->pointer) {
        throw std::runtime_error("Could not pin array");
    }
}

void *ArrayPinHandler::get() {
    return this->pointer;
}

ArrayPinHandler::~ArrayPinHandler() {

    JNIEnv *env = this->env;
    jarray array = this->array;
    ArrayPinHandler::jarray_type type = this->type;
    ArrayPinHandler::release_mode mode = this->mode;
    void *pointer = this->pointer;

    switch (type) {

    case DOUBLE:
        env->ReleaseDoubleArrayElements((jdoubleArray) array, (jdouble *) pointer, mode);
        break;

    case INT:
        env->ReleaseIntArrayElements((jintArray) array, (jint *) pointer, mode);
        break;

    case BYTE:
        env->ReleaseByteArrayElements((jbyteArray) array, (jbyte *) pointer, mode);
        break;

    case PRIMITIVE:
        env->ReleasePrimitiveArrayCritical((jarray) array, (void *) pointer, mode);
        break;

    case STRING_UTF:
        env->ReleaseStringUTFChars((jstring) array, (char *) pointer);
        break;

    case STRING:
        env->ReleaseStringChars((jstring) array, (jchar *) pointer);
        break;

    case STRING_PRIMITIVE:
        env->ReleaseStringCritical((jstring) array, (jchar *) pointer);
        break;

    default:
        env->FatalError("Array type not recognized for unpin");
        break;
    }
}

MonitorHandler::MonitorHandler(JNIEnv *env, const jobject obj) {

    if (env->MonitorEnter(obj)) {
        throw std::runtime_error("Could not acquire monitor");
    }

    this->env = env;
    this->obj = obj;
}

void MonitorHandler::get() {
    return;
}

MonitorHandler::~MonitorHandler() {

    JNIEnv *env = this->env;
    jobject obj = this->obj;

    if (env->MonitorExit(obj)) {

        env->FatalError("Could not release monitor");
        return;
    }
}

//

void Common::throwNew(JNIEnv *env, std::exception &exception) {

    if (!env->ExceptionCheck()) {
        env->ThrowNew(env->FindClass("java/lang/RuntimeException"), exception.what());
    }
}

jclass Common::findClass(JNIEnv *env, const char *name) {

    jclass clazz = env->FindClass(name);

    if (env->ExceptionCheck()) {
        throw std::runtime_error("Class not found");
    }

    return clazz;
}

jfieldID Common::getFieldID(JNIEnv *env, jclass clazz, //
        const char *name, const char *type) {

    jfieldID id = env->GetFieldID(clazz, name, type);

    if (env->ExceptionCheck()) {
        throw std::runtime_error("Member field not found");
    }

    return id;
}

jfieldID Common::getStaticFieldID(JNIEnv *env, jclass clazz, //
        const char *name, const char *type) {

    jfieldID id = env->GetStaticFieldID(clazz, name, type);

    if (env->ExceptionCheck()) {
        throw std::runtime_error("Static field not found");
    }

    return id;
}

jmethodID Common::getMethodID(JNIEnv *env, jclass clazz, //
        const char *name, const char *type) {

    jmethodID id = env->GetMethodID(clazz, name, type);

    if (env->ExceptionCheck()) {
        throw std::runtime_error("Member method not found");
    }

    return id;
}

jmethodID Common::getStaticMethodID(JNIEnv *env, jclass clazz, //
        const char *name, const char *type) {

    jmethodID id = env->GetStaticMethodID(clazz, name, type);

    if (env->ExceptionCheck()) {
        throw std::runtime_error("Static method not found");
    }

    return id;
}

jweak Common::newWeakGlobalRef(JNIEnv *env, jobject object) {

    jweak weak = env->NewWeakGlobalRef(object);

    if (env->ExceptionCheck()) {
        throw std::runtime_error("Could not create weak global reference");
    }

    return weak;
}

void Common::deleteWeakGlobalRef(JNIEnv *env, jweak weak) {
    env->DeleteWeakGlobalRef(weak);
}

jdoubleArray Common::newDoubleArray(JNIEnv *env, jint len) {

    jdoubleArray array = env->NewDoubleArray(len);

    if (!array) {
        throw std::runtime_error("Could not create double array");
    }

    return array;
}

jintArray Common::newIntArray(JNIEnv *env, jint len) {

    jintArray array = env->NewIntArray(len);

    if (!array) {
        throw std::runtime_error("Could not create int array");
    }

    return array;
}

jbyteArray Common::newByteArray(JNIEnv *env, jint len) {

    jbyteArray array = env->NewByteArray(len);

    if (!array) {
        throw std::runtime_error("Could not create byte array");
    }

    return array;
}

jobjectArray Common::newObjectArray(JNIEnv *env, jint len, jclass clazz) {

    jobjectArray array = env->NewObjectArray(len, clazz, NULL);

    if (env->ExceptionCheck()) {
        throw std::runtime_error("Could not create object array");
    }

    return array;
}

jstring Common::newStringUTF(JNIEnv *env, const char *utf) {

    jstring str = env->NewStringUTF(utf);

    if (env->ExceptionCheck()) {
        throw std::runtime_error("Could not create string");
    }

    return str;
}

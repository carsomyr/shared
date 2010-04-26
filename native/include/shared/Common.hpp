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

#include <math.h>

#include <algorithm>
#include <stdexcept>
#include <cstring>

#include <JNIWrap.hpp>

#ifndef _Included_Common
#define _Included_Common

/**
 * An object which manages some resource. Its life cycle is tied to the local scope.
 */
template<class T> class CleanupHandler {

public:

    /**
     * Gets the resource.
     */
    virtual T get() = 0;
};

/**
 * A subclass of CleanupHandler for allocating and freeing heap memory.
 */
class MallocHandler: public CleanupHandler<void *> {

public:

    /**
     * Requests the given number of bytes.
     * 
     * @param nbytes
     *      the number of bytes.
     */
    explicit MallocHandler(jint nbytes);

    virtual void *get();

    virtual ~MallocHandler();

private:

    MallocHandler(const MallocHandler &);

    MallocHandler &operator=(const MallocHandler &);

    void *ptr;
};

/**
 * A subclass of CleanupHandler for pinning and releasing arrays.
 */
class ArrayPinHandler: public CleanupHandler<void *> {

public:

    /**
     * An enumeration of Java array types.
     */
    enum jarray_type {

        /**
         * Indicates an array of 'double'.
         */
        DOUBLE, //

        /**
         * Indicates an array of 'int'.
         */
        INT, //

        /**
         * Indicates an array of 'byte'.
         */
        BYTE, //

        /**
         * Indicates a critical array.
         */
        PRIMITIVE, //

        /**
         * Indicates a string with UTF encoding.
         */
        STRING_UTF, //

        /**
         * Indicates a string with Unicode encoding.
         */
        STRING, //

        /**
         * Indicates a critical string.
         */
        STRING_PRIMITIVE, //

        /**
         * Indicates an array of 'Object'.
         */
        OBJECT
    };

    /**
     * An enumeration of array release modes.
     */
    enum release_mode {

        /**
         * Indicates read-write.
         */
        READ_WRITE = 0, //

        /**
         * Indicates read-only.
         */
        READ_ONLY = JNI_ABORT
    };

    /**
     * Default constructor.
     * 
     * @param env
     *      the JNI environment.
     * @param array
     *      the array.
     * @param type
     *      the array type.
     * @param mode
     *      the release mode.
     */
    explicit ArrayPinHandler(JNIEnv *env, const jarray array, //
            jarray_type type, release_mode mode);

    virtual void *get();

    virtual ~ArrayPinHandler();

private:

    ArrayPinHandler(const ArrayPinHandler &);

    ArrayPinHandler &operator=(const ArrayPinHandler &);

    JNIEnv *env;

    jarray array;

    jarray_type type;

    release_mode mode;

    void *pointer;
};

/**
 * A subclass of CleanupHandler for acquiring and releasing monitors.
 */
class MonitorHandler: public CleanupHandler<void> {

public:

    /**
     * Default constructor.
     * 
     * @param env
     *      the JNI environment.
     * @param obj
     *      the monitor.
     */
    explicit MonitorHandler(JNIEnv *env, const jobject obj);

    virtual void get();

    virtual ~MonitorHandler();

private:

    MonitorHandler(const MonitorHandler &);

    MonitorHandler &operator=(const MonitorHandler &);

    JNIEnv *env;

    jobject obj;
};

/**
 * A storage structure for entries with a sorting key and a payload.
 */
template<class V, class P> struct permutation_entry {

    /**
     * The sorting key.
     */
    V value;

    /**
     * The payload.
     */
    P payload;

    /**
     * Default constructor.
     * 
     * @param value
     *      the sorting key.
     * @param payload
     *      the payload.
     */
    inline permutation_entry(V value, P payload) {

        this->value = value;
        this->payload = payload;
    }

    /**
     * Performs a comparison on the basis of sorting keys.
     * 
     * @param a
     *      the left hand side.
     * @param b
     *      the right hand side.
     */
    inline friend bool operator<(const permutation_entry &a, const permutation_entry &b) {
        return a.value < b.value;
    }
};

/**
 * A representation of complex numbers.
 */
struct jcomplex {

    /**
     * The real part.
     */
    jdouble re;

    /**
     * The imaginary part.
     */
    jdouble im;

    /**
     * Default constructor.
     * 
     * @param re
     *      the real part.
     * @param im
     *      the imaginary part.
     */
    inline jcomplex(jdouble re, jdouble im) {

        this->re = re;
        this->im = im;
    }

    /**
     * Alternate constructor.
     */
    inline jcomplex() {

        this->re = 0;
        this->im = 0;
    }

    /**
     * Adds two complex numbers.
     * 
     * @param a
     *      the left hand side.
     * @param b
     *      the right hand side.
     */
    inline friend jcomplex operator+(const jcomplex &a, const jcomplex &b) {
        return jcomplex(a.re + b.re, a.im + b.im);
    }

    /**
     * Subtracts two complex numbers.
     * 
     * @param a
     *      the left hand side.
     * @param b
     *      the right hand side.
     */
    inline friend jcomplex operator-(const jcomplex &a, const jcomplex &b) {
        return jcomplex(a.re - b.re, a.im - b.im);
    }

    /**
     * Multiplies two complex numbers.
     * 
     * @param a
     *      the left hand side.
     * @param b
     *      the right hand side.
     */
    inline friend jcomplex operator*(const jcomplex &a, const jcomplex &b) {
        return jcomplex(a.re * b.re - a.im * b.im, a.re * b.im + a.im * b.re);
    }

    /**
     * Divides two complex numbers.
     * 
     * @param a
     *      the left hand side.
     * @param b
     *      the right hand side.
     */
    inline friend jcomplex operator/(const jcomplex &a, const jcomplex &b) {
        return jcomplex( //
                (a.re * b.re + a.im * b.im) / (b.re * b.re + b.im * b.im), //
                (a.im * b.re - a.re * b.im) / (b.re * b.re + b.im * b.im));
    }

    /**
     * Adds two complex numbers and mutates the left hand side.
     * 
     * @param a
     *      the left hand side.
     * @param b
     *      the right hand side.
     */
    inline friend jcomplex &operator+=(jcomplex &a, const jcomplex &b) {

        a.re += b.re;
        a.im += b.im;

        return a;
    }

    /**
     * Multiplies two complex numbers and mutates the left hand side.
     * 
     * @param a
     *      the left hand side.
     * @param b
     *      the right hand side.
     */
    inline friend jcomplex &operator*=(jcomplex &a, const jcomplex &b) {

        jdouble aRe = a.re * b.re - a.im * b.im;
        jdouble aIm = a.re * b.im + a.im * b.re;

        a.re = aRe;
        a.im = aIm;

        return a;
    }

    /**
     * Performs a comparison on the basis of magnitudes.
     * 
     * @param a
     *      the left hand side.
     * @param b
     *      the right hand side.
     */
    inline friend bool operator<(const jcomplex &a, const jcomplex &b) {
        return (a.re * a.re + a.im * a.im) < (b.re * b.re + b.im * b.im);
    }

    /**
     * Gets the complex magnitude.
     */
    inline jdouble abs() {
        return sqrt(this->re * this->re + this->im * this->im);
    }
};

/**
 * A class of commonly used static methods.
 */
class Common {

public:

    /**
     * Takes the sum of an array of values.
     * 
     * @param values
     *      the values.
     * @param len
     *      the length.
     * @param zero
     *      the concept of '0'.
     */
    template<class T> static T sum(T *values, jint len, T zero) {

        T acc = zero;

        for (jint i = 0; i < len; i++) {
            acc += values[i];
        }

        return acc;
    }

    /**
     * Takes the product of an array of values.
     * 
     * @param values
     *      the values.
     * @param len
     *      the length.
     * @param one
     *      the concept of '1'.
     */
    template<class T> static T product(T *values, jint len, T one) {

        T acc = one;

        for (jint i = 0; i < len; i++) {
            acc *= values[i];
        }

        return acc;
    }

    /**
     * Throws a new Java RuntimeException.
     * 
     * @param env
     *      the JNI environment.
     * @param exception
     *      the exception.
     */
    static void throwNew(JNIEnv *env, std::exception &exception);

    /**
     * Finds and loads a class.
     * 
     * @param env
     *      the JNI environment.
     * @param name
     *      the class name.
     */
    static jclass findClass(JNIEnv *env, const char *name);

    /**
     * Gets a field identifier.
     * 
     * @param env
     *      the JNI environment.
     * @param clazz
     *      the class.
     * @param name
     *      the class name.
     * @param type
     *      the type.
     */
    static jfieldID getFieldID(JNIEnv *env, //
            jclass clazz, const char *name, const char *type);

    /**
     * Gets a static field identifier.
     * 
     * @param env
     *      the JNI environment.
     * @param clazz
     *      the class.
     * @param name
     *      the class name.
     * @param type
     *      the type.
     */
    static jfieldID getStaticFieldID(JNIEnv *env, //
            jclass clazz, const char *name, const char *type);

    /**
     * Gets a method identifier.
     * 
     * @param env
     *      the JNI environment.
     * @param clazz
     *      the class.
     * @param name
     *      the class name.
     * @param type
     *      the type.
     */
    static jmethodID getMethodID(JNIEnv *env, //
            jclass clazz, const char *name, const char *type);

    /**
     * Gets a static method identifier.
     * 
     * @param env
     *      the JNI environment.
     * @param clazz
     *      the class.
     * @param name
     *      the class name.
     * @param type
     *      the type.
     */
    static jmethodID getStaticMethodID(JNIEnv *env, //
            jclass clazz, const char *name, const char *type);

    /**
     * Creates a global weak reference to the given object.
     * 
     * @param env
     *      the JNI environment.
     * @param object
     *      the referent.
     */
    static jweak newWeakGlobalRef(JNIEnv *env, jobject object);

    /**
     * Deletes a global weak reference.
     * 
     * @param env
     *      the JNI environment.
     * @param weak
     *      the weak reference.
     */
    static void deleteWeakGlobalRef(JNIEnv *env, jweak weak);

    /**
     * Creates a Java array of type 'double[]'.
     * 
     * @param env
     *      the JNI environment.
     * @param len
     *      the length.
     */
    static jdoubleArray newDoubleArray(JNIEnv *env, jint len);

    /**
     * Creates a Java array of type 'int[]'.
     * 
     * @param env
     *      the JNI environment.
     * @param len
     *      the length.
     */
    static jintArray newIntArray(JNIEnv *env, jint len);

    /**
     * Creates a Java array of type 'byte[]'.
     * 
     * @param env
     *      the JNI environment.
     * @param len
     *      the length.
     */
    static jbyteArray newByteArray(JNIEnv *env, jint len);

    /**
     * Creates a Java array of the given component type.
     * 
     * @param env
     *      the JNI environment.
     * @param len
     *      the length.
     * @param clazz
     *      the component type.
     */
    static jobjectArray newObjectArray(JNIEnv *env, jint len, jclass clazz);

    /**
     * Creates a Java string.
     * 
     * @param env
     *      the JNI environment.
     * @param utf
     *      the null-terminated string.
     */
    static jstring newStringUTF(JNIEnv *env, const char *utf);
};

#endif

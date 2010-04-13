/*
 * This file is part of the Shared Scientific Toolbox in Java ("this library").
 * 
 * Copyright (C) 2008 The Regents of the University of California
 * 
 * This library is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License, version 2, as published by the Free Software Foundation.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this library. If not, see
 * http://www.gnu.org/licenses/.
 */

#include <BindingsX.hpp>

JNIEXPORT void JNICALL Java_sharedx_fftw_Plan_transform(JNIEnv *env, jobject thisObj, jdoubleArray in, jdoubleArray out) {
    Plan::transform(env, thisObj, in, out);
}

JNIEXPORT jbyteArray JNICALL Java_sharedx_fftw_Plan_create(JNIEnv *env, jobject thisObj, jint type, //
        jintArray dims, jint logicalMode) {
    return Plan::create(env, thisObj, type, dims, logicalMode);
}

JNIEXPORT void JNICALL Java_sharedx_fftw_Plan_destroy(JNIEnv *env, jobject thisObj) {
    Plan::destroy(env, thisObj);
}

JNIEXPORT jstring JNICALL Java_sharedx_fftw_Plan_exportWisdom(JNIEnv *env, jclass clazz) {
    return Plan::exportWisdom(env);
}

JNIEXPORT void JNICALL Java_sharedx_fftw_Plan_importWisdom(JNIEnv *env, jclass clazz, jstring wisdom) {
    Plan::importWisdom(env, wisdom);
}

JNIEXPORT void JNICALL Java_sharedx_test_BenchmarkNative_testConvolve(JNIEnv *env, jobject thisObj) {
    Benchmark::testConvolve(env, thisObj);
}

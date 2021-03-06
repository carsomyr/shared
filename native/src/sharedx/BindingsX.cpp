/*
 * Copyright (c) 2008 The Regents of the University of California
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

#include <BindingsX.hpp>

JNIEXPORT void JNICALL Java_org_sharedx_fftw_Plan_transform(JNIEnv *env, jobject thisObj, //
        jdoubleArray in, jdoubleArray out) {
    Plan::transform(env, thisObj, in, out);
}

JNIEXPORT jbyteArray JNICALL Java_org_sharedx_fftw_Plan_create(JNIEnv *env, jobject thisObj, jint type, //
        jintArray dims, jint logicalMode) {
    return Plan::create(env, thisObj, type, dims, logicalMode);
}

JNIEXPORT void JNICALL Java_org_sharedx_fftw_Plan_destroy(JNIEnv *env, jobject thisObj) {
    Plan::destroy(env, thisObj);
}

JNIEXPORT jstring JNICALL Java_org_sharedx_fftw_Plan_exportWisdom(JNIEnv *env, jclass clazz) {
    return Plan::exportWisdom(env);
}

JNIEXPORT void JNICALL Java_org_sharedx_fftw_Plan_importWisdom(JNIEnv *env, jclass clazz, jstring wisdom) {
    Plan::importWisdom(env, wisdom);
}

JNIEXPORT void JNICALL Java_org_sharedx_test_BenchmarkNative_testConvolve(JNIEnv *env, jobject thisObj) {
    Benchmark::testConvolve(env, thisObj);
}

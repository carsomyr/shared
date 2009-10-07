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

#include <Bindings.hpp>

JNIEXPORT void JNICALL Java_shared_array_jni_NativeArrayKernel_randomize(JNIEnv *env, jobject thisObj) {
    srand((jlong) time(NULL));
}

JNIEXPORT void JNICALL Java_shared_array_jni_NativeArrayKernel_derandomize(JNIEnv *env, jobject thisObj) {
    srand(0);
}

JNIEXPORT void JNICALL Java_shared_array_jni_NativeArrayKernel_map(JNIEnv *env, jobject thisObj, //
        jintArray bounds, //
        jobject srcV, jintArray srcD, jintArray srcS, //
        jobject dstV, jintArray dstD, jintArray dstS) {
    MappingOps::map(env, thisObj, //
            bounds, //
            srcV, srcD, srcS, //
            dstV, dstD, dstS);
}

JNIEXPORT void JNICALL Java_shared_array_jni_NativeArrayKernel_slice(JNIEnv *env, jobject thisObj, //
        jintArray slices, //
        jobject srcV, jintArray srcD, jintArray srcS, //
        jobject dstV, jintArray dstD, jintArray dstS) {
    MappingOps::slice(env, thisObj, //
            slices, //
            srcV, srcD, srcS, //
            dstV, dstD, dstS);
}

JNIEXPORT void JNICALL Java_shared_array_jni_NativeArrayKernel_rrOp(JNIEnv *env, jobject thisObj, jint type, //
        jdoubleArray srcV, jintArray srcD, jintArray srcS, //
        jdoubleArray dstV, jintArray dstD, jintArray dstS, //
        jintArray selectedDims) {
    DimensionOps::rrOp(env, thisObj, type, //
            srcV, srcD, srcS, //
            dstV, dstD, dstS, //
            selectedDims);
}

JNIEXPORT void JNICALL Java_shared_array_jni_NativeArrayKernel_riOp(JNIEnv *env, jobject thisObj, jint type, //
        jdoubleArray srcV, jintArray srcD, jintArray srcS, //
        jintArray dstV, //
        jint dim) {
    DimensionOps::riOp(env, thisObj, type, //
            srcV, srcD, srcS, //
            dstV, //
            dim);
}

JNIEXPORT void JNICALL Java_shared_array_jni_NativeArrayKernel_rdOp(JNIEnv *env, jobject thisObj, jint type, //
        jdoubleArray srcV, jintArray srcD, jintArray srcS, jdoubleArray dstV, //
        jintArray selectedDims) {
    DimensionOps::rdOp(env, thisObj, type, //
            srcV, srcD, srcS, dstV, //
            selectedDims);
}

JNIEXPORT jdouble JNICALL Java_shared_array_jni_NativeArrayKernel_raOp(JNIEnv *env, jobject thisObj, jint type, //
        jdoubleArray srcV) {
    return ElementOps::raOp(env, thisObj, type, srcV);
}

JNIEXPORT jdoubleArray JNICALL Java_shared_array_jni_NativeArrayKernel_caOp(JNIEnv *env, jobject thisObj, jint type, //
        jdoubleArray srcV) {
    return ElementOps::caOp(env, thisObj, type, srcV);
}

JNIEXPORT void JNICALL Java_shared_array_jni_NativeArrayKernel_ruOp(JNIEnv *env, jobject thisObj, jint type, //
        jdouble a, jdoubleArray srcV) {
    ElementOps::ruOp(env, thisObj, type, a, srcV);
}

JNIEXPORT void JNICALL Java_shared_array_jni_NativeArrayKernel_cuOp(JNIEnv *env, jobject thisObj, jint type, //
        jdouble aRe, jdouble aIm, jdoubleArray srcV) {
    ElementOps::cuOp(env, thisObj, type, aRe, aIm, srcV);
}

JNIEXPORT void JNICALL Java_shared_array_jni_NativeArrayKernel_iuOp(JNIEnv *env, jobject thisObj, jint type, //
        jint a, jintArray srcV) {
    ElementOps::iuOp(env, thisObj, type, a, srcV);
}

JNIEXPORT void JNICALL Java_shared_array_jni_NativeArrayKernel_eOp(JNIEnv *env, jobject thisObj, jint type, //
        jobject lhsV, jobject rhsV, jobject dstV, jboolean isComplex) {
    ElementOps::eOp(env, thisObj, type, lhsV, rhsV, dstV, isComplex);
}

JNIEXPORT void JNICALL Java_shared_array_jni_NativeArrayKernel_convert(JNIEnv *env, jobject thisObj, jint type, //
        jobject srcV, jboolean srcIsComplex, jobject dstV, jboolean dstIsComplex) {
    ElementOps::convert(env, thisObj, type, srcV, srcIsComplex, dstV, dstIsComplex);
}

JNIEXPORT void JNICALL Java_shared_array_jni_NativeArrayKernel_mul(JNIEnv *env, jobject thisObj, //
        jdoubleArray lhsV, jdoubleArray rhsV, jint lhsR, jint rhsC, jdoubleArray dstV, jboolean isComplex) {
    MatrixOps::mul(env, thisObj, lhsV, rhsV, lhsR, rhsC, dstV, isComplex);
}

JNIEXPORT void JNICALL Java_shared_array_jni_NativeArrayKernel_diag(JNIEnv *env, jobject thisObj, //
        jdoubleArray srcV, jdoubleArray dstV, jint size, jboolean isComplex) {
    MatrixOps::diag(env, thisObj, srcV, dstV, size, isComplex);
}

JNIEXPORT void JNICALL Java_shared_array_jni_NativeArrayKernel_svd(JNIEnv *env, jobject thisObj, //
        jdoubleArray srcV, jint srcStrideRow, jint srcStrideCol, //
        jdoubleArray uV, jdoubleArray sV, jdoubleArray vV, //
        jint nrows, jint ncols) {
    LinearAlgebraOps::svd(env, thisObj, srcV, srcStrideRow, srcStrideCol, uV, sV, vV, nrows, ncols);
}

JNIEXPORT void JNICALL Java_shared_array_jni_NativeArrayKernel_eigs(JNIEnv *env, jobject thisObj, //
        jdoubleArray srcV, jdoubleArray vecV, jdoubleArray valV, jint size) {
    LinearAlgebraOps::eigs(env, thisObj, srcV, vecV, valV, size);
}

JNIEXPORT void JNICALL Java_shared_array_jni_NativeArrayKernel_invert(JNIEnv *env, jobject thisObj, //
        jdoubleArray srcV, jdoubleArray dstV, jint size) {
    LinearAlgebraOps::invert(env, thisObj, srcV, dstV, size);
}

JNIEXPORT void JNICALL Java_shared_image_jni_NativeImageKernel_createIntegralImage(JNIEnv *env, jobject thisObj, //
        jdoubleArray srcV, jintArray srcD, jintArray srcS, //
        jdoubleArray dstV, jintArray dstD, jintArray dstS) {
    NativeImageKernel::createIntegralImage(env, thisObj, //
            srcV, srcD, srcS, //
            dstV, dstD, dstS);
}

JNIEXPORT void JNICALL Java_shared_image_jni_NativeImageKernel_createIntegralHistogram(JNIEnv *env, jobject thisObj, //
        jdoubleArray srcV, jintArray srcD, jintArray srcS, jintArray memV, //
        jdoubleArray dstV, jintArray dstD, jintArray dstS) {
    NativeImageKernel::createIntegralHistogram(env, thisObj, //
            srcV, srcD, srcS, memV, //
            dstV, dstD, dstS);
}

JNIEXPORT jintArray JNICALL Java_shared_array_jni_NativeArrayKernel_find(JNIEnv *env, jobject thisObj, //
        jintArray srcV, jintArray srcD, jintArray srcS, jintArray logical) {
    return IndexOps::find(env, thisObj, srcV, srcD, srcS, logical);
}

JNIEXPORT jobject JNICALL Java_shared_array_jni_NativeArrayKernel_insertSparse(JNIEnv *env, jobject thisObj, //
        jobject oldV, jintArray oldD, jintArray oldS, jintArray oldDO, jintArray oldI, //
        jobject newV, jintArray newLI) {
    return SparseOps::insert(env, thisObj, oldV, oldD, oldS, oldDO, oldI, newV, newLI);
}

JNIEXPORT jobject JNICALL Java_shared_array_jni_NativeArrayKernel_sliceSparse(JNIEnv *env, jobject thisObj, //
        jintArray slices, //
        jobject srcV, jintArray srcD, jintArray srcS, jintArray srcDO, //
        jintArray srcI, jintArray srcIO, jintArray srcII, //
        jobject dstV, jintArray dstD, jintArray dstS, jintArray dstDO, //
        jintArray dstI, jintArray dstIO, jintArray dstII) {
    return SparseOps::slice(env, thisObj, slices, //
            srcV, srcD, srcS, srcDO, //
            srcI, srcIO, srcII, //
            dstV, dstD, dstS, dstDO, //
            dstI, dstIO, dstII);
}
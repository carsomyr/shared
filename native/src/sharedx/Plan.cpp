/*
 * Copyright (c) 2006 The Regents of the University of California
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

#include <Plan.hpp>

static jclass planClass = NULL;

static jfieldID typeFieldId;
static jfieldID dimsFieldId;
static jfieldID memFieldId;

void Plan::init(JNIEnv *env) {

    planClass = (jclass) Common::newWeakGlobalRef(env, Common::findClass(env, "org/sharedx/fftw/Plan"));
    typeFieldId = Common::getFieldId(env, planClass, "type", "I");
    dimsFieldId = Common::getFieldId(env, planClass, "dims", "[I");
    memFieldId = Common::getFieldId(env, planClass, "memory", "[B");
}

void Plan::destroy(JNIEnv *env) {
    Common::deleteWeakGlobalRef(env, planClass);
}

void Plan::transform(JNIEnv *env, jobject thisObj, jdoubleArray in, jdoubleArray out) {

    try {

        if (!in || !out) {
            throw std::runtime_error("Invalid arguments");
        }

        jint type = env->GetIntField(thisObj, typeFieldId);
        jintArray dims = (jintArray) env->GetObjectField(thisObj, dimsFieldId);
        jbyteArray mem = (jbyteArray) env->GetObjectField(thisObj, memFieldId);

        if (!mem) {
            throw std::runtime_error("The byte array reference was not properly initialized");
        }

        jint nDims = env->GetArrayLength(dims);
        jint inLen = env->GetArrayLength(in);
        jint outLen = env->GetArrayLength(out);

        //
        //
        // Initialize pinned arrays.

        ArrayPinHandler dimsH(env, dims, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        ArrayPinHandler inH(env, in, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        ArrayPinHandler outH(env, out, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_WRITE);
        ArrayPinHandler memH(env, mem, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        // NO JNI AFTER THIS POINT!

        jint *dimsArr = (jint *) dimsH.get();
        jdouble *inArr = (jdouble *) inH.get();
        jdouble *outArr = (jdouble *) outH.get();
        fftw_plan *memArr = (fftw_plan *) memH.get();

        //
        //
        // Set up and execute!

        jint inLenExpected, outLenExpected;
        jdouble scalingFactor;

        Plan::getTransformParameters(inLenExpected, outLenExpected, scalingFactor, type, dimsArr, nDims);

        if (inLen != inLenExpected || outLen != outLenExpected) {
            throw std::runtime_error("Input and/or output arrays do not have expected sizes");
        }

        // Execution of a plan via the guru interface is thread-safe, so one need not acquire any monitors.
        Plan::executePlan(type, *memArr, inArr, inLen, outArr, outLen, scalingFactor);

    } catch (std::exception &e) {

        Common::throwNew(env, e);
    }
}

jbyteArray Plan::create(JNIEnv *env, jobject thisObj, jint type, jintArray dims, jint logicalMode) {

    jbyteArray mem = NULL;

    try {

        if (!dims) {
            throw std::runtime_error("Invalid arguments");
        }

        jint nDims = env->GetArrayLength(dims);

        mem = Common::newByteArray(env, sizeof(fftw_plan));

        // Acquire the class monitor before pinning/creating.
        MonitorHandler monitorH(env, (jobject) planClass);

        ArrayPinHandler dimsH(env, dims, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_ONLY);
        ArrayPinHandler memH(env, mem, ArrayPinHandler::PRIMITIVE, ArrayPinHandler::READ_WRITE);
        // NO JNI AFTER THIS POINT!

        jint *dimsArr = (jint *) dimsH.get();
        fftw_plan *memArr = (fftw_plan *) memH.get();

        // Attempt to create a plan.

        jint inLen, outLen;
        jdouble scalingFactor;

        Plan::getTransformParameters(inLen, outLen, scalingFactor, type, dimsArr, nDims);

        // Plan creation is NOT thread-safe.
        *memArr = Plan::createPlan(type, dimsArr, nDims, logicalMode, inLen, outLen);

    } catch (std::exception &e) {

        Common::throwNew(env, e);
    }

    return mem;
}

void Plan::destroy(JNIEnv *env, jobject thisObj) {

    jbyteArray mem = (jbyteArray) env->GetObjectField(thisObj, memFieldId);

    // Null field value; perhaps the constructor didn't finish. Return immediately because there is nothing to clean up.
    if (!mem) {
        return;
    }

    try {

        // Acquire the class monitor to safely perform the destruction operation.
        MonitorHandler monitorH(env, (jobject) planClass);

        ArrayPinHandler memH(env, mem, ArrayPinHandler::BYTE, ArrayPinHandler::READ_WRITE);
        // NO JNI AFTER THIS POINT!

        fftw_plan *memArr = (fftw_plan *) memH.get();

        // Check if the plan pointer is still there.
        if (!*memArr) {
            return;
        }

        // Plan destruction is NOT thread-safe.
        fftw_destroy_plan(*memArr);
        *memArr = NULL;

    } catch (...) {

        // Do nothing.
    }
}

jstring Plan::exportWisdom(JNIEnv *env) {

    jstring res = NULL;

    char *str = fftw_export_wisdom_to_string();

    try {

        if (!str) {
            throw std::runtime_error("Failed to export wisdom");
        }

        MonitorHandler monitorH(env, (jobject) planClass);

        res = Common::newStringUtf(env, str);

    } catch (std::exception &e) {

        Common::throwNew(env, e);
    }

    if (str) {
        fftw_free(str);
    }

    return res;
}

void Plan::importWisdom(JNIEnv *env, jstring wisdom) {

    try {

        if (!wisdom) {
            throw std::runtime_error("Invalid arguments");
        }

        MonitorHandler monitorH(env, (jobject) planClass);

        ArrayPinHandler wisdomH(env, (jarray) wisdom, ArrayPinHandler::STRING_UTF, ArrayPinHandler::READ_ONLY);
        // NO JNI AFTER THIS POINT!

        char *wisdomArr = (char *) wisdomH.get();

        if (!fftw_import_wisdom_from_string(wisdomArr)) {
            throw std::runtime_error("Failed to import wisdom");
        }

    } catch (std::exception &e) {

        Common::throwNew(env, e);
    }
}

inline void Plan::getTransformParameters( //
        jint &inLen, jint &outLen, //
        jdouble &scalingFactor, jint type, const jint *dimsArr, jint nDims) {

    if (nDims == 0) {
        throw std::runtime_error("Rank must be greater than zero");
    }

    for (jint dim = 0; dim < nDims; dim++) {

        if (dimsArr[dim] <= 0) {
            throw std::runtime_error("Invalid dimensions");
        }
    }

    switch (type) {

    case org_sharedx_fftw_Plan_R_TO_C:

    {
        jint acc1 = dimsArr[nDims - 1];
        jint acc2 = 2 * ((dimsArr[nDims - 1] / 2) + 1);

        for (jint i = 0, n = nDims - 1; i < n; i++) {

            acc1 *= dimsArr[i];
            acc2 *= dimsArr[i];
        }

        inLen = acc1;
        outLen = acc2;
        scalingFactor = 1.0;
    }

        break;

    case org_sharedx_fftw_Plan_C_TO_R:

    {
        jint acc1 = 2 * ((dimsArr[nDims - 1] / 2) + 1);
        jint acc2 = dimsArr[nDims - 1];

        for (jint i = 0, n = nDims - 1; i < n; i++) {

            acc1 *= dimsArr[i];
            acc2 *= dimsArr[i];
        }

        inLen = acc1;
        outLen = acc2;
        scalingFactor = 1.0 / outLen;
    }

        break;

    case org_sharedx_fftw_Plan_FORWARD:

    {
        jint acc1 = 2 * dimsArr[nDims - 1];

        for (jint i = 0, n = nDims - 1; i < n; i++) {
            acc1 *= dimsArr[i];
        }

        inLen = acc1;
        outLen = acc1;
        scalingFactor = 1.0;
    }

        break;

    case org_sharedx_fftw_Plan_BACKWARD:

    {
        jint acc1 = 2 * dimsArr[nDims - 1];

        for (jint i = 0, n = nDims - 1; i < n; i++) {
            acc1 *= dimsArr[i];
        }

        inLen = acc1;
        outLen = acc1;
        scalingFactor = 2.0 / outLen;
    }

        break;

    default:
        throw std::runtime_error("Transform type not recognized");
    }
}

inline fftw_plan Plan::createPlan( //
        jint type, const jint *dimsArr, jint nDims, //
        jint logicalMode, jint inLen, jint outLen) {

    fftw_plan plan = NULL;

    MallocHandler mallocH(sizeof(jdouble) * (inLen + outLen));
    jdouble *all = (jdouble *) mallocH.get();

    jdouble *inArr = all;
    jdouble *outArr = all + inLen;

    jint mode;

    // Convert a Java logical constant into a FFTW constant.
    switch (logicalMode) {

    case org_sharedx_fftw_Plan_FFTW_ESTIMATE:
        mode = FFTW_ESTIMATE;
        break;

    case org_sharedx_fftw_Plan_FFTW_MEASURE:
        mode = FFTW_MEASURE;
        break;

    case org_sharedx_fftw_Plan_FFTW_PATIENT:
        mode = FFTW_PATIENT;
        break;

    case org_sharedx_fftw_Plan_FFTW_EXHAUSTIVE:
        mode = FFTW_EXHAUSTIVE;
        break;

    default:
        throw std::runtime_error("Plan type not recognized");
    }

    switch (type) {

    case org_sharedx_fftw_Plan_R_TO_C:
        plan = fftw_plan_dft_r2c(nDims, (const int *) dimsArr, //
                inArr, (fftw_complex *) outArr, mode | FFTW_PRESERVE_INPUT | FFTW_UNALIGNED);
        break;

    case org_sharedx_fftw_Plan_C_TO_R:
        // NOTE: Complex-to-real transforms may destroy their input.
        plan = fftw_plan_dft_c2r(nDims, (const int *) dimsArr, //
                (fftw_complex *) inArr, outArr, mode | FFTW_DESTROY_INPUT | FFTW_UNALIGNED);
        break;

    case org_sharedx_fftw_Plan_FORWARD:
        plan = fftw_plan_dft(nDims, (const int *) dimsArr, //
                (fftw_complex *) inArr, (fftw_complex *) outArr, //
                FFTW_FORWARD, mode | FFTW_PRESERVE_INPUT | FFTW_UNALIGNED);
        break;

    case org_sharedx_fftw_Plan_BACKWARD:
        plan = fftw_plan_dft(nDims, (const int *) dimsArr, //
                (fftw_complex *) inArr, (fftw_complex *) outArr, //
                FFTW_BACKWARD, mode | FFTW_PRESERVE_INPUT | FFTW_UNALIGNED);
        break;

    default:
        throw std::runtime_error("Plan direction not recognized");
    }

    if (!plan) {
        throw std::runtime_error("Plan creation failed");
    }

    return plan;
}

inline void Plan::executePlan( //
        jint type, fftw_plan plan, //
        jdouble *inArr, jint inLen, //
        jdouble *outArr, jint outLen, //
        jdouble scalingFactor) {

    switch (type) {

    case org_sharedx_fftw_Plan_R_TO_C:
        fftw_execute_dft_r2c(plan, inArr, (fftw_complex *) outArr);
        break;

    case org_sharedx_fftw_Plan_C_TO_R:
        executePlanCToR(plan, inArr, outArr, inLen);
        break;

    case org_sharedx_fftw_Plan_FORWARD:
    case org_sharedx_fftw_Plan_BACKWARD:
        fftw_execute_dft(plan, (fftw_complex *) inArr, (fftw_complex *) outArr);
        break;

    default:
        throw std::runtime_error("Plan type not recognized");
    }

    if (scalingFactor != 1.0) {

        for (jint i = 0; i < outLen; i++) {
            outArr[i] *= scalingFactor;
        }
    }
}

void Plan::executePlanCToR( //
        fftw_plan plan, jdouble *inArr, jdouble *outArr, jint inLen) {

    MallocHandler mallocH(sizeof(jdouble) * inLen);
    fftw_complex *tmpArr = (fftw_complex *) mallocH.get();

    memcpy(tmpArr, inArr, sizeof(jdouble) * inLen);

    fftw_execute_dft_c2r(plan, tmpArr, outArr);
}

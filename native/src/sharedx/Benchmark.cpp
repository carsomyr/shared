/*
 * Copyright (C) 2008 The Regents of the University of California
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

#include <Benchmark.hpp>

void Benchmark::testConvolve(JNIEnv *env, jobject thisObj) {

    jcomplex *out1 = NULL;
    jcomplex *out2 = NULL;

    try {

        jint mode;

        const jint benchmarkMode = sharedx_test_BenchmarkSpecification_MODE;

        switch (benchmarkMode) {

        case sharedx_fftw_Plan_FFTW_ESTIMATE:
            mode = FFTW_ESTIMATE;
            break;

        case sharedx_fftw_Plan_FFTW_MEASURE:
            mode = FFTW_MEASURE;
            break;

        case sharedx_fftw_Plan_FFTW_PATIENT:
            mode = FFTW_PATIENT;
            break;

        case sharedx_fftw_Plan_FFTW_EXHAUSTIVE:
            mode = FFTW_EXHAUSTIVE;
            break;

        default:
            throw std::runtime_error("Plan type not recognized");
        }

        jint size = sharedx_test_BenchmarkSpecification_SIZE;
        jint nreps = sharedx_test_BenchmarkSpecification_NREPS;

        const int dims[2] = { size, size };
        jint len = size * size;

        if (sizeof(fftw_complex) != sizeof(jcomplex)) {
            throw std::runtime_error("The structs fftw_complex and jcomplex are not interchangeable");
        }

        MallocHandler mallocH(sizeof(jcomplex) * 2 * len);

        jcomplex *all = (jcomplex *) mallocH.get();
        memset(all, 0, sizeof(jcomplex) * 2 * len);

        jcomplex *in = all;
        jcomplex *ker = all + len;

        // Acquire the class monitor just to be safe.
        MonitorHandler monitorH(env, (jobject) env->FindClass("sharedx/fftw/Plan"));

        fftw_plan forward = fftw_plan_dft(2, dims, //
                (fftw_complex *) in, (fftw_complex *) ker, //
                FFTW_FORWARD, mode | FFTW_PRESERVE_INPUT | FFTW_UNALIGNED);

        fftw_plan backward = fftw_plan_dft(2, dims, //
                (fftw_complex *) in, (fftw_complex *) ker, //
                FFTW_BACKWARD, mode | FFTW_PRESERVE_INPUT | FFTW_UNALIGNED);

        if (forward && backward) {

            for (jint i = 0; i < nreps; i++) {

                out1 = new jcomplex[len];
                out2 = new jcomplex[len];

                // Elementwise multiplication of transforms is equivalent to convolution of originals.
                for (jint i = 0; i < len; i++) {
                    out1[i] = in[i] * ker[i];
                }

                // Simulate the IFFT of intertwined transforms.
                fftw_execute_dft(backward, (fftw_complex *) out1, (fftw_complex *) out2);

                // Simulate rescaling actions.
                jcomplex scalingFactor = jcomplex(1.0 / len, 0.0);

                for (jint i = 0; i < len; i++) {
                    out2[i] *= scalingFactor;
                }

                delete[] out1;
                out1 = NULL;

                delete[] out2;
                out2 = NULL;
            }
        }

        if (forward) {
            fftw_destroy_plan(forward);
        }

        if (backward) {
            fftw_destroy_plan(backward);
        }

    } catch (std::exception &e) {

        if (out1) {
            delete[] out1;
        }

        if (out2) {
            delete[] out2;
        }

        Common::throwNew(env, e);
    }
}

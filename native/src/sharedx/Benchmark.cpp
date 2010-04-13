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

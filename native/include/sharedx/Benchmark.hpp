/*
 * This file is part of the Shared Scientific Toolbox in Java ("this library").
 * 
 * Copyright (C) 2008 Roy Liu, The Regents of the University of California
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

#include <Common.hpp>
#include <ElementOps.hpp>

#include <JNIHeadersXWrap.hpp>

#include <fftw3.h>

#ifndef _Included_Benchmark
#define _Included_Benchmark

/**
 * A class of native benchmarks.
 */
class Benchmark {

public:

    /**
     * Benchmarks repeated convolutions.
     * 
     * @param env
     *      the JNI environment.
     * @param thisObj
     *      this object.
     */
    static void testConvolve(JNIEnv *env, jobject thisObj);
};

#endif
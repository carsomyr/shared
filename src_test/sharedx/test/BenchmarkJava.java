/**
 * <p>
 * Copyright (c) 2008 The Regents of the University of California<br>
 * All rights reserved.
 * </p>
 * <p>
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * </p>
 * <ul>
 * <li>Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.</li>
 * <li>Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution.</li>
 * <li>Neither the name of the author nor the names of any contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.</li>
 * </ul>
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * </p>
 */

package sharedx.test;

import static shared.array.ArrayBase.FFTService;

import org.junit.BeforeClass;
import org.junit.Test;

import shared.array.ComplexArray;
import shared.fft.ConvolutionCache;
import sharedx.fftw.Plan;

/**
 * A collection of Java performance benchmarks.
 * 
 * @author Roy Liu
 */
public class BenchmarkJava implements BenchmarkSpecification {

    /**
     * Default constructor.
     */
    public BenchmarkJava() {
    }

    /**
     * Induces <a href="http://www.fftw.org/">FFTW3</a> precomputation so that results aren't biased.
     */
    @BeforeClass
    final public static void initClass() {

        final String modeStr;

        switch (MODE) {

        case Plan.FFTW_ESTIMATE:
            modeStr = "estimate";
            break;

        case Plan.FFTW_MEASURE:
            modeStr = "measure";
            break;

        case Plan.FFTW_PATIENT:
            modeStr = "patient";
            break;

        case Plan.FFTW_EXHAUSTIVE:
            modeStr = "exhaustive";
            break;

        default:
            throw new AssertionError();
        }

        FFTService.setHint("mode", modeStr);

        ComplexArray tmp = new ComplexArray(SIZE, SIZE, 2);

        for (int i = 0; i < 64; i++) {
            tmp.fft().ifft();
        }
    }

    @Test
    @Override
    public void testConvolve() {

        ComplexArray kernel = new ComplexArray(SIZE, SIZE, 2);
        ComplexArray im = new ComplexArray(SIZE, SIZE, 2);

        ConvolutionCache cc = ConvolutionCache.getInstance();

        for (int i = 0; i < N_REPS; i++) {
            im.eMul(cc.get(kernel, im.dims())).ifft();
        }
    }
}

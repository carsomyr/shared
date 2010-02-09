/**
 * This file is part of the Shared Scientific Toolbox in Java ("this library"). <br />
 * <br />
 * Copyright (C) 2008 Roy Liu, The Regents of the University of California <br />
 * <br />
 * This library is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License, version 2, as published by the Free Software Foundation. <br />
 * <br />
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. <br />
 * <br />
 * You should have received a copy of the GNU General Public License along with this library. If not, see <a
 * href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
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
    public void testConvolve() {

        ComplexArray kernel = new ComplexArray(SIZE, SIZE, 2);
        ComplexArray im = new ComplexArray(SIZE, SIZE, 2);

        ConvolutionCache cc = ConvolutionCache.getInstance();

        for (int i = 0; i < NREPS; i++) {
            im.eMul(cc.get(kernel, im.dims())).ifft();
        }
    }
}

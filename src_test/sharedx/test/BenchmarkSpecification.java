/**
 * This file is part of the Shared Scientific Toolbox in Java ("this library"). <br />
 * <br />
 * Copyright (C) 2008 The Regents of the University of California <br />
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

import sharedx.fftw.Plan;

/**
 * A specification ascribed to by Java and native performance benchmarks.
 * 
 * @author Roy Liu
 */
public interface BenchmarkSpecification {

    /**
     * The image size.
     */
    final public static int SIZE = 256;

    /**
     * The number of repetitions.
     */
    final public static int NREPS = 2048;

    /**
     * The desired level of <a href="http://www.fftw.org/">FFTW3</a> precomputation.
     */
    final public static int MODE = Plan.FFTW_MEASURE;

    /**
     * Benchmarks convolution capabilities.
     */
    public void testConvolve();
}

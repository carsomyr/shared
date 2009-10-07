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

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * A collection of native performance benchmarks.
 * 
 * @author Roy Liu
 */
public class BenchmarkNative implements BenchmarkSpecification {

    /**
     * Default constructor.
     */
    public BenchmarkNative() {
    }

    /**
     * Delegates to {@link BenchmarkJava#initClass()}.
     */
    @BeforeClass
    final public static void initClass() {
        BenchmarkJava.initClass();
    }

    @Test
    final public native void testConvolve();
}

/**
 * This file is part of the Shared Scientific Toolbox in Java ("this library"). <br />
 * <br />
 * Copyright (C) 2007 The Regents of the University of California <br />
 * <br />
 * This library is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation, either version 2.1 of the License, or (at your option)
 * any later version. <br />
 * <br />
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details. <br />
 * <br />
 * You should have received a copy of the GNU Lesser General Public License along with this library. If not, see <a
 * href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 */

package shared.test.fft;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * A suite encompassing all FFT tests.
 * 
 * @apiviz.owns shared.test.fft.ArrayFFTTest
 * @apiviz.owns shared.test.fft.ConvolutionCacheTest
 * @author Roy Liu
 */
@RunWith(Suite.class)
@SuiteClasses(value = {
//
        ArrayFFTTest.class, //
        ConvolutionCacheTest.class //
})
public class AllFFTTests {

    // Dummy constructor.
    AllFFTTests() {
    }
}

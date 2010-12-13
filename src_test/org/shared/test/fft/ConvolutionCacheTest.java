/**
 * <p>
 * Copyright (c) 2007 The Regents of the University of California<br>
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

package org.shared.test.fft;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.shared.array.RealArray;
import org.shared.fft.ConvolutionCache;
import org.shared.test.Tests;

/**
 * A class of unit tests for {@link ConvolutionCache}.
 * 
 * @author Roy Liu
 */
public class ConvolutionCacheTest {

    /**
     * Default constructor.
     */
    public ConvolutionCacheTest() {
    }

    /**
     * Tests {@link ConvolutionCache#convolve(org.shared.array.ComplexArray, RealArray)}.
     */
    @Test
    public void testConvolve() {

        RealArray a = new RealArray(new double[] {
                //
                0, 1, 4, 9, 16, 25, //
                9, 16, 25, 36, 49, 64, //
                36, 49, 64, 0, 1, 4, //
                0, 1, 4, 9, 16, 25, //
                9, 16, 25, 36, 49, 64, //
                36, 49, 64, 0, 1, 4 //
                }, //
                6, 6 //
        );

        RealArray b = new RealArray(new double[] {
                //
                1, -2, 1, //
                -2, 4, -2, //
                1, -2, 1 //
                }, //
                3, 3 //
        );

        RealArray expected = new RealArray(new double[] {
                //
                0, -81, 63, 0, //
                0, 162, -126, 0, //
                0, -81, 63, 0, //
                0, -81, 63, 0 //
                }, //
                4, 4 //
        );

        Assert.assertTrue(Tests.equals( //
                ConvolutionCache.getInstance().convolve(a.rfft(), b).values(), expected.values()));
    }

    /**
     * Tests {@link ConvolutionCache#pad(RealArray, int...)}.
     */
    @Test
    public void testPad() {

        RealArray a = new RealArray(new double[] {
                //
                0, 1, 2, //
                3, 4, 5, //
                6, 7, 8, //
                //
                9, 10, 11, //
                12, 13, 14, //
                15, 16, 17, //
                //
                18, 19, 20, //
                21, 22, 23, //
                24, 25, 26 //
                }, //
                3, 3, 3 //
        );

        RealArray expected = new RealArray(new double[] {
                //
                0, 0, 1, 2, 2, //
                0, 0, 1, 2, 2, //
                3, 3, 4, 5, 5, //
                6, 6, 7, 8, 8, //
                6, 6, 7, 8, 8, //
                //
                0, 0, 1, 2, 2, //
                0, 0, 1, 2, 2, //
                3, 3, 4, 5, 5, //
                6, 6, 7, 8, 8, //
                6, 6, 7, 8, 8, //
                //
                9, 9, 10, 11, 11, //
                9, 9, 10, 11, 11, //
                12, 12, 13, 14, 14, //
                15, 15, 16, 17, 17, //
                15, 15, 16, 17, 17, //
                //
                18, 18, 19, 20, 20, //
                18, 18, 19, 20, 20, //
                21, 21, 22, 23, 23, //
                24, 24, 25, 26, 26, //
                24, 24, 25, 26, 26, //
                //
                18, 18, 19, 20, 20, //
                18, 18, 19, 20, 20, //
                21, 21, 22, 23, 23, //
                24, 24, 25, 26, 26, //
                24, 24, 25, 26, 26 //
                }, //
                5, 5, 5 //
        );

        Assert.assertTrue(Arrays.equals(ConvolutionCache.pad(a, 1, 1, 1).values(), expected.values()));

        a = new RealArray(new double[] {
                //
                0, 1, 2, 3, 4, //
                5, 6, 7, 8, 9, //
                10, 11, 12, 13, 14 //
                }, //
                3, 5 //
        );

        expected = new RealArray(new double[] {
                //
                5, 5, 6, 7, 8, 9, 9, //
                0, 0, 1, 2, 3, 4, 4, //
                0, 0, 1, 2, 3, 4, 4, //
                5, 5, 6, 7, 8, 9, 9, //
                10, 10, 11, 12, 13, 14, 14, //
                10, 10, 11, 12, 13, 14, 14, //
                5, 5, 6, 7, 8, 9, 9 //
                }, //
                7, 7 //
        );

        Assert.assertTrue(Arrays.equals(ConvolutionCache.pad(a, 2, 1).values(), expected.values()));
    }
}

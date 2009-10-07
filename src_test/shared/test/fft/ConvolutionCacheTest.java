/**
 * This file is part of the Shared Scientific Toolbox in Java ("this library"). <br />
 * <br />
 * Copyright (C) 2007 Roy Liu, The Regents of the University of California <br />
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

import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import shared.array.RealArray;
import shared.fft.ConvolutionCache;
import shared.test.Tests;

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
     * Tests {@link ConvolutionCache#convolve(shared.array.ComplexArray, RealArray)}.
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

        assertTrue(Tests.equals( //
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

        assertTrue(Arrays.equals(ConvolutionCache.pad(a, 1, 1, 1).values(), expected.values()));

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

        assertTrue(Arrays.equals(ConvolutionCache.pad(a, 2, 1).values(), expected.values()));
    }
}

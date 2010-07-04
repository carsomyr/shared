/**
 * <p>
 * Copyright (C) 2007 Roy Liu<br />
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

package shared.test.array;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Test;

import shared.array.Array;
import shared.array.Array.IndexingOrder;
import shared.array.ComplexArray;
import shared.array.IntegerArray;
import shared.array.RealArray;
import shared.array.kernel.ArrayIOKernel;
import shared.array.kernel.MatlabIOKernel;
import shared.test.Tests;
import shared.util.Control;

/**
 * A class of unit tests for {@link ArrayIOKernel} and, more specifically, {@link MatlabIOKernel}.
 * 
 * @author Roy Liu
 */
public class ArrayIOKernelTest {

    /**
     * Default constructor.
     */
    public ArrayIOKernelTest() {
    }

    /**
     * Tests {@link RealArray} reading from and writing to "mat" files.
     * 
     * @throws IOException
     *             when something goes awry.
     */
    @Test
    public void testRealArrayReadWrite() throws IOException {

        MatlabIOKernel kernel = new MatlabIOKernel();

        RealArray expected = new RealArray(new double[] {
                //
                0, 1, 2, 3, 4, //
                5, 6, 7, 8, 9, //
                10, 11, 12, 13, 14, //
                15, 16, 17, 18, 19, //
                //
                20, 21, 22, 23, 24, //
                25, 26, 27, 28, 29, //
                30, 31, 32, 33, 34, //
                35, 36, 37, 38, 39 //
                }, //
                IndexingOrder.FAR, //
                2, 4, 5 //
        );

        assertTrue(Arrays.equals(expected.values(), //
                ((RealArray) kernel.parseMat( //
                        kernel.getMatBytes(expected))[0]).values()));

        assertTrue(Arrays.equals(expected.values(), //
                RealArray.parse(expected.getBytes()).values()));

        expected = new RealArray(new double[] {
                //
                0, 1, 2, 3, 4, //
                5, 6, 7, 8, 9, //
                10, 11, 12, 13, 14, //
                15, 16, 17, 18, 19, //
                //
                20, 21, 22, 23, 24, //
                25, 26, 27, 28, 29, //
                30, 31, 32, 33, 34, //
                35, 36, 37, 38, 39, //
                //
                40, 41, 42, 43, 44, //
                45, 46, 47, 48, 49, //
                50, 51, 52, 53, 54, //
                55, 56, 57, 58, 59 //
                }, //
                IndexingOrder.FAR, //
                3, 4, 5 //
        );

        Array<?, ?>[] arrays = kernel.parseMat(Control.getBytes(Thread.currentThread().getContextClassLoader() //
                .getResourceAsStream("shared/test/array/r.mat")));

        assertTrue(Arrays.equals(expected.values(), ((RealArray) arrays[0]).values()));
        assertTrue(Tests.equals(expected.uMul(1 << 24).uAdd(0.5).values(), ((RealArray) arrays[1]).values()));
    }

    /**
     * Tests {@link ComplexArray} reading from and writing to "mat" files.
     * 
     * @throws IOException
     *             when something goes awry.
     */
    @Test
    public void testComplexArrayReadWrite() throws IOException {

        MatlabIOKernel kernel = new MatlabIOKernel();

        ComplexArray expected = new ComplexArray(new double[] {
                //
                1, 1, 2, 2, 3, 3, //
                4, 4, 5, 5, 6, 6, //
                7, 7, 8, 8, 9, 9, //
                10, 10, 11, 11, 12, 12, //
                13, 13, 14, 14, 15, 15 //
                }, //
                5, 3, 2 //
        );

        assertTrue(Arrays.equals(expected.values(), //
                ((ComplexArray) kernel.parseMat( //
                        kernel.getMatBytes(expected))[0]).values()));

        assertTrue(Arrays.equals(expected.values(), //
                ComplexArray.parse(expected.getBytes()).values()));

        expected = new ComplexArray(new double[] {
                //
                0, 1, 1, 2, 2, 3, 3, 4, 4, 5, //
                5, 6, 6, 7, 7, 8, 8, 9, 9, 10, //
                10, 11, 11, 12, 12, 13, 13, 14, 14, 15, //
                15, 16, 16, 17, 17, 18, 18, 19, 19, 20, //
                //
                20, 21, 21, 22, 22, 23, 23, 24, 24, 25, //
                25, 26, 26, 27, 27, 28, 28, 29, 29, 30, //
                30, 31, 31, 32, 32, 33, 33, 34, 34, 35, //
                35, 36, 36, 37, 37, 38, 38, 39, 39, 40, //
                //
                40, 41, 41, 42, 42, 43, 43, 44, 44, 45, //
                45, 46, 46, 47, 47, 48, 48, 49, 49, 50, //
                50, 51, 51, 52, 52, 53, 53, 54, 54, 55, //
                55, 56, 56, 57, 57, 58, 58, 59, 59, 60 //
                }, //
                3, 4, 5, 2 //
        );

        Array<?, ?>[] arrays = kernel.parseMat(Control.getBytes(Thread.currentThread().getContextClassLoader() //
                .getResourceAsStream("shared/test/array/c.mat")));

        assertTrue(Arrays.equals(expected.values(), ((ComplexArray) arrays[0]).values()));
        assertTrue(Tests.equals(expected.uMul(1 << 24, 0).uAdd(0.5, 0.0).values(), //
                ((ComplexArray) arrays[1]).values()));
    }

    /**
     * Tests {@link IntegerArray} reading from and writing to "mat" files.
     * 
     * @throws IOException
     *             when something goes awry.
     */
    @Test
    public void testIntegerArrayReadWrite() throws IOException {

        MatlabIOKernel kernel = new MatlabIOKernel();

        IntegerArray expected = new IntegerArray(new int[] {
                //
                0, 1, 2, 3, //
                4, 5, 6, 7, //
                8, 9, 10, 11, //
                //
                12, 13, 14, 15, //
                16, 17, 18, 19, //
                20, 21, 22, 23, //
                //
                24, 25, 26, 27, //
                28, 29, 30, 31, //
                32, 33, 34, 35 //
                }, //
                IndexingOrder.FAR, //
                3, 3, 4 //
        );

        assertTrue(Arrays.equals(expected.values(), //
                ((IntegerArray) kernel.parseMat( //
                        kernel.getMatBytes(expected))[0]).values()));

        assertTrue(Arrays.equals(expected.values(), //
                IntegerArray.parse(expected.getBytes()).values()));

        expected = new IntegerArray(new int[] {
                //
                0, 1, 2, 3, 4, //
                5, 6, 7, 8, 9, //
                10, 11, 12, 13, 14, //
                15, 16, 17, 18, 19, //
                //
                20, 21, 22, 23, 24, //
                25, 26, 27, 28, 29, //
                30, 31, 32, 33, 34, //
                35, 36, 37, 38, 39, //
                //
                40, 41, 42, 43, 44, //
                45, 46, 47, 48, 49, //
                50, 51, 52, 53, 54, //
                55, 56, 57, 58, 59 //
                }, //
                IndexingOrder.FAR, //
                3, 4, 5 //
        );

        Array<?, ?>[] arrays = kernel.parseMat(Control.getBytes(Thread.currentThread().getContextClassLoader() //
                .getResourceAsStream("shared/test/array/i.mat")));

        assertTrue(Arrays.equals(expected.values(), ((IntegerArray) arrays[0]).values()));
        assertTrue(Arrays.equals(expected.uMul(1 << 24).values(), ((IntegerArray) arrays[1]).values()));
    }
}

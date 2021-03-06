/**
 * <p>
 * Copyright (c) 2007 Roy Liu<br>
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

package org.shared.test.array;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.shared.array.AbstractRealArray;
import org.shared.array.AbstractRealArray.RealMap;
import org.shared.array.AbstractRealArray.RealReduce;
import org.shared.array.Array;
import org.shared.array.Array.IndexingOrder;
import org.shared.array.IntegerArray;
import org.shared.array.RealArray;
import org.shared.test.Tests;
import org.shared.util.Arithmetic;

/**
 * A class of unit tests for {@link RealArray}.
 * 
 * @author Roy Liu
 */
public class RealArrayTest {

    /**
     * Default constructor.
     */
    public RealArrayTest() {
    }

    /**
     * Tests {@link Array#map(Array, int...)}.
     */
    @Test
    public void testMap() {

        RealArray a = new RealArray(new double[] {
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

        RealArray expected = a.map( //
                new RealArray(IndexingOrder.NEAR, 3, 3, 4), //
                1, 0, 2, //
                0, 1, 2, //
                1, 0, 3);

        a = a.map(new RealArray(IndexingOrder.FAR, 3, 3, 4), //
                1, 0, 2, 0, 1, 2, 1, 0, 3);

        Assert.assertTrue(Arrays.equals(a.reverseOrder().values(), expected.values()));
    }

    /**
     * Tests {@link Array#splice(Array, int...)}.
     */
    @Test
    public void testSlice() {

        RealArray original = new RealArray(new double[] {
                //
                0, 1, 2, 3, 4, 5, 6, //
                7, 8, 9, 10, 11, 12, 13, //
                14, 15, 16, 17, 18, 19, 20, //
                21, 22, 23, 24, 25, 26, 27, //
                28, 29, 30, 31, 32, 33, 34, //
                35, 36, 37, 38, 39, 40, 41, //
                42, 43, 44, 45, 46, 47, 48 //
                }, //
                IndexingOrder.FAR, //
                7, 7 //
        );

        RealArray a = original.splice(new RealArray(IndexingOrder.FAR, 5, 5), //
                //
                1, 0, 0, //
                3, 2, 0, //
                5, 4, 0, //
                //
                0, 0, 1, //
                2, 1, 1, //
                4, 3, 1, //
                6, 4, 1);

        RealArray expected = new RealArray(new double[] {
                //
                7, 9, 0, 11, 13, //
                0, 0, 0, 0, 0, //
                21, 23, 0, 25, 27, //
                0, 0, 0, 0, 0, //
                35, 37, 0, 39, 41 //
                }, //
                IndexingOrder.FAR, //
                5, 5 //
        );

        Assert.assertTrue(Arrays.equals(a.values(), expected.values()));

        a = original.slice( //
                new int[][] {
                        //
                        new int[] { 1, 3, 5 }, //
                        new int[] { 0, 2, 4, 6 } //

                }, //
                new RealArray(IndexingOrder.FAR, 5, 5), //
                //
                new int[][] {
                        //
                        new int[] { 0, 2, 4 }, //
                        new int[] { 0, 1, 3, 4 } //

                });

        Assert.assertTrue(Arrays.equals(a.values(), expected.values()));

        a = new RealArray(new double[] {
                //
                1, 2, 3, //
                4, 5, 6, //
                7, 8, 9 //
                }, //
                IndexingOrder.FAR, //
                3, 3 //
        );

        expected = new RealArray(new double[] {
                //
                1, 0, 2, 0, 3, //
                0, 0, 0, 0, 0, //
                4, 0, 5, 0, 6, //
                0, 0, 0, 0, 0, //
                7, 0, 8, 0, 9 //
                }, //
                IndexingOrder.FAR, //
                5, 5 //
        );

        a = a.slice(new RealArray(IndexingOrder.FAR, 5, 5), //
                new int[] { 0, 2, 4 }, //
                new int[] { 0, 2, 4 });

        Assert.assertTrue(Arrays.equals(a.values(), expected.values()));

        a = new RealArray(new double[] {
                //
                1, 2, 3, 4, 5, //
                6, 7, 8, 9, 10, //
                11, 12, 13, 14, 15, //
                16, 17, 18, 19, 20, //
                21, 22, 23, 24, 25 //
                }, //
                IndexingOrder.FAR, //
                5, 5 //
        );

        expected = new RealArray(new double[] {
                //
                0, 2, 0, 4, 0, //
                6, 7, 8, 9, 10, //
                0, 12, 0, 14, 0, //
                16, 17, 18, 19, 20, //
                0, 22, 0, 24, 0 //
                }, //
                IndexingOrder.FAR, //
                5, 5 //
        );

        a = a.slice((double) 0, //
                new int[] { 0, 2, 4 }, //
                new int[] { 0, 2, 4 });

        Assert.assertTrue(Arrays.equals(a.values(), expected.values()));

        a = original.slice( //
                new int[] { 1, 3, 5 }, //
                new int[] { 0, 2, 4, 6 });

        expected = new RealArray(new double[] {
                //
                7, 9, 11, 13, //
                21, 23, 25, 27, //
                35, 37, 39, 41 //
                }, //
                IndexingOrder.FAR, //
                3, 4 //
        );

        Assert.assertTrue(Arrays.equals(a.values(), expected.values()));
    }

    /**
     * Tests {@link RealArray#tile(int...)}.
     */
    @Test
    public void testTile() {

        RealArray a = new RealArray(new double[] {
                //
                0, 1, //
                2, 3, //
                //
                4, 5, //
                6, 7 //
                }, //
                IndexingOrder.FAR, //
                2, 2, 2 //
        );

        RealArray expected = new RealArray(new double[] {
                //
                0, 1, 0, 1, //
                2, 3, 2, 3, //
                0, 1, 0, 1, //
                2, 3, 2, 3, //
                //
                4, 5, 4, 5, //
                6, 7, 6, 7, //
                4, 5, 4, 5, //
                6, 7, 6, 7, //
                //
                0, 1, 0, 1, //
                2, 3, 2, 3, //
                0, 1, 0, 1, //
                2, 3, 2, 3, //
                //
                4, 5, 4, 5, //
                6, 7, 6, 7, //
                4, 5, 4, 5, //
                6, 7, 6, 7 //
                }, //
                IndexingOrder.FAR, //
                4, 4, 4 //
        );

        Assert.assertTrue(Arrays.equals(a.tile(2, 2, 2).values(), expected.values()));

        //

        a = new RealArray(new double[] {
                //
                0, 1, 2, 3, 4 //
                }, //
                IndexingOrder.FAR, //
                5, 1 //
        );

        expected = new RealArray(new double[] {
                //
                0, 0, 0, 0, 0, //
                1, 1, 1, 1, 1, //
                2, 2, 2, 2, 2, //
                3, 3, 3, 3, 3, //
                4, 4, 4, 4, 4 //
                }, //
                IndexingOrder.FAR, //
                5, 5 //
        );

        Assert.assertTrue(Arrays.equals(a.tile(1, 5).values(), expected.values()));
    }

    /**
     * Tests {@link RealArray#transpose(int...)}.
     */
    @Test
    public void testTranspose() {

        RealArray a = new RealArray(new double[] {
                //
                0, 1, 2, //
                3, 4, 5, //
                6, 7, 8, //
                9, 10, 11 //
                }, //
                4, 3 //
        );

        RealArray expected = new RealArray(new double[] {
                //
                0, 3, 6, 9, //
                1, 4, 7, 10, //
                2, 5, 8, 11 //
                }, //
                3, 4 //
        );

        Assert.assertTrue(Arrays.equals(a.transpose(1, 0).values(), expected.values()));

        a = new RealArray(new double[] {
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

        expected = new RealArray(new double[] {
                //
                0, 20, 40, //
                1, 21, 41, //
                2, 22, 42, //
                3, 23, 43, //
                4, 24, 44, //
                //
                5, 25, 45, //
                6, 26, 46, //
                7, 27, 47, //
                8, 28, 48, //
                9, 29, 49, //
                //
                10, 30, 50, //
                11, 31, 51, //
                12, 32, 52, //
                13, 33, 53, //
                14, 34, 54, //
                //
                15, 35, 55, //
                16, 36, 56, //
                17, 37, 57, //
                18, 38, 58, //
                19, 39, 59 //
                }, //
                IndexingOrder.FAR, //
                5, 4, 3 //
        );

        Assert.assertTrue(Arrays.equals(a.transpose(2, 0, 1).values(), expected.values()));
    }

    /**
     * Tests {@link RealArray#shift(int...)}.
     */
    @Test
    public void testShift() {

        RealArray a = new RealArray(new double[] {
                //
                0, 1, 2, 3, 4, //
                1, 2, 3, 4, 3, //
                2, 3, 4, 3, 2, //
                3, 4, 3, 2, 1, //
                4, 3, 2, 1, 0, //
                //
                1, 2, 3, 4, 5, //
                2, 3, 4, 5, 4, //
                3, 4, 5, 4, 3, //
                4, 5, 4, 3, 2, //
                5, 4, 3, 2, 1, //
                //
                2, 3, 4, 5, 6, //
                3, 4, 5, 6, 5, //
                4, 5, 6, 5, 4, //
                5, 6, 5, 4, 3, //
                6, 5, 4, 3, 2, //
                //
                3, 4, 5, 6, 7, //
                4, 5, 6, 7, 6, //
                5, 6, 7, 6, 5, //
                6, 7, 6, 5, 4, //
                7, 6, 5, 4, 3, //
                //
                4, 5, 6, 7, 8, //
                5, 6, 7, 8, 7, //
                6, 7, 8, 7, 6, //
                7, 8, 7, 6, 5, //
                8, 7, 6, 5, 4 //
                }, //
                IndexingOrder.FAR, //
                5, 5, 5 //
        );

        RealArray expected = new RealArray(new double[] {
                //
                6, 4, 5, 6, 7, //
                5, 5, 6, 7, 6, //
                4, 6, 7, 6, 5, //
                3, 7, 6, 5, 4, //
                7, 3, 4, 5, 6, //
                //
                7, 5, 6, 7, 8, //
                6, 6, 7, 8, 7, //
                5, 7, 8, 7, 6, //
                4, 8, 7, 6, 5, //
                8, 4, 5, 6, 7, //
                //
                3, 1, 2, 3, 4, //
                2, 2, 3, 4, 3, //
                1, 3, 4, 3, 2, //
                0, 4, 3, 2, 1, //
                4, 0, 1, 2, 3, //
                //
                4, 2, 3, 4, 5, //
                3, 3, 4, 5, 4, //
                2, 4, 5, 4, 3, //
                1, 5, 4, 3, 2, //
                5, 1, 2, 3, 4, //
                //
                5, 3, 4, 5, 6, //
                4, 4, 5, 6, 5, //
                3, 5, 6, 5, 4, //
                2, 6, 5, 4, 3, //
                6, 2, 3, 4, 5 //
                }, //
                IndexingOrder.FAR, //
                5, 5, 5 //
        );

        Assert.assertTrue(Arrays.equals(a.shift(-3, -1, 1).values(), expected.values()));
    }

    /**
     * Tests {@link RealArray#subarray(int...)}.
     */
    @Test
    public void testSubarray() {

        RealArray a = new RealArray(new double[] {
                //
                0, 1, 2, 3, //
                4, 5, 6, 7, //
                8, 9, 10, 11, //
                12, 13, 14, 15, //
                //
                16, 17, 18, 19, //
                20, 21, 22, 23, //
                24, 25, 26, 27, //
                28, 29, 30, 31, //
                //
                32, 33, 34, 35, //
                36, 37, 38, 39, //
                40, 41, 42, 43, //
                44, 45, 46, 47, //
                //
                48, 49, 50, 51, //
                52, 53, 54, 55, //
                56, 57, 58, 59, //
                60, 61, 62, 63 //
                }, //
                IndexingOrder.FAR, //
                4, 4, 4 //
        );

        RealArray expected = new RealArray(new double[] {
                //
                21, 22, //
                25, 26, //
                29, 30, //
                //
                37, 38, //
                41, 42, //
                45, 46 //
                }, //
                IndexingOrder.FAR, //
                2, 3, 2 //
        );

        Assert.assertTrue(Arrays.equals(a.subarray(1, 3, 1, 4, 1, 3).values(), expected.values()));
    }

    /**
     * Tests {@link RealArray#reshape(int...)}.
     */
    @Test
    public void testReshape() {

        RealArray a = new RealArray(new double[] {
                //
                1, 2, 3, 4, 5, 6, //
                7, 8, 9, 10, 11, 12, //
                13, 14, 15, 16, 17, 18 //
                }, //
                IndexingOrder.FAR, //
                3, 6 //
        );

        RealArray expected = new RealArray(new double[] {
                //
                231, 252, 273, //
                537, 594, 651, //
                843, 936, 1029 //
                }, //
                IndexingOrder.FAR, //
                3, 3 //
        );

        Assert.assertTrue(Arrays.equals(a.mMul(a.reshape(6, 3)).values(), expected.values()));

        a = new RealArray(new double[] {
                //
                1, 2, 3, //
                4, 5, 6, //
                7, 8, 9, //
                //
                10, 11, 12, //
                13, 14, 15, //
                16, 17, 18, //
                //
                19, 20, 21, //
                22, 23, 24, //
                25, 26, 27 //
                }, //
                IndexingOrder.FAR, //
                3, 3, 3 //
        );

        expected = new RealArray(new double[] {
                //
                1, 2, 3, 1, 2, 3, //
                4, 5, 6, 4, 5, 6, //
                7, 8, 9, 7, 8, 9, //
                //
                10, 11, 12, 10, 11, 12, //
                13, 14, 15, 13, 14, 15, //
                16, 17, 18, 16, 17, 18, //
                //
                19, 20, 21, 19, 20, 21, //
                22, 23, 24, 22, 23, 24, //
                25, 26, 27, 25, 26, 27 //
                }, //
                IndexingOrder.FAR, //
                9, 6 //
        );

        Assert.assertTrue(Arrays.equals(a.reshape(9, 3).tile(1, 2).values(), expected.values()));
    }

    /**
     * Tests {@link RealArray#reverse(int...)}.
     */
    @Test
    public void testReverse() {

        RealArray a = new RealArray(new double[] {
                //
                0, 1, 2, 3, 0, //
                1, 2, 3, 0, 1, //
                2, 3, 0, 1, 2, //
                3, 0, 1, 2, 3, //
                //
                1, 2, 3, 0, 1, //
                2, 3, 0, 1, 2, //
                3, 0, 1, 2, 3, //
                0, 1, 2, 3, 0, //
                //
                2, 3, 0, 1, 2, //
                3, 0, 1, 2, 3, //
                0, 1, 2, 3, 0, //
                1, 2, 3, 0, 1 //
                }, //
                IndexingOrder.FAR, //
                3, 4, 5 //
        );

        RealArray expected = new RealArray(new double[] {
                //
                2, 1, 0, 3, 2, //
                3, 2, 1, 0, 3, //
                0, 3, 2, 1, 0, //
                1, 0, 3, 2, 1, //
                //
                1, 0, 3, 2, 1, //
                2, 1, 0, 3, 2, //
                3, 2, 1, 0, 3, //
                0, 3, 2, 1, 0, //
                //
                0, 3, 2, 1, 0, //
                1, 0, 3, 2, 1, //
                2, 1, 0, 3, 2, //
                3, 2, 1, 0, 3 //
                }, //
                IndexingOrder.FAR, //
                3, 4, 5 //
        );

        Assert.assertTrue(Arrays.equals(a.reverse(2, 0).values(), expected.values()));
    }

    /**
     * Tests {@link Array#concat(int, Array...)}.
     */
    @Test
    public void testConcat() {

        RealArray a = new RealArray(new double[] {
                //
                0, 1, //
                2, 3, //
                //
                4, 5, //
                6, 7 //
                }, //
                IndexingOrder.FAR, //
                2, 2, 2 //
        );

        RealArray b = new RealArray(new double[] {
                //
                0, 1, //
                2, 3, //
                4, 5, //
                //
                6, 7, //
                8, 9, //
                10, 11 //
                }, //
                IndexingOrder.FAR, //
                2, 3, 2 //
        );

        RealArray expected = new RealArray(new double[] {
                //
                0, 1, //
                2, 3, //
                0, 1, //
                2, 3, //
                4, 5, //
                0, 1, //
                2, 3, //
                //
                4, 5, //
                6, 7, //
                6, 7, //
                8, 9, //
                10, 11, //
                4, 5, //
                6, 7 //
                }, //
                IndexingOrder.FAR, //
                2, 7, 2 //
        );

        Assert.assertTrue(Arrays.equals(a.concat(1, b, a).values(), expected.values()));
    }

    /**
     * Tests {@link RealArray#reverseOrder()}.
     */
    @Test
    public void testReverseOrder() {

        RealArray a = new RealArray(new double[] {
                //
                0, 1, 2, 3, 4, 5, 6, 7, 8, //
                9, 10, 11, 12, 13, 14, 15, 16, 17, //
                18, 19, 20, 21, 22, 23, 24, 25, 26, //
                27, 28, 29, 30, 31, 32, 33, 34, 35 //
                }, //
                IndexingOrder.FAR, //
                3, 3, 4 //
        );

        RealArray expected = new RealArray(new double[] {
                //
                0, 12, 24, 4, 16, 28, 8, 20, 32, //
                1, 13, 25, 5, 17, 29, 9, 21, 33, //
                2, 14, 26, 6, 18, 30, 10, 22, 34, //
                3, 15, 27, 7, 19, 31, 11, 23, 35 //
                }, //
                IndexingOrder.NEAR, //
                3, 3, 4 //
        );

        Assert.assertTrue(Arrays.equals(a.reverseOrder().reverseOrder().reverseOrder().values(), expected.values()));
    }

    /**
     * Tests dimension reduce functions.
     */
    @Test
    public void testRrOps() {

        RealArray a = new RealArray(new double[] {
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

        RealArray expected = new RealArray(new double[] {
                //
                30, 34, 38, 42, 46, //
                110, 114, 118, 122, 126, //
                190, 194, 198, 202, 206 //
                }, //
                IndexingOrder.FAR, //
                3, 1, 5 //
        );

        Assert.assertTrue(Tests.equals( //
                a.rSum(1).values(), expected.values()));

        a = new RealArray(new double[] {
                //
                0, 1, 2, //
                3, 4, 5, //
                6, 7, 8, //
                9, 10, 11, //
                //
                12, 13, 14, //
                15, 16, 17, //
                18, 19, 20, //
                21, 22, 23, //
                //
                24, 25, 26, //
                27, 28, 29, //
                30, 31, 32, //
                33, 34, 35, //
                //
                36, 37, 38, //
                39, 40, 41, //
                42, 43, 44, //
                45, 46, 47, //
                //
                48, 49, 50, //
                51, 52, 53, //
                54, 55, 56, //
                57, 58, 59 //
                }, //
                IndexingOrder.NEAR, //
                3, 4, 5 //
        );

        expected = new RealArray(new double[] {
                //
                18, 22, 26, //
                66, 70, 74, //
                114, 118, 122, //
                162, 166, 170, //
                210, 214, 218 //
                }, //
                IndexingOrder.NEAR, //
                3, 1, 5 //
        );

        Assert.assertTrue(Tests.equals( //
                a.rSum(1).values(), expected.values()));

        expected = new RealArray(new double[] {
                //
                66, //
                210, //
                354, //
                498, //
                642 //
                }, //
                IndexingOrder.NEAR, //
                1, 1, 5 //
        );

        Assert.assertTrue(Tests.equals(a.rSum(1, 0).values(), expected.values()));
        Assert.assertTrue(Arrays.equals(a.rSum(1, 0).dims(), expected.dims()));

        expected = new RealArray(new double[] {
                //
                570, //
                590, //
                610 //
                }, //
                IndexingOrder.NEAR, //
                3, 1, 1 //
        );

        Assert.assertTrue(Tests.equals(a.rSum(1, 2).values(), expected.values()));
        Assert.assertTrue(Arrays.equals(a.rSum(1, 2).dims(), expected.dims()));

        expected = new RealArray(new double[] { 1770 }, //
                IndexingOrder.NEAR, //
                1, 1, 1);

        Assert.assertTrue(Tests.equals(a.rSum(1, 0, 2).values(), expected.values()));
        Assert.assertTrue(Arrays.equals(a.rSum(1, 0, 2).dims(), expected.dims()));

        a = new RealArray(new double[] {
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

        expected = new RealArray(new double[] {
                //
                0, 1056, 2856, 5616, 9576, //
                525000, 609336, 703296, 807576, 922896 //
                }, //
                IndexingOrder.FAR, //
                2, 1, 5 //
        );

        Assert.assertTrue(Tests.equals( //
                a.rProd(1).values(), expected.values()));

        a = new RealArray(new double[] {
                //
                0, 0, 0, 0, 0, //
                1, -1, 1, 1, 1, //
                1, 2, -2, 2, 2, //
                3, 3, 3, -3, 3, //
                4, 4, 4, 4, -4 //
                }, //
                IndexingOrder.FAR, //
                5, 5 //
        );

        expected = new RealArray(new double[] {
                //
                0, -1, -2, -3, -4 //
                }, //
                IndexingOrder.FAR, //
                1, 5 //
        );

        Assert.assertTrue(Arrays.equals(a.rMin(0).values(), expected.values()));

        a = new RealArray(new double[] {
                //
                0, 0, 0, 0, 0, //
                1, 2, 1, 1, 1, //
                1, 2, 4, 2, 2, //
                3, 3, 3, 6, 3, //
                4, 4, 4, 4, 8 //
                }, //
                IndexingOrder.FAR, //
                5, 5 //
        );

        expected = new RealArray(new double[] {
                //
                0, //
                2, //
                4, //
                6, //
                8 //
                }, //
                IndexingOrder.FAR, //
                5, 1 //
        );

        Assert.assertTrue(Arrays.equals(a.rMax(1).values(), expected.values()));

        a = new RealArray(new double[] {
                //
                1, 1, 2, //
                2, 1, 4, //
                3, 1, 6, //
                4, 1, 8, //
                5, 1, 10 //
                }, //
                IndexingOrder.FAR, //
                5, 3 //
        );

        expected = new RealArray(new double[] {
                //
                2, 0, 8 //
                }, //
                IndexingOrder.FAR, //
                1, 3 //
        );

        Assert.assertTrue(Arrays.equals(a.rVar(0).values(), expected.values()));
    }

    /**
     * Tests dimension index functions.
     */
    @Test
    public void testRiOps() {

        RealArray a = new RealArray(new double[] {
                //
                1, 0, 0, 0, 1, //
                0, 1, 0, 0, 0, //
                1, 0, 1, 0, 0, //
                0, 1, 0, 1, 0, //
                //
                0, 1, 0, 1, 0, //
                1, 0, 0, 0, 1, //
                0, 1, 0, 0, 0, //
                1, 0, 1, 0, 0, //
                //
                1, 0, 1, 0, 0, //
                1, 1, 0, 1, 0, //
                1, 1, 0, 0, 1, //
                0, 1, 0, 0, 0 //
                }, //
                3, 4, 5 //
        );

        IntegerArray expected = new IntegerArray(new int[] {
                //
                0, 1, 2, 3, 0, //
                2, 3, -1, -1, -1, //
                -1, -1, -1, -1, -1, //
                -1, -1, -1, -1, -1, //
                //
                1, 0, 3, 0, 1, //
                3, 2, -1, -1, -1, //
                -1, -1, -1, -1, -1, //
                -1, -1, -1, -1, -1, //
                //
                0, 1, 0, 1, 2, //
                1, 2, -1, -1, -1, //
                2, 3, -1, -1, -1, //
                -1, -1, -1, -1, -1 //
                }, //
                3, 4, 5 //
        );

        Assert.assertTrue(Arrays.equals(a.iMax(1).values(), expected.values()));
        Assert.assertTrue(Arrays.equals(a.uMul(-1.0).iMin(1).values(), expected.values()));

        a = new RealArray(new double[] {
                //
                1, 0, 0, 0, 1, //
                0, 1, 0, 0, 0, //
                1, 0, 1, 0, 0, //
                0, 1, 0, 1, 0, //
                //
                0, 1, 0, 1, 0, //
                1, 0, 0, 0, 1, //
                0, 1, 0, 0, 0, //
                1, 0, 1, 0, 0, //
                //
                1, 0, 1, 0, 0, //
                1, 1, 0, 1, 0, //
                1, 1, 0, 0, 1, //
                0, 1, 0, 0, 0 //
                }, //
                3, 4, 5 //
        );

        expected = new IntegerArray(new int[] {
                //
                1, 2, 3, -1, -1, //
                0, 2, 3, 4, -1, //
                1, 3, 4, -1, -1, //
                0, 2, 4, -1, -1, //
                //
                0, 2, 4, -1, -1, //
                1, 2, 3, -1, -1, //
                0, 2, 3, 4, -1, //
                1, 3, 4, -1, -1, //
                //
                1, 3, 4, -1, -1, //
                2, 4, -1, -1, -1, //
                2, 3, -1, -1, -1, //
                0, 2, 3, 4, -1 //
                }, //
                3, 4, 5 //
        );

        Assert.assertTrue(Arrays.equals(a.iZero(2).values(), expected.values()));

        a = new RealArray(new double[] {
                //
                1, 0, 0, 0, 2, //
                0, 1, 0, 0, 0, //
                1, 0, 2, 0, 0, //
                0, 1, 0, 2, 0, //
                //
                0, 1, 0, 2, 0, //
                1, 0, 0, 0, 2, //
                0, 1, 0, 0, 0, //
                1, 0, 2, 0, 0, //
                //
                1, 0, 2, 0, 0, //
                1, 2, 0, 3, 0, //
                1, 2, 0, 0, 3, //
                0, 2, 0, 0, 0 //
                }, //
                3, 4, 5 //
        );

        expected = new IntegerArray(new int[] {
                //
                0, 4, -1, -1, -1, //
                1, -1, -1, -1, -1, //
                0, 2, -1, -1, -1, //
                1, 3, -1, -1, -1, //
                //
                1, 3, -1, -1, -1, //
                0, 4, -1, -1, -1, //
                1, -1, -1, -1, -1, //
                0, 2, -1, -1, -1, //
                //
                0, 2, -1, -1, -1, //
                0, 1, 3, -1, -1, //
                0, 1, 4, -1, -1, //
                1, -1, -1, -1, -1 //
                }, //
                3, 4, 5 //
        );

        Assert.assertTrue(Arrays.equals(a.iGZero(2).values(), expected.values()));
        Assert.assertTrue(Arrays.equals(a.clone().uMul(-1.0).iLZero(2).values(), expected.values()));

        a = new RealArray(new double[] {
                //
                0, 1, 2, 3, 0, //
                1, 2, 3, 0, 1, //
                2, 3, 0, 1, 2, //
                3, 0, 1, 2, 3, //
                //
                1, 2, 3, 0, 1, //
                2, 3, 0, 1, 2, //
                3, 0, 1, 2, 3, //
                0, 1, 2, 3, 0, //
                //
                2, 3, 0, 1, 2, //
                3, 0, 1, 2, 3, //
                0, 1, 2, 3, 0, //
                1, 2, 3, 0, 1 //
                }, //
                3, 4, 5 //
        );

        expected = new IntegerArray(new int[] {
                //
                0, 3, 2, 1, 0, //
                1, 0, 3, 2, 1, //
                2, 1, 0, 3, 2, //
                3, 2, 1, 0, 3, //
                //
                3, 2, 1, 0, 3, //
                0, 3, 2, 1, 0, //
                1, 0, 3, 2, 1, //
                2, 1, 0, 3, 2, //
                //
                2, 1, 0, 3, 2, //
                3, 2, 1, 0, 3, //
                0, 3, 2, 1, 0, //
                1, 0, 3, 2, 1 //
                }, //
                3, 4, 5 //
        );

        RealArray valuesExpected = new RealArray(new double[] {
                //
                0, 0, 0, 0, 0, //
                1, 1, 1, 1, 1, //
                2, 2, 2, 2, 2, //
                3, 3, 3, 3, 3, //
                //
                0, 0, 0, 0, 0, //
                1, 1, 1, 1, 1, //
                2, 2, 2, 2, 2, //
                3, 3, 3, 3, 3, //
                //
                0, 0, 0, 0, 0, //
                1, 1, 1, 1, 1, //
                2, 2, 2, 2, 2, //
                3, 3, 3, 3, 3 //
                }, //
                3, 4, 5 //
        );

        // Sort along the dimension.
        IntegerArray indices = a.iSort(1);

        Assert.assertTrue(Arrays.equals(a.values(), valuesExpected.values()) //
                && Arrays.equals(indices.values(), expected.values()));

        a = new RealArray(new double[] {
                //
                0, 1, 2, 3, 4, //
                5, 44, 7, 8, 9, //
                10, 11, 12, 13, 14, //
                15, 16, 17, 45, 19, //
                20, 21, 22, 23, 24 //
                }, //
                5, 5 //
        );

        expected = new IntegerArray(new int[] {
                //
                0, 0, 0, 0, 0, //
                0, 0, 0, 0, 0, //
                0, 0, 0, 0, 0, //
                0, 0, 0, 1, 0, //
                0, 0, 0, 0, 0 //
                }, //
                5, 5 //
        );

        Assert.assertTrue(Arrays.equals(a.iMax(-1).values(), expected.values()));

        a = new RealArray(new double[] {
                //
                0, 1, 2, 3, 4, //
                5, 44, 7, -43, 9, //
                10, 11, 12, 13, 14, //
                15, -42, 17, 45, 19, //
                20, 21, 22, 23, 24 //
                }, //
                5, 5 //
        );

        expected = new IntegerArray(new int[] {
                //
                0, 0, 0, 0, 0, //
                0, 0, 0, 1, 0, //
                0, 0, 0, 0, 0, //
                0, 0, 0, 0, 0, //
                0, 0, 0, 0, 0 //
                }, //
                5, 5 //
        );

        Assert.assertTrue(Arrays.equals(a.iMin(-1).values(), expected.values()));

        a = new RealArray(new double[] {
                //
                1, 0, 0, 0, 1, //
                0, 1, 0, 1, 0, //
                0, 0, 1, 0, 0, //
                0, 1, 0, 1, 0, //
                1, 0, 0, 0, 1 //
                }, //
                5, 5 //
        );

        expected = new IntegerArray(new int[] {
                //
                0, 1, 1, 1, 0, //
                1, 0, 1, 0, 1, //
                1, 1, 0, 1, 1, //
                1, 0, 1, 0, 1, //
                0, 1, 1, 1, 0 //
                }, //
                5, 5 //
        );

        Assert.assertTrue(Arrays.equals(a.iZero(-1).values(), expected.values()));

        a = new RealArray(new double[] {
                //
                1, 0, 0, 0, 1, //
                0, 2, 0, 2, 0, //
                0, 0, 3, 0, 0, //
                0, 2, 0, 2, 0, //
                1, 0, 0, 0, 1 //
                }, //
                5, 5 //
        );

        expected = new IntegerArray(new int[] {
                //
                1, 0, 0, 0, 1, //
                0, 1, 0, 1, 0, //
                0, 0, 1, 0, 0, //
                0, 1, 0, 1, 0, //
                1, 0, 0, 0, 1 //
                }, //
                5, 5 //
        );

        Assert.assertTrue(Arrays.equals(a.iGZero(-1).values(), expected.values()));
        Assert.assertTrue(Arrays.equals(a.clone().uMul(-1.0).iLZero(-1).values(), expected.values()));

        a = new RealArray(new double[] {
                //
                24, 23, 22, 21, 20, //
                0, 1, 2, 3, 4, //
                5, 6, 7, 8, 9, //
                14, 13, 12, 11, 10, //
                15, 16, 17, 18, 19 //
                }, //
                5, 5 //
        );

        expected = new IntegerArray(new int[] {
                //
                5, 6, 7, 8, 9, //
                10, 11, 12, 13, 14, //
                19, 18, 17, 16, 15, //
                20, 21, 22, 23, 24, //
                4, 3, 2, 1, 0 //
                }, //
                5, 5 //
        );

        valuesExpected = new RealArray(new double[] {
                //
                0, 1, 2, 3, 4, //
                5, 6, 7, 8, 9, //
                10, 11, 12, 13, 14, //
                15, 16, 17, 18, 19, //
                20, 21, 22, 23, 24 //
                }, //
                5, 5 //
        );

        // Sort the backing array.
        indices = a.iSort(-1);

        Assert.assertTrue(Arrays.equals(a.values(), valuesExpected.values()) //
                && Arrays.equals(indices.values(), expected.values()));
    }

    /**
     * Tests dimension functions.
     */
    @Test
    public void testRdOps() {

        RealArray a = new RealArray(new double[] {
                //
                0, 1, 2, 3, 0, //
                1, 2, 3, 0, 1, //
                2, 3, 0, 1, 2, //
                3, 0, 1, 2, 3, //
                //
                1, 2, 3, 0, 1, //
                2, 3, 0, 1, 2, //
                3, 0, 1, 2, 3, //
                0, 1, 2, 3, 0, //
                //
                2, 3, 0, 1, 2, //
                3, 0, 1, 2, 3, //
                0, 1, 2, 3, 0, //
                1, 2, 3, 0, 1 //
                }, //
                IndexingOrder.FAR, //
                3, 4, 5 //
        );

        RealArray expected = new RealArray(new double[] {
                //
                0, 1, 3, 6, 6, //
                1, 3, 6, 6, 7, //
                2, 5, 5, 6, 8, //
                3, 3, 4, 6, 9, //
                //
                1, 4, 9, 12, 13, //
                3, 8, 11, 12, 15, //
                5, 8, 9, 12, 17, //
                3, 4, 7, 12, 15, //
                //
                3, 9, 14, 18, 21, //
                6, 11, 15, 18, 24, //
                5, 9, 12, 18, 23, //
                4, 7, 13, 18, 22 //
                }, //
                IndexingOrder.FAR, //
                3, 4, 5 //
        );

        Assert.assertTrue(Arrays.equals(a.dSum(0, 2).values(), expected.values()));

        a = new RealArray(new double[] {
                //
                1, 1, 2, 3, 1, //
                1, 2, 3, 1, 1, //
                2, 3, 1, 1, 2, //
                3, 1, 1, 2, 3, //
                //
                1, 2, 3, 1, 1, //
                2, 3, 1, 1, 2, //
                3, 1, 1, 2, 3, //
                1, 1, 2, 3, 1 //
                }, //
                IndexingOrder.FAR, //
                2, 4, 5 //
        );

        expected = new RealArray(new double[] {
                //
                1, 1, 2, 3, 1, //
                1, 2, 6, 3, 1, //
                2, 6, 6, 3, 2, //
                6, 6, 6, 6, 6, //
                //
                1, 2, 3, 1, 1, //
                2, 6, 3, 1, 2, //
                6, 6, 3, 2, 6, //
                6, 6, 6, 6, 6 //
                //
                }, //
                IndexingOrder.FAR, //
                2, 4, 5 //
        );

        Assert.assertTrue(Arrays.equals(a.dProd(1).values(), expected.values()));
    }

    /**
     * Tests {@link AbstractRealArray#map(RealMap)}.
     */
    @Test
    public void testRealMap() {

        final int size = 16;

        RealArray a = new RealArray(Arithmetic.doubleRange(size * size), size, size);

        RealArray expected = a.clone().map(new RealMap() {

            @Override
            public double apply(double value, int[] logical) {

                Assert.assertTrue(Math.abs(value - (size * logical[0] + logical[1])) < 1e-8);

                return 2 * value;
            }
        });

        Assert.assertTrue(Tests.equals( //
                a.clone().uMul(2.0).values(), expected.values()));
    }

    /**
     * Tests {@link AbstractRealArray#reduce(RealReduce)}.
     */
    @Test
    public void testRealReduce() {

        final int size = 16;

        RealArray a = new RealArray(Arithmetic.doubleRange(size * size), size, size);

        double expected = a.clone().reduce(new RealReduce() {

            double acc = 0.0;

            @Override
            public void apply(double value, int[] logical) {
                this.acc += value * (size * logical[0] + logical[1]);
            }

            @Override
            public double get() {
                return this.acc;
            }
        });

        Assert.assertTrue(Math.abs(a.clone().uSqr().aSum() - expected) < 1e-8);
    }

    /**
     * Tests corner cases.
     */
    @Test
    public void testCornerCases() {

        new RealArray(10, 10, 10).slice( //
                new int[][] {
                        //
                        { 1, 2, 3, 4, 5, 6, 7, 8 }, //
                        {}, //
                        { 0, 1 } //
                }, //
                new RealArray(10, 10, 10), //
                new int[][] {
                        //
                        { 0, 1, 2, 3, 4, 5, 6, 7 }, //
                        {}, //
                        { 1, 2 } //
                });

        new RealArray(10, 10, 0).map(new RealArray(10, 10, 10), //
                0, 0, 5, //
                5, 5, 5, //
                0, 0, 10);

        new RealArray(10, 10, 10).map(new RealArray(10, 0, 10), //
                0, 0, 5, //
                5, 5, 5, //
                0, 0, 10);

        new RealArray(10, 10, 10).map(new RealArray(10, 10, 10), //
                0, 0, 5, //
                5, 5, 0, //
                0, 0, 10);
    }
}

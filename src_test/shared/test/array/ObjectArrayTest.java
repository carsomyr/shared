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

package shared.test.array;

import static shared.util.Control.NullRunnable;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import shared.array.Array;
import shared.array.Array.IndexingOrder;
import shared.array.IntegerArray;
import shared.array.ObjectArray;

/**
 * A class of unit tests for {@link ObjectArray}.
 * 
 * @author Roy Liu
 */
public class ObjectArrayTest {

    /**
     * Default constructor.
     */
    public ObjectArrayTest() {
    }

    /**
     * Tests {@link ObjectArray#map(Array, int...)}.
     */
    @Test
    public void testMap() {

        ObjectArray<String> a = new ObjectArray<String>(new String[] {
                //
                "0", "1", "2", "3", //
                "4", "5", "6", "7", //
                "8", "9", "10", "11", //
                //
                "12", "13", "14", "15", //
                "16", "17", "18", "19", //
                "20", "21", "22", "23", //
                //
                "24", "25", "26", "27", //
                "28", "29", "30", "31", //
                "32", "33", "34", "35" //
        //
                }, //
                IndexingOrder.FAR, //
                3, 3, 4 //
        );

        ObjectArray<String> expected = a.map( //
                new ObjectArray<String>(String.class, IndexingOrder.NEAR, 3, 3, 4), //
                1, 0, 2, //
                0, 1, 2, //
                1, 0, 3);

        a = a.reverseOrder() //
                .map(a, //
                        0, 0, 3, //
                        0, 0, 3, //
                        0, 0, 4) //
                .map(new ObjectArray<String>(String.class, IndexingOrder.FAR, 3, 3, 4), //
                        1, 0, 2, //
                        0, 1, 2, //
                        1, 0, 3);

        Assert.assertTrue(Arrays.equals(a.reverseOrder().values(), expected.values()));
    }

    /**
     * Tests {@link ObjectArray#slice(int[][], shared.array.ProtoArray, int[][])}.
     */
    @Test
    public void testSlice() {

        ObjectArray<Integer> original = new ObjectArray<Integer>(new Integer[] {
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

        ObjectArray<Integer> a = original.splice(new ObjectArray<Integer>(Integer.class, IndexingOrder.NEAR, //
                2, 3, 4), //
                //
                1, 0, 0, //
                1, 1, 0, //
                //
                1, 0, 1, //
                1, 1, 1, //
                //
                1, 0, 2, //
                2, 1, 2, //
                2, 2, 2, //
                3, 3, 2);

        ObjectArray<Integer> expected = new ObjectArray<Integer>(new Integer[] {
                //
                26, 27, 27, 28, //
                26, 27, 27, 28, //
                null, null, null, null, //
                //
                26, 27, 27, 28, //
                26, 27, 27, 28, //
                null, null, null, null //
                }, //
                IndexingOrder.FAR, //
                2, 3, 4 //
        );

        Assert.assertTrue(Arrays.equals(a.reverseOrder().values(), expected.values()));

        a = original.slice( //
                new int[][] {
                        //
                        new int[] { 1, 1 }, //
                        new int[] { 1, 1 }, //
                        new int[] { 1, 2, 2, 3 } //
                }, //
                new ObjectArray<Integer>(Integer.class, IndexingOrder.NEAR, 2, 3, 4), //
                //
                new int[][] {
                        //
                        new int[] { 0, 1 }, //
                        new int[] { 0, 1 }, //
                        new int[] { 0, 1, 2, 3 } //
                });

        Assert.assertTrue(Arrays.equals(a.reverseOrder().values(), expected.values()));
    }

    /**
     * Tests {@link ObjectArray#iSort(int)}.
     */
    @Test
    public void testISort() {

        ObjectArray<Integer> a = new ObjectArray<Integer>(new Integer[] {
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

        IntegerArray expected = new IntegerArray(new int[] {
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

        ObjectArray<Integer> valuesExpected = new ObjectArray<Integer>(new Integer[] {
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

        a = new ObjectArray<Integer>(new Integer[] {
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

        valuesExpected = new ObjectArray<Integer>(new Integer[] {
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
     * Tests that a {@link RuntimeException} is thrown.
     */
    @SuppressWarnings("unchecked")
    @Test(expected = RuntimeException.class)
    public void testThrowException() {

        ObjectArray<Runnable> runnableArray = new ObjectArray<Runnable>(new Runnable[] { NullRunnable }, 1);
        ObjectArray<Thread> threadArray = new ObjectArray<Thread>(new Thread[] { Thread.currentThread() }, 1);

        try {

            runnableArray.slice((ObjectArray<Runnable>) ((ObjectArray<?>) threadArray), new int[] { 0 });

        } catch (RuntimeException e) {

            Assert.assertTrue(e.getMessage().equals("Invalid array types"));

            throw e;
        }
    }
}

/**
 * This file is part of the Shared Scientific Toolbox in Java ("this library"). <br />
 * <br />
 * Copyright (C) 2007 Roy Liu <br />
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

package shared.test.array;

import static org.junit.Assert.assertTrue;
import static shared.util.Control.NullRunnable;

import java.util.Arrays;

import org.junit.Test;

import shared.array.Array;
import shared.array.IntegerArray;
import shared.array.ObjectArray;
import shared.array.Array.IndexingOrder;

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

        assertTrue(Arrays.equals(a.reverseOrder().values(), expected.values()));
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

        assertTrue(Arrays.equals(a.reverseOrder().values(), expected.values()));

        a = original.slice( //
                new int[][] {
                //
                        new int[] { 1, 1 }, //
                        new int[] { 1, 1 }, //
                        new int[] { 1, 2, 2, 3 } //
                }, //
                //
                new ObjectArray<Integer>(Integer.class, IndexingOrder.NEAR, 2, 3, 4), //
                //
                new int[][] {
                //
                        new int[] { 0, 1 }, //
                        new int[] { 0, 1 }, //
                        new int[] { 0, 1, 2, 3 } //
                });

        assertTrue(Arrays.equals(a.reverseOrder().values(), expected.values()));
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

        assertTrue(Arrays.equals(a.values(), valuesExpected.values()) //
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

        assertTrue(Arrays.equals(a.values(), valuesExpected.values()) //
                && Arrays.equals(indices.values(), expected.values()));
    }

    /**
     * Tests that a {@link RuntimeException} is thrown.
     */
    @SuppressWarnings("unchecked")
    @Test(expected = RuntimeException.class)
    public void testThrowException() {

        ObjectArray runnableArray = new ObjectArray<Runnable>(new Runnable[] { NullRunnable }, 1);
        ObjectArray threadArray = new ObjectArray<Thread>(new Thread[] { Thread.currentThread() }, 1);

        try {

            ((ObjectArray<Runnable>) runnableArray).slice(threadArray, new int[] { 0 });

        } catch (RuntimeException e) {

            assertTrue(e.getMessage().equals("Invalid array types"));

            throw e;
        }
    }
}

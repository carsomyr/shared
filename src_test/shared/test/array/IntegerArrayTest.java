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

import java.util.Arrays;

import org.junit.Test;

import shared.array.Array;
import shared.array.IntegerArray;
import shared.array.Array.IndexingOrder;

/**
 * A class of unit tests for {@link IntegerArray}.
 * 
 * @author Roy Liu
 */
public class IntegerArrayTest {

    /**
     * Default constructor.
     */
    public IntegerArrayTest() {
    }

    /**
     * Tests {@link Array#map(Array, int...)}.
     */
    @Test
    public void testMap() {

        IntegerArray a = new IntegerArray(new int[] {
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

        IntegerArray expected = a.map( //
                new IntegerArray(IndexingOrder.NEAR, 3, 3, 4), //
                1, 0, 2, //
                0, 1, 2, //
                1, 0, 3);

        a = a.map(new IntegerArray(IndexingOrder.FAR, 3, 3, 4), //
                1, 0, 2, 0, 1, 2, 1, 0, 3);

        assertTrue(Arrays.equals(a.reverseOrder().values(), expected.values()));
    }

    /**
     * Tests {@link Array#splice(Array, int...)}.
     */
    @Test
    public void testSlice() {

        IntegerArray a = new IntegerArray(new int[] {
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

        a = a.splice(new IntegerArray(IndexingOrder.NEAR, 2, 3, 4), //
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

        IntegerArray expected = new IntegerArray(new int[] {
        //
                26, 27, 27, 28, //
                26, 27, 27, 28, //
                0, 0, 0, 0, //
                //
                26, 27, 27, 28, //
                26, 27, 27, 28, //
                0, 0, 0, 0 //
                }, //
                IndexingOrder.FAR, //
                2, 3, 4 //
        );

        assertTrue(Arrays.equals(a.reverseOrder().values(), expected.values()));

        a = new IntegerArray(new int[] {
        //
                0, 1, 2, 3, 4, //
                5, 6, 7, 8, 9, //
                10, 11, 12, 13, 14, //
                15, 16, 17, 18, 19, //
                20, 21, 22, 23, 24 //
                }, //
                IndexingOrder.FAR, //
                5, 5 //
        );

        a = a.splice(new IntegerArray(IndexingOrder.FAR, 3, 3), //
                //
                1, 0, 0, //
                2, 1, 0, //
                3, 2, 0, //
                //
                0, 0, 1, //
                2, 1, 1, //
                4, 2, 1);

        expected = new IntegerArray(new int[] {
        //
                5, 7, 9, //
                10, 12, 14, //
                15, 17, 19 //
                }, //
                IndexingOrder.FAR, //
                3, 3 //
        );

        assertTrue(Arrays.equals(a.values(), expected.values()));
    }

    /**
     * Tests {@link IntegerArray#find(int...)}.
     */
    @Test
    public void testFind() {

        IntegerArray a = new IntegerArray(new int[] {
        //
                0, 1, 2, 3, -1, //
                5, 6, 7, -1, -1, //
                10, 11, -1, -1, -1, //
                15, -1, -1, -1, -1, //
                //
                20, 21, 22, 23, -1, //
                25, 26, 27, -1, -1, //
                30, 31, -1, -1, -1, //
                35, -1, -1, -1, -1, //
                //
                40, 41, 42, 43, -1, //
                45, 46, 47, -1, -1, //
                50, 51, -1, -1, -1, //
                55, -1, -1, -1, -1 //
                }, //
                IndexingOrder.FAR, //
                3, 4, 5 //
        );

        assertTrue(Arrays.equals(a.find(0, 0, -1), new int[] { 0, 1, 2, 3 }));
        assertTrue(Arrays.equals(a.find(0, 1, -1), new int[] { 5, 6, 7 }));
        assertTrue(Arrays.equals(a.find(0, 2, -1), new int[] { 10, 11 }));
        assertTrue(Arrays.equals(a.find(0, 3, -1), new int[] { 15 }));
        assertTrue(Arrays.equals(a.find(1, 0, -1), new int[] { 20, 21, 22, 23 }));
        assertTrue(Arrays.equals(a.find(1, 1, -1), new int[] { 25, 26, 27 }));
        assertTrue(Arrays.equals(a.find(1, 2, -1), new int[] { 30, 31 }));
        assertTrue(Arrays.equals(a.find(1, 3, -1), new int[] { 35 }));
        assertTrue(Arrays.equals(a.find(2, 0, -1), new int[] { 40, 41, 42, 43 }));
        assertTrue(Arrays.equals(a.find(2, 1, -1), new int[] { 45, 46, 47 }));
        assertTrue(Arrays.equals(a.find(2, 2, -1), new int[] { 50, 51 }));
        assertTrue(Arrays.equals(a.find(2, 3, -1), new int[] { 55 }));
    }

    /**
     * Tests {@link IntegerArray#ndgrid(int...)}.
     */
    @Test
    public void testNDGrid() {

        IntegerArray[] arrays = IntegerArray.ndgrid(-3, 0, 1, 3, 0, -1, 6, 0, -2);

        assertTrue(Arrays.equals(arrays[0].values(), new int[] {
        //
                -3, -3, -3, //
                -3, -3, -3, //
                -3, -3, -3, //
                //
                -2, -2, -2, //
                -2, -2, -2, //
                -2, -2, -2, //
                //
                -1, -1, -1, //
                -1, -1, -1, //
                -1, -1, -1 //
                }) //
        );

        assertTrue(Arrays.equals(arrays[1].values(), new int[] {
        //
                3, 3, 3, //
                2, 2, 2, //
                1, 1, 1, //
                //
                3, 3, 3, //
                2, 2, 2, //
                1, 1, 1, //
                //
                3, 3, 3, //
                2, 2, 2, //
                1, 1, 1 //
                }) //
        );

        assertTrue(Arrays.equals(arrays[2].values(), new int[] {
        //
                6, 4, 2, //
                6, 4, 2, //
                6, 4, 2, //
                //
                6, 4, 2, //
                6, 4, 2, //
                6, 4, 2, //
                //
                6, 4, 2, //
                6, 4, 2, //
                6, 4, 2 //
                }) //
        );
    }
}

/**
 * This file is part of the Shared Scientific Toolbox in Java ("this library"). <br />
 * <br />
 * Copyright (C) 2009 Roy Liu <br />
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

package shared.test.util;

import static org.junit.Assert.assertTrue;
import static shared.util.Arrays.binarySearchNearest;
import static shared.util.Arrays.compare;

import java.math.RoundingMode;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.junit.Test;

import shared.util.Arrays;

/**
 * A class of unit tests for {@link Arrays}.
 * 
 * @author Roy Liu
 */
public class ArraysTest {

    /**
     * The canonical {@link Comparator} for {@link Double}s, except in reverse.
     */
    final protected static Comparator<Double> ReverseDoubleComparator = new Comparator<Double>() {

        public int compare(Double lhs, Double rhs) {
            return -lhs.compareTo(rhs);
        }
    };

    /**
     * Default constructor.
     */
    public ArraysTest() {
    }

    /**
     * Tests all "nearest" binary search variants.
     */
    @Test
    public void testBinarySearchNearest() {

        int[] intArray = new int[] { 1, 2, 4, 8, 16, 32, 64, 128, 256 };
        double[] doubleArray = new double[] { -16, -8, -4, -2, -1, 0, 1, 2, 4, 8, 16 };
        Double[] objectArray = new Double[] { -16.0, -8.0, -4.0, -2.0, -1.0, 0.0, 1.0, 2.0, 4.0, 8.0, 16.0 };
        List<Double> objectList = java.util.Arrays.asList(objectArray);

        Double[] reverseArray = objectArray.clone();
        List<Double> reverseList = java.util.Arrays.asList(reverseArray);
        Collections.reverse(reverseList);

        assertTrue(binarySearchNearest(intArray, 16) == 4);
        assertTrue(binarySearchNearest(intArray, 48) == 5);
        assertTrue(binarySearchNearest(intArray, 48, RoundingMode.HALF_DOWN) == 5);
        assertTrue(binarySearchNearest(intArray, 48, RoundingMode.HALF_UP) == 6);
        assertTrue(binarySearchNearest(intArray, 18, RoundingMode.UP) == 5);
        assertTrue(binarySearchNearest(intArray, 30, RoundingMode.DOWN) == 4);

        assertTrue(binarySearchNearest(doubleArray, 8.0) == 9);
        assertTrue(binarySearchNearest(doubleArray, 0.5) == 5);
        assertTrue(binarySearchNearest(doubleArray, 0.5, RoundingMode.HALF_DOWN) == 5);
        assertTrue(binarySearchNearest(doubleArray, 0.5, RoundingMode.HALF_UP) == 6);
        assertTrue(binarySearchNearest(doubleArray, -7.75, RoundingMode.UP) == 2);
        assertTrue(binarySearchNearest(doubleArray, -4.25, RoundingMode.DOWN) == 1);

        assertTrue(binarySearchNearest(objectArray, 8.0) == 9);
        assertTrue(binarySearchNearest(objectArray, 0.75) == 5);
        assertTrue(binarySearchNearest(objectArray, -7.75, RoundingMode.UP) == 2);
        assertTrue(binarySearchNearest(objectArray, -4.25, RoundingMode.DOWN) == 1);

        assertTrue(binarySearchNearest(objectList, 8.0) == 9);
        assertTrue(binarySearchNearest(objectList, 0.75) == 5);
        assertTrue(binarySearchNearest(objectList, -7.75, RoundingMode.UP) == 2);
        assertTrue(binarySearchNearest(objectList, -4.25, RoundingMode.DOWN) == 1);

        assertTrue(binarySearchNearest(reverseArray, 8.0, ReverseDoubleComparator) == 1);
        assertTrue(binarySearchNearest(reverseArray, 0.75, ReverseDoubleComparator) == 4);
        assertTrue(binarySearchNearest(reverseArray, -7.75, ReverseDoubleComparator, RoundingMode.UP) == 9);
        assertTrue(binarySearchNearest(reverseArray, -4.25, ReverseDoubleComparator, RoundingMode.DOWN) == 8);

        assertTrue(binarySearchNearest(reverseList, 8.0, ReverseDoubleComparator) == 1);
        assertTrue(binarySearchNearest(reverseList, 0.75, ReverseDoubleComparator) == 4);
        assertTrue(binarySearchNearest(reverseList, -7.75, ReverseDoubleComparator, RoundingMode.UP) == 9);
        assertTrue(binarySearchNearest(reverseList, -4.25, ReverseDoubleComparator, RoundingMode.DOWN) == 8);
    }

    /**
     * Tests all array comparison variants.
     */
    @Test
    public void testCompare() {

        assertTrue(0 == compare(new int[] {}, new int[] {}));
        assertTrue(0 > compare(new int[] { 1, 2, 3 }, new int[] { 1, 2, 3, 0, 0 }));
        assertTrue(0 < compare(new int[] { 1, 2, 3, 0, 0 }, new int[] { 1, 2, 3 }));
        assertTrue(0 == compare(new int[] { 1, 2, 3, 4, 5 }, new int[] { 1, 2, 3, 4, 5 }));
        assertTrue(0 > compare(new int[] { 1, 2, 3, 4, 5 }, new int[] { 1, 2, 3, 6, 6 }));
        assertTrue(0 < compare(new int[] { 1, 2, 3, 6, 6 }, new int[] { 1, 2, 3, 4, 5 }));

        assertTrue(0 == compare(new double[] {}, new double[] {}));
        assertTrue(0 > compare(new double[] { 1, 2, 3 }, new double[] { 1, 2, 3, 0, 0 }));
        assertTrue(0 < compare(new double[] { 1, 2, 3, 0, 0 }, new double[] { 1, 2, 3 }));
        assertTrue(0 == compare(new double[] { 1, 2, 3, 4, 5 }, new double[] { 1, 2, 3, 4, 5 }));
        assertTrue(0 > compare(new double[] { 1, 2, 3, 4, 5 }, new double[] { 1, 2, 3, 6, 6 }));
        assertTrue(0 < compare(new double[] { 1, 2, 3, 6, 6 }, new double[] { 1, 2, 3, 4, 5 }));

        assertTrue(0 == compare(new Integer[] {}, new Integer[] {}));
        assertTrue(0 > compare(new Integer[] { 1, 2, 3 }, new Integer[] { 1, 2, 3, 0, 0 }));
        assertTrue(0 < compare(new Integer[] { 1, 2, 3, 0, 0 }, new Integer[] { 1, 2, 3 }));
        assertTrue(0 == compare(new Integer[] { 1, 2, 3, 4, 5 }, new Integer[] { 1, 2, 3, 4, 5 }));
        assertTrue(0 > compare(new Integer[] { 1, 2, 3, 4, 5 }, new Integer[] { 1, 2, 3, 6, 6 }));
        assertTrue(0 < compare(new Integer[] { 1, 2, 3, 6, 6 }, new Integer[] { 1, 2, 3, 4, 5 }));

        assertTrue(0 < compare( //
                new Double[] { 1.0, 2.0, 3.0, 4.0, 5.0 }, //
                new Double[] { 1.0, 2.0, 3.0, 6.0, 6.0 }, //
                ReverseDoubleComparator));
        assertTrue(0 > compare( //
                new Double[] { 1.0, 2.0, 3.0, 6.0, 6.0 }, //
                new Double[] { 1.0, 2.0, 3.0, 4.0, 5.0 }, //
                ReverseDoubleComparator));
    }

    /**
     * Tests all array slicing variants.
     */
    @Test
    public void testSlice() {

        assertTrue(java.util.Arrays.equals(Arrays.slice(new int[] { 1, 2, 3, 4, 5 }, //
                new int[] { 0, 1, 1, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 4 }), //
                new int[] { 1, 2, 2, 3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 5, 5 }));
        assertTrue(java.util.Arrays.equals(Arrays.slice(new double[] { 1, 2, 3, 4, 5 }, //
                new int[] { 0, 1, 1, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 4 }), //
                new double[] { 1, 2, 2, 3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 5, 5 }));
        assertTrue(java.util.Arrays.equals(Arrays.slice(new Integer[] { 1, 2, 3, 4, 5 }, //
                new int[] { 0, 1, 1, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 4 }, Integer.class), //
                new Integer[] { 1, 2, 2, 3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 5, 5 }));
    }
}

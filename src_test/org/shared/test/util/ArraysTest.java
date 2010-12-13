/**
 * <p>
 * Copyright (c) 2009 Roy Liu<br>
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

package org.shared.test.util;

import java.math.RoundingMode;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.shared.util.Arrays;

/**
 * A class of unit tests for {@link Arrays}.
 * 
 * @author Roy Liu
 */
public class ArraysTest {

    /**
     * The canonical {@link Comparator} for {@link Double}s, except in reverse.
     */
    final protected static Comparator<Double> reverseDoubleComparator = new Comparator<Double>() {

        @Override
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
     * Tests all nearest binary search variants.
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

        Assert.assertTrue(Arrays.binarySearchNearest(intArray, 16) == 4);
        Assert.assertTrue(Arrays.binarySearchNearest(intArray, 48) == 5);
        Assert.assertTrue(Arrays.binarySearchNearest(intArray, 48, RoundingMode.HALF_DOWN) == 5);
        Assert.assertTrue(Arrays.binarySearchNearest(intArray, 48, RoundingMode.HALF_UP) == 6);
        Assert.assertTrue(Arrays.binarySearchNearest(intArray, 18, RoundingMode.UP) == 5);
        Assert.assertTrue(Arrays.binarySearchNearest(intArray, 30, RoundingMode.DOWN) == 4);

        Assert.assertTrue(Arrays.binarySearchNearest(doubleArray, 8.0) == 9);
        Assert.assertTrue(Arrays.binarySearchNearest(doubleArray, 0.5) == 5);
        Assert.assertTrue(Arrays.binarySearchNearest(doubleArray, 0.5, RoundingMode.HALF_DOWN) == 5);
        Assert.assertTrue(Arrays.binarySearchNearest(doubleArray, 0.5, RoundingMode.HALF_UP) == 6);
        Assert.assertTrue(Arrays.binarySearchNearest(doubleArray, -7.75, RoundingMode.UP) == 2);
        Assert.assertTrue(Arrays.binarySearchNearest(doubleArray, -4.25, RoundingMode.DOWN) == 1);

        Assert.assertTrue(Arrays.binarySearchNearest(objectArray, 8.0) == 9);
        Assert.assertTrue(Arrays.binarySearchNearest(objectArray, 0.75) == 5);
        Assert.assertTrue(Arrays.binarySearchNearest(objectArray, -7.75, RoundingMode.UP) == 2);
        Assert.assertTrue(Arrays.binarySearchNearest(objectArray, -4.25, RoundingMode.DOWN) == 1);

        Assert.assertTrue(Arrays.binarySearchNearest(objectList, 8.0) == 9);
        Assert.assertTrue(Arrays.binarySearchNearest(objectList, 0.75) == 5);
        Assert.assertTrue(Arrays.binarySearchNearest(objectList, -7.75, RoundingMode.UP) == 2);
        Assert.assertTrue(Arrays.binarySearchNearest(objectList, -4.25, RoundingMode.DOWN) == 1);

        Assert.assertTrue(Arrays.binarySearchNearest( //
                reverseArray, 8.0, reverseDoubleComparator) == 1);
        Assert.assertTrue(Arrays.binarySearchNearest( //
                reverseArray, 0.75, reverseDoubleComparator) == 4);
        Assert.assertTrue(Arrays.binarySearchNearest( //
                reverseArray, -7.75, reverseDoubleComparator, RoundingMode.UP) == 9);
        Assert.assertTrue(Arrays.binarySearchNearest( //
                reverseArray, -4.25, reverseDoubleComparator, RoundingMode.DOWN) == 8);

        Assert.assertTrue(Arrays.binarySearchNearest( //
                reverseList, 8.0, reverseDoubleComparator) == 1);
        Assert.assertTrue(Arrays.binarySearchNearest( //
                reverseList, 0.75, reverseDoubleComparator) == 4);
        Assert.assertTrue(Arrays.binarySearchNearest( //
                reverseList, -7.75, reverseDoubleComparator, RoundingMode.UP) == 9);
        Assert.assertTrue(Arrays.binarySearchNearest( //
                reverseList, -4.25, reverseDoubleComparator, RoundingMode.DOWN) == 8);
    }

    /**
     * Tests all array comparison variants.
     */
    @Test
    public void testCompare() {

        Assert.assertTrue(0 == Arrays.compare(new int[] {}, new int[] {}));
        Assert.assertTrue(0 > Arrays.compare(new int[] { 1, 2, 3 }, new int[] { 1, 2, 3, 0, 0 }));
        Assert.assertTrue(0 < Arrays.compare(new int[] { 1, 2, 3, 0, 0 }, new int[] { 1, 2, 3 }));
        Assert.assertTrue(0 == Arrays.compare(new int[] { 1, 2, 3, 4, 5 }, new int[] { 1, 2, 3, 4, 5 }));
        Assert.assertTrue(0 > Arrays.compare(new int[] { 1, 2, 3, 4, 5 }, new int[] { 1, 2, 3, 6, 6 }));
        Assert.assertTrue(0 < Arrays.compare(new int[] { 1, 2, 3, 6, 6 }, new int[] { 1, 2, 3, 4, 5 }));

        Assert.assertTrue(0 == Arrays.compare(new double[] {}, new double[] {}));
        Assert.assertTrue(0 > Arrays.compare(new double[] { 1, 2, 3 }, new double[] { 1, 2, 3, 0, 0 }));
        Assert.assertTrue(0 < Arrays.compare(new double[] { 1, 2, 3, 0, 0 }, new double[] { 1, 2, 3 }));
        Assert.assertTrue(0 == Arrays.compare(new double[] { 1, 2, 3, 4, 5 }, new double[] { 1, 2, 3, 4, 5 }));
        Assert.assertTrue(0 > Arrays.compare(new double[] { 1, 2, 3, 4, 5 }, new double[] { 1, 2, 3, 6, 6 }));
        Assert.assertTrue(0 < Arrays.compare(new double[] { 1, 2, 3, 6, 6 }, new double[] { 1, 2, 3, 4, 5 }));

        Assert.assertTrue(0 == Arrays.compare(new Integer[] {}, new Integer[] {}));
        Assert.assertTrue(0 > Arrays.compare(new Integer[] { 1, 2, 3 }, new Integer[] { 1, 2, 3, 0, 0 }));
        Assert.assertTrue(0 < Arrays.compare(new Integer[] { 1, 2, 3, 0, 0 }, new Integer[] { 1, 2, 3 }));
        Assert.assertTrue(0 == Arrays.compare(new Integer[] { 1, 2, 3, 4, 5 }, new Integer[] { 1, 2, 3, 4, 5 }));
        Assert.assertTrue(0 > Arrays.compare(new Integer[] { 1, 2, 3, 4, 5 }, new Integer[] { 1, 2, 3, 6, 6 }));
        Assert.assertTrue(0 < Arrays.compare(new Integer[] { 1, 2, 3, 6, 6 }, new Integer[] { 1, 2, 3, 4, 5 }));

        Assert.assertTrue(0 < Arrays.compare( //
                new Double[] { 1.0, 2.0, 3.0, 4.0, 5.0 }, //
                new Double[] { 1.0, 2.0, 3.0, 6.0, 6.0 }, //
                reverseDoubleComparator));
        Assert.assertTrue(0 > Arrays.compare( //
                new Double[] { 1.0, 2.0, 3.0, 6.0, 6.0 }, //
                new Double[] { 1.0, 2.0, 3.0, 4.0, 5.0 }, //
                reverseDoubleComparator));
    }

    /**
     * Tests all array slicing variants.
     */
    @Test
    public void testSlice() {

        Assert.assertTrue(java.util.Arrays.equals(Arrays.slice(new int[] { 1, 2, 3, 4, 5 }, //
                new int[] { 0, 1, 1, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 4 }), //
                new int[] { 1, 2, 2, 3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 5, 5 }));
        Assert.assertTrue(java.util.Arrays.equals(Arrays.slice(new double[] { 1, 2, 3, 4, 5 }, //
                new int[] { 0, 1, 1, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 4 }), //
                new double[] { 1, 2, 2, 3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 5, 5 }));
        Assert.assertTrue(java.util.Arrays.equals(Arrays.slice(new Integer[] { 1, 2, 3, 4, 5 }, //
                new int[] { 0, 1, 1, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 4 }, Integer.class), //
                new Integer[] { 1, 2, 2, 3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 5, 5 }));
    }
}

/**
 * This file is part of the Shared Scientific Toolbox in Java ("this library"). <br />
 * <br />
 * Copyright (C) 2009 The Regents of the University of California <br />
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

package shared.test.stat;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import shared.stat.util.Combinatorics;

/**
 * A class of unit tests for {@link Combinatorics}.
 * 
 * @author Roy Liu
 */
public class CombinatoricsTest {

    /**
     * Default constructor.
     */
    public CombinatoricsTest() {
    }

    /**
     * Tests {@link Combinatorics#partition(int, int, int)}.
     */
    @Test
    public void testPartition() {

        assertTrue(Arrays.deepEquals(Combinatorics.partition(5), //
                new int[][] {
                //
                        { 5 }, //
                        { 1, 4 }, //
                        { 2, 3 }, //
                        { 1, 1, 3 }, //
                        { 1, 2, 2 }, //
                        { 1, 1, 1, 2 }, //
                        { 1, 1, 1, 1, 1 } //
                }) //
        );

        assertTrue(Arrays.deepEquals(Combinatorics.partition(7, 3, 5), //
                new int[][] {
                //
                        { 1, 1, 5 }, //
                        { 1, 2, 4 }, //
                        { 1, 3, 3 }, //
                        { 2, 2, 3 }, //
                        { 1, 1, 1, 4 }, //
                        { 1, 1, 2, 3 }, //
                        { 1, 2, 2, 2 } //
                }) //
        );

        assertTrue(Arrays.deepEquals(Combinatorics.partition(8, 4, 5), //
                new int[][] {
                //
                        { 1, 1, 1, 5 }, //
                        { 1, 1, 2, 4 }, //
                        { 1, 1, 3, 3 }, //
                        { 1, 2, 2, 3 }, //
                        { 2, 2, 2, 2 } //
                }) //
        );

        assertTrue(Arrays.deepEquals(Combinatorics.partition(9, 2, 3), //
                new int[][] {
                //
                        { 1, 8 }, //
                        { 2, 7 }, //
                        { 3, 6 }, //
                        { 4, 5 } //
                }) //
        );

        assertTrue(Arrays.deepEquals(Combinatorics.partition(9, 1, 2), //
                new int[][] {
                //
                { 9 } //
                }) //
        );

        assertTrue(Arrays.deepEquals(Combinatorics.partition(9, 9, 10), //
                new int[][] {
                //
                { 1, 1, 1, 1, 1, 1, 1, 1, 1 } //
                }) //
        );
    }

    /**
     * Tests {@link Combinatorics#orderedPartition(int, int, int)}.
     */
    @Test
    public void testOrderedPartition() {

        assertTrue(Arrays.deepEquals(Combinatorics.orderedPartition(3), //
                new int[][] {
                //
                        { 3 }, //
                        { 0, 3 }, //
                        { 1, 2 }, //
                        { 2, 1 }, //
                        { 3, 0 }, //
                        { 0, 0, 3 }, //
                        { 0, 1, 2 }, //
                        { 0, 2, 1 }, //
                        { 0, 3, 0 }, //
                        { 1, 0, 2 }, //
                        { 1, 1, 1 }, //
                        { 1, 2, 0 }, //
                        { 2, 0, 1 }, //
                        { 2, 1, 0 }, //
                        { 3, 0, 0 } //
                }) //
        );

        assertTrue(Arrays.deepEquals(Combinatorics.orderedPartition(8, 2, 3), //
                new int[][] {
                //
                        { 0, 8 }, //
                        { 1, 7 }, //
                        { 2, 6 }, //
                        { 3, 5 }, //
                        { 4, 4 }, //
                        { 5, 3 }, //
                        { 6, 2 }, //
                        { 7, 1 }, //
                        { 8, 0 } //
                }) //
        );

        assertTrue(Arrays.deepEquals(Combinatorics.orderedPartition(2, 4, 5), //
                new int[][] {
                //
                        { 0, 0, 0, 2 }, //
                        { 0, 0, 1, 1 }, //
                        { 0, 0, 2, 0 }, //
                        { 0, 1, 0, 1 }, //
                        { 0, 1, 1, 0 }, //
                        { 0, 2, 0, 0 }, //
                        { 1, 0, 0, 1 }, //
                        { 1, 0, 1, 0 }, //
                        { 1, 1, 0, 0 }, //
                        { 2, 0, 0, 0 } //
                }) //
        );

        assertTrue(Arrays.deepEquals(Combinatorics.orderedPartition(9, 1, 2), //
                new int[][] {
                //
                { 9 } //
                }) //
        );
    }

    /**
     * Tests {@link Combinatorics#gamma(double)}.
     */
    @Test
    public void testGamma() {

        for (int i = 1, acc = 1; i < 8; i++, acc *= i) {
            assertTrue(Math.abs(acc - Combinatorics.gamma(i + 1)) < 1e-8);
        }
    }
}

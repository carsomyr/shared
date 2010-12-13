/**
 * <p>
 * Copyright (c) 2009 The Regents of the University of California<br>
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

package org.shared.test.stat;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.shared.stat.util.Combinatorics;

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

        Assert.assertTrue(Arrays.deepEquals(Combinatorics.partition(5), //
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

        Assert.assertTrue(Arrays.deepEquals(Combinatorics.partition(7, 3, 5), //
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

        Assert.assertTrue(Arrays.deepEquals(Combinatorics.partition(8, 4, 5), //
                new int[][] {
                        //
                        { 1, 1, 1, 5 }, //
                        { 1, 1, 2, 4 }, //
                        { 1, 1, 3, 3 }, //
                        { 1, 2, 2, 3 }, //
                        { 2, 2, 2, 2 } //
                }) //
        );

        Assert.assertTrue(Arrays.deepEquals(Combinatorics.partition(9, 2, 3), //
                new int[][] {
                        //
                        { 1, 8 }, //
                        { 2, 7 }, //
                        { 3, 6 }, //
                        { 4, 5 } //
                }) //
        );

        Assert.assertTrue(Arrays.deepEquals(Combinatorics.partition(9, 1, 2), //
                new int[][] {
                //
                { 9 } //
                }) //
        );

        Assert.assertTrue(Arrays.deepEquals(Combinatorics.partition(9, 9, 10), //
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

        Assert.assertTrue(Arrays.deepEquals(Combinatorics.orderedPartition(3), //
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

        Assert.assertTrue(Arrays.deepEquals(Combinatorics.orderedPartition(8, 2, 3), //
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

        Assert.assertTrue(Arrays.deepEquals(Combinatorics.orderedPartition(2, 4, 5), //
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

        Assert.assertTrue(Arrays.deepEquals(Combinatorics.orderedPartition(9, 1, 2), //
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
            Assert.assertTrue(Math.abs(acc - Combinatorics.gamma(i + 1)) < 1e-8);
        }
    }
}

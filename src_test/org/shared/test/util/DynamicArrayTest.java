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

import org.junit.Assert;
import org.junit.Test;
import org.shared.util.Arithmetic;
import org.shared.util.Arrays;
import org.shared.util.DynamicArray;
import org.shared.util.DynamicDoubleArray;
import org.shared.util.DynamicIntArray;
import org.shared.util.DynamicLongArray;
import org.shared.util.DynamicObjectArray;

/**
 * A class of unit tests for {@link DynamicArray}.
 * 
 * @author Roy Liu
 */
public class DynamicArrayTest {

    /**
     * Default constructor.
     */
    public DynamicArrayTest() {
    }

    /**
     * Tests implementations of {@link DynamicArray}.
     */
    @Test
    public void testDynamicArray() {

        DynamicIntArray dia = new DynamicIntArray(0);
        DynamicDoubleArray dda = new DynamicDoubleArray(0);
        DynamicLongArray dla = new DynamicLongArray(0);
        DynamicObjectArray<Integer> doa = new DynamicObjectArray<Integer>(Integer.class, 0);

        DynamicArray<?, ?, ?>[] arrays = new DynamicArray[] { dia, dda, dla, doa };

        int nStages = 7;

        for (int i = 0, count = 0; i < nStages; i++) {

            int length = 1 << i;

            for (DynamicArray<?, ?, ?> a : arrays) {
                Assert.assertTrue(a.capacity() == length - 1);
            }

            for (int j = 0; j < length; j++, count++) {

                dia.push(count);
                dda.push(count);
                dla.push(count);
                doa.push(count);
            }
        }

        Assert.assertTrue(java.util.Arrays.equals(dia.values(), Arithmetic.range((1 << nStages) - 1)));
        Assert.assertTrue(java.util.Arrays.equals(dda.values(), Arithmetic.doubleRange((1 << nStages) - 1)));
        Assert.assertTrue(java.util.Arrays.equals(dla.values(), Arithmetic.longRange((1 << nStages) - 1)));
        Assert.assertTrue(java.util.Arrays.equals(doa.values(), Arrays.box(Arithmetic.range((1 << nStages) - 1))));

        for (int i = 0, length = 1 << (nStages - 1); i < length; i++) {

            dia.pop();
            dda.pop();
            dla.pop();
            doa.pop();
        }

        for (int i = nStages - 2; i >= 0; i--) {

            int length = 1 << i;

            for (DynamicArray<?, ?, ?> a : arrays) {
                Assert.assertTrue(a.capacity() == 4 * length - 1);
            }

            for (int j = 0; j < length; j++) {

                dia.pop();
                dda.pop();
                dla.pop();
                doa.pop();
            }
        }
    }
}

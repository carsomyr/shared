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

import org.junit.Test;

import shared.util.Arithmetic;
import shared.util.Arrays;
import shared.util.DynamicArray;
import shared.util.DynamicDoubleArray;
import shared.util.DynamicIntArray;
import shared.util.DynamicLongArray;
import shared.util.DynamicObjectArray;

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

        int nstages = 7;

        for (int i = 0, count = 0; i < nstages; i++) {

            int length = 1 << i;

            for (DynamicArray<?, ?, ?> a : arrays) {
                assertTrue(a.capacity() == length - 1);
            }

            for (int j = 0; j < length; j++, count++) {

                dia.push(count);
                dda.push(count);
                dla.push(count);
                doa.push(count);
            }
        }

        assertTrue(java.util.Arrays.equals(dia.values(), Arithmetic.range((1 << nstages) - 1)));
        assertTrue(java.util.Arrays.equals(dda.values(), Arithmetic.doubleRange((1 << nstages) - 1)));
        assertTrue(java.util.Arrays.equals(dla.values(), Arithmetic.longRange((1 << nstages) - 1)));
        assertTrue(java.util.Arrays.equals(doa.values(), Arrays.box(Arithmetic.range((1 << nstages) - 1))));

        for (int i = 0, length = 1 << (nstages - 1); i < length; i++) {

            dia.pop();
            dda.pop();
            dla.pop();
            doa.pop();
        }

        for (int i = nstages - 2; i >= 0; i--) {

            int length = 1 << i;

            for (DynamicArray<?, ?, ?> a : arrays) {
                assertTrue(a.capacity() == 4 * length - 1);
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

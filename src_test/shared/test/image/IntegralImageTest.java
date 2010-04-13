/**
 * This file is part of the Shared Scientific Toolbox in Java ("this library"). <br />
 * <br />
 * Copyright (C) 2008 The Regents of the University of California <br />
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

package shared.test.image;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import shared.array.RealArray;
import shared.array.Array.IndexingOrder;
import shared.image.IntegralImage;
import shared.util.Arithmetic;

/**
 * A class of unit tests for {@link IntegralImage}.
 * 
 * @author Roy Liu
 */
public class IntegralImageTest {

    /**
     * Default constructor.
     */
    public IntegralImageTest() {
    }

    /**
     * Tests {@link IntegralImage}. Multiple regions are queried and compared against naive baseline summations.
     */
    @Test
    public void testIntegralImage() {

        int baseSize = 64;
        int maxDims = 3;

        int ntrials = 3;
        int nqueries = 128;

        for (int trialIndex = 0; trialIndex < ntrials; trialIndex++) {

            for (int ndims = 1; ndims <= maxDims; ndims++) {

                int[] dims = new int[ndims];

                for (int dim = 0; dim < ndims; dim++) {
                    dims[dim] = baseSize + Arithmetic.nextInt(baseSize);
                }

                int nvalues = Arithmetic.product(dims);

                RealArray mat = new RealArray(Arithmetic.doubleRange(nvalues), //
                        Arithmetic.nextInt(2) == 0 ? IndexingOrder.FAR : IndexingOrder.NEAR, //
                        dims).uMul(1.0 / nvalues);

                IntegralImage ii = new IntegralImage(mat);

                for (int i = 0; i < nqueries; i++) {

                    int[] bounds = new int[2 * ndims];

                    for (int dim = 0; dim < ndims; dim++) {

                        bounds[dim << 1] = Arithmetic.nextInt(dims[dim]);
                        bounds[(dim << 1) + 1] = //
                        bounds[dim << 1] + Arithmetic.nextInt(dims[dim] - bounds[dim << 1]) + 1;
                    }

                    assertTrue(Math.abs(ii.query(bounds) - mat.subarray(bounds).aSum()) < 1e-8);
                }
            }
        }
    }
}

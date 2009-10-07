/**
 * This file is part of the Shared Scientific Toolbox in Java ("this library"). <br />
 * <br />
 * Copyright (C) 2008 Roy Liu, The Regents of the University of California <br />
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

import shared.array.IntegerArray;
import shared.array.RealArray;
import shared.array.Array.IndexingOrder;
import shared.image.IntegralHistogram;
import shared.image.IntegralImage;
import shared.test.Tests;
import shared.util.Arithmetic;

/**
 * A class of unit tests for {@link IntegralHistogram}.
 * 
 * @author Roy Liu
 */
public class IntegralHistogramTest {

    /**
     * Default constructor.
     */
    public IntegralHistogramTest() {
    }

    /**
     * Tests {@link IntegralHistogram}. Multiple regions are queried and compared against naive baseline histograms.
     */
    @Test
    public void testIntegralHistogram() {

        int baseSize = 32;
        int maxDims = 3;

        int baseBins = 4;

        int ntrials = 3;
        int nqueries = 128;

        for (int trialIndex = 0; trialIndex < ntrials; trialIndex++) {

            for (int ndims = 1; ndims <= maxDims; ndims++) {

                int[] dims = new int[ndims];

                for (int dim = 0; dim < ndims; dim++) {
                    dims[dim] = baseSize + Arithmetic.nextInt(baseSize);
                }

                int nvalues = Arithmetic.product(dims);
                int nbins = baseBins + Arithmetic.nextInt(baseBins);

                IndexingOrder order = //
                Arithmetic.nextInt(2) == 0 ? IndexingOrder.FAR : IndexingOrder.NEAR;

                RealArray mat = new RealArray(Arithmetic.doubleRange(nvalues), //
                        order, dims).uMul(1.0 / nvalues);

                double[] values = mat.values();

                IntegerArray memberships = new IntegerArray(order, dims);

                int[] mValues = memberships.values();

                double[][] valuesArr = new double[nbins][];

                for (int i = 0; i < nbins; i++) {
                    valuesArr[i] = new double[mValues.length];
                }

                for (int i = 0, n = mValues.length; i < n; i++) {
                    valuesArr[mValues[i] = Arithmetic.nextInt(nbins)][i] = values[i];
                }

                IntegralImage[] iis = new IntegralImage[nbins];

                for (int i = 0; i < nbins; i++) {
                    iis[i] = new IntegralImage(new RealArray(valuesArr[i], order, dims));
                }

                double[] h = new double[nbins];
                double[] hExpected = new double[nbins];

                IntegralHistogram ih = new IntegralHistogram(mat, memberships, nbins);

                for (int i = 0; i < nqueries; i++) {

                    int[] bounds = new int[2 * ndims];

                    for (int dim = 0; dim < ndims; dim++) {

                        bounds[dim << 1] = Arithmetic.nextInt(dims[dim]);
                        bounds[(dim << 1) + 1] = //
                        bounds[dim << 1] + Arithmetic.nextInt(dims[dim] - bounds[dim << 1]) + 1;
                    }

                    for (int j = 0; j < nbins; j++) {
                        hExpected[j] = iis[j].query(bounds);
                    }

                    assertTrue(Tests.equals(ih.query(h, bounds), hExpected));
                }
            }
        }
    }
}

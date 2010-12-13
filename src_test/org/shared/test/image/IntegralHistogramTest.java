/**
 * <p>
 * Copyright (c) 2008 The Regents of the University of California<br>
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

package org.shared.test.image;

import org.junit.Assert;
import org.junit.Test;
import org.shared.array.Array.IndexingOrder;
import org.shared.array.IntegerArray;
import org.shared.array.RealArray;
import org.shared.image.IntegralHistogram;
import org.shared.image.IntegralImage;
import org.shared.test.Tests;
import org.shared.util.Arithmetic;

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

        int nTrials = 3;
        int nQueries = 128;

        for (int trialIndex = 0; trialIndex < nTrials; trialIndex++) {

            for (int nDims = 1; nDims <= maxDims; nDims++) {

                int[] dims = new int[nDims];

                for (int dim = 0; dim < nDims; dim++) {
                    dims[dim] = baseSize + Arithmetic.nextInt(baseSize);
                }

                int nValues = Arithmetic.product(dims);
                int nBins = baseBins + Arithmetic.nextInt(baseBins);

                IndexingOrder order = Arithmetic.nextInt(2) == 0 ? IndexingOrder.FAR : IndexingOrder.NEAR;

                RealArray mat = new RealArray(Arithmetic.doubleRange(nValues), //
                        order, dims).uMul(1.0 / nValues);

                double[] values = mat.values();

                IntegerArray memberships = new IntegerArray(order, dims);

                int[] mValues = memberships.values();

                double[][] valuesArr = new double[nBins][];

                for (int i = 0; i < nBins; i++) {
                    valuesArr[i] = new double[mValues.length];
                }

                for (int i = 0, n = mValues.length; i < n; i++) {
                    valuesArr[mValues[i] = Arithmetic.nextInt(nBins)][i] = values[i];
                }

                IntegralImage[] iis = new IntegralImage[nBins];

                for (int i = 0; i < nBins; i++) {
                    iis[i] = new IntegralImage(new RealArray(valuesArr[i], order, dims));
                }

                double[] h = new double[nBins];
                double[] hExpected = new double[nBins];

                IntegralHistogram ih = new IntegralHistogram(mat, memberships, nBins);

                for (int i = 0; i < nQueries; i++) {

                    int[] bounds = new int[2 * nDims];

                    for (int dim = 0; dim < nDims; dim++) {

                        bounds[dim << 1] = Arithmetic.nextInt(dims[dim]);
                        bounds[(dim << 1) + 1] = //
                        bounds[dim << 1] + Arithmetic.nextInt(dims[dim] - bounds[dim << 1]) + 1;
                    }

                    for (int j = 0; j < nBins; j++) {
                        hExpected[j] = iis[j].query(bounds);
                    }

                    Assert.assertTrue(Tests.equals(ih.query(h, bounds), hExpected));
                }
            }
        }
    }
}

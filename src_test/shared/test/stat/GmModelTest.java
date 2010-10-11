/**
 * <p>
 * Copyright (c) 2007 The Regents of the University of California<br>
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

package shared.test.stat;

import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import shared.array.RealArray;
import shared.stat.ml.GmComponents;
import shared.stat.ml.GmModel;
import shared.stat.ml.KMeans;

/**
 * A class of unit tests for {@link GmModel}.
 * 
 * @author Roy Liu
 */
public class GmModelTest {

    /**
     * Default constructor.
     */
    public GmModelTest() {
    }

    /**
     * Tests {@link GmModel#train(RealArray, int, double, double)}.
     */
    @Test
    public void testTrain() {

        RealArray input = new RealArray(new double[] {
                //
                1.0, 1.0, 1.0, 1.0, 2.0, 2.0, 2.0, 2.0, 1.0, //
                1.0, 2.0, 4.0, 8.0, 8.0, 4.0, 2.0, 1.0, 1.0, //
                16.0, 8.0, 4.0, 2.0, 4.0, 4.0, 4.0, 4.0, 1.0 //
                }, 3, 9 //
        );

        int nDims = input.size(1) / 2;

        RealArray generated = generatePoints(input, 8192);

        GmComponents gmc = new GmModel().train(generated, input.size(0), 0.0001, 0.000000000001);

        RealArray expected = new RealArray(input.size(0), input.size(1));
        gmc.centers.map(expected, 0, 0, input.size(0), 0, 0, nDims);
        gmc.covariances.clone().uSqrt().map(expected, 0, 0, input.size(0), 0, nDims, nDims);
        gmc.weights.map(expected, 0, 0, input.size(0), 0, 2 * nDims, 1);

        Assert.assertTrue(KMeans.distances(input, expected).rMin(0).rMax(1).singleton() < 0.5);
    }

    /**
     * Draws random samples from a mixture of Gaussians.
     */
    final protected static RealArray generatePoints(RealArray input, int nPoints) {

        Random rnd = new Random(0xadeaddeb);

        int nDims = input.size(1) / 2;

        RealArray weights = input.subarray(0, input.size(0), 2 * nDims, 2 * nDims + 1);
        weights = weights.uMul(1.0 / weights.aSum());
        weights.map(input, 0, 0, weights.size(0), 0, 2 * nDims, 1);

        RealArray means = input.subarray(0, input.size(0), 0, nDims);
        RealArray deviations = input.subarray(0, input.size(0), nDims, 2 * nDims);

        int nGenerated = 0;

        for (int i = 0, n = weights.size(0); i < n; i++) {
            nGenerated += (int) (nPoints * weights.get(i, 0));
        }

        RealArray generated = new RealArray(nGenerated, nDims);

        for (int i = 0, n = weights.size(0), ctr = 0; i < n; i++) {

            for (int j = 0, m = (int) (nPoints * weights.get(i, 0)); j < m; j++, ctr++) {

                for (int k = 0; k < nDims; k++) {
                    generated.set( //
                            rnd.nextGaussian() * deviations.get(i, k) + means.get(i, k), //
                            ctr, k);
                }
            }
        }

        return generated;
    }
}

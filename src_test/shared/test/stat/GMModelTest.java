/**
 * This file is part of the Shared Scientific Toolbox in Java ("this library"). <br />
 * <br />
 * Copyright (C) 2007 Roy Liu <br />
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

import java.util.Random;

import org.junit.Test;

import shared.array.RealArray;
import shared.stat.ml.GMComponents;
import shared.stat.ml.GMModel;
import shared.stat.ml.KMeans;

/**
 * A class of unit tests for {@link GMModel}.
 * 
 * @author Roy Liu
 */
public class GMModelTest {

    /**
     * Default constructor.
     */
    public GMModelTest() {
    }

    /**
     * Tests {@link GMModel#train(RealArray, int, double, double)}.
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

        int ndims = input.size(1) / 2;

        RealArray generated = generatePoints(input, 8192);

        GMComponents gmc = new GMModel().train(generated, input.size(0), 0.0001, 0.000000000001);

        RealArray expected = new RealArray(input.size(0), input.size(1));
        gmc.centers.map(expected, 0, 0, input.size(0), 0, 0, ndims);
        gmc.covariances.clone().uSqrt().map(expected, 0, 0, input.size(0), 0, ndims, ndims);
        gmc.weights.map(expected, 0, 0, input.size(0), 0, 2 * ndims, 1);

        assertTrue(KMeans.distances(input, expected).rMin(0).rMax(1).get(0, 0) < 0.5);
    }

    /**
     * Draws random samples from a mixture of Gaussians.
     */
    final protected static RealArray generatePoints(RealArray input, int npoints) {

        Random rnd = new Random(0xadeaddeb);

        int ndims = input.size(1) / 2;

        RealArray weights = input.subarray(0, input.size(0), 2 * ndims, 2 * ndims + 1);
        weights = weights.uMul(1.0 / weights.aSum());
        weights.map(input, 0, 0, weights.size(0), 0, 2 * ndims, 1);

        RealArray means = input.subarray(0, input.size(0), 0, ndims);
        RealArray deviations = input.subarray(0, input.size(0), ndims, 2 * ndims);

        int ngenerated = 0;

        for (int i = 0, n = weights.size(0); i < n; i++) {
            ngenerated += (int) (npoints * weights.get(i, 0));
        }

        RealArray generated = new RealArray(ngenerated, ndims);

        for (int i = 0, n = weights.size(0), ctr = 0; i < n; i++) {

            for (int j = 0, m = (int) (npoints * weights.get(i, 0)); j < m; j++, ctr++) {

                for (int k = 0; k < ndims; k++) {
                    generated.set( //
                            rnd.nextGaussian() * deviations.get(i, k) + means.get(i, k), //
                            ctr, k);
                }
            }
        }

        return generated;
    }
}

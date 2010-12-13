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

package org.shared.stat.ml;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.shared.array.RealArray;
import org.shared.util.Arithmetic;
import org.shared.util.Control;

/**
 * A class for K-Means clustering with subset furthest first initialization.
 * 
 * @author Roy Liu
 */
public class KMeans {

    /**
     * Computes point-to-point distances.
     * 
     * @param aPts
     *            the origin points.
     * @param bPts
     *            the destination points.
     * @return a {@link RealArray} of distances.
     */
    final public static RealArray distances(RealArray aPts, RealArray bPts) {

        int nDims = Control.checkEquals(aPts.size(1), bPts.size(1));
        int nAPts = aPts.size(0), nBPts = bPts.size(0);

        RealArray acc = new RealArray(nAPts, nBPts);

        for (int dim = 0; dim < nDims; dim++) {
            acc = acc.lAdd(aPts.subarray(0, aPts.size(0), dim, dim + 1).tile(1, nBPts) //
                    .lSub(bPts.subarray(0, bPts.size(0), dim, dim + 1) //
                            .transpose(1, 0).tile(nAPts, 1)).uSqr());
        }

        return acc.uSqrt();
    }

    /**
     * Groups a set of points into the given number of clusters.
     * 
     * @param nClusters
     *            the number of clusters.
     * @param points
     *            the input.
     * @return a {@link List} of clusters.
     */
    final public static List<RealArray> cluster(int nClusters, RealArray points) {

        int nPoints = points.size(0);
        int nDims = points.size(1);

        Control.checkTrue(nPoints >= nClusters);

        // Subset furthest first initialization of centers.
        RealArray centers = subsetFurthestFirst(nClusters, points);

        int[] memberships = org.shared.util.Arrays.newArray(nPoints, -1), newMemberships;

        for (;;) {

            newMemberships = distances(centers, points).iMin(0).subarray(0, 1, 0, points.size(0)).values();

            int nCenters = centers.size(0);

            // Create the clusters.

            int[] counts = new int[nCenters];

            for (int i = 0; i < nPoints; i++) {
                counts[newMemberships[i]]++;
            }

            RealArray[] clusters = new RealArray[nCenters];

            for (int i = 0; i < nCenters; i++) {
                clusters[i] = new RealArray(counts[i], nDims);
            }

            // Build clusters and increment counts.
            counts = new int[nCenters];

            for (int i = 0; i < nPoints; i++) {

                points.map(clusters[newMemberships[i]], //
                        i, counts[newMemberships[i]], 1, //
                        0, 0, nDims);

                counts[newMemberships[i]]++;
            }

            // Compute new centers.

            int nNonzero = 0;

            for (int i = 0; i < nCenters; i++) {

                if (clusters[i].size(0) > 0) {

                    clusters[i].rMean(0).map(centers, //
                            0, nNonzero, 1, //
                            0, 0, nDims);

                    nNonzero++;
                }
            }

            // This should happen VERY rarely.
            for (int i = nNonzero; i < nCenters; i++) {
                centers.subarray(0, i, 0, centers.size(1)).rMean(0).map(centers, //
                        0, i, 1, //
                        0, 0, nDims);
            }

            // Check for convergence and return if necessary.

            if (Arrays.equals(newMemberships, memberships)) {

                return Arrays.asList(clusters);

            } else {

                memberships = newMemberships;
            }
        }
    }

    /**
     * Performs subset furthest first initialization.
     * 
     * @param nCenters
     *            the number of centers.
     * @param points
     *            the collection of points.
     * @return a collection of centers.
     */
    final protected static RealArray subsetFurthestFirst(int nCenters, RealArray points) {

        int nDims = points.size(1);
        int nSamples = Math.min(points.size(0), 2 * nCenters * (int) (Math.log(nCenters + 1) + 1));

        // Make the permutation predictable and sample from a subset.
        int[] perm = Arithmetic.range(points.size(0));
        Collections.shuffle(Arrays.asList(perm), new Random(Arrays.hashCode(points.values())));

        RealArray samples = new RealArray(nSamples, nDims);

        for (int i = 0; i < nSamples; i++) {
            points.map(samples, //
                    perm[i], i, 1, //
                    0, 0, nDims);
        }

        //

        RealArray res = samples.map(new RealArray(nCenters, nDims), //
                0, 0, 1, //
                0, 0, nDims);

        for (int i = 1; i < nCenters; i++) {

            RealArray d = distances(res.subarray(0, i, 0, res.size(1)), samples);

            samples.map(res, //
                    d.rMin(0).iMax(1).get(0, 0), i, 1, //
                    0, 0, nDims);
        }

        return res;
    }

    // Dummy constructor.
    KMeans() {
    }
}

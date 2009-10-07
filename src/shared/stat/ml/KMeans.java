/**
 * This file is part of the Shared Scientific Toolbox in Java ("this library"). <br />
 * <br />
 * Copyright (C) 2007 Roy Liu, The Regents of the University of California <br />
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

package shared.stat.ml;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import shared.array.RealArray;
import shared.util.Arithmetic;
import shared.util.Control;

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

        int ndims = Control.checkEquals(aPts.size(1), bPts.size(1));
        int naPts = aPts.size(0), nbPts = bPts.size(0);

        RealArray acc = new RealArray(naPts, nbPts);

        for (int dim = 0; dim < ndims; dim++) {
            acc = acc.lAdd(aPts.subarray(0, aPts.size(0), dim, dim + 1).tile(1, nbPts) //
                    .lSub(bPts.subarray(0, bPts.size(0), dim, dim + 1) //
                            .transpose(1, 0).tile(naPts, 1)).uSqr());
        }

        return acc.uSqrt();
    }

    /**
     * Groups a set of points into the given number of clusters.
     * 
     * @param nclusters
     *            the number of clusters.
     * @param points
     *            the input.
     * @return a {@link List} of clusters.
     */
    final public static List<RealArray> cluster(int nclusters, RealArray points) {

        int npoints = points.size(0);
        int ndims = points.size(1);

        Control.checkTrue(npoints >= nclusters);

        // Subset furthest first initialization of centers.
        RealArray centers = subsetFurthestFirst(nclusters, points);

        int[] memberships = shared.util.Arrays.newArray(npoints, -1), newMemberships;

        for (;;) {

            newMemberships = distances(centers, points).iMin(0).subarray(0, 1, 0, points.size(0)).values();

            int ncenters = centers.size(0);

            // Create the clusters.

            int[] counts = new int[ncenters];

            for (int i = 0; i < npoints; i++) {
                counts[newMemberships[i]]++;
            }

            RealArray[] clusters = new RealArray[ncenters];

            for (int i = 0; i < ncenters; i++) {
                clusters[i] = new RealArray(counts[i], ndims);
            }

            // Build clusters and increment counts.
            counts = new int[ncenters];

            for (int i = 0; i < npoints; i++) {

                points.map(clusters[newMemberships[i]], //
                        i, counts[newMemberships[i]], 1, //
                        0, 0, ndims);

                counts[newMemberships[i]]++;
            }

            // Compute new centers.

            int nnonzero = 0;

            for (int i = 0; i < ncenters; i++) {

                if (clusters[i].size(0) > 0) {

                    clusters[i].rMean(0).map(centers, //
                            0, nnonzero, 1, //
                            0, 0, ndims);

                    nnonzero++;
                }
            }

            // This should happen VERY rarely.
            for (int i = nnonzero; i < ncenters; i++) {
                centers.subarray(0, i, 0, centers.size(1)).rMean(0).map(centers, //
                        0, i, 1, //
                        0, 0, ndims);
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
     * @param ncenters
     *            the number of centers.
     * @param points
     *            the collection of points.
     * @return a collection of centers.
     */
    final protected static RealArray subsetFurthestFirst(int ncenters, RealArray points) {

        int ndims = points.size(1);
        int nsamples = Math.min(points.size(0), 2 * ncenters * (int) (Math.log(ncenters + 1) + 1));

        // Make the permutation predictable and sample from a subset.
        int[] perm = Arithmetic.range(points.size(0));
        Collections.shuffle(Arrays.asList(perm), new Random(Arrays.hashCode(points.values())));

        RealArray samples = new RealArray(nsamples, ndims);

        for (int i = 0; i < nsamples; i++) {
            points.map(samples, //
                    perm[i], i, 1, //
                    0, 0, ndims);
        }

        //

        RealArray res = samples.map(new RealArray(ncenters, ndims), //
                0, 0, 1, //
                0, 0, ndims);

        for (int i = 1; i < ncenters; i++) {

            RealArray d = distances(res.subarray(0, i, 0, res.size(1)), samples);

            samples.map(res, //
                    d.rMin(0).iMax(1).get(0, 0), i, 1, //
                    0, 0, ndims);
        }

        return res;
    }

    // Dummy constructor.
    KMeans() {
    }
}

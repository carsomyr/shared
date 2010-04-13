/**
 * This file is part of the Shared Scientific Toolbox in Java ("this library"). <br />
 * <br />
 * Copyright (C) 2007 The Regents of the University of California <br />
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

package shared.image;

import shared.array.IntegerArray;
import shared.array.RealArray;
import shared.image.kernel.ImageOps;
import shared.util.Control;

/**
 * A data structure for computing the histogram over any rectangular region quickly.
 * 
 * @author Roy Liu
 */
public class IntegralHistogram extends RealArray {

    final int[] ilut;

    /**
     * Default constructor.
     * 
     * @param src
     *            the {@link RealArray} to integrate over.
     * @param membership
     *            the class membership information.
     * @param nbins
     *            the number of bins.
     */
    public IntegralHistogram(RealArray src, IntegerArray membership, int nbins) {
        super(getDimensionsPlusOne(src, nbins));

        RealArray dst = this;

        int[] srcDims = src.dims();
        int[] dstDims = dst.dims();

        Control.checkTrue(src.order() == membership.order(), //
                "Indexing order mismatch");

        ImageOps.ImKernel.createIntegralHistogram( //
                src.values(), srcDims, src.order().strides(srcDims), membership.values(), //
                dst.values(), dstDims, dst.order().strides(dstDims));

        this.ilut = ImageOps.createILUT(ndims() - 1);
    }

    /**
     * Queries for the values histogram within a rectangular region, whose bounds are expressed in the same way as
     * {@link RealArray#subarray(int...)}.
     * 
     * @see RealArray#subarray(int...)
     */
    public double[] query(double[] res, int... bounds) {

        double[] values = values();

        int ndims = ndims() - 1;
        int stride = ndims + 1;
        int[] ilut = this.ilut;

        int nbins = size(ndims);
        int binStride = stride(ndims);

        for (int binIndex = 0, binOffset = 0; binIndex < nbins; binIndex++, binOffset += binStride) {

            double sum = 0.0;

            for (int i = 0, n = (1 << ndims), offset = 0; i < n; i++, offset += stride) {

                int index = binOffset;

                for (int dim = 0; dim < ndims; dim++) {
                    index += bounds[ilut[offset + dim]] * stride(dim);
                }

                sum += values[index] * ilut[offset + ndims];
            }

            res[binIndex] = sum;
        }

        return res;
    }

    /**
     * Gets the number of bins.
     */
    public int nbins() {
        return size(ndims() - 1);
    }

    /**
     * Gets the original dimensions plus one with an additional "bins" dimension at the end.
     */
    final protected static int[] getDimensionsPlusOne(RealArray arr, int nbins) {

        int ndims = arr.ndims();

        int[] res = new int[ndims + 1];

        for (int dim = 0; dim < ndims; dim++) {
            res[dim] = arr.size(dim) + 1;
        }

        res[ndims] = nbins;

        return res;
    }
}

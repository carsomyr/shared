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

package shared.image;

import shared.array.RealArray;
import shared.image.kernel.ImageOps;

/**
 * A data structure for computing the sum over any rectangular region quickly.
 * 
 * @author Roy Liu
 */
public class IntegralImage extends RealArray {

    final int[] ilut;

    /**
     * Default constructor.
     * 
     * @param src
     *            the {@link RealArray} to integrate over.
     */
    public IntegralImage(RealArray src) {
        super(getDimensionsPlusOne(src));

        RealArray dst = this;

        int[] srcDims = src.dims();
        int[] dstDims = dst.dims();

        ImageOps.ImKernel.createIntegralImage( //
                src.values(), srcDims, src.order().strides(srcDims), //
                dst.values(), dstDims, dst.order().strides(dstDims));

        this.ilut = ImageOps.createILUT(ndims());
    }

    /**
     * Queries for the sum of the values within a rectangular region, whose bounds are expressed in the same way as
     * {@link RealArray#subarray(int...)}.
     * 
     * @see RealArray#subarray(int...)
     */
    public double query(int... bounds) {

        double sum = 0.0;
        double[] values = values();

        int ndims = ndims();
        int stride = ndims + 1;
        int[] ilut = this.ilut;

        for (int i = 0, n = (1 << ndims), offset = 0; i < n; i++, offset += stride) {

            int index = 0;

            for (int dim = 0; dim < ndims; dim++) {
                index += bounds[ilut[offset + dim]] * stride(dim);
            }

            sum += values[index] * ilut[offset + ndims];
        }

        return sum;
    }

    /**
     * Gets the original dimensions plus one.
     */
    final protected static int[] getDimensionsPlusOne(RealArray arr) {

        int ndims = arr.ndims();

        int[] res = new int[ndims];

        for (int dim = 0; dim < ndims; dim++) {
            res[dim] = arr.size(dim) + 1;
        }

        return res;
    }
}

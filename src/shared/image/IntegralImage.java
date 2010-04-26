/**
 * <p>
 * Copyright (C) 2007 The Regents of the University of California<br />
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

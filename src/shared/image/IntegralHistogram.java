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

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

package shared.fft;

import shared.array.AbstractArray;
import shared.array.ComplexArray;
import shared.array.RealArray;
import shared.util.Control;

/**
 * A specialization of {@link FftCache} for {@link RealArray}s and {@link ComplexArray}s.
 * 
 * @apiviz.owns shared.fft.Cacheable
 * @author Roy Liu
 */
public class ConvolutionCache extends FftCache<ComplexArray, RealArray> {

    /**
     * A global instance.
     */
    protected static ConvolutionCache instance = null;

    /**
     * Gets the global instance.
     */
    final public static ConvolutionCache getInstance() {

        if (instance == null) {
            instance = new ConvolutionCache();
        }

        return instance;
    }

    /**
     * Performs convolution in the complex domain.
     * 
     * @param cIm
     *            the already transformed image.
     * @param ker
     *            the kernel.
     * @return the convolution result.
     */
    public ComplexArray convolve(ComplexArray cIm, ComplexArray ker) {

        int[] dimsT = cIm.dims();
        ComplexArray res = (cIm.eMul(ker instanceof Cacheable ? get(ker, dimsT) //
                : createCacheable(ker, dimsT))).ifft();

        int[] dims = res.dims();
        int nDims = dims.length;
        int[] bounds = new int[nDims * 2];

        for (int i = 0, n = nDims - 1; i < n; i++) {
            Control.checkTrue((bounds[2 * i + 1] = dims[i] - ker.size(i) + 1) > 0, //
                    "Invalid kernel size");
        }

        bounds[bounds.length - 1] = 2;

        return res.subarray(bounds);
    }

    /**
     * Performs convolution in the real domain.
     * 
     * @param cIm
     *            the already transformed image.
     * @param ker
     *            the kernel.
     * @return the convolution result.
     */
    public RealArray convolve(ComplexArray cIm, RealArray ker) {

        int[] dimsT = cIm.rifftDimensions();
        RealArray res = (cIm.eMul(ker instanceof Cacheable ? get(ker, dimsT) //
                : createCacheable(ker, dimsT))).rifft();

        int[] dims = res.dims();
        int nDims = dims.length;
        int[] bounds = new int[nDims * 2];

        for (int dim = 0; dim < nDims; dim++) {
            bounds[2 * dim + 1] = dims[dim] - ker.size(dim) + 1;
        }

        return res.subarray(bounds);
    }

    @Override
    protected <A extends AbstractArray<?, ComplexArray, ?, ?>> ComplexArray createCacheable(A array, int[] dims) {

        int nDims = dims.length;
        int[] bounds = new int[nDims * 3];

        for (int dim = 0; dim < nDims; dim++) {
            bounds[3 * dim + 2] = array.size(dim);
        }

        if (array instanceof RealArray) {

            return ((RealArray) array).map(new RealArray(dims), bounds).rfft().uConj();

        } else if (array instanceof ComplexArray) {

            return ((ComplexArray) array).map(new ComplexArray(dims), bounds).fft().uConj();

        } else {

            throw new IllegalArgumentException("Invalid array type");
        }
    }

    /**
     * Pads an image in an extrapolative way.
     * 
     * @param im
     *            the intensity image.
     * @param margins
     *            the padding margins.
     * @return the padded result.
     */
    final public static RealArray pad(RealArray im, int... margins) {

        int nDims = im.nDims();
        int[] dims = im.dims();
        int[] newDims = dims.clone();

        Control.checkTrue(dims.length == margins.length, //
                "Dimensionality mismatch");

        for (int dim = 0; dim < nDims; dim++) {
            newDims[dim] += 2 * margins[dim];
        }

        RealArray res = new RealArray(newDims);

        // Slice for all partitions of dimensions into corners and edges.

        Control.checkTrue(nDims <= 16, //
                "Too many dimensions");

        for (int i = 0, n = (1 << nDims), cdi = 0, edi = 0; i < n; i++, cdi = 0, edi = 0) {

            int nCornerDims = Integer.bitCount(i);
            int nEdgeDims = nDims - nCornerDims;

            int[] cornerDimIndices = new int[nCornerDims];
            int[] edgeDimIndices = new int[nEdgeDims];

            int nEdgeSlices = 0;

            for (int dim = 0; dim < nDims; dim++) {

                if (((i >>> dim) & 0x1) == 0x1) {

                    cornerDimIndices[cdi++] = dim;

                } else {

                    nEdgeSlices += dims[edgeDimIndices[edi++] = dim];
                }
            }

            //

            int nMarginIndices = 0;

            for (int j = 0; j < nCornerDims; j++) {
                nMarginIndices += margins[cornerDimIndices[j]];
            }

            int[] slices = new int[6 * nMarginIndices + 3 * nEdgeSlices];

            for (int j = 0, base = 0, offset = 3 * nMarginIndices; j < nCornerDims; j++) {

                int dim = cornerDimIndices[j];
                int size = dims[dim];
                int margin = margins[dim];

                for (int k = 0; k < margin; k++, base += 3) {

                    slices[base] = k;
                    slices[base + 1] = margin - 1 - k;
                    slices[base + 2] = dim;

                    slices[offset + base] = size - margin + k;
                    slices[offset + base + 1] = size + 2 * margin - 1 - k;
                    slices[offset + base + 2] = dim;
                }
            }

            for (int j = 0, base = 0, offset = 6 * nMarginIndices; j < nEdgeDims; j++) {

                int dim = edgeDimIndices[j];
                int size = dims[dim];
                int margin = margins[dim];

                for (int k = 0; k < size; k++, base += 3) {

                    slices[offset + base] = k;
                    slices[offset + base + 1] = margin + k;
                    slices[offset + base + 2] = dim;
                }
            }

            //

            im.splice(res, slices);
        }

        return res;
    }

    // Dummy constructor.
    ConvolutionCache() {
    }
}

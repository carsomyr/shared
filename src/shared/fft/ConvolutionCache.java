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

package shared.fft;

import shared.array.AbstractArray;
import shared.array.ComplexArray;
import shared.array.RealArray;
import shared.util.Control;

/**
 * A specialization of {@link FFTCache} for {@link RealArray}s and {@link ComplexArray}s.
 * 
 * @apiviz.owns shared.fft.Cacheable
 * @author Roy Liu
 */
public class ConvolutionCache extends FFTCache<ComplexArray, RealArray> {

    /**
     * A global instance.
     */
    protected static ConvolutionCache Instance = null;

    /**
     * Gets the global instance.
     */
    final public static ConvolutionCache getInstance() {

        if (Instance == null) {
            Instance = new ConvolutionCache();
        }

        return Instance;
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
        int ndims = dims.length;
        int[] bounds = new int[ndims * 2];

        for (int i = 0, n = ndims - 1; i < n; i++) {
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
        int ndims = dims.length;
        int[] bounds = new int[ndims * 2];

        for (int dim = 0; dim < ndims; dim++) {
            bounds[2 * dim + 1] = dims[dim] - ker.size(dim) + 1;
        }

        return res.subarray(bounds);
    }

    @Override
    protected <A extends AbstractArray<?, ComplexArray, ?, ?>> ComplexArray createCacheable(A array, int[] dims) {

        int ndims = dims.length;
        int[] bounds = new int[ndims * 3];

        for (int dim = 0; dim < ndims; dim++) {
            bounds[3 * dim + 2] = array.size(dim);
        }

        if (array instanceof RealArray) {

            return ((RealArray) array).map(new RealArray(dims), bounds).rfft().uConj();

        } else if (array instanceof ComplexArray) {

            return ((ComplexArray) array).map(new ComplexArray(dims), bounds).fft().uConj();

        } else {

            throw new RuntimeException("Invalid array type");
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

        int ndims = im.ndims();
        int[] dims = im.dims();
        int[] newDims = dims.clone();

        Control.checkTrue(dims.length == margins.length, //
                "Dimensionality mismatch");

        for (int dim = 0; dim < ndims; dim++) {
            newDims[dim] += 2 * margins[dim];
        }

        RealArray res = new RealArray(newDims);

        // Slice for all partitions of dimensions into corners and edges.

        Control.checkTrue(ndims <= 16, //
                "Too many dimensions");

        for (int i = 0, n = (1 << ndims), cdi = 0, edi = 0; i < n; i++, cdi = 0, edi = 0) {

            int ncornerDims = Integer.bitCount(i);
            int nedgeDims = ndims - ncornerDims;

            int[] cornerDimIndices = new int[ncornerDims];
            int[] edgeDimIndices = new int[nedgeDims];

            int nedgeSlices = 0;

            for (int dim = 0; dim < ndims; dim++) {

                if (((i >>> dim) & 0x1) == 0x1) {

                    cornerDimIndices[cdi++] = dim;

                } else {

                    nedgeSlices += dims[edgeDimIndices[edi++] = dim];
                }
            }

            //

            int nmarginIndices = 0;

            for (int j = 0; j < ncornerDims; j++) {
                nmarginIndices += margins[cornerDimIndices[j]];
            }

            int[] slices = new int[6 * nmarginIndices + 3 * nedgeSlices];

            for (int j = 0, base = 0, offset = 3 * nmarginIndices; j < ncornerDims; j++) {

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

            for (int j = 0, base = 0, offset = 6 * nmarginIndices; j < nedgeDims; j++) {

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

/**
 * This file is part of the Shared Scientific Toolbox in Java ("this library"). <br />
 * <br />
 * Copyright (C) 2008 Roy Liu, The Regents of the University of California <br />
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

package shared.image.kernel;

import java.util.Arrays;

import shared.array.kernel.MappingOps;
import shared.util.Control;

/**
 * A class for image processing operations in pure Java.
 * 
 * @author Roy Liu
 */
public class ImageOps {

    /**
     * The image processing kernel.
     */
    public static ModalImageKernel ImKernel = new ModalImageKernel();

    /**
     * Creates an index lookup table for speedy index calculations.
     */
    final public static int[] createILUT(int ndims) {

        int stride = ndims + 1;
        int[] ilut = new int[(1 << ndims) * stride];

        for (int i = 0, n = (1 << ndims), offset = 0, parity = ndims % 2; i < n; i++, offset += stride) {

            for (int dim = 0; dim < ndims; dim++) {
                ilut[offset + dim] = (dim << 1) + ((i >>> dim) & 0x1);
            }

            ilut[offset + ndims] = 1 - (((Integer.bitCount(i) + parity) % 2) << 1);
        }

        return ilut;
    }

    /**
     * Supports {@link JavaImageKernel#createIntegralImage(double[], int[], int[], double[], int[], int[])}.
     */
    final public static void createIntegralImage( //
            double[] srcV, int[] srcD, int[] srcS, //
            double[] dstV, int[] dstD, int[] dstS) {

        int ndims = srcD.length;

        Control.checkTrue(ndims == srcS.length //
                && ndims == dstD.length //
                && ndims == dstS.length);

        int srcLen = MappingOps.checkDimensions(srcV.length, srcD, srcS);
        int dstLen = MappingOps.checkDimensions(dstV.length, dstD, dstS);

        int dstOffset = 0;

        for (int dim = 0; dim < ndims; dim++) {

            Control.checkTrue(srcD[dim] + 1 == dstD[dim], //
                    "Dimension mismatch");

            dstOffset += dstS[dim];
        }

        if (srcLen == 0) {
            return;
        }

        int[] srcIndices = MappingOps.assignMappingIndices(srcLen, srcD, srcS);
        int[] dstIndices = MappingOps.assignMappingIndices(srcLen, srcD, dstS);

        for (int i = 0; i < srcLen; i++) {
            dstV[dstIndices[i] + dstOffset] = srcV[srcIndices[i]];
        }

        //

        dstIndices = MappingOps.assignMappingIndices(dstLen, dstD, dstS);

        for (int dim = 0, indexBlockIncrement = dstLen; dim < ndims; indexBlockIncrement /= dstD[dim++]) {

            int size = dstD[dim];
            int stride = dstS[dim];

            for (int lower = 0, upper = indexBlockIncrement / size; //
            lower < dstLen; //
            lower += indexBlockIncrement, upper += indexBlockIncrement) {

                for (int indexIndex = lower; indexIndex < upper; indexIndex++) {

                    double acc = 0.0;

                    for (int k = 0, physical = dstIndices[indexIndex]; k < size; k++, physical += stride) {

                        acc += dstV[physical];
                        dstV[physical] = acc;
                    }
                }
            }
        }
    }

    /**
     * Supports {@link JavaImageKernel#createIntegralHistogram(double[], int[], int[], int[], double[], int[], int[])}.
     */
    final public static void createIntegralHistogram( //
            double[] srcV, int[] srcD, int[] srcS, int[] memV, //
            double[] dstV, int[] dstD, int[] dstS) {

        int memLen = memV.length;
        int ndims = srcD.length;

        Control.checkTrue(ndims == srcS.length //
                && ndims + 1 == dstD.length //
                && ndims + 1 == dstS.length //
                && memLen == srcV.length);

        int[] dstDModified = Arrays.copyOf(dstD, ndims);
        int[] dstSModified = Arrays.copyOf(dstS, ndims);

        int srcLen = MappingOps.checkDimensions(srcV.length, srcD, srcS);
        int dstLen = MappingOps.checkDimensions(dstV.length, dstD, dstS);

        int dstOffset = 0;

        for (int dim = 0; dim < ndims; dim++) {

            Control.checkTrue(srcD[dim] + 1 == dstD[dim], //
                    "Dimension mismatch");

            dstOffset += dstS[dim];
        }

        if (srcLen == 0) {
            return;
        }

        int[] srcIndices = MappingOps.assignMappingIndices(srcLen, srcD, srcS);
        int[] dstIndices = MappingOps.assignMappingIndices(srcLen, srcD, dstSModified);

        int nbins = dstD[ndims];
        int binStride = dstS[ndims];
        int dstLenModified = dstLen / nbins;

        for (int i = 0; i < srcLen; i++) {

            int index = memV[srcIndices[i]];

            Control.checkTrue(index >= 0 && index < nbins, //
                    "Invalid membership index");

            dstV[dstIndices[i] + dstOffset + index * binStride] = srcV[srcIndices[i]];
        }

        //

        dstIndices = MappingOps.assignMappingIndices(dstLenModified, dstDModified, dstSModified);

        for (int dim = 0, indexBlockIncrement = dstLenModified; dim < ndims; indexBlockIncrement /= dstDModified[dim++]) {

            int size = dstDModified[dim];
            int stride = dstSModified[dim];

            for (int lower = 0, upper = indexBlockIncrement / size; //
            lower < dstLenModified; //
            lower += indexBlockIncrement, upper += indexBlockIncrement) {

                for (int indexIndex = lower; indexIndex < upper; indexIndex++) {

                    for (int binIndex = 0, binOffset = 0; binIndex < nbins; binIndex++, binOffset += binStride) {

                        double acc = 0.0;

                        for (int k = 0, physical = dstIndices[indexIndex] + binOffset; k < size; k++, physical += stride) {

                            acc += dstV[physical];
                            dstV[physical] = acc;
                        }
                    }
                }
            }
        }
    }

    // Dummy constructor.
    ImageOps() {
    }
}

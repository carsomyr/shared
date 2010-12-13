/**
 * <p>
 * Copyright (c) 2008 The Regents of the University of California<br>
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

package org.shared.image.kernel;

import java.util.Arrays;

import org.shared.array.kernel.MappingOps;
import org.shared.util.Control;

/**
 * A class for image processing operations in pure Java.
 * 
 * @author Roy Liu
 */
public class ImageOps {

    /**
     * The image processing kernel.
     */
    public static ModalImageKernel imKernel = new ModalImageKernel();

    /**
     * Creates an index lookup table for speedy index calculations.
     */
    final public static int[] createIlut(int nDims) {

        int stride = nDims + 1;
        int[] ilut = new int[(1 << nDims) * stride];

        for (int i = 0, n = (1 << nDims), offset = 0, parity = nDims % 2; i < n; i++, offset += stride) {

            for (int dim = 0; dim < nDims; dim++) {
                ilut[offset + dim] = (dim << 1) + ((i >>> dim) & 0x1);
            }

            ilut[offset + nDims] = 1 - (((Integer.bitCount(i) + parity) % 2) << 1);
        }

        return ilut;
    }

    /**
     * Supports {@link JavaImageKernel#createIntegralImage(double[], int[], int[], double[], int[], int[])}.
     */
    final public static void createIntegralImage( //
            double[] srcV, int[] srcD, int[] srcS, //
            double[] dstV, int[] dstD, int[] dstS) {

        int nDims = srcD.length;

        Control.checkTrue(nDims == srcS.length //
                && nDims == dstD.length //
                && nDims == dstS.length);

        int srcLen = MappingOps.checkDimensions(srcV.length, srcD, srcS);
        int dstLen = MappingOps.checkDimensions(dstV.length, dstD, dstS);

        int dstOffset = 0;

        for (int dim = 0; dim < nDims; dim++) {

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

        for (int dim = 0, indexBlockIncrement = dstLen; dim < nDims; indexBlockIncrement /= dstD[dim++]) {

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
        int nDims = srcD.length;

        Control.checkTrue(nDims == srcS.length //
                && nDims + 1 == dstD.length //
                && nDims + 1 == dstS.length //
                && memLen == srcV.length);

        int[] dstDModified = Arrays.copyOf(dstD, nDims);
        int[] dstSModified = Arrays.copyOf(dstS, nDims);

        int srcLen = MappingOps.checkDimensions(srcV.length, srcD, srcS);
        int dstLen = MappingOps.checkDimensions(dstV.length, dstD, dstS);

        int dstOffset = 0;

        for (int dim = 0; dim < nDims; dim++) {

            Control.checkTrue(srcD[dim] + 1 == dstD[dim], //
                    "Dimension mismatch");

            dstOffset += dstS[dim];
        }

        if (srcLen == 0) {
            return;
        }

        int[] srcIndices = MappingOps.assignMappingIndices(srcLen, srcD, srcS);
        int[] dstIndices = MappingOps.assignMappingIndices(srcLen, srcD, dstSModified);

        int nBins = dstD[nDims];
        int binStride = dstS[nDims];
        int dstLenModified = dstLen / nBins;

        for (int i = 0; i < srcLen; i++) {

            int index = memV[srcIndices[i]];

            Control.checkTrue(index >= 0 && index < nBins, //
                    "Invalid membership index");

            dstV[dstIndices[i] + dstOffset + index * binStride] = srcV[srcIndices[i]];
        }

        //

        dstIndices = MappingOps.assignMappingIndices(dstLenModified, dstDModified, dstSModified);

        for (int dim = 0, indexBlockIncrement = dstLenModified; //
        dim < nDims; //
        indexBlockIncrement /= dstDModified[dim++]) {

            int size = dstDModified[dim];
            int stride = dstSModified[dim];

            for (int lower = 0, upper = indexBlockIncrement / size; //
            lower < dstLenModified; //
            lower += indexBlockIncrement, upper += indexBlockIncrement) {

                for (int indexIndex = lower; indexIndex < upper; indexIndex++) {

                    for (int binIndex = 0, binOffset = 0; binIndex < nBins; binIndex++, binOffset += binStride) {

                        double acc = 0.0;

                        for (int k = 0, physical = dstIndices[indexIndex] + binOffset; //
                        k < size; //
                        k++, physical += stride) {

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

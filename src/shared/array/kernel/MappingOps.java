/**
 * <p>
 * Copyright (c) 2007 Roy Liu<br>
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

package shared.array.kernel;

import java.lang.reflect.Array;
import java.util.Arrays;

import shared.util.Control;

/**
 * A class for mapping operations in pure Java.
 * 
 * @author Roy Liu
 */
public class MappingOps {

    /**
     * Creates an array of physical indices.
     * 
     * @param nIndices
     *            the number of indices.
     * @param dims
     *            the mapping dimensions.
     * @param strides
     *            the strides.
     * @return the physical indices.
     */
    final public static int[] assignMappingIndices(int nIndices, int[] dims, int[] strides) {

        int[] indices = new int[nIndices];

        for (int k = dims.length - 1, blockSize = 1, stride, size; k >= 0; blockSize *= size, k--) {

            stride = strides[k];
            size = dims[k];

            for (int offset = blockSize, m = blockSize * size; offset < m; offset += blockSize) {

                for (int i = offset - blockSize, j = offset; i < offset; i++, j++) {
                    indices[j] = indices[i] + stride;
                }
            }
        }

        return indices;
    }

    /**
     * Creates an array of physical slicing indices.
     * 
     * @param nIndices
     *            the number of indices.
     * @param strides
     *            the strides.
     * @param sliceIndices
     *            the indices to slice on arranged by dimension.
     * @return the physical indices.
     */
    final public static int[] assignSlicingIndices(int nIndices, int[] strides, int[][] sliceIndices) {

        int[] indices = new int[nIndices];

        for (int i = 0, n = strides.length; i < n; i++) {
            indices[0] += strides[i] * sliceIndices[i][0];
        }

        for (int k = strides.length - 1, blockSize = 1, strideOffset, size; k >= 0; blockSize *= size, k--) {

            int[] arr = sliceIndices[k];
            size = arr.length;

            for (int offset = blockSize, m = blockSize * size, n = 1; offset < m; offset += blockSize, n++) {

                strideOffset = strides[k] * (arr[n] - arr[n - 1]);

                for (int i = offset - blockSize, j = offset; i < offset; i++, j++) {
                    indices[j] = indices[i] + strideOffset;
                }
            }
        }

        return indices;
    }

    /**
     * Checks an array's dimensions and strides.
     * 
     * @param len
     *            the array length.
     * @param dims
     *            the dimensions.
     * @param strides
     *            the strides.
     * @return the array length.
     */
    final public static int checkDimensions(int len, int[] dims, int[] strides) {

        int acc = 0;

        for (int dim = 0, nDims = dims.length; dim < nDims; dim++) {

            Control.checkTrue(dims[dim] >= 0 && strides[dim] >= 0, //
                    "Invalid dimensions and/or strides");

            acc += (dims[dim] - 1) * strides[dim];
        }

        Control.checkTrue(acc == len - 1, //
                "Invalid dimensions and/or strides");

        return len;
    }

    /**
     * Assigns source values to destination values based on arrays of physical indices.
     * 
     * @param srcV
     *            the source array.
     * @param srcIndices
     *            the source indices.
     * @param dstV
     *            the destination array.
     * @param dstIndices
     *            the destination indices.
     */
    final public static void assign(Object srcV, int[] srcIndices, Object dstV, int[] dstIndices) {

        int nIndices = Control.checkEquals(srcIndices.length, dstIndices.length, //
                "Invalid arguments");

        if (srcV instanceof double[] && dstV instanceof double[]) {

            double[] srcVArr = (double[]) srcV;
            double[] dstVArr = (double[]) dstV;

            for (int i = 0; i < nIndices; i++) {
                dstVArr[dstIndices[i]] = srcVArr[srcIndices[i]];
            }

        } else if (srcV instanceof int[] && dstV instanceof int[]) {

            int[] srcVArr = (int[]) srcV;
            int[] dstVArr = (int[]) dstV;

            for (int i = 0; i < nIndices; i++) {
                dstVArr[dstIndices[i]] = srcVArr[srcIndices[i]];
            }

        } else if (srcV instanceof Object[] && dstV instanceof Object[]) {

            Control.checkTrue(dstV.getClass().isAssignableFrom(srcV.getClass()), //
                    "Invalid array types");

            Object[] srcVArr = (Object[]) srcV;
            Object[] dstVArr = (Object[]) dstV;

            for (int i = 0; i < nIndices; i++) {
                dstVArr[dstIndices[i]] = srcVArr[srcIndices[i]];
            }

        } else {

            throw new IllegalArgumentException("Invalid arguments");
        }
    }

    /**
     * A mapping operation in support of {@link JavaArrayKernel#map(int[], Object, int[], int[], Object, int[], int[])}.
     */
    final public static void map(int[] bounds, //
            Object srcV, int[] srcD, int[] srcS, //
            Object dstV, int[] dstD, int[] dstS) {

        int nDims = srcD.length;

        Control.checkTrue(nDims == srcS.length //
                && nDims == dstD.length //
                && nDims == dstS.length //
                && 3 * nDims == bounds.length, //
                "Invalid arguments");

        int srcLen = checkDimensions(Array.getLength(srcV), srcD, srcS);
        int dstLen = checkDimensions(Array.getLength(dstV), dstD, dstS);

        int mapLen = 1;

        for (int dim = 0, offset = 0; dim < nDims; dim++, offset += 3) {

            int size = bounds[offset + 2];

            Control.checkTrue(size >= 0, //
                    "Invalid mapping parameters");

            mapLen *= size;
        }

        if (srcLen == 0 || dstLen == 0) {
            return;
        }

        int[][] ssi = new int[nDims][];
        int[][] dsi = new int[nDims][];

        for (int dim = 0, offset = 0; dim < nDims; dim++, offset += 3) {

            int mapSize = bounds[offset + 2];

            int[] srcSlices = (ssi[dim] = new int[mapSize]);
            int[] dstSlices = (dsi[dim] = new int[mapSize]);

            for (int j = 0, //
            srcSize = srcD[dim], //
            srcOffset = (((bounds[offset]) % srcSize) + srcSize) % srcSize, //
            dstSize = dstD[dim], //
            dstOffset = (((bounds[offset + 1]) % dstSize) + dstSize) % dstSize; //
            j < mapSize; //
            j++, //
            srcOffset = (srcOffset + 1) % srcSize, //
            dstOffset = (dstOffset + 1) % dstSize) {

                srcSlices[j] = srcOffset;
                dstSlices[j] = dstOffset;
            }
        }

        if (mapLen == 0) {
            return;
        }

        int[] srcIndices = assignSlicingIndices(mapLen, srcS, ssi);
        int[] dstIndices = assignSlicingIndices(mapLen, dstS, dsi);

        assign(srcV, srcIndices, dstV, dstIndices);
    }

    /**
     * A slicing operation in support of
     * {@link JavaArrayKernel#slice(int[], Object, int[], int[], Object, int[], int[])}.
     */
    final public static void slice( //
            int[] slices, //
            Object srcV, int[] srcD, int[] srcS, //
            Object dstV, int[] dstD, int[] dstS) {

        Control.checkTrue(slices.length % 3 == 0, //
                "Invalid slicing specification");

        int nSlices = slices.length / 3;
        int nDims = srcD.length;

        Control.checkTrue(nDims == srcS.length //
                && nDims == dstD.length //
                && nDims == dstS.length);

        checkDimensions(Array.getLength(srcV), srcD, srcS);
        checkDimensions(Array.getLength(dstV), dstD, dstS);

        for (int i = 0, n = 3 * nSlices; i < n; i += 3) {

            int srcIndex = slices[i];
            int dstIndex = slices[i + 1];
            int dim = slices[i + 2];

            Control.checkTrue(dim >= 0 && dim < nDims, //
                    "Invalid dimension");

            Control.checkTrue((srcIndex >= 0 && srcIndex < srcD[dim]) //
                    && (dstIndex >= 0 && dstIndex < dstD[dim]), //
                    "Invalid index");
        }

        int[] dimCounts = new int[nDims];

        for (int i = 0, n = 3 * nSlices; i < n; i += 3) {
            dimCounts[slices[i + 2]]++;
        }

        int nIndices = 1;

        int[][] ssi = new int[nDims][];
        int[][] dsi = new int[nDims][];

        for (int dim = 0; dim < nDims; dim++) {

            ssi[dim] = new int[dimCounts[dim]];
            dsi[dim] = new int[dimCounts[dim]];

            nIndices *= dimCounts[dim];
        }

        Arrays.fill(dimCounts, 0);

        for (int i = 0, n = 3 * nSlices; i < n; i += 3) {

            int dim = slices[i + 2];
            int idx = dimCounts[dim]++;

            ssi[dim][idx] = slices[i];
            dsi[dim][idx] = slices[i + 1];
        }

        if (nIndices == 0) {
            return;
        }

        int[] srcIndices = assignSlicingIndices(nIndices, srcS, ssi);
        int[] dstIndices = assignSlicingIndices(nIndices, dstS, dsi);

        assign(srcV, srcIndices, dstV, dstIndices);
    }

    // Dummy constructor.
    MappingOps() {
    }
}

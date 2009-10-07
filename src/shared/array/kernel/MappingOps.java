/**
 * This file is part of the Shared Scientific Toolbox in Java ("this library"). <br />
 * <br />
 * Copyright (C) 2007 Roy Liu <br />
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
     * @param nindices
     *            the number of indices.
     * @param dims
     *            the mapping dimensions.
     * @param strides
     *            the strides.
     * @return the physical indices.
     */
    final public static int[] assignMappingIndices(int nindices, int[] dims, int[] strides) {

        int[] indices = new int[nindices];

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
     * @param nindices
     *            the number of indices.
     * @param strides
     *            the strides.
     * @param sliceIndices
     *            the indices to slice on arranged by dimension.
     * @return the physical indices.
     */
    final public static int[] assignSlicingIndices(int nindices, int[] strides, int[][] sliceIndices) {

        int[] indices = new int[nindices];

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

        for (int dim = 0, ndims = dims.length; dim < ndims; dim++) {

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

        int nindices = Control.checkEquals(srcIndices.length, dstIndices.length, //
                "Invalid arguments");

        if (srcV instanceof double[] && dstV instanceof double[]) {

            double[] srcVArr = (double[]) srcV;
            double[] dstVArr = (double[]) dstV;

            for (int i = 0; i < nindices; i++) {
                dstVArr[dstIndices[i]] = srcVArr[srcIndices[i]];
            }

        } else if (srcV instanceof int[] && dstV instanceof int[]) {

            int[] srcVArr = (int[]) srcV;
            int[] dstVArr = (int[]) dstV;

            for (int i = 0; i < nindices; i++) {
                dstVArr[dstIndices[i]] = srcVArr[srcIndices[i]];
            }

        } else if (srcV instanceof Object[] && dstV instanceof Object[]) {

            Control.checkTrue(dstV.getClass().isAssignableFrom(srcV.getClass()), //
                    "Invalid array types");

            Object[] srcVArr = (Object[]) srcV;
            Object[] dstVArr = (Object[]) dstV;

            for (int i = 0; i < nindices; i++) {
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

        int ndims = srcD.length;

        Control.checkTrue(ndims == srcS.length //
                && ndims == dstD.length //
                && ndims == dstS.length //
                && 3 * ndims == bounds.length, //
                "Invalid arguments");

        int srcLen = checkDimensions(Array.getLength(srcV), srcD, srcS);
        int dstLen = checkDimensions(Array.getLength(dstV), dstD, dstS);

        int nslices = 0;
        int mapLen = 1;

        for (int dim = 0, offset = 0; dim < ndims; dim++, offset += 3) {

            int size = bounds[offset + 2];

            Control.checkTrue(size >= 0, //
                    "Invalid mapping parameters");

            nslices += size;
            mapLen *= size;
        }

        if (srcLen == 0 || dstLen == 0) {
            return;
        }

        int[][] ssi = new int[ndims][];
        int[][] dsi = new int[ndims][];

        for (int dim = 0, acc = 0, offset = 0; dim < ndims; dim++, acc += bounds[offset + 2], offset += 3) {

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

        int nslices = slices.length / 3;
        int ndims = srcD.length;

        Control.checkTrue(ndims == srcS.length //
                && ndims == dstD.length //
                && ndims == dstS.length);

        checkDimensions(Array.getLength(srcV), srcD, srcS);
        checkDimensions(Array.getLength(dstV), dstD, dstS);

        for (int i = 0, n = 3 * nslices; i < n; i += 3) {

            int srcIndex = slices[i];
            int dstIndex = slices[i + 1];
            int dim = slices[i + 2];

            Control.checkTrue(dim >= 0 && dim < ndims, //
                    "Invalid dimension");

            Control.checkTrue((srcIndex >= 0 && srcIndex < srcD[dim]) //
                    && (dstIndex >= 0 && dstIndex < dstD[dim]), //
                    "Invalid index");
        }

        int[] dimCounts = new int[ndims];

        for (int i = 0, n = 3 * nslices; i < n; i += 3) {
            dimCounts[slices[i + 2]]++;
        }

        int nindices = 1;

        int[][] ssi = new int[ndims][];
        int[][] dsi = new int[ndims][];

        for (int dim = 0; dim < ndims; dim++) {

            ssi[dim] = new int[dimCounts[dim]];
            dsi[dim] = new int[dimCounts[dim]];

            nindices *= dimCounts[dim];
        }

        Arrays.fill(dimCounts, 0);

        for (int i = 0, n = 3 * nslices; i < n; i += 3) {

            int dim = slices[i + 2];
            int idx = dimCounts[dim]++;

            ssi[dim][idx] = slices[i];
            dsi[dim][idx] = slices[i + 1];
        }

        if (nindices == 0) {
            return;
        }

        int[] srcIndices = assignSlicingIndices(nindices, srcS, ssi);
        int[] dstIndices = assignSlicingIndices(nindices, dstS, dsi);

        assign(srcV, srcIndices, dstV, dstIndices);
    }

    // Dummy constructor.
    MappingOps() {
    }
}

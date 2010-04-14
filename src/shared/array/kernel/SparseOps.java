/**
 * This file is part of the Shared Scientific Toolbox in Java ("this library"). <br />
 * <br />
 * Copyright (C) 2009 Roy Liu <br />
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

import shared.array.sparse.SparseArrayState;
import shared.util.Arithmetic;
import shared.util.Control;

/**
 * A class for sparse array operations in pure Java.
 * 
 * @author Roy Liu
 */
public class SparseOps {

    /**
     * An insertion operation in support of
     * {@link JavaArrayKernel#insertSparse(Object, int[], int[], int[], int[], Object, int[])}.
     */
    @SuppressWarnings("unchecked")
    final public static <V> SparseArrayState<V> insert( //
            V oldV, int[] oldD, int[] oldS, int[] oldDO, int[] oldI, //
            V newV, int[] newI) {

        int ndims = oldD.length;
        int oldLen = Array.getLength(oldV);
        int newLen = Array.getLength(newV);

        Control.checkTrue(ndims == oldS.length //
                && oldLen == oldI.length //
                && newLen == newI.length, //
                "Invalid arguments");

        MappingOps.checkDimensions(Arithmetic.product(oldD), oldD, oldS);

        for (int dim = 0; dim < ndims; dim++) {
            Control.checkTrue(oldDO[dim + 1] - oldDO[dim] - 1 == oldD[dim], //
                    "Invalid arguments");
        }

        PermutationEntry<Integer>[] entries = new PermutationEntry[newLen];

        for (int i = 0; i < newLen; i++) {
            entries[i] = new PermutationEntry<Integer>(newI[i], i);
        }

        Arrays.sort(entries);

        int[] perm = new int[newLen];

        for (int i = 0; i < newLen; i++) {

            newI[i] = entries[i].getValue();
            perm[i] = entries[i].getOrder();
        }

        for (int i = 1; i < newLen; i++) {
            Control.checkTrue(entries[i - 1].getValue() != entries[i].getValue(), //
                    "Duplicate values are not allowed");
        }

        int[][] mergeResult = merge( //
                oldI, Arithmetic.range(oldLen), //
                newI, Arithmetic.range(newLen), //
                oldD, oldS, oldDO);

        int[] newIndirections = mergeResult[3];

        for (int i = 0; i < newLen; i++) {
            perm[i] = newIndirections[perm[i]];
        }

        System.arraycopy(perm, 0, newIndirections, 0, newLen);

        return assign(oldV, newV, mergeResult);
    }

    /**
     * A slicing operation in support of
     * {@link JavaArrayKernel#sliceSparse(int[], Object, int[], int[], int[], int[], int[], int[], Object, int[], int[], int[], int[], int[], int[])}
     * .
     */
    final public static <V> SparseArrayState<V> slice(int[] slices, //
            V srcV, int[] srcD, int[] srcS, int[] srcDO, //
            int[] srcI, int[] srcIO, int[] srcII, //
            V dstV, int[] dstD, int[] dstS, int[] dstDO, //
            int[] dstI, int[] dstIO, int[] dstII) {

        int nslices = slices.length / 3;
        int ndims = srcD.length;
        int srcLen = Array.getLength(srcV);
        int dstLen = Array.getLength(dstV);

        Control.checkTrue(ndims == srcS.length //
                && ndims == dstD.length //
                && ndims == dstS.length //
                && slices.length % 3 == 0 //
                && srcLen == srcI.length //
                && dstLen == dstI.length //
                && srcDO.length == ndims + 1 //
                && srcIO.length == Arithmetic.sum(srcD) + ndims //
                && srcII.length == ndims * srcLen //
                && dstDO.length == ndims + 1 //
                && dstIO.length == Arithmetic.sum(dstD) + ndims //
                && dstII.length == ndims * dstLen, //
                "Invalid arguments");

        MappingOps.checkDimensions(Arithmetic.product(srcD), srcD, srcS);
        MappingOps.checkDimensions(Arithmetic.product(dstD), dstD, dstS);

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

        for (int dim = 0; dim < ndims; dim++) {
            Control.checkTrue((srcDO[dim + 1] - srcDO[dim] - 1 == srcD[dim]) //
                    && (dstDO[dim + 1] - dstDO[dim] - 1 == dstD[dim]), //
                    "Invalid arguments");
        }

        //

        int offsetArrayLen = srcDO[ndims];

        int[] srcSliceCounts = new int[ndims];
        int[] lookupCounts = new int[offsetArrayLen];

        Arrays.fill(srcSliceCounts, 0);
        Arrays.fill(lookupCounts, 0);

        for (int i = 0, n = 3 * nslices; i < n; i += 3) {

            int srcIndex = slices[i];
            int dim = slices[i + 2];

            srcSliceCounts[dim]++;
            lookupCounts[srcDO[dim] + srcIndex]++;
        }

        //

        int[] sliceOffsets = new int[ndims + 1];
        int[] lookupOffsets = new int[offsetArrayLen];

        int sliceOffset = 0;
        int lookupOffset = 0;

        for (int dim = 0; dim < ndims; dim++) {

            sliceOffsets[dim] = sliceOffset;
            sliceOffset += srcSliceCounts[dim];

            int dimSize = srcD[dim];

            for (int dimIndex = 0; dimIndex < dimSize; dimIndex++) {

                lookupOffsets[srcDO[dim] + dimIndex] = lookupOffset;
                lookupOffset += lookupCounts[srcDO[dim] + dimIndex];
            }

            lookupOffsets[srcDO[dim] + dimSize] = lookupOffset;
        }

        sliceOffsets[ndims] = sliceOffset;

        //

        int[] srcSlices = new int[sliceOffset];
        int[] dstSlices = new int[sliceOffset];
        int[] dstSliceCounts = new int[ndims];
        int[] dstLookups = new int[lookupOffset];

        Arrays.fill(srcSliceCounts, 0);
        Arrays.fill(lookupCounts, 0);

        for (int i = 0, n = 3 * nslices; i < n; i += 3) {

            int srcIndex = slices[i];
            int dstIndex = slices[i + 1];
            int dim = slices[i + 2];

            srcSlices[sliceOffsets[dim] + srcSliceCounts[dim]] = srcIndex;
            dstSlices[sliceOffsets[dim] + srcSliceCounts[dim]] = dstIndex;
            dstLookups[lookupOffsets[srcDO[dim] + srcIndex] //
                    + lookupCounts[srcDO[dim] + srcIndex]] = dstIndex;

            srcSliceCounts[dim]++;
            lookupCounts[srcDO[dim] + srcIndex]++;
        }

        //

        Arrays.fill(srcSliceCounts, 0);
        Arrays.fill(lookupCounts, 0);

        for (int dim = 0; dim < ndims; dim++) {

            srcSliceCounts[dim] = normalize(srcSlices, sliceOffsets[dim], sliceOffsets[dim + 1]);
            dstSliceCounts[dim] = normalize(dstSlices, sliceOffsets[dim], sliceOffsets[dim + 1]);

            for (int dimIndex = 0, dimSize = srcD[dim]; dimIndex < dimSize; dimIndex++) {
                lookupCounts[srcDO[dim] + dimIndex] = normalize(dstLookups, //
                        lookupOffsets[srcDO[dim] + dimIndex], //
                        lookupOffsets[srcDO[dim] + dimIndex + 1]);
            }
        }

        //

        int[] srcIndirections = getSlicedIndirections(sliceOffsets, srcSliceCounts, srcSlices, //
                srcDO, srcIO, srcII, srcD);
        int nsrcIndirections = srcIndirections.length;

        int[] dstIndirections = getSlicedIndirections(sliceOffsets, dstSliceCounts, dstSlices, //
                dstDO, dstIO, dstII, dstD);
        int ndstIndirections = dstIndirections.length;

        //

        int[] indirectionOffsets = new int[nsrcIndirections + 1];
        int indirectionOffset = 0;

        for (int i = 0, prodD = Arithmetic.product(srcD); i < nsrcIndirections; i++) {

            int indirection = srcIndirections[i];

            Control.checkTrue(indirection >= 0 && indirection < srcLen, //
                    "Invalid indirection index");

            int physical = srcI[indirection];

            Control.checkTrue(physical >= 0 && physical < prodD, //
                    "Invalid physical index");

            int mapLen = 1;

            for (int dim = 0; dim < ndims; dim++) {

                int logicalIndex = physical / srcS[dim];

                mapLen *= lookupCounts[srcDO[dim] + logicalIndex];

                physical %= srcS[dim];
            }

            indirectionOffsets[i] = indirectionOffset;
            indirectionOffset += mapLen;
        }

        indirectionOffsets[nsrcIndirections] = indirectionOffset;

        //

        int[] newIndirections = new int[indirectionOffset];
        int[] newIndices = new int[indirectionOffset];
        int[] logical = new int[ndims];

        for (int i = 0; i < nsrcIndirections; i++) {

            int indirection = srcIndirections[i];
            int physical = srcI[indirection];

            indirectionOffset = indirectionOffsets[i];

            newIndices[indirectionOffset] = 0;

            for (int dim = 0; dim < ndims; dim++) {

                logical[dim] = physical / srcS[dim];

                newIndices[indirectionOffset] += dstS[dim] //
                        * dstLookups[lookupOffsets[srcDO[dim] + logical[dim]]];

                physical %= srcS[dim];
            }

            Arrays.fill(newIndirections, indirectionOffsets[i], indirectionOffsets[i + 1], indirection);

            for (int dim = ndims - 1, blockSize = 1, size; dim >= 0; blockSize *= size, dim--) {

                int start = lookupOffsets[srcDO[dim] + logical[dim]];
                size = lookupCounts[srcDO[dim] + logical[dim]];

                for (int offset = indirectionOffset + blockSize, //
                offsetEnd = indirectionOffset + blockSize * size, n = start + 1; //
                offset < offsetEnd; offset += blockSize, n++) {

                    int strideOffset = dstS[dim] * (dstLookups[n] - dstLookups[n - 1]);

                    for (int j = offset - blockSize, k = offset; j < offset; j++, k++) {
                        newIndices[k] = newIndices[j] + strideOffset;
                    }
                }
            }
        }

        //

        final int[] oldIndices;
        final int[] oldIndirections;

        if (ndstIndirections > 0) {

            oldIndices = new int[dstI.length - ndstIndirections];
            oldIndirections = new int[dstI.length - ndstIndirections];

            int count = 0;

            for (int i = 0, n = dstIndirections[0]; i < n; i++, count++) {

                oldIndices[count] = dstI[i];
                oldIndirections[count] = i;
            }

            for (int i = 0, n = ndstIndirections - 1; i < n; i++) {

                for (int j = dstIndirections[i] + 1, m = dstIndirections[i + 1]; j < m; j++, count++) {

                    oldIndices[count] = dstI[j];
                    oldIndirections[count] = j;
                }
            }

            for (int i = dstIndirections[ndstIndirections - 1] + 1, n = dstI.length; i < n; i++, count++) {

                oldIndices[count] = dstI[i];
                oldIndirections[count] = i;
            }

        } else {

            oldIndices = dstI;
            oldIndirections = Arithmetic.range(dstI.length);
        }

        //

        int[][] res = merge(newIndices, newIndirections);
        newIndices = res[0];
        newIndirections = res[1];

        return assign(dstV, srcV, merge(oldIndices, oldIndirections, newIndices, newIndirections, //
                dstD, dstS, dstDO));
    }

    /**
     * Aggregates old values, new values, and their assignments into a {@link SparseArrayState}.
     * 
     * @param oldV
     *            the old values.
     * @param newV
     *            the new values.
     * @param mergeResult
     *            the result of {@link #merge(int[], int[], int[], int[], int[], int[], int[])}.
     * @param <V>
     *            the storage array type.
     * @return the {@link SparseArrayState}.
     */
    @SuppressWarnings("unchecked")
    final public static <V> SparseArrayState<V> assign(V oldV, V newV, int[][] mergeResult) {

        int[] oldAssignments = mergeResult[0];
        int[] oldIndirections = mergeResult[1];
        int[] newAssignments = mergeResult[2];
        int[] newIndirections = mergeResult[3];
        int[] indices = mergeResult[4];
        int[] indirectionOffsets = mergeResult[5];
        int[] indirections = mergeResult[6];

        int nindices = indices.length;

        final V values;

        if (oldV instanceof double[] && newV instanceof double[]) {

            double[] oldVArr = (double[]) oldV;
            double[] newVArr = (double[]) newV;
            double[] dstVArr = new double[nindices];

            for (int i = 0, n = oldAssignments.length; i < n; i++) {
                dstVArr[oldAssignments[i]] = oldVArr[oldIndirections[i]];
            }

            for (int i = 0, n = newAssignments.length; i < n; i++) {
                dstVArr[newAssignments[i]] = newVArr[newIndirections[i]];
            }

            values = (V) dstVArr;

        } else if (oldV instanceof int[] && newV instanceof int[]) {

            int[] oldVArr = (int[]) oldV;
            int[] newVArr = (int[]) newV;
            int[] dstVArr = new int[nindices];

            for (int i = 0, n = oldAssignments.length; i < n; i++) {
                dstVArr[oldAssignments[i]] = oldVArr[oldIndirections[i]];
            }

            for (int i = 0, n = newAssignments.length; i < n; i++) {
                dstVArr[newAssignments[i]] = newVArr[newIndirections[i]];
            }

            values = (V) dstVArr;

        } else if (oldV instanceof Object[] && newV instanceof Object[]) {

            Control.checkTrue(oldV.getClass().isAssignableFrom(newV.getClass()), //
                    "Invalid array types");

            Object[] oldVArr = (Object[]) oldV;
            Object[] newVArr = (Object[]) newV;
            Object[] dstVArr = (Object[]) Array.newInstance( //
                    oldVArr.getClass().getComponentType(), nindices);

            for (int i = 0, n = oldAssignments.length; i < n; i++) {
                dstVArr[oldAssignments[i]] = oldVArr[oldIndirections[i]];
            }

            for (int i = 0, n = newAssignments.length; i < n; i++) {
                dstVArr[newAssignments[i]] = newVArr[newIndirections[i]];
            }

            values = (V) dstVArr;

        } else {

            throw new IllegalArgumentException("Invalid arguments");
        }

        return new SparseArrayState<V>(values, indices, indirectionOffsets, indirections);
    }

    /**
     * Merges old and new array metadata.
     * 
     * @param oldIndices
     *            the old physical indices. Invariant: Sorted in ascending order, and does not contain duplicates.
     * @param oldIndirections
     *            the indirections on old values.
     * @param newIndices
     *            the new physical indices. Invariant: Sorted in ascending order, and does not contain duplicates.
     * @param newIndirections
     *            the indirections on new values.
     * @param dims
     *            the dimensions.
     * @param strides
     *            the strides.
     * @param dimOffsets
     *            the dimension offsets.
     * @return the assignments and metadata.
     */
    final public static int[][] merge( //
            int[] oldIndices, int[] oldIndirections, //
            int[] newIndices, int[] newIndirections, //
            int[] dims, int[] strides, int[] dimOffsets) {

        int oldLen = oldIndices.length;
        int newLen = newIndices.length;

        int[] oldAssignments = new int[oldLen];
        int[] newAssignments = new int[newLen];

        int count = 0;
        int oldCount = 0;
        int newCount = 0;

        for (; oldCount < oldLen && newCount < newLen;) {

            if (oldIndices[oldCount] < newIndices[newCount]) {

                oldAssignments[oldCount++] = count++;

            } else if (oldIndices[oldCount] > newIndices[newCount]) {

                newAssignments[newCount++] = count++;

            } else {

                oldAssignments[oldCount++] = count;
                newAssignments[newCount++] = count;
                count++;
            }
        }

        for (; oldCount < oldLen; oldCount++, count++) {
            oldAssignments[oldCount] = count;
        }

        for (; newCount < newLen; newCount++, count++) {
            newAssignments[newCount] = count;
        }

        //

        int[] indices = new int[count];

        for (int i = 0; i < oldLen; i++) {
            indices[oldAssignments[i]] = oldIndices[i];
        }

        for (int i = 0; i < newLen; i++) {
            indices[newAssignments[i]] = newIndices[i];
        }

        //

        int sumD = Arithmetic.sum(dims);
        int prodD = Arithmetic.product(dims);
        int ndims = Control.checkEquals(dims.length, strides.length);

        //

        int[] dimCounts = new int[sumD + ndims];

        Arrays.fill(dimCounts, 0);

        for (int i = 0, acc; i < count; i++) {

            acc = indices[i];

            Control.checkTrue(acc >= 0 && acc < prodD, //
                    "Invalid physical index");

            for (int dim = 0; dim < ndims; dim++) {

                int dimOffset = dimOffsets[dim] + acc / strides[dim];

                dimCounts[dimOffset]++;
                acc %= strides[dim];
            }
        }

        //

        int[] indirectionOffsets = new int[sumD + ndims];

        for (int dim = 0, acc; dim < ndims; dim++) {

            acc = 0;

            int dimOffset = dimOffsets[dim];
            int dimSize = dims[dim];

            for (int dimIndex = 0; dimIndex < dimSize; dimIndex++) {

                indirectionOffsets[dimOffset + dimIndex] = acc;
                acc += dimCounts[dimOffsets[dim] + dimIndex];
            }

            indirectionOffsets[dimOffset + dimSize] = count;
        }

        //

        Arrays.fill(dimCounts, 0);

        int[] indirections = new int[ndims * count];

        for (int i = 0, acc; i < count; i++) {

            acc = indices[i];

            for (int dim = 0; dim < ndims; dim++) {

                int dimOffset = dimOffsets[dim] + acc / strides[dim];

                indirections[count * dim + indirectionOffsets[dimOffset] + dimCounts[dimOffset]] = i;
                dimCounts[dimOffset]++;
                acc %= strides[dim];
            }
        }

        return new int[][] {
        //
                oldAssignments, //
                oldIndirections, //
                newAssignments, //
                newIndirections, //
                indices, //
                indirectionOffsets, //
                indirections //
        };
    }

    /**
     * Normalizes a range of sorted values such that duplicates are removed.
     * 
     * @param values
     *            the array of values.
     * @param start
     *            the start index.
     * @param end
     *            the end index.
     * @return the number of resulting unique values.
     */
    final public static int normalize(int[] values, int start, int end) {

        Arrays.sort(values, start, end);

        int propagate = 0;

        for (int i = start, current = -1; i < end; i++) {

            values[i - propagate] = values[i];

            if (current != values[i]) {

                current = values[i];

            } else {

                propagate++;
            }
        }

        return end - start - propagate;
    }

    /**
     * Gets the sliced indirections.
     * 
     * @param sliceOffsets
     *            the slice offsets.
     * @param sliceCounts
     *            the slice counts.
     * @param slices
     *            the slices.
     * @param dimOffsets
     *            the dimension offsets.
     * @param indirectionOffsets
     *            the indirection offsets.
     * @param indirections
     *            the indirections.
     * @param dims
     *            the dimensions.
     * @return the sliced indirections.
     */
    final public static int[] getSlicedIndirections( //
            int[] sliceOffsets, int[] sliceCounts, int[] slices, //
            int[] dimOffsets, int[] indirectionOffsets, int[] indirections, int[] dims) {

        int ndims = sliceOffsets.length - 1;
        int nindirections = indirections.length / ndims;

        int[] intersection = null;
        int len = 0;

        for (int dim = 0; dim < ndims; dim++) {

            int dimOffset = dimOffsets[dim];

            int sliceStart = sliceOffsets[dim];
            int sliceEnd = sliceOffsets[dim] + sliceCounts[dim];

            int nslices = sliceEnd - sliceStart;

            int nelts = 0;

            for (int i = 0; i < nslices; i++) {

                int index = slices[i + sliceStart];

                int start = indirectionOffsets[dimOffset + index];
                int end = indirectionOffsets[dimOffset + index + 1];

                Control.checkTrue(start >= 0 //
                        && start <= end //
                        && end >= 0 //
                        && end <= nindirections, //
                        "Invalid arguments");

                nelts += end - start;
            }

            int[] res = new int[nelts];

            for (int i = 0, resCount = 0; i < nslices; i++) {

                int index = slices[i + sliceStart];

                int start = indirectionOffsets[dimOffset + index];
                int end = indirectionOffsets[dimOffset + index + 1];

                for (int j = start; j < end; j++) {
                    res[resCount++] = indirections[nindirections * dim + j];
                }
            }

            Arrays.sort(res);

            if (intersection != null) {

                int intersectionLower = 0;
                int intersectionUpper = len;

                int resLower = 0;
                int resUpper = nelts;

                int propagate = 0;

                for (; intersectionLower < intersectionUpper && resLower < resUpper;) {

                    if (intersection[intersectionLower] < res[resLower]) {

                        intersection[intersectionLower - propagate] = intersection[intersectionLower];
                        propagate++;

                        intersectionLower++;

                    } else if (intersection[intersectionLower] > res[resLower]) {

                        resLower++;

                    } else {

                        intersection[intersectionLower - propagate] = intersection[intersectionLower];

                        intersectionLower++;
                        resLower++;
                    }
                }

                len = intersectionLower - propagate;

            } else {

                intersection = res;
                len = nelts;
            }
        }

        return Arrays.copyOf(intersection, len);
    }

    /**
     * Merges sliced indirections by physical index.
     * 
     * @param indices
     *            the physical indices.
     * @param indirections
     *            the sliced indirections.
     * @return the merged physical indices and indirections.
     */
    @SuppressWarnings("unchecked")
    final public static int[][] merge(int[] indices, int[] indirections) {

        int nelts = Control.checkEquals(indices.length, indirections.length, //
                "Invalid arguments");

        PermutationEntry<Integer>[] entries = new PermutationEntry[nelts];

        for (int i = 0; i < nelts; i++) {
            entries[i] = new PermutationEntry<Integer>(indices[i], indirections[i]);
        }

        Arrays.sort(entries);

        int propagate = 0;

        for (int i = 0, current = -1; i < nelts; i++) {

            entries[i - propagate] = entries[i];

            if (current != entries[i].getValue()) {

                current = entries[i].getValue();

            } else {

                propagate++;
            }
        }

        int resLen = nelts - propagate;

        int[] resIndices = new int[resLen];
        int[] resIndirections = new int[resLen];

        for (int i = 0; i < resLen; i++) {

            resIndices[i] = entries[i].getValue();
            resIndirections[i] = entries[i].getOrder();
        }

        return new int[][] { resIndices, resIndirections };
    }

    // Dummy constructor.
    SparseOps() {
    }
}

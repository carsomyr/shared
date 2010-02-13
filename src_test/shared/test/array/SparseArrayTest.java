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

package shared.test.array;

import static org.junit.Assert.assertTrue;
import static shared.array.ArrayBase.DEFAULT_ORDER;
import static shared.util.Control.NullRunnable;

import java.util.Arrays;

import org.junit.Test;

import shared.array.IntegerArray;
import shared.array.ObjectArray;
import shared.array.ProtoArray;
import shared.array.RealArray;
import shared.array.sparse.IntegerSparseArray;
import shared.array.sparse.ObjectSparseArray;
import shared.array.sparse.ProtoSparseArray;
import shared.array.sparse.RealSparseArray;
import shared.util.Arithmetic;

/**
 * A class of unit tests for sparse arrays.
 * 
 * @author Roy Liu
 */
public class SparseArrayTest {

    /**
     * Default constructor.
     */
    public SparseArrayTest() {
    }

    /**
     * Tests all operations supported by {@link ProtoSparseArray} against those supported by {@link ProtoArray}.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testOperations() {

        int size = 16;
        int ndims = 3;
        int ntrials = 16;

        int[] dims = new int[ndims];
        int nelts = 1;

        for (int dim = 0; dim < ndims; dim++) {

            dims[dim] = size;
            nelts *= size;
        }

        int[] physicals = Arithmetic.range(nelts);
        int[] dimIndices = Arithmetic.range(size);

        for (int i = 0; i < ntrials; i++) {

            int nselected = Arithmetic.nextInt(nelts + 1);

            RealArray srcR = new RealArray(DEFAULT_ORDER, dims);
            RealArray dstR = new RealArray(DEFAULT_ORDER, dims);

            IntegerArray srcI = new IntegerArray(DEFAULT_ORDER, dims);
            IntegerArray dstI = new IntegerArray(DEFAULT_ORDER, dims);

            ObjectArray<Integer> srcO = new ObjectArray<Integer>(Integer.class, DEFAULT_ORDER, dims);
            ObjectArray<Integer> dstO = new ObjectArray<Integer>(Integer.class, DEFAULT_ORDER, dims);

            double[] srcRValues = srcR.values();
            double[] dstRValues = dstR.values();

            int[] srcIValues = srcI.values();
            int[] dstIValues = dstI.values();

            Integer[] srcOValues = srcO.values();
            Integer[] dstOValues = dstO.values();

            RealSparseArray srcRSparse = new RealSparseArray(dims);
            RealSparseArray dstRSparse = new RealSparseArray(dims);

            IntegerSparseArray srcISparse = new IntegerSparseArray(dims);
            IntegerSparseArray dstISparse = new IntegerSparseArray(dims);

            ObjectSparseArray<Integer> srcOSparse = new ObjectSparseArray<Integer>(Integer.class, dims);
            ObjectSparseArray<Integer> dstOSparse = new ObjectSparseArray<Integer>(Integer.class, dims);

            double[] srcRNewValues = new double[nselected];
            double[] dstRNewValues = new double[nselected];

            int[] srcINewValues = new int[nselected];
            int[] dstINewValues = new int[nselected];

            Integer[] srcONewValues = new Integer[nselected];
            Integer[] dstONewValues = new Integer[nselected];

            int[] srcLogicals = new int[ndims * nselected];
            int[] dstLogicals = new int[ndims * nselected];

            //

            Arithmetic.shuffle(physicals);

            for (int j = 0; j < nselected; j++) {

                int physical = physicals[j];

                srcRValues[physical] = j;
                srcRNewValues[j] = j;

                srcIValues[physical] = j;
                srcINewValues[j] = j;

                srcOValues[physical] = j;
                srcONewValues[j] = j;

                for (int dim = 0; dim < ndims; dim++) {

                    int stride = srcR.stride(dim);

                    srcLogicals[ndims * j + dim] = physical / stride;
                    physical %= stride;
                }
            }

            Arithmetic.shuffle(physicals);

            for (int j = 0; j < nselected; j++) {

                int physical = physicals[j];

                dstRValues[physical] = -j;
                dstRNewValues[j] = -j;

                dstIValues[physical] = -j;
                dstINewValues[j] = -j;

                dstOValues[physical] = -j;
                dstONewValues[j] = -j;

                for (int dim = 0; dim < ndims; dim++) {

                    int stride = dstR.stride(dim);

                    dstLogicals[ndims * j + dim] = physical / stride;
                    physical %= stride;
                }
            }

            srcRSparse.insert(srcRNewValues, srcLogicals);
            dstRSparse.insert(dstRNewValues, dstLogicals);

            srcISparse.insert(srcINewValues, srcLogicals);
            dstISparse.insert(dstINewValues, dstLogicals);

            srcOSparse.insert(srcONewValues, srcLogicals);
            dstOSparse.insert(dstONewValues, dstLogicals);

            //

            int[][] srcSlices = new int[ndims][];
            int[][] dstSlices = new int[ndims][];

            for (int dim = 0; dim < ndims; dim++) {

                nselected = Arithmetic.nextInt(size);

                srcSlices[dim] = Arrays.copyOf(Arithmetic.shuffle(dimIndices), nselected);
                dstSlices[dim] = Arrays.copyOf(Arithmetic.shuffle(dimIndices), nselected);
            }

            //

            RealArray dstRCompare = srcR.slice(srcSlices, dstR.clone(), dstSlices);
            RealSparseArray dstRSparseCompare = //
            srcRSparse.slice(srcSlices, dstRSparse.clone(), dstSlices);

            IntegerArray dstICompare = srcI.slice(srcSlices, dstI.clone(), dstSlices);
            IntegerSparseArray dstISparseCompare = //
            srcISparse.slice(srcSlices, dstISparse.clone(), dstSlices);

            ObjectArray<Integer> dstOCompare = srcO.slice(srcSlices, dstO.clone(), dstSlices);
            ObjectSparseArray<Integer> dstOSparseCompare = //
            srcOSparse.slice(srcSlices, dstOSparse.clone(), dstSlices);

            assertTrue(Arrays.equals(dstRCompare.values(), dstRSparseCompare.toDense().values()));
            assertTrue(Arrays.equals(dstICompare.values(), dstISparseCompare.toDense().values()));
            assertTrue(Arrays.equals(dstOCompare.values(), dstOSparseCompare.toDense().values()));

            //

            dstRCompare = srcR.slice(srcSlices);
            dstRSparseCompare = srcRSparse.slice(srcSlices);

            dstICompare = srcI.slice(srcSlices);
            dstISparseCompare = srcISparse.slice(srcSlices);

            dstOCompare = srcO.slice(srcSlices);
            dstOSparseCompare = srcOSparse.slice(srcSlices);

            assertTrue(Arrays.equals(dstRCompare.values(), dstRSparseCompare.toDense().values()));
            assertTrue(Arrays.equals(dstICompare.values(), dstISparseCompare.toDense().values()));
            assertTrue(Arrays.equals(dstOCompare.values(), dstOSparseCompare.toDense().values()));

            //

            for (int dim = 0; dim < ndims; dim++) {
                dstSlices[dim] = Arithmetic.shuffle(dimIndices).clone();
            }

            dstRCompare = srcR.slice(dstR.clone(), dstSlices);
            dstRSparseCompare = srcRSparse.slice(dstRSparse.clone(), dstSlices);

            dstICompare = srcI.slice(dstI.clone(), dstSlices);
            dstISparseCompare = srcISparse.slice(dstISparse.clone(), dstSlices);

            dstOCompare = srcO.slice(dstO.clone(), dstSlices);
            dstOSparseCompare = srcOSparse.slice(dstOSparse.clone(), dstSlices);

            assertTrue(Arrays.equals(dstRCompare.values(), dstRSparseCompare.toDense().values()));
            assertTrue(Arrays.equals(dstICompare.values(), dstISparseCompare.toDense().values()));
            assertTrue(Arrays.equals(dstOCompare.values(), dstOSparseCompare.toDense().values()));

            //

            dstRCompare = srcR.clone().slice(-1.0, srcSlices);
            dstRSparseCompare = srcRSparse.clone().slice(-1.0, srcSlices);

            dstICompare = srcI.clone().slice(-1, srcSlices);
            dstISparseCompare = srcISparse.clone().slice(-1, srcSlices);

            dstOCompare = srcO.clone().slice(-1, srcSlices);
            dstOSparseCompare = srcOSparse.clone().slice(-1, srcSlices);

            assertTrue(Arrays.equals(dstRCompare.values(), dstRSparseCompare.toDense().values()));
            assertTrue(Arrays.equals(dstICompare.values(), dstISparseCompare.toDense().values()));
            assertTrue(Arrays.equals(dstOCompare.values(), dstOSparseCompare.toDense().values()));

            //

            int[] perm = Arithmetic.shuffle(Arithmetic.range(ndims));

            dstRCompare = srcR.transpose(perm);
            dstRSparseCompare = srcRSparse.transpose(perm);

            dstICompare = srcI.transpose(perm);
            dstISparseCompare = srcISparse.transpose(perm);

            dstOCompare = srcO.transpose(perm);
            dstOSparseCompare = srcOSparse.transpose(perm);

            assertTrue(Arrays.equals(dstRCompare.values(), dstRSparseCompare.toDense().values()));
            assertTrue(Arrays.equals(dstICompare.values(), dstISparseCompare.toDense().values()));
            assertTrue(Arrays.equals(dstOCompare.values(), dstOSparseCompare.toDense().values()));

            //

            int[] opDims = Arrays.copyOf(Arithmetic.shuffle(perm), //
                    Arithmetic.nextInt(ndims));

            dstRCompare = srcR.reverse(opDims);
            dstRSparseCompare = srcRSparse.reverse(opDims);

            dstICompare = srcI.reverse(opDims);
            dstISparseCompare = srcISparse.reverse(opDims);

            dstOCompare = srcO.reverse(opDims);
            dstOSparseCompare = srcOSparse.reverse(opDims);

            assertTrue(Arrays.equals(dstRCompare.values(), dstRSparseCompare.toDense().values()));
            assertTrue(Arrays.equals(dstICompare.values(), dstISparseCompare.toDense().values()));
            assertTrue(Arrays.equals(dstOCompare.values(), dstOSparseCompare.toDense().values()));

            //

            int[] shifts = new int[ndims];

            for (int dim = 0; dim < ndims; dim++) {
                shifts[dim] = Arithmetic.nextInt(4 * size + 1) - 2 * size;
            }

            dstRCompare = srcR.shift(shifts);
            dstRSparseCompare = srcRSparse.shift(shifts);

            dstICompare = srcI.shift(shifts);
            dstISparseCompare = srcISparse.shift(shifts);

            dstOCompare = srcO.shift(shifts);
            dstOSparseCompare = srcOSparse.shift(shifts);

            assertTrue(Arrays.equals(dstRCompare.values(), dstRSparseCompare.toDense().values()));
            assertTrue(Arrays.equals(dstICompare.values(), dstISparseCompare.toDense().values()));
            assertTrue(Arrays.equals(dstOCompare.values(), dstOSparseCompare.toDense().values()));

            //

            int[] repetitions = new int[ndims];

            for (int dim = 0; dim < ndims; dim++) {
                repetitions[dim] = Arithmetic.nextInt(2) + 1;
            }

            dstRCompare = srcR.tile(repetitions);
            dstRSparseCompare = srcRSparse.tile(repetitions);

            dstICompare = srcI.tile(repetitions);
            dstISparseCompare = srcISparse.tile(repetitions);

            dstOCompare = srcO.tile(repetitions);
            dstOSparseCompare = srcOSparse.tile(repetitions);

            assertTrue(Arrays.equals(dstRCompare.values(), dstRSparseCompare.toDense().values()));
            assertTrue(Arrays.equals(dstICompare.values(), dstISparseCompare.toDense().values()));
            assertTrue(Arrays.equals(dstOCompare.values(), dstOSparseCompare.toDense().values()));

            //

            int[] reshapeDims = new int[ndims - 1];

            for (int dim = 0; dim < ndims - 2; dim++) {
                reshapeDims[dim] = dims[dim];
            }

            reshapeDims[ndims - 2] = dims[ndims - 2] * dims[ndims - 1];

            dstRCompare = srcR.reshape(reshapeDims);
            dstRSparseCompare = srcRSparse.reshape(reshapeDims);

            dstICompare = srcI.reshape(reshapeDims);
            dstISparseCompare = srcISparse.reshape(reshapeDims);

            dstOCompare = srcO.reshape(reshapeDims);
            dstOSparseCompare = srcOSparse.reshape(reshapeDims);

            assertTrue(Arrays.equals(dstRCompare.values(), dstRSparseCompare.toDense().values()));
            assertTrue(Arrays.equals(dstICompare.values(), dstISparseCompare.toDense().values()));
            assertTrue(Arrays.equals(dstOCompare.values(), dstOSparseCompare.toDense().values()));

            //

            int[] bounds = new int[2 * ndims];

            for (int dim = 0; dim < ndims; dim++) {

                int len = Arithmetic.nextInt(size + 1);
                int offset = Arithmetic.nextInt(size + 1 - len);

                bounds[2 * dim] = offset;
                bounds[2 * dim + 1] = offset + len;
            }

            dstRCompare = srcR.subarray(bounds);
            dstRSparseCompare = srcRSparse.subarray(bounds);

            dstICompare = srcI.subarray(bounds);
            dstISparseCompare = srcISparse.subarray(bounds);

            dstOCompare = srcO.subarray(bounds);
            dstOSparseCompare = srcOSparse.subarray(bounds);

            assertTrue(Arrays.equals(dstRCompare.values(), dstRSparseCompare.toDense().values()));
            assertTrue(Arrays.equals(dstICompare.values(), dstISparseCompare.toDense().values()));
            assertTrue(Arrays.equals(dstOCompare.values(), dstOSparseCompare.toDense().values()));

            //

            int opDim = Arithmetic.nextInt(ndims);

            dstRCompare = srcR.concat(opDim, dstR);
            dstRSparseCompare = srcRSparse.concat(opDim, dstRSparse);

            dstICompare = srcI.concat(opDim, dstI);
            dstISparseCompare = srcISparse.concat(opDim, dstISparse);

            dstOCompare = srcO.concat(opDim, dstO);
            dstOSparseCompare = srcOSparse.concat(opDim, dstOSparse);

            assertTrue(Arrays.equals(dstRCompare.values(), dstRSparseCompare.toDense().values()));
            assertTrue(Arrays.equals(dstICompare.values(), dstISparseCompare.toDense().values()));
            assertTrue(Arrays.equals(dstOCompare.values(), dstOSparseCompare.toDense().values()));
        }
    }

    /**
     * Tests corner cases.
     */
    @Test
    public void testCornerCases() {

        new RealSparseArray(10, 10, 10).slice( //
                new int[][] {
                //
                        { 1, 2, 3, 4, 5, 6, 7, 8 }, //
                        {}, //
                        { 0, 1 } //
                }, //
                new RealSparseArray(10, 10, 10), //
                new int[][] {
                //
                        { 0, 1, 2, 3, 4, 5, 6, 7 }, //
                        {}, //
                        { 1, 2 } //
                });

        new RealSparseArray(10, 10, 0).map(new RealSparseArray(10, 10, 10), //
                0, 0, 5, //
                5, 5, 5, //
                0, 0, 10);

        new RealSparseArray(10, 10, 10).map(new RealSparseArray(10, 0, 10), //
                0, 0, 5, //
                5, 5, 5, //
                0, 0, 10);

        new RealSparseArray(10, 10, 10).map(new RealSparseArray(10, 10, 10), //
                0, 0, 5, //
                5, 5, 0, //
                0, 0, 10);
    }

    /**
     * Tests that a {@link RuntimeException} is thrown.
     */
    @SuppressWarnings("unchecked")
    @Test(expected = RuntimeException.class)
    public void testThrowException() {

        ObjectSparseArray runnableArray = new ObjectSparseArray<Runnable>(Runnable.class, 3, 3);
        ObjectSparseArray threadArray = new ObjectSparseArray<Thread>(Thread.class, 3, 3);

        runnableArray.insert(shared.util.Arrays.newArray(Runnable.class, 9, NullRunnable), //
                //
                0, 0, //
                0, 1, //
                0, 2, //
                1, 0, //
                1, 1, //
                1, 2, //
                2, 0, //
                2, 1, //
                2, 2);

        threadArray.insert(shared.util.Arrays.newArray(Thread.class, 9, Thread.currentThread()), //
                //
                0, 0, //
                0, 1, //
                0, 2, //
                1, 0, //
                1, 1, //
                1, 2, //
                2, 0, //
                2, 1, //
                2, 2);

        try {

            ((ObjectSparseArray<Runnable>) runnableArray).slice(threadArray, //
                    new int[] { 0, 1, 2 }, //
                    new int[] { 0, 1, 2 });

        } catch (RuntimeException e) {

            assertTrue(e.getMessage().equals("Invalid array types"));

            throw e;
        }
    }
}

/**
 * <p>
 * Copyright (c) 2009 Roy Liu<br>
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

package shared.test.array;

import static shared.array.ArrayBase.DEFAULT_ORDER;
import static shared.util.Control.NullRunnable;

import java.util.Arrays;

import org.junit.Assert;
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
        int nDims = 3;
        int nTrials = 16;

        int[] dims = new int[nDims];
        int nElts = 1;

        for (int dim = 0; dim < nDims; dim++) {

            dims[dim] = size;
            nElts *= size;
        }

        int[] physicals = Arithmetic.range(nElts);
        int[] dimIndices = Arithmetic.range(size);

        for (int i = 0; i < nTrials; i++) {

            int nSelected = Arithmetic.nextInt(nElts + 1);

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

            double[] srcRNewValues = new double[nSelected];
            double[] dstRNewValues = new double[nSelected];

            int[] srcINewValues = new int[nSelected];
            int[] dstINewValues = new int[nSelected];

            Integer[] srcONewValues = new Integer[nSelected];
            Integer[] dstONewValues = new Integer[nSelected];

            int[] srcLogicals = new int[nDims * nSelected];
            int[] dstLogicals = new int[nDims * nSelected];

            //

            Arithmetic.shuffle(physicals);

            for (int j = 0; j < nSelected; j++) {

                int physical = physicals[j];

                srcRValues[physical] = j;
                srcRNewValues[j] = j;

                srcIValues[physical] = j;
                srcINewValues[j] = j;

                srcOValues[physical] = j;
                srcONewValues[j] = j;

                for (int dim = 0; dim < nDims; dim++) {

                    int stride = srcR.stride(dim);

                    srcLogicals[nDims * j + dim] = physical / stride;
                    physical %= stride;
                }
            }

            Arithmetic.shuffle(physicals);

            for (int j = 0; j < nSelected; j++) {

                int physical = physicals[j];

                dstRValues[physical] = -j;
                dstRNewValues[j] = -j;

                dstIValues[physical] = -j;
                dstINewValues[j] = -j;

                dstOValues[physical] = -j;
                dstONewValues[j] = -j;

                for (int dim = 0; dim < nDims; dim++) {

                    int stride = dstR.stride(dim);

                    dstLogicals[nDims * j + dim] = physical / stride;
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

            int[][] srcSlices = new int[nDims][];
            int[][] dstSlices = new int[nDims][];

            for (int dim = 0; dim < nDims; dim++) {

                nSelected = Arithmetic.nextInt(size);

                srcSlices[dim] = Arrays.copyOf(Arithmetic.shuffle(dimIndices), nSelected);
                dstSlices[dim] = Arrays.copyOf(Arithmetic.shuffle(dimIndices), nSelected);
            }

            //

            RealArray dstRCompare = srcR.slice(srcSlices, dstR.clone(), dstSlices);
            RealSparseArray dstRSparseCompare = srcRSparse.slice(srcSlices, dstRSparse.clone(), dstSlices);

            IntegerArray dstICompare = srcI.slice(srcSlices, dstI.clone(), dstSlices);
            IntegerSparseArray dstISparseCompare = srcISparse.slice(srcSlices, dstISparse.clone(), dstSlices);

            ObjectArray<Integer> dstOCompare = srcO.slice(srcSlices, dstO.clone(), dstSlices);
            ObjectSparseArray<Integer> dstOSparseCompare = srcOSparse.slice(srcSlices, dstOSparse.clone(), dstSlices);

            Assert.assertTrue(Arrays.equals(dstRCompare.values(), dstRSparseCompare.toDense().values()));
            Assert.assertTrue(Arrays.equals(dstICompare.values(), dstISparseCompare.toDense().values()));
            Assert.assertTrue(Arrays.equals(dstOCompare.values(), dstOSparseCompare.toDense().values()));

            //

            dstRCompare = srcR.slice(srcSlices);
            dstRSparseCompare = srcRSparse.slice(srcSlices);

            dstICompare = srcI.slice(srcSlices);
            dstISparseCompare = srcISparse.slice(srcSlices);

            dstOCompare = srcO.slice(srcSlices);
            dstOSparseCompare = srcOSparse.slice(srcSlices);

            Assert.assertTrue(Arrays.equals(dstRCompare.values(), dstRSparseCompare.toDense().values()));
            Assert.assertTrue(Arrays.equals(dstICompare.values(), dstISparseCompare.toDense().values()));
            Assert.assertTrue(Arrays.equals(dstOCompare.values(), dstOSparseCompare.toDense().values()));

            //

            for (int dim = 0; dim < nDims; dim++) {
                dstSlices[dim] = Arithmetic.shuffle(dimIndices).clone();
            }

            dstRCompare = srcR.slice(dstR.clone(), dstSlices);
            dstRSparseCompare = srcRSparse.slice(dstRSparse.clone(), dstSlices);

            dstICompare = srcI.slice(dstI.clone(), dstSlices);
            dstISparseCompare = srcISparse.slice(dstISparse.clone(), dstSlices);

            dstOCompare = srcO.slice(dstO.clone(), dstSlices);
            dstOSparseCompare = srcOSparse.slice(dstOSparse.clone(), dstSlices);

            Assert.assertTrue(Arrays.equals(dstRCompare.values(), dstRSparseCompare.toDense().values()));
            Assert.assertTrue(Arrays.equals(dstICompare.values(), dstISparseCompare.toDense().values()));
            Assert.assertTrue(Arrays.equals(dstOCompare.values(), dstOSparseCompare.toDense().values()));

            //

            dstRCompare = srcR.clone().slice(-1.0, srcSlices);
            dstRSparseCompare = srcRSparse.clone().slice(-1.0, srcSlices);

            dstICompare = srcI.clone().slice(-1, srcSlices);
            dstISparseCompare = srcISparse.clone().slice(-1, srcSlices);

            dstOCompare = srcO.clone().slice(-1, srcSlices);
            dstOSparseCompare = srcOSparse.clone().slice(-1, srcSlices);

            Assert.assertTrue(Arrays.equals(dstRCompare.values(), dstRSparseCompare.toDense().values()));
            Assert.assertTrue(Arrays.equals(dstICompare.values(), dstISparseCompare.toDense().values()));
            Assert.assertTrue(Arrays.equals(dstOCompare.values(), dstOSparseCompare.toDense().values()));

            //

            int[] perm = Arithmetic.shuffle(Arithmetic.range(nDims));

            dstRCompare = srcR.transpose(perm);
            dstRSparseCompare = srcRSparse.transpose(perm);

            dstICompare = srcI.transpose(perm);
            dstISparseCompare = srcISparse.transpose(perm);

            dstOCompare = srcO.transpose(perm);
            dstOSparseCompare = srcOSparse.transpose(perm);

            Assert.assertTrue(Arrays.equals(dstRCompare.values(), dstRSparseCompare.toDense().values()));
            Assert.assertTrue(Arrays.equals(dstICompare.values(), dstISparseCompare.toDense().values()));
            Assert.assertTrue(Arrays.equals(dstOCompare.values(), dstOSparseCompare.toDense().values()));

            //

            int[] opDims = Arrays.copyOf(Arithmetic.shuffle(perm), //
                    Arithmetic.nextInt(nDims));

            dstRCompare = srcR.reverse(opDims);
            dstRSparseCompare = srcRSparse.reverse(opDims);

            dstICompare = srcI.reverse(opDims);
            dstISparseCompare = srcISparse.reverse(opDims);

            dstOCompare = srcO.reverse(opDims);
            dstOSparseCompare = srcOSparse.reverse(opDims);

            Assert.assertTrue(Arrays.equals(dstRCompare.values(), dstRSparseCompare.toDense().values()));
            Assert.assertTrue(Arrays.equals(dstICompare.values(), dstISparseCompare.toDense().values()));
            Assert.assertTrue(Arrays.equals(dstOCompare.values(), dstOSparseCompare.toDense().values()));

            //

            int[] shifts = new int[nDims];

            for (int dim = 0; dim < nDims; dim++) {
                shifts[dim] = Arithmetic.nextInt(4 * size + 1) - 2 * size;
            }

            dstRCompare = srcR.shift(shifts);
            dstRSparseCompare = srcRSparse.shift(shifts);

            dstICompare = srcI.shift(shifts);
            dstISparseCompare = srcISparse.shift(shifts);

            dstOCompare = srcO.shift(shifts);
            dstOSparseCompare = srcOSparse.shift(shifts);

            Assert.assertTrue(Arrays.equals(dstRCompare.values(), dstRSparseCompare.toDense().values()));
            Assert.assertTrue(Arrays.equals(dstICompare.values(), dstISparseCompare.toDense().values()));
            Assert.assertTrue(Arrays.equals(dstOCompare.values(), dstOSparseCompare.toDense().values()));

            //

            int[] repetitions = new int[nDims];

            for (int dim = 0; dim < nDims; dim++) {
                repetitions[dim] = Arithmetic.nextInt(2) + 1;
            }

            dstRCompare = srcR.tile(repetitions);
            dstRSparseCompare = srcRSparse.tile(repetitions);

            dstICompare = srcI.tile(repetitions);
            dstISparseCompare = srcISparse.tile(repetitions);

            dstOCompare = srcO.tile(repetitions);
            dstOSparseCompare = srcOSparse.tile(repetitions);

            Assert.assertTrue(Arrays.equals(dstRCompare.values(), dstRSparseCompare.toDense().values()));
            Assert.assertTrue(Arrays.equals(dstICompare.values(), dstISparseCompare.toDense().values()));
            Assert.assertTrue(Arrays.equals(dstOCompare.values(), dstOSparseCompare.toDense().values()));

            //

            int[] reshapeDims = new int[nDims - 1];

            for (int dim = 0; dim < nDims - 2; dim++) {
                reshapeDims[dim] = dims[dim];
            }

            reshapeDims[nDims - 2] = dims[nDims - 2] * dims[nDims - 1];

            dstRCompare = srcR.reshape(reshapeDims);
            dstRSparseCompare = srcRSparse.reshape(reshapeDims);

            dstICompare = srcI.reshape(reshapeDims);
            dstISparseCompare = srcISparse.reshape(reshapeDims);

            dstOCompare = srcO.reshape(reshapeDims);
            dstOSparseCompare = srcOSparse.reshape(reshapeDims);

            Assert.assertTrue(Arrays.equals(dstRCompare.values(), dstRSparseCompare.toDense().values()));
            Assert.assertTrue(Arrays.equals(dstICompare.values(), dstISparseCompare.toDense().values()));
            Assert.assertTrue(Arrays.equals(dstOCompare.values(), dstOSparseCompare.toDense().values()));

            //

            int[] bounds = new int[2 * nDims];

            for (int dim = 0; dim < nDims; dim++) {

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

            Assert.assertTrue(Arrays.equals(dstRCompare.values(), dstRSparseCompare.toDense().values()));
            Assert.assertTrue(Arrays.equals(dstICompare.values(), dstISparseCompare.toDense().values()));
            Assert.assertTrue(Arrays.equals(dstOCompare.values(), dstOSparseCompare.toDense().values()));

            //

            int opDim = Arithmetic.nextInt(nDims);

            dstRCompare = srcR.concat(opDim, dstR);
            dstRSparseCompare = srcRSparse.concat(opDim, dstRSparse);

            dstICompare = srcI.concat(opDim, dstI);
            dstISparseCompare = srcISparse.concat(opDim, dstISparse);

            dstOCompare = srcO.concat(opDim, dstO);
            dstOSparseCompare = srcOSparse.concat(opDim, dstOSparse);

            Assert.assertTrue(Arrays.equals(dstRCompare.values(), dstRSparseCompare.toDense().values()));
            Assert.assertTrue(Arrays.equals(dstICompare.values(), dstISparseCompare.toDense().values()));
            Assert.assertTrue(Arrays.equals(dstOCompare.values(), dstOSparseCompare.toDense().values()));
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

        ObjectSparseArray<Runnable> runnableArray = new ObjectSparseArray<Runnable>(Runnable.class, 3, 3);
        ObjectSparseArray<Thread> threadArray = new ObjectSparseArray<Thread>(Thread.class, 3, 3);

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

            runnableArray.slice((ObjectSparseArray<Runnable>) ((ObjectSparseArray<?>) threadArray), //
                    new int[] { 0, 1, 2 }, //
                    new int[] { 0, 1, 2 });

        } catch (RuntimeException e) {

            Assert.assertTrue(e.getMessage().equals("Invalid array types"));

            throw e;
        }
    }
}

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

package org.shared.array;

import static org.shared.array.ArrayBase.DEFAULT_ORDER;
import static org.shared.array.ArrayBase.ioKernel;
import static org.shared.array.ArrayBase.opKernel;

import org.shared.util.Arithmetic;
import org.shared.util.Arrays;
import org.shared.util.Control;

/**
 * A multidimensional real array class.
 * 
 * @author Roy Liu
 */
public class RealArray extends AbstractRealArray<RealArray, ComplexArray> implements Matrix<RealArray, Double> {

    /**
     * Default constructor.
     */
    public RealArray(int... dims) {
        this(0, DEFAULT_ORDER, dims.clone());
    }

    /**
     * Alternate constructor.
     */
    public RealArray(IndexingOrder order, int... dims) {
        this(0, order, dims.clone());
    }

    /**
     * Internal constructor with a distinctive signature.
     */
    protected RealArray(int unused, IndexingOrder order, int[] dims) {
        super(new double[Arithmetic.product(dims)], order, dims, order.strides(dims));

        Control.checkTrue(dims.length > 0);
    }

    /**
     * Alternate constructor.
     */
    public RealArray(double[] values, int... dims) {
        this(0, values, DEFAULT_ORDER, ArrayBase.inferDimensions(dims, values.length, false));
    }

    /**
     * Alternate constructor.
     */
    public RealArray(double[] values, IndexingOrder order, int... dims) {
        this(0, values, order, ArrayBase.inferDimensions(dims, values.length, false));
    }

    /**
     * Alternate constructor.
     */
    public RealArray(RealArray array) {
        this(0, array.values.clone(), array.order, array.dims);
    }

    /**
     * Internal constructor with a distinctive signature.
     */
    protected RealArray(int unused, double[] values, IndexingOrder order, int[] dims) {
        super(values, order, dims, order.strides(dims));

        Control.checkTrue(dims.length > 0 //
                && values.length == Arithmetic.product(dims));
    }

    /**
     * Internal constructor for package use only.
     */
    protected RealArray(IndexingOrder order, int[] dims, int[] strides) {
        super(new double[Arithmetic.product(dims)], order, dims, strides);
    }

    @Override
    protected RealArray wrap(int parity, IndexingOrder order, int[] dims, int[] strides) {
        return new RealArray(order, dims, strides);
    }

    @Override
    protected ComplexArray wrapUp(int parity, IndexingOrder order, int[] dims, int[] strides) {
        return new ComplexArray(parity, dims, strides);
    }

    @Override
    public RealArray mMul(RealArray b) {

        RealArray a = this;

        a.checkMatrixOrder();
        b.checkMatrixOrder();

        Control.checkTrue(Control.checkEquals(a.dims.length, b.dims.length, //
                "Dimensionality mismatch") == 2, //
                "Arrays must have exactly two dimensions");

        RealArray res = new RealArray(DEFAULT_ORDER, a.dims[0], b.dims[1]);

        opKernel.mul(a.values, b.values, a.dims[0], b.dims[1], res.values, false);

        return res;
    }

    @Override
    public RealArray mDiag() {

        RealArray a = this;

        a.checkMatrixOrder();

        Control.checkTrue(a.dims.length == 2, //
                "Array must have exactly two dimensions");

        int size = Control.checkEquals(a.dims[0], a.dims[1], //
                "Dimensionality mismatch");

        RealArray res = new RealArray(DEFAULT_ORDER, size, 1);

        opKernel.diag(a.values, res.values, size, false);

        return res;
    }

    @Override
    public RealArray mTranspose() {

        RealArray a = this;

        a.checkMatrixOrder();

        Control.checkTrue(a.dims.length == 2, //
                "Array must have exactly two dimensions");

        return transpose(1, 0);
    }

    @Override
    public RealArray[] mSvd() {

        RealArray a = this;

        a.checkMatrixOrder();

        Control.checkTrue(a.dims.length == 2, //
                "Array must have exactly two dimensions");

        int nRows = a.dims[0];
        int nCols = a.dims[1];

        final int matStrideRow, matStrideCol, nRowsT, nColsT;

        boolean transpose = (nRows < nCols);

        if (!transpose) {

            nRowsT = nRows;
            nColsT = nCols;

            matStrideRow = nColsT;
            matStrideCol = 1;

        } else {

            nRowsT = nCols;
            nColsT = nRows;

            matStrideRow = 1;
            matStrideCol = nRowsT;
        }

        RealArray u = new RealArray(nRowsT, nColsT);
        RealArray s = new RealArray(nColsT, nColsT);
        RealArray v = new RealArray(nColsT, nColsT);

        double[] sV = new double[nColsT];

        opKernel.svd(a.values, matStrideRow, matStrideCol, u.values(), sV, v.values(), nRowsT, nColsT);

        double[] sValues = s.values();

        for (int i = 0; i < nColsT; i++) {
            sValues[nColsT * i + i] = sV[i];
        }

        return !transpose ? new RealArray[] { u, s, v } : new RealArray[] { v, s, u };
    }

    @Override
    public RealArray[] mEigs() {

        RealArray a = this;

        a.checkMatrixOrder();

        Control.checkTrue(a.dims.length == 2, //
                "Array must have exactly two dimensions");

        int size = Control.checkEquals(a.dims[0], a.dims[1], //
                "Dimensionality mismatch");

        RealArray eigVectors = new RealArray(size, size);

        double[] eigValues = new double[2 * size];

        opKernel.eigs(a.values, eigVectors.values, eigValues, size);

        RealArray eigValueMatrix = new RealArray(size, size);

        for (int i = 0, n = 2 * size; i < n; i += 2) {

            int dim = i >>> 1;

            if (i < n - 2 //
                    && eigValues[i] == eigValues[i + 2] //
                    && eigValues[i + 1] > 0 //
                    && eigValues[i + 3] < 0) {

                eigValueMatrix.set(eigValues[i], dim, dim);
                eigValueMatrix.set(eigValues[i + 1], dim, dim + 1);
                eigValueMatrix.set(eigValues[i + 2], dim + 1, dim + 1);
                eigValueMatrix.set(eigValues[i + 3], dim + 1, dim);

                i += 2;

            } else {

                eigValueMatrix.set(eigValues[i], dim, dim);
            }
        }

        return new RealArray[] { eigVectors, eigValueMatrix };
    }

    @Override
    public RealArray mInvert() {

        RealArray a = this;

        a.checkMatrixOrder();

        Control.checkTrue(a.dims.length == 2, //
                "Array must have exactly two dimensions");

        int size = Control.checkEquals(a.dims[0], a.dims[1], //
                "Dimensionality mismatch");

        RealArray res = new RealArray(DEFAULT_ORDER, size, size);

        opKernel.invert(a.values, res.values, size);

        return res;
    }

    @Override
    public byte[] getBytes() {
        return ioKernel.getBytes(this);
    }

    /**
     * Gets the singleton value from this array.
     */
    public double singleton() {

        Control.checkTrue(this.values.length == 1, //
                "Array must contain exactly one value");

        return this.values[0];
    }

    /**
     * Parses an array from {@code byte}s.
     */
    final public static RealArray parse(byte[] data) {
        return ioKernel.parse(data);
    }

    /**
     * Creates an array of the given size and number of dimensions, with ones for diagonals.
     */
    final public static RealArray eye(int size, int nDims) {

        RealArray res = new RealArray(Arrays.newArray(nDims, size));
        double[] resValues = res.values();

        for (int i = 0, offset = 0, increment = Arithmetic.sum(res.strides()); i < size; i++, offset += increment) {
            resValues[offset] = 1.0;
        }

        return res;
    }
}

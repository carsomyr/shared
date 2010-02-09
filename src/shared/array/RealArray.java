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

package shared.array;

import static shared.array.ArrayBase.DEFAULT_ORDER;
import static shared.array.ArrayBase.IOKernel;
import static shared.array.ArrayBase.OpKernel;
import shared.util.Arithmetic;
import shared.util.Arrays;
import shared.util.Control;

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

    public RealArray mMul(RealArray b) {

        RealArray a = this;

        a.checkMatrixOrder();
        b.checkMatrixOrder();

        Control.checkTrue(Control.checkEquals(a.dims.length, b.dims.length, //
                "Dimensionality mismatch") == 2, //
                "Arrays must have exactly two dimensions");

        RealArray res = new RealArray(DEFAULT_ORDER, a.dims[0], b.dims[1]);

        OpKernel.mul(a.values, b.values, a.dims[0], b.dims[1], res.values, false);

        return res;
    }

    public RealArray mDiag() {

        RealArray a = this;

        a.checkMatrixOrder();

        Control.checkTrue(a.dims.length == 2, //
                "Array must have exactly two dimensions");

        int size = Control.checkEquals(a.dims[0], a.dims[1], //
                "Dimensionality mismatch");

        RealArray res = new RealArray(DEFAULT_ORDER, size, 1);

        OpKernel.diag(a.values, res.values, size, false);

        return res;
    }

    public RealArray mTranspose() {

        RealArray a = this;

        a.checkMatrixOrder();

        Control.checkTrue(a.dims.length == 2, //
                "Array must have exactly two dimensions");

        return transpose(1, 0);
    }

    public RealArray[] mSVD() {

        RealArray a = this;

        a.checkMatrixOrder();

        Control.checkTrue(a.dims.length == 2, //
                "Array must have exactly two dimensions");

        int nrows = a.dims[0];
        int ncols = a.dims[1];

        final int matStrideRow, matStrideCol, nrowsT, ncolsT;

        boolean transpose = (nrows < ncols);

        if (!transpose) {

            nrowsT = nrows;
            ncolsT = ncols;

            matStrideRow = ncolsT;
            matStrideCol = 1;

        } else {

            nrowsT = ncols;
            ncolsT = nrows;

            matStrideRow = 1;
            matStrideCol = nrowsT;
        }

        RealArray u = new RealArray(nrowsT, ncolsT);
        RealArray s = new RealArray(ncolsT, ncolsT);
        RealArray v = new RealArray(ncolsT, ncolsT);

        double[] sV = new double[ncolsT];

        OpKernel.svd(a.values, matStrideRow, matStrideCol, u.values(), sV, v.values(), nrowsT, ncolsT);

        double[] sValues = s.values();

        for (int i = 0; i < ncolsT; i++) {
            sValues[ncolsT * i + i] = sV[i];
        }

        return !transpose ? new RealArray[] { u, s, v } : new RealArray[] { v, s, u };
    }

    public RealArray[] mEigs() {

        RealArray a = this;

        a.checkMatrixOrder();

        Control.checkTrue(a.dims.length == 2, //
                "Array must have exactly two dimensions");

        int size = Control.checkEquals(a.dims[0], a.dims[1], //
                "Dimensionality mismatch");

        RealArray eigVectors = new RealArray(size, size);

        double[] eigValues = new double[2 * size];

        OpKernel.eigs(a.values, eigVectors.values, eigValues, size);

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

    public RealArray mInvert() {

        RealArray a = this;

        a.checkMatrixOrder();

        Control.checkTrue(a.dims.length == 2, //
                "Array must have exactly two dimensions");

        int size = Control.checkEquals(a.dims[0], a.dims[1], //
                "Dimensionality mismatch");

        RealArray res = new RealArray(DEFAULT_ORDER, size, size);

        OpKernel.invert(a.values, res.values, size);

        return res;
    }

    @Override
    public byte[] getBytes() {
        return IOKernel.getBytes(this);
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
        return IOKernel.parse(data);
    }

    /**
     * Creates an array of the given size and number of dimensions, with ones for diagonals.
     */
    final public static RealArray eye(int size, int ndims) {

        RealArray res = new RealArray(Arrays.newArray(ndims, size));
        double[] resValues = res.values();

        for (int i = 0, offset = 0, increment = Arithmetic.sum(res.strides()); i < size; i++, offset += increment) {
            resValues[offset] = 1.0;
        }

        return res;
    }
}

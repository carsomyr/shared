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

import static shared.array.ArrayBase.IOKernel;
import static shared.array.ArrayBase.OpKernel;
import static shared.array.ArrayBase.formatEmptyArray;
import static shared.array.ArrayBase.formatSlice;
import static shared.array.kernel.ArrayKernel.ITOR;

import java.util.Arrays;
import java.util.Formatter;

import shared.array.kernel.ArrayKernel;
import shared.array.kernel.MappingOps;
import shared.util.Arithmetic;
import shared.util.Control;

/**
 * A multidimensional integer array class.
 * 
 * @apiviz.uses shared.array.ArrayBase
 * @author Roy Liu
 */
public class IntegerArray extends ProtoArray<IntegerArray, int[], Integer> {

    /**
     * Default constructor.
     */
    public IntegerArray(IndexingOrder order, int... dims) {
        this(0, order, dims.clone());
    }

    /**
     * Internal constructor with a distinctive signature.
     */
    protected IntegerArray(int unused, IndexingOrder order, int[] dims) {
        super(new int[Arithmetic.product(dims)], order, dims, order.strides(dims));

        Control.checkTrue(dims.length > 0);
    }

    /**
     * Alternate constructor.
     */
    public IntegerArray(int[] values, int... dims) {
        this(0, values, IndexingOrder.FAR, dims);
    }

    /**
     * Alternate constructor.
     */
    public IntegerArray(int[] values, IndexingOrder order, int... dims) {
        this(0, values, order, dims);
    }

    /**
     * Alternate constructor.
     */
    public IntegerArray(IntegerArray array) {
        this(0, array.values.clone(), array.order, array.dims);
    }

    /**
     * Internal constructor with a distinctive signature.
     */
    protected IntegerArray(int unused, int[] values, IndexingOrder order, int[] dims) {
        super(values, order, dims, order.strides(dims));

        Control.checkTrue(dims.length > 0 //
                && values.length == Arithmetic.product(dims));
    }

    /**
     * Internal constructor for package use only.
     */
    protected IntegerArray(IndexingOrder order, int[] dims, int[] strides) {
        super(new int[Arithmetic.product(dims)], order, dims, strides);
    }

    @Override
    protected IntegerArray wrap(IndexingOrder order, int[] dims, int[] strides) {
        return new IntegerArray(order, dims, strides);
    }

    @Override
    protected IntegerArray wrap(Integer value, IndexingOrder order, int[] dims, int[] strides) {
        return wrap(order, dims, strides).uFill(value);
    }

    /**
     * Gets the value at the given logical index.
     */
    public int get(int... s) {
        return this.values[physical(s)];
    }

    /**
     * Sets the value at the given logical index.
     */
    public void set(int value, int... s) {
        this.values[physical(s)] = value;
    }

    @Override
    public int[] values() {
        return this.values;
    }

    @Override
    public byte[] getBytes() {
        return IOKernel.getBytes(this);
    }

    public Class<Integer> getComponentType() {
        return Integer.class;
    }

    @Override
    public String toString() {

        int[] values = this.values;
        int[] dims = this.dims;
        int[] strides = this.strides;

        int ndims = dims.length;
        int nrows = (ndims == 1) ? 1 : size(ndims - 2);
        int ncols = size(ndims - 1);
        int sliceSize = nrows * ncols;

        int max = Math.max(Arithmetic.max(values), Math.abs(Arithmetic.min(values)));
        int exponent = max > 0 ? (int) Math.log10(max) : 0;

        Formatter f = new Formatter();

        if (values.length == 0) {

            formatEmptyArray(f, dims);

            return f.toString();
        }

        String format = String.format("%%%dd", exponent + 3);

        int[] indices = MappingOps.assignMappingIndices(Arithmetic.product(dims), //
                dims, strides);

        strides = IndexingOrder.FAR.strides(dims);

        if (ndims <= 2) {

            f.format("%n");

            formatSlice(f, format, //
                    values, indices, 0, nrows, ncols, false);

            return f.toString();
        }

        for (int offset = 0, m = values.length; offset < m; offset += sliceSize) {

            f.format("%n[slice (");

            for (int i = 0, n = ndims - 2, offsetAcc = offset; i < n; offsetAcc %= strides[i], i++) {
                f.format("%d, ", offsetAcc / strides[i]);
            }

            f.format(":, :)]%n");

            formatSlice(f, format, //
                    values, indices, offset, nrows, ncols, false);
        }

        return f.toString();
    }

    /**
     * Assumes that this array results from an indexing operation. Extracts the valid indices along a dimension anchored
     * at the given logical index.
     * 
     * @param s
     *            the logical index, where the dimension of interest is marked with a {@code -1}.
     * @return the valid indices along said dimension and at said logical index.
     */
    public int[] find(int... s) {

        IntegerArray a = this;

        return OpKernel.find(a.values, a.dims, a.strides, s);
    }

    /**
     * Converts this array to a {@link RealArray}.
     */
    public RealArray tor() {

        IntegerArray a = this;

        RealArray res = new RealArray(a.order, a.dims);

        OpKernel.convert(ITOR, a.values, false, res.values, false);

        return res;
    }

    /**
     * Mutatively adds the argument to the elements.
     */
    public IntegerArray uAdd(int a) {
        return applyKernelIntegerUnaryOperation(a, ArrayKernel.IU_ADD);
    }

    /**
     * Mutatively multiplies the elements by the argument.
     */
    public IntegerArray uMul(int a) {
        return applyKernelIntegerUnaryOperation(a, ArrayKernel.IU_MUL);
    }

    /**
     * Mutatively fills this array with the argument.
     */
    public IntegerArray uFill(int a) {
        return applyKernelIntegerUnaryOperation(a, ArrayKernel.IU_FILL);
    }

    /**
     * Mutatively shuffles this array.
     */
    public IntegerArray uShuffle() {
        return applyKernelIntegerUnaryOperation(Integer.MIN_VALUE, ArrayKernel.IU_SHUFFLE);
    }

    /**
     * Computes the elementwise addition.
     */
    public IntegerArray eAdd(IntegerArray array) {
        return applyKernelElementwiseOperation(array, ArrayKernel.IE_ADD);
    }

    /**
     * Computes the elementwise subtraction.
     */
    public IntegerArray eSub(IntegerArray array) {
        return applyKernelElementwiseOperation(array, ArrayKernel.IE_SUB);
    }

    /**
     * Computes the elementwise multiplication.
     */
    public IntegerArray eMul(IntegerArray array) {
        return applyKernelElementwiseOperation(array, ArrayKernel.IE_MUL);
    }

    /**
     * Computes the elementwise maximum.
     */
    public IntegerArray eMax(IntegerArray array) {
        return applyKernelElementwiseOperation(array, ArrayKernel.IE_MAX);
    }

    /**
     * Computes the elementwise minimum.
     */
    public IntegerArray eMin(IntegerArray array) {
        return applyKernelElementwiseOperation(array, ArrayKernel.IE_MIN);
    }

    /**
     * Computes the left elementwise addition.
     */
    public IntegerArray lAdd(IntegerArray array) {
        return applyKernelLeftElementwiseOperation(array, ArrayKernel.IE_ADD);
    }

    /**
     * Computes the left elementwise subtraction.
     */
    public IntegerArray lSub(IntegerArray array) {
        return applyKernelLeftElementwiseOperation(array, ArrayKernel.IE_SUB);
    }

    /**
     * Computes the left elementwise multiplication.
     */
    public IntegerArray lMul(IntegerArray array) {
        return applyKernelLeftElementwiseOperation(array, ArrayKernel.IE_MUL);
    }

    /**
     * Computes the left elementwise maximum.
     */
    public IntegerArray lMax(IntegerArray array) {
        return applyKernelLeftElementwiseOperation(array, ArrayKernel.IE_MAX);
    }

    /**
     * Computes the left elementwise minimum.
     */
    public IntegerArray lMin(IntegerArray array) {
        return applyKernelLeftElementwiseOperation(array, ArrayKernel.IE_MIN);
    }

    /**
     * Supports the e* series of operations.
     */
    protected IntegerArray applyKernelElementwiseOperation(IntegerArray b, int type) {

        checkShape(b);

        IntegerArray a = this;
        IntegerArray res = wrap(a.order, a.dims, a.strides);

        OpKernel.eOp(type, a.values, b.values, res.values, false);

        return res;
    }

    /**
     * Supports the l* series of operations.
     */
    protected IntegerArray applyKernelLeftElementwiseOperation(IntegerArray b, int type) {

        checkShape(b);

        IntegerArray a = this;

        OpKernel.eOp(type, a.values, b.values, a.values, false);

        return a;
    }

    /**
     * Supports the u* series of operations.
     */
    protected IntegerArray applyKernelIntegerUnaryOperation(int a, int type) {

        OpKernel.iuOp(type, a, this.values);

        return this;
    }

    /**
     * Checks that two {@link IntegerArray}s have the same size and underlying {@link Array.IndexingOrder}.
     * 
     * @param b
     *            the {@link IntegerArray} to compare to.
     */
    protected void checkShape(IntegerArray b) {

        IntegerArray a = this;

        Control.checkTrue(a.order == b.order, //
                "Indexing orders do not match");

        Control.checkTrue(Arrays.equals(a.dims, b.dims), //
                "Dimensions do not match");
    }

    /**
     * Parses an array from {@code byte}s.
     */
    final public static IntegerArray parse(byte[] data) {
        return IOKernel.parse(data);
    }
}

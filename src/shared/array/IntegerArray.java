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

package shared.array;

import static shared.array.ArrayBase.IOKernel;
import static shared.array.ArrayBase.OpKernel;
import static shared.array.kernel.ArrayKernel.I_TO_R;

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
        this(0, values, IndexingOrder.FAR, ArrayBase.inferDimensions(dims, values.length, false));
    }

    /**
     * Alternate constructor.
     */
    public IntegerArray(int[] values, IndexingOrder order, int... dims) {
        this(0, values, order, ArrayBase.inferDimensions(dims, values.length, false));
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
    public byte[] getBytes() {
        return IOKernel.getBytes(this);
    }

    @Override
    public Class<Integer> getComponentType() {
        return Integer.class;
    }

    @Override
    public String toString() {

        int[] values = this.values;
        int[] dims = this.dims;
        int[] strides = this.strides;

        int nDims = dims.length;
        int nRows = (nDims == 1) ? 1 : size(nDims - 2);
        int nCols = size(nDims - 1);
        int sliceSize = nRows * nCols;

        int max = Math.max(Arithmetic.max(values), Math.abs(Arithmetic.min(values)));
        int exponent = max > 0 ? (int) Math.log10(max) : 0;

        Formatter f = new Formatter();

        if (values.length == 0) {

            ArrayBase.formatEmptyArray(f, dims);

            return f.toString();
        }

        String format = String.format("%%%dd", exponent + 3);

        int[] indices = MappingOps.assignMappingIndices(Arithmetic.product(dims), //
                dims, strides);

        strides = IndexingOrder.FAR.strides(dims);

        if (nDims <= 2) {

            f.format("%n");

            ArrayBase.formatSlice(f, format, //
                    values, indices, 0, nRows, nCols, false);

            return f.toString();
        }

        for (int offset = 0, m = values.length; offset < m; offset += sliceSize) {

            f.format("%n[slice (");

            for (int i = 0, n = nDims - 2, offsetAcc = offset; i < n; offsetAcc %= strides[i], i++) {
                f.format("%d, ", offsetAcc / strides[i]);
            }

            f.format(":, :)]%n");

            ArrayBase.formatSlice(f, format, //
                    values, indices, offset, nRows, nCols, false);
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

        OpKernel.convert(I_TO_R, a.values, false, res.values, false);

        return res;
    }

    /**
     * Gets the singleton value from this array.
     */
    public int singleton() {

        Control.checkTrue(this.values.length == 1, //
                "Array must contain exactly one value");

        return this.values[0];
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

    /**
     * Generates arrays representing integral coordinates along each dimension of a multidimensional function's domain.
     * Intended to mimic the behavior of the MATLAB function of the same name.
     * 
     * @param ranges
     *            the range specifications as an array of three-tuples. Given the <code>i</code>th tuple, the first
     *            component denotes the start value, the second component denotes the end value, and the third component
     *            denotes the step size.
     * @return an array of <code>n</code> {@link IntegerArray}s, where <code>n</code> is the number of dimensions.
     */
    final public static IntegerArray[] ndgrid(int... ranges) {

        Control.checkTrue(ranges.length % 3 == 0, //
                "Invalid range specifications");

        int nDims = ranges.length / 3;
        int[] dims = new int[nDims];

        IntegerArray[] arrays = new IntegerArray[nDims];

        for (int dim = 0, offset = 0; dim < nDims; dim++, offset += 3) {

            int start = ranges[offset];
            int end = ranges[offset + 1];
            int step = ranges[offset + 2];

            int[] values = Arithmetic.range(start, end, step);

            Arrays.fill(dims, 1);
            dims[dim] = values.length;

            arrays[dim] = new IntegerArray(values, dims);
        }

        for (int dim = 0; dim < nDims; dim++) {
            dims[dim] = arrays[dim].size(dim);
        }

        for (int dim = 0; dim < nDims; dim++) {

            int dimSize = dims[dim];

            dims[dim] = 1;
            arrays[dim] = arrays[dim].tile(dims);
            dims[dim] = dimSize;
        }

        return arrays;
    }
}

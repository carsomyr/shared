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

package shared.array.sparse;

import static shared.array.ArrayBase.DEFAULT_ORDER;
import static shared.array.ArrayBase.OpKernel;
import static shared.array.ArrayBase.formatSparseArray;

import java.util.Formatter;

import shared.array.ArrayBase;
import shared.array.IntegerArray;
import shared.util.Arithmetic;
import shared.util.Arrays;
import shared.util.Control;

/**
 * A sparse integer array class.
 * 
 * @author Roy Liu
 */
public class IntegerSparseArray extends ProtoSparseArray<IntegerSparseArray, int[], Integer, IntegerArray> {

    /**
     * An empty array.
     */
    final protected static int[] Empty = new int[] {};

    /**
     * Default constructor.
     */
    public IntegerSparseArray(int... dims) {
        this(0, dims.clone());
    }

    /**
     * Internal constructor with a distinctive signature.
     */
    protected IntegerSparseArray(int unused, int[] dims) {
        super(new SparseArrayState<int[]>(Empty, dims), //
                dims, DEFAULT_ORDER.strides(dims), createDimensionOffsets(dims));

        Control.checkTrue(dims.length > 0);
    }

    /**
     * Alternate constructor.
     */
    public IntegerSparseArray(IntegerSparseArray array) {
        this(array.state, array.dims, array.strides, array.dimOffsets);
    }

    /**
     * Internal constructor for package use only.
     */
    protected IntegerSparseArray(SparseArrayState<int[]> state, int[] dims, int[] strides, int[] dimOffsets) {
        super(state, dims, strides, dimOffsets);
    }

    public Class<Integer> getComponentType() {
        return Integer.class;
    }

    @Override
    protected IntegerSparseArray wrap(Integer value, int[] dims, int[] strides, int[] dimOffsets) {

        final SparseArrayState<int[]> state;

        if (value == null) {

            state = new SparseArrayState<int[]>(Empty, dims);

        } else {

            int[] values = Arrays.newArray(Arithmetic.product(dims), value.intValue());

            state = OpKernel.insertSparse( //
                    Empty, dims, strides, dimOffsets, EmptyIndices, //
                    values, Arithmetic.range(values.length));
        }

        return new IntegerSparseArray(state, dims, strides, dimOffsets);
    }

    @Override
    protected IntegerSparseArray wrap(SparseArrayState<int[]> state, int[] dims, int[] strides, int[] dimOffsets) {
        return new IntegerSparseArray(state, dims, strides, dimOffsets);
    }

    @Override
    protected int length(int[] values) {
        return values.length;
    }

    @Override
    protected int[] empty() {
        return Empty;
    }

    @Override
    public IntegerArray toDense() {

        IntegerSparseArray src = this;

        IntegerArray dst = new IntegerArray(src.order(), src.dims);

        SparseArrayState<int[]> srcState = src.state;

        int[] srcValues = srcState.values;
        int[] srcIndices = srcState.indices;

        int[] dstValues = dst.values();

        for (int i = 0, n = srcValues.length; i < n; i++) {
            dstValues[srcIndices[i]] = srcValues[i];
        }

        return dst;
    }

    @Override
    public String toString() {

        SparseArrayState<int[]> state = this.state;

        int[] values = state.values;
        int[] indices = state.indices;
        int[] dims = this.dims;
        int[] strides = this.strides;

        int max = Math.max(Arithmetic.max(values), Math.abs(Arithmetic.min(values)));
        int exponent = max > 0 ? (int) Math.log10(max) : 0;

        int maxIndex = Arithmetic.max(dims);
        int exponentIndex = maxIndex > 0 ? (int) Math.log10(maxIndex) : 0;

        Formatter f = new Formatter();

        if (values.length == 0) {

            ArrayBase.formatEmptyArray(f, dims);

            return f.toString();
        }

        String valueFormat = String.format("%%%dd", exponent + 3);
        String indexFormat = String.format("%%%dd", exponentIndex + 2);

        formatSparseArray(f, valueFormat, indexFormat, values, indices, strides);

        return f.toString();
    }
}

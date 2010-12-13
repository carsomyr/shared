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

package org.shared.array.sparse;

import static org.shared.array.ArrayBase.DEFAULT_ORDER;
import static org.shared.array.ArrayBase.opKernel;

import java.util.Formatter;

import org.shared.array.ArrayBase;
import org.shared.array.IntegerArray;
import org.shared.util.Arithmetic;
import org.shared.util.Arrays;
import org.shared.util.Control;

/**
 * A sparse integer array class.
 * 
 * @author Roy Liu
 */
public class IntegerSparseArray extends ProtoSparseArray<IntegerSparseArray, int[], Integer, IntegerArray> {

    /**
     * An empty array.
     */
    final protected static int[] empty = new int[] {};

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
        super(new SparseArrayState<int[]>(empty, dims), //
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

    @Override
    public Class<Integer> getComponentType() {
        return Integer.class;
    }

    @Override
    protected IntegerSparseArray wrap(Integer value, int[] dims, int[] strides, int[] dimOffsets) {

        final SparseArrayState<int[]> state;

        if (value == null) {

            state = new SparseArrayState<int[]>(empty, dims);

        } else {

            int[] values = Arrays.newArray(Arithmetic.product(dims), value.intValue());

            state = opKernel.insertSparse( //
                    empty, dims, strides, dimOffsets, emptyIndices, //
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
        return empty;
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

        ArrayBase.formatSparseArray(f, valueFormat, indexFormat, values, indices, strides);

        return f.toString();
    }
}

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

import java.lang.reflect.Array;
import java.util.Formatter;

import shared.array.ArrayBase;
import shared.array.ObjectArray;
import shared.util.Arithmetic;
import shared.util.Arrays;
import shared.util.Control;

/**
 * A sparse object array class.
 * 
 * @param <T>
 *            the storage type.
 */
public class ObjectSparseArray<T> extends ProtoSparseArray<ObjectSparseArray<T>, T[], T, ObjectArray<T>> {

    /**
     * Default constructor.
     */
    public ObjectSparseArray(Class<T> clazz, int... dims) {
        this(0, clazz, dims.clone());
    }

    /**
     * Internal constructor with a distinctive signature.
     */
    @SuppressWarnings("unchecked")
    protected ObjectSparseArray(int unused, Class<T> clazz, int[] dims) {
        super(new SparseArrayState<T[]>((T[]) Array.newInstance(clazz, 0), dims), //
                dims, DEFAULT_ORDER.strides(dims), createDimensionOffsets(dims));

        Control.checkTrue(dims.length > 0);
    }

    /**
     * Alternate constructor.
     */
    public ObjectSparseArray(ObjectSparseArray<T> array) {
        this(array.state, array.dims, array.strides, array.dimOffsets);
    }

    /**
     * Internal constructor for package use only.
     */
    protected ObjectSparseArray(SparseArrayState<T[]> state, int[] dims, int[] strides, int[] dimOffsets) {
        super(state, dims, strides, dimOffsets);
    }

    @SuppressWarnings("unchecked")
    public Class<T> getComponentType() {
        return (Class<T>) this.state.values.getClass().getComponentType();
    }

    @Override
    protected ObjectSparseArray<T> wrap(T value, int[] dims, int[] strides, int[] dimOffsets) {

        final SparseArrayState<T[]> state;

        if (value == null) {

            state = new SparseArrayState<T[]>(empty(), dims);

        } else {

            T[] values = Arrays.newArray(getComponentType(), Arithmetic.product(dims), value);

            state = OpKernel.insertSparse( //
                    empty(), dims, strides, dimOffsets, EmptyIndices, //
                    values, Arithmetic.range(values.length));
        }

        return new ObjectSparseArray<T>(state, dims, strides, dimOffsets);
    }

    @Override
    protected ObjectSparseArray<T> wrap(SparseArrayState<T[]> state, int[] dims, int[] strides, int[] dimOffsets) {
        return new ObjectSparseArray<T>(state, dims, strides, dimOffsets);
    }

    @Override
    protected int length(T[] values) {
        return values.length;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected T[] empty() {
        return (T[]) Array.newInstance(getComponentType(), 0);
    }

    @Override
    public ObjectArray<T> toDense() {

        ObjectSparseArray<T> src = this;

        ObjectArray<T> dst = new ObjectArray<T>(getComponentType(), src.order(), src.dims);

        SparseArrayState<T[]> srcState = src.state;

        T[] srcValues = srcState.values;
        int[] srcIndices = srcState.indices;

        T[] dstValues = dst.values();

        for (int i = 0, n = srcValues.length; i < n; i++) {
            dstValues[srcIndices[i]] = srcValues[i];
        }

        return dst;
    }

    @Override
    public String toString() {

        SparseArrayState<T[]> state = this.state;

        T[] values = state.values;
        int[] indices = state.indices;
        int[] dims = this.dims;
        int[] strides = this.strides;

        int maxIndex = Arithmetic.max(dims);
        int exponentIndex = maxIndex > 0 ? (int) Math.log10(maxIndex) : 0;

        Formatter f = new Formatter();

        if (values.length == 0) {

            ArrayBase.formatEmptyArray(f, dims);

            return f.toString();
        }

        String valueFormat = String.format(" \"%%s\"");
        String indexFormat = String.format("%%%dd", exponentIndex + 2);

        formatSparseArray(f, valueFormat, indexFormat, values, indices, strides);

        return f.toString();
    }
}

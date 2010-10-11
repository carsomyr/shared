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

package shared.array.sparse;

import static shared.array.ArrayBase.DEFAULT_ORDER;
import static shared.array.ArrayBase.opKernel;

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
    @Override
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

            state = opKernel.insertSparse( //
                    empty(), dims, strides, dimOffsets, emptyIndices, //
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

        ArrayBase.formatSparseArray(f, valueFormat, indexFormat, values, indices, strides);

        return f.toString();
    }
}

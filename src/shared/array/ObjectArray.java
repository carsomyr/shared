/**
 * <p>
 * Copyright (C) 2007 Roy Liu<br />
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

import static shared.array.ArrayBase.formatEmptyArray;
import static shared.array.ArrayBase.formatSlice;

import java.lang.reflect.Array;
import java.util.Formatter;

import shared.array.kernel.DimensionOps;
import shared.array.kernel.MappingOps;
import shared.array.kernel.PermutationEntry;
import shared.util.Arithmetic;
import shared.util.Control;

/**
 * A multidimensional object array class.
 * 
 * @apiviz.uses shared.array.ArrayBase
 * @param <T>
 *            the storage type.
 * @author Roy Liu
 */
public class ObjectArray<T> extends ProtoArray<ObjectArray<T>, T[], T> {

    /**
     * Default constructor.
     */
    public ObjectArray(Class<T> clazz, int... dims) {
        this(0, clazz, IndexingOrder.FAR, dims);
    }

    /**
     * Alternate constructor.
     */
    public ObjectArray(Class<T> clazz, IndexingOrder order, int... dims) {
        this(0, clazz, order, dims);
    }

    /**
     * Internal constructor with a distinctive signature.
     */
    @SuppressWarnings("unchecked")
    protected ObjectArray(int unused, Class<T> clazz, IndexingOrder order, int[] dims) {
        super((T[]) Array.newInstance(clazz, Arithmetic.product(dims)), order, dims, order.strides(dims));

        Control.checkTrue(dims.length > 0);
    }

    /**
     * Alternate constructor.
     */
    public ObjectArray(T[] values, int... dims) {
        this(0, values, IndexingOrder.FAR, //
                ArrayBase.inferDimensions(dims, values.length, false));
    }

    /**
     * Alternate constructor.
     */
    public ObjectArray(T[] values, IndexingOrder order, int... dims) {
        this(0, values, order, //
                ArrayBase.inferDimensions(dims, values.length, false));
    }

    /**
     * Alternate constructor.
     */
    public ObjectArray(ObjectArray<T> array) {
        this(0, array.values.clone(), array.order, array.dims);
    }

    /**
     * Internal constructor with a distinctive signature.
     */
    protected ObjectArray(int unused, T[] values, IndexingOrder order, int[] dims) {
        super(values, order, dims, order.strides(dims));

        Control.checkTrue(dims.length > 0 //
                && values.length == Arithmetic.product(dims));
    }

    /**
     * Internal constructor for package use only.
     */
    @SuppressWarnings("unchecked")
    protected ObjectArray(Class<T> clazz, IndexingOrder order, int[] dims, int[] strides) {
        super((T[]) Array.newInstance(clazz, Arithmetic.product(dims)), order, dims, strides);
    }

    @Override
    protected ObjectArray<T> wrap(IndexingOrder order, int[] dims, int[] strides) {
        return new ObjectArray<T>(getComponentType(), order, dims, strides);
    }

    @Override
    protected ObjectArray<T> wrap(T value, IndexingOrder order, int[] dims, int[] strides) {

        ObjectArray<T> tmp = wrap(order, dims, strides);
        java.util.Arrays.fill(tmp.values, value);

        return tmp;
    }

    @SuppressWarnings("unchecked")
    public Class<T> getComponentType() {
        return (Class<T>) this.values.getClass().getComponentType();
    }

    /**
     * Gets the value at the given logical index.
     */
    public T get(int... s) {
        return this.values[physical(s)];
    }

    /**
     * Sets the value at the given logical index.
     */
    public void set(T value, int... s) {
        this.values[physical(s)] = value;
    }

    @Override
    public T[] values() {
        return this.values;
    }

    @Override
    public byte[] getBytes() {
        throw new UnsupportedOperationException("Serialization of object arrays is not yet supported");
    }

    @Override
    public String toString() {

        T[] values = this.values;
        int[] dims = this.dims;
        int[] strides = this.strides;

        int nDims = dims.length;
        int nRows = (nDims == 1) ? 1 : size(nDims - 2);
        int nCols = size(nDims - 1);
        int sliceSize = nRows * nCols;

        Formatter f = new Formatter();

        if (values.length == 0) {

            formatEmptyArray(f, dims);

            return f.toString();
        }

        int[] indices = MappingOps.assignMappingIndices(Arithmetic.product(dims), //
                dims, strides);

        strides = IndexingOrder.FAR.strides(dims);

        if (nDims <= 2) {

            f.format("%n");

            formatSlice(f, " \"%s\"", //
                    values, indices, 0, nRows, nCols, false);

            return f.toString();
        }

        for (int offset = 0, m = values.length; offset < m; offset += sliceSize) {

            f.format("%n[slice (");

            for (int i = 0, n = nDims - 2, offsetAcc = offset; i < n; offsetAcc %= strides[i], i++) {
                f.format("%d, ", offsetAcc / strides[i]);
            }

            f.format(":, :)]%n");

            formatSlice(f, " \"%s\"", //
                    values, indices, offset, nRows, nCols, false);
        }

        return f.toString();
    }

    /**
     * Sorts along the given dimension.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public IntegerArray iSort(int dim) {

        ObjectArray<T> src = this;
        IntegerArray dst = new IntegerArray(src.order, src.dims);

        T[] srcV = src.values;
        int[] srcD = src.dims;
        int[] srcS = src.strides;

        int[] dstV = dst.values();

        int srcLen = MappingOps.checkDimensions(srcV.length, srcD, srcS);

        Control.checkTrue(srcLen == dstV.length, //
                "Invalid arguments");

        if (srcLen == 0) {
            return dst;
        }

        if (dim != -1) {

            PermutationEntry.iSort(((ObjectArray<Comparable>) this).values(), //
                    DimensionOps.assignBaseIndices(srcLen / srcD[dim], srcD, srcS, dim), //
                    dstV, //
                    srcD[dim], srcS[dim]);

        } else {

            PermutationEntry.iSort(((ObjectArray<Comparable>) this).values(), //
                    null, dstV, -1, -1);
        }

        return dst;
    }

    /**
     * Gets the singleton value from this array.
     */
    public T singleton() {

        Control.checkTrue(this.values.length == 1, //
                "Array must contain exactly one value");

        return this.values[0];
    }
}

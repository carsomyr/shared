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
import static shared.array.ArrayBase.canonicalizeSlices;
import static shared.array.ArrayBase.createReverseSlices;

import java.util.Arrays;

import shared.util.Arithmetic;
import shared.util.Control;

/**
 * An abstract, primordial base class for all multidimensional arrays. Supports operations like mapping, slicing,
 * tiling, shifting, transposition of dimensions, and storage order reversals.
 * 
 * @param <T>
 *            the parameterization lower bounded by {@link ProtoArray} itself.
 * @param <V>
 *            the storage array type.
 * @param <E>
 *            the element type.
 * @author Roy Liu
 */
abstract public class ProtoArray<T extends ProtoArray<T, V, E>, V, E> implements Array<T, E> {

    /**
     * The backing values.
     */
    final protected V values;

    /**
     * The storage order.
     */
    final protected IndexingOrder order;

    /**
     * The dimensions.
     */
    final protected int[] dims;

    /**
     * The strides.
     */
    final protected int[] strides;

    /**
     * Default constructor.
     */
    protected ProtoArray(V values, IndexingOrder order, int[] dims, int[] strides) {

        this.values = values;
        this.order = order;
        this.dims = dims;
        this.strides = strides;
    }

    /**
     * Allocates a new array.
     * 
     * @param order
     *            the storage order.
     * @param dims
     *            the dimensions.
     * @param strides
     *            the strides.
     * @return the new array.
     */
    abstract protected T wrap(IndexingOrder order, int[] dims, int[] strides);

    /**
     * Allocates a new array initialized to the given value.
     * 
     * @param value
     *            the initial value.
     * @param order
     *            the storage order.
     * @param dims
     *            the dimensions.
     * @param strides
     *            the strides.
     * @return the new array.
     */
    abstract protected T wrap(E value, IndexingOrder order, int[] dims, int[] strides);

    /**
     * Converts this array into {@code byte}s.
     * 
     * @return the {@code byte}s.
     */
    @SuppressWarnings("unchecked")
    public byte[] getBytes() {
        return IOKernel.getBytes((T) this);
    }

    /**
     * Gets the backing values.
     */
    protected V values() {
        return this.values;
    }

    /**
     * Converts a logical index into a physical index.
     */
    protected int physical(int[] logical) {

        int index = 0;

        for (int i = 0, n = logical.length; i < n; i++) {
            index += this.strides[i] * logical[i];
        }

        return index;
    }

    public T map(T dst, int... bounds) {

        ProtoArray<T, V, E> src = this;

        Control.checkTrue(src != dst, //
                "Source and destination cannot be the same");

        OpKernel.map(bounds, //
                src.values, src.dims, src.strides, //
                dst.values, dst.dims, dst.strides);

        return dst;
    }

    public T splice(T dst, int... slices) {

        ProtoArray<T, V, E> src = this;

        Control.checkTrue(src != dst, //
                "Source and destination cannot be the same");

        OpKernel.slice(slices, //
                src.values, src.dims, src.strides, //
                dst.values, dst.dims, dst.strides);

        return dst;
    }

    public T slice(int[][] srcSlices, T dst, int[][] dstSlices) {

        ProtoArray<T, V, E> src = this;

        Control.checkTrue(src != dst, //
                "Source and destination cannot be the same");

        int[] slices = ArrayBase.canonicalizeSlices(srcSlices, src.dims, dstSlices, dst.dims);

        OpKernel.slice(slices, //
                src.values, src.dims, src.strides, //
                dst.values, dst.dims, dst.strides);

        return dst;
    }

    public T slice(T dst, int[]... dstSlices) {

        ProtoArray<T, V, E> src = this;

        Control.checkTrue(src != dst, //
                "Source and destination cannot be the same");

        OpKernel.slice(canonicalizeSlices(src.dims, dst.dims, dstSlices), //
                src.values, src.dims, src.strides, //
                dst.values, dst.dims, dst.strides);

        return dst;
    }

    @SuppressWarnings("unchecked")
    public T slice(E value, int[]... srcSlices) {

        T src = (T) this;

        int ndims = srcSlices.length;
        int[] dstDims = new int[ndims];

        for (int dim = 0; dim < ndims; dim++) {
            dstDims[dim] = srcSlices[dim].length;
        }

        return wrap(value, src.order, dstDims, src.order.strides(dstDims)).slice(src, srcSlices);
    }

    public T slice(int[]... srcSlices) {

        ProtoArray<T, V, E> src = this;

        int ndims = Control.checkEquals(src.dims.length, srcSlices.length, //
                "Dimensionality mismatch");

        int nslices = 0;
        int[] dstDims = new int[ndims];

        for (int dim = 0; dim < ndims; dim++) {
            nslices += (dstDims[dim] = srcSlices[dim].length);
        }

        T dst = wrap(src.order, dstDims, src.order.strides(dstDims));

        OpKernel.slice(canonicalizeSlices(nslices, src.dims, srcSlices), //
                src.values, src.dims, src.strides, //
                dst.values, dst.dims, dst.strides);

        return dst;
    }

    public T tile(int... repetitions) {

        ProtoArray<T, V, E> src = this;

        int ndims = Control.checkEquals(src.dims.length, repetitions.length, //
                "Dimensionality mismatch");

        int[] newDims = src.dims.clone();

        for (int dim = 0; dim < ndims; dim++) {
            newDims[dim] *= repetitions[dim];
        }

        T dst = wrap(src.order, newDims, src.order.strides(newDims));

        int[] mappingBounds = new int[3 * ndims];

        for (int dim = 0, offset = 0; dim < ndims; dim++, offset += 3) {
            mappingBounds[offset + 2] = dst.dims[dim];
        }

        OpKernel.map(mappingBounds, //
                src.values, src.dims, src.strides, //
                dst.values, dst.dims, dst.strides);

        return dst;
    }

    public T transpose(int... permutation) {

        ProtoArray<T, V, E> src = this;

        int ndims = Control.checkEquals(src.dims.length, permutation.length, //
                "Dimensionality mismatch");

        int[] newDims = new int[ndims];
        int[] copy = permutation.clone();

        Arrays.sort(copy);

        for (int dim = 0; dim < ndims; dim++) {

            newDims[permutation[dim]] = src.dims[dim];

            Control.checkTrue(copy[dim] == dim, //
                    "Invalid permutation");
        }

        T dst = wrap(src.order, newDims, src.order.strides(newDims));

        int[] mappingBounds = new int[3 * ndims];

        for (int dim = 0, offset = 0; dim < ndims; dim++, offset += 3) {
            mappingBounds[offset + 2] = src.dims[dim];
        }

        for (int dim = 0; dim < ndims; dim++) {
            copy[dim] = dst.strides[permutation[dim]];
        }

        OpKernel.map(mappingBounds, //
                src.values, src.dims, src.strides, //
                dst.values, src.dims, copy);

        return dst;
    }

    public T shift(int... shifts) {

        ProtoArray<T, V, E> src = this;

        int ndims = Control.checkEquals(src.dims.length, shifts.length, //
                "Dimensionality mismatch");

        int[] mappingBounds = new int[3 * ndims];

        for (int dim = 0, offset = 0; dim < ndims; dim++, offset += 3) {

            mappingBounds[offset + 1] = shifts[dim];
            mappingBounds[offset + 2] = src.dims[dim];
        }

        T dst = wrap(src.order, src.dims, src.strides);

        OpKernel.map(mappingBounds, //
                src.values, src.dims, src.strides, //
                dst.values, dst.dims, dst.strides);

        return dst;
    }

    public T reverseOrder() {

        ProtoArray<T, V, E> src = this;

        IndexingOrder newOrder = src.order.reverse();
        T dst = wrap(newOrder, src.dims, newOrder.strides(src.dims));

        int ndims = src.dims.length;

        int[] mappingBounds = new int[3 * ndims];

        for (int dim = 0, offset = 0; dim < ndims; dim++, offset += 3) {
            mappingBounds[offset + 2] = src.dims[dim];
        }

        OpKernel.map(mappingBounds, //
                src.values, src.dims, src.strides, //
                dst.values, dst.dims, dst.strides);

        return dst;
    }

    public T subarray(int... bounds) {

        ProtoArray<T, V, E> src = this;

        int ndims = src.dims.length;

        Control.checkTrue(ndims * 2 == bounds.length, //
                "Invalid subarray bounds");

        int[] newDims = new int[ndims];
        int[] mappingBounds = new int[3 * ndims];

        for (int dim = 0; dim < ndims; dim++) {

            int lower = bounds[2 * dim];
            int upper = bounds[2 * dim + 1];

            newDims[dim] = upper - lower;
            mappingBounds[3 * dim] = lower;
            mappingBounds[3 * dim + 2] = newDims[dim];
        }

        T dst = wrap(src.order, newDims, src.order.strides(newDims));

        OpKernel.map(mappingBounds, //
                src.values, src.dims, src.strides, //
                dst.values, dst.dims, dst.strides);

        return dst;
    }

    public T reverse(int... selectedDims) {

        ProtoArray<T, V, E> src = this;

        T dst = wrap(src.order, src.dims, src.strides);

        OpKernel.slice(createReverseSlices(src.dims, selectedDims), //
                src.values, src.dims, src.strides, //
                dst.values, dst.dims, dst.strides);

        return dst;
    }

    public T reshape(int... dims) {

        ProtoArray<T, V, E> src = this;

        int len = Control.checkEquals( //
                Arithmetic.product(dims), Arithmetic.product(src.dims), //
                "Cardinality mismatch");

        T dst = wrap(src.order, dims, src.order.strides(dims));

        System.arraycopy(src.values, 0, dst.values, 0, len);

        return dst;
    }

    @Override
    public T clone() {

        ProtoArray<T, V, E> src = this;

        T dst = wrap(src.order, src.dims, src.strides);

        System.arraycopy(src.values, 0, dst.values, 0, Arithmetic.product(src.dims));

        return dst;
    }

    public IndexingOrder order() {
        return this.order;
    }

    public int size(int i) {
        return this.dims[i];
    }

    public int stride(int i) {
        return this.strides[i];
    }

    public int ndims() {
        return this.dims.length;
    }

    public int[] dims() {
        return this.dims.clone();
    }

    public int[] strides() {
        return this.strides.clone();
    }
}

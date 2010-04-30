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

        int nDims = srcSlices.length;
        int[] dstDims = new int[nDims];

        for (int dim = 0; dim < nDims; dim++) {
            dstDims[dim] = srcSlices[dim].length;
        }

        return wrap(value, src.order, dstDims, src.order.strides(dstDims)).slice(src, srcSlices);
    }

    public T slice(int[]... srcSlices) {

        ProtoArray<T, V, E> src = this;

        int nDims = Control.checkEquals(src.dims.length, srcSlices.length, //
                "Dimensionality mismatch");

        int nSlices = 0;
        int[] dstDims = new int[nDims];

        for (int dim = 0; dim < nDims; dim++) {
            nSlices += (dstDims[dim] = srcSlices[dim].length);
        }

        T dst = wrap(src.order, dstDims, src.order.strides(dstDims));

        OpKernel.slice(canonicalizeSlices(nSlices, src.dims, srcSlices), //
                src.values, src.dims, src.strides, //
                dst.values, dst.dims, dst.strides);

        return dst;
    }

    public T tile(int... repetitions) {

        ProtoArray<T, V, E> src = this;

        int nDims = Control.checkEquals(src.dims.length, repetitions.length, //
                "Dimensionality mismatch");

        int[] newDims = src.dims.clone();

        for (int dim = 0; dim < nDims; dim++) {
            newDims[dim] *= repetitions[dim];
        }

        T dst = wrap(src.order, newDims, src.order.strides(newDims));

        int[] mappingBounds = new int[3 * nDims];

        for (int dim = 0, offset = 0; dim < nDims; dim++, offset += 3) {
            mappingBounds[offset + 2] = dst.dims[dim];
        }

        OpKernel.map(mappingBounds, //
                src.values, src.dims, src.strides, //
                dst.values, dst.dims, dst.strides);

        return dst;
    }

    public T transpose(int... permutation) {

        ProtoArray<T, V, E> src = this;

        int nDims = Control.checkEquals(src.dims.length, permutation.length, //
                "Dimensionality mismatch");

        int[] newDims = new int[nDims];
        int[] copy = permutation.clone();

        Arrays.sort(copy);

        for (int dim = 0; dim < nDims; dim++) {

            newDims[permutation[dim]] = src.dims[dim];

            Control.checkTrue(copy[dim] == dim, //
                    "Invalid permutation");
        }

        T dst = wrap(src.order, newDims, src.order.strides(newDims));

        int[] mappingBounds = new int[3 * nDims];

        for (int dim = 0, offset = 0; dim < nDims; dim++, offset += 3) {
            mappingBounds[offset + 2] = src.dims[dim];
        }

        for (int dim = 0; dim < nDims; dim++) {
            copy[dim] = dst.strides[permutation[dim]];
        }

        OpKernel.map(mappingBounds, //
                src.values, src.dims, src.strides, //
                dst.values, src.dims, copy);

        return dst;
    }

    public T shift(int... shifts) {

        ProtoArray<T, V, E> src = this;

        int nDims = Control.checkEquals(src.dims.length, shifts.length, //
                "Dimensionality mismatch");

        int[] mappingBounds = new int[3 * nDims];

        for (int dim = 0, offset = 0; dim < nDims; dim++, offset += 3) {

            mappingBounds[offset + 1] = shifts[dim];
            mappingBounds[offset + 2] = src.dims[dim];
        }

        T dst = wrap(src.order, src.dims, src.strides);

        OpKernel.map(mappingBounds, //
                src.values, src.dims, src.strides, //
                dst.values, dst.dims, dst.strides);

        return dst;
    }

    public T subarray(int... bounds) {

        ProtoArray<T, V, E> src = this;

        int nDims = src.dims.length;

        Control.checkTrue(nDims * 2 == bounds.length, //
                "Invalid subarray bounds");

        int[] newDims = new int[nDims];
        int[] mappingBounds = new int[3 * nDims];

        for (int dim = 0; dim < nDims; dim++) {

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

    public T reverse(int... opDims) {

        ProtoArray<T, V, E> src = this;

        T dst = wrap(src.order, src.dims, src.strides);

        OpKernel.slice(createReverseSlices(src.dims, opDims), //
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

    @SuppressWarnings("unchecked")
    public T concat(int opDim, T... srcs) {

        ProtoArray<T, V, E> src = this;

        int nDims = src.dims.length;

        Control.checkTrue(opDim >= 0 && opDim < nDims, //
                "Invalid dimension");

        srcs = Arrays.copyOf(srcs, srcs.length + 1);
        System.arraycopy(srcs, 0, srcs, 1, srcs.length - 1);
        srcs[0] = (T) this;

        int offset = 0;

        for (T elt : srcs) {

            int dimSize = elt.size(opDim);

            for (int dim = 0; dim < opDim; dim++) {
                Control.checkTrue(src.dims[dim] == elt.dims[dim], //
                        "Dimension mismatch");
            }

            for (int dim = opDim + 1; dim < nDims; dim++) {
                Control.checkTrue(src.dims[dim] == elt.dims[dim], //
                        "Dimension mismatch");
            }

            offset += dimSize;
        }

        int[] newDims = src.dims.clone();
        newDims[opDim] = offset;

        int[] mappingBounds = new int[3 * nDims];

        for (int dim = 0; dim < nDims; dim++) {
            mappingBounds[3 * dim + 2] = src.dims[dim];
        }

        T dst = wrap(src.order, newDims, src.order.strides(newDims));

        offset = 0;

        for (T elt : srcs) {

            int dimSize = elt.size(opDim);

            mappingBounds[3 * opDim + 1] = offset;
            mappingBounds[3 * opDim + 2] = dimSize;

            elt.map(dst, mappingBounds);

            offset += dimSize;
        }

        return dst;
    }

    public T reverseOrder() {

        ProtoArray<T, V, E> src = this;

        IndexingOrder newOrder = src.order.reverse();
        T dst = wrap(newOrder, src.dims, newOrder.strides(src.dims));

        int nDims = src.dims.length;

        int[] mappingBounds = new int[3 * nDims];

        for (int dim = 0, offset = 0; dim < nDims; dim++, offset += 3) {
            mappingBounds[offset + 2] = src.dims[dim];
        }

        OpKernel.map(mappingBounds, //
                src.values, src.dims, src.strides, //
                dst.values, dst.dims, dst.strides);

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

    public int nDims() {
        return this.dims.length;
    }

    public int[] dims() {
        return this.dims.clone();
    }

    public int[] strides() {
        return this.strides.clone();
    }
}

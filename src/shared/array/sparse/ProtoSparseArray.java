/**
 * <p>
 * Copyright (C) 2009 Roy Liu<br />
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
import static shared.array.ArrayBase.OpKernel;
import static shared.array.ArrayBase.canonicalizeSlices;
import static shared.array.ArrayBase.createReverseSlices;

import java.util.Arrays;

import shared.array.Array;
import shared.util.Arithmetic;
import shared.util.Control;

/**
 * An abstract, primordial base class for all sparse arrays.
 * 
 * @apiviz.owns shared.array.sparse.SparseArrayState
 * @param <T>
 *            the parameterization lower bounded by {@link ProtoSparseArray} itself.
 * @param <V>
 *            the storage array type.
 * @param <E>
 *            the element type.
 * @param <D>
 *            the dense array type.
 * @author Roy Liu
 */
abstract public class ProtoSparseArray<T extends ProtoSparseArray<T, V, E, D>, V, E, D extends Array<D, E>> implements
        Array<T, E> {

    /**
     * An empty array of indices.
     */
    final protected static int[] EmptyIndices = new int[] {};

    /**
     * The {@link SparseArrayState}.
     */
    volatile protected SparseArrayState<V> state;

    /**
     * The dimensions.
     */
    final protected int[] dims;

    /**
     * The strides.
     */
    final protected int[] strides;

    /**
     * The dimension offsets.
     */
    final protected int[] dimOffsets;

    /**
     * Default constructor.
     */
    protected ProtoSparseArray(SparseArrayState<V> state, int[] dims, int[] strides, int[] dimOffsets) {

        this.state = state;
        this.dims = dims;
        this.strides = strides;
        this.dimOffsets = dimOffsets;
    }

    /**
     * Creates dimension offsets from the given dimensions.
     * 
     * @param dims
     *            the dimensions.
     * @return the dimension offsets.
     */
    final protected static int[] createDimensionOffsets(int[] dims) {

        int nDims = dims.length;
        int[] dimOffsets = new int[nDims + 1];
        int dimOffset = 0;

        for (int dim = 0; dim < nDims; dim++) {

            dimOffsets[dim] = dimOffset;
            dimOffset += dims[dim] + 1;
        }

        dimOffsets[nDims] = dimOffset;

        return dimOffsets;
    }

    /**
     * Allocates a new array.
     * 
     * @param state
     *            the {@link SparseArrayState}.
     * @param dims
     *            the dimensions.
     * @param strides
     *            the strides.
     * @param dimOffsets
     *            the dimension offsets.
     * @return the new array.
     */
    abstract protected T wrap(SparseArrayState<V> state, int[] dims, int[] strides, int[] dimOffsets);

    /**
     * Allocates a new array initialized to the given value.
     * 
     * @param value
     *            the initial value.
     * @param dims
     *            the dimensions.
     * @param strides
     *            the strides.
     * @param dimOffsets
     *            the dimension offsets.
     * @return the new array.
     */
    abstract protected T wrap(E value, int[] dims, int[] strides, int[] dimOffsets);

    /**
     * Gets the length of the given array.
     * 
     * @param values
     *            the array.
     * @return the length.
     */
    abstract protected int length(V values);

    /**
     * Gets an empty array of values.
     */
    abstract protected V empty();

    /**
     * Converts this array to its dense form.
     */
    abstract public D toDense();

    /**
     * Inserts values at the given logical indices, which are given in row major order.
     * 
     * @param values
     *            the values to be inserted.
     * @param logicals
     *            the logical indices.
     * @return this array.
     */
    @SuppressWarnings("unchecked")
    public T insert(V values, int... logicals) {

        ProtoSparseArray<T, V, E, D> src = this;

        int nDims = src.dims.length;
        int newLen = length(values);

        Control.checkTrue(nDims * newLen == logicals.length, //
                "Invalid arguments");

        int[] newI = new int[newLen];

        for (int i = 0; i < newLen; i++) {

            int physical = 0;

            for (int dim = 0; dim < nDims; dim++) {
                physical += src.strides[dim] * logicals[nDims * i + dim];
            }

            newI[i] = physical;
        }

        SparseArrayState<V> srcState = src.state;

        src.state = OpKernel.insertSparse( //
                srcState.values, src.dims, src.strides, src.dimOffsets, srcState.indices, //
                values, newI);

        return (T) src;
    }

    /**
     * Gets the number of non-zero elements.
     */
    public int size() {
        return length(this.state.values);
    }

    @Override
    public T map(T dst, int... bounds) {

        ProtoSparseArray<T, V, E, D> src = this;

        int nDims = Control.checkEquals(src.dims.length, dst.dims.length, //
                "Dimensionality mismatch");

        Control.checkTrue(3 * nDims == bounds.length, //
                "Invalid arguments");

        //

        int nSlices = 0;

        for (int dim = 0, offset = 0; dim < nDims; dim++, offset += 3) {

            int size = bounds[offset + 2];

            Control.checkTrue(size >= 0, //
                    "Invalid mapping parameters");

            nSlices += size;
        }

        if (Arithmetic.product(src.dims) == 0 || Arithmetic.product(dst.dims) == 0) {
            return dst;
        }

        //

        int[] slices = new int[3 * nSlices];

        for (int dim = 0, acc = 0, offset = 0, sliceOffset = 0; dim < nDims; dim++, acc += bounds[offset + 2], offset += 3) {

            int size = bounds[offset + 2];

            for (int j = 0, //
            srcSize = src.dims[dim], //
            srcOffset = (((bounds[offset]) % srcSize) + srcSize) % srcSize, //
            dstSize = dst.dims[dim], //
            dstOffset = (((bounds[offset + 1]) % dstSize) + dstSize) % dstSize; //
            j < size; //
            j++, //
            srcOffset = (srcOffset + 1) % srcSize, //
            dstOffset = (dstOffset + 1) % dstSize, //
            sliceOffset += 3) {

                slices[sliceOffset] = srcOffset;
                slices[sliceOffset + 1] = dstOffset;
                slices[sliceOffset + 2] = dim;
            }
        }

        return splice(dst, slices);
    }

    @Override
    public T splice(T dst, int... slices) {

        ProtoSparseArray<T, V, E, D> src = this;

        Control.checkTrue(src != dst, //
                "Source and destination cannot be the same");

        SparseArrayState<V> srcState = src.state;
        SparseArrayState<V> dstState = dst.state;

        dst.state = OpKernel.sliceSparse(slices, //
                srcState.values, src.dims, src.strides, src.dimOffsets, //
                srcState.indices, srcState.indirectionOffsets, srcState.indirections, //
                dstState.values, dst.dims, dst.strides, dst.dimOffsets, //
                dstState.indices, dstState.indirectionOffsets, dstState.indirections);

        return dst;
    }

    @Override
    public T slice(int[][] srcSlices, T dst, int[][] dstSlices) {

        ProtoSparseArray<T, V, E, D> src = this;

        Control.checkTrue(src != dst, //
                "Source and destination cannot be the same");

        SparseArrayState<V> srcState = src.state;
        SparseArrayState<V> dstState = dst.state;

        dst.state = OpKernel.sliceSparse(canonicalizeSlices(srcSlices, src.dims, dstSlices, dst.dims), //
                srcState.values, src.dims, src.strides, src.dimOffsets, //
                srcState.indices, srcState.indirectionOffsets, srcState.indirections, //
                dstState.values, dst.dims, dst.strides, dst.dimOffsets, //
                dstState.indices, dstState.indirectionOffsets, dstState.indirections);

        return dst;
    }

    @Override
    public T slice(T dst, int[]... dstSlices) {

        ProtoSparseArray<T, V, E, D> src = this;

        Control.checkTrue(src != dst, //
                "Source and destination cannot be the same");

        SparseArrayState<V> srcState = src.state;
        SparseArrayState<V> dstState = dst.state;

        dst.state = OpKernel.sliceSparse(canonicalizeSlices(src.dims, dst.dims, dstSlices), //
                srcState.values, src.dims, src.strides, src.dimOffsets, //
                srcState.indices, srcState.indirectionOffsets, srcState.indirections, //
                dstState.values, dst.dims, dst.strides, dst.dimOffsets, //
                dstState.indices, dstState.indirectionOffsets, dstState.indirections);

        return dst;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T slice(E value, int[]... srcSlices) {

        T src = (T) this;

        int nDims = srcSlices.length;
        int[] dstDims = new int[nDims];

        for (int dim = 0; dim < nDims; dim++) {
            dstDims[dim] = srcSlices[dim].length;
        }

        return wrap(value, dstDims, src.order().strides(dstDims), createDimensionOffsets(dstDims)) //
                .slice(src, srcSlices);
    }

    @Override
    public T slice(int[]... srcSlices) {

        ProtoSparseArray<T, V, E, D> src = this;

        int nDims = Control.checkEquals(src.dims.length, srcSlices.length, //
                "Dimensionality mismatch");

        int nSlices = 0;
        int[] dstDims = new int[nDims];

        for (int dim = 0; dim < nDims; dim++) {
            nSlices += (dstDims[dim] = srcSlices[dim].length);
        }

        T dst = wrap((E) null, dstDims, src.order().strides(dstDims), createDimensionOffsets(dstDims));

        SparseArrayState<V> srcState = src.state;
        SparseArrayState<V> dstState = dst.state;

        dst.state = OpKernel.sliceSparse(canonicalizeSlices(nSlices, src.dims, srcSlices), //
                srcState.values, src.dims, src.strides, src.dimOffsets, //
                srcState.indices, srcState.indirectionOffsets, srcState.indirections, //
                dstState.values, dst.dims, dst.strides, dst.dimOffsets, //
                dstState.indices, dstState.indirectionOffsets, dstState.indirections);

        return dst;
    }

    @Override
    public T tile(int... repetitions) {

        ProtoSparseArray<T, V, E, D> src = this;

        int nDims = Control.checkEquals(src.dims.length, repetitions.length, //
                "Dimensionality mismatch");

        int[] newDims = src.dims.clone();

        for (int dim = 0; dim < nDims; dim++) {
            newDims[dim] *= repetitions[dim];
        }

        T dst = wrap((E) null, newDims, src.order().strides(newDims), createDimensionOffsets(newDims));

        int[] mappingBounds = new int[3 * nDims];

        for (int dim = 0, offset = 0; dim < nDims; dim++, offset += 3) {
            mappingBounds[offset + 2] = dst.dims[dim];
        }

        return map(dst, mappingBounds);
    }

    @Override
    public T transpose(int... permutation) {

        ProtoSparseArray<T, V, E, D> src = this;

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

        SparseArrayState<V> srcState = src.state;

        int nIndices = srcState.indices.length;
        int[] newStrides = src.order().strides(newDims);
        int[] newDimOffsets = createDimensionOffsets(newDims);
        int[] newIndices = new int[nIndices];

        for (int i = 0; i < nIndices; i++) {

            int newPhysical = 0;
            int physical = srcState.indices[i];

            for (int dim = 0; dim < nDims; dim++) {

                newPhysical += newStrides[permutation[dim]] * (physical / src.strides[dim]);
                physical %= src.strides[dim];
            }

            newIndices[i] = newPhysical;
        }

        return wrap(OpKernel.insertSparse( //
                empty(), newDims, newStrides, newDimOffsets, EmptyIndices, //
                srcState.values, newIndices), newDims, newStrides, newDimOffsets);
    }

    @Override
    public T shift(int... shifts) {

        ProtoSparseArray<T, V, E, D> src = this;

        int nDims = Control.checkEquals(src.dims.length, shifts.length, //
                "Dimensionality mismatch");

        int[] mappingBounds = new int[3 * nDims];

        for (int dim = 0, offset = 0; dim < nDims; dim++, offset += 3) {

            mappingBounds[offset + 1] = shifts[dim];
            mappingBounds[offset + 2] = src.dims[dim];
        }

        return map(wrap((E) null, src.dims, src.strides, src.dimOffsets), mappingBounds);
    }

    @Override
    public T subarray(int... bounds) {

        ProtoSparseArray<T, V, E, D> src = this;

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

        return map(wrap((E) null, newDims, src.order().strides(newDims), createDimensionOffsets(newDims)), //
                mappingBounds);
    }

    @Override
    public T reverse(int... opDims) {

        ProtoSparseArray<T, V, E, D> src = this;

        T dst = wrap((E) null, src.dims, src.strides, src.dimOffsets);

        SparseArrayState<V> srcState = src.state;
        SparseArrayState<V> dstState = dst.state;

        dst.state = OpKernel.sliceSparse(createReverseSlices(src.dims, opDims), //
                srcState.values, src.dims, src.strides, src.dimOffsets, //
                srcState.indices, srcState.indirectionOffsets, srcState.indirections, //
                dstState.values, dst.dims, dst.strides, dst.dimOffsets, //
                dstState.indices, dstState.indirectionOffsets, dstState.indirections);

        return dst;
    }

    @Override
    public T reshape(int... dims) {

        ProtoSparseArray<T, V, E, D> src = this;

        Control.checkTrue(Arithmetic.product(dims) == Arithmetic.product(src.dims), //
                "Cardinality mismatch");

        int[] strides = src.order().strides(dims);
        int[] dimOffsets = createDimensionOffsets(dims);

        SparseArrayState<V> srcState = src.state;

        T dst = wrap(OpKernel.insertSparse( //
                empty(), dims, strides, dimOffsets, EmptyIndices, //
                srcState.values, srcState.indices), dims, strides, dimOffsets);

        return dst;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T concat(int opDim, T... srcs) {

        ProtoSparseArray<T, V, E, D> src = this;

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

        T dst = wrap((E) null, newDims, src.order().strides(newDims), createDimensionOffsets(newDims));

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

    @SuppressWarnings("unchecked")
    @Override
    public T clone() {

        try {

            return (T) super.clone();

        } catch (CloneNotSupportedException e) {

            throw new RuntimeException(e);
        }
    }

    @Override
    public IndexingOrder order() {
        return DEFAULT_ORDER;
    }

    @Override
    public int size(int i) {
        return this.dims[i];
    }

    @Override
    public int stride(int i) {
        return this.strides[i];
    }

    @Override
    public int nDims() {
        return this.dims.length;
    }

    @Override
    public int[] dims() {
        return this.dims.clone();
    }

    @Override
    public int[] strides() {
        return this.strides.clone();
    }

    @Override
    public T reverseOrder() {
        throw new UnsupportedOperationException("Sparse arrays always have row major indexing");
    }
}

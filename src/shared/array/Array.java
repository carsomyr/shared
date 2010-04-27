/**
 * <p>
 * Copyright (C) 2007-2010 Roy Liu<br />
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

/**
 * Defines functionality that can be expected from all multidimensional arrays.
 * 
 * @apiviz.owns shared.array.Array.IndexingOrder
 * @param <T>
 *            the parameterization lower bounded by {@link Array} itself.
 * @param <E>
 *            the component type.
 * @author Roy Liu
 */
public interface Array<T extends Array<T, E>, E> extends Cloneable {

    /**
     * Gets the storage order of this array.
     */
    public IndexingOrder order();

    /**
     * Gets the size along the given dimension.
     */
    public int size(int i);

    /**
     * Gets the stride along the given dimension.
     */
    public int stride(int i);

    /**
     * Gets the number of dimensions.
     */
    public int nDims();

    /**
     * Gets the dimensions.
     */
    public int[] dims();

    /**
     * Gets the strides.
     */
    public int[] strides();

    /**
     * Maps this array into a destination array.
     * 
     * @param dst
     *            the destination array.
     * @param bounds
     *            the mapping bounds as an array of three-tuples. Given the <tt>i</tt>th tuple, the first component
     *            denotes the source start, the second component denotes the destination start, and the third component
     *            denotes the mapping size. Is very much the multidimensional analogy to
     *            {@link System#arraycopy(Object, int, Object, int, int)}.
     * @return the destination array.
     */
    public T map(T dst, int... bounds);

    /**
     * A primitive slicing operation.
     * 
     * @param dst
     *            the destination array.
     * @param slices
     *            the slicing specifications as an array of three-tuples. Given the <tt>i</tt>th tuple, the first
     *            component denotes the source index, the second component denotes the destination index, and the third
     *            component denotes the dimension of interest. The sliced portion is the Cartesian product subarray
     *            delineated by these specifications.
     * @return the destination array.
     */
    public T splice(T dst, int... slices);

    /**
     * Slices this array into a destination array.
     * 
     * @param srcSlices
     *            the source slicing indices along each dimension.
     * @param dst
     *            the destination array.
     * @param dstSlices
     *            the destination slicing indices along each dimension.
     * @return the destination array.
     */
    public T slice(int[][] srcSlices, T dst, int[][] dstSlices);

    /**
     * Slices this entire array into a destination array.
     * 
     * @param dst
     *            the destination array.
     * @param dstSlices
     *            the destination slicing indices along each dimension.
     * @return the destination array.
     */
    public T slice(T dst, int[]... dstSlices);

    /**
     * Slices a value into this array.
     * 
     * @param value
     *            the value.
     * @param srcSlices
     *            the source slicing indices along each dimension.
     * @return this array.
     */
    public T slice(E value, int[]... srcSlices);

    /**
     * Gets a subarray specified by the given slicing indices.
     * 
     * @param srcSlices
     *            the slicing indices.
     * @return the subarray.
     */
    public T slice(int[]... srcSlices);

    /**
     * Gets a subarray delimited by the given bounds.
     * 
     * @param bounds
     *            the subarray bounds as an array of two-tuples. Given the <tt>i</tt>th tuple, the first component is
     *            the (inclusive) lower range and the second component is the (exclusive) upper range.
     * @return the subarray.
     */
    public T subarray(int... bounds);

    /**
     * Tiles this array according to the given number repetitions along each dimension.
     * 
     * @param repetitions
     *            the number times to repeat along each dimension.
     * @return the tiled result.
     */
    public T tile(int... repetitions);

    /**
     * Transposes (permutes) the dimensions of this array.
     * 
     * @param permutation
     *            the dimension permutation.
     * @return the transposed result.
     */
    public T transpose(int... permutation);

    /**
     * Circularly shifts this array.
     * 
     * @param shifts
     *            the shift amounts.
     * @return the shifted array.
     */
    public T shift(int... shifts);

    /**
     * Reshapes this array.
     * 
     * @param dims
     *            the reshaped dimensions.
     * @return the reshaped array.
     */
    public T reshape(int... dims);

    /**
     * Reverses along the given dimensions.
     * 
     * @param opDims
     *            the dimensions to reverse along.
     * @return the reversed array.
     */
    public T reverse(int... opDims);

    /**
     * Concatenates this array with the given arrays along the given dimension.
     * 
     * @param opDim
     *            the dimension to concatenate along.
     * @param srcs
     *            the arrays to concatenate with.
     * @return the concatenation result.
     */
    public T concat(int opDim, T... srcs);

    /**
     * Creates an array where the storage order is reversed.
     */
    public T reverseOrder();

    /**
     * Copies this array.
     */
    public T clone();

    /**
     * Gets the component type.
     */
    public Class<E> getComponentType();

    /**
     * Creates a human-readable representation of this array.
     */
    @Override
    public String toString();

    /**
     * An enumeration of internal storage orders for {@link Array}s.
     */
    public interface IndexingOrder {

        /**
         * An ordering where low indices vary most.
         */
        final public static IndexingOrder NEAR = new IndexingOrder() {

            public int[] strides(int[] dims) {

                final int nDims = dims.length;
                final int[] strides = new int[nDims];

                strides[0] = 1;

                for (int dim = 1; dim < nDims; dim++) {
                    strides[dim] = strides[dim - 1] * dims[dim - 1];
                }

                return strides;
            }

            public IndexingOrder reverse() {
                return FAR;
            }
        };

        /**
         * An ordering where high indices vary most.
         */
        final public static IndexingOrder FAR = new IndexingOrder() {

            public int[] strides(int[] dims) {

                final int nDims = dims.length;
                final int[] strides = new int[nDims];

                strides[nDims - 1] = 1;

                for (int dim = nDims - 2; dim >= 0; dim--) {
                    strides[dim] = strides[dim + 1] * dims[dim + 1];
                }

                return strides;
            }

            public IndexingOrder reverse() {
                return NEAR;
            }
        };

        /**
         * Creates strides for the given dimensions.
         * 
         * @return the strides -- a vector such that its dot product with a logical index yields a physical index.
         */
        public int[] strides(int[] dims);

        /**
         * Gets the reversed value.
         */
        public IndexingOrder reverse();
    }
}

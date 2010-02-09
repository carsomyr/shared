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
    public int ndims();

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
     * @param selectedDims
     *            the dimensions to reverse along.
     * @return the reversed array.
     */
    public T reverse(int... selectedDims);

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
    public enum IndexingOrder {

        /**
         * An ordering where low indices vary most.
         */
        NEAR {

            @Override
            public int[] strides(int[] dims) {

                final int numDims = dims.length;
                final int[] strides = new int[numDims];

                strides[0] = 1;

                for (int i = 1; i < numDims; i++) {
                    strides[i] = strides[i - 1] * dims[i - 1];
                }

                return strides;
            }

            @Override
            public IndexingOrder reverse() {
                return FAR;
            }
        }, //

        /**
         * An ordering where high indices vary most.
         */
        FAR {

            @Override
            public int[] strides(int[] dims) {

                final int numDims = dims.length;
                final int[] strides = new int[numDims];

                strides[numDims - 1] = 1;

                for (int i = numDims - 2; i >= 0; i--) {
                    strides[i] = strides[i + 1] * dims[i + 1];
                }

                return strides;
            }

            @Override
            public IndexingOrder reverse() {
                return NEAR;
            }
        };

        /**
         * Creates strides for the given dimensions.
         * 
         * @return the strides -- a vector such that its dot product with a logical index yields a physical index.
         */
        abstract public int[] strides(int[] dims);

        /**
         * Gets the reversed value.
         */
        abstract public IndexingOrder reverse();
    }
}

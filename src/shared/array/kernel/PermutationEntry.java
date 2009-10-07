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

package shared.array.kernel;

import java.util.Arrays;

/**
 * A bookkeeping class for permutations.
 * 
 * @param <C>
 *            the {@link Comparable} type.
 * @author Roy Liu
 */
public class PermutationEntry<C extends Comparable<? super C>> implements Comparable<PermutationEntry<C>> {

    C value;

    int order;

    /**
     * Default constructor.
     */
    public PermutationEntry(C value, int order) {

        this.value = value;
        this.order = order;
    }

    /**
     * Compares values.
     */
    public int compareTo(PermutationEntry<C> o) {
        return this.value.compareTo(o.value);
    }

    /**
     * Gets the value.
     */
    public C getValue() {
        return this.value;
    }

    /**
     * Gets the order.
     */
    public int getOrder() {
        return this.order;
    }

    /**
     * Performs an index sort operation in support of {@link DimensionOps}.
     * 
     * @param srcV
     *            the source values.
     * @param srcIndices
     *            the source indices.
     * @param dstV
     *            the destination values.
     * @param size
     *            the dimension size.
     * @param stride
     *            the dimension stride.
     * @param <T>
     *            the component type.
     */
    @SuppressWarnings("unchecked")
    final public static <T extends Comparable<? super T>> void iSort(T[] srcV, int[] srcIndices, int[] dstV, int size,
            int stride) {

        int len = (srcIndices != null) ? size : srcV.length;

        PermutationEntry<T>[] entries = new PermutationEntry[len];

        for (int i = 0; i < len; i++) {
            entries[i] = new PermutationEntry<T>((T) null, -1);
        }

        if (srcIndices != null) {

            for (int i = 0, nindices = srcIndices.length; i < nindices; i++) {

                for (int offset = 0, j = 0; j < size; offset += stride, j++) {

                    entries[j].value = srcV[srcIndices[i] + offset];
                    entries[j].order = j;
                }

                Arrays.sort(entries);

                for (int offset = 0, j = 0; j < size; offset += stride, j++) {

                    srcV[srcIndices[i] + offset] = entries[j].value;
                    dstV[srcIndices[i] + offset] = entries[j].order;
                }
            }

        } else {

            for (int i = 0, n = srcV.length; i < n; i++) {

                entries[i].value = srcV[i];
                entries[i].order = i;
            }

            Arrays.sort(entries);

            for (int i = 0, n = srcV.length; i < n; i++) {

                srcV[i] = entries[i].value;
                dstV[i] = entries[i].order;
            }
        }
    }
}

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
    @Override
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

            for (int i = 0, nIndices = srcIndices.length; i < nIndices; i++) {

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

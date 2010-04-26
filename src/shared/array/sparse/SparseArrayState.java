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

import shared.util.Arithmetic;

/**
 * A container class for sparse array state.
 * 
 * @param <V>
 *            the storage array type.
 * @author Roy Liu
 */
public class SparseArrayState<V> {

    /**
     * An empty array.
     */
    final protected static int[] Empty = new int[] {};

    /**
     * The values.
     */
    final protected V values;

    /**
     * The physical indices.
     */
    final protected int[] indices;

    /**
     * The offsets into {@link #indirections}.
     */
    final protected int[] indirectionOffsets;

    /**
     * The indirection indices into {@link #values} and {@link #indices}.
     */
    final protected int[] indirections;

    /**
     * Default constructor.
     */
    public SparseArrayState(V values, int[] indices, int[] indirectionOffsets, int[] indirections) {

        this.values = values;
        this.indices = indices;
        this.indirectionOffsets = indirectionOffsets;
        this.indirections = indirections;
    }

    /**
     * Alternate constructor.
     */
    public SparseArrayState(V emptyValues, int[] dims) {

        this.values = emptyValues;
        this.indices = Empty;
        this.indirectionOffsets = new int[Arithmetic.sum(dims) + dims.length];
        this.indirections = Empty;
    }
}

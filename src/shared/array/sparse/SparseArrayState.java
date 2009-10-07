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

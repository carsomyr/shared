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

package shared.util;

/**
 * Defines an array that can grow and shrink dynamically.
 * 
 * @param <T>
 *            the parameterization lower bounded by {@link DynamicArray} itself.
 * @param <V>
 *            the storage array type.
 * @param <E>
 *            the component type.
 * @author Roy Liu
 */
public interface DynamicArray<T extends DynamicArray<T, V, E>, V, E> extends Cloneable {

    /**
     * Clears this array.
     * 
     * @return this array.
     */
    public T clear();

    /**
     * Ensures that this array has at least the given capacity.
     * 
     * @param capacity
     *            the desired capacity.
     * @return this array.
     */
    public T ensureCapacity(int capacity);

    /**
     * Gets the number of elements.
     * 
     * @return the number of elements.
     */
    public int size();

    /**
     * Gets the capacity.
     * 
     * @return the capacity.
     */
    public int capacity();

    /**
     * Gets the storage array truncated to the current size.
     * 
     * @return a truncated copy of the storage array.
     */
    public V values();

    /**
     * Gets the component type.
     */
    public Class<E> getComponentType();

    /**
     * Creates a human-readable representation of this array.
     */
    public String toString();

    /**
     * Clones this array.
     */
    public T clone();
}

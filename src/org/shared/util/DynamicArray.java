/**
 * <p>
 * Copyright (c) 2009 Roy Liu<br>
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

package org.shared.util;

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
    @Override
    public String toString();

    /**
     * Clones this array.
     */
    public T clone();
}

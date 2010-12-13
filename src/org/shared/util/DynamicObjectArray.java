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

import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * An implementation of {@link DynamicArray} for objects.
 * 
 * @param <T>
 *            the component type.
 * @author Roy Liu
 */
public class DynamicObjectArray<T> implements DynamicArray<DynamicObjectArray<T>, T[], T> {

    /**
     * The initial capacity.
     */
    final public static int INITIAL_CAPACITY = 127;

    T[] values;

    int size;

    /**
     * Default constructor.
     */
    public DynamicObjectArray(Class<T> clazz) {
        this(clazz, INITIAL_CAPACITY);
    }

    /**
     * Alternate constructor.
     */
    @SuppressWarnings("unchecked")
    public DynamicObjectArray(Class<T> clazz, int capacity) {

        this.values = (T[]) Array.newInstance(clazz, capacity);
        this.size = 0;
    }

    /**
     * Alternate constructor.
     */
    protected DynamicObjectArray(T[] values, int size) {

        this.values = values;
        this.size = size;
    }

    /**
     * Gets the value at the given index.
     */
    public T get(int index) {

        Control.checkTrue(index >= 0 && index < this.size, //
                "Invalid index");

        return this.values[index];
    }

    /**
     * Sets a value at the given index.
     */
    public DynamicObjectArray<T> push(T value, int index) {

        Control.checkTrue(index >= 0 && index < this.size, //
                "Invalid index");

        this.values[index] = value;

        return this;
    }

    /**
     * Pops off the last pushed on value.
     */
    public T pop() {

        Control.checkTrue(this.size > 0, //
                "No more elements");

        if (this.size <= (this.values.length + 1) >>> 2) {
            this.values = Arrays.copyOf(this.values, this.values.length >>> 1);
        }

        return this.values[--this.size];
    }

    /**
     * Pushes on a value.
     */
    public DynamicObjectArray<T> push(T value) {

        if (this.size == this.values.length) {
            this.values = Arrays.copyOf(this.values, 2 * this.values.length + 1);
        }

        this.values[this.size++] = value;

        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public DynamicObjectArray<T> clear() {

        this.values = (T[]) Array.newInstance(getComponentType(), INITIAL_CAPACITY);
        this.size = 0;

        return this;
    }

    @Override
    public DynamicObjectArray<T> ensureCapacity(int capacity) {

        this.values = (this.values.length < capacity) ? Arrays.copyOf(this.values, capacity) : this.values;

        return this;
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public int capacity() {
        return this.values.length;
    }

    @Override
    public T[] values() {
        return Arrays.copyOf(this.values, this.size);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<T> getComponentType() {
        return (Class<T>) this.values.getClass().getComponentType();
    }

    @Override
    public String toString() {
        return String.format("%s[size = %d, capacity = %d, values = %s]", //
                getClass().getSimpleName(), size(), capacity(), Arrays.toString(values()));
    }

    @Override
    public DynamicObjectArray<T> clone() {
        return new DynamicObjectArray<T>(this.values.clone(), this.size);
    }
}

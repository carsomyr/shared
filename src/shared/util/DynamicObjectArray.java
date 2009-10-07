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
    public DynamicObjectArray<T> clear() {

        this.values = (T[]) Array.newInstance(getComponentType(), INITIAL_CAPACITY);
        this.size = 0;

        return this;
    }

    public DynamicObjectArray<T> ensureCapacity(int capacity) {

        this.values = (this.values.length < capacity) ? Arrays.copyOf(this.values, capacity) : this.values;

        return this;
    }

    public int size() {
        return this.size;
    }

    public int capacity() {
        return this.values.length;
    }

    public T[] values() {
        return Arrays.copyOf(this.values, this.size);
    }

    @SuppressWarnings("unchecked")
    public Class<T> getComponentType() {
        return (Class<T>) this.values.getClass().getComponentType();
    }

    @Override
    public String toString() {
        return String.format("%s[size = %d, capacity = %d, values = %s]", //
                DynamicObjectArray.class.getSimpleName(), //
                size(), capacity(), Arrays.toString(values()));
    }

    @Override
    public DynamicObjectArray<T> clone() {
        return new DynamicObjectArray<T>(this.values.clone(), this.size);
    }
}

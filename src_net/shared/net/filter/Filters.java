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

package shared.net.filter;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import shared.util.Control;

/**
 * A collection of static utility methods in support of filtering.
 * 
 * @author Roy Liu
 */
public class Filters {

    /**
     * Creates a new coalescing {@link Queue} whose {@link Queue#add(Object)} method incorporates an element only if the
     * tail element is not the same (by reference equality). This is an optimization that should be acceptable for the
     * purposes of filtering.
     * 
     * @param <V>
     *            the value type.
     */
    final public static <V> Queue<V> createQueue() {

        final LinkedList<V> backing = new LinkedList<V>();

        return new Queue<V>() {

            public boolean add(V e) {

                Control.checkTrue(e != null, //
                        "Element must be non-null");

                if (backing.isEmpty() || backing.getLast() != e) {

                    backing.add(e);

                    return true;

                } else {

                    return false;
                }
            }

            public V peek() {
                return backing.peek();
            }

            public V element() {
                return backing.element();
            }

            public V poll() {
                return backing.poll();
            }

            public V remove() {
                return backing.remove();
            }

            public boolean isEmpty() {
                return backing.isEmpty();
            }

            public void clear() {
                backing.clear();
            }

            public int size() {
                return backing.size();
            }

            @Override
            public String toString() {
                return backing.toString();
            }

            public boolean offer(V e) {
                throw new UnsupportedOperationException();
            }

            public boolean addAll(Collection<? extends V> c) {
                throw new UnsupportedOperationException();
            }

            public boolean contains(Object o) {
                throw new UnsupportedOperationException();
            }

            public boolean containsAll(Collection<?> c) {
                throw new UnsupportedOperationException();
            }

            public Iterator<V> iterator() {
                throw new UnsupportedOperationException();
            }

            public boolean remove(Object o) {
                throw new UnsupportedOperationException();
            }

            public boolean removeAll(Collection<?> c) {
                throw new UnsupportedOperationException();
            }

            public boolean retainAll(Collection<?> c) {
                throw new UnsupportedOperationException();
            }

            public Object[] toArray() {
                throw new UnsupportedOperationException();
            }

            public <T> T[] toArray(T[] a) {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * Creates a read-only {@link Queue} backed by the given {@link Queue}.
     * 
     * @param <V>
     *            the value type.
     */
    final protected static <V> Queue<V> readOnlyQueue(final Queue<V> backing) {

        return new Queue<V>() {

            public V peek() {
                return backing.peek();
            }

            public V element() {
                return backing.element();
            }

            public V poll() {
                return backing.poll();
            }

            public V remove() {
                return backing.remove();
            }

            public boolean isEmpty() {
                return backing.isEmpty();
            }

            public void clear() {
                backing.clear();
            }

            public int size() {
                return backing.size();
            }

            @Override
            public String toString() {
                return backing.toString();
            }

            public boolean add(V e) {
                throw new UnsupportedOperationException();
            }

            public boolean offer(V e) {
                throw new UnsupportedOperationException();
            }

            public boolean addAll(Collection<? extends V> c) {
                throw new UnsupportedOperationException();
            }

            public boolean contains(Object o) {
                throw new UnsupportedOperationException();
            }

            public boolean containsAll(Collection<?> c) {
                throw new UnsupportedOperationException();
            }

            public Iterator<V> iterator() {
                throw new UnsupportedOperationException();
            }

            public boolean remove(Object o) {
                throw new UnsupportedOperationException();
            }

            public boolean removeAll(Collection<?> c) {
                throw new UnsupportedOperationException();
            }

            public boolean retainAll(Collection<?> c) {
                throw new UnsupportedOperationException();
            }

            public Object[] toArray() {
                throw new UnsupportedOperationException();
            }

            public <T> T[] toArray(T[] a) {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * Creates a write-only {@link Queue} backed by the given {@link Queue}.
     * 
     * @param <V>
     *            the value type.
     */
    final protected static <V> Queue<V> writeOnlyQueue(final Queue<V> backing) {

        return new Queue<V>() {

            public boolean add(V e) {
                return backing.add(e);
            }

            public boolean isEmpty() {
                return backing.isEmpty();
            }

            public int size() {
                return backing.size();
            }

            @Override
            public String toString() {
                return backing.toString();
            }

            public V peek() {
                throw new UnsupportedOperationException();
            }

            public V element() {
                throw new UnsupportedOperationException();
            }

            public V poll() {
                throw new UnsupportedOperationException();
            }

            public V remove() {
                throw new UnsupportedOperationException();
            }

            public void clear() {
                throw new UnsupportedOperationException();
            }

            public boolean offer(V e) {
                throw new UnsupportedOperationException();
            }

            public boolean addAll(Collection<? extends V> c) {
                throw new UnsupportedOperationException();
            }

            public boolean contains(Object o) {
                throw new UnsupportedOperationException();
            }

            public boolean containsAll(Collection<?> c) {
                throw new UnsupportedOperationException();
            }

            public Iterator<V> iterator() {
                throw new UnsupportedOperationException();
            }

            public boolean remove(Object o) {
                throw new UnsupportedOperationException();
            }

            public boolean removeAll(Collection<?> c) {
                throw new UnsupportedOperationException();
            }

            public boolean retainAll(Collection<?> c) {
                throw new UnsupportedOperationException();
            }

            public Object[] toArray() {
                throw new UnsupportedOperationException();
            }

            public <T> T[] toArray(T[] a) {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * Unrolls the given {@link StatefulFilter} into session, bind, and shutdown {@link Filter}s.
     * 
     * @param <I>
     *            the nominal input type.
     * @param <O>
     *            the nominal output type.
     * @param statefulFilter
     *            the {@link StatefulFilter}.
     * @return the unrolled {@link Filter}s.
     */
    @SuppressWarnings("unchecked")
    final public static <I, O> Filter<I, O>[] unroll(final StatefulFilter<I, O> statefulFilter) {

        Filter<I, O>[] filters = new Filter[3];

        filters[0] = statefulFilter;

        filters[1] = new Filter<I, O>() {

            public void getInbound(Queue<I> in, Queue<O> out) {
                statefulFilter.bindInbound(in, out);
            }

            public void getOutbound(Queue<O> in, Queue<I> out) {
                statefulFilter.bindOutbound(in, out);
            }
        };

        filters[2] = new Filter<I, O>() {

            public void getInbound(Queue<I> in, Queue<O> out) {
                statefulFilter.shutdownInbound(in, out);
            }

            public void getOutbound(Queue<O> in, Queue<I> out) {
                statefulFilter.shutdownOutbound(in, out);
            }
        };

        return filters;
    }

    // Dummy constructor.
    Filters() {
    }
}

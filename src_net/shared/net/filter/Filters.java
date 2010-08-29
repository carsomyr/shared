/**
 * <p>
 * Copyright (C) 2009-2010 Roy Liu<br />
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

            @Override
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

            @Override
            public boolean offer(V e) {
                return add(e);
            }

            @Override
            public V element() {
                return backing.element();
            }

            @Override
            public V peek() {
                return backing.peek();
            }

            @Override
            public V poll() {
                return backing.poll();
            }

            @Override
            public V remove() {
                return backing.remove();
            }

            @Override
            public void clear() {
                backing.clear();
            }

            @Override
            public int size() {
                return backing.size();
            }

            @Override
            public boolean isEmpty() {
                return backing.isEmpty();
            }

            @Override
            public String toString() {
                return backing.toString();
            }

            @Override
            public boolean addAll(Collection<? extends V> c) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean contains(Object o) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean containsAll(Collection<?> c) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Iterator<V> iterator() {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean remove(Object o) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean removeAll(Collection<?> c) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean retainAll(Collection<?> c) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Object[] toArray() {
                throw new UnsupportedOperationException();
            }

            @Override
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
    final public static <V> Queue<V> readOnlyQueue(final Queue<V> backing) {

        return new Queue<V>() {

            @Override
            public V element() {
                return backing.element();
            }

            @Override
            public V peek() {
                return backing.peek();
            }

            @Override
            public V poll() {
                return backing.poll();
            }

            @Override
            public V remove() {
                return backing.remove();
            }

            @Override
            public void clear() {
                backing.clear();
            }

            @Override
            public int size() {
                return backing.size();
            }

            @Override
            public boolean isEmpty() {
                return backing.isEmpty();
            }

            @Override
            public String toString() {
                return backing.toString();
            }

            @Override
            public boolean add(V e) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean offer(V e) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean addAll(Collection<? extends V> c) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean contains(Object o) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean containsAll(Collection<?> c) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Iterator<V> iterator() {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean remove(Object o) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean removeAll(Collection<?> c) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean retainAll(Collection<?> c) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Object[] toArray() {
                throw new UnsupportedOperationException();
            }

            @Override
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
    final public static <V> Queue<V> writeOnlyQueue(final Queue<V> backing) {

        return new Queue<V>() {

            @Override
            public boolean add(V e) {
                return backing.add(e);
            }

            @Override
            public boolean offer(V e) {
                return backing.offer(e);
            }

            @Override
            public int size() {
                return backing.size();
            }

            @Override
            public boolean isEmpty() {
                return backing.isEmpty();
            }

            @Override
            public String toString() {
                return backing.toString();
            }

            @Override
            public V peek() {
                throw new UnsupportedOperationException();
            }

            @Override
            public V element() {
                throw new UnsupportedOperationException();
            }

            @Override
            public V poll() {
                throw new UnsupportedOperationException();
            }

            @Override
            public V remove() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void clear() {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean addAll(Collection<? extends V> c) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean contains(Object o) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean containsAll(Collection<?> c) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Iterator<V> iterator() {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean remove(Object o) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean removeAll(Collection<?> c) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean retainAll(Collection<?> c) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Object[] toArray() {
                throw new UnsupportedOperationException();
            }

            @Override
            public <T> T[] toArray(T[] a) {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * Wraps the given {@link Filter} with an {@link OOBFilter} adapter that has trivial behavior on {@link OOBEvent}s.
     * 
     * @param filter
     *            the {@link Filter}.
     * @param <I>
     *            the input type.
     * @param <O>
     *            the output type.
     * @return the {@link OOBFilter} adapter, or the original {@link Filter} if it is already an instance.
     */
    final public static <I, O> OOBFilter<I, O> asOOBFilter(final Filter<I, O> filter) {

        return (filter instanceof OOBFilter<?, ?>) ? (OOBFilter<I, O>) filter : new OOBFilter<I, O>() {

            @Override
            public void getInbound(Queue<I> in, Queue<O> out) {
                filter.getInbound(in, out);
            }

            @Override
            public void getOutbound(Queue<O> in, Queue<I> out) {
                filter.getOutbound(in, out);
            }

            @Override
            public void getInboundOOB( //
                    Queue<I> in, Queue<OOBEvent> inEvts, //
                    Queue<O> out, Queue<OOBEvent> outEvts) {

                transfer(inEvts, outEvts);
                getInbound(in, out);
            }

            @Override
            public void getOutboundOOB( //
                    Queue<O> in, Queue<OOBEvent> inEvts, //
                    Queue<I> out, Queue<OOBEvent> outEvts) {

                transfer(inEvts, outEvts);
                getOutbound(in, out);
            }
        };
    }

    /**
     * Transfers elements from the given input {@link Queue} to the given output {@link Queue}.
     * 
     * @param <T>
     *            the element type.
     */
    final public static <T> void transfer(Queue<T> in, Queue<T> out) {

        for (T elt; (elt = in.poll()) != null;) {
            out.add(elt);
        }
    }

    // Dummy constructor.
    Filters() {
    }
}

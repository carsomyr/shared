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

import java.util.Arrays;
import java.util.Queue;

import shared.net.Connection;

/**
 * An implementation of {@link FilterFactory} that enables chaining of multiple underlying {@link FilterFactory}s.
 * 
 * @apiviz.owns shared.net.filter.FilterFactory
 * @apiviz.uses shared.net.filter.Filters
 * @param <I>
 *            the nominal input type.
 * @param <O>
 *            the nominal output type.
 * @param <C>
 *            the {@link Connection} type.
 * @author Roy Liu
 */
public class ChainFilterFactory<I, O, C extends Connection> implements FilterFactory<I, O, C> {

    FilterFactory<Object, Object, ? super C>[] factories;

    /**
     * Default constructor.
     */
    @SuppressWarnings("unchecked")
    public ChainFilterFactory() {
        this.factories = new FilterFactory[] {};
    }

    /**
     * Internal constructor.
     */
    @SuppressWarnings("unchecked")
    protected ChainFilterFactory(FilterFactory<?, ?, ? super C>[] factories, FilterFactory<?, O, ? super C> factory) {

        this.factories = Arrays.copyOf(factories, factories.length + 1, FilterFactory[].class);
        this.factories[this.factories.length - 1] = (FilterFactory<Object, Object, ? super C>) factory;
    }

    /**
     * Adds the given {@link FilterFactory} to the end of this chain.
     * 
     * @param <T>
     *            the nominal output type of the resulting chain.
     * @return the resulting longer chain.
     */
    public <T> ChainFilterFactory<I, T, C> add(FilterFactory<? super O, T, ? super C> factory) {
        return new ChainFilterFactory<I, T, C>(this.factories, factory);
    }

    @SuppressWarnings("unchecked")
    public StatefulFilter<I, O> newFilter(C connection) {

        final int nfilters = this.factories.length;

        final Filter<Object, Object>[] sessionFilters = new Filter[nfilters];
        final Filter<Object, Object>[] bindFilters = new Filter[nfilters];
        final Filter<Object, Object>[] shutdownFilters = new Filter[nfilters];
        final Queue<Object>[] inboundReadOnlys = new Queue[nfilters - 1];
        final Queue<Object>[] inboundWriteOnlys = new Queue[nfilters - 1];
        final Queue<Object>[] outboundReadOnlys = new Queue[nfilters - 1];
        final Queue<Object>[] outboundWriteOnlys = new Queue[nfilters - 1];

        for (int i = 0; i < nfilters; i++) {

            Filter<Object, Object> filter = this.factories[i].newFilter(connection);

            if (filter instanceof StatefulFilter) {

                Filter<Object, Object>[] filters = Filters.unroll( //
                        (StatefulFilter<Object, Object>) filter);

                sessionFilters[i] = filters[0];
                bindFilters[i] = filters[1];
                shutdownFilters[i] = filters[2];

            } else {

                sessionFilters[i] = bindFilters[i] = shutdownFilters[i] = filter;
            }
        }

        for (int i = 0, n = nfilters - 1; i < n; i++) {

            Queue<Object> inbound = Filters.createQueue();
            inboundReadOnlys[i] = Filters.readOnlyQueue(inbound);
            inboundWriteOnlys[i] = Filters.writeOnlyQueue(inbound);

            Queue<Object> outbound = Filters.createQueue();
            outboundReadOnlys[i] = Filters.readOnlyQueue(outbound);
            outboundWriteOnlys[i] = Filters.writeOnlyQueue(outbound);
        }

        return new StatefulFilter<I, O>() {

            public void getInbound(Queue<I> in, Queue<O> out) {
                getInbound(sessionFilters, in, out);
            }

            public void getOutbound(Queue<O> in, Queue<I> out) {
                getOutbound(sessionFilters, in, out);
            }

            public void bindInbound(Queue<I> in, Queue<O> out) {
                getInbound(bindFilters, in, out);
            }

            public void bindOutbound(Queue<O> in, Queue<I> out) {
                getOutbound(bindFilters, in, out);
            }

            public void shutdownInbound(Queue<I> in, Queue<O> out) {
                getInbound(shutdownFilters, in, out);
            }

            public void shutdownOutbound(Queue<O> in, Queue<I> out) {
                getOutbound(shutdownFilters, in, out);
            }

            void getInbound(Filter<Object, Object>[] filters, Queue<I> in, Queue<O> out) {

                switch (filters.length) {

                case 1:
                    ((Filter<I, O>) filters[0]).getInbound(in, out);
                    break;

                default:

                    ((Filter<I, Object>) filters[0]) //
                            .getInbound(in, inboundWriteOnlys[0]);

                    for (int i = 1, n = nfilters - 1; i < n; i++) {
                        filters[i].getInbound(inboundReadOnlys[i - 1], inboundWriteOnlys[i]);
                    }

                    ((Filter<Object, O>) filters[nfilters - 1]) //
                            .getInbound(inboundReadOnlys[nfilters - 2], out);

                    break;
                }
            }

            void getOutbound(Filter<Object, Object>[] filters, Queue<O> in, Queue<I> out) {

                switch (filters.length) {

                case 1:
                    ((Filter<I, O>) filters[0]).getOutbound(in, out);
                    break;

                default:

                    ((Filter<Object, O>) filters[nfilters - 1]) //
                            .getOutbound(in, outboundWriteOnlys[nfilters - 2]);

                    for (int i = nfilters - 2; i >= 1; i--) {
                        filters[i].getOutbound(outboundReadOnlys[i], outboundWriteOnlys[i - 1]);
                    }

                    ((Filter<I, Object>) filters[0]) //
                            .getOutbound(outboundReadOnlys[0], out);

                    break;
                }
            }
        };
    }
}

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
public class ChainFilterFactory<I, O, C extends Connection> implements FilterFactory<OOBFilter<I, O>, I, O, C> {

    FilterFactory<? extends Filter<Object, Object>, Object, Object, ? super C>[] factories;

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
    protected ChainFilterFactory( //
            FilterFactory<? extends Filter<?, ?>, ?, ?, ? super C>[] factories, //
            FilterFactory<? extends Filter<?, O>, ?, O, ? super C> factory) {

        this.factories = Arrays.copyOf(factories, factories.length + 1, FilterFactory[].class);
        this.factories[this.factories.length - 1] = (FilterFactory<? extends Filter<Object, Object>, Object, Object, ? super C>) factory;
    }

    /**
     * Adds the given {@link FilterFactory} to the end of this chain.
     * 
     * @param <T>
     *            the nominal output type of the resulting chain.
     * @return the resulting longer chain.
     */
    public <T> ChainFilterFactory<I, T, C> add( //
            FilterFactory<? extends Filter<? super O, T>, ? super O, T, ? super C> factory) {
        return new ChainFilterFactory<I, T, C>(this.factories, factory);
    }

    @SuppressWarnings("unchecked")
    public OOBFilter<I, O> newFilter(C connection) {

        final int nfilters = this.factories.length;

        final OOBFilter<Object, Object>[] filters = new OOBFilter[nfilters];
        final Queue<Object>[] inboundReadOnlys = new Queue[nfilters - 1];
        final Queue<Object>[] inboundWriteOnlys = new Queue[nfilters - 1];
        final Queue<Object>[] outboundReadOnlys = new Queue[nfilters - 1];
        final Queue<Object>[] outboundWriteOnlys = new Queue[nfilters - 1];
        final Queue<OOBEvent>[] inboundReadOnlyOOBs = new Queue[nfilters - 1];
        final Queue<OOBEvent>[] inboundWriteOnlyOOBs = new Queue[nfilters - 1];
        final Queue<OOBEvent>[] outboundReadOnlyOOBs = new Queue[nfilters - 1];
        final Queue<OOBEvent>[] outboundWriteOnlyOOBs = new Queue[nfilters - 1];

        for (int i = 0; i < nfilters; i++) {
            filters[i] = Filters.asOOBFilter(this.factories[i].newFilter(connection));
        }

        for (int i = 0, n = nfilters - 1; i < n; i++) {

            Queue<Object> inbound = Filters.createQueue();
            inboundReadOnlys[i] = Filters.readOnlyQueue(inbound);
            inboundWriteOnlys[i] = Filters.writeOnlyQueue(inbound);

            Queue<Object> outbound = Filters.createQueue();
            outboundReadOnlys[i] = Filters.readOnlyQueue(outbound);
            outboundWriteOnlys[i] = Filters.writeOnlyQueue(outbound);

            Queue<OOBEvent> inboundOOB = Filters.createQueue();
            inboundReadOnlyOOBs[i] = Filters.readOnlyQueue(inboundOOB);
            inboundWriteOnlyOOBs[i] = Filters.writeOnlyQueue(inboundOOB);

            Queue<OOBEvent> outboundOOB = Filters.createQueue();
            outboundReadOnlyOOBs[i] = Filters.readOnlyQueue(outboundOOB);
            outboundWriteOnlyOOBs[i] = Filters.writeOnlyQueue(outboundOOB);
        }

        return new OOBFilter<I, O>() {

            public void getInbound(Queue<I> in, Queue<O> out) {

                switch (filters.length) {

                case 1:
                    ((Filter<I, O>) filters[0]).getInbound(in, out);
                    break;

                default:

                    ((Filter<I, Object>) filters[0]).getInbound(in, inboundWriteOnlys[0]);

                    for (int i = 1, n = nfilters - 1; i < n; i++) {
                        filters[i].getInbound(inboundReadOnlys[i - 1], inboundWriteOnlys[i]);
                    }

                    ((Filter<Object, O>) filters[nfilters - 1]).getInbound(inboundReadOnlys[nfilters - 2], out);

                    break;
                }
            }

            public void getOutbound(Queue<O> in, Queue<I> out) {

                switch (filters.length) {

                case 1:
                    ((Filter<I, O>) filters[0]).getOutbound(in, out);
                    break;

                default:

                    ((Filter<Object, O>) filters[nfilters - 1]).getOutbound(in, outboundWriteOnlys[nfilters - 2]);

                    for (int i = nfilters - 2; i >= 1; i--) {
                        filters[i].getOutbound(outboundReadOnlys[i], outboundWriteOnlys[i - 1]);
                    }

                    ((Filter<I, Object>) filters[0]).getOutbound(outboundReadOnlys[0], out);

                    break;
                }
            }

            public void getInboundOOB(Queue<I> in, Queue<OOBEvent> inEvts, Queue<O> out, Queue<OOBEvent> outEvts) {

                switch (filters.length) {

                case 1:
                    ((OOBFilter<I, O>) filters[0]).getInboundOOB(in, inEvts, out, outEvts);
                    break;

                default:

                    ((OOBFilter<I, Object>) filters[0]).getInboundOOB( //
                            in, inEvts, //
                            inboundWriteOnlys[0], inboundWriteOnlyOOBs[0]);

                    for (int i = 1, n = nfilters - 1; i < n; i++) {
                        filters[i].getInboundOOB( //
                                inboundReadOnlys[i - 1], inboundReadOnlyOOBs[i - 1], //
                                inboundWriteOnlys[i], inboundWriteOnlyOOBs[i]);
                    }

                    ((OOBFilter<Object, O>) filters[nfilters - 1]).getInboundOOB( //
                            inboundReadOnlys[nfilters - 2], inboundReadOnlyOOBs[nfilters - 2], //
                            out, outEvts);

                    break;
                }
            }

            public void getOutboundOOB(Queue<O> in, Queue<OOBEvent> inEvts, Queue<I> out, Queue<OOBEvent> outEvts) {

                switch (filters.length) {

                case 1:
                    ((OOBFilter<I, O>) filters[0]).getOutboundOOB(in, inEvts, out, outEvts);
                    break;

                default:

                    ((OOBFilter<Object, O>) filters[nfilters - 1]).getOutboundOOB( //
                            in, inEvts, //
                            outboundWriteOnlys[nfilters - 2], outboundWriteOnlyOOBs[nfilters - 2]);

                    for (int i = nfilters - 2; i >= 1; i--) {
                        filters[i].getOutboundOOB( //
                                outboundReadOnlys[i], outboundReadOnlyOOBs[i], //
                                outboundWriteOnlys[i - 1], outboundWriteOnlyOOBs[i - 1]);
                    }

                    ((OOBFilter<I, Object>) filters[0]).getOutboundOOB( //
                            outboundReadOnlys[0], outboundReadOnlyOOBs[0], //
                            out, outEvts);

                    break;
                }
            }
        };
    }
}

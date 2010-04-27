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

        final int nFilters = this.factories.length;

        final OOBFilter<Object, Object>[] filters = new OOBFilter[nFilters];
        final Queue<Object>[] inboundReadOnlys = new Queue[nFilters - 1];
        final Queue<Object>[] inboundWriteOnlys = new Queue[nFilters - 1];
        final Queue<Object>[] outboundReadOnlys = new Queue[nFilters - 1];
        final Queue<Object>[] outboundWriteOnlys = new Queue[nFilters - 1];
        final Queue<OOBEvent>[] inboundReadOnlyOOBs = new Queue[nFilters - 1];
        final Queue<OOBEvent>[] inboundWriteOnlyOOBs = new Queue[nFilters - 1];
        final Queue<OOBEvent>[] outboundReadOnlyOOBs = new Queue[nFilters - 1];
        final Queue<OOBEvent>[] outboundWriteOnlyOOBs = new Queue[nFilters - 1];

        for (int i = 0; i < nFilters; i++) {
            filters[i] = Filters.asOOBFilter(this.factories[i].newFilter(connection));
        }

        for (int i = 0, n = nFilters - 1; i < n; i++) {

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

                    for (int i = 1, n = nFilters - 1; i < n; i++) {
                        filters[i].getInbound(inboundReadOnlys[i - 1], inboundWriteOnlys[i]);
                    }

                    ((Filter<Object, O>) filters[nFilters - 1]).getInbound(inboundReadOnlys[nFilters - 2], out);

                    break;
                }
            }

            public void getOutbound(Queue<O> in, Queue<I> out) {

                switch (filters.length) {

                case 1:
                    ((Filter<I, O>) filters[0]).getOutbound(in, out);
                    break;

                default:

                    ((Filter<Object, O>) filters[nFilters - 1]).getOutbound(in, outboundWriteOnlys[nFilters - 2]);

                    for (int i = nFilters - 2; i >= 1; i--) {
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

                    for (int i = 1, n = nFilters - 1; i < n; i++) {
                        filters[i].getInboundOOB( //
                                inboundReadOnlys[i - 1], inboundReadOnlyOOBs[i - 1], //
                                inboundWriteOnlys[i], inboundWriteOnlyOOBs[i]);
                    }

                    ((OOBFilter<Object, O>) filters[nFilters - 1]).getInboundOOB( //
                            inboundReadOnlys[nFilters - 2], inboundReadOnlyOOBs[nFilters - 2], //
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

                    ((OOBFilter<Object, O>) filters[nFilters - 1]).getOutboundOOB( //
                            in, inEvts, //
                            outboundWriteOnlys[nFilters - 2], outboundWriteOnlyOOBs[nFilters - 2]);

                    for (int i = nFilters - 2; i >= 1; i--) {
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

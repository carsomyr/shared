/**
 * <p>
 * Copyright (c) 2009-2010 Roy Liu<br>
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

import shared.net.ConnectionHandler;

/**
 * An implementation of {@link FilterFactory} that enables chaining of multiple underlying {@link FilterFactory}s.
 * 
 * @apiviz.owns shared.net.filter.FilterFactory
 * @apiviz.uses shared.net.filter.Filters
 * @param <I>
 *            the inbound type.
 * @param <O>
 *            the outbound type.
 * @param <H>
 *            the {@link ConnectionHandler} type.
 * @author Roy Liu
 */
public class ChainFilterFactory<I, O, H extends ConnectionHandler<?>> //
        implements FilterFactory<OobFilter<I, O>, I, O, H> {

    FilterFactory<? extends Filter<Object, Object>, Object, Object, ? super H>[] factories;

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
            FilterFactory<? extends Filter<?, ?>, ?, ?, ? super H>[] factories, //
            FilterFactory<? extends Filter<?, O>, ?, O, ? super H> factory) {

        this.factories = Arrays.copyOf(factories, factories.length + 1, FilterFactory[].class);
        this.factories[this.factories.length - 1] = //
        (FilterFactory<? extends Filter<Object, Object>, Object, Object, ? super H>) factory;
    }

    /**
     * Adds the given {@link FilterFactory} to the end of this chain.
     * 
     * @param <T>
     *            the outbound type of the resulting chain.
     * @return the resulting, longer chain.
     */
    public <T> ChainFilterFactory<I, T, H> add( //
            FilterFactory<? extends Filter<? super O, T>, ? super O, T, ? super H> factory) {
        return new ChainFilterFactory<I, T, H>(this.factories, factory);
    }

    @SuppressWarnings("unchecked")
    @Override
    public OobFilter<I, O> newFilter(H handler) {

        final int nFilters = this.factories.length;

        final OobFilter<Object, Object>[] filters = new OobFilter[nFilters];
        final Queue<Object>[] inboundsReadOnly = new Queue[nFilters - 1];
        final Queue<Object>[] inboundsWriteOnly = new Queue[nFilters - 1];
        final Queue<Object>[] outboundsReadOnly = new Queue[nFilters - 1];
        final Queue<Object>[] outboundsWriteOnly = new Queue[nFilters - 1];
        final Queue<OobEvent>[] inboundEvtsReadOnly = new Queue[nFilters - 1];
        final Queue<OobEvent>[] inboundEvtsWriteOnly = new Queue[nFilters - 1];
        final Queue<OobEvent>[] outboundEvtsReadOnly = new Queue[nFilters - 1];
        final Queue<OobEvent>[] outboundEvtsWriteOnly = new Queue[nFilters - 1];

        for (int i = 0; i < nFilters; i++) {
            filters[i] = Filters.asOobFilter(this.factories[i].newFilter(handler));
        }

        for (int i = 0, n = nFilters - 1; i < n; i++) {

            Queue<Object> inbounds = Filters.createQueue();
            inboundsReadOnly[i] = Filters.readOnlyQueue(inbounds);
            inboundsWriteOnly[i] = Filters.writeOnlyQueue(inbounds);

            Queue<Object> outbounds = Filters.createQueue();
            outboundsReadOnly[i] = Filters.readOnlyQueue(outbounds);
            outboundsWriteOnly[i] = Filters.writeOnlyQueue(outbounds);

            Queue<OobEvent> inboundEvts = Filters.createQueue();
            inboundEvtsReadOnly[i] = Filters.readOnlyQueue(inboundEvts);
            inboundEvtsWriteOnly[i] = Filters.writeOnlyQueue(inboundEvts);

            Queue<OobEvent> outboundEvts = Filters.createQueue();
            outboundEvtsReadOnly[i] = Filters.readOnlyQueue(outboundEvts);
            outboundEvtsWriteOnly[i] = Filters.writeOnlyQueue(outboundEvts);
        }

        return new OobFilter<I, O>() {

            @Override
            public void applyInbound(Queue<I> inputs, Queue<O> outputs) {

                switch (filters.length) {

                case 1:
                    ((Filter<I, O>) filters[0]).applyInbound(inputs, outputs);
                    break;

                default:

                    ((Filter<I, Object>) filters[0]).applyInbound(inputs, inboundsWriteOnly[0]);

                    for (int i = 1, n = nFilters - 1; i < n; i++) {
                        filters[i].applyInbound(inboundsReadOnly[i - 1], inboundsWriteOnly[i]);
                    }

                    ((Filter<Object, O>) filters[nFilters - 1]).applyInbound(inboundsReadOnly[nFilters - 2], outputs);

                    break;
                }
            }

            @Override
            public void applyOutbound(Queue<O> inputs, Queue<I> outputs) {

                switch (filters.length) {

                case 1:
                    ((Filter<I, O>) filters[0]).applyOutbound(inputs, outputs);
                    break;

                default:

                    ((Filter<Object, O>) filters[nFilters - 1]).applyOutbound(inputs, outboundsWriteOnly[nFilters - 2]);

                    for (int i = nFilters - 2; i >= 1; i--) {
                        filters[i].applyOutbound(outboundsReadOnly[i], outboundsWriteOnly[i - 1]);
                    }

                    ((Filter<I, Object>) filters[0]).applyOutbound(outboundsReadOnly[0], outputs);

                    break;
                }
            }

            @Override
            public void applyInboundOob(Queue<OobEvent> inputs, Queue<OobEvent> outputs) {

                switch (filters.length) {

                case 1:
                    filters[0].applyInboundOob(inputs, outputs);
                    break;

                default:

                    filters[0].applyInboundOob(inputs, inboundEvtsWriteOnly[0]);

                    for (int i = 1, n = nFilters - 1; i < n; i++) {
                        filters[i].applyInboundOob(inboundEvtsReadOnly[i - 1], inboundEvtsWriteOnly[i]);
                    }

                    filters[nFilters - 1].applyInboundOob(inboundEvtsReadOnly[nFilters - 2], outputs);

                    break;
                }
            }

            @Override
            public void applyOutboundOob(Queue<OobEvent> inputs, Queue<OobEvent> outputs) {

                switch (filters.length) {

                case 1:
                    filters[0].applyOutboundOob(inputs, outputs);
                    break;

                default:

                    filters[nFilters - 1].applyOutboundOob(inputs, outboundEvtsWriteOnly[nFilters - 2]);

                    for (int i = nFilters - 2; i >= 1; i--) {
                        filters[i].applyOutboundOob(outboundEvtsReadOnly[i], outboundEvtsWriteOnly[i - 1]);
                    }

                    filters[0].applyOutboundOob(outboundEvtsReadOnly[0], outputs);

                    break;
                }
            }
        };
    }
}

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

import java.nio.ByteBuffer;
import java.util.Queue;

import shared.event.Handler;
import shared.net.AbstractManagedConnection;
import shared.net.ConnectionManager;
import shared.net.filter.OobEvent.OobEventType;

/**
 * An abstract base class implementing much of {@link FilteredConnection}.
 * 
 * @apiviz.uses shared.net.filter.Filters
 * @param <C>
 *            the parameterization lower bounded by {@link FilteredManagedConnection} itself.
 * @param <T>
 *            the {@link Filter} inbound type.
 * @author Roy Liu
 */
abstract public class FilteredManagedConnection<C extends FilteredManagedConnection<C, T>, T> //
        extends AbstractManagedConnection<C> //
        implements FilteredConnection<C, T> {

    final Queue<ByteBuffer> inbounds;
    final Queue<ByteBuffer> inboundsReadOnly;
    final Queue<T> inboundsFiltered;
    final Queue<T> inboundsFilteredWriteOnly;

    final Queue<T> outbounds;
    final Queue<T> outboundsReadOnly;
    final Queue<ByteBuffer> outboundsFiltered;
    final Queue<ByteBuffer> outboundsFilteredWriteOnly;

    final Queue<OobEvent> inboundEvts;
    final Queue<OobEvent> inboundEvtsReadOnly;
    final Queue<OobEvent> inboundEvtsFiltered;
    final Queue<OobEvent> inboundEvtsFilteredWriteOnly;

    final Queue<OobEvent> outboundEvts;
    final Queue<OobEvent> outboundEvtsReadOnly;
    final Queue<OobEvent> outboundEvtsFiltered;
    final Queue<OobEvent> outboundEvtsFilteredWriteOnly;

    OobFilter<ByteBuffer, T> filter;

    /**
     * Default constructor.
     */
    public FilteredManagedConnection(String name, ConnectionManager manager) {
        super(name, manager);

        this.inbounds = Filters.createQueue();
        this.inboundsReadOnly = Filters.readOnlyQueue(this.inbounds);
        this.inboundsFiltered = Filters.createQueue();
        this.inboundsFilteredWriteOnly = Filters.writeOnlyQueue(this.inboundsFiltered);

        this.outbounds = Filters.createQueue();
        this.outboundsReadOnly = Filters.readOnlyQueue(this.outbounds);
        this.outboundsFiltered = Filters.createQueue();
        this.outboundsFilteredWriteOnly = Filters.writeOnlyQueue(this.outboundsFiltered);

        this.inboundEvts = Filters.createQueue();
        this.inboundEvtsReadOnly = Filters.readOnlyQueue(this.inboundEvts);
        this.inboundEvtsFiltered = Filters.createQueue();
        this.inboundEvtsFilteredWriteOnly = Filters.writeOnlyQueue(this.inboundEvtsFiltered);

        this.outboundEvts = Filters.createQueue();
        this.outboundEvtsReadOnly = Filters.readOnlyQueue(this.outboundEvts);
        this.outboundEvtsFiltered = Filters.createQueue();
        this.outboundEvtsFilteredWriteOnly = Filters.writeOnlyQueue(this.outboundEvtsFiltered);

        this.filter = new OobFilter<ByteBuffer, T>() {

            @Override
            public void applyInbound(Queue<ByteBuffer> inputs, Queue<T> outputs) {
                throw new UnsupportedOperationException("Please initialize the filter factory");
            }

            @Override
            public void applyOutbound(Queue<T> inputs, Queue<ByteBuffer> outputs) {
                throw new UnsupportedOperationException("Please initialize the filter factory");
            }

            @Override
            public void applyInboundOob(Queue<ByteBuffer> inputs, Queue<OobEvent> inputEvts, //
                    Queue<T> outputs, Queue<OobEvent> outputEvts) {
                throw new UnsupportedOperationException("Please initialize the filter factory");
            }

            @Override
            public void applyOutboundOob(Queue<T> inputs, Queue<OobEvent> inputEvts, //
                    Queue<ByteBuffer> outputs, Queue<OobEvent> outputEvts) {
                throw new UnsupportedOperationException("Please initialize the filter factory");
            }
        };
    }

    @SuppressWarnings("unchecked")
    @Override
    public C setFilterFactory(FilterFactory<? extends Filter<ByteBuffer, T>, ByteBuffer, T, ? super C> filterFactory) {

        this.filter = Filters.asOobFilter(filterFactory.newFilter((C) this));

        return (C) this;
    }

    @Override
    public int sendOutbound(T output) {

        // All outbound filtering is done under the protection of the connection monitor.
        synchronized (this) {

            if (output != null) {
                this.outbounds.add(output);
            }

            this.filter.applyOutbound(this.outboundsReadOnly, this.outboundsFilteredWriteOnly);

            int remaining = 0;

            for (ByteBuffer bb; (bb = this.outboundsFiltered.poll()) != null;) {
                remaining = send(bb);
            }

            return remaining;
        }
    }

    @Override
    public void onBind() {

        onOobEvent(OobEventType.BIND, null, new Handler<Queue<T>>() {

            @Override
            public void handle(Queue<T> inputs) {
                onBind(inputs);
            }
        });
    }

    @Override
    public void onReceive(ByteBuffer bb) {

        this.inbounds.add(bb);
        this.filter.applyInbound(this.inboundsReadOnly, this.inboundsFilteredWriteOnly);

        onReceive(this.inboundsFiltered);
    }

    @Override
    public void onClosing(final ClosingType type, ByteBuffer bb) {

        final OobEventType eventType;

        switch (type) {

        case EOS:
            eventType = OobEventType.CLOSING_EOS;
            break;

        case USER:
            eventType = OobEventType.CLOSING_USER;
            break;

        case ERROR:
            eventType = OobEventType.CLOSING_ERROR;
            break;

        default:
            throw new IllegalArgumentException("Invalid closing type");
        }

        onOobEvent(eventType, bb, new Handler<Queue<T>>() {

            @Override
            public void handle(Queue<T> inputs) {
                onClosing(type, inputs);
            }
        });
    }

    /**
     * Propagates an {@link OobEvent} through the underlying {@link OobFilter}.
     */
    protected void onOobEvent(OobEventType type, ByteBuffer bb, Handler<Queue<T>> handler) {

        OobEvent evt = new OobEvent(type, null);

        if (bb != null) {
            this.inbounds.add(bb);
        }

        this.inboundEvts.add(evt);
        this.filter.applyInboundOob( //
                this.inboundsReadOnly, this.inboundEvtsReadOnly, //
                this.inboundsFilteredWriteOnly, this.inboundEvtsFilteredWriteOnly);

        handler.handle(this.inboundsFiltered);

        // We can't do anything with events that are filtering byproducts.
        this.inboundEvtsFiltered.clear();

        synchronized (this) {

            this.outboundEvts.add(evt);
            this.filter.applyOutboundOob( //
                    this.outboundsReadOnly, this.outboundEvtsReadOnly, //
                    this.outboundsFilteredWriteOnly, this.outboundEvtsFilteredWriteOnly);

            for (; (bb = this.outboundsFiltered.poll()) != null;) {
                send(bb);
            }

            // We can't do anything with events that are filtering byproducts.
            this.outboundEvtsFiltered.clear();
        }
    }

    @Override
    public void onClose() {
        // No further action is required.
    }
}

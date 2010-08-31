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
import shared.net.filter.OOBEvent.OOBEventType;
import shared.util.Control;

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

    final Queue<ByteBuffer> in;
    final Queue<ByteBuffer> inReadOnly;
    final Queue<T> inFiltered;
    final Queue<T> inFilteredWriteOnly;
    final Queue<T> out;
    final Queue<T> outReadOnly;
    final Queue<ByteBuffer> outFiltered;
    final Queue<ByteBuffer> outFilteredWriteOnly;
    final Queue<OOBEvent> inEvts;
    final Queue<OOBEvent> inEvtsReadOnly;
    final Queue<OOBEvent> inEvtsFiltered;
    final Queue<OOBEvent> inEvtsFilteredWriteOnly;
    final Queue<OOBEvent> outEvts;
    final Queue<OOBEvent> outEvtsReadOnly;
    final Queue<OOBEvent> outEvtsFiltered;
    final Queue<OOBEvent> outEvtsFilteredWriteOnly;

    OOBFilter<ByteBuffer, T> filter;
    Filter<ByteBuffer, T> bindFilter;
    Filter<ByteBuffer, T> shutdownFilter;

    /**
     * Default constructor.
     */
    public FilteredManagedConnection(String name, ConnectionManager manager) {
        super(name, manager);

        this.in = Filters.createQueue();
        this.inReadOnly = Filters.readOnlyQueue(this.in);
        this.inFiltered = Filters.createQueue();
        this.inFilteredWriteOnly = Filters.writeOnlyQueue(this.inFiltered);
        this.out = Filters.createQueue();
        this.outReadOnly = Filters.readOnlyQueue(this.out);
        this.outFiltered = Filters.createQueue();
        this.outFilteredWriteOnly = Filters.writeOnlyQueue(this.outFiltered);
        this.inEvts = Filters.createQueue();
        this.inEvtsReadOnly = Filters.readOnlyQueue(this.inEvts);
        this.inEvtsFiltered = Filters.createQueue();
        this.inEvtsFilteredWriteOnly = Filters.writeOnlyQueue(this.inEvtsFiltered);
        this.outEvts = Filters.createQueue();
        this.outEvtsReadOnly = Filters.readOnlyQueue(this.outEvts);
        this.outEvtsFiltered = Filters.createQueue();
        this.outEvtsFilteredWriteOnly = Filters.writeOnlyQueue(this.outEvtsFiltered);

        this.filter = new OOBFilter<ByteBuffer, T>() {

            @Override
            public void getInbound(Queue<ByteBuffer> in, Queue<T> out) {
                throw new IllegalArgumentException("Please initialize the filter factory");
            }

            @Override
            public void getOutbound(Queue<T> in, Queue<ByteBuffer> out) {
                throw new IllegalArgumentException("Please initialize the filter factory");
            }

            @Override
            public void getInboundOOB(Queue<ByteBuffer> in, Queue<OOBEvent> inEvts, //
                    Queue<T> out, Queue<OOBEvent> outEvts) {
                throw new IllegalArgumentException("Please initialize the filter factory");
            }

            @Override
            public void getOutboundOOB(Queue<T> in, Queue<OOBEvent> inEvts, //
                    Queue<ByteBuffer> out, Queue<OOBEvent> outEvts) {
                throw new IllegalArgumentException("Please initialize the filter factory");
            }
        };
    }

    @SuppressWarnings("unchecked")
    @Override
    public C setFilterFactory(FilterFactory<? extends Filter<ByteBuffer, T>, ByteBuffer, T, ? super C> filterFactory) {

        Filter<ByteBuffer, T> filter = filterFactory.newFilter((C) this);

        synchronized (this) {

            Control.checkTrue(!isSubmitted(), //
                    "Cannot modify already submitted connections");

            this.filter = Filters.asOOBFilter(filter);
        }

        return (C) this;
    }

    @Override
    public int sendOutbound(T output) {

        // All outbound filtering is done under the protection of the connection monitor.
        synchronized (this) {

            if (output != null) {
                this.out.add(output);
            }

            this.filter.getOutbound(this.outReadOnly, this.outFilteredWriteOnly);

            int remaining = 0;

            for (ByteBuffer bb; (bb = this.outFiltered.poll()) != null;) {
                remaining = send(bb);
            }

            return remaining;
        }
    }

    @Override
    public void onReceive(ByteBuffer input) {

        this.in.add(input);
        this.filter.getInbound(this.inReadOnly, this.inFilteredWriteOnly);

        onReceive(this.inFiltered);
    }

    @Override
    public void onBind() {

        onOOBEvent(OOBEventType.BIND, null, new Handler<Queue<T>>() {

            @Override
            public void handle(Queue<T> inbounds) {
                onBind(inbounds);
            }
        });
    }

    @Override
    public void onClosing(final ClosingType type, ByteBuffer bb) {

        final OOBEventType eventType;

        switch (type) {

        case EOS:
            eventType = OOBEventType.CLOSING_EOS;
            break;

        case USER:
            eventType = OOBEventType.CLOSING_USER;
            break;

        case ERROR:
            eventType = OOBEventType.CLOSING_ERROR;
            break;

        default:
            throw new AssertionError("Control should never reach here");
        }

        onOOBEvent(eventType, bb, new Handler<Queue<T>>() {

            @Override
            public void handle(Queue<T> inbounds) {
                onClosing(type, inbounds);
            }
        });
    }

    /**
     * Propagates an {@link OOBEvent} through the underlying {@link OOBFilter}.
     */
    protected void onOOBEvent(OOBEventType type, ByteBuffer input, Handler<Queue<T>> handler) {

        OOBEvent evt = new OOBEvent(type, null);

        if (input != null) {
            this.in.add(input);
        }

        this.inEvts.add(evt);
        this.filter.getInboundOOB( //
                this.inReadOnly, this.inEvtsReadOnly, //
                this.inFilteredWriteOnly, this.inEvtsFilteredWriteOnly);

        handler.handle(this.inFiltered);

        // We can't do anything with events that are filtering byproducts.
        this.inEvtsFiltered.clear();

        synchronized (this) {

            this.outEvts.add(evt);
            this.filter.getOutboundOOB( //
                    this.outReadOnly, this.outEvtsReadOnly, //
                    this.outFilteredWriteOnly, this.outEvtsFilteredWriteOnly);

            for (ByteBuffer bb; (bb = this.outFiltered.poll()) != null;) {
                send(bb);
            }

            // We can't do anything with events that are filtering byproducts.
            this.outEvtsFiltered.clear();
        }
    }

    @Override
    public void onClose() {
        // No further action is required.
    }
}

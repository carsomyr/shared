/**
 * This file is part of the Shared Scientific Toolbox in Java ("this library"). <br />
 * <br />
 * Copyright (C) 2009-2010 Roy Liu <br />
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

import java.nio.ByteBuffer;
import java.util.Queue;

import shared.event.Handler;
import shared.net.AbstractManagedConnection;
import shared.net.ConnectionManager;
import shared.net.filter.OOBEvent.OOBEventType;
import shared.util.Control;

/**
 * A subclass of {@link AbstractManagedConnection} that also implements {@link FilteredConnection}.
 * 
 * @apiviz.uses shared.net.filter.Filters
 * @param <C>
 *            the parameterization lower bounded by {@link FilteredManagedConnection} itself.
 * @param <T>
 *            the {@link Filter} output type.
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
     * 
     * @see AbstractManagedConnection#AbstractManagedConnection(String, ConnectionManager)
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

            public void getInbound(Queue<ByteBuffer> in, Queue<T> out) {
                throw new IllegalArgumentException("Please initialize the filter factory");
            }

            public void getOutbound(Queue<T> in, Queue<ByteBuffer> out) {
                throw new IllegalArgumentException("Please initialize the filter factory");
            }

            public void getInboundOOB(Queue<ByteBuffer> in, Queue<OOBEvent> inEvts, //
                    Queue<T> out, Queue<OOBEvent> outEvts) {
                throw new IllegalArgumentException("Please initialize the filter factory");
            }

            public void getOutboundOOB(Queue<T> in, Queue<OOBEvent> inEvts, //
                    Queue<ByteBuffer> out, Queue<OOBEvent> outEvts) {
                throw new IllegalArgumentException("Please initialize the filter factory");
            }
        };
    }

    @SuppressWarnings("unchecked")
    public C setFilterFactory(FilterFactory<? extends Filter<ByteBuffer, T>, ByteBuffer, T, ? super C> filterFactory) {

        Filter<ByteBuffer, T> filter = filterFactory.newFilter((C) this);

        synchronized (this) {

            Control.checkTrue(!isSubmitted(), //
                    "Cannot modify already submitted connections");

            this.filter = Filters.asOOBFilter(filter);
        }

        return (C) this;
    }

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

    public void onReceive(ByteBuffer input) {

        this.in.add(input);
        this.filter.getInbound(this.inReadOnly, this.inFilteredWriteOnly);

        onReceiveInbound(this.inFiltered);
    }

    public void onBind() {

        onOOBEvent(OOBEventType.BIND, null, new Handler<Queue<T>>() {

            public void handle(Queue<T> inbounds) {
                onBindInbound(inbounds);
            }
        });
    }

    public void onClosingUser(ByteBuffer bb) {

        onOOBEvent(OOBEventType.CLOSE_USER, bb, new Handler<Queue<T>>() {

            public void handle(Queue<T> inbounds) {
                onCloseInbound(inbounds);
            }
        });
    }

    public void onClosingEOS(ByteBuffer bb) {

        onOOBEvent(OOBEventType.CLOSE_EOS, bb, new Handler<Queue<T>>() {

            public void handle(Queue<T> inbounds) {
                onEOSInbound(inbounds);
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

    public void onClose() {
        // No further action is required.
    }
}

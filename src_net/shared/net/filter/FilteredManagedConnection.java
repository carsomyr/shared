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

import java.nio.ByteBuffer;
import java.util.Queue;

import shared.event.Handler;
import shared.net.AbstractManagedConnection;
import shared.net.ConnectionManager;
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

    final Handler<Queue<T>> receiveHandler = new Handler<Queue<T>>() {

        public void handle(Queue<T> inbounds) {
            onReceiveInbound(inbounds);
        }
    };

    final Queue<ByteBuffer> in;
    final Queue<ByteBuffer> inReadOnly;
    final Queue<T> inFiltered;
    final Queue<T> inFilteredWriteOnly;
    final Queue<T> out;
    final Queue<T> outReadOnly;
    final Queue<ByteBuffer> outFiltered;
    final Queue<ByteBuffer> outFilteredWriteOnly;

    Filter<ByteBuffer, T> sessionFilter;
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

        this.sessionFilter = this.bindFilter = this.shutdownFilter = new Filter<ByteBuffer, T>() {

            public void getInbound(Queue<ByteBuffer> in, Queue<T> out) {
                throw new IllegalArgumentException("Please initialize the filter factory");
            }

            public void getOutbound(Queue<T> in, Queue<ByteBuffer> out) {
                throw new IllegalArgumentException("Please initialize the filter factory");
            }
        };
    }

    @SuppressWarnings("unchecked")
    public C setFilterFactory(FilterFactory<ByteBuffer, T, ? super C> filterFactory) {

        Filter<ByteBuffer, T> filter = filterFactory.newFilter((C) this);

        synchronized (this) {

            Control.checkTrue(!isSubmitted(), //
                    "Cannot modify already submitted connections");

            if (filter instanceof StatefulFilter) {

                StatefulFilter<ByteBuffer, T> statefulFilter = (StatefulFilter<ByteBuffer, T>) filter;
                Filter<ByteBuffer, T>[] filters = Filters.unroll(statefulFilter);

                this.sessionFilter = filters[0];
                this.bindFilter = filters[1];
                this.shutdownFilter = filters[2];

            } else {

                this.sessionFilter = this.bindFilter = this.shutdownFilter = filter;
            }
        }

        return (C) this;
    }

    public int sendOutbound(T output) {

        // All outbound filtering is done under the protection of the connection monitor.
        synchronized (this) {
            return doOutbound(this.sessionFilter, output);
        }
    }

    /**
     * Processes an outbound object with the given {@link Filter}.
     * 
     * @param filter
     *            the {@link Filter}.
     * @param outbound
     *            the outbound object.
     * @return the number of bytes remaining to be written.
     */
    protected int doOutbound(Filter<ByteBuffer, T> filter, T outbound) {

        if (outbound != null) {
            this.out.add(outbound);
        }

        filter.getOutbound(this.outReadOnly, this.outFilteredWriteOnly);

        int remaining = 0;

        for (ByteBuffer bb = null; (bb = this.outFiltered.poll()) != null;) {
            remaining = send(bb);
        }

        return remaining;
    }

    public void onReceive(ByteBuffer bb) {
        doInbound(this.sessionFilter, bb, this.receiveHandler);
    }

    /**
     * Processes inbound data with the given {@link Filter} and inbound {@link Handler}.
     * 
     * @param filter
     *            the {@link Filter}.
     * @param inbound
     *            the inbound data.
     * @param handler
     *            the {@link Handler} for filtered inbound data.
     */
    protected void doInbound(Filter<ByteBuffer, T> filter, ByteBuffer inbound, Handler<Queue<T>> handler) {

        if (inbound != null) {
            this.in.add(inbound);
        }

        filter.getInbound(this.inReadOnly, this.inFilteredWriteOnly);

        handler.handle(this.inFiltered);
    }

    public void onBind() {
        doStatefulFilterLifeCycle(this.bindFilter, null, new Handler<Queue<T>>() {

            public void handle(Queue<T> inbounds) {
                onBindInbound(inbounds);
            }
        });
    }

    public void onClosingUser(ByteBuffer bb) {
        doStatefulFilterLifeCycle(this.shutdownFilter, bb, new Handler<Queue<T>>() {

            public void handle(Queue<T> inbounds) {
                onCloseInbound(inbounds);
            }
        });
    }

    public void onClosingEOS(ByteBuffer bb) {
        doStatefulFilterLifeCycle(this.shutdownFilter, bb, new Handler<Queue<T>>() {

            public void handle(Queue<T> inbounds) {
                onEOSInbound(inbounds);
            }
        });
    }

    /**
     * Processes parts of the {@link StatefulFilter} life cycle: {@link StatefulFilter#bindInbound(Queue, Queue)},
     * {@link StatefulFilter#bindOutbound(Queue, Queue)}, and {@link StatefulFilter#shutdownInbound(Queue, Queue)},
     * {@link StatefulFilter#shutdownOutbound(Queue, Queue)}.
     */
    protected void doStatefulFilterLifeCycle(Filter<ByteBuffer, T> filter, ByteBuffer bb, Handler<Queue<T>> handler) {

        doInbound(filter, bb, handler);

        synchronized (this) {
            doOutbound(filter, null);
        }
    }

    public void onClose() {
        // No further action is required.
    }
}

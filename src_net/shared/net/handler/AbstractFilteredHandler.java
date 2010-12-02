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

package shared.net.handler;

import static shared.net.filter.OobEvent.OobEventType.BIND;
import static shared.net.filter.OobEvent.OobEventType.CLOSING_EOS;
import static shared.net.filter.OobEvent.OobEventType.CLOSING_ERROR;
import static shared.net.filter.OobEvent.OobEventType.CLOSING_USER;
import static shared.net.filter.OobEvent.OobEventType.USER;

import java.io.Closeable;
import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import shared.net.Connection;
import shared.net.filter.BaseOobEvent;
import shared.net.filter.Filter;
import shared.net.filter.FilterFactory;
import shared.net.filter.Filters;
import shared.net.filter.OobEvent;
import shared.net.filter.OobEvent.OobEventType;
import shared.net.filter.OobFilter;

/**
 * An abstract base class implementing much of {@link FilteredHandler}.
 * 
 * @apiviz.uses shared.net.filter.Filters
 * @param <H>
 *            the parameterization lower bounded by {@link AbstractFilteredHandler} itself.
 * @param <C>
 *            the {@link Connection} type.
 * @param <T>
 *            the {@link Filter} inbound type.
 * @author Roy Liu
 */
abstract public class AbstractFilteredHandler<H extends AbstractFilteredHandler<H, C, T>, C extends Connection, T> //
        implements FilteredHandler<H, C, T>, OobHandler<C>, Closeable {

    /**
     * A trivial {@link Future} that returns immediately with {@code null}.
     */
    final protected static Future<?> dummyFuture = new Future<Object>() {

        @Override
        public Object get() {
            return null;
        }

        @Override
        public Object get(long timeout, TimeUnit unit) {
            return null;
        }

        @Override
        public boolean isDone() {
            return true;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }
    };

    final String name;

    final Queue<ByteBuffer> inbounds;
    final Queue<ByteBuffer> inboundsReadOnly;
    final Queue<T> inboundsFiltered;
    final Queue<T> inboundsFilteredReadOnly;
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

    OobFilter<T, ByteBuffer> filter;

    C connection;

    /**
     * Default constructor.
     */
    public AbstractFilteredHandler(String name) {

        this.name = name;

        this.inbounds = Filters.createQueue();
        this.inboundsReadOnly = Filters.readOnlyQueue(this.inbounds);
        this.inboundsFiltered = Filters.createQueue();
        this.inboundsFilteredReadOnly = Filters.readOnlyQueue(this.inboundsFiltered);
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

        this.filter = new OobFilter<T, ByteBuffer>() {

            @Override
            public void applyInbound(Queue<ByteBuffer> inputs, Queue<T> outputs) {
                throw new UnsupportedOperationException("Please initialize the filter factory");
            }

            @Override
            public void applyOutbound(Queue<T> inputs, Queue<ByteBuffer> outputs) {
                throw new UnsupportedOperationException("Please initialize the filter factory");
            }

            @Override
            public void applyInboundOob(Queue<OobEvent> inputs, Queue<OobEvent> outputs) {
                throw new UnsupportedOperationException("Please initialize the filter factory");
            }

            @Override
            public void applyOutboundOob(Queue<OobEvent> inputs, Queue<OobEvent> outputs) {
                throw new UnsupportedOperationException("Please initialize the filter factory");
            }
        };

        this.connection = null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public H setFilterFactory(FilterFactory<? extends Filter<T, ByteBuffer>, T, ByteBuffer, ? super H> filterFactory) {

        this.filter = Filters.asOobFilter(filterFactory.newFilter((H) this));

        return (H) this;
    }

    @Override
    public int send(T output) {

        // All outbound filtering is done under the protection of the connection monitor.
        synchronized (this.connection.getLock()) {

            if (output != null) {
                this.outbounds.add(output);
            }

            this.filter.applyOutbound(this.outboundsReadOnly, this.outboundsFilteredWriteOnly);

            int remaining = 0;

            for (ByteBuffer bb; (bb = this.outboundsFiltered.poll()) != null;) {
                remaining = this.connection.send(bb);
            }

            return remaining;
        }
    }

    @Override
    public void onBind() {
        onOob(new BaseOobEvent(BIND, null), null);
    }

    @Override
    public void onReceive(ByteBuffer bb) {

        this.inbounds.add(bb);
        this.filter.applyInbound(this.inboundsReadOnly, this.inboundsFilteredWriteOnly);

        onReceive(this.inboundsFilteredReadOnly);
    }

    @Override
    public void onClosing(final ClosingType type, ByteBuffer bb) {

        final OobEventType eventType;

        switch (type) {

        case EOS:
            eventType = CLOSING_EOS;
            break;

        case USER:
            eventType = CLOSING_USER;
            break;

        case ERROR:
            eventType = CLOSING_ERROR;
            break;

        default:
            throw new IllegalArgumentException("Invalid closing type");
        }

        onOob(new BaseOobEvent(eventType, null), bb);
    }

    @Override
    public Future<?> onOob(final OobEvent evt) {

        if (evt.getType() != USER) {
            throw new IllegalArgumentException("User-defined out-of-band events must have type USER");
        }

        // Are we in the manager thread?
        if (this.connection.isManagerThread()) {

            onOob(evt, null);

            return dummyFuture;
        }
        // If not, ensure thread safety.
        else {

            return this.connection.invoke(new Callable<Object>() {

                @Override
                public Object call() {

                    onOob(evt, null);

                    return null;
                }
            });
        }
    }

    @Override
    public C getConnection() {
        return this.connection;
    }

    @Override
    public void setConnection(C connection) {
        this.connection = connection;
    }

    @Override
    public void close() {

        C connection = this.connection;

        if (connection != null) {
            connection.close();
        }
    }

    @Override
    public String toString() {
        return this.name;
    }

    /**
     * Propagates the given {@link OobEvent} through the underlying {@link OobFilter}.
     */
    protected void onOob(OobEvent evt, ByteBuffer bb) {

        if (bb != null) {
            this.inbounds.add(bb);
        }

        // Propagate OOB information and filter.
        this.inboundEvts.add(evt);
        this.filter.applyInboundOob(this.inboundEvtsReadOnly, this.inboundEvtsFilteredWriteOnly);
        this.filter.applyInbound(this.inboundsReadOnly, this.inboundsFilteredWriteOnly);

        switch (evt.getType()) {

        case BIND:
            onBind(this.inboundsFilteredReadOnly);
            break;

        case CLOSING_EOS:
            onClosing(ClosingType.EOS, this.inboundsFilteredReadOnly);
            break;

        case CLOSING_USER:
            onClosing(ClosingType.USER, this.inboundsFilteredReadOnly);
            break;

        case CLOSING_ERROR:
            onClosing(ClosingType.ERROR, this.inboundsFilteredReadOnly);
            break;

        case USER:
            // Do nothing.
            break;

        default:
            throw new IllegalArgumentException("Invalid event type");
        }

        // We can't do anything with events that are filtering byproducts.
        this.inboundEvtsFiltered.clear();

        synchronized (this.connection.getLock()) {

            // Propagate OOB information and filter.
            this.outboundEvts.add(evt);
            this.filter.applyOutboundOob(this.outboundEvtsReadOnly, this.outboundEvtsFilteredWriteOnly);
            this.filter.applyOutbound(this.outboundsReadOnly, this.outboundsFilteredWriteOnly);

            for (; (bb = this.outboundsFiltered.poll()) != null;) {
                this.connection.send(bb);
            }

            // We can't do anything with events that are filtering byproducts.
            this.outboundEvtsFiltered.clear();
        }
    }
}

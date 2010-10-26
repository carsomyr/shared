/**
 * <p>
 * Copyright (c) 2005-2010 Roy Liu<br>
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

package shared.net.nio;

import static shared.net.Constants.DEFAULT_BUFFER_SIZE;
import static shared.net.nio.NioEvent.NioEventType.ACCEPT;
import static shared.net.nio.NioEvent.NioEventType.CLOSE;
import static shared.net.nio.NioEvent.NioEventType.CONNECT;
import static shared.net.nio.NioEvent.NioEventType.ERROR;
import static shared.net.nio.NioEvent.NioEventType.EXECUTE;
import static shared.net.nio.NioEvent.NioEventType.OP;
import static shared.net.nio.NioEvent.NioEventType.REGISTER;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import shared.event.EnumStatus;
import shared.net.Buffers;
import shared.net.SocketConnection;
import shared.net.nio.NioEvent.NioEventType;
import shared.util.Control;

/**
 * An abstract asynchronous sockets class internally managed by {@link NioManager}. Instantiating classes must implement
 * callbacks for receipt of data, connecting, accepting, and error handling.
 * 
 * @apiviz.composedOf shared.net.nio.NioConnection.WriteHandler
 * @apiviz.owns shared.net.nio.NioConnection.NioConnectionStatus
 * @apiviz.owns shared.net.nio.ProxySource
 * @apiviz.uses shared.net.Buffers
 * @apiviz.uses shared.net.Constants
 * @param <C>
 *            the parameterization lower bounded by {@link NioConnection} itself.
 * @author Roy Liu
 */
abstract public class NioConnection<C extends NioConnection<C>> //
        implements SocketConnection<C>, EnumStatus<NioConnection.NioConnectionStatus> {

    /**
     * An enumeration of connection states.
     */
    public enum NioConnectionStatus {

        /**
         * The connection is virgin.
         */
        VIRGIN, //

        /**
         * A connect request has been made.
         */
        CONNECT, //

        /**
         * An accept request has been made.
         */
        ACCEPT, //

        /**
         * The connection is active.
         */
        ACTIVE, //

        /**
         * The connection is closing.
         */
        CLOSING, //

        /**
         * The connection is closed.
         */
        CLOSED;
    }

    /**
     * A bit flag indicating that the connection has been submitted to an {@link NioManager}.
     */
    final protected static int FLAG_SUBMITTED = 1 << 0;

    /**
     * A bit flag indicating that the connection has been bound.
     */
    final protected static int FLAG_BOUND = 1 << 1;

    /**
     * A bit flag indicating that the connection has been closed.
     */
    final protected static int FLAG_CLOSED = 1 << 2;

    /**
     * Defines a handler for outgoing data.
     */
    protected interface WriteHandler {

        /**
         * Attempts to write out the contents of a {@link ByteBuffer}.
         * 
         * @return the number of {@code byte}s remaining in this connection's write buffer.
         */
        public int write(ByteBuffer bb);
    }

    /**
     * A {@link WriteHandler} used when writes are deferred. Enqueues data without further action.
     */
    final protected WriteHandler bufferedHandler = new WriteHandler() {

        @Override
        public int write(ByteBuffer bb) {

            NioConnection<C> conn = NioConnection.this;
            assert Thread.holdsLock(conn.getLock());

            conn.writeBuffer = Buffers.append(conn.writeBuffer, bb, 1);

            return conn.writeBuffer.position();
        }
    };

    /**
     * A {@link WriteHandler} used when bytes can be written through directly to the underlying transport.
     */
    final protected WriteHandler writeThroughHandler = new WriteHandler() {

        @Override
        public int write(ByteBuffer bb) {

            NioConnection<C> conn = NioConnection.this;
            assert Thread.holdsLock(conn.getLock());

            assert (conn.writeBuffer.position() == 0);

            try {

                for (; bb.hasRemaining() && conn.channel.write(bb) > 0;) {
                }

                conn.writeBuffer = Buffers.append(conn.writeBuffer, bb, 1);

                int remaining = conn.writeBuffer.position();

                if (remaining > 0) {

                    conn.thread.debug("[%s] writes deferred.", conn);

                    conn.setBufferedHandler();
                    conn.setEnabled(OperationType.WRITE, true);
                }

                return remaining;

            } catch (Throwable t) {

                conn.setNullHandler();
                conn.setException(t);

                return 0;
            }
        }
    };

    /**
     * A {@link WriteHandler} that forgets all data offered to it.
     */
    final protected WriteHandler nullHandler = new WriteHandler() {

        @Override
        public int write(ByteBuffer bb) {

            NioConnection<C> conn = NioConnection.this;
            assert Thread.holdsLock(conn.getLock());

            // Set the position to the limit to simulate that we have read all bytes.
            bb.position(bb.limit());

            return 0;
        }
    };

    /**
     * An internal handler for deferred writes.
     */
    final protected Runnable deferredHandler = new Runnable() {

        @Override
        public void run() {

            NioConnection<C> conn = NioConnection.this;
            Object lock = conn.getLock();
            assert !Thread.holdsLock(lock);

            final boolean disableWrites;

            try {

                synchronized (lock) {

                    for (conn.writeBuffer.flip(); //
                    conn.writeBuffer.hasRemaining() && conn.channel.write(conn.writeBuffer) > 0;) {
                    }

                    conn.writeBuffer.compact();

                    disableWrites = (conn.writeBuffer.position() == 0);

                    if (disableWrites) {

                        if (conn.writeBuffer.capacity() > conn.bufferSize) {
                            conn.writeBuffer = ByteBuffer.allocate(conn.bufferSize);
                        }

                        conn.thread.debug("[%s] canceled write defer.", conn);

                        // Notify anyone waiting for restoration of the write-through handler.
                        conn.setWriteThroughHandler();
                        lock.notifyAll();
                    }
                }

            } catch (Throwable t) {

                conn.thread.handleError(conn, t);

                return;
            }

            // No longer under the protection of the monitor, perform the operation interest change.
            if (disableWrites) {
                conn.thread.handleOp(conn, SelectionKey.OP_WRITE, false);
            }
        }
    };

    /**
     * An internal handler for connection closure.
     */
    final protected Runnable closingHandler = new Runnable() {

        @Override
        public void run() {

            NioConnection<C> conn = NioConnection.this;
            Object lock = conn.getLock();
            assert !Thread.holdsLock(lock);

            final boolean closeConnection;

            try {

                synchronized (lock) {

                    for (conn.writeBuffer.flip(); //
                    conn.writeBuffer.hasRemaining() && conn.channel.write(conn.writeBuffer) > 0;) {
                    }

                    conn.writeBuffer.compact();

                    closeConnection = (conn.writeBuffer.position() == 0);
                }

            } catch (Throwable t) {

                conn.thread.handleError(conn, t);

                return;
            }

            // No longer under the protection of the monitor, close the connection.
            if (closeConnection) {
                conn.thread.handleClose(conn);
            }
        }
    };

    /**
     * External thread call -- Submits this connection to its associated {@link NioManager}.
     * 
     * @param <T>
     *            the argument type.
     * @param type
     *            the {@link shared.net.Connection.InitializationType}.
     * @param argument
     *            the argument.
     */
    @Override
    public <R, T> Future<R> init(InitializationType type, T argument) {

        synchronized (getLock()) {

            Control.checkTrue((this.stateMask & FLAG_SUBMITTED) == 0, //
                    "The connection has already been submitted");

            this.stateMask |= FLAG_SUBMITTED;
        }

        final NioEventType eventType;

        switch (type) {

        case CONNECT:
            eventType = CONNECT;
            break;

        case ACCEPT:
            eventType = ACCEPT;
            break;

        case REGISTER:
            eventType = REGISTER;
            break;

        default:
            throw new IllegalArgumentException("Invalid initialization type");
        }

        this.manager.initConnection(this, eventType, argument);

        return new Future<R>() {

            @Override
            public R get() throws InterruptedException, ExecutionException {

                NioConnection<?> conn = NioConnection.this;
                Object lock = conn.getLock();

                synchronized (lock) {

                    for (; !isDone();) {
                        lock.wait();
                    }
                }

                if (conn.exception != null) {
                    throw new ExecutionException(conn.exception);
                }

                return getResult();
            }

            @Override
            public R get(long timeout, TimeUnit unit) //
                    throws InterruptedException, ExecutionException, TimeoutException {

                NioConnection<?> conn = NioConnection.this;
                Object lock = conn.getLock();

                long timeoutMillis = unit.toMillis(timeout);

                synchronized (lock) {

                    for (long remaining = timeoutMillis, end = System.currentTimeMillis() + timeoutMillis; //
                    !isDone() && remaining > 0; //
                    remaining = end - System.currentTimeMillis()) {
                        lock.wait(remaining);
                    }

                    if (!isDone()) {
                        throw new TimeoutException("Operation timed out");
                    }
                }

                if (conn.exception != null) {
                    throw new ExecutionException(conn.exception);
                }

                return getResult();
            }

            @Override
            public boolean isDone() {

                NioConnection<?> conn = NioConnection.this;

                synchronized (conn.getLock()) {
                    return (conn.stateMask & (FLAG_BOUND | FLAG_CLOSED)) != 0;
                }
            }

            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            /**
             * Gets the result tailored to the {@link shared.net.Connection.InitializationType}.
             */
            @SuppressWarnings("unchecked")
            protected R getResult() {

                switch (eventType) {

                case CONNECT:
                    return (R) getLocalAddress();

                case ACCEPT:
                    return (R) getRemoteAddress();

                case REGISTER:
                    return null;

                default:
                    throw new IllegalArgumentException("Invalid event type");
                }
            }
        };
    }

    @Override
    public int send(ByteBuffer bb) {

        // All send operations are performed under the protection of the connection monitor.
        synchronized (getLock()) {
            return this.writeHandler.write(bb);
        }
    }

    @Override
    public void setEnabled(OperationType type, boolean enabled) {

        final int opType;

        switch (type) {

        case READ:
            opType = SelectionKey.OP_READ;
            break;

        case WRITE:
            opType = SelectionKey.OP_WRITE;
            break;

        default:
            throw new IllegalArgumentException("Invalid operation type");
        }

        this.proxy.onLocal(createOpEvent(opType, enabled));
    }

    @Override
    public Throwable getException() {
        return this.exception;
    }

    @Override
    public void setException(Throwable exception) {
        this.proxy.onLocal(new NioEvent<Throwable>(ERROR, exception, this.proxy));
    }

    @Override
    public Object getLock() {
        return this;
    }

    @Override
    public InetSocketAddress getLocalAddress() {

        synchronized (getLock()) {

            Control.checkTrue((this.stateMask & FLAG_BOUND) != 0, //
                    "Connection must be bound");

            return (InetSocketAddress) this.channel.socket().getLocalSocketAddress();
        }
    }

    @Override
    public InetSocketAddress getRemoteAddress() {

        synchronized (getLock()) {

            Control.checkTrue((this.stateMask & FLAG_BOUND) != 0, //
                    "Connection must be bound");

            return (InetSocketAddress) this.channel.socket().getRemoteSocketAddress();
        }
    }

    @Override
    public int getBufferSize() {
        return this.bufferSize;
    }

    @SuppressWarnings("unchecked")
    @Override
    public C setBufferSize(int bufferSize) {

        this.bufferSize = bufferSize;

        return (C) this;
    }

    @Override
    public void execute(Runnable r) {
        this.proxy.onLocal(new NioEvent<Runnable>(EXECUTE, r, this.proxy));
    }

    @Override
    public void close() {
        this.proxy.onLocal(new NioEvent<Object>(CLOSE, this.proxy));
    }

    @Override
    public String toString() {
        return String.format("%s[%s]", this.name, this.status);
    }

    @Override
    public NioConnectionStatus getStatus() {
        return this.status;
    }

    @Override
    public void setStatus(NioConnectionStatus status) {
        this.status = status;
    }

    final NioManager manager;
    final String name;
    final ProxySource<C> proxy;

    NioManagerThread thread;
    WriteHandler writeHandler;
    Runnable internalHandler;
    SelectionKey key;
    SocketChannel channel;
    int bufferSize;
    ByteBuffer readBuffer;
    ByteBuffer writeBuffer;
    int stateMask;
    Throwable exception;
    NioConnectionStatus status;

    /**
     * Default constructor.
     * 
     * @param name
     *            the name of this connection.
     * @param manager
     *            the {@link NioManager} with which this connection will be registered.
     */
    @SuppressWarnings("unchecked")
    protected NioConnection(String name, NioManager manager) {

        this.manager = manager;
        this.name = name;

        this.proxy = new ProxySource<C>((C) this);

        //

        this.thread = manager.getThread();

        // The connection starts with writes deferred to the manager.
        this.writeHandler = this.bufferedHandler;
        this.internalHandler = this.deferredHandler;

        this.key = null;
        this.channel = null;

        this.bufferSize = DEFAULT_BUFFER_SIZE;

        this.readBuffer = ByteBuffer.allocate(0);
        this.writeBuffer = ByteBuffer.allocate(0);

        this.stateMask = 0;
        this.exception = null;
        this.status = NioConnectionStatus.VIRGIN;
    }

    /**
     * Alternate constructor.
     */
    protected NioConnection(String name) {
        this(name, NioManager.getInstance());
    }

    /**
     * Gets the {@link ProxySource} representing this connection.
     */
    protected ProxySource<C> getProxy() {
        return this.proxy;
    }

    /**
     * Sets {@link #nullHandler}.
     */
    protected void setNullHandler() {
        this.writeHandler = this.nullHandler;
    }

    /**
     * Sets {@link #bufferedHandler}.
     */
    protected void setBufferedHandler() {
        this.writeHandler = this.bufferedHandler;
    }

    /**
     * Sets {@link #writeThroughHandler}.
     */
    protected void setWriteThroughHandler() {
        this.writeHandler = this.writeThroughHandler;
    }

    /**
     * Gets the {@link NioManagerThread} currently servicing this connection.
     */
    protected NioManagerThread getThread() {
        return this.thread;
    }

    /**
     * Sets the {@link NioManagerThread} currently servicing this connection.
     */
    protected void setThread(NioManagerThread thread) {
        this.thread = thread;
    }

    /**
     * Creates an operation interest change {@link NioEvent} that originates from this connection.
     */
    protected NioEvent<Integer> createOpEvent(int mask, boolean enabled) {
        return new NioEvent<Integer>(OP, mask | (enabled ? 0x80000000 : 0x0), this.proxy);
    }

    /**
     * Gets the {@link SelectionKey}.
     */
    protected SelectionKey getKey() {
        return this.key;
    }

    /**
     * {@link NioManagerThread} call -- Sets up the {@link SocketChannel} behind this connection.
     * 
     * @throws IOException
     *             when something goes awry.
     */
    protected void setup(SocketChannel channel) throws IOException {

        Control.checkTrue(this.channel == null, //
                "The channel cannot already be initialized");

        this.channel = channel;

        Socket socket = channel.socket();

        // Set some magical TCP socket options to improve responsiveness.
        socket.setTcpNoDelay(true);
        socket.setKeepAlive(true);
        socket.setSendBufferSize(this.bufferSize);
        socket.setReceiveBufferSize(this.bufferSize);

        channel.configureBlocking(false);

        this.readBuffer = ByteBuffer.allocate(this.bufferSize);

        synchronized (getLock()) {
            this.writeBuffer = Buffers.resize((ByteBuffer) this.writeBuffer.flip(), this.bufferSize);
        }
    }

    /**
     * {@link NioManagerThread} call -- Registers the underlying {@link SelectionKey}.
     * 
     * @throws IOException
     *             when something goes awry.
     */
    protected void registerKey(Selector selector, int initialOps) throws IOException {

        Control.checkTrue(this.key == null, //
                "The key cannot already be initialized");

        this.key = this.channel.register(selector, initialOps, this);
    }

    /**
     * {@link NioManagerThread} call -- Deregisters the underlying {@link SelectionKey}.
     */
    protected void deregisterKey() {

        if (this.key != null) {

            this.key.cancel();
            this.key = null;
        }
    }

    /**
     * {@link NioManagerThread} call -- Finishes connecting/accepting. Does <i>not</i> do own exception handling.
     */
    protected void doBind() {

        Object lock = getLock();

        onBind();

        // Regardless of whether or not execution reaches this point, monitor notification will happen when the
        // connection is closed.
        synchronized (lock) {

            this.stateMask |= FLAG_BOUND;
            lock.notifyAll();
        }
    }

    /**
     * {@link NioManagerThread} call -- Does a ready write. Does own exception handling.
     */
    protected void doWrite() {
        this.internalHandler.run();
    }

    /**
     * {@link NioManagerThread} call -- Does a ready read. Does own exception handling.
     */
    protected void doRead() {

        int bytesRead = 0;

        try {

            for (; this.readBuffer.hasRemaining() && (bytesRead = this.channel.read(this.readBuffer)) > 0;) {

                this.readBuffer.flip();

                try {

                    // It is the callee's responsibility to actively drain the buffer.
                    onReceive(this.readBuffer);

                } finally {

                    this.readBuffer.compact();
                }
            }

        } catch (Throwable t) {

            this.thread.handleError(this, t);

            return;
        }

        if (bytesRead == -1) {
            this.thread.handleClosingEos(this);
        }
    }

    /**
     * {@link NioManagerThread} call -- Does an operation interest change. Does <i>not</i> do own exception handling.
     */
    protected void doOp(int mask, boolean enabled) {

        if (enabled) {

            this.key.interestOps(this.key.interestOps() | mask);

            // We adhere to the intended meaning of operation interest events in the context of managed connections. For
            // example, if the user turns on read interest, then she is interested in getting called back at least once
            // -- even when the underlying socket's receive buffer bytes have been drained into our implementation's
            // receive buffer, and thus would not cause a ready read.
            switch (mask) {

            case SelectionKey.OP_READ:

                this.readBuffer.flip();

                try {

                    // It is the callee's responsibility to actively drain the buffer.
                    onReceive(this.readBuffer);

                } finally {

                    this.readBuffer.compact();
                }

                break;
            }

        } else {

            this.key.interestOps(this.key.interestOps() & ~mask);
        }
    }

    /**
     * {@link NioManagerThread} call -- Initiates connection closure. Does <i>not</i> do own exception handling.
     */
    protected void doClosing(ClosingType type) {

        // All writes shall now be buffered.
        synchronized (getLock()) {
            setBufferedHandler();
        }

        doOp(SelectionKey.OP_READ, false);
        doOp(SelectionKey.OP_WRITE, true);

        this.internalHandler = this.closingHandler;

        this.readBuffer.flip();

        try {

            onClosing(type, this.readBuffer);

        } finally {

            this.readBuffer.compact();
        }
    }

    /**
     * {@link NioManagerThread} call -- Sets the exception that occurred. Does <i>not</i> do own exception handling.
     */
    protected void doError(Throwable exception) {

        this.exception = exception;

        this.readBuffer.flip();

        try {

            onClosing(ClosingType.ERROR, this.readBuffer);

        } finally {

            this.readBuffer.compact();
        }
    }

    /**
     * {@link NioManagerThread} call -- Closes this connection. Does <i>not</i> do own exception handling.
     */
    protected void doClose() {

        Object lock = getLock();

        // Writes to the connection will now have no effect.
        synchronized (lock) {
            setNullHandler();
        }

        Control.close(this.channel);
        deregisterKey();

        try {

            onClose();

        } finally {

            synchronized (lock) {

                this.stateMask |= FLAG_CLOSED;
                lock.notifyAll();
            }
        }
    }
}

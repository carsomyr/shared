/**
 * <p>
 * Copyright (C) 2005-2010 Roy Liu<br />
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

package shared.net;

import static shared.net.Constants.DEFAULT_BUFFER_SIZE;
import static shared.net.InterestEvent.InterestEventType.ACCEPT;
import static shared.net.InterestEvent.InterestEventType.CLOSE;
import static shared.net.InterestEvent.InterestEventType.CONNECT;
import static shared.net.InterestEvent.InterestEventType.ERROR;
import static shared.net.InterestEvent.InterestEventType.EXECUTE;
import static shared.net.InterestEvent.InterestEventType.OP;
import static shared.net.InterestEvent.InterestEventType.REGISTER;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import shared.event.EnumStatus;
import shared.net.InterestEvent.InterestEventType;
import shared.util.Control;

/**
 * An abstract asynchronous sockets class internally managed by {@link ConnectionManager}. Instantiating classes must
 * implement callbacks for receipt of data, connecting, accepting, and error handling.
 * 
 * @apiviz.composedOf shared.net.AbstractManagedConnection.WriteHandler
 * @apiviz.owns shared.net.AbstractManagedConnection.AbstractManagedConnectionStatus
 * @apiviz.owns shared.net.ProxySource
 * @apiviz.uses shared.net.Buffers
 * @apiviz.uses shared.net.Constants
 * @param <C>
 *            the parameterization lower bounded by {@link AbstractManagedConnection} itself.
 * @author Roy Liu
 */
abstract public class AbstractManagedConnection<C extends AbstractManagedConnection<C>> //
        implements Connection, Closeable, SocketInformation<C>, //
        EnumStatus<AbstractManagedConnection.AbstractManagedConnectionStatus>, Executor {

    /**
     * An enumeration of connection states.
     */
    public enum AbstractManagedConnectionStatus {

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
     * A bit mask indicating that the connection has been submitted to a {@link ConnectionManager}.
     */
    final protected static int SUBMITTED_MASK = 1 << 0;

    /**
     * A bit mask indicating that the connection has been bound.
     */
    final protected static int BOUND_MASK = 1 << 1;

    /**
     * A bit mask indicating that the connection has been closed.
     */
    final protected static int CLOSED_MASK = 1 << 2;

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
    final protected WriteHandler syncHandler = new WriteHandler() {

        public int write(ByteBuffer bb) {

            AbstractManagedConnection<C> amc = AbstractManagedConnection.this;
            assert Thread.holdsLock(amc);

            amc.writeBuffer = Buffers.append(amc.writeBuffer, bb, 1);

            return amc.writeBuffer.position();
        }
    };

    /**
     * A {@link WriteHandler} used when asynchronous writes are still allowed.
     */
    final protected WriteHandler asyncHandler = new WriteHandler() {

        public int write(ByteBuffer bb) {

            AbstractManagedConnection<C> amc = AbstractManagedConnection.this;
            assert Thread.holdsLock(amc);

            assert (amc.writeBuffer.position() == 0);

            try {

                for (; bb.hasRemaining() && amc.channel.write(bb) > 0;) {
                }

                amc.writeBuffer = Buffers.append(amc.writeBuffer, bb, 1);

                int remaining = amc.writeBuffer.position();

                if (remaining > 0) {

                    getThread().debug("Writes deferred [%s].", amc);

                    setSyncHandler();
                    setWriteEnabled(true);
                }

                return remaining;

            } catch (Throwable t) {

                setNullHandler();
                error(t);

                return 0;
            }
        }
    };

    /**
     * A {@link WriteHandler} that forgets all data offered to it.
     */
    final protected WriteHandler nullHandler = new WriteHandler() {

        public int write(ByteBuffer bb) {

            AbstractManagedConnection<C> amc = AbstractManagedConnection.this;
            assert Thread.holdsLock(amc);

            // Set the position to the limit to simulate that we have read all bytes.
            bb.position(bb.limit());

            return 0;
        }
    };

    /**
     * An internal handler for deferred writes.
     */
    final protected Runnable deferredHandler = new Runnable() {

        public void run() {

            AbstractManagedConnection<C> amc = AbstractManagedConnection.this;
            assert !Thread.holdsLock(amc);

            try {

                final boolean disableWrites;

                synchronized (amc) {

                    for (amc.writeBuffer.flip(); amc.writeBuffer.hasRemaining() //
                            && amc.channel.write(amc.writeBuffer) > 0;) {
                    }

                    amc.writeBuffer.compact();

                    disableWrites = (amc.writeBuffer.position() == 0);

                    if (disableWrites) {

                        if (amc.writeBuffer.capacity() > amc.bufferSize) {
                            amc.writeBuffer = ByteBuffer.allocate(amc.bufferSize);
                        }

                        getThread().debug("Canceled write defer [%s].", amc);

                        // Notify anyone waiting for restoration of the asynchronous write
                        // handler.
                        setAsyncHandler();
                        amc.notifyAll();
                    }
                }

                // No longer under the protection of the monitor, perform the operation interest change.
                if (disableWrites) {
                    doOp(SelectionKey.OP_WRITE, false);
                }

            } catch (Throwable t) {

                amc.thread.handleError(amc, t);
            }
        }
    };

    /**
     * An internal handler for connection closure.
     */
    final protected Runnable closingHandler = new Runnable() {

        public void run() {

            AbstractManagedConnection<C> amc = AbstractManagedConnection.this;
            assert !Thread.holdsLock(amc);

            final boolean closeConnection;

            try {

                synchronized (amc) {

                    for (amc.writeBuffer.flip(); amc.writeBuffer.hasRemaining() //
                            && amc.channel.write(amc.writeBuffer) > 0;) {
                    }

                    amc.writeBuffer.compact();

                    closeConnection = (amc.writeBuffer.position() == 0);
                }

            } catch (Throwable t) {

                amc.thread.handleError(amc, t);

                return;
            }

            // No longer under the protection of the monitor, close the connection.
            if (closeConnection) {
                amc.thread.handleClose(amc);
            }
        }
    };

    /**
     * Sets the {@link #onClosingEOS(ByteBuffer)} handler.
     */
    final protected Runnable onClosingEOSHandler = new Runnable() {

        public void run() {
            onClosingEOS(AbstractManagedConnection.this.readBuffer);
        }
    };

    /**
     * Sets the {@link #onClosingUser(ByteBuffer)} handler.
     */
    final protected Runnable onClosingUserHandler = new Runnable() {

        public void run() {
            onClosingUser(AbstractManagedConnection.this.readBuffer);
        }
    };

    public Future<InetSocketAddress> connect(InetSocketAddress address) {
        return submit(CONNECT, address);
    }

    public Future<InetSocketAddress> accept(InetSocketAddress address) {
        return submit(ACCEPT, address);
    }

    public Future<?> register(SocketChannel channel) {
        return submit(REGISTER, channel);
    }

    public void setReadEnabled(boolean enabled) {

        synchronized (this) {
            this.proxy.onLocal(createOpEvent(SelectionKey.OP_READ, enabled));
        }
    }

    public void setWriteEnabled(boolean enabled) {

        synchronized (this) {
            this.proxy.onLocal(createOpEvent(SelectionKey.OP_WRITE, enabled));
        }
    }

    public void execute(Runnable r) {

        synchronized (this) {
            this.proxy.onLocal(new InterestEvent<Runnable>(EXECUTE, r, this.proxy));
        }
    }

    public void close() {

        synchronized (this) {
            this.proxy.onLocal(new InterestEvent<Object>(CLOSE, this.proxy));
        }
    }

    public void error(Throwable error) {

        synchronized (this) {
            this.proxy.onLocal(new InterestEvent<Throwable>(ERROR, error, this.proxy));
        }
    }

    public int send(ByteBuffer bb) {

        // All send operations are performed under the protection of the connection monitor.
        synchronized (this) {
            return this.writeHandler.write(bb);
        }
    }

    public InetSocketAddress getLocalAddress() {

        synchronized (this) {
            return (this.channel != null) ? (InetSocketAddress) this.channel.socket().getLocalSocketAddress() : null;
        }
    }

    public InetSocketAddress getRemoteAddress() {

        synchronized (this) {
            return (this.channel != null) ? (InetSocketAddress) this.channel.socket().getRemoteSocketAddress() : null;
        }
    }

    public int getBufferSize() {
        return this.bufferSize;
    }

    @SuppressWarnings("unchecked")
    public C setBufferSize(int bufferSize) {

        this.bufferSize = bufferSize;

        return (C) this;
    }

    public Throwable getError() {
        return this.error;
    }

    public boolean isSubmitted() {
        return (this.stateMask & SUBMITTED_MASK) != 0;
    }

    public boolean isBound() {
        return (this.stateMask & BOUND_MASK) != 0;
    }

    public boolean isClosed() {
        return (this.stateMask & CLOSED_MASK) != 0;
    }

    /**
     * Creates a human-readable representation of this connection that includes the name and status.
     */
    @Override
    public String toString() {
        return String.format("%s[%s]", this.name, this.status);
    }

    public AbstractManagedConnectionStatus getStatus() {
        return this.status;
    }

    public void setStatus(AbstractManagedConnectionStatus status) {
        this.status = status;
    }

    final ConnectionManager manager;
    final String name;
    final ProxySource<C> proxy;

    ConnectionManagerThread thread;
    WriteHandler writeHandler;
    Runnable internalHandler;
    Runnable closeHandler;
    SelectionKey key;
    SocketChannel channel;
    int bufferSize;
    ByteBuffer readBuffer, writeBuffer;
    int stateMask;
    Throwable error;
    AbstractManagedConnectionStatus status;

    /**
     * Default constructor.
     * 
     * @param name
     *            the name of this connection.
     * @param manager
     *            the {@link ConnectionManager} with which this connection will be registered.
     */
    @SuppressWarnings("unchecked")
    protected AbstractManagedConnection(String name, ConnectionManager manager) {

        this.manager = manager;
        this.name = name;

        this.proxy = new ProxySource<C>((C) this);

        //

        this.thread = manager.getThread();

        // The connection starts with writes deferred to the manager.
        this.writeHandler = this.syncHandler;
        this.internalHandler = this.deferredHandler;
        this.closeHandler = null;

        this.key = null;
        this.channel = null;

        this.bufferSize = DEFAULT_BUFFER_SIZE;

        this.readBuffer = ByteBuffer.allocate(0);

        synchronized (this) {
            this.writeBuffer = ByteBuffer.allocate(0);
        }

        this.stateMask = 0;
        this.error = null;
        this.status = AbstractManagedConnectionStatus.VIRGIN;
    }

    /**
     * Alternate constructor.
     * 
     * @see #AbstractManagedConnection(String, ConnectionManager)
     */
    protected AbstractManagedConnection(String name) {
        this(name, ConnectionManager.getInstance());
    }

    /**
     * Gets the {@link ProxySource} representing this connection.
     */
    protected ProxySource<C> getProxy() {
        return this.proxy;
    }

    /**
     * Sets the state bit mask.
     */
    protected void setStateMask(int stateMask) {

        synchronized (this) {

            this.stateMask = stateMask;
            notifyAll();
        }
    }

    /**
     * Sets {@link #nullHandler}.
     */
    protected void setNullHandler() {
        this.writeHandler = this.nullHandler;
    }

    /**
     * Sets {@link #syncHandler}.
     */
    protected void setSyncHandler() {
        this.writeHandler = this.syncHandler;
    }

    /**
     * Sets {@link #asyncHandler} -- Any thread can write to this connection.
     */
    protected void setAsyncHandler() {
        this.writeHandler = this.asyncHandler;
    }

    /**
     * Gets the {@link ConnectionManagerThread} currently servicing this connection.
     */
    protected ConnectionManagerThread getThread() {
        return this.thread;
    }

    /**
     * Sets the {@link ConnectionManagerThread} currently servicing this connection.
     */
    protected void setThread(ConnectionManagerThread thread) {
        this.thread = thread;
    }

    /**
     * Creates an operation interest change {@link InterestEvent} that originates from this connection.
     */
    protected InterestEvent<Integer> createOpEvent(int mask, boolean enabled) {
        return new InterestEvent<Integer>(OP, mask | (enabled ? 0x80000000 : 0x0), this.proxy);
    }

    /**
     * Gets the {@link SelectionKey}.
     */
    protected SelectionKey getKey() {
        return this.key;
    }

    /**
     * {@link ConnectionManagerThread} call -- Sets up the {@link SocketChannel} behind this connection.
     * 
     * @throws IOException
     *             when something goes awry.
     */
    protected void setup(SocketChannel channel) throws IOException {

        Control.checkTrue(this.channel == null, //
                "The channel cannot already be initialized");

        this.channel = channel;

        int socketBufferSize = Math.max(this.bufferSize, DEFAULT_BUFFER_SIZE);

        Socket socket = channel.socket();

        // Set some magical TCP options to improve responsiveness.
        socket.setTcpNoDelay(true);
        socket.setKeepAlive(true);
        socket.setSendBufferSize(socketBufferSize);
        socket.setReceiveBufferSize(socketBufferSize);

        channel.configureBlocking(false);

        this.readBuffer = ByteBuffer.allocate(this.bufferSize);

        synchronized (this) {
            this.writeBuffer = Buffers.resize((ByteBuffer) this.writeBuffer.flip(), this.bufferSize);
        }
    }

    /**
     * {@link ConnectionManagerThread} call -- Registers the underlying {@link SelectionKey}.
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
     * {@link ConnectionManagerThread} call -- Deregisters the underlying {@link SelectionKey}.
     */
    protected void deregisterKey() {

        if (this.key != null) {

            this.key.cancel();
            this.key = null;
        }
    }

    /**
     * External thread call -- Submits this connection to its associated {@link ConnectionManager}.
     * 
     * @param <T>
     *            the argument type.
     * @param opType
     *            the {@link InterestEventType}.
     * @param argument
     *            the argument.
     */
    protected <T> Future<InetSocketAddress> submit(final InterestEventType opType, T argument) {

        synchronized (this) {

            Control.checkTrue(!isSubmitted(), //
                    "The connection has already been submitted");

            setStateMask(this.stateMask | SUBMITTED_MASK);
        }

        this.manager.initConnection(this, opType, argument);

        return new Future<InetSocketAddress>() {

            public InetSocketAddress get() throws InterruptedException, ExecutionException {

                AbstractManagedConnection<?> amc = AbstractManagedConnection.this;

                synchronized (amc) {

                    for (; !isDone();) {
                        amc.wait();
                    }
                }

                if (amc.error != null) {
                    throw new ExecutionException(amc.error);
                }

                switch (opType) {

                case ACCEPT:
                    return getRemoteAddress();

                case CONNECT:
                    return getLocalAddress();

                case REGISTER:
                    return null;

                default:
                    throw new AssertionError("Control should never reach here");
                }
            }

            public InetSocketAddress get(long timeout, TimeUnit unit) //
                    throws InterruptedException, ExecutionException, TimeoutException {

                AbstractManagedConnection<?> amc = AbstractManagedConnection.this;

                long timeoutMillis = unit.toMillis(timeout);

                synchronized (amc) {

                    for (long remaining = timeoutMillis, end = System.currentTimeMillis() + timeoutMillis; //
                    remaining > 0 && !isDone(); //
                    remaining = end - System.currentTimeMillis()) {
                        amc.wait(remaining);
                    }

                    if (!isDone()) {
                        throw new TimeoutException();
                    }
                }

                if (amc.error != null) {
                    throw new ExecutionException(amc.error);
                }

                switch (opType) {

                case ACCEPT:
                    return getRemoteAddress();

                case CONNECT:
                    return getLocalAddress();

                case REGISTER:
                    return null;

                default:
                    throw new AssertionError("Control should never reach here");
                }
            }

            public boolean isDone() {
                return isBound() || isClosed();
            }

            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }

            public boolean isCancelled() {
                return false;
            }
        };
    }

    /**
     * {@link ConnectionManagerThread} call -- Finishes connecting/accepting. Does <i>not</i> do own exception handling.
     */
    protected void doBind() {

        assert !Thread.holdsLock(this);

        onBind();

        setStateMask(this.stateMask | BOUND_MASK);
    }

    /**
     * {@link ConnectionManagerThread} call -- Reads exclusively from this connection's receive buffer. Does <i>not</i>
     * do own exception handling. <br />
     * <br />
     * IMPORTANT NOTE: This method complies with the behavior specified in
     * {@link ConnectionManagerThread#handleOp(AbstractManagedConnection, int, boolean)}.
     * 
     * @see ConnectionManagerThread#handleOp(AbstractManagedConnection, int, boolean)
     */
    protected void doReadBuffer() {

        assert !Thread.holdsLock(this);

        // It is the callee's responsibility to actively drain the buffer.
        this.readBuffer.flip();
        onReceive(this.readBuffer);
        this.readBuffer.compact();
    }

    /**
     * {@link ConnectionManagerThread} call -- Does a synchronous write. Does own exception handling.
     */
    protected void doWrite() {

        assert !Thread.holdsLock(this);

        this.internalHandler.run();
    }

    /**
     * {@link ConnectionManagerThread} call -- Does a ready read. Does own exception handling.
     */
    protected void doRead() {

        assert !Thread.holdsLock(this);

        int bytesRead = 0;

        try {

            for (; this.readBuffer.hasRemaining() //
                    && (bytesRead = this.channel.read(this.readBuffer)) > 0;) {
                doReadBuffer();
            }

        } catch (Throwable t) {

            this.thread.handleError(this, t);

            return;
        }

        if (bytesRead == -1) {
            this.thread.handleClosingEOS(this);
        }
    }

    /**
     * {@link ConnectionManagerThread} call -- Does an operation interest change. Does <i>not</i> do own exception
     * handling.
     */
    protected void doOp(int mask, boolean enabled) {

        if (enabled) {

            this.key.interestOps(this.key.interestOps() | mask);

            // We adhere to the intended meaning of operation interest events in the context of managed
            // connections. For example, if the user turns on read interest, then she is interested in
            // getting called back at least once -- even when the underlying socket's receive buffer bytes
            // have been drained into our implementation's receive buffer, and thus would not cause a ready
            // read.
            switch (mask) {

            case SelectionKey.OP_READ:
                doReadBuffer();
                break;
            }

        } else {

            this.key.interestOps(this.key.interestOps() & ~mask);
        }
    }

    /**
     * {@link ConnectionManagerThread} call -- Initiates connection closure. Does <i>not</i> do own exception handling.
     */
    protected void doClosing(Runnable closingHandler) {

        // All writes shall now be synchronous.
        synchronized (this) {
            setSyncHandler();
        }

        doOp(SelectionKey.OP_READ, false);
        doOp(SelectionKey.OP_WRITE, true);

        this.internalHandler = this.closingHandler;

        this.readBuffer.flip();
        closingHandler.run();
        this.readBuffer.compact();
    }

    /**
     * {@link ConnectionManagerThread} call -- Sets the error that occurred. Does <i>not</i> do own exception handling.
     */
    protected void doError(Throwable error) {

        this.error = error;

        this.readBuffer.flip();
        onError(this.error, this.readBuffer);
        this.readBuffer.compact();
    }

    /**
     * {@link ConnectionManagerThread} call -- Closes this connection. Does <i>not</i> do own exception handling.
     */
    protected void doClose() {

        // Writes to the connection will now have no effect.
        synchronized (this) {
            setNullHandler();
        }

        // SocketChannel operations are thread-safe.
        Control.close(this.channel);
        deregisterKey();

        try {

            onClose();

        } finally {

            setStateMask(this.stateMask | CLOSED_MASK);
        }
    }
}

/**
 * <p>
 * Copyright (c) 2008 Roy Liu<br>
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

import static shared.net.Constants.DEFAULT_BUFFER_SIZE;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import shared.net.Buffers;
import shared.net.Connection;
import shared.net.Connection.OperationType;
import shared.net.filter.IdentityFilterFactory;
import shared.util.Control;

/**
 * A subclass of {@link AbstractFilteredHandler} with synchronous behavior.
 * 
 * @apiviz.composedOf shared.net.handler.SynchronousHandler.ManagedInputStream
 * @apiviz.composedOf shared.net.handler.SynchronousHandler.ManagedOutputStream
 * @apiviz.composedOf shared.net.handler.SynchronousHandler.Resolver
 * @apiviz.uses shared.net.Buffers
 * @param <C>
 *            the {@link Connection} type.
 * @author Roy Liu
 */
public class SynchronousHandler<C extends Connection> //
        extends AbstractFilteredHandler<SynchronousHandler<C>, C, ByteBuffer> {

    /**
     * Defines a gadget that supports {@link SynchronousHandler.ManagedInputStream} and
     * {@link SynchronousHandler.ManagedOutputStream}.
     */
    protected interface Resolver {

        /**
         * Resolves a read/write.
         * 
         * @param size
         *            the number of {@code byte}s read or remaining to be written.
         * @return the return code.
         * @throws IOException
         *             when something goes awry.
         */
        public int resolve(int size) throws IOException;
    }

    /**
     * Sets a read {@link Resolver} for when read operation interest is enabled.
     */
    protected void setInResolverEnabled() {

        if ((this.stateMask & FLAG_CLOSED) != 0) {
            return;
        }

        this.inResolver = new Resolver() {

            @Override
            public int resolve(int size) {

                if (size > 0) {

                    return size;

                } else {

                    throw new IllegalArgumentException("Cannot resolve a nonpositive size");
                }
            }
        };
    }

    /**
     * Sets a read {@link Resolver} for when read operation interest is disabled.
     */
    protected void setInResolverDisabled() {

        if ((this.stateMask & FLAG_CLOSED) != 0) {
            return;
        }

        this.inResolver = new Resolver() {

            @Override
            public int resolve(int size) {

                SynchronousHandler<C> handler = SynchronousHandler.this;
                ByteBuffer bb = handler.in.buffer;

                // If running low, request more!
                if (bb.remaining() <= bb.capacity() >>> 1) {

                    debug("[%s] reads enabled.", handler);

                    handler.setInResolverEnabled();
                    handler.getConnection().setEnabled(OperationType.READ, true);
                }

                if (size > 0) {

                    return size;

                } else {

                    throw new IllegalArgumentException("Cannot resolve a nonpositive size");
                }
            }
        };
    }

    /**
     * Sets a read {@link Resolver} for when an end-of-stream has been reached.
     */
    protected void setInResolverEos() {

        this.inResolver = new Resolver() {

            @Override
            public int resolve(int size) {
                return (size > 0) ? size : -1;
            }
        };
    }

    /**
     * Sets an error read {@link Resolver}.
     */
    protected void setInResolverError(final IOException e) {

        this.inResolver = new Resolver() {

            @Override
            public int resolve(int size) throws IOException {

                if (size > 0) {

                    return size;

                } else {

                    throw e;
                }
            }
        };
    }

    /**
     * Sets the default write {@link Resolver}.
     */
    protected void setOutResolverDefault() {

        this.outResolver = new Resolver() {

            @Override
            public int resolve(int remaining) {

                SynchronousHandler<C> handler = SynchronousHandler.this;
                return handler.send(handler.out.buffer) + handler.out.buffer.remaining();
            }
        };
    }

    /**
     * Sets an error write {@link Resolver}.
     */
    protected void setOutResolverError(final IOException e) {

        this.outResolver = new Resolver() {

            @Override
            public int resolve(int remaining) throws IOException {

                SynchronousHandler<C> handler = SynchronousHandler.this;

                // If there's nothing left to write, return normally.
                if (remaining + handler.out.buffer.remaining() == 0) {

                    return 0;

                }
                // Otherwise, throw an exception.
                else {

                    throw e;
                }
            }
        };
    }

    /**
     * A bit flag indicating that the associated connection has been bound.
     */
    final protected static int FLAG_BOUND = 1 << 0;

    /**
     * A bit flag indicating that the associated connection has been closed.
     */
    final protected static int FLAG_CLOSED = 1 << 1;

    /**
     * The static {@link Logger} instance.
     */
    final protected static Logger log = LoggerFactory.getLogger(SynchronousHandler.class);

    final int bufferSize;

    Resolver inResolver;
    Resolver outResolver;

    ManagedInputStream in;
    ManagedOutputStream out;

    int stateMask;

    /**
     * Default constructor.
     */
    public SynchronousHandler(String name, int bufferSize) {
        super(name);

        this.bufferSize = bufferSize;

        this.inResolver = null;
        this.outResolver = null;

        this.in = null;
        this.out = null;

        this.stateMask = 0;

        IdentityFilterFactory<ByteBuffer> iff = IdentityFilterFactory.getInstance();
        setFilterFactory(iff);
    }

    /**
     * Alternate constructor.
     */
    public SynchronousHandler(String name) {
        this(name, DEFAULT_BUFFER_SIZE);
    }

    /**
     * Gets the {@link InputStream}.
     */
    public InputStream getInputStream() {

        synchronized (getConnection().getLock()) {

            Control.checkTrue((this.stateMask & FLAG_BOUND) != 0, //
                    "Connection must be bound");

            return this.in;
        }
    }

    /**
     * Gets the {@link OutputStream}.
     */
    public OutputStream getOutputStream() {

        synchronized (getConnection().getLock()) {

            Control.checkTrue((this.stateMask & FLAG_BOUND) != 0, //
                    "Connection must be bound");

            return this.out;
        }
    }

    @Override
    public void onBind(Queue<ByteBuffer> inputs) {

        synchronized (getConnection().getLock()) {

            // Set the initial read/write resolvers.
            setInResolverEnabled();
            setOutResolverDefault();

            // Initialize input/output streams.
            this.in = new ManagedInputStream();
            this.out = new ManagedOutputStream();

            this.stateMask |= FLAG_BOUND;
        }
    }

    @Override
    public void onReceive(Queue<ByteBuffer> inputs) {

        boolean disableReads = false;

        Connection conn = getConnection();
        Object lock = conn.getLock();

        synchronized (lock) {

            lock.notifyAll();

            for (ByteBuffer bb = null; (bb = inputs.peek()) != null && !disableReads;) {

                ByteBuffer receiveBb = this.in.buffer;

                receiveBb.compact();

                int size = Math.min(bb.remaining(), receiveBb.remaining());
                int save = bb.position();

                receiveBb.put(bb.array(), save, size).flip();
                bb.position(save + size);

                disableReads = (receiveBb.remaining() == receiveBb.capacity()) //
                        && bb.hasRemaining();

                if (!bb.hasRemaining()) {
                    inputs.remove();
                }
            }

            // Disable reads if the incoming buffer is full.
            if (disableReads) {

                debug("[%s] reads disabled.", this);

                setInResolverDisabled();
                conn.setEnabled(OperationType.READ, false);
            }
        }
    }

    @Override
    public void onClosing(ClosingType type, Queue<ByteBuffer> inputs) {

        Connection conn = getConnection();

        final IOException e;

        synchronized (conn.getLock()) {

            switch (type) {

            case EOS:

                e = new IOException("Connection closed");

                setInResolverEos();
                setOutResolverError(e);

                // Append the remainders onto the incoming buffer.
                for (ByteBuffer bb; (bb = inputs.poll()) != null;) {
                    this.in.buffer = (ByteBuffer) Buffers.append(this.in.buffer.compact(), bb).flip();
                }

                break;

            case USER:

                e = new IOException("Connection closed");

                setInResolverError(e);
                setOutResolverError(e);

                // Append the remainders onto the incoming buffer.
                for (ByteBuffer bb; (bb = inputs.poll()) != null;) {
                    this.in.buffer = (ByteBuffer) Buffers.append(this.in.buffer.compact(), bb).flip();
                }

                break;

            case ERROR:

                e = (IOException) new IOException("Connection encountered an error").initCause(conn.getException());

                setInResolverError(e);
                setOutResolverError(e);

                // Do NOT append the remainder onto the incoming buffer, since the very cause of the error could be a
                // buffer discontinuity.

                break;

            default:
                throw new IllegalArgumentException("Invalid closing type");
            }
        }
    }

    @Override
    public void onClose() {

        Object lock = getConnection().getLock();

        synchronized (lock) {

            this.stateMask |= FLAG_CLOSED;
            lock.notifyAll();
        }
    }

    /**
     * Waits interruptibly on this connection's monitor.
     * 
     * @exception IOException
     *                when the current operation is interrupted; wraps an {@link InterruptedException}.
     */
    protected void waitInterruptibly() throws IOException {

        try {

            getConnection().getLock().wait();

        } catch (InterruptedException e) {

            throw (IOException) new IOException("Operation was interrupted").initCause(e);
        }
    }

    /**
     * Logs a debugging message.
     */
    final protected static void debug(String format, Object... args) {

        if (log.isDebugEnabled()) {
            log.debug(String.format(format, args));
        }
    }

    /**
     * A subclass of {@link InputStream} that is managed underneath.
     */
    protected class ManagedInputStream extends InputStream {

        ByteBuffer buffer;

        /**
         * Default constructor.
         */
        protected ManagedInputStream() {

            // Invariant: The incoming buffer is always in the ready-to-read configuration.
            this.buffer = (ByteBuffer) ByteBuffer.allocate(SynchronousHandler.this.bufferSize).flip();
        }

        /**
         * Reads a {@code byte}.
         */
        @Override
        public int read() throws IOException {

            SynchronousHandler<C> handler = SynchronousHandler.this;

            synchronized (handler.getConnection().getLock()) {

                for (; (handler.stateMask & FLAG_CLOSED) == 0 && !this.buffer.hasRemaining();) {
                    handler.waitInterruptibly();
                }

                if (this.buffer.hasRemaining()) {

                    int res = this.buffer.get() & 0x000000FF;

                    handler.inResolver.resolve(1);

                    return res;

                } else {

                    return handler.inResolver.resolve(0);
                }
            }
        }

        /**
         * Reads {@code byte}s.
         */
        @Override
        public int read(byte[] dst, int offset, int length) throws IOException {

            SynchronousHandler<C> handler = SynchronousHandler.this;

            if (dst == null) {
                throw new NullPointerException("Null destination array");
            }

            if (length < 0 || dst.length < offset + length) {
                throw new IndexOutOfBoundsException("Invalid offset/length");
            }

            if (length == 0) {
                return 0;
            }

            synchronized (handler.getConnection().getLock()) {

                for (; (handler.stateMask & FLAG_CLOSED) == 0 && !this.buffer.hasRemaining();) {
                    handler.waitInterruptibly();
                }

                int size = Math.min(this.buffer.remaining(), length);
                this.buffer.get(dst, offset, size);

                return handler.inResolver.resolve(size);
            }
        }

        /**
         * Gets the number of readable bytes before blocking.
         */
        @Override
        public int available() {

            SynchronousHandler<C> handler = SynchronousHandler.this;

            synchronized (handler.getConnection().getLock()) {
                return this.buffer.remaining();
            }
        }

        /**
         * Closes this stream.
         */
        @Override
        public void close() {
            Control.close(SynchronousHandler.this);
        }
    }

    /**
     * A subclass of {@link OutputStream} that is managed underneath.
     */
    protected class ManagedOutputStream extends OutputStream {

        final ByteBuffer buffer;

        /**
         * Default constructor.
         */
        protected ManagedOutputStream() {

            // Invariant: The outgoing buffer is always in the ready-to-read configuration.
            this.buffer = (ByteBuffer) ByteBuffer.allocate(SynchronousHandler.this.bufferSize).flip();
        }

        /**
         * Writes a {@code byte}.
         */
        @Override
        public void write(int b) throws IOException {

            SynchronousHandler<C> handler = SynchronousHandler.this;

            synchronized (handler.getConnection().getLock()) {

                for (int size, length = 1; length > 0; length -= size) {

                    this.buffer.compact();
                    size = Math.min(this.buffer.remaining(), length);
                    ((size > 0) ? this.buffer.put((byte) b) : this.buffer).flip();

                    for (; handler.outResolver.resolve(length - size) > 0;) {
                        handler.waitInterruptibly();
                    }
                }
            }
        }

        /**
         * Writes {@code byte}s.
         */
        @Override
        public void write(byte[] src, int offset, int length) throws IOException {

            SynchronousHandler<C> handler = SynchronousHandler.this;

            if (src == null) {
                throw new NullPointerException("Null source array");
            }

            if (length < 0 || src.length < offset + length) {
                throw new IndexOutOfBoundsException("Invalid offset/length");
            }

            synchronized (handler.getConnection().getLock()) {

                for (int size; length > 0; offset += size, length -= size) {

                    this.buffer.compact();
                    size = Math.min(this.buffer.remaining(), length);
                    this.buffer.put(src, offset, size).flip();

                    for (; handler.outResolver.resolve(length - size) > 0;) {
                        handler.waitInterruptibly();
                    }
                }
            }
        }

        /**
         * Closes this stream.
         */
        @Override
        public void close() {
            Control.close(SynchronousHandler.this);
        }
    }
}

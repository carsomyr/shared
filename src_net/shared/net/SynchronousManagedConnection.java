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

package shared.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Queue;

import shared.net.filter.FilteredManagedConnection;
import shared.net.filter.IdentityFilterFactory;
import shared.util.Control;

/**
 * A subclass of {@link AbstractManagedConnection} with synchronous behavior.
 * 
 * @apiviz.composedOf shared.net.SynchronousManagedConnection.ManagedInputStream
 * @apiviz.composedOf shared.net.SynchronousManagedConnection.ManagedOutputStream
 * @apiviz.composedOf shared.net.SynchronousManagedConnection.Resolver
 * @apiviz.uses shared.net.Buffers
 * @author Roy Liu
 */
public class SynchronousManagedConnection extends FilteredManagedConnection<SynchronousManagedConnection, ByteBuffer> {

    /**
     * Defines a gadget that supports {@link ManagedInputStream} and {@link ManagedOutputStream}.
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

        if (isClosed()) {
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

        if (isClosed()) {
            return;
        }

        this.inResolver = new Resolver() {

            @Override
            public int resolve(int size) {

                SynchronousManagedConnection smc = SynchronousManagedConnection.this;
                ByteBuffer bb = smc.in.buffer;

                // If running low, request more!
                if (bb.remaining() <= bb.capacity() >>> 1) {

                    smc.getThread().debug("Reads enabled [%s].", smc);

                    smc.setInResolverEnabled();
                    smc.setEnabled(OperationType.READ, true);
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
    protected void setInResolverEOS() {

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

                SynchronousManagedConnection smc = SynchronousManagedConnection.this;
                return smc.sendOutbound(smc.out.buffer) + smc.out.buffer.remaining();
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

                SynchronousManagedConnection smc = SynchronousManagedConnection.this;

                // If there's nothing left to write, return normally.
                if (remaining + smc.out.buffer.remaining() == 0) {

                    return 0;

                }
                // Otherwise, throw an exception.
                else {

                    throw e;
                }
            }
        };
    }

    Resolver inResolver;
    Resolver outResolver;

    ManagedInputStream in;
    ManagedOutputStream out;

    /**
     * Default constructor.
     */
    public SynchronousManagedConnection(String name, ConnectionManager manager) {
        super(name, manager);

        this.inResolver = null;
        this.outResolver = null;

        this.in = null;
        this.out = null;

        IdentityFilterFactory<ByteBuffer, SynchronousManagedConnection> iff = IdentityFilterFactory.getInstance();
        setFilterFactory(iff);
    }

    /**
     * Alternate constructor.
     */
    public SynchronousManagedConnection(String name) {
        this(name, ConnectionManager.getInstance());
    }

    /**
     * Gets the {@link InputStream}.
     */
    public InputStream getInputStream() {

        synchronized (this) {

            Control.checkTrue(isBound(), //
                    "Connection must be bound");

            return this.in;
        }
    }

    /**
     * Gets the {@link OutputStream}.
     */
    public OutputStream getOutputStream() {

        synchronized (this) {

            Control.checkTrue(isBound(), //
                    "Connection must be bound");

            return this.out;
        }
    }

    @Override
    public void onBind(Queue<ByteBuffer> inputs) {

        synchronized (this) {

            // Set the initial read/write resolvers.
            setInResolverEnabled();
            setOutResolverDefault();

            // Initialize input/output streams.
            this.in = new ManagedInputStream();
            this.out = new ManagedOutputStream();
        }
    }

    @Override
    public void onReceive(Queue<ByteBuffer> inputs) {

        boolean disableReads = false;

        synchronized (this) {

            notifyAll();

            for (ByteBuffer bb = null; (bb = inputs.peek()) != null && !disableReads;) {

                ByteBuffer receiveBB = this.in.buffer;

                receiveBB.compact();

                int size = Math.min(bb.remaining(), receiveBB.remaining());
                int save = bb.position();

                receiveBB.put(bb.array(), save, size).flip();
                bb.position(save + size);

                disableReads = (receiveBB.remaining() == receiveBB.capacity()) //
                        && bb.hasRemaining();

                if (!bb.hasRemaining()) {
                    inputs.remove();
                }
            }

            // Disable reads if the incoming buffer is full.
            if (disableReads) {

                getThread().debug("Reads disabled [%s].", this);

                setInResolverDisabled();
                setEnabled(OperationType.READ, false);
            }
        }
    }

    @Override
    public void onClosing(ClosingType type, Queue<ByteBuffer> inputs) {

        final IOException e;

        synchronized (this) {

            switch (type) {

            case EOS:

                e = new IOException("Connection closed");

                setInResolverEOS();
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

                e = (IOException) new IOException("Connection encountered an error").initCause(getException());

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

    /**
     * Waits interruptibly on this connection's monitor.
     * 
     * @exception IOException
     *                when the current operation is interrupted; wraps an {@link InterruptedException}.
     */
    protected void waitInterruptibly() throws IOException {

        try {

            wait();

        } catch (InterruptedException e) {

            throw (IOException) new IOException("Operation was interrupted").initCause(e);
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
            this.buffer = (ByteBuffer) ByteBuffer.allocate(getBufferSize()).flip();
        }

        /**
         * Reads a {@code byte}.
         */
        @Override
        public int read() throws IOException {

            SynchronousManagedConnection smc = SynchronousManagedConnection.this;

            synchronized (smc) {

                for (; !smc.isClosed() && !this.buffer.hasRemaining();) {
                    smc.waitInterruptibly();
                }

                if (this.buffer.hasRemaining()) {

                    int res = this.buffer.get() & 0x000000FF;

                    smc.inResolver.resolve(1);

                    return res;

                } else {

                    return smc.inResolver.resolve(0);
                }
            }
        }

        /**
         * Reads {@code byte}s.
         */
        @Override
        public int read(byte[] dst, int offset, int length) throws IOException {

            SynchronousManagedConnection smc = SynchronousManagedConnection.this;

            if (dst == null) {
                throw new NullPointerException("Null destination array");
            }

            if (length < 0 || dst.length < offset + length) {
                throw new IndexOutOfBoundsException("Invalid offset/length");
            }

            if (length == 0) {
                return 0;
            }

            synchronized (smc) {

                for (; !smc.isClosed() && !this.buffer.hasRemaining();) {
                    smc.waitInterruptibly();
                }

                int size = Math.min(this.buffer.remaining(), length);
                this.buffer.get(dst, offset, size);

                return smc.inResolver.resolve(size);
            }
        }

        /**
         * Gets the number of readable bytes before blocking.
         */
        @Override
        public int available() {

            SynchronousManagedConnection smc = SynchronousManagedConnection.this;

            synchronized (smc) {
                return this.buffer.remaining();
            }
        }

        /**
         * Closes this stream.
         */
        @Override
        public void close() {
            Control.close(SynchronousManagedConnection.this);
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
            this.buffer = (ByteBuffer) ByteBuffer.allocate(getBufferSize()).flip();
        }

        /**
         * Writes a {@code byte}.
         */
        @Override
        public void write(int b) throws IOException {

            SynchronousManagedConnection smc = SynchronousManagedConnection.this;

            synchronized (smc) {

                for (int size, length = 1; length > 0; length -= size) {

                    this.buffer.compact();
                    size = Math.min(this.buffer.remaining(), length);
                    ((size > 0) ? this.buffer.put((byte) b) : this.buffer).flip();

                    for (; smc.outResolver.resolve(length - size) > 0;) {
                        smc.waitInterruptibly();
                    }
                }
            }
        }

        /**
         * Writes {@code byte}s.
         */
        @Override
        public void write(byte[] src, int offset, int length) throws IOException {

            SynchronousManagedConnection smc = SynchronousManagedConnection.this;

            if (src == null) {
                throw new NullPointerException("Null source array");
            }

            if (length < 0 || src.length < offset + length) {
                throw new IndexOutOfBoundsException("Invalid offset/length");
            }

            synchronized (smc) {

                for (int size; length > 0; offset += size, length -= size) {

                    this.buffer.compact();
                    size = Math.min(this.buffer.remaining(), length);
                    this.buffer.put(src, offset, size).flip();

                    for (; smc.outResolver.resolve(length - size) > 0;) {
                        smc.waitInterruptibly();
                    }
                }
            }
        }

        /**
         * Closes this stream.
         */
        @Override
        public void close() {
            Control.close(SynchronousManagedConnection.this);
        }
    }
}

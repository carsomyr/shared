/**
 * <p>
 * Copyright (C) 2008 Roy Liu<br />
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

import java.io.Closeable;
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
     * Defines a managed input/output stream.
     */
    public interface ManagedStream extends Closeable {

        /**
         * Gets the containing instance.
         */
        public SynchronousManagedConnection getConnection();

        /**
         * Closes this stream.
         */
        public void close();
    }

    /**
     * Defines a gadget that supports {@link ManagedInputStream}.
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
    protected void setEnabledReader() {

        assert Thread.holdsLock(this);

        if (isClosed()) {
            return;
        }

        this.inReader = new Resolver() {

            public int resolve(int size) throws IOException {

                SynchronousManagedConnection smc = SynchronousManagedConnection.this;
                assert Thread.holdsLock(smc);

                if (size > 0) {

                    return size;

                } else {

                    throw new IOException("Control should never reach here");
                }
            }
        };
    }

    /**
     * Sets a read {@link Resolver} for when read operation interest is disabled.
     */
    protected void setDisabledReader() {

        assert Thread.holdsLock(this);

        if (isClosed()) {
            return;
        }

        this.inReader = new Resolver() {

            public int resolve(int size) throws IOException {

                SynchronousManagedConnection smc = SynchronousManagedConnection.this;
                assert Thread.holdsLock(smc);

                ByteBuffer bb = smc.in.inBuffer;

                // If running low, request more!
                if (bb.remaining() <= bb.capacity() >>> 1) {

                    getThread().debug("Reads enabled [%s].", smc);

                    setEnabledReader();
                    setEnabled(OperationType.READ, true);
                }

                if (size > 0) {

                    return size;

                } else {

                    throw new IOException("Control should never reach here");
                }
            }
        };
    }

    /**
     * Sets a read {@link Resolver} for when an end-of-stream has been reached.
     */
    protected void setEOSReader() {

        assert Thread.holdsLock(this);

        this.inReader = new Resolver() {

            public int resolve(int size) {

                SynchronousManagedConnection smc = SynchronousManagedConnection.this;
                assert Thread.holdsLock(smc);

                return (size > 0) ? size : -1;
            }
        };
    }

    /**
     * Sets an error read {@link Resolver}.
     */
    protected void setErrorReader(final IOException x) {

        assert Thread.holdsLock(this);

        this.inReader = new Resolver() {

            public int resolve(int size) throws IOException {

                SynchronousManagedConnection smc = SynchronousManagedConnection.this;
                assert Thread.holdsLock(smc);

                if (size > 0) {

                    return size;

                } else {

                    throw x;
                }
            }
        };
    }

    /**
     * Sets the default write {@link Resolver}.
     */
    protected void setDefaultWriter() {

        assert Thread.holdsLock(this);

        this.outWriter = new Resolver() {

            public int resolve(int remaining) {

                SynchronousManagedConnection smc = SynchronousManagedConnection.this;
                assert Thread.holdsLock(smc);

                return sendOutbound(smc.out.outBuffer) + smc.out.outBuffer.remaining();
            }
        };
    }

    /**
     * Sets an error write {@link Resolver}.
     */
    protected void setErrorWriter(final IOException x) {

        assert Thread.holdsLock(this);

        this.outWriter = new Resolver() {

            public int resolve(int remaining) throws IOException {

                SynchronousManagedConnection smc = SynchronousManagedConnection.this;
                assert Thread.holdsLock(smc);

                // If there's nothing left to write, return normally.
                if (remaining + smc.out.outBuffer.remaining() == 0) {

                    return 0;

                }
                // Otherwise, throw an exception.
                else {

                    throw x;
                }
            }
        };
    }

    Resolver inReader;
    Resolver outWriter;

    ManagedInputStream in;
    ManagedOutputStream out;

    /**
     * Default constructor.
     * 
     * @see AbstractManagedConnection#AbstractManagedConnection(String, ConnectionManager)
     */
    public SynchronousManagedConnection(String name, ConnectionManager manager) {
        super(name, manager);

        this.in = null;
        this.out = null;

        synchronized (this) {

            setEnabledReader();
            setDefaultWriter();
        }

        IdentityFilterFactory<ByteBuffer, SynchronousManagedConnection> iFF = IdentityFilterFactory.getInstance();
        setFilterFactory(iFF);
    }

    /**
     * Alternate constructor.
     * 
     * @see #SynchronousManagedConnection(String, ConnectionManager)
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
                    "Connection is not bound");

            return this.in;
        }
    }

    /**
     * Gets the {@link OutputStream}.
     */
    public OutputStream getOutputStream() {

        synchronized (this) {

            Control.checkTrue(isBound(), //
                    "Connection is not bound");

            return this.out;
        }
    }

    public void onBind(Queue<ByteBuffer> inbounds) {

        synchronized (this) {

            // Initialize input/output streams.
            this.in = new ManagedInputStream();
            this.out = new ManagedOutputStream();
        }
    }

    public void onReceive(Queue<ByteBuffer> inbounds) {

        boolean disableReads = false;

        synchronized (this) {

            notifyAll();

            for (ByteBuffer inbound = null; (inbound = inbounds.peek()) != null && !disableReads;) {

                ByteBuffer receiveBB = this.in.inBuffer;

                receiveBB.compact();

                int size = Math.min(inbound.remaining(), receiveBB.remaining());
                int save = inbound.position();

                receiveBB.put(inbound.array(), save, size).flip();
                inbound.position(save + size);

                disableReads = (receiveBB.remaining() == receiveBB.capacity()) //
                        && inbound.hasRemaining();

                if (!inbound.hasRemaining()) {
                    inbounds.remove();
                }
            }

            // Disable reads if the incoming buffer is full.
            if (disableReads) {

                getThread().debug("Reads disabled [%s].", this);

                setDisabledReader();
                setEnabled(OperationType.READ, false);
            }
        }
    }

    public void onClosing(ClosingType type, Queue<ByteBuffer> inbounds) {

        final IOException x;

        synchronized (this) {

            switch (type) {

            case EOS:

                x = new IOException("Connection closed");

                setEOSReader();
                setErrorWriter(x);

                // Append the remainders onto the incoming buffer.
                for (ByteBuffer bb; (bb = inbounds.poll()) != null;) {
                    this.in.inBuffer = (ByteBuffer) Buffers.append(this.in.inBuffer.compact(), bb).flip();
                }

                break;

            case USER:

                x = new IOException("Connection closed");

                setErrorReader(x);
                setErrorWriter(x);

                // Append the remainders onto the incoming buffer.
                for (ByteBuffer bb; (bb = inbounds.poll()) != null;) {
                    this.in.inBuffer = (ByteBuffer) Buffers.append(this.in.inBuffer.compact(), bb).flip();
                }

                break;

            case ERROR:

                x = (IOException) new IOException("Connection encountered an error").initCause(getError());

                setErrorReader(x);
                setErrorWriter(x);

                // Do NOT append the remainder onto the incoming buffer, since the very cause of the error could be a
                // buffer discontinuity.

                break;

            default:
                throw new AssertionError("Control should never reach here");
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
    protected class ManagedInputStream extends InputStream implements ManagedStream {

        ByteBuffer inBuffer;

        /**
         * Default constructor.
         */
        protected ManagedInputStream() {

            // Invariant: The incoming buffer is always in the ready-to-read configuration.
            this.inBuffer = (ByteBuffer) ByteBuffer.allocate(getBufferSize()).flip();
        }

        /**
         * Reads a {@code byte}.
         */
        @Override
        public int read() throws IOException {

            SynchronousManagedConnection smc = getConnection();

            synchronized (smc) {

                for (; !isClosed() && !this.inBuffer.hasRemaining();) {
                    smc.waitInterruptibly();
                }

                if (this.inBuffer.hasRemaining()) {

                    int res = this.inBuffer.get() & 0x000000FF;

                    smc.inReader.resolve(1);

                    return res;

                } else {

                    return smc.inReader.resolve(0);
                }
            }
        }

        /**
         * Reads {@code byte}s.
         */
        @Override
        public int read(byte[] dst, int offset, int length) throws IOException {

            SynchronousManagedConnection smc = getConnection();

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

                for (; !isClosed() && !this.inBuffer.hasRemaining();) {
                    smc.waitInterruptibly();
                }

                int size = Math.min(this.inBuffer.remaining(), length);
                this.inBuffer.get(dst, offset, size);

                return smc.inReader.resolve(size);
            }
        }

        /**
         * Closes this stream.
         */
        @Override
        public void close() {
            Control.close(getConnection());
        }

        /**
         * Gets the number of readable bytes before blocking.
         */
        @Override
        public int available() {

            SynchronousManagedConnection smc = getConnection();

            synchronized (smc) {
                return this.inBuffer.remaining();
            }
        }

        public SynchronousManagedConnection getConnection() {
            return SynchronousManagedConnection.this;
        }
    }

    /**
     * A subclass of {@link OutputStream} that is managed underneath.
     */
    protected class ManagedOutputStream extends OutputStream implements ManagedStream {

        final ByteBuffer outBuffer;

        /**
         * Default constructor.
         */
        protected ManagedOutputStream() {

            // Invariant: The outgoing buffer is always in the ready-to-read configuration.
            this.outBuffer = (ByteBuffer) ByteBuffer.allocate(getBufferSize()).flip();
        }

        /**
         * Writes a {@code byte}.
         */
        @Override
        public void write(int b) throws IOException {

            SynchronousManagedConnection smc = getConnection();

            synchronized (smc) {

                for (int size, length = 1; length > 0; length -= size) {

                    this.outBuffer.compact();
                    size = Math.min(this.outBuffer.remaining(), length);
                    ((size > 0) ? this.outBuffer.put((byte) b) : this.outBuffer).flip();

                    for (; smc.outWriter.resolve(length - size) > 0;) {
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

            SynchronousManagedConnection smc = getConnection();

            if (src == null) {
                throw new NullPointerException("Null source array");
            }

            if (length < 0 || src.length < offset + length) {
                throw new IndexOutOfBoundsException("Invalid offset/length");
            }

            synchronized (smc) {

                for (int size; length > 0; offset += size, length -= size) {

                    this.outBuffer.compact();
                    size = Math.min(this.outBuffer.remaining(), length);
                    this.outBuffer.put(src, offset, size).flip();

                    for (; smc.outWriter.resolve(length - size) > 0;) {
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
            Control.close(getConnection());
        }

        public SynchronousManagedConnection getConnection() {
            return SynchronousManagedConnection.this;
        }
    }
}

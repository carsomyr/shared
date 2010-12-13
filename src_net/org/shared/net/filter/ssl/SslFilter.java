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

package org.shared.net.filter.ssl;

import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.Executor;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLEngineResult.Status;
import javax.net.ssl.SSLException;

import org.shared.net.Buffers;
import org.shared.net.Connection;
import org.shared.net.Connection.OperationType;
import org.shared.net.filter.Filter;
import org.shared.net.filter.Filters;
import org.shared.net.filter.IdentityFilterFactory;
import org.shared.net.filter.OobEvent;
import org.shared.net.filter.OobFilter;
import org.shared.net.handler.FilteredHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of {@link Filter} that encrypts/decrypts traffic using the <a
 * href="http://en.wikipedia.org/wiki/Transport_Layer_Security">SSL/TLS</a> protocol.
 * 
 * @author Roy Liu
 */
public class SslFilter implements OobFilter<ByteBuffer, ByteBuffer> {

    /**
     * The global {@link Logger} instance.
     */
    final protected static Logger log = LoggerFactory.getLogger(SslFilter.class);

    /**
     * The {@link Filter} to use when TLS is on.
     */
    final protected OobFilter<ByteBuffer, ByteBuffer> tlsOnFilter = new OobFilter<ByteBuffer, ByteBuffer>() {

        @Override
        public void applyInbound(Queue<ByteBuffer> inputs, Queue<ByteBuffer> outputs) {

            SslFilter sslF = SslFilter.this;

            sslF.decryptBuffer.compact();

            //

            for (ByteBuffer bb; (bb = inputs.poll()) != null;) {
                sslF.readBuffer = (ByteBuffer) Buffers.append(sslF.readBuffer.compact(), bb, 1).flip();
            }

            try {

                loop: for (;;) {

                    SSLEngineResult result = sslF.engine.unwrap( //
                            (ByteBuffer) sslF.readBuffer.compact().flip(), //
                            sslF.decryptBuffer);

                    Status status = result.getStatus();
                    HandshakeStatus handshakeStatus = result.getHandshakeStatus();

                    // The first step is to read in TLS packets.

                    switch (status) {

                    case BUFFER_OVERFLOW:

                        sslF.debugStatusInbound(result);

                        // Expand the application buffer.
                        sslF.decryptBuffer = Buffers.resize( //
                                (ByteBuffer) sslF.decryptBuffer.flip(), //
                                (sslF.decryptBuffer.capacity() << 1) + 1);

                        // Continue the loop: See what the next network buffer read brings.
                        continue loop;

                    case BUFFER_UNDERFLOW:

                        sslF.debugStatusInbound(result);

                        // Break the loop: Wait for the next network buffer read.
                        break loop;

                    case CLOSED:

                        sslF.debugStatusInbound(result);

                        // Clear the read buffer for good measure.
                        sslF.readBuffer.clear().flip();

                        // Break the loop: Although a final SSLEngine#wrap is possible with bidirectional shutdown, we
                        // stick with the unidirectional case.
                        break loop;

                    case OK:
                        // Continue processing: Everything is OK.
                        break;

                    default:
                        throw new IllegalStateException("Invalid result status");
                    }

                    // The second step is to deal with handshaking.

                    switch (handshakeStatus) {

                    case NEED_UNWRAP:

                        sslF.debugHandshakeStatusInbound(result);

                        // Continue the loop: The first step is conveniently calling SSLEngine#unwrap.
                        continue loop;

                    case NEED_WRAP:

                        sslF.debugHandshakeStatusInbound(result);

                        sslF.handler.send(null);

                        // Break the loop: We can't proceed until the remote host responds.
                        break loop;

                    case NEED_TASK:

                        sslF.debugHandshakeStatusInbound(result);

                        synchronized (sslF.handler.getConnection().getLock()) {
                            sslF.runDelegatedTasks();
                        }

                        // Break the loop: We can't proceed until external tasks have finished.
                        break loop;

                    case FINISHED:

                        sslF.debugHandshakeStatusInbound(result);

                        // Write pending outbound data.
                        sslF.handler.send(null);

                        // Continue the loop: We just finished handshaking.
                        continue loop;

                    case NOT_HANDSHAKING:
                        // Continue processing: Nothing remarkable happened.
                        break;

                    default:
                        throw new IllegalStateException("Invalid handshake status");
                    }

                    // Break the loop: There's nothing to read and all special handling is done.
                    if (!sslF.readBuffer.hasRemaining()) {
                        break loop;
                    }
                }

            } catch (SSLException e) {

                throw new RuntimeException(e);
            }

            //

            outputs.add((ByteBuffer) sslF.decryptBuffer.flip());
        }

        @Override
        public void applyOutbound(Queue<ByteBuffer> inputs, Queue<ByteBuffer> outputs) {

            SslFilter sslF = SslFilter.this;

            sslF.encryptBuffer.compact();

            //

            for (ByteBuffer bb; (bb = inputs.poll()) != null;) {
                sslF.writeBuffer = (ByteBuffer) Buffers.append(sslF.writeBuffer.compact(), bb, 1).flip();
            }

            try {

                loop: for (;;) {

                    SSLEngineResult result = sslF.engine.wrap( //
                            (ByteBuffer) sslF.writeBuffer.compact().flip(), //
                            sslF.encryptBuffer);

                    Status status = result.getStatus();
                    HandshakeStatus handshakeStatus = result.getHandshakeStatus();

                    // The first step is to write out TLS packets.

                    switch (status) {

                    case BUFFER_OVERFLOW:

                        sslF.debugStatusOutbound(result);

                        // Expand the network buffer.
                        sslF.encryptBuffer = Buffers.resize( //
                                (ByteBuffer) sslF.encryptBuffer.flip(), //
                                (sslF.encryptBuffer.capacity() << 1) + 1);

                        // Continue the loop: See what the next application buffer read brings.
                        continue loop;

                    case CLOSED:

                        sslF.debugStatusOutbound(result);

                        // Clear the write buffer for good measure.
                        sslF.writeBuffer.clear().flip();

                        // Break the loop: Writers have no more possible actions.
                        break loop;

                    case OK:

                        if (sslF.shutdownOutbound) {

                            debug("[%s] shut down outbound.", sslF.handler);

                            // Write out a close_notify.
                            sslF.engine.closeOutbound();
                            sslF.shutdownOutbound = false;

                            continue loop;
                        }

                        // Continue processing: Everything is OK.
                        break;

                    case BUFFER_UNDERFLOW:
                    default:
                        throw new IllegalStateException("Invalid result status");
                    }

                    // The second step is to deal with handshaking.

                    switch (handshakeStatus) {

                    case NEED_UNWRAP:

                        sslF.debugHandshakeStatusOutbound(result);

                        // Break the loop: We can't proceed until the remote host responds.
                        break loop;

                    case NEED_WRAP:

                        sslF.debugHandshakeStatusOutbound(result);

                        // Continue the loop: The first step is conveniently calling SSLEngine#wrap.
                        continue loop;

                    case NEED_TASK:

                        sslF.debugHandshakeStatusOutbound(result);

                        sslF.runDelegatedTasks();

                        // Break the loop: We can't proceed until external tasks have finished.
                        break loop;

                    case FINISHED:

                        sslF.debugHandshakeStatusOutbound(result);

                        // Continue the loop: We just finished handshaking.
                        continue loop;

                    case NOT_HANDSHAKING:
                        // Continue processing: Nothing remarkable happened.
                        break;

                    default:
                        throw new IllegalStateException("Invalid handshake status");
                    }

                    // Break the loop: There's nothing to write and all special handling is done.
                    if (!sslF.writeBuffer.hasRemaining()) {
                        break loop;
                    }
                }

            } catch (SSLException e) {

                throw new RuntimeException(e);
            }

            //

            outputs.add((ByteBuffer) sslF.encryptBuffer.flip());
        }

        @Override
        public void applyInboundOob(Queue<OobEvent> inputs, Queue<OobEvent> outputs) {
            Filters.transfer(inputs, outputs);
        }

        @Override
        public void applyOutboundOob(Queue<OobEvent> inputs, Queue<OobEvent> outputs) {

            SslFilter sslF = SslFilter.this;

            for (OobEvent evt; (evt = inputs.poll()) != null;) {

                switch (evt.getType()) {

                case CLOSING_USER:
                    sslF.shutdownOutbound = true;
                    break;
                }

                outputs.add(evt);
            }
        }
    };

    /**
     * The {@link Filter} to use when TLS is off.
     */
    final protected OobFilter<ByteBuffer, ByteBuffer> tlsOffFilter = IdentityFilterFactory.newFilter();

    final SSLEngine engine;
    final FilteredHandler<?, ?, ?> handler;
    final Executor executor;

    ByteBuffer encryptBuffer;
    ByteBuffer decryptBuffer;
    ByteBuffer readBuffer;
    ByteBuffer writeBuffer;

    boolean shutdownOutbound;

    OobFilter<ByteBuffer, ByteBuffer> filter;

    /**
     * Default constructor.
     * 
     * @param engine
     *            the {@link SSLEngine} for encryption/decryption of traffic.
     * @param handler
     *            the associated {@link FilteredHandler}.
     * @param executor
     *            the {@link Executor} for carrying out delegated tasks.
     */
    protected SslFilter(SSLEngine engine, FilteredHandler<?, ?, ?> handler, Executor executor) {

        this.engine = engine;
        this.handler = handler;
        this.executor = executor;

        this.encryptBuffer = ByteBuffer.allocate(0);
        this.decryptBuffer = ByteBuffer.allocate(0);
        this.readBuffer = ByteBuffer.allocate(0);
        this.writeBuffer = ByteBuffer.allocate(0);

        this.shutdownOutbound = false;

        this.filter = this.tlsOnFilter;
    }

    @Override
    public void applyInbound(Queue<ByteBuffer> inputs, Queue<ByteBuffer> outputs) {

        Connection conn = this.handler.getConnection();
        assert !Thread.holdsLock(conn.getLock()) && conn.isManagerThread();

        this.filter.applyInbound(inputs, outputs);
    }

    @Override
    public void applyOutbound(Queue<ByteBuffer> inputs, Queue<ByteBuffer> outputs) {

        assert Thread.holdsLock(this.handler.getConnection().getLock());

        this.filter.applyOutbound(inputs, outputs);
    }

    @Override
    public void applyInboundOob(Queue<OobEvent> inputs, Queue<OobEvent> outputs) {

        Connection conn = this.handler.getConnection();
        assert !Thread.holdsLock(conn.getLock()) && conn.isManagerThread();

        this.filter.applyInboundOob(inputs, outputs);
    }

    @Override
    public void applyOutboundOob(Queue<OobEvent> inputs, Queue<OobEvent> outputs) {

        assert Thread.holdsLock(this.handler.getConnection().getLock());

        this.filter.applyOutboundOob(inputs, outputs);
    }

    /**
     * Sets {@link #tlsOnFilter}.
     */
    protected void setTlsOnFilter() {
        this.filter = this.tlsOnFilter;
    }

    /**
     * Sets {@link #tlsOffFilter}.
     */
    protected void setTlsOffFilter() {
        this.filter = this.tlsOffFilter;
    }

    /**
     * Runs delegated tasks.
     */
    protected void runDelegatedTasks() {

        assert Thread.holdsLock(this.handler.getConnection().getLock());

        for (Runnable r = null; (r = this.engine.getDelegatedTask()) != null;) {

            final Runnable rr = r;

            this.executor.execute(new Runnable() {

                @Override
                public void run() {

                    SslFilter sslF = SslFilter.this;

                    debug("[%s] begin delegated task [%s].", //
                            sslF.handler, rr);

                    try {

                        rr.run();

                    } finally {

                        // Force a read to get things going again.
                        sslF.handler.getConnection().setEnabled(OperationType.READ, true);

                        debug("[%s] finish delegated task [%s].", //
                                sslF.handler, rr);
                    }
                }
            });
        }
    }

    /**
     * Logs a debugging message based on the current {@link Status} for the inbound direction.
     */
    protected void debugStatusInbound(SSLEngineResult result) {
        debug("[%s] inbound %s, c=%d, r=%d, d=%d.", //
                this.handler, result.getStatus(), //
                result.bytesConsumed(), this.readBuffer.remaining(), this.decryptBuffer.position());
    }

    /**
     * Logs a debugging message based on the current {@link HandshakeStatus} for the inbound direction.
     */
    protected void debugHandshakeStatusInbound(SSLEngineResult result) {
        debug("[%s] inbound %s, c=%d.", //
                this.handler, result.getHandshakeStatus(), result.bytesConsumed());
    }

    /**
     * Logs a debugging message based on the current {@link Status} for the outbound direction.
     */
    protected void debugStatusOutbound(SSLEngineResult result) {
        debug("[%s] outbound %s, p=%d, w=%d, e=%d.", //
                this.handler, result.getStatus(), //
                result.bytesProduced(), this.writeBuffer.remaining(), this.encryptBuffer.position());
    }

    /**
     * Logs a debugging message based on the current {@link HandshakeStatus} for the outbound direction.
     */
    protected void debugHandshakeStatusOutbound(SSLEngineResult result) {
        debug("[%s] outbound %s, p=%d.", //
                this.handler, result.getHandshakeStatus(), result.bytesProduced());
    }

    /**
     * Logs a debugging message.
     */
    final protected static void debug(String format, Object... args) {

        if (log.isDebugEnabled()) {
            log.debug(String.format(format, args));
        }
    }
}

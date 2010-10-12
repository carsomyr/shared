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
import java.util.concurrent.Executor;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import shared.net.Buffers;
import shared.net.Connection.OperationType;

/**
 * An implementation of {@link Filter} that encrypts/decrypts traffic using the <a
 * href="http://en.wikipedia.org/wiki/Transport_Layer_Security">SSL/TLS</a> protocol.
 * 
 * @param <C>
 *            the {@link FilteredConnection} type.
 * @author Roy Liu
 */
public class SslFilter<C extends FilteredConnection<C, ?>> implements OobFilter<ByteBuffer, ByteBuffer> {

    /**
     * The global {@link Logger} instance.
     */
    final protected static Logger log = LoggerFactory.getLogger(SslFilter.class);

    final SSLEngine engine;
    final C connection;
    final Executor executor;

    ByteBuffer encryptBuffer;
    ByteBuffer decryptBuffer;
    ByteBuffer readBuffer;
    ByteBuffer writeBuffer;

    boolean shutdownOutbound;

    /**
     * Default constructor.
     * 
     * @param engine
     *            the {@link SSLEngine} for encryption/decryption of traffic.
     * @param connection
     *            the associated {@link FilteredConnection}.
     * @param executor
     *            the {@link Executor} for carrying out delegated tasks.
     */
    protected SslFilter(SSLEngine engine, C connection, Executor executor) {

        this.engine = engine;
        this.connection = connection;
        this.executor = executor;

        this.encryptBuffer = ByteBuffer.allocate(0);
        this.decryptBuffer = ByteBuffer.allocate(0);
        this.readBuffer = ByteBuffer.allocate(0);
        this.writeBuffer = ByteBuffer.allocate(0);

        this.shutdownOutbound = false;
    }

    @Override
    public void applyInbound(Queue<ByteBuffer> inputs, Queue<ByteBuffer> outputs) {

        assert !Thread.holdsLock(this.connection.getLock());

        this.decryptBuffer.compact();

        //

        for (ByteBuffer bb; (bb = inputs.poll()) != null;) {
            this.readBuffer = (ByteBuffer) Buffers.append(this.readBuffer.compact(), bb, 1).flip();
        }

        try {

            loop: for (;;) {

                SSLEngineResult result = this.engine.unwrap( //
                        (ByteBuffer) this.readBuffer.compact().flip(), //
                        this.decryptBuffer);

                // The first step is to read in TLS packets.

                switch (result.getStatus()) {

                case BUFFER_OVERFLOW:

                    debug("[%s] inbound %s, r=%d, d=%d.", //
                            this.connection, result, //
                            this.readBuffer.remaining(), this.decryptBuffer.position());

                    // Expand the application buffer.
                    this.decryptBuffer = Buffers.resize( //
                            (ByteBuffer) this.decryptBuffer.flip(), //
                            (this.decryptBuffer.capacity() << 1) + 1);

                    // Continue the loop: See what the next network buffer read brings.
                    continue loop;

                case BUFFER_UNDERFLOW:

                    debug("[%s] inbound %s, r=%d, d=%d.", //
                            this.connection, result, //
                            this.readBuffer.remaining(), this.decryptBuffer.position());

                    // Break the loop: Wait for the next network buffer read.
                    break loop;

                case CLOSED:

                    debug("[%s] inbound %s.", this.connection, result);

                    // Clear the read buffer for good measure.
                    this.readBuffer.clear().flip();

                    // Break the loop: Although a final SSLEngine#wrap is possible with bidirectional shutdown, we stick
                    // with the unidirectional case.
                    break loop;

                case OK:
                    // Continue processing: Everything is OK.
                    break;

                default:
                    throw new IllegalStateException("Invalid result status");
                }

                // The second step is to deal with handshaking.

                switch (result.getHandshakeStatus()) {

                case NEED_UNWRAP:

                    debug("[%s] inbound %s.", this.connection, result);

                    // Continue the loop: The first step is conveniently calling SSLEngine#unwrap.
                    continue loop;

                case NEED_WRAP:

                    debug("[%s] inbound %s.", this.connection, result);

                    this.connection.sendOutbound(null);

                    // Break the loop: We can't proceed until the remote host responds.
                    break loop;

                case NEED_TASK:

                    debug("[%s] inbound %s.", this.connection, result);

                    synchronized (this.connection.getLock()) {
                        runDelegatedTasks();
                    }

                    // Break the loop: We can't proceed until external tasks have finished.
                    break loop;

                case FINISHED:

                    debug("[%s] inbound %s.", this.connection, result);

                    // Write pending outbound data.
                    this.connection.sendOutbound(null);

                    // Continue the loop: We just finished handshaking.
                    continue loop;

                case NOT_HANDSHAKING:
                    // Continue processing: Nothing remarkable happened.
                    break;

                default:
                    throw new IllegalStateException("Invalid handshake status");
                }

                // Break the loop: There's nothing to read and all special handling is done.
                if (!this.readBuffer.hasRemaining()) {
                    break loop;
                }
            }

        } catch (SSLException e) {

            throw new RuntimeException(e);
        }

        //

        outputs.add((ByteBuffer) this.decryptBuffer.flip());
    }

    @Override
    public void applyOutbound(Queue<ByteBuffer> inputs, Queue<ByteBuffer> outputs) {

        assert Thread.holdsLock(this.connection.getLock());

        this.encryptBuffer.compact();

        //

        for (ByteBuffer bb; (bb = inputs.poll()) != null;) {
            this.writeBuffer = (ByteBuffer) Buffers.append(this.writeBuffer.compact(), bb, 1).flip();
        }

        try {

            loop: for (;;) {

                SSLEngineResult result = this.engine.wrap( //
                        (ByteBuffer) this.writeBuffer.compact().flip(), //
                        this.encryptBuffer);

                // The first step is to write out TLS packets.

                switch (result.getStatus()) {

                case BUFFER_OVERFLOW:

                    debug("[%s] outbound %s, w=%d, e=%d.", //
                            this.connection, result, //
                            this.writeBuffer.remaining(), this.encryptBuffer.position());

                    // Expand the network buffer.
                    this.encryptBuffer = Buffers.resize( //
                            (ByteBuffer) this.encryptBuffer.flip(), //
                            (this.encryptBuffer.capacity() << 1) + 1);

                    // Continue the loop: See what the next application buffer read brings.
                    continue loop;

                case CLOSED:

                    debug("[%s] outbound %s.", this.connection, result);

                    // Clear the write buffer for good measure.
                    this.writeBuffer.clear().flip();

                    // Break the loop: Writers have no more possible actions.
                    break loop;

                case OK:

                    if (this.shutdownOutbound) {

                        debug("[%s] shut down outbound.", this.connection);

                        // Write out a close_notify.
                        this.engine.closeOutbound();
                        this.shutdownOutbound = false;

                        continue loop;
                    }

                    // Continue processing: Everything is OK.
                    break;

                case BUFFER_UNDERFLOW:
                default:
                    throw new IllegalStateException("Invalid result status");
                }

                // The second step is to deal with handshaking.

                switch (result.getHandshakeStatus()) {

                case NEED_UNWRAP:

                    debug("[%s] outbound %s.", this.connection, result);

                    // Break the loop: We can't proceed until the remote host responds.
                    break loop;

                case NEED_WRAP:

                    debug("[%s] outbound %s.", this.connection, result);

                    // Continue the loop: The first step is conveniently calling SSLEngine#wrap.
                    continue loop;

                case NEED_TASK:

                    debug("[%s] outbound %s.", this.connection, result);

                    runDelegatedTasks();

                    // Break the loop: We can't proceed until external tasks have finished.
                    break loop;

                case FINISHED:

                    debug("[%s] outbound %s.", this.connection, result);

                    // Continue the loop: We just finished handshaking.
                    continue loop;

                case NOT_HANDSHAKING:
                    // Continue processing: Nothing remarkable happened.
                    break;

                default:
                    throw new IllegalStateException("Invalid handshake status");
                }

                // Break the loop: There's nothing to write and all special handling is done.
                if (!this.writeBuffer.hasRemaining()) {
                    break loop;
                }
            }

        } catch (SSLException e) {

            throw new RuntimeException(e);
        }

        //

        outputs.add((ByteBuffer) this.encryptBuffer.flip());
    }

    @Override
    public void applyInboundOob(Queue<OobEvent> inputs, Queue<OobEvent> outputs) {
        Filters.transfer(inputs, outputs);
    }

    @Override
    public void applyOutboundOob(Queue<OobEvent> inputs, Queue<OobEvent> outputs) {

        for (OobEvent evt; (evt = inputs.poll()) != null;) {

            switch (evt.getType()) {

            case CLOSING_USER:
                this.shutdownOutbound = true;
                break;
            }

            outputs.add(evt);
        }
    }

    /**
     * Runs delegated tasks.
     */
    protected void runDelegatedTasks() {

        assert Thread.holdsLock(this.connection.getLock());

        for (Runnable r = null; (r = this.engine.getDelegatedTask()) != null;) {

            final Runnable rr = r;

            this.executor.execute(new Runnable() {

                @Override
                public void run() {

                    SslFilter<C> sslF = SslFilter.this;

                    debug("[%s] begin delegated task [%s].", //
                            sslF.connection, rr);

                    try {

                        rr.run();

                    } finally {

                        // Force a read to get things going again.
                        sslF.connection.setEnabled(OperationType.READ, true);

                        debug("[%s] finish delegated task [%s].", //
                                sslF.connection, rr);
                    }
                }
            });
        }
    }

    /**
     * Logs a debugging message.
     */
    final protected static void debug(String format, Object... args) {

        if (log.isDebugEnabled()) {

            for (int i = 0, n = args.length; i < n; i++) {

                if (args[i] instanceof SSLEngineResult) {

                    SSLEngineResult result = (SSLEngineResult) args[i];
                    args[i] = String.format("Result[%s, %s, c=%d, p=%d]", //
                            result.getStatus(), result.getHandshakeStatus(), //
                            result.bytesConsumed(), result.bytesProduced());
                }
            }

            log.debug(String.format(format, args));
        }
    }
}
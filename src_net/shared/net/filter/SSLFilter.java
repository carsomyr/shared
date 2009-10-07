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
import java.util.concurrent.Executor;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import shared.net.Buffers;
import shared.net.Connection;

/**
 * An implementation of {@link Filter} that encrypts/decrypts traffic using the <a
 * href="http://en.wikipedia.org/wiki/Transport_Layer_Security">SSL/TLS</a> protocol.
 * 
 * @param <C>
 *            the {@link FilteredConnection} type.
 * @author Roy Liu
 */
public class SSLFilter<C extends FilteredConnection<C, ?>> implements StatefulFilter<ByteBuffer, ByteBuffer> {

    /**
     * The global {@link Logger} instance.
     */
    final protected static Logger Log = LoggerFactory.getLogger(SSLFilter.class);

    final SSLEngine engine;
    final C connection;
    final Executor executor;

    Filter<ByteBuffer, ByteBuffer> filter;

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
     *            the associated {@link Connection}.
     * @param executor
     *            the {@link Executor} for carrying out delegated tasks.
     */
    protected SSLFilter(SSLEngine engine, C connection, Executor executor) {

        this.engine = engine;
        this.connection = connection;
        this.executor = executor;

        this.encryptBuffer = Buffers.EmptyBuffer;
        this.decryptBuffer = Buffers.EmptyBuffer;
        this.readBuffer = Buffers.EmptyBuffer;
        this.writeBuffer = Buffers.EmptyBuffer;

        this.shutdownOutbound = false;
    }

    public void getInbound(Queue<ByteBuffer> in, Queue<ByteBuffer> out) {

        assert !Thread.holdsLock(this.connection);

        this.decryptBuffer.compact();

        //

        for (ByteBuffer bb = null; (bb = in.poll()) != null;) {
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

                    // We got a "close_notify".

                    if (!this.engine.isInboundDone()) {
                        this.engine.closeInbound();
                    }

                    if (!this.engine.isOutboundDone()) {
                        this.engine.closeOutbound();
                    }

                    // Clear the read buffer for good measure.
                    this.readBuffer.clear().flip();

                    // Break the loop: Although a final wrap() is possible with bidirectional
                    // shutdown, we stick with the unidirectional case.
                    break loop;

                case OK:
                    // Continue processing: Everything is OK.
                    break;

                default:
                    throw new AssertionError("Control should never reach here");
                }

                // The second step is to deal with handshaking.

                switch (result.getHandshakeStatus()) {

                case NEED_UNWRAP:

                    debug("[%s] inbound %s.", this.connection, result);

                    // Continue the loop: The first step is conveniently calling unwrap().
                    continue loop;

                case NEED_WRAP:

                    debug("[%s] inbound %s.", this.connection, result);

                    // Continue the loop: We can immediately do a write.
                    this.connection.sendOutbound(null);
                    continue loop;

                case NEED_TASK:

                    debug("[%s] inbound %s.", this.connection, result);

                    // Break the loop: We can't proceed until external tasks are completed.
                    synchronized (this.connection) {
                        runDelegatedTasks();
                    }

                    break loop;

                case FINISHED:

                    debug("[%s] inbound %s.", this.connection, result);

                    // Continue the loop: We just finished handshaking and should write pending
                    // outbound data.
                    this.connection.sendOutbound(null);
                    continue loop;

                case NOT_HANDSHAKING:
                    // Continue processing: Nothing remarkable happened.
                    break;

                default:
                    throw new AssertionError("Control should never reach here");
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

        out.add((ByteBuffer) this.decryptBuffer.flip());
    }

    public void getOutbound(Queue<ByteBuffer> in, Queue<ByteBuffer> out) {

        assert Thread.holdsLock(this.connection);

        this.encryptBuffer.compact();

        //

        for (ByteBuffer bb = null; (bb = in.poll()) != null;) {
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

                        // Write out a "close_notify".
                        this.engine.closeOutbound();
                        this.shutdownOutbound = false;

                        continue loop;
                    }

                    // Continue processing: Everything is OK.
                    break;

                case BUFFER_UNDERFLOW:
                default:
                    throw new AssertionError("Control should never reach here");
                }

                // The second step is to deal with handshaking.

                switch (result.getHandshakeStatus()) {

                case NEED_UNWRAP:

                    debug("[%s] outbound %s.", this.connection, result);

                    // Break the loop: Let the inbound handle this.
                    break loop;

                case NEED_WRAP:

                    debug("[%s] outbound %s.", this.connection, result);

                    // Continue the loop: The first step is conveniently calling wrap().
                    continue loop;

                case NEED_TASK:

                    debug("[%s] outbound %s.", this.connection, result);

                    // Break the loop: We can't proceed until external tasks are completed.
                    runDelegatedTasks();
                    break loop;

                case FINISHED:

                    debug("[%s] outbound %s.", this.connection, result);

                    // Continue the loop: We just finished handshaking.
                    continue loop;

                case NOT_HANDSHAKING:
                    // Continue processing: Nothing remarkable happened.
                    break;

                default:
                    throw new AssertionError("Control should never reach here");
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

        out.add((ByteBuffer) this.encryptBuffer.flip());
    }

    public void bindInbound(Queue<ByteBuffer> in, Queue<ByteBuffer> out) {
        getInbound(in, out);
    }

    public void bindOutbound(Queue<ByteBuffer> in, Queue<ByteBuffer> out) {
        getOutbound(in, out);
    }

    public void shutdownInbound(Queue<ByteBuffer> in, Queue<ByteBuffer> out) {
        getInbound(in, out);
    }

    public void shutdownOutbound(Queue<ByteBuffer> in, Queue<ByteBuffer> out) {

        this.shutdownOutbound = true;

        getOutbound(in, out);
    }

    /**
     * Runs delegated tasks.
     */
    protected void runDelegatedTasks() {

        assert Thread.holdsLock(this.connection);

        for (Runnable r = null; (r = this.engine.getDelegatedTask()) != null;) {

            final Runnable rr = r;

            this.executor.execute(new Runnable() {

                public void run() {

                    SSLFilter<C> sslF = SSLFilter.this;

                    debug("[%s] begin delegated task [%s].", //
                            sslF.connection, rr);

                    try {

                        rr.run();

                    } finally {

                        // Force a read to get things going again.
                        sslF.connection.setReadEnabled(true);

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

        if (Log.isDebugEnabled()) {

            for (int i = 0, n = args.length; i < n; i++) {

                if (args[i] instanceof SSLEngineResult) {

                    SSLEngineResult result = (SSLEngineResult) args[i];
                    args[i] = String.format("Result[%s, %s, c=%d, p=%d]", //
                            result.getStatus(), result.getHandshakeStatus(), //
                            result.bytesConsumed(), result.bytesProduced());
                }
            }

            Log.debug(String.format(format, args));
        }
    }
}
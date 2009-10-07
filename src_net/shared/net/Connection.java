/**
 * This file is part of the Shared Scientific Toolbox in Java ("this library"). <br />
 * <br />
 * Copyright (C) 2005 Roy Liu <br />
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

package shared.net;

import java.io.Closeable;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Future;

/**
 * Defines an asynchronous, callback-based socket.
 * 
 * @author Roy Liu
 */
public interface Connection extends Closeable {

    /**
     * On receipt of data.
     * 
     * @param bb
     *            the {@link ByteBuffer} containing data.
     */
    public void onReceive(ByteBuffer bb);

    /**
     * On successful binding.
     */
    public void onBind();

    /**
     * On end-of-stream.
     * 
     * @param bb
     *            the {@link ByteBuffer} containing data. It must be completely drained, as this is the final callback.
     */
    public void onClosingEOS(ByteBuffer bb);

    /**
     * On user-requested close.
     * 
     * @param bb
     *            the {@link ByteBuffer} containing data. It must be completely drained, as this is the final callback.
     */
    public void onClosingUser(ByteBuffer bb);

    /**
     * On error.
     * 
     * @param error
     *            the error that occurred.
     * @param bb
     *            the {@link ByteBuffer} containing data. It must be completely drained, as this is the final callback.
     */
    public void onError(Throwable error, ByteBuffer bb);

    /**
     * On completion of connection closure, which can result from {@link #onClosingEOS(ByteBuffer)},
     * {@link #onClosingUser(ByteBuffer)}, or {@link #onError(Throwable, ByteBuffer)}.
     */
    public void onClose();

    /**
     * Asynchronously sends data to the remote host.
     * 
     * @param bb
     *            the {@link ByteBuffer} containing data. It must be in ready-to-read mode.
     * @return the number of {@code byte}s remaining in this connection's write buffer; is greater than {@code 0} if and
     *         only if not all {@code byte}s could be written out to the network, and have to be stored locally for the
     *         time being.
     */
    public int send(ByteBuffer bb);

    /**
     * Asynchronously requests a connection to the given IP address.
     * 
     * @return a completion {@link Future} that yields the local endpoint {@link InetSocketAddress}.
     */
    public Future<InetSocketAddress> connect(InetSocketAddress addr);

    /**
     * Asynchronously binds to and listens on some network interface associated with the given IP address.
     * 
     * @return a completion {@link Future} that yields the remote endpoint {@link InetSocketAddress}.
     */
    public Future<InetSocketAddress> accept(InetSocketAddress addr);

    /**
     * Registers an existing {@link SocketChannel} with this connection.
     * 
     * @return a completion {@link Future}.
     */
    public Future<?> register(SocketChannel channel);

    /**
     * Sets whether synchronous reads are enabled. This method, with argument {@code true}, shall guarantee that
     * {@link #onReceive(ByteBuffer)} will subsequently be called at least once.
     * 
     * @param enabled
     *            whether synchronous reads are enabled.
     */
    public void setReadEnabled(boolean enabled);

    /**
     * Sets whether synchronous writes are enabled.
     * 
     * @param enabled
     *            whether synchronous writes are enabled.
     */
    public void setWriteEnabled(boolean enabled);

    /**
     * Delivers an error to this connection.
     * 
     * @param error
     *            the error.
     */
    public void error(Throwable error);

    /**
     * Closes this connection.
     */
    public void close();
}

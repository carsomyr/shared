/**
 * <p>
 * Copyright (C) 2005 Roy Liu<br />
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

/**
 * <p>
 * Copyright (c) 2010 Roy Liu<br>
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

package org.shared.net;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * Defines a socket-based {@link ConnectionManager}.
 * 
 * @param <M>
 *            the parameterization lower bounded by {@link SocketManager} itself.
 * @param <C>
 *            the {@link SocketConnection} type.
 * @author Roy Liu
 */
public interface SocketManager<M extends SocketManager<M, C>, C extends SocketConnection> extends ConnectionManager<C> {

    /**
     * Gets the list of bound addresses.
     */
    public List<InetSocketAddress> getBoundAddresses();

    /**
     * Gets the listen backlog size.
     */
    public int getBacklogSize();

    /**
     * Sets the listen backlog size.
     * 
     * @param backlogSize
     *            the backlog size.
     */
    public M setBacklogSize(int backlogSize);

    /**
     * Gets the read/write buffer size.
     */
    public int getBufferSize();

    /**
     * Sets the read/write buffer size.
     * 
     * @param bufferSize
     *            the buffer size.
     */
    public M setBufferSize(int bufferSize);
}

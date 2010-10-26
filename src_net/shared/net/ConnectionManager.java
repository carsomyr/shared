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

package shared.net;

import java.io.Closeable;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Defines a transport layer for managing {@link Connection}s.
 * 
 * @apiviz.has shared.net.ConnectionManager.InitializationType - - - argument
 * @param <C>
 *            the {@link Connection} type.
 * @author Roy Liu
 */
public interface ConnectionManager<C extends Connection> extends Closeable {

    /**
     * An enumeration of the ways in which a connection may be initialized.
     */
    public static enum InitializationType {

        /**
         * Denotes connecting to a remote address.
         */
        CONNECT, //

        /**
         * Denotes accepting from a local address.
         */
        ACCEPT, //

        /**
         * Denotes registration of an existing, unmanaged connection.
         */
        REGISTER;
    }

    /**
     * Initializes a {@link Connection}.
     * 
     * @param type
     *            the {@link InitializationType}.
     * @param handler
     *            the {@link ConnectionHandler}.
     * @param argument
     *            the argument.
     * @param <T>
     *            the argument type.
     * @return a {@link Future} for retrieving the newly initialized {@link Connection}.
     * @see InitializationType
     */
    public <T> Future<C> init(InitializationType type, ConnectionHandler<? super C> handler, T argument);

    /**
     * Gets the list of {@link Connection}s.
     */
    public List<C> getConnections();

    /**
     * Shuts down this manager and reclaims its resources.
     */
    @Override
    public void close();

    /**
     * Creates a human-readable representation of this manager.
     */
    @Override
    public String toString();
}

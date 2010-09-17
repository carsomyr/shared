/**
 * <p>
 * Copyright (c) 2005 Roy Liu<br>
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
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import shared.util.Control;

/**
 * A bookkeeping class for storing pending accepts on listening sockets.
 * 
 * @apiviz.composedOf shared.net.AcceptRegistry.Entry
 * @author Roy Liu
 */
public class AcceptRegistry {

    final Selector selector;
    final int backlogSize;

    final Map<InetSocketAddress, Entry> addressToEntryMap;
    final Map<AbstractManagedConnection<?>, Entry> connectionToEntryMap;

    /**
     * Default constructor.
     */
    protected AcceptRegistry(Selector selector, int backlogSize) {

        this.selector = selector;
        this.backlogSize = backlogSize;

        this.addressToEntryMap = new HashMap<InetSocketAddress, Entry>();
        this.connectionToEntryMap = new HashMap<AbstractManagedConnection<?>, Entry>();
    }

    /**
     * Registers a connection.
     * 
     * @throws IOException
     *             when something goes awry.
     */
    protected Entry register(AbstractManagedConnection<?> conn, InetSocketAddress address) throws IOException {

        Entry entry = this.addressToEntryMap.get(address);

        if (entry == null) {

            entry = new Entry(address);
            this.addressToEntryMap.put(entry.getAddress(), entry);
        }

        entry.getPending().add(conn);
        this.connectionToEntryMap.put(conn, entry);

        return entry;
    }

    /**
     * Removes a pending accept.
     */
    protected void removePending(AbstractManagedConnection<?> conn) {

        Entry entry = this.connectionToEntryMap.remove(conn);

        // Null reference. Nothing to do.
        if (entry == null) {
            return;
        }

        Set<AbstractManagedConnection<?>> pending = entry.getPending();
        InetSocketAddress address = entry.getAddress();
        SelectionKey key = entry.getKey();

        pending.remove(conn);

        // Close the server socket if it has no remaining accept interests.
        if (pending.isEmpty()) {

            this.addressToEntryMap.remove(address);

            Control.close(key.channel());
            key.cancel();
        }
    }

    /**
     * Gets the bound addresses.
     */
    protected Set<InetSocketAddress> getAddresses() {
        return Collections.unmodifiableSet(this.addressToEntryMap.keySet());
    }

    /**
     * A container class for information on bound {@link ServerSocket}s.
     */
    protected class Entry {

        final InetSocketAddress address;
        final SelectionKey key;
        final Set<AbstractManagedConnection<?>> pending;

        /**
         * Default constructor.
         * 
         * @throws IOException
         *             when a {@link ServerSocket} could not be bound to the given address.
         */
        protected Entry(InetSocketAddress address) throws IOException {

            // Bind the server socket.
            ServerSocketChannel channel = ServerSocketChannel.open();

            ServerSocket socket = channel.socket();

            socket.setReuseAddress(true);
            socket.bind(address, AcceptRegistry.this.backlogSize);

            channel.configureBlocking(false);

            // Normalize the recently bound local address.
            this.address = new InetSocketAddress((address != null) ? address.getAddress() : null, //
                    ((InetSocketAddress) socket.getLocalSocketAddress()).getPort());

            // Create a SelectionKey for the server socket.
            this.key = channel.register(AcceptRegistry.this.selector, SelectionKey.OP_ACCEPT);

            this.pending = new LinkedHashSet<AbstractManagedConnection<?>>();
        }

        /**
         * Gets the bound address.
         */
        protected InetSocketAddress getAddress() {
            return this.address;
        }

        /**
         * Gets the {@link SelectionKey}.
         */
        protected SelectionKey getKey() {
            return this.key;
        }

        /**
         * Gets the pending accepts.
         */
        protected Set<AbstractManagedConnection<?>> getPending() {
            return this.pending;
        }
    }
}

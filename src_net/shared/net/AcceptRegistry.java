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
    final int backlog;

    final Map<InetSocketAddress, Entry> addressToEntryMap;
    final Map<AbstractManagedConnection<?>, Entry> connectionToEntryMap;

    /**
     * Default constructor.
     */
    protected AcceptRegistry(Selector selector, int backlog) {

        this.selector = selector;
        this.backlog = backlog;

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

        Control.checkTrue(address.getPort() > 0, //
                "Wildcard ports are not allowed");

        Entry entry = this.addressToEntryMap.get(address);

        boolean newEntry = (entry == null);

        entry = newEntry ? new Entry(address) : entry;

        entry.pending.add(conn);
        this.connectionToEntryMap.put(conn, entry);

        if (newEntry) {
            this.addressToEntryMap.put(entry.address, entry);
        }

        return entry;
    }

    /**
     * Removes a pending accept.
     */
    protected void removePending(AbstractManagedConnection<?> conn) {

        Entry entry = this.connectionToEntryMap.get(conn);

        // Null reference. Nothing to do.
        if (entry == null) {
            return;
        }

        Set<AbstractManagedConnection<?>> pending = entry.getPending();

        pending.remove(conn);
        this.connectionToEntryMap.remove(conn);

        // Close the server socket if it has no remaining accept interests.
        if (pending.isEmpty()) {

            this.addressToEntryMap.remove(entry.address);

            Control.close(entry.key.channel());
            entry.key.cancel();
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
            socket.bind(address, AcceptRegistry.this.backlog);

            channel.configureBlocking(false);

            this.address = new InetSocketAddress(address.getAddress(), //
                    ((InetSocketAddress) socket.getLocalSocketAddress()).getPort());

            // Create a selection key for the server socket.
            this.key = channel.register(AcceptRegistry.this.selector, SelectionKey.OP_ACCEPT);

            // Connections are sorted by their sequence numbers.
            this.pending = new LinkedHashSet<AbstractManagedConnection<?>>();
        }

        /**
         * Gets the bound address.
         */
        protected InetSocketAddress getAddress() {
            return this.address;
        }

        /**
         * Gets the pending accepts.
         */
        protected Set<AbstractManagedConnection<?>> getPending() {
            return this.pending;
        }

        /**
         * Gets the {@link SelectionKey}.
         */
        protected SelectionKey getKey() {
            return this.key;
        }
    }
}

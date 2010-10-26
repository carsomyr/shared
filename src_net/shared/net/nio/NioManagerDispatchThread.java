/**
 * <p>
 * Copyright (c) 2009 Roy Liu<br>
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

package shared.net.nio;

import static shared.net.Constants.DEFAULT_BACKLOG_SIZE;
import static shared.net.nio.NioEvent.NioEventType.DISPATCH;
import static shared.net.nio.NioEvent.NioEventType.GET_CONNECTIONS;
import static shared.net.nio.NioEvent.NioEventType.SHUTDOWN;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import shared.event.Handler;
import shared.event.Transitions;
import shared.event.Transitions.Transition;
import shared.net.nio.NioConnection.NioConnectionStatus;
import shared.net.nio.NioManagerDispatchThread.AcceptRegistry.Entry;
import shared.util.Control;

/**
 * A specialized {@link NioManagerThread} that dispatches newly created connections to {@link NioManagerIoThread}s.
 * 
 * @apiviz.composedOf shared.net.nio.NioManagerDispatchThread.AcceptRegistry
 * @apiviz.composedOf shared.net.nio.NioManagerIoThread
 * @author Roy Liu
 */
public class NioManagerDispatchThread extends NioManagerThread {

    @Override
    protected void onStart() {
        initFsms();
    }

    @Override
    protected void onStop() {

        // Tell the bad news to all pending connections.
        for (SelectionKey key : this.selector.keys()) {

            Object attachment = key.attachment();

            if (attachment instanceof Entry) {

                Set<NioConnection> pending = ((Entry) attachment).getPending();

                // Copy the collection to prevent concurrent modification.
                for (NioConnection pendingConn : new ArrayList<NioConnection>(pending)) {
                    handleError(pendingConn, this.exception);
                }

                // All pending connections must have been deregistered.
                assert pending.isEmpty();
            }
        }

        for (NioManagerIoThread ioThread : this.ioThreads) {
            ioThread.onLocal(new NioEvent<Object>(SHUTDOWN, null));
        }
    }

    @Override
    protected void doReadyOps(int readyOps, SelectionKey key) {

        // Each operation is responsible for its own exception handling.

        if ((readyOps & SelectionKey.OP_ACCEPT) != 0) {
            doAccept(key);
        }

        if ((readyOps & SelectionKey.OP_CONNECT) != 0) {
            doConnect((NioConnection) key.attachment());
        }
    }

    @Override
    protected void purge(NioConnection conn) {
        this.acceptRegistry.removePending(conn);
    }

    /**
     * Starts this thread and its helper {@link NioManagerIoThread}s.
     */
    @Override
    public void start() {

        for (NioManagerThread ioThread : this.ioThreads) {
            ioThread.start();
        }

        super.start();
    }

    /**
     * Dispatches the given connection to an {@link NioManagerIoThread}.
     */
    protected void dispatch(NioConnection conn) {

        NioManagerIoThread ioThread = this.ioThreads.removeFirst();
        this.ioThreads.add(ioThread);

        // Break the connection's relationship with this thread.
        conn.deregisterKey();

        // Acquire the connection monitor to shut out external requests.
        synchronized (conn.getLock()) {

            conn.setThread(ioThread);
            ioThread.onLocal(new NioEvent<Object>(DISPATCH, conn));
        }
    }

    /**
     * Finishes the accept cycle on a ready {@link ServerSocketChannel}.
     */
    protected void doAccept(SelectionKey key) {

        Entry entry = (Entry) key.attachment();
        ServerSocketChannel ssChannel = (ServerSocketChannel) key.channel();

        Set<NioConnection> pending = entry.getPending();

        // We had better have pending accepts.
        assert !pending.isEmpty();

        NioConnection conn = pending.iterator().next();

        // The connection had better be in the correct state.
        assert (conn.getStatus() == NioConnectionStatus.ACCEPT);

        try {

            final SocketChannel channel;

            try {

                channel = ssChannel.accept();

            } catch (IOException e) {

                this.acceptRegistry.removePending(conn);

                // Copy the collection to prevent concurrent modification.
                for (NioConnection pendingConn : new ArrayList<NioConnection>(pending)) {
                    handleError(pendingConn, e);
                }

                // All pending connections must have been deregistered.
                assert pending.isEmpty();

                throw e;
            }

            this.acceptRegistry.removePending(conn);

            conn.setup(channel);
            conn.doBind();

            debug("[%s] accepted at \"%s\".", conn, conn.getLocalAddress());

            conn.setStatus(NioConnectionStatus.ACTIVE);

            dispatch(conn);

        } catch (Throwable t) {

            handleError(conn, t);
        }
    }

    /**
     * Finishes the connect cycle on a ready connection.
     */
    protected void doConnect(NioConnection conn) {

        // The connection had better be in the correct state.
        assert (conn.getStatus() == NioConnectionStatus.CONNECT);

        try {

            // It had better be the case that this method either throws an exception or returns true.
            Control.checkTrue(((SocketChannel) conn.getKey().channel()).finishConnect(), //
                    "Expected to finish connecting");

            conn.doBind();

            debug("[%s] connected to \"%s\".", conn, conn.getRemoteAddress());

            conn.setStatus(NioConnectionStatus.ACTIVE);

            dispatch(conn);

        } catch (Throwable t) {

            handleError(conn, t);
        }
    }

    /**
     * Handles a connection connect request.
     */
    protected void handleConnect(NioConnection conn, InetSocketAddress address) {

        // The connection had better be in the correct state.
        assert (conn.getStatus() == NioConnectionStatus.VIRGIN);

        final boolean connectImmediately;

        try {

            SocketChannel channel = SocketChannel.open();

            conn.setup(channel);
            conn.registerKey(this.selector, SelectionKey.OP_CONNECT);

            connectImmediately = channel.connect(address);

            debug("[%s] connect to \"%s\".", conn, address);

            conn.setStatus(NioConnectionStatus.CONNECT);

        } catch (Throwable t) {

            handleError(conn, t);

            return;
        }

        // If connected for some reason, then finish up the process.
        if (connectImmediately) {
            doConnect(conn);
        }
    }

    /**
     * Handles a connection accept request.
     */
    protected void handleAccept(NioConnection conn, InetSocketAddress address) {

        // The connection had better be in the correct state.
        assert (conn.getStatus() == NioConnectionStatus.VIRGIN);

        try {

            Entry entry = this.acceptRegistry.register(conn, address);
            entry.getKey().attach(entry);

            debug("[%s] listen at \"%s\" (%d in queue).", //
                    conn, entry.getAddress(), entry.getPending().size());

            conn.setStatus(NioConnectionStatus.ACCEPT);

        } catch (Throwable t) {

            handleError(conn, t);
        }
    }

    /**
     * Handles a connection registration request.
     */
    protected void handleRegister(NioConnection conn, SocketChannel chan) {

        // The connection had better be in the correct state.
        assert (conn.getStatus() == NioConnectionStatus.VIRGIN);

        try {

            // Set up the connection and simulate deferred writes.
            conn.setup(chan);
            conn.doBind();

            debug("[%s] registered.", conn);

            conn.setStatus(NioConnectionStatus.ACTIVE);

            dispatch(conn);

        } catch (Throwable t) {

            handleError(conn, t);
        }
    }

    /**
     * Handles a request to get the list of bound addresses.
     */
    protected void handleGetBoundAddresses(Request<?, List<InetSocketAddress>> request) {
        request.set(new ArrayList<InetSocketAddress>(this.acceptRegistry.getAddresses()));
    }

    /**
     * Handles a request to get the list of connections, which is an aggregate of the lists reported by helper
     * {@link NioManagerIoThread}s.
     */
    @SuppressWarnings("unchecked")
    protected void handleGetConnections(Request<?, List<NioConnection>> request) {

        List<NioConnection> res = new ArrayList<NioConnection>();

        for (NioManagerIoThread ioThread : this.ioThreads) {
            res.addAll((List<NioConnection>) ioThread.request(GET_CONNECTIONS, null));
        }

        request.set(res);
    }

    /**
     * Handles a request to get the listen backlog size.
     */
    protected void handleGetBacklogSize(Request<?, Integer> request) {
        request.set(this.backlogSize);
    }

    /**
     * Handles a request to set the listen backlog size.
     */
    protected void handleSetBacklogSize(Request<Integer, ?> request) {

        int backlogSize = request.getArgument();

        if (backlogSize > 0) {

            this.backlogSize = backlogSize;

            request.set(null);

        } else {

            request.setException(new IllegalArgumentException("Invalid backlog size"));
        }
    }

    @Transition(currentState = "VIRGIN", eventType = "CONNECT")
    final Handler<NioEvent<InetSocketAddress>> connectHandler = new Handler<NioEvent<InetSocketAddress>>() {

        @Override
        public void handle(NioEvent<InetSocketAddress> evt) {
            handleConnect((NioConnection) evt.getSource(), evt.getArgument());
        }
    };

    @Transition(currentState = "VIRGIN", eventType = "ACCEPT")
    final Handler<NioEvent<InetSocketAddress>> acceptHandler = new Handler<NioEvent<InetSocketAddress>>() {

        @Override
        public void handle(NioEvent<InetSocketAddress> evt) {
            handleAccept((NioConnection) evt.getSource(), evt.getArgument());
        }
    };

    @Transition(currentState = "VIRGIN", eventType = "REGISTER")
    final Handler<NioEvent<SocketChannel>> registerHandler = new Handler<NioEvent<SocketChannel>>() {

        @Override
        public void handle(NioEvent<SocketChannel> evt) {
            handleRegister((NioConnection) evt.getSource(), evt.getArgument());
        }
    };

    @Transitions(transitions = {
            //
            @Transition(currentState = "CONNECT", eventType = "CLOSE"), //
            @Transition(currentState = "ACCEPT", eventType = "CLOSE") //
    })
    final Handler<NioEvent<?>> closeHandler = new Handler<NioEvent<?>>() {

        @Override
        public void handle(NioEvent<?> evt) {
            handleClose((NioConnection) evt.getSource());
        }
    };

    @Transitions(transitions = {
            //
            @Transition(currentState = "VIRGIN", eventType = "ERROR"), //
            @Transition(currentState = "CONNECT", eventType = "ERROR"), //
            @Transition(currentState = "ACCEPT", eventType = "ERROR") //
    })
    final Handler<NioEvent<Throwable>> errorHandler = new Handler<NioEvent<Throwable>>() {

        @Override
        public void handle(NioEvent<Throwable> evt) {
            handleError((NioConnection) evt.getSource(), evt.getArgument());
        }
    };

    @Transitions(transitions = {
            //
            @Transition(currentState = "CONNECT", eventType = "EXECUTE"), //
            @Transition(currentState = "ACCEPT", eventType = "EXECUTE") //
    })
    final Handler<NioEvent<Runnable>> executeHandler = new Handler<NioEvent<Runnable>>() {

        @Override
        public void handle(NioEvent<Runnable> evt) {
            handleExecute((NioConnection) evt.getSource(), evt.getArgument());
        }
    };

    @Transition(currentState = "RUN", eventType = "GET_BOUND_ADDRESSES", group = "internal")
    final Handler<NioEvent<Request<?, List<InetSocketAddress>>>> getBoundAddressesHandler = //
    new Handler<NioEvent<Request<?, List<InetSocketAddress>>>>() {

        @Override
        public void handle(NioEvent<Request<?, List<InetSocketAddress>>> evt) {
            handleGetBoundAddresses(evt.getArgument());
        }
    };

    @Transition(currentState = "RUN", eventType = "GET_CONNECTIONS", group = "internal")
    final Handler<NioEvent<Request<?, List<NioConnection>>>> getConnectionsHandler = //
    new Handler<NioEvent<Request<?, List<NioConnection>>>>() {

        @Override
        public void handle(NioEvent<Request<?, List<NioConnection>>> evt) {
            handleGetConnections(evt.getArgument());
        }
    };

    @Transition(currentState = "RUN", eventType = "GET_BACKLOG_SIZE", group = "internal")
    final Handler<NioEvent<Request<?, Integer>>> getBacklogSizeHandler = //
    new Handler<NioEvent<Request<?, Integer>>>() {

        @Override
        public void handle(NioEvent<Request<?, Integer>> evt) {
            handleGetBacklogSize(evt.getArgument());
        }
    };

    @Transition(currentState = "RUN", eventType = "SET_BACKLOG_SIZE", group = "internal")
    final Handler<NioEvent<Request<Integer, ?>>> setBacklogSizeHandler = //
    new Handler<NioEvent<Request<Integer, ?>>>() {

        @Override
        public void handle(NioEvent<Request<Integer, ?>> evt) {
            handleSetBacklogSize(evt.getArgument());
        }
    };

    @Transition(currentState = "RUN", eventType = "SHUTDOWN", group = "internal")
    final Handler<NioEvent<?>> shutdownHandler = new Handler<NioEvent<?>>() {

        @Override
        public void handle(NioEvent<?> evt) {
            handleShutdown();
        }
    };

    final AcceptRegistry acceptRegistry;
    final LinkedList<NioManagerIoThread> ioThreads;

    int backlogSize;

    /**
     * Default constructor.
     */
    protected NioManagerDispatchThread(String name, int nThreads) {
        super(String.format("%s/Dispatch", name));

        this.acceptRegistry = new AcceptRegistry();

        this.ioThreads = new LinkedList<NioManagerIoThread>();

        for (int i = 0; i < nThreads; i++) {
            this.ioThreads.add(new NioManagerIoThread(String.format("%s/IO-%d", name, i), this));
        }

        this.backlogSize = DEFAULT_BACKLOG_SIZE;
    }

    /**
     * A bookkeeping class for storing pending accepts on listening sockets.
     * 
     * @apiviz.composedOf shared.net.nio.NioManagerDispatchThread.AcceptRegistry.Entry
     */
    protected class AcceptRegistry {

        final Map<InetSocketAddress, Entry> addressToEntryMap;
        final Map<NioConnection, Entry> connectionToEntryMap;

        /**
         * Default constructor.
         */
        protected AcceptRegistry() {

            this.addressToEntryMap = new HashMap<InetSocketAddress, Entry>();
            this.connectionToEntryMap = new HashMap<NioConnection, Entry>();
        }

        /**
         * Registers a connection.
         * 
         * @throws IOException
         *             when something goes awry.
         */
        protected Entry register(NioConnection conn, InetSocketAddress address) throws IOException {

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
        protected void removePending(NioConnection conn) {

            Entry entry = this.connectionToEntryMap.remove(conn);

            // Null reference. Nothing to do.
            if (entry == null) {
                return;
            }

            Set<NioConnection> pending = entry.getPending();
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
            final Set<NioConnection> pending;

            /**
             * Default constructor.
             * 
             * @throws IOException
             *             when a {@link ServerSocket} could not be bound to the given address.
             */
            protected Entry(InetSocketAddress address) throws IOException {

                NioManagerDispatchThread thread = NioManagerDispatchThread.this;

                // Bind the server socket.
                ServerSocketChannel channel = ServerSocketChannel.open();

                ServerSocket socket = channel.socket();

                socket.setReuseAddress(true);
                socket.bind(address, thread.backlogSize);

                channel.configureBlocking(false);

                // Normalize the recently bound local address.
                this.address = new InetSocketAddress((address != null) ? address.getAddress() : null, //
                        ((InetSocketAddress) socket.getLocalSocketAddress()).getPort());

                // Create a SelectionKey for the server socket.
                this.key = channel.register(thread.selector, SelectionKey.OP_ACCEPT);

                this.pending = new LinkedHashSet<NioConnection>();
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
            protected Set<NioConnection> getPending() {
                return this.pending;
            }
        }
    }
}

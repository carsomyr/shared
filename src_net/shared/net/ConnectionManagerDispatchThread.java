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

package shared.net;

import static shared.net.InterestEvent.InterestEventType.DISPATCH;
import static shared.net.InterestEvent.InterestEventType.QUERY_CONNECTIONS;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import shared.event.Handler;
import shared.event.Transitions;
import shared.event.Transitions.Transition;
import shared.net.AbstractManagedConnection.AbstractManagedConnectionStatus;
import shared.net.AcceptRegistry.Entry;
import shared.util.Control;
import shared.util.RequestFuture;

/**
 * A specialized {@link ConnectionManagerThread} that dispatches newly created connections to
 * {@link ConnectionManagerIOThread}s.
 * 
 * @apiviz.composedOf shared.net.ConnectionManagerIOThread
 * @apiviz.composedOf shared.net.AcceptRegistry
 * @author Roy Liu
 */
public class ConnectionManagerDispatchThread extends ConnectionManagerThread {

    @Override
    protected void onStart() {
        initFSMs();
    }

    @Override
    protected void onStop() {

        for (ConnectionManagerIOThread ioThread : this.ioThreads) {
            Control.close(ioThread);
        }
    }

    @Override
    protected void doReadyOps(int readyOps, SelectionKey key) {

        // Each operation is responsible for its own exception handling.

        if ((readyOps & SelectionKey.OP_ACCEPT) != 0) {
            doAccept(key);
        }

        if ((readyOps & SelectionKey.OP_CONNECT) != 0) {
            doConnect((AbstractManagedConnection<?>) key.attachment());
        }
    }

    @Override
    protected void purge(AbstractManagedConnection<?> conn) {
        this.acceptRegistry.removePending(conn);
    }

    /**
     * Starts this thread and its helper {@link ConnectionManagerIOThread}s.
     */
    @Override
    public void start() {

        for (ConnectionManagerThread ioThread : this.ioThreads) {
            ioThread.start();
        }

        super.start();
    }

    /**
     * Dispatches the given connection to a {@link ConnectionManagerIOThread}.
     */
    protected void dispatch(AbstractManagedConnection<?> conn) {

        ConnectionManagerIOThread ioThread = this.ioThreads.removeFirst();
        this.ioThreads.add(ioThread);

        // Break the connection's relationship with this thread.
        conn.deregisterKey();

        // Acquire the connection monitor to shut out external requests.
        synchronized (conn) {

            conn.setThread(ioThread);
            ioThread.onLocal(new InterestEvent<Object>(DISPATCH, conn.getProxy()));
        }
    }

    /**
     * Finishes the accept cycle on a ready {@link ServerSocketChannel}.
     */
    protected void doAccept(SelectionKey key) {

        Entry entry = (Entry) key.attachment();
        ServerSocketChannel ssChannel = (ServerSocketChannel) key.channel();

        Set<AbstractManagedConnection<?>> pending = entry.getPending();

        // We had better have pending accepts.
        Control.assertTrue(!pending.isEmpty());

        AbstractManagedConnection<?> conn = pending.iterator().next();

        // The connection had better be in the correct state.
        assert (conn.getStatus() == AbstractManagedConnectionStatus.ACCEPT);

        try {

            final SocketChannel channel;

            try {

                channel = ssChannel.accept();

            } catch (IOException e) {

                this.acceptRegistry.removePending(conn);

                for (; !pending.isEmpty();) {
                    handleError(pending.iterator().next(), e);
                }

                throw e;
            }

            this.acceptRegistry.removePending(conn);

            conn.setup(channel);

            debug("Accepted [%s] at '%s'.", conn, conn.getLocalAddress());

            conn.setStatus(AbstractManagedConnectionStatus.ACTIVE);

            dispatch(conn);

        } catch (Throwable t) {

            handleError(conn, t);
        }
    }

    /**
     * Finishes the connect cycle on a ready connection.
     */
    protected void doConnect(AbstractManagedConnection<?> conn) {

        // The connection had better be in the correct state.
        assert (conn.getStatus() == AbstractManagedConnectionStatus.CONNECT);

        try {

            // It had better be the case that this method either throws an exception or returns true.
            Control.assertTrue(((SocketChannel) conn.getKey().channel()).finishConnect());

            debug("Connected [%s] to '%s'.", conn, conn.getRemoteAddress());

            conn.setStatus(AbstractManagedConnectionStatus.ACTIVE);

            dispatch(conn);

        } catch (Throwable t) {

            handleError(conn, t);
        }
    }

    /**
     * Handles a connection connect request.
     */
    protected void handleConnect(AbstractManagedConnection<?> conn, InetSocketAddress address) {

        // The connection had better be in the correct state.
        assert (conn.getStatus() == AbstractManagedConnectionStatus.VIRGIN);

        final boolean connectImmediately;

        try {

            SocketChannel channel = SocketChannel.open();

            conn.setup(channel);
            conn.registerKey(this.selector, SelectionKey.OP_CONNECT);

            connectImmediately = channel.connect(address);

            debug("Connect [%s] to '%s'.", conn, address);

            conn.setStatus(AbstractManagedConnectionStatus.CONNECT);

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
    protected void handleAccept(AbstractManagedConnection<?> conn, InetSocketAddress address) {

        // The connection had better be in the correct state.
        assert (conn.getStatus() == AbstractManagedConnectionStatus.VIRGIN);

        try {

            Entry entry = this.acceptRegistry.register(conn, address);
            entry.getKey().attach(entry);

            debug("Listen [%s] at '%s' (%d in queue).", //
                    conn, entry.getAddress(), entry.getPending().size());

            conn.setStatus(AbstractManagedConnectionStatus.ACCEPT);

        } catch (Throwable t) {

            handleError(conn, t);
        }
    }

    /**
     * Handles a connection registration request.
     */
    protected void handleRegister(AbstractManagedConnection<?> conn, SocketChannel chan) {

        // The connection had better be in the correct state.
        assert (conn.getStatus() == AbstractManagedConnectionStatus.VIRGIN);

        try {

            // Set up the connection and simulate deferred writes.
            conn.setup(chan);

            debug("Registered [%s].", conn);

            conn.setStatus(AbstractManagedConnectionStatus.ACTIVE);

            dispatch(conn);

        } catch (Throwable t) {

            handleError(conn, t);
        }
    }

    /**
     * Handles a query for the list of bound addresses.
     */
    protected void handleQueryBoundAddresses(RequestFuture<List<InetSocketAddress>> future) {
        future.set(new ArrayList<InetSocketAddress>(this.acceptRegistry.getAddresses()));
    }

    /**
     * Handles a query for the list of connections, which is an aggregate of the lists reported by helper
     * {@link ConnectionManagerIOThread}s.
     */
    @SuppressWarnings("unchecked")
    protected void handleQueryConnections(RequestFuture<List<AbstractManagedConnection<?>>> evt) {

        List<AbstractManagedConnection<?>> res = new ArrayList<AbstractManagedConnection<?>>();

        for (ConnectionManagerIOThread ioThread : this.ioThreads) {
            res.addAll((List<AbstractManagedConnection<?>>) ioThread.query(QUERY_CONNECTIONS));
        }

        evt.set(res);
    }

    @Transition(currentState = "VIRGIN", eventType = "CONNECT")
    final Handler<InterestEvent<InetSocketAddress>> connectHandler = new Handler<InterestEvent<InetSocketAddress>>() {

        public void handle(InterestEvent<InetSocketAddress> evt) {
            handleConnect(((ProxySource<?>) evt.getSource()).getConnection(), evt.getArgument());
        }
    };

    @Transition(currentState = "VIRGIN", eventType = "ACCEPT")
    final Handler<InterestEvent<InetSocketAddress>> acceptHandler = new Handler<InterestEvent<InetSocketAddress>>() {

        public void handle(InterestEvent<InetSocketAddress> evt) {
            handleAccept(((ProxySource<?>) evt.getSource()).getConnection(), evt.getArgument());
        }
    };

    @Transition(currentState = "VIRGIN", eventType = "REGISTER")
    final Handler<InterestEvent<SocketChannel>> registerHandler = new Handler<InterestEvent<SocketChannel>>() {

        public void handle(InterestEvent<SocketChannel> evt) {
            handleRegister(((ProxySource<?>) evt.getSource()).getConnection(), evt.getArgument());
        }
    };

    @Transitions(transitions = {
    //
            @Transition(currentState = "CONNECT", eventType = "EXECUTE"), //
            @Transition(currentState = "ACCEPT", eventType = "EXECUTE") //
    })
    final Handler<InterestEvent<Runnable>> executeHandler = new Handler<InterestEvent<Runnable>>() {

        public void handle(InterestEvent<Runnable> evt) {
            handleExecute(((ProxySource<?>) evt.getSource()).getConnection(), evt.getArgument());
        }
    };

    @Transitions(transitions = {
    //
            @Transition(currentState = "CONNECT", eventType = "CLOSE"), //
            @Transition(currentState = "ACCEPT", eventType = "CLOSE") //
    })
    final Handler<InterestEvent<?>> closeHandler = new Handler<InterestEvent<?>>() {

        public void handle(InterestEvent<?> evt) {
            handleClose(((ProxySource<?>) evt.getSource()).getConnection());
        }
    };

    @Transitions(transitions = {
    //
            @Transition(currentState = "VIRGIN", eventType = "ERROR"), //
            @Transition(currentState = "CONNECT", eventType = "ERROR"), //
            @Transition(currentState = "ACCEPT", eventType = "ERROR") //
    })
    final Handler<InterestEvent<Throwable>> errorHandler = new Handler<InterestEvent<Throwable>>() {

        public void handle(InterestEvent<Throwable> evt) {
            handleError(((ProxySource<?>) evt.getSource()).getConnection(), evt.getArgument());
        }
    };

    @Transition(currentState = "RUN", eventType = "QUERY_BOUND_ADDRESSES", group = "internal")
    final Handler<InterestEvent<RequestFuture<List<InetSocketAddress>>>> queryBoundAddressesHandler = //
    new Handler<InterestEvent<RequestFuture<List<InetSocketAddress>>>>() {

        public void handle(InterestEvent<RequestFuture<List<InetSocketAddress>>> evt) {
            handleQueryBoundAddresses(evt.getArgument());
        }
    };

    @Transition(currentState = "RUN", eventType = "QUERY_CONNECTIONS", group = "internal")
    final Handler<InterestEvent<RequestFuture<List<AbstractManagedConnection<?>>>>> queryConnectionsHandler = //
    new Handler<InterestEvent<RequestFuture<List<AbstractManagedConnection<?>>>>>() {

        public void handle(InterestEvent<RequestFuture<List<AbstractManagedConnection<?>>>> evt) {
            handleQueryConnections(evt.getArgument());
        }
    };

    @Transition(currentState = "RUN", eventType = "SHUTDOWN", group = "internal")
    final Handler<InterestEvent<?>> shutdownHandler = new Handler<InterestEvent<?>>() {

        public void handle(InterestEvent<?> evt) {
            handleShutdown();
        }
    };

    final AcceptRegistry acceptRegistry;
    final LinkedList<ConnectionManagerIOThread> ioThreads;
    final int backlog;

    /**
     * Default constructor.
     */
    protected ConnectionManagerDispatchThread(String name, int backlog, int nthreads) {
        super(String.format("%s/Dispatch", name));

        this.acceptRegistry = new AcceptRegistry(this.selector, this.backlog);

        this.ioThreads = new LinkedList<ConnectionManagerIOThread>();

        for (int i = 0; i < nthreads; i++) {
            this.ioThreads.add(new ConnectionManagerIOThread(String.format("%s/IO-%d", name, i), this));
        }

        this.backlog = backlog;
    }
}
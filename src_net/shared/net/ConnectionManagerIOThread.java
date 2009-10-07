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

import static shared.net.InterestEvent.InterestEventType.SHUTDOWN;

import java.nio.channels.SelectionKey;
import java.util.ArrayList;
import java.util.List;

import shared.event.Handler;
import shared.event.Transitions;
import shared.event.Transitions.Transition;
import shared.net.AbstractManagedConnection.AbstractManagedConnectionStatus;
import shared.util.RequestFuture;

/**
 * A specialized {@link ConnectionManagerThread} that reads from and writes to connections.
 * 
 * @author Roy Liu
 */
public class ConnectionManagerIOThread extends ConnectionManagerThread {

    @Override
    protected void onStart() {
        initFSMs();
    }

    @Override
    protected void onStop() {
        this.parent.onLocal(new InterestEvent<Object>(SHUTDOWN, null));
    }

    @Override
    protected void doReadyOps(int readyOps, SelectionKey key) {

        AbstractManagedConnection<?> conn = (AbstractManagedConnection<?>) key.attachment();

        // Each operation is responsible for its own exception handling.

        if ((readyOps & SelectionKey.OP_READ) != 0) {
            conn.doRead();
        }

        if ((readyOps & SelectionKey.OP_WRITE) != 0) {
            conn.doWrite();
        }
    }

    /**
     * Handles a connection dispatch.
     */
    protected void handleDispatch(AbstractManagedConnection<?> conn) {

        // The connection had better be in the correct state.
        assert (conn.getStatus() == AbstractManagedConnectionStatus.ACTIVE);

        try {

            // Set up the connection and simulate deferred writes.
            conn.registerKey(this.selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            conn.doBind();

            debug("Received dispatch [%s].", conn);

        } catch (Throwable t) {

            handleError(conn, t);
        }
    }

    /**
     * Handles a query for the list of connections.
     */
    protected void handleQueryConnections(RequestFuture<List<AbstractManagedConnection<?>>> future) {

        List<AbstractManagedConnection<?>> res = new ArrayList<AbstractManagedConnection<?>>();

        for (SelectionKey key : this.selector.keys()) {

            Object attachment = key.attachment();

            if (attachment instanceof AbstractManagedConnection<?>) {
                res.add((AbstractManagedConnection<?>) attachment);
            }
        }

        future.set(res);
    }

    @Transition(currentState = "ACTIVE", eventType = "DISPATCH")
    final Handler<InterestEvent<RequestFuture<Object>>> dispatchHandler = new Handler<InterestEvent<RequestFuture<Object>>>() {

        public void handle(InterestEvent<RequestFuture<Object>> evt) {
            handleDispatch(((ProxySource<?>) evt.getSource()).getConnection());
        }
    };

    @Transition(currentState = "ACTIVE", eventType = "OP")
    final Handler<InterestEvent<Integer>> opHandler = new Handler<InterestEvent<Integer>>() {

        public void handle(InterestEvent<Integer> evt) {

            int opMask = evt.getArgument();
            handleOp(((ProxySource<?>) evt.getSource()).getConnection(), //
                    opMask & 0x7FFFFFFF, (opMask & 0x80000000) != 0);
        }
    };

    @Transition(currentState = "ACTIVE", eventType = "EXECUTE")
    final Handler<InterestEvent<Runnable>> executeHandler = new Handler<InterestEvent<Runnable>>() {

        public void handle(InterestEvent<Runnable> evt) {
            handleExecute(((ProxySource<?>) evt.getSource()).getConnection(), evt.getArgument());
        }
    };

    @Transition(currentState = "ACTIVE", eventType = "CLOSE")
    final Handler<InterestEvent<?>> closeHandler = new Handler<InterestEvent<?>>() {

        public void handle(InterestEvent<?> evt) {
            handleClosingUser(((ProxySource<?>) evt.getSource()).getConnection());
        }
    };

    @Transitions(transitions = {
    //
            @Transition(currentState = "ACTIVE", eventType = "ERROR"), //
            @Transition(currentState = "CLOSING", eventType = "ERROR") //
    })
    final Handler<InterestEvent<Throwable>> errorHandler = new Handler<InterestEvent<Throwable>>() {

        public void handle(InterestEvent<Throwable> evt) {
            handleError(((ProxySource<?>) evt.getSource()).getConnection(), evt.getArgument());
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

    final ConnectionManagerDispatchThread parent;

    /**
     * Default constructor.
     */
    protected ConnectionManagerIOThread(String name, ConnectionManagerDispatchThread parent) {
        super(name);

        this.parent = parent;
    }

    @Override
    protected void purge(AbstractManagedConnection<?> conn) {
        // Do nothing.
    }
}

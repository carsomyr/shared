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
     * Handles a connection dispatch notification.
     */
    protected void handleDispatch(AbstractManagedConnection<?> conn) {

        // The connection had better be in the correct state.
        assert (conn.getStatus() == AbstractManagedConnectionStatus.ACTIVE);

        try {

            // Set up the connection and simulate deferred writes.
            conn.registerKey(this.selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            conn.doBind();

            debug("Received as dispatch [%s].", conn);

        } catch (Throwable t) {

            handleError(conn, t);
        }
    }

    /**
     * Handles a request to get the list of connections.
     */
    protected void handleGetConnections(RequestFuture<List<AbstractManagedConnection<?>>> future) {

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
    final Handler<InterestEvent<?>> dispatchHandler = new Handler<InterestEvent<?>>() {

        @Override
        public void handle(InterestEvent<?> evt) {
            handleDispatch(((ProxySource<?>) evt.getSource()).getConnection());
        }
    };

    @Transition(currentState = "ACTIVE", eventType = "OP")
    final Handler<InterestEvent<Integer>> opHandler = new Handler<InterestEvent<Integer>>() {

        @Override
        public void handle(InterestEvent<Integer> evt) {

            int opMask = evt.getArgument();
            handleOp(((ProxySource<?>) evt.getSource()).getConnection(), //
                    opMask & 0x7FFFFFFF, (opMask & 0x80000000) != 0);
        }
    };

    @Transition(currentState = "ACTIVE", eventType = "CLOSE")
    final Handler<InterestEvent<?>> closeHandler = new Handler<InterestEvent<?>>() {

        @Override
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

        @Override
        public void handle(InterestEvent<Throwable> evt) {
            handleError(((ProxySource<?>) evt.getSource()).getConnection(), evt.getArgument());
        }
    };

    @Transition(currentState = "ACTIVE", eventType = "EXECUTE")
    final Handler<InterestEvent<Runnable>> executeHandler = new Handler<InterestEvent<Runnable>>() {

        @Override
        public void handle(InterestEvent<Runnable> evt) {
            handleExecute(((ProxySource<?>) evt.getSource()).getConnection(), evt.getArgument());
        }
    };

    @Transition(currentState = "RUN", eventType = "GET_CONNECTIONS", group = "internal")
    final Handler<InterestEvent<RequestFuture<List<AbstractManagedConnection<?>>>>> getConnectionsHandler = //
    new Handler<InterestEvent<RequestFuture<List<AbstractManagedConnection<?>>>>>() {

        @Override
        public void handle(InterestEvent<RequestFuture<List<AbstractManagedConnection<?>>>> evt) {
            handleGetConnections(evt.getArgument());
        }
    };

    @Transition(currentState = "RUN", eventType = "SHUTDOWN", group = "internal")
    final Handler<InterestEvent<?>> shutdownHandler = new Handler<InterestEvent<?>>() {

        @Override
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

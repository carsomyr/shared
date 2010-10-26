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

import static shared.net.nio.NioEvent.NioEventType.SHUTDOWN;

import java.nio.channels.SelectionKey;
import java.util.ArrayList;
import java.util.List;

import shared.event.Handler;
import shared.event.Transitions;
import shared.event.Transitions.Transition;
import shared.net.nio.NioConnection.NioConnectionStatus;

/**
 * A specialized {@link NioManagerThread} that reads from and writes to connections.
 * 
 * @author Roy Liu
 */
public class NioManagerIoThread extends NioManagerThread {

    @Override
    protected void onStart() {
        initFsms();
    }

    @Override
    protected void onStop() {

        // Tell the bad news to all registered connections.
        for (SelectionKey key : this.selector.keys()) {

            Object attachment = key.attachment();

            if (attachment instanceof NioConnection) {
                handleError((NioConnection) attachment, this.exception);
            }
        }

        this.parent.onLocal(new NioEvent<Object>(SHUTDOWN, null));
    }

    @Override
    protected void doReadyOps(int readyOps, SelectionKey key) {

        NioConnection conn = (NioConnection) key.attachment();

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
    protected void handleDispatch(NioConnection conn) {

        // The connection had better be in the correct state.
        assert (conn.getStatus() == NioConnectionStatus.ACTIVE);

        try {

            // Set up the connection and simulate deferred writes.
            conn.registerKey(this.selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);

            debug("[%s] received as dispatch.", conn);

        } catch (Throwable t) {

            handleError(conn, t);
        }
    }

    /**
     * Handles a request to get the list of connections.
     */
    protected void handleGetConnections(Request<?, List<NioConnection>> request) {

        List<NioConnection> res = new ArrayList<NioConnection>();

        for (SelectionKey key : this.selector.keys()) {

            Object attachment = key.attachment();

            if (attachment instanceof NioConnection) {
                res.add((NioConnection) attachment);
            }
        }

        request.set(res);
    }

    @Transition(currentState = "ACTIVE", eventType = "DISPATCH")
    final Handler<NioEvent<?>> dispatchHandler = new Handler<NioEvent<?>>() {

        @Override
        public void handle(NioEvent<?> evt) {
            handleDispatch((NioConnection) evt.getSource());
        }
    };

    @Transition(currentState = "ACTIVE", eventType = "OP")
    final Handler<NioEvent<Integer>> opHandler = new Handler<NioEvent<Integer>>() {

        @Override
        public void handle(NioEvent<Integer> evt) {

            int opMask = evt.getArgument();
            handleOp((NioConnection) evt.getSource(), opMask & 0x7FFFFFFF, (opMask & 0x80000000) != 0);
        }
    };

    @Transition(currentState = "ACTIVE", eventType = "CLOSE")
    final Handler<NioEvent<?>> closeHandler = new Handler<NioEvent<?>>() {

        @Override
        public void handle(NioEvent<?> evt) {
            handleClosingUser((NioConnection) evt.getSource());
        }
    };

    @Transitions(transitions = {
            //
            @Transition(currentState = "ACTIVE", eventType = "ERROR"), //
            @Transition(currentState = "CLOSING", eventType = "ERROR") //
    })
    final Handler<NioEvent<Throwable>> errorHandler = new Handler<NioEvent<Throwable>>() {

        @Override
        public void handle(NioEvent<Throwable> evt) {
            handleError((NioConnection) evt.getSource(), evt.getArgument());
        }
    };

    @Transition(currentState = "ACTIVE", eventType = "EXECUTE")
    final Handler<NioEvent<Runnable>> executeHandler = new Handler<NioEvent<Runnable>>() {

        @Override
        public void handle(NioEvent<Runnable> evt) {
            handleExecute((NioConnection) evt.getSource(), evt.getArgument());
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

    @Transition(currentState = "RUN", eventType = "SHUTDOWN", group = "internal")
    final Handler<NioEvent<?>> shutdownHandler = new Handler<NioEvent<?>>() {

        @Override
        public void handle(NioEvent<?> evt) {
            handleShutdown();
        }
    };

    final NioManagerDispatchThread parent;

    /**
     * Default constructor.
     */
    protected NioManagerIoThread(String name, NioManagerDispatchThread parent) {
        super(name);

        this.parent = parent;
    }

    @Override
    protected void purge(NioConnection conn) {
        // Do nothing.
    }
}

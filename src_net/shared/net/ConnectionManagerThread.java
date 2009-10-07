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

import static shared.net.InterestEvent.InterestEventType.SHUTDOWN;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Collections;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import shared.event.EnumStatus;
import shared.event.Source;
import shared.event.SourceLocal;
import shared.event.StateTable;
import shared.net.AbstractManagedConnection.AbstractManagedConnectionStatus;
import shared.net.AcceptRegistry.Entry;
import shared.net.InterestEvent.InterestEventType;
import shared.util.Control;
import shared.util.CoreThread;
import shared.util.RequestFuture;

/**
 * An abstract base class for {@link ConnectionManager} service threads.
 * 
 * @apiviz.owns shared.net.ConnectionManagerThread.ConnectionManagerThreadStatus
 * @apiviz.has shared.net.InterestEvent - - - event
 * @author Roy Liu
 */
abstract public class ConnectionManagerThread //
        extends CoreThread //
        implements SourceLocal<InterestEvent<?>>, Closeable, //
        EnumStatus<ConnectionManagerThread.ConnectionManagerThreadStatus> {

    /**
     * An enumeration of thread states.
     */
    public enum ConnectionManagerThreadStatus {

        /**
         * The thread is running.
         */
        RUN, //

        /**
         * The thread is in the process of shutting down.
         */
        CLOSING, //

        /**
         * The thread has shut down.
         */
        CLOSED;
    }

    /**
     * Enqueues the given event and wakes this thread up from a possible {@link Selector#select()}.
     */
    public void onLocal(InterestEvent<?> evt) {

        this.queue.add(evt);
        this.selector.wakeup();
    }

    /**
     * Creates a human-readable representation of this thread that includes the name and status.
     */
    @Override
    public String toString() {
        return String.format("%s[%s]", //
                getName(), this.status);
    }

    public ConnectionManagerThreadStatus getStatus() {
        return this.status;
    }

    public void setStatus(ConnectionManagerThreadStatus status) {
        this.status = status;
    }

    /**
     * Runs the main operation readiness and event processing loop.
     */
    @Override
    protected void runUnchecked() {

        debug("Started.");

        onStart();

        outer: for (; getStatus() == ConnectionManagerThreadStatus.RUN;) {

            try {

                this.selector.select();

            } catch (IOException e) {

                debug("Caught unexpected exception while in select() (%s).", e.getMessage());

                continue outer;
            }

            // Iterate over the selected key set.
            inner: for (Iterator<SelectionKey> itr = this.selector.selectedKeys().iterator(); itr.hasNext();) {

                SelectionKey key = itr.next();

                int readyOps;

                try {

                    readyOps = key.readyOps();

                } catch (CancelledKeyException e) {

                    // Canceled? OK, just ignore and continue.
                    continue inner;
                }

                doReadyOps(readyOps, key);

                // Signify that we've examined the currently selected connection.
                itr.remove();
            }

            // Keep polling for internal events.
            for (InterestEvent<?> evt; (evt = this.queue.poll()) != null;) {

                ProxySource<?> stub = (ProxySource<?>) evt.getSource();

                if (stub == null) {

                    this.fsmInternal.lookup(this, evt);

                } else {

                    ConnectionManagerThread connThread = stub.getConnection().getThread();

                    // The event was indeed intended for this thread.
                    if (this == connThread) {

                        this.fsm.lookup(stub.getConnection(), evt);

                    }
                    // The event needs to be redirected to another thread.
                    else {

                        connThread.onLocal(evt);
                    }
                }
            }
        }
    }

    /**
     * Sets the status to {@link ConnectionManagerThreadStatus#CLOSING}, since user code didn't explicitly call
     * {@link #close()}.
     */
    @Override
    protected void runCatch(Throwable t) {

        synchronized (this) {
            setStatus(ConnectionManagerThreadStatus.CLOSING);
        }

        this.exception = t;

        Control.rethrow(t);
    }

    /**
     * Releases any currently held resources.
     */
    @Override
    protected void runFinalizer() {

        // Everyone who has a request pending will get an error.
        for (InterestEvent<?> evt; (evt = this.queue.poll()) != null;) {

            Source<InterestEvent<?>, SourceType> source = evt.getSource();

            // If the source is not null, then it must be a proxy for a connection.
            if (source != null) {
                handleError(((ProxySource<?>) source).getConnection(), this.exception);
            }
        }

        // Perform a dummy select() to get rid of canceled keys.
        try {

            this.selector.selectNow();

        } catch (IOException e) {

            // Ah well.
        }

        // Tell the bad news to all registered connections.
        for (SelectionKey key : this.selector.keys()) {

            Object attachment = key.attachment();

            if (attachment instanceof AbstractManagedConnection<?>) {

                handleError((AbstractManagedConnection<?>) attachment, this.exception);

            } else if (attachment instanceof Entry) {

                Set<AbstractManagedConnection<?>> pending = ((Entry) attachment).getPending();

                for (; !pending.isEmpty();) {
                    handleError(pending.iterator().next(), this.exception);
                }
            }
        }

        // Finally, close the selector.
        try {

            this.selector.close();

        } catch (IOException e) {

            // Ah well.
        }

        onStop();

        // Notify anyone calling close().
        synchronized (this) {

            for (RequestFuture<?> future : this.futures) {
                future.setException(this.exception);
            }

            setStatus(ConnectionManagerThreadStatus.CLOSED);
            notifyAll();
        }

        debug("Stopped.");
    }

    /**
     * Stops this thread and waits for it to terminate.
     */
    public void close() {

        onLocal(new InterestEvent<Object>(SHUTDOWN, null));

        synchronized (this) {

            for (; getStatus() != ConnectionManagerThreadStatus.CLOSED;) {

                try {

                    wait();

                } catch (InterruptedException e) {

                    // Ignore. Keep on waiting.
                }
            }
        }
    }

    /**
     * Initializes the underlying {@link StateTable}s.
     */
    protected void initFSMs() {

        this.fsm = new StateTable<AbstractManagedConnectionStatus, InterestEventType, InterestEvent<?>>( //
                this, AbstractManagedConnectionStatus.class, InterestEventType.class);
        this.fsmInternal = new StateTable<ConnectionManagerThreadStatus, InterestEventType, InterestEvent<?>>( //
                this, ConnectionManagerThreadStatus.class, InterestEventType.class, "internal");
    }

    /**
     * Logs a debugging message.
     */
    protected void debug(String format, Object... args) {

        if (this.log.isDebugEnabled()) {
            this.log.debug(String.format(format, args));
        }
    }

    /**
     * Logs a debugging error message.
     */
    protected void debug(Throwable error, String format, Object... args) {

        if (this.log.isDebugEnabled()) {
            this.log.debug(String.format(format, args), error);
        }
    }

    /**
     * Queries the internal state. Blocks until query completion.
     * 
     * @param type
     *            the {@link InterestEventType}.
     * @param <T>
     *            the query result type.
     */
    protected <T> T query(InterestEventType type) {

        RequestFuture<T> fut = new RequestFuture<T>();

        onLocal(new InterestEvent<RequestFuture<T>>(type, fut, null));

        synchronized (this) {

            Control.checkTrue(getStatus() == ConnectionManagerThreadStatus.RUN, //
                    "The connection manager thread has exited");

            this.futures.add(fut);
        }

        try {

            return fut.get();

        } catch (Exception e) {

            throw new RuntimeException(e);
        }
    }

    /**
     * On thread start.
     */
    abstract protected void onStart();

    /**
     * On thread stop.
     */
    abstract protected void onStop();

    /**
     * Performs actions stipulated by a ready operations bit vector on the given {@link SelectionKey}.
     */
    abstract protected void doReadyOps(int readyOps, SelectionKey key);

    /**
     * A sequence of actions to take when deleting a connection, as far as this thread is concerned.
     */
    abstract protected void purge(AbstractManagedConnection<?> conn);

    //

    /**
     * Handles a connection operation interest change request.
     */
    protected void handleOp(AbstractManagedConnection<?> conn, int mask, boolean enabled) {

        assert !Thread.holdsLock(conn);

        try {

            conn.doOp(mask, enabled);

        } catch (Throwable t) {

            handleError(conn, t);
        }
    }

    /**
     * Handles an asynchronous execution request.
     */
    protected void handleExecute(AbstractManagedConnection<?> conn, Runnable r) {

        assert !Thread.holdsLock(conn);

        try {

            r.run();

        } catch (Throwable t) {

            handleError(conn, t);
        }
    }

    /**
     * Handles a connection end-of-stream.
     */
    protected void handleClosingEOS(AbstractManagedConnection<?> conn) {

        assert !Thread.holdsLock(conn);

        try {

            conn.doClosing(conn.onClosingEOSHandler);

            debug("EOS [%s].", conn);

            conn.setStatus(AbstractManagedConnectionStatus.CLOSING);

        } catch (Throwable t) {

            handleError(conn, t);
        }
    }

    /**
     * Handles a connection close request.
     */
    protected void handleClosingUser(AbstractManagedConnection<?> conn) {

        assert !Thread.holdsLock(conn);

        try {

            conn.doClosing(conn.onClosingUserHandler);

            debug("Close [%s].", conn);

            conn.setStatus(AbstractManagedConnectionStatus.CLOSING);

        } catch (Throwable t) {

            handleError(conn, t);
        }
    }

    /**
     * Handles a connection closure.
     */
    protected void handleClose(AbstractManagedConnection<?> conn) {

        assert !Thread.holdsLock(conn);

        try {

            conn.doClose();

            debug("Closed [%s].", conn);

        } catch (Throwable t) {

            debug(t, "Ignored error [%s].", conn);
        }

        purge(conn);

        conn.setStatus(AbstractManagedConnectionStatus.CLOSED);
    }

    /**
     * Handles a connection error.
     */
    protected void handleError(AbstractManagedConnection<?> conn, Throwable error) {

        // Connection already invalidated. Nothing to do.
        if (conn.getStatus() == AbstractManagedConnectionStatus.CLOSED) {
            return;
        }

        assert !Thread.holdsLock(conn);

        try {

            conn.doError(error);

            debug(error, "Error [%s].", conn);

        } catch (Throwable t) {

            debug(t, "Ignored error [%s].", conn);
        }

        try {

            conn.doClose();

            debug("Closed [%s].", conn);

        } catch (Throwable t) {

            debug(t, "Ignored error [%s].", conn);
        }

        purge(conn);

        conn.setStatus(AbstractManagedConnectionStatus.CLOSED);
    }

    /**
     * Handles a shutdown request.
     */
    protected void handleShutdown() {

        synchronized (this) {
            setStatus(ConnectionManagerThreadStatus.CLOSING);
        }
    }

    //

    /**
     * A {@link Selector} specific to this thread.
     */
    final protected Selector selector;

    /**
     * The event queue.
     */
    final protected Queue<InterestEvent<?>> queue;

    /**
     * A weak {@link Set} of {@link RequestFuture}s associated with external, asynchronous requests.
     */
    final protected Set<RequestFuture<?>> futures;

    /**
     * The {@link Logger} for debugging messages.
     */
    final protected Logger log;

    /**
     * The external state machine.
     */
    protected StateTable<AbstractManagedConnectionStatus, InterestEventType, InterestEvent<?>> fsm;

    /**
     * The internal state machine.
     */
    protected StateTable<ConnectionManagerThreadStatus, InterestEventType, InterestEvent<?>> fsmInternal;

    /**
     * The exception that caused this thread to exit.
     */
    protected Throwable exception;

    /**
     * The current {@link ConnectionManagerThreadStatus}.
     */
    protected ConnectionManagerThreadStatus status;

    /**
     * Default constructor.
     */
    protected ConnectionManagerThread(String name) {
        super(name);

        try {

            this.selector = Selector.open();

        } catch (IOException e) {

            throw new IllegalStateException(e);
        }

        this.queue = new LinkedBlockingQueue<InterestEvent<?>>();
        this.futures = Collections.newSetFromMap(new WeakHashMap<RequestFuture<?>, Boolean>());
        this.exception = new IllegalStateException("The connection manager thread has exited");

        this.log = LoggerFactory.getLogger(String.format("%s.%s", ConnectionManager.class.getName(), name));

        this.fsm = null;
        this.fsmInternal = null;

        this.status = ConnectionManagerThreadStatus.RUN;
    }
}
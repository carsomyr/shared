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
import shared.net.Connection.ClosingType;
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
abstract public class ConnectionManagerThread extends CoreThread //
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
    @Override
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

    @Override
    public ConnectionManagerThreadStatus getStatus() {
        return this.status;
    }

    @Override
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

                final int readyOps;

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

        // Perform a dummy Selector#select to get rid of canceled keys.
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

        // Notify anyone calling #close.
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
    @Override
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
     * Logs a debugging exception message.
     */
    protected void debug(Throwable exception, String format, Object... args) {

        if (this.log.isDebugEnabled()) {
            this.log.debug(String.format(format, args), exception);
        }
    }

    /**
     * Retrieves this thread's internal state. Blocks until request completion.
     * 
     * @param type
     *            the {@link InterestEventType}.
     * @param <T>
     *            the result type.
     */
    protected <T> T request(InterestEventType type) {

        RequestFuture<T> fut = new RequestFuture<T>();

        onLocal(new InterestEvent<RequestFuture<T>>(type, fut, null));

        synchronized (this) {

            Control.checkTrue(getStatus() == ConnectionManagerThreadStatus.RUN, //
                    "The connection manager thread has exited");

            this.futures.add(fut);
        }

        try {

            return fut.get();

        } catch (RuntimeException e) {

            throw e;

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

        try {

            conn.doOp(mask, enabled);

        } catch (Throwable t) {

            handleError(conn, t);
        }
    }

    /**
     * Handles a connection close request.
     */
    protected void handleClosingUser(AbstractManagedConnection<?> conn) {

        try {

            conn.doClosing(ClosingType.USER);

            debug("Close [%s].", conn);

            conn.setStatus(AbstractManagedConnectionStatus.CLOSING);

        } catch (Throwable t) {

            handleError(conn, t);
        }
    }

    /**
     * Handles a connection end-of-stream notification.
     */
    protected void handleClosingEOS(AbstractManagedConnection<?> conn) {

        try {

            conn.doClosing(ClosingType.EOS);

            debug("EOS [%s].", conn);

            conn.setStatus(AbstractManagedConnectionStatus.CLOSING);

        } catch (Throwable t) {

            handleError(conn, t);
        }
    }

    /**
     * Handles a connection closure notification.
     */
    protected void handleClose(AbstractManagedConnection<?> conn) {

        try {

            conn.doClose();

            debug("Closed [%s].", conn);

        } catch (Throwable t) {

            debug(t, "Ignored exception [%s].", conn);
        }

        purge(conn);

        conn.setStatus(AbstractManagedConnectionStatus.CLOSED);
    }

    /**
     * Handles a connection error notification.
     */
    protected void handleError(AbstractManagedConnection<?> conn, Throwable exception) {

        // Connection already invalidated. Nothing to do.
        if (conn.getStatus() == AbstractManagedConnectionStatus.CLOSED) {
            return;
        }

        try {

            conn.doError(exception);

            debug(exception, "Error [%s].", conn);

        } catch (Throwable t) {

            debug(t, "Ignored exception [%s].", conn);
        }

        try {

            conn.doClose();

            debug("Closed [%s].", conn);

        } catch (Throwable t) {

            debug(t, "Ignored exception [%s].", conn);
        }

        purge(conn);

        conn.setStatus(AbstractManagedConnectionStatus.CLOSED);
    }

    /**
     * Handles a request to execute code on this thread.
     */
    protected void handleExecute(AbstractManagedConnection<?> conn, Runnable r) {

        try {

            r.run();

        } catch (Throwable t) {

            handleError(conn, t);
        }
    }

    /**
     * Handles a request to shut down this thread.
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
     * A weak {@link Set} of {@link RequestFuture}s for cleanup purposes.
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

            throw new RuntimeException(e);
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

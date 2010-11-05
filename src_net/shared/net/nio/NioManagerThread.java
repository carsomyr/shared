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

package shared.net.nio;

import static shared.net.nio.NioEvent.NioEventType.SHUTDOWN;

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
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import shared.event.EnumStatus;
import shared.event.SourceLocal;
import shared.event.StateTable;
import shared.net.ConnectionHandler.ClosingType;
import shared.net.nio.NioConnection.NioConnectionStatus;
import shared.net.nio.NioEvent.NioEventType;
import shared.util.Control;
import shared.util.CoreThread;

/**
 * An abstract base class for {@link NioManager} service threads.
 * 
 * @apiviz.owns shared.net.nio.NioManagerThread.NioManagerThreadStatus
 * @apiviz.has shared.net.nio.NioEvent - - - event
 * @author Roy Liu
 */
abstract public class NioManagerThread extends CoreThread //
        implements SourceLocal<NioEvent<?>>, Closeable, EnumStatus<NioManagerThread.NioManagerThreadStatus> {

    /**
     * An enumeration of thread states.
     */
    public enum NioManagerThreadStatus {

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
    public void onLocal(NioEvent<?> evt) {

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
    public NioManagerThreadStatus getStatus() {
        return this.status;
    }

    @Override
    public void setStatus(NioManagerThreadStatus status) {
        this.status = status;
    }

    /**
     * Runs the main operation readiness and event processing loop.
     */
    @Override
    protected void doRun() {

        debug("Started.");

        onStart();

        outer: for (; getStatus() == NioManagerThreadStatus.RUN;) {

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
            for (NioEvent<?> evt; (evt = this.queue.poll()) != null;) {

                NioConnection conn = (NioConnection) evt.getSource();

                if (conn == null) {

                    this.fsmInternal.lookup(this, evt);

                } else {

                    // Note: Reading a stale thread reference is OK, since only this thread is capable of reassigning
                    // any connection's thread reference currently pointing to it. As a consequence, any events
                    // originating from such connections are really meant for this thread. On the other hand, any events
                    // originating from connections not referencing this thread are forwarded to their putative
                    // destination threads. Observe that even if these destinations are read from stale thread
                    // references, the forwarding scheme is eventually correct -- Events will, after an indeterminate
                    // number of forwarding hops, reach their intended destinations.
                    //
                    // This explanation may be a bit overwrought for a simple, single hop handoff, but it's better to be
                    // paranoid than to suffer from subtle, once-in-a-billion race conditions.
                    NioManagerThread connThread = conn.getThread();

                    // The event was indeed intended for this thread.
                    if (this == connThread) {

                        this.fsm.lookup(conn, evt);

                    }
                    // The event needs to be redirected to another thread.
                    else {

                        connThread.onLocal(evt);
                    }
                }
            }
        }

        this.exception = new IllegalStateException("The connection manager thread has exited");
    }

    /**
     * Sets the status to {@link NioManagerThreadStatus#CLOSING}, since user code didn't explicitly call
     * {@link #close()}.
     */
    @Override
    protected void doCatch(Throwable t) {

        synchronized (this) {
            setStatus(NioManagerThreadStatus.CLOSING);
        }

        this.exception = new IllegalStateException("The connection manager thread has encountered " //
                + "an unexpected exception", t);
    }

    /**
     * Releases any currently held resources.
     */
    @Override
    protected void doFinally() {

        // Everyone who has a request pending will get an error.
        for (NioEvent<?> evt; (evt = this.queue.poll()) != null;) {

            NioConnection conn = (NioConnection) evt.getSource();

            // If the source is not null, then it must be a connection.
            if (conn != null) {
                handleError(conn, this.exception);
            }
        }

        // Perform a dummy Selector#select to get rid of canceled keys.
        try {

            this.selector.selectNow();

        } catch (IOException e) {

            // Ah well.
        }

        onStop();

        // Finally, close the selector.
        try {

            this.selector.close();

        } catch (IOException e) {

            // Ah well.
        }

        // Notify anyone calling #close.
        synchronized (this) {

            for (Request<?, ?> request : this.requests) {
                request.setException(this.exception);
            }

            setStatus(NioManagerThreadStatus.CLOSED);
            notifyAll();
        }

        debug("Stopped.");
    }

    /**
     * Stops this thread and waits for it to terminate.
     */
    @Override
    public void close() {

        onLocal(new NioEvent<Object>(SHUTDOWN, null));

        synchronized (this) {

            for (; getStatus() != NioManagerThreadStatus.CLOSED;) {

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
    protected void initFsms() {

        this.fsm = new StateTable<NioConnectionStatus, NioEventType, NioEvent<?>>( //
                this, NioConnectionStatus.class, NioEventType.class);
        this.fsmInternal = new StateTable<NioManagerThreadStatus, NioEventType, NioEvent<?>>( //
                this, NioManagerThreadStatus.class, NioEventType.class, "internal");
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
     *            the {@link NioEventType}.
     * @param argument
     *            the input argument.
     * @param conn
     *            the originating {@link NioConnection}.
     * @param <I>
     *            the input type.
     * @param <O>
     *            the output type.
     */
    protected <I, O> Future<O> request(NioEventType type, I argument, NioConnection conn) {

        Request<I, O> request = new Request<I, O>(argument);

        onLocal(new NioEvent<Request<I, O>>(type, request, conn));

        synchronized (this) {

            Control.checkTrue(getStatus() == NioManagerThreadStatus.RUN, //
                    "The connection manager thread has exited");

            this.requests.add(request);
        }

        return request;
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
    abstract protected void purge(NioConnection conn);

    //

    /**
     * Handles a connection operation interest change request.
     */
    protected void handleOp(NioConnection conn, int mask, boolean enabled) {

        try {

            conn.doOp(mask, enabled);

        } catch (Throwable t) {

            handleError(conn, t);
        }
    }

    /**
     * Handles a connection close request.
     */
    protected void handleClosingUser(NioConnection conn) {

        try {

            conn.doClosing(ClosingType.USER);

            debug("[%s] close.", conn);

            conn.setStatus(NioConnectionStatus.CLOSING);

        } catch (Throwable t) {

            handleError(conn, t);
        }
    }

    /**
     * Handles a connection end-of-stream notification.
     */
    protected void handleClosingEos(NioConnection conn) {

        try {

            conn.doClosing(ClosingType.EOS);

            debug("[%s] EOS.", conn);

            conn.setStatus(NioConnectionStatus.CLOSING);

        } catch (Throwable t) {

            handleError(conn, t);
        }
    }

    /**
     * Handles a connection closure notification.
     */
    protected void handleClose(NioConnection conn) {

        try {

            debug("[%s] closed.", conn);

            conn.doClose();

        } catch (Throwable t) {

            debug(t, "[%s] ignored exception.", conn);
        }

        purge(conn);

        conn.setStatus(NioConnectionStatus.CLOSED);
    }

    /**
     * Handles a connection error notification.
     */
    protected void handleError(NioConnection conn, Throwable exception) {

        // Connection already invalidated. Nothing to do.
        if (conn.getStatus() == NioConnectionStatus.CLOSED) {
            return;
        }

        try {

            debug(exception, "[%s] error.", conn);

            conn.doError(exception);

        } catch (Throwable t) {

            debug(t, "[%s] ignored exception.", conn);
        }

        try {

            debug("[%s] closed.", conn);

            conn.doClose();

        } catch (Throwable t) {

            debug(t, "[%s] ignored exception.", conn);
        }

        purge(conn);

        conn.setStatus(NioConnectionStatus.CLOSED);
    }

    /**
     * Handles a request to execute code on this thread.
     * 
     * @param <V>
     *            the result type.
     */
    protected <V> void handleInvoke(Request<Callable<V>, V> request) {

        try {

            request.set(request.argument.call());

        } catch (Throwable t) {

            request.setException(t);
        }
    }

    /**
     * Handles a request to shut down this thread.
     */
    protected void handleShutdown() {

        synchronized (this) {
            setStatus(NioManagerThreadStatus.CLOSING);
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
    final protected Queue<NioEvent<?>> queue;

    /**
     * A weak {@link Set} of {@link Request}s for cleanup purposes.
     */
    final protected Set<Request<?, ?>> requests;

    /**
     * The {@link Logger} for debugging messages.
     */
    final protected Logger log;

    /**
     * The external state machine.
     */
    protected StateTable<NioConnectionStatus, NioEventType, NioEvent<?>> fsm;

    /**
     * The internal state machine.
     */
    protected StateTable<NioManagerThreadStatus, NioEventType, NioEvent<?>> fsmInternal;

    /**
     * The exception that caused this thread to exit.
     */
    protected Throwable exception;

    /**
     * The current {@link NioManagerThreadStatus}.
     */
    protected NioManagerThreadStatus status;

    /**
     * Default constructor.
     */
    protected NioManagerThread(String name) {
        super(name);

        try {

            this.selector = Selector.open();

        } catch (IOException e) {

            throw new RuntimeException(e);
        }

        this.queue = new LinkedBlockingQueue<NioEvent<?>>();
        this.requests = Collections.newSetFromMap(new WeakHashMap<Request<?, ?>, Boolean>());
        this.log = LoggerFactory.getLogger(String.format("%s.%s", NioManager.class.getName(), name));

        this.fsm = null;
        this.fsmInternal = null;
        this.exception = null;

        this.status = NioManagerThreadStatus.RUN;
    }

    /**
     * A subclass of {@link FutureTask} for signaling manager threads and retrieving their internal states.
     * 
     * @param <I>
     *            the input type.
     * @param <O>
     *            the output type.
     */
    public static class Request<I, O> extends FutureTask<O> {

        /**
         * A null {@link Runnable} that has an empty {@link Runnable#run()} method.
         */
        final protected static Runnable nullRunnable = new Runnable() {

            @Override
            public void run() {
            }
        };

        final I argument;

        /**
         * Default constructor.
         */
        public Request(I argument) {
            super(nullRunnable, null);

            this.argument = argument;
        }

        /**
         * Gets the input argument.
         */
        public I getArgument() {
            return this.argument;
        }

        /**
         * Gives public visibility to the {@link #set(Object)} method.
         */
        @Override
        public void set(O v) {
            super.set(v);
        }

        /**
         * Gives public visibility to the {@link #setException(Throwable)} method.
         */
        @Override
        public void setException(Throwable t) {
            super.setException(t);
        }
    }
}

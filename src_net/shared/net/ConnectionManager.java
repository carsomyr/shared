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

import static shared.net.Constants.DEFAULT_BACKLOG_SIZE;
import static shared.net.InterestEvent.InterestEventType.QUERY_BOUND_ADDRESSES;
import static shared.net.InterestEvent.InterestEventType.QUERY_CONNECTIONS;

import java.io.Closeable;
import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.util.List;

import shared.net.ConnectionManagerThread.ConnectionManagerThreadStatus;
import shared.net.InterestEvent.InterestEventType;
import shared.util.Control;

/**
 * A transparent, asynchronous sockets layer for easy network programming that performs readiness selection on
 * {@link AbstractManagedConnection}s via callbacks.
 * 
 * @apiviz.composedOf shared.net.ConnectionManagerDispatchThread
 * @apiviz.uses shared.net.Constants
 * @author Roy Liu
 */
public class ConnectionManager implements Closeable {

    /**
     * A {@link WeakReference} to the global instance.
     */
    protected static WeakReference<ConnectionManager> DefaultInstanceRef = new WeakReference<ConnectionManager>(null);

    /**
     * A global lock for the entire class.
     */
    final protected static Object ClassLock = new Object();

    /**
     * Gets the global instance.
     */
    final public static ConnectionManager getInstance() {

        synchronized (ClassLock) {

            ConnectionManager cm = DefaultInstanceRef.get();

            if (cm != null) {

                ConnectionManagerThread cmt = cm.getThread();

                synchronized (cmt) {

                    if (cmt.getStatus() == ConnectionManagerThreadStatus.RUN) {
                        return cm;
                    }
                }
            }

            cm = new ConnectionManager("DefaultCM");

            DefaultInstanceRef = new WeakReference<ConnectionManager>(cm);

            return cm;
        }
    }

    final ConnectionManagerDispatchThread thread;

    /**
     * Default constructor.
     * 
     * @param name
     *            the name.
     * @param backlog
     *            the listen backlog size.
     */
    public ConnectionManager(String name, int backlog) {

        this.thread = new ConnectionManagerDispatchThread(name, //
                backlog, Runtime.getRuntime().availableProcessors());
        this.thread.start();
    }

    /**
     * Alternate constructor.
     * 
     * @param backlog
     *            the listen backlog size.
     */
    public ConnectionManager(int backlog) {
        this("", backlog);
    }

    /**
     * Alternate constructor.
     * 
     * @param name
     *            the name.
     */
    public ConnectionManager(String name) {
        this(name, DEFAULT_BACKLOG_SIZE);
    }

    /**
     * Alternate constructor.
     */
    public ConnectionManager() {
        this("", DEFAULT_BACKLOG_SIZE);
    }

    /**
     * Shuts down the underlying {@link ConnectionManagerThread}. Blocks until shutdown completion.
     */
    @Override
    public void close() {
        Control.close(this.thread);
    }

    /**
     * Gets the name of the underlying {@link ConnectionManagerThread}.
     */
    @Override
    public String toString() {
        return this.thread.toString();
    }

    /**
     * Gets the list of connections.
     */
    public List<AbstractManagedConnection<?>> getConnections() {
        return this.thread.query(QUERY_CONNECTIONS);
    }

    /**
     * Gets the list of bound addresses.
     */
    public List<InetSocketAddress> getBoundAddresses() {
        return this.thread.query(QUERY_BOUND_ADDRESSES);
    }

    /**
     * Gets the {@link ConnectionManagerDispatchThread}.
     */
    protected ConnectionManagerDispatchThread getThread() {
        return this.thread;
    }

    /**
     * Initializes the given connection.
     * 
     * @param <T>
     *            the argument type.
     */
    protected <T> void initConnection(AbstractManagedConnection<?> conn, InterestEventType opType, T argument) {

        synchronized (this.thread) {

            Control.checkTrue(this.thread.getStatus() == ConnectionManagerThreadStatus.RUN, //
                    "The connection manager thread has exited");

            this.thread.onLocal(new InterestEvent<T>(opType, argument, conn.getProxy()));
        }
    }

    // A finalizer guardian for the manager thread.
    final Object threadReaper = new Object() {

        @Override
        protected void finalize() {
            Control.close(ConnectionManager.this);
        }
    };
}

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
    protected static WeakReference<ConnectionManager> DefaultInstanceRef = //
    new WeakReference<ConnectionManager>(null);

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
     *            the backlog size.
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
     *            the backlog size.
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
     * Gets the name of the underlying {@link ConnectionManagerThread}.
     */
    @Override
    public String toString() {
        return this.thread.toString();
    }

    /**
     * Shuts down the underlying {@link ConnectionManagerThread}. Blocks until shutdown completion.
     */
    public void close() {
        Control.close(this.thread);
    }

    /**
     * Gets the list of bound addresses.
     */
    public List<InetSocketAddress> getBoundAddresses() {
        return this.thread.query(QUERY_BOUND_ADDRESSES);
    }

    /**
     * Gets the list of connections.
     */
    public List<AbstractManagedConnection<?>> getConnections() {
        return this.thread.query(QUERY_CONNECTIONS);
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

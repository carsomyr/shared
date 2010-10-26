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

import static shared.net.nio.NioEvent.NioEventType.GET_BACKLOG_SIZE;
import static shared.net.nio.NioEvent.NioEventType.GET_BOUND_ADDRESSES;
import static shared.net.nio.NioEvent.NioEventType.GET_CONNECTIONS;
import static shared.net.nio.NioEvent.NioEventType.SET_BACKLOG_SIZE;

import java.io.Closeable;
import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.util.List;

import shared.net.nio.NioEvent.NioEventType;
import shared.net.nio.NioManagerThread.NioManagerThreadStatus;
import shared.util.Control;

/**
 * A transparent, asynchronous sockets layer for easy network programming that performs readiness selection on
 * {@link NioConnection}s via callbacks.
 * 
 * @apiviz.composedOf shared.net.nio.NioManagerDispatchThread
 * @apiviz.uses shared.net.Constants
 * @author Roy Liu
 */
public class NioManager implements Closeable {

    /**
     * A {@link WeakReference} to the global instance.
     */
    protected static WeakReference<NioManager> defaultInstanceRef = new WeakReference<NioManager>(null);

    /**
     * A global lock for the entire class.
     */
    final protected static Object classLock = new Object();

    /**
     * Gets the global instance.
     */
    final public static NioManager getInstance() {

        synchronized (classLock) {

            NioManager cm = defaultInstanceRef.get();

            if (cm != null) {

                NioManagerThread cmt = cm.getThread();

                synchronized (cmt) {

                    if (cmt.getStatus() == NioManagerThreadStatus.RUN) {
                        return cm;
                    }
                }
            }

            cm = new NioManager("DefaultCM");

            defaultInstanceRef = new WeakReference<NioManager>(cm);

            return cm;
        }
    }

    final NioManagerDispatchThread thread;

    /**
     * Default constructor.
     * 
     * @param name
     *            the name.
     */
    public NioManager(String name) {

        this.thread = new NioManagerDispatchThread(name, Runtime.getRuntime().availableProcessors());
        this.thread.start();
    }

    /**
     * Shuts down the underlying {@link NioManagerThread}. Blocks until shutdown completion.
     */
    @Override
    public void close() {
        Control.close(this.thread);
    }

    /**
     * Gets the name of the underlying {@link NioManagerThread}.
     */
    @Override
    public String toString() {
        return this.thread.toString();
    }

    /**
     * Gets the list of connections.
     */
    public List<NioConnection<?>> getConnections() {
        return this.thread.request(GET_CONNECTIONS, null);
    }

    /**
     * Gets the list of bound addresses.
     */
    public List<InetSocketAddress> getBoundAddresses() {
        return this.thread.request(GET_BOUND_ADDRESSES, null);
    }

    /**
     * Gets the listen backlog size.
     */
    public int getBacklogSize() {
        return (Integer) this.thread.request(GET_BACKLOG_SIZE, null);
    }

    /**
     * Sets the listen backlog size.
     */
    public NioManager setBacklogSize(int backlogSize) {

        this.thread.request(SET_BACKLOG_SIZE, backlogSize);

        return this;
    }

    /**
     * Gets the {@link NioManagerDispatchThread}.
     */
    protected NioManagerDispatchThread getThread() {
        return this.thread;
    }

    /**
     * Initializes the given connection.
     * 
     * @param <T>
     *            the argument type.
     */
    protected <T> void initConnection(NioConnection<?> conn, NioEventType opType, T argument) {

        synchronized (this.thread) {

            Control.checkTrue(this.thread.getStatus() == NioManagerThreadStatus.RUN, //
                    "The connection manager thread has exited");

            this.thread.onLocal(new NioEvent<T>(opType, argument, conn.getProxy()));
        }
    }

    // A finalizer guardian for the manager thread.
    final Object threadReaper = new Object() {

        @Override
        protected void finalize() {
            Control.close(NioManager.this);
        }
    };
}

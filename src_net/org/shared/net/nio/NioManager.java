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

package org.shared.net.nio;

import static org.shared.net.Constants.DEFAULT_BUFFER_SIZE;
import static org.shared.net.nio.NioEvent.NioEventType.ACCEPT;
import static org.shared.net.nio.NioEvent.NioEventType.CONNECT;
import static org.shared.net.nio.NioEvent.NioEventType.GET_BACKLOG_SIZE;
import static org.shared.net.nio.NioEvent.NioEventType.GET_BOUND_ADDRESSES;
import static org.shared.net.nio.NioEvent.NioEventType.GET_CONNECTIONS;
import static org.shared.net.nio.NioEvent.NioEventType.REGISTER;
import static org.shared.net.nio.NioEvent.NioEventType.SET_BACKLOG_SIZE;

import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.shared.net.ConnectionHandler;
import org.shared.net.SocketManager;
import org.shared.net.nio.NioEvent.NioEventType;
import org.shared.net.nio.NioManagerThread.NioManagerThreadStatus;

/**
 * A transparent, asynchronous sockets layer for easy network programming that performs readiness selection on
 * {@link NioConnection}s via callbacks.
 * 
 * @apiviz.composedOf org.shared.net.nio.NioManagerDispatchThread
 * @apiviz.uses org.shared.net.Constants
 * @author Roy Liu
 */
public class NioManager implements SocketManager<NioManager, NioConnection> {

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

                NioManagerThread cmt = cm.thread;

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

    int bufferSize;

    /**
     * Default constructor.
     * 
     * @param name
     *            the name.
     */
    public NioManager(String name) {

        this.thread = new NioManagerDispatchThread(name, Runtime.getRuntime().availableProcessors());
        this.thread.start();

        this.bufferSize = DEFAULT_BUFFER_SIZE;
    }

    @Override
    public <T> Future<NioConnection> init(InitializationType type, //
            ConnectionHandler<? super NioConnection> handler, T argument) {

        final NioEventType eventType;

        switch (type) {

        case CONNECT:
            eventType = CONNECT;
            break;

        case ACCEPT:
            eventType = ACCEPT;
            break;

        case REGISTER:
            eventType = REGISTER;
            break;

        default:
            throw new IllegalArgumentException("Invalid initialization type");
        }

        NioConnection conn = new NioConnection(handler, this.bufferSize, this.thread);

        synchronized (this.thread) {

            if (this.thread.getStatus() != NioManagerThreadStatus.RUN) {
                throw new IllegalStateException("The connection manager thread has exited");
            }

            this.thread.onLocal(new NioEvent<T>(eventType, argument, conn));
        }

        return conn;
    }

    @Override
    public List<NioConnection> getConnections() {

        List<NioConnection> res = new ArrayList<NioConnection>();
        Future<List<Future<List<NioConnection>>>> fut = this.thread.request(GET_CONNECTIONS, null, null);

        try {

            for (Future<List<NioConnection>> threadFuture : fut.get()) {
                res.addAll(threadFuture.get());
            }

        } catch (RuntimeException e) {

            throw e;

        } catch (Exception e) {

            throw new RuntimeException(e);
        }

        return res;
    }

    @Override
    public List<InetSocketAddress> getBoundAddresses() {

        Future<List<InetSocketAddress>> fut = this.thread.request(GET_BOUND_ADDRESSES, null, null);

        try {

            return fut.get();

        } catch (RuntimeException e) {

            throw e;

        } catch (Exception e) {

            throw new RuntimeException(e);
        }
    }

    @Override
    public int getBacklogSize() {

        Future<Integer> fut = this.thread.request(GET_BACKLOG_SIZE, null, null);

        try {

            return fut.get();

        } catch (RuntimeException e) {

            throw e;

        } catch (Exception e) {

            throw new RuntimeException(e);
        }
    }

    @Override
    public NioManager setBacklogSize(int backlogSize) {

        Future<?> fut = this.thread.request(SET_BACKLOG_SIZE, backlogSize, null);

        try {

            fut.get();

        } catch (RuntimeException e) {

            throw e;

        } catch (Exception e) {

            throw new RuntimeException(e);
        }

        return this;
    }

    @Override
    public int getBufferSize() {
        return this.bufferSize;
    }

    @Override
    public NioManager setBufferSize(int bufferSize) {

        if (bufferSize <= 0) {
            throw new IllegalArgumentException("Invalid buffer size");
        }

        this.bufferSize = bufferSize;

        return this;
    }

    @Override
    public void close() {
        this.thread.close();
    }

    @Override
    public String toString() {
        return this.thread.toString();
    }

    // A finalizer guardian for the manager thread.
    final Object threadReaper = new Object() {

        @Override
        protected void finalize() {
            NioManager.this.close();
        }
    };
}

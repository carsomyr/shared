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

package shared.net.filter;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;

import shared.util.Control;

/**
 * An implementation of {@link FilterFactory} that creates {@link SSLFilter}s.
 * 
 * @apiviz.owns shared.net.filter.SSLFilter
 * @param <C>
 *            the {@link FilteredConnection} type.
 * @author Roy Liu
 */
public class SSLFilterFactory<C extends FilteredConnection<C, ?>> //
        implements FilterFactory<ByteBuffer, ByteBuffer, C>, SSLEngineFactory<SSLFilterFactory<C>> {

    final ThreadPoolExecutor executor;

    KeyManager[] keyManagers;
    TrustManager[] trustManagers;
    SecureRandom random;
    boolean requireClientAuth;

    Mode mode;

    SSLContext context;

    /**
     * Default constructor.
     */
    public SSLFilterFactory() {

        this.executor = Control.createPool(0, Integer.MAX_VALUE, new SynchronousQueue<Runnable>(), null);

        this.keyManagers = null;
        this.trustManagers = null;
        this.random = null;
        this.requireClientAuth = false;

        this.context = null;
    }

    public SSLFilterFactory<C> setTrustManagers(TrustManager... trustManagers) {

        checkUninitialized();

        this.trustManagers = trustManagers;

        return this;
    }

    public SSLFilterFactory<C> setKeyManagers(KeyManager... keyManagers) {

        checkUninitialized();

        this.keyManagers = keyManagers;

        return this;
    }

    public SSLFilterFactory<C> setSecureRandom(SecureRandom random) {

        checkUninitialized();

        this.random = random;

        return this;
    }

    public SSLFilterFactory<C> setRequireClientAuth(boolean requireClientAuth) {

        checkUninitialized();

        this.requireClientAuth = requireClientAuth;

        return this;
    }

    public SSLFilterFactory<C> setMode(Mode mode) {

        checkUninitialized();

        this.mode = mode;

        return this;
    }

    public SSLEngine newSSLEngine() {

        if (this.context == null) {

            final SSLContext context;

            try {

                context = SSLContext.getInstance("TLS");
                context.init(this.keyManagers, this.trustManagers, this.random);

            } catch (Exception e) {

                throw new RuntimeException(e);
            }

            this.context = context;
        }

        SSLEngine res = this.context.createSSLEngine();

        Control.checkTrue(this.mode != null, //
                "Please specify the operating mode");

        switch (this.mode) {

        case CLIENT:
            res.setUseClientMode(true);
            break;

        case SERVER:
            res.setUseClientMode(false);
            res.setNeedClientAuth(this.requireClientAuth);
            break;

        default:
            throw new AssertionError("Control should never reach here");
        }

        return res;
    }

    public SSLFilter<C> newFilter(final C connection) {
        return new SSLFilter<C>(newSSLEngine(), connection, this.executor);
    }

    /**
     * Checks that this factory is uninitialized.
     */
    protected void checkUninitialized() {
        Control.checkTrue(this.context == null, //
                "SSL context is already initialized");
    }

    // A finalizer guardian for the thread pool.
    final Object poolReaper = new Object() {

        @Override
        protected void finalize() {
            SSLFilterFactory.this.executor.shutdownNow();
        }
    };
}

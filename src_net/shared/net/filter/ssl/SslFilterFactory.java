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

package shared.net.filter.ssl;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;

import shared.net.filter.FilterFactory;
import shared.net.handler.FilteredHandler;

/**
 * An implementation of {@link FilterFactory} that creates {@link SslFilter}s.
 * 
 * @apiviz.owns shared.net.filter.ssl.SslFilter
 * @author Roy Liu
 */
public class SslFilterFactory //
        implements FilterFactory<SslFilter, ByteBuffer, ByteBuffer, FilteredHandler<?, ?, ?>>, //
        SslEngineFactory<SslFilterFactory> {

    final ExecutorService executor;

    KeyManager[] keyManagers;
    TrustManager[] trustManagers;
    SecureRandom random;
    boolean requireClientAuth;

    Mode mode;

    SSLContext context;

    /**
     * Default constructor.
     */
    public SslFilterFactory() {

        this.executor = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS, //
                new SynchronousQueue<Runnable>(), //
                new ThreadFactory() {

                    @Override
                    public Thread newThread(Runnable r) {

                        Thread t = new Thread(r, "SSL Delegated Task Worker");
                        t.setDaemon(true);

                        return t;
                    }
                } //
        );

        this.keyManagers = null;
        this.trustManagers = null;
        this.random = null;
        this.requireClientAuth = false;

        this.context = null;
    }

    @Override
    public SslFilterFactory setTrustManagers(TrustManager... trustManagers) {

        checkUninitialized();

        this.trustManagers = trustManagers;

        return this;
    }

    @Override
    public SslFilterFactory setKeyManagers(KeyManager... keyManagers) {

        checkUninitialized();

        this.keyManagers = keyManagers;

        return this;
    }

    @Override
    public SslFilterFactory setSecureRandom(SecureRandom random) {

        checkUninitialized();

        this.random = random;

        return this;
    }

    @Override
    public SslFilterFactory setRequireClientAuth(boolean requireClientAuth) {

        checkUninitialized();

        this.requireClientAuth = requireClientAuth;

        return this;
    }

    @Override
    public SslFilterFactory setMode(Mode mode) {

        checkUninitialized();

        this.mode = mode;

        return this;
    }

    @Override
    public SSLEngine newSslEngine() {

        if (this.context == null) {

            final SSLContext context;

            try {

                context = SSLContext.getInstance("TLS");
                context.init(this.keyManagers, this.trustManagers, this.random);

            } catch (RuntimeException e) {

                throw e;

            } catch (Exception e) {

                throw new RuntimeException(e);
            }

            this.context = context;
        }

        SSLEngine res = this.context.createSSLEngine();

        if (this.mode == null) {
            throw new IllegalStateException("Please specify the operating mode");
        }

        switch (this.mode) {

        case CLIENT:
            res.setUseClientMode(true);
            break;

        case SERVER:
            res.setUseClientMode(false);
            res.setNeedClientAuth(this.requireClientAuth);
            break;

        default:
            throw new IllegalStateException("Invalid mode");
        }

        return res;
    }

    @Override
    public SslFilter newFilter(FilteredHandler<?, ?, ?> handler) {
        return new SslFilter(newSslEngine(), handler, this.executor);
    }

    /**
     * Checks that this factory is uninitialized.
     */
    protected void checkUninitialized() {

        if (this.context != null) {
            throw new IllegalStateException("SSL context is already initialized");
        }
    }

    // A finalizer guardian for the thread pool.
    final Object poolReaper = new Object() {

        @Override
        protected void finalize() {
            SslFilterFactory.this.executor.shutdownNow();
        }
    };
}

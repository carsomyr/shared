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

package shared.net.filter;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;

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
        implements FilterFactory<SSLFilter<C>, ByteBuffer, ByteBuffer, C>, SSLEngineFactory<SSLFilterFactory<C>> {

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
    public SSLFilterFactory() {

        this.executor = Control.createPool(0, Integer.MAX_VALUE, new SynchronousQueue<Runnable>(), null);

        this.keyManagers = null;
        this.trustManagers = null;
        this.random = null;
        this.requireClientAuth = false;

        this.context = null;
    }

    @Override
    public SSLFilterFactory<C> setTrustManagers(TrustManager... trustManagers) {

        checkUninitialized();

        this.trustManagers = trustManagers;

        return this;
    }

    @Override
    public SSLFilterFactory<C> setKeyManagers(KeyManager... keyManagers) {

        checkUninitialized();

        this.keyManagers = keyManagers;

        return this;
    }

    @Override
    public SSLFilterFactory<C> setSecureRandom(SecureRandom random) {

        checkUninitialized();

        this.random = random;

        return this;
    }

    @Override
    public SSLFilterFactory<C> setRequireClientAuth(boolean requireClientAuth) {

        checkUninitialized();

        this.requireClientAuth = requireClientAuth;

        return this;
    }

    @Override
    public SSLFilterFactory<C> setMode(Mode mode) {

        checkUninitialized();

        this.mode = mode;

        return this;
    }

    @Override
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

    @Override
    public SSLFilter<C> newFilter(C connection) {
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

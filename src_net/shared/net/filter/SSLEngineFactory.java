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

import java.security.SecureRandom;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;

/**
 * Defines a configurable factory for creating {@link SSLEngine}s.
 * 
 * @apiviz.has shared.net.filter.SSLEngineFactory.Mode - - - argument
 * @param <T>
 *            the parameterization lower bounded by {@link SSLEngineFactory} itself.
 * @author Roy Liu
 */
public interface SSLEngineFactory<T extends SSLEngineFactory<T>> {

    /**
     * An enumeration of {@link SSLEngine} operating modes.
     */
    public enum Mode {

        /**
         * Indicates the client.
         */
        CLIENT, //

        /**
         * Indicates the server.
         */
        SERVER;
    }

    /**
     * Sets the {@link KeyManager}s.
     */
    public T setKeyManagers(KeyManager... keyManagers);

    /**
     * Sets the {@link TrustManager}s.
     */
    public T setTrustManagers(TrustManager... trustManagers);

    /**
     * Sets the {@link SecureRandom} source.
     */
    public T setSecureRandom(SecureRandom random);

    /**
     * Sets the {@link SSLEngine} operating {@link Mode}.
     */
    public T setMode(Mode mode);

    /**
     * Sets whether the server should require client authorization.
     */
    public T setRequireClientAuth(boolean requireClientAuth);

    /**
     * Creates a new {@link SSLEngine}.
     */
    public SSLEngine newSSLEngine();
}

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

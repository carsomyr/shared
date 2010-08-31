/**
 * <p>
 * Copyright (c) 2008 Roy Liu<br>
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

package shared.test.net;

import java.io.IOException;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import shared.net.ConnectionManager;
import shared.net.filter.FilteredConnection;
import shared.net.filter.SSLEngineFactory.Mode;
import shared.net.filter.SSLFilter;
import shared.net.filter.SSLFilterFactory;

/**
 * A suite encompassing all networking tests.
 * 
 * @apiviz.owns shared.test.net.AsynchronousConnectionTest
 * @apiviz.owns shared.test.net.SynchronousConnectionTest
 * @author Roy Liu
 */
@RunWith(Suite.class)
@SuiteClasses(value = {
//
        AsynchronousConnectionTest.class, //
        SynchronousConnectionTest.class //
})
public class AllNetTests {

    /**
     * The {@link KeyStore} password.
     */
    final protected static String KEYSTORE_PASSWORD = "123456";

    /**
     * The {@link KeyStore} pathname.
     */
    final protected static String KEYSTORE_PATHNAME = "shared/test/net/keystore.jks";

    /**
     * The names of {@link Logger}s used in this test.
     */
    final protected static String[] LoggerNames = new String[] {
            //
            ConnectionManager.class.getName(), //
            SSLFilter.class.getName() //
    };

    /**
     * The unit test parameterizations.
     */
    final protected static Collection<Object[]> Parameterizations;

    /**
     * Whether the logging {@link Level} should be set low enough so as to enable debugging messages to appear.
     */
    final protected static boolean DEBUG;

    static {

        Properties p = new Properties();

        try {

            p.load(Thread.currentThread().getContextClassLoader() //
                    .getResourceAsStream("shared/test/net/parameters.properties"));

        } catch (IOException e) {

            throw new RuntimeException(e);
        }

        Properties pNoSSL = (Properties) p.clone();
        pNoSSL.setProperty("use_SSL", "no");
        Object[] paramsNoSSL = new Object[] { pNoSSL };

        Properties pSSL = (Properties) p.clone();
        pSSL.setProperty("use_SSL", "yes");
        Object[] paramsSSL = new Object[] { pSSL };

        Parameterizations = Arrays.asList(new Object[][] { paramsNoSSL, paramsSSL });

        DEBUG = p.getProperty("debug").equals("yes");
    }

    /**
     * Configures the Java {@link Logger} for use by {@link ConnectionManager}s.
     */
    @BeforeClass
    final public static void initClass() {

        Level debugLevel = DEBUG ? Level.DEBUG : Level.INFO;

        for (String loggerName : LoggerNames) {
            Logger.getLogger(loggerName).setLevel(debugLevel);
        }
    }

    /**
     * Derives {@link KeyManager}s from the given {@link KeyStore} class path resource.
     * 
     * @param pathname
     *            the resource pathname.
     */
    final protected static KeyManager[] getKeyManagers(String pathname, String password) {

        try {

            KeyStore keystore = KeyStore.getInstance("JKS");
            keystore.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(pathname), //
                    password.toCharArray());

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keystore, password.toCharArray());

            return kmf.getKeyManagers();

        } catch (Exception e) {

            throw new RuntimeException(e);
        }
    }

    /**
     * Gets dummy, do-nothing {@link TrustManager}s for the purposes of testing.
     */
    final protected static TrustManager[] getTrustManagers() {

        return shared.util.Arrays.wrap(new X509TrustManager() {

            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
                // Accept unconditionally.
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                // Accept unconditionally.
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[] {};
            }
        });
    }

    /**
     * Creates a client-side {@link SSLFilterFactory}.
     * 
     * @param <C>
     *            the {@link FilteredConnection} type.
     */
    final protected static <C extends FilteredConnection<C, ?>> SSLFilterFactory<C> createClientSSLFilterFactory() {
        return new SSLFilterFactory<C>() //
                .setMode(Mode.CLIENT) //
                .setTrustManagers(getTrustManagers());
    }

    /**
     * Creates a server-side {@link SSLFilterFactory}.
     * 
     * @param <C>
     *            the {@link FilteredConnection} type.
     */
    final protected static <C extends FilteredConnection<C, ?>> SSLFilterFactory<C> createServerSSLFilterFactory() {
        return new SSLFilterFactory<C>() //
                .setMode(Mode.SERVER) //
                .setKeyManagers(getKeyManagers(KEYSTORE_PATHNAME, KEYSTORE_PASSWORD)) //
                .setTrustManagers(getTrustManagers());
    }

    // Dummy constructor.
    AllNetTests() {
    }
}

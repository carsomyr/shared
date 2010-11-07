/**
 * <p>
 * Copyright (c) 2007 Roy Liu<br>
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

import static shared.test.net.AllNetTests.parameterizations;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Future;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import shared.net.ConnectionManager.InitializationType;
import shared.net.filter.ChainFilterFactory;
import shared.net.filter.FrameFilterFactory;
import shared.net.filter.XmlFilterFactory;
import shared.net.filter.ssl.SslFilterFactory;
import shared.net.nio.NioManager;
import shared.test.net.TestXmlEvent.DataXmlEvent;
import shared.test.net.TestXmlEvent.ReceiverXmlVerifier;
import shared.test.net.TestXmlEvent.SenderXmlVerifier;
import shared.test.net.TestXmlEvent.SequenceXmlEvent;
import shared.util.Arithmetic;
import shared.util.Control;

/**
 * A class of unit tests for {@link NioManager}.
 * 
 * @apiviz.composedOf shared.test.net.TestXmlEvent.ReceiverXmlVerifier
 * @apiviz.composedOf shared.test.net.TestXmlEvent.SenderXmlVerifier
 * @apiviz.composedOf shared.test.net.TestXmlHandler
 * @author Roy Liu
 */
@RunWith(value = Parameterized.class)
public class AsynchronousHandlerTest {

    /**
     * The server {@link SslFilterFactory}.
     */
    final protected static SslFilterFactory serverSslFilterFactory = AllNetTests.createServerSslFilterFactory();

    /**
     * The client {@link SslFilterFactory}.
     */
    final protected static SslFilterFactory clientSslFilterFactory = AllNetTests.createClientSslFilterFactory();

    final InetSocketAddress remoteAddress;
    final long delay;
    final int messageLength;
    final int nMessages;
    final int nConnections;
    final boolean useSsl;

    NioManager rcm;
    NioManager scm;

    /**
     * Default constructor.
     */
    public AsynchronousHandlerTest(Properties p) {

        this.remoteAddress = new InetSocketAddress(p.getProperty("remote"), Integer.parseInt(p.getProperty("port")));
        this.delay = Long.parseLong(p.getProperty("delay"));
        this.messageLength = Integer.parseInt(p.getProperty("message_length"));
        this.nMessages = Integer.parseInt(p.getProperty("n_messages"));
        this.nConnections = Integer.parseInt(p.getProperty("n_async_conns"));
        this.useSsl = p.getProperty("use_ssl").equals("yes");
    }

    /**
     * Derives testing parameters.
     */
    @Parameters
    final public static Collection<Object[]> parameters() {
        return parameterizations;
    }

    /**
     * Creates a sender and a receiver.
     */
    @Before
    public void init() {

        int bufferSize = this.messageLength << 2;

        this.rcm = new NioManager("RCM").setBacklogSize(this.nConnections).setBufferSize(bufferSize);
        this.scm = new NioManager("SCM").setBacklogSize(this.nConnections).setBufferSize(bufferSize);
    }

    /**
     * Tests the {@link NioManager} transport mechanism. The sender sends {@link DataXmlEvent}s to the receiver,
     * whereupon the results are checked.
     * 
     * @exception Exception
     *                when something goes awry.
     */
    @Test
    public void testTransport() throws Exception {

        InetSocketAddress listenAddress = //
        new InetSocketAddress(this.remoteAddress.getPort());

        InetSocketAddress connectAddress = //
        new InetSocketAddress(this.remoteAddress.getHostName(), this.remoteAddress.getPort());

        int minMessageSize = this.messageLength << 3;
        int maxMessageSize = this.messageLength << 6;
        FrameFilterFactory fff = new FrameFilterFactory(minMessageSize, maxMessageSize);

        List<Future<?>> verifiers = new ArrayList<Future<?>>();

        for (int i = 0, n = this.nConnections; i < n; i++) {

            ReceiverXmlVerifier xmlV = new ReceiverXmlVerifier();

            TestXmlHandler xmlR = new TestXmlHandler(String.format("r_xml_%d", i), //
                    minMessageSize, maxMessageSize, xmlV);

            if (this.useSsl) {
                xmlR.setFilterFactory(new ChainFilterFactory<ByteBuffer, ByteBuffer, TestXmlHandler>() //
                        .add(serverSslFilterFactory) //
                        .add(fff) //
                        .add(XmlFilterFactory.getInstance()) //
                        .add(xmlR));
            }

            this.rcm.init(InitializationType.ACCEPT, xmlR, listenAddress);

            verifiers.add(xmlV);
        }

        Control.sleep(this.delay);

        for (int i = 0, n = this.nConnections; i < n; i++) {

            long seqNo = Arithmetic.nextInt(4096);

            SenderXmlVerifier xmlV = new SenderXmlVerifier(seqNo, this.nMessages, this.messageLength);

            TestXmlHandler xmlS = new TestXmlHandler(String.format("s_xml_%d", i), //
                    minMessageSize, maxMessageSize, xmlV);

            if (this.useSsl) {
                xmlS.setFilterFactory(new ChainFilterFactory<ByteBuffer, ByteBuffer, TestXmlHandler>() //
                        .add(clientSslFilterFactory) //
                        .add(fff) //
                        .add(XmlFilterFactory.getInstance()) //
                        .add(xmlS));
            }

            Future<?> fut = this.scm.init(InitializationType.CONNECT, xmlS, connectAddress);

            // The asynchronous sockets specification allows us to write data before connecting; we should test this
            // case.
            xmlS.onRemote(new SequenceXmlEvent(seqNo, this.nMessages, null));

            // Wait for connection establishment.
            fut.get();

            verifiers.add(xmlV);
        }

        // Reverse the verifier list so that we synchronize on senders first and detect any errors that may arise.
        Collections.reverse(verifiers);

        for (Future<?> v : verifiers) {
            v.get();
        }
    }

    /**
     * Destroys the sender and the receiver.
     */
    @After
    public void destroy() {

        Control.close(this.rcm);
        Control.close(this.scm);
    }
}

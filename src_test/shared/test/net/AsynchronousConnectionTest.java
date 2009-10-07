/**
 * This file is part of the Shared Scientific Toolbox in Java ("this library"). <br />
 * <br />
 * Copyright (C) 2007 Roy Liu <br />
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

package shared.test.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import shared.net.ConnectionManager;
import shared.net.filter.ChainFilterFactory;
import shared.net.filter.FrameFilterFactory;
import shared.net.filter.SSLFilterFactory;
import shared.net.filter.XMLFilterFactory;
import shared.test.net.TestXMLEvent.DataXMLEvent;
import shared.test.net.TestXMLEvent.ReceiverXMLVerifier;
import shared.test.net.TestXMLEvent.SenderXMLVerifier;
import shared.test.net.TestXMLEvent.SequenceXMLEvent;
import shared.util.Arithmetic;
import shared.util.Control;

/**
 * A class of unit tests for {@link ConnectionManager}.
 * 
 * @apiviz.composedOf shared.test.net.TestXMLEvent.ReceiverXMLVerifier
 * @apiviz.composedOf shared.test.net.TestXMLEvent.SenderXMLVerifier
 * @apiviz.composedOf shared.test.net.TestXMLConnection
 * @author Roy Liu
 */
@RunWith(value = Parameterized.class)
public class AsynchronousConnectionTest {

    /**
     * The server {@link SSLFilterFactory}.
     */
    final protected static SSLFilterFactory<TestXMLConnection> ServerSSLFilterFactory = //
    AllNetTests.createServerSSLFilterFactory();

    /**
     * The client {@link SSLFilterFactory}.
     */
    final protected static SSLFilterFactory<TestXMLConnection> ClientSSLFilterFactory = //
    AllNetTests.createClientSSLFilterFactory();

    final InetSocketAddress remoteAddress;
    final long delay;
    final int messageLength;
    final int nmessages;
    final int nconnections;
    final boolean useSSL;

    ConnectionManager rcm;
    ConnectionManager scm;

    /**
     * Default constructor.
     */
    public AsynchronousConnectionTest(Properties p) {

        this.remoteAddress = new InetSocketAddress(p.getProperty("remote"), Integer.parseInt(p.getProperty("port")));
        this.delay = Long.parseLong(p.getProperty("delay"));
        this.messageLength = Integer.parseInt(p.getProperty("message_length"));
        this.nmessages = Integer.parseInt(p.getProperty("nmessages"));
        this.nconnections = Integer.parseInt(p.getProperty("nasync_connections"));
        this.useSSL = p.getProperty("use_SSL").equals("yes");
    }

    /**
     * Derives testing parameters.
     */
    @Parameters
    final public static Collection<Object[]> parameters() {
        return AllNetTests.Parameterizations;
    }

    /**
     * Creates a sender and a receiver.
     */
    @Before
    public void init() {

        this.rcm = new ConnectionManager("RCM", this.nconnections);
        this.scm = new ConnectionManager("SCM", this.nconnections);
    }

    /**
     * Tests the {@link ConnectionManager} transport mechanism. The sender sends {@link DataXMLEvent}s to the receiver,
     * whereupon the results are checked.
     * 
     * @exception IOException
     *                when something goes awry.
     */
    @Test
    public void testTransport() throws IOException {

        InetSocketAddress listenAddress = //
        new InetSocketAddress(this.remoteAddress.getPort());

        InetSocketAddress connectAddress = //
        new InetSocketAddress(this.remoteAddress.getHostName(), this.remoteAddress.getPort());

        int minMessageSize = this.messageLength << 3;
        int maxMessageSize = this.messageLength << 6;
        int bufferSize = this.messageLength << 2;

        FrameFilterFactory<TestXMLConnection> fFF = new FrameFilterFactory<TestXMLConnection>( //
                minMessageSize, maxMessageSize);

        List<AbstractTestVerifier<?>> verifiers = new ArrayList<AbstractTestVerifier<?>>();

        for (int i = 0, n = this.nconnections; i < n; i++) {

            ReceiverXMLVerifier xmlV = new ReceiverXMLVerifier();

            TestXMLConnection xmlRConn = new TestXMLConnection(String.format("r_xml_%d", i), //
                    minMessageSize, maxMessageSize, this.rcm, xmlV) //
                    .setBufferSize(bufferSize);

            if (this.useSSL) {
                xmlRConn.setFilterFactory( //
                        new ChainFilterFactory<ByteBuffer, ByteBuffer, TestXMLConnection>() //
                                .add(ServerSSLFilterFactory) //
                                .add(fFF) //
                                .add(XMLFilterFactory.getInstance()) //
                                .add(xmlRConn));
            }

            xmlRConn.accept(listenAddress);

            verifiers.add(xmlV);
        }

        Control.sleep(this.delay);

        for (int i = 0, n = this.nconnections; i < n; i++) {

            long seqNo = Arithmetic.nextInt(4096);

            SenderXMLVerifier xmlV = new SenderXMLVerifier(seqNo, this.nmessages, this.messageLength);

            TestXMLConnection xmlSConn = new TestXMLConnection(String.format("s_xml_%d", i), //
                    minMessageSize, maxMessageSize, this.scm, xmlV) //
                    .setBufferSize(bufferSize);

            if (this.useSSL) {
                xmlSConn.setFilterFactory( //
                        new ChainFilterFactory<ByteBuffer, ByteBuffer, TestXMLConnection>() //
                                .add(ClientSSLFilterFactory) //
                                .add(fFF) //
                                .add(XMLFilterFactory.getInstance()) //
                                .add(xmlSConn));
            }

            // The asynchronous sockets specification allows us to write data before connecting; we should
            // test this case.
            xmlSConn.onRemote(new SequenceXMLEvent(seqNo, this.nmessages, null));

            try {

                xmlSConn.connect(connectAddress).get();

            } catch (Exception e) {

                throw new RuntimeException(e);
            }

            verifiers.add(xmlV);
        }

        // Reverse the verifier list so that we synchronize on senders first and detect any errors that may
        // arise.
        Collections.reverse(verifiers);

        for (AbstractTestVerifier<?> v : verifiers) {
            v.sync();
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

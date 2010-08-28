/**
 * <p>
 * Copyright (C) 2007 Roy Liu<br />
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

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Queue;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import shared.net.Connection;
import shared.net.ConnectionManager;
import shared.net.filter.ChainFilterFactory;
import shared.net.filter.Filter;
import shared.net.filter.FilterFactory;
import shared.net.filter.FilteredManagedConnection;
import shared.net.filter.FrameFilterFactory;
import shared.test.Demo;
import shared.util.Control;

/**
 * A simple client and server demo.
 * 
 * @author Roy Liu
 */
public class ClientServerTest {

    /**
     * Creates the demo directory if it doesn't already exist in the file system.
     */
    @BeforeClass
    final public static void initClass() {
        Demo.createDemoDir();
    }

    /**
     * Default constructor.
     */
    public ClientServerTest() {
    }

    /**
     * Tests client and server communication.
     */
    @Test
    public void testTransport() {

        UTF8Connection clientConnection = new UTF8Connection("Client");
        UTF8Connection serverConnection = new UTF8Connection("Server");

        int port = 10101;

        serverConnection.accept(new InetSocketAddress(port));
        clientConnection.connect(new InetSocketAddress("localhost", port));

        // Client sends stuff to the server.
        clientConnection.sendOutbound("hello");
        clientConnection.sendOutbound("from");
        clientConnection.sendOutbound("the");
        clientConnection.sendOutbound("client");

        // Server sends stuff to the client.
        serverConnection.sendOutbound("hello");
        serverConnection.sendOutbound("from");
        serverConnection.sendOutbound("the");
        serverConnection.sendOutbound("server");

        // Allow messages to propagate.
        Control.sleep(1000);

        clientConnection.close();
        serverConnection.close();

        // Give some time for a clean shutdown.
        Control.sleep(1000);

        // Free the default manager's threads.
        ConnectionManager.getInstance().close();
    }

    /**
     * An internal {@link Connection} class for demo purposes.
     */
    protected static class UTF8Connection extends FilteredManagedConnection<UTF8Connection, String> //
            implements FilterFactory<Filter<ByteBuffer, String>, ByteBuffer, String, UTF8Connection>, //
            Filter<ByteBuffer, String> {

        final Logger log;

        /**
         * Default constructor.
         */
        protected UTF8Connection(String name) {
            super(name, ConnectionManager.getInstance());

            this.log = LoggerFactory.getLogger( //
                    String.format("%s.%s", ClientServerTest.class.getName(), name));

            setFilterFactory(new ChainFilterFactory<ByteBuffer, ByteBuffer, UTF8Connection>() //
                    .add(new FrameFilterFactory<UTF8Connection>()) //
                    .add(this));
        }

        public void onReceive(Queue<String> inbounds) {

            for (String str; (str = inbounds.poll()) != null;) {
                this.log.info(String.format("Received: \"%s\".", str));
            }
        }

        public void onBind(Queue<String> inbounds) {
            this.log.info("Connection is now bound.");
        }

        public void onClosingUser(Queue<String> inbounds) {
            this.log.info("Connection has encountered a user-requested close.");
        }

        public void onClosingEOS(Queue<String> inbounds) {
            this.log.info("Connection has encountered an end-of-stream.");
        }

        public void onError(Throwable error, ByteBuffer bb) {
            this.log.info("Connection has encountered an error.", error);
        }

        public Filter<ByteBuffer, String> newFilter(UTF8Connection connection) {
            return this;
        }

        public void getInbound(Queue<ByteBuffer> in, Queue<String> out) {

            for (ByteBuffer bb; (bb = in.poll()) != null;) {

                byte[] bytes = new byte[bb.remaining()];
                bb.get(bytes);

                out.add(new String(bytes));
            }
        }

        public void getOutbound(Queue<String> in, Queue<ByteBuffer> out) {

            for (String str; (str = in.poll()) != null;) {
                out.add(ByteBuffer.wrap(str.getBytes()));
            }
        }
    }
}

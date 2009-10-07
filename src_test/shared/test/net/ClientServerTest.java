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
            implements FilterFactory<ByteBuffer, String, UTF8Connection>, Filter<ByteBuffer, String> {

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

        public void onReceiveInbound(Queue<String> inbounds) {

            for (String str = null; (str = inbounds.poll()) != null;) {
                this.log.info(String.format("Received: \"%s\".", str));
            }
        }

        public void onBindInbound(Queue<String> inbounds) {
            this.log.info("Connection is now bound.");
        }

        public void onCloseInbound(Queue<String> inbounds) {
            this.log.info("Connection has encountered a user-requested close.");
        }

        public void onEOSInbound(Queue<String> inbounds) {
            this.log.info("Connection has encountered an end-of-stream.");
        }

        public void onError(Throwable error, ByteBuffer bb) {
            this.log.info("Connection has encountered an error.", error);
        }

        public Filter<ByteBuffer, String> newFilter(UTF8Connection connection) {
            return this;
        }

        public void getInbound(Queue<ByteBuffer> in, Queue<String> out) {

            for (ByteBuffer bb = null; (bb = in.poll()) != null;) {

                byte[] bytes = new byte[bb.remaining()];
                bb.get(bytes);

                out.add(new String(bytes));
            }
        }

        public void getOutbound(Queue<String> in, Queue<ByteBuffer> out) {

            for (String str = null; (str = in.poll()) != null;) {
                out.add(ByteBuffer.wrap(str.getBytes()));
            }
        }
    }
}

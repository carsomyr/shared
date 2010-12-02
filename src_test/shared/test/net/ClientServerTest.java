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

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Queue;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import shared.net.Connection;
import shared.net.ConnectionManager;
import shared.net.ConnectionManager.InitializationType;
import shared.net.filter.ChainFilterFactory;
import shared.net.filter.Filter;
import shared.net.filter.FilterFactory;
import shared.net.filter.FrameFilterFactory;
import shared.net.handler.AbstractFilteredHandler;
import shared.net.nio.NioConnection;
import shared.net.nio.NioManager;
import shared.test.Demo;

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
     * 
     * @throws InterruptedException
     *             when something goes awry.
     */
    @Test
    public void testTransport() throws InterruptedException {

        ConnectionManager<NioConnection> cm = NioManager.getInstance();

        Utf8Handler clientHandler = new Utf8Handler("Client");
        Utf8Handler serverHandler = new Utf8Handler("Server");

        int port = 10101;

        cm.init(InitializationType.ACCEPT, clientHandler, new InetSocketAddress(port));
        cm.init(InitializationType.CONNECT, serverHandler, new InetSocketAddress("localhost", port));

        // Client sends stuff to the server.
        clientHandler.send("hello");
        clientHandler.send("from");
        clientHandler.send("the");
        clientHandler.send("client");

        // Server sends stuff to the client.
        serverHandler.send("hello");
        serverHandler.send("from");
        serverHandler.send("the");
        serverHandler.send("server");

        // Allow messages to propagate.
        Thread.sleep(1000);

        clientHandler.close();
        serverHandler.close();

        // Give some time for a clean shutdown.
        Thread.sleep(1000);

        // Free the default manager's threads.
        cm.close();
    }

    /**
     * An internal {@link Connection} class for demo purposes.
     */
    protected static class Utf8Handler extends AbstractFilteredHandler<Utf8Handler, Connection, String> //
            implements FilterFactory<Filter<String, ByteBuffer>, String, ByteBuffer, Utf8Handler>, //
            Filter<String, ByteBuffer> {

        final Logger log;

        /**
         * Default constructor.
         */
        protected Utf8Handler(String name) {
            super(name);

            this.log = LoggerFactory.getLogger( //
                    String.format("%s.%s", ClientServerTest.class.getName(), name));

            setFilterFactory(new ChainFilterFactory<ByteBuffer, ByteBuffer, Utf8Handler>() //
                    .add(new FrameFilterFactory()) //
                    .add(this));
        }

        @Override
        public void onBind(Queue<String> inputs) {
            this.log.info("Connection is now bound.");
        }

        @Override
        public void onReceive(Queue<String> inputs) {

            for (String str; (str = inputs.poll()) != null;) {
                this.log.info(String.format("Received: \"%s\".", str));
            }
        }

        @Override
        public void onClosing(ClosingType type, Queue<String> inputs) {

            switch (type) {

            case EOS:
                this.log.info("Connection has encountered a user close request.");
                break;

            case USER:
                this.log.info("Connection has encountered an end-of-stream.");
                break;

            case ERROR:
                this.log.info("Connection has encountered an error.", getConnection().getException());
                break;

            default:
                throw new IllegalArgumentException("Invalid closing type");
            }
        }

        @Override
        public void onClose() {
            this.log.info("Connection is now closed.");
        }

        @Override
        public Filter<String, ByteBuffer> newFilter(Utf8Handler handler) {
            return this;
        }

        @Override
        public void applyInbound(Queue<ByteBuffer> inputs, Queue<String> outputs) {

            for (ByteBuffer bb; (bb = inputs.poll()) != null;) {

                byte[] bytes = new byte[bb.remaining()];
                bb.get(bytes);

                outputs.add(new String(bytes));
            }
        }

        @Override
        public void applyOutbound(Queue<String> inputs, Queue<ByteBuffer> outputs) {

            for (String str; (str = inputs.poll()) != null;) {
                outputs.add(ByteBuffer.wrap(str.getBytes()));
            }
        }
    }
}

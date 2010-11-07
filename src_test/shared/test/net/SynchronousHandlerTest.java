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

import static shared.test.net.AllNetTests.parameterizations;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import shared.net.Connection;
import shared.net.ConnectionManager.InitializationType;
import shared.net.filter.ssl.SslFilterFactory;
import shared.net.handler.SynchronousHandler;
import shared.net.nio.NioManager;
import shared.util.Control;

/**
 * A class of unit tests for {@link NioManager}.
 * 
 * @author Roy Liu
 */
@RunWith(value = Parameterized.class)
public class SynchronousHandlerTest {

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

    NioManager rcm, scm;
    ExecutorService executor;

    /**
     * Default constructor.
     */
    public SynchronousHandlerTest(Properties p) {

        this.remoteAddress = new InetSocketAddress(p.getProperty("remote"), Integer.parseInt(p.getProperty("port")));
        this.delay = Long.parseLong(p.getProperty("delay"));
        this.messageLength = Integer.parseInt(p.getProperty("message_length"));
        this.nMessages = Integer.parseInt(p.getProperty("n_messages"));
        this.nConnections = Integer.parseInt(p.getProperty("n_sync_conns"));
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

        this.rcm = NioManager.getInstance().setBufferSize(bufferSize);
        this.scm = new NioManager("SCM").setBufferSize(bufferSize);
        this.executor = Executors.newCachedThreadPool(new ThreadFactory() {

            AtomicInteger threadCount = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {

                Thread t = new Thread(r, String.format("Test Worker #%d", this.threadCount.getAndIncrement()));
                t.setDaemon(true);

                return t;
            }
        });
    }

    /**
     * Tests the transport capabilities of synchronous connections.
     * 
     * @exception Exception
     *                when something goes awry.
     */
    @Test
    public void testTransport() throws Exception {

        int basePort = this.remoteAddress.getPort();
        String hostname = this.remoteAddress.getHostName();

        List<Future<?>> futures = new ArrayList<Future<?>>();

        for (int i = 0, n = this.nConnections, port = basePort; i < n; i++, port++) {
            futures.add(this.executor.submit(createReceiver(i, new InetSocketAddress(port))));
        }

        Control.sleep(this.delay);

        for (int i = 0, n = this.nConnections, port = basePort; i < n; i++, port++) {
            futures.add(this.executor.submit(createSender(i, new InetSocketAddress(hostname, port))));
        }

        // Reverse the verifier list so that we synchronize on senders first and detect any errors that may arise.
        Collections.reverse(futures);

        for (Future<?> future : futures) {
            future.get();
        }
    }

    /**
     * Creates a receiving verifier.
     */
    protected Callable<?> createReceiver(int index, final InetSocketAddress localAddress) {

        final SynchronousHandler<Connection> receiver = //
        new SynchronousHandler<Connection>(String.format("r-%d", index), this.rcm.getBufferSize());

        if (this.useSsl) {
            receiver.setFilterFactory(serverSslFilterFactory);
        }

        return new Callable<Object>() {

            @Override
            public Object call() throws Exception {

                SynchronousHandlerTest sht = SynchronousHandlerTest.this;
                sht.rcm.init(InitializationType.ACCEPT, receiver, localAddress).get();

                InputStream in = receiver.getInputStream();
                OutputStream out = receiver.getOutputStream();

                ByteBuffer header = ByteBuffer.allocate(8);

                for (int i = 0; i < 8; i++) {
                    header.put((byte) in.read());
                }

                Control.checkTrue(((ByteBuffer) header.flip()).getLong() == 0xCAFEBABEDEADBEEFL, //
                        "Invalid data");

                byte[] arr = new byte[sht.messageLength << 3];

                for (int i = 0, n = sht.nMessages, acc = 0; i < n; i++, acc += sht.messageLength) {

                    for (int size, length = arr.length, offset = 0; length > 0; length -= size, offset += size) {

                        size = in.read(arr, offset, length);

                        Control.checkTrue(size != -1, //
                                "Invalid data");
                    }

                    for (int j = 0; j < 256; j++) {
                        out.write((byte) j);
                    }

                    Control.checkTrue(Arrays.equals(arr, createMessage(acc, sht.messageLength).array()), //
                            "Invalid data");
                }

                Control.close(in);

                return null;
            }
        };
    }

    /**
     * Creates a sending verifier.
     */
    protected Callable<?> createSender(int index, final InetSocketAddress remoteAddress) {

        final SynchronousHandler<Connection> sender = //
        new SynchronousHandler<Connection>(String.format("s-%d", index), this.scm.getBufferSize());

        if (this.useSsl) {
            sender.setFilterFactory(clientSslFilterFactory);
        }

        return new Callable<Object>() {

            @Override
            public Object call() throws Exception {

                SynchronousHandlerTest sht = SynchronousHandlerTest.this;
                sht.scm.init(InitializationType.CONNECT, sender, remoteAddress).get();

                InputStream in = sender.getInputStream();
                OutputStream out = sender.getOutputStream();

                ByteBuffer header = (ByteBuffer) ByteBuffer.allocate(8).putLong(0xCAFEBABEDEADBEEFL).flip();

                for (int i = 0; i < 8; i++) {
                    out.write(header.get());
                }

                for (int i = 0, n = sht.nMessages, acc = 0; i < n; i++, acc += sht.messageLength) {

                    ByteBuffer bb = createMessage(acc, sht.messageLength);

                    out.write(bb.array(), 0, bb.capacity());

                    for (int j = 0; j < 256; j++) {
                        Control.checkTrue(in.read() == j, //
                                "Invalid data");
                    }
                }

                Control.checkTrue(in.read() == -1, //
                        "Invalid data");

                return null;
            }
        };
    }

    /**
     * Creates a message.
     */
    final protected static ByteBuffer createMessage(int offset, int size) {

        ByteBuffer res = ByteBuffer.allocate(size << 3);

        for (int i = offset, n = offset + size; i < n; i++) {
            res.putLong(i);
        }

        return res;
    }

    /**
     * Destroys the sender and the receiver.
     */
    @After
    public void destroy() {

        Control.close(this.rcm);
        Control.close(this.scm);
        this.executor.shutdown();
    }
}

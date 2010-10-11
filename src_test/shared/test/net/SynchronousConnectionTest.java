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

import java.io.IOException;
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
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import shared.net.Connection.InitializationType;
import shared.net.ConnectionManager;
import shared.net.SynchronousManagedConnection;
import shared.net.filter.SslFilterFactory;
import shared.util.Control;
import shared.util.CoreThread;

/**
 * A class of unit tests for {@link ConnectionManager}.
 * 
 * @author Roy Liu
 */
@RunWith(value = Parameterized.class)
public class SynchronousConnectionTest {

    /**
     * The server {@link SslFilterFactory}.
     */
    final protected static SslFilterFactory<SynchronousManagedConnection> serverSslFilterFactory = //
    AllNetTests.createServerSslFilterFactory();

    /**
     * The client {@link SslFilterFactory}.
     */
    final protected static SslFilterFactory<SynchronousManagedConnection> clientSslFilterFactory = //
    AllNetTests.createClientSslFilterFactory();

    final InetSocketAddress remoteAddress;
    final long delay;
    final int messageLength;
    final int nMessages;
    final int nConnections;
    final boolean useSsl;

    ConnectionManager rcm, scm;

    /**
     * Default constructor.
     */
    public SynchronousConnectionTest(Properties p) {

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

        this.rcm = ConnectionManager.getInstance();
        this.scm = new ConnectionManager("SCM");
    }

    /**
     * Tests the transport capabilities of synchronous connections.
     * 
     * @exception IOException
     *                when something goes awry.
     */
    @Test
    public void testTransport() throws IOException {

        int bufferSize = this.messageLength << 2;
        int basePort = this.remoteAddress.getPort();
        String hostname = this.remoteAddress.getHostName();

        List<Verifier> verifiers = new ArrayList<Verifier>();

        for (int i = 0, n = this.nConnections, port = basePort; i < n; i++, port++) {
            verifiers.add(createReceiver(i, bufferSize, new InetSocketAddress(port)));
        }

        Control.sleep(this.delay);

        for (int i = 0, n = this.nConnections, port = basePort; i < n; i++, port++) {
            verifiers.add(createSender(i, bufferSize, new InetSocketAddress(hostname, port)));
        }

        // Reverse the verifier list so that we synchronize on senders first and detect any errors that may arise.
        Collections.reverse(verifiers);

        for (Verifier v : verifiers) {
            v.sync();
        }
    }

    /**
     * Creates a receiver {@link Verifier}.
     * 
     * @exception IOException
     *                when something goes awry.
     */
    protected Verifier createReceiver(int index, //
            int bufferSize, final InetSocketAddress localAddress) throws IOException {

        final SynchronousManagedConnection receiver = new SynchronousManagedConnection( //
                String.format("r-%d", index), this.rcm) //
                .setBufferSize(bufferSize);

        if (this.useSsl) {
            receiver.setFilterFactory(serverSslFilterFactory);
        }

        final AtomicBoolean success = new AtomicBoolean(false);

        final Thread r = new CoreThread("Receiver Thread") {

            @Override
            protected void doRun() throws Exception {

                SynchronousConnectionTest sct = SynchronousConnectionTest.this;

                receiver.init(InitializationType.ACCEPT, localAddress).get();

                InputStream in = receiver.getInputStream();
                OutputStream out = receiver.getOutputStream();

                ByteBuffer header = ByteBuffer.allocate(8);

                for (int i = 0; i < 8; i++) {
                    header.put((byte) in.read());
                }

                Control.checkTrue(((ByteBuffer) header.flip()).getLong() == 0xCAFEBABEDEADBEEFL, //
                        "Invalid data");

                byte[] arr = new byte[sct.messageLength << 3];

                for (int i = 0, n = sct.nMessages, acc = 0; i < n; i++, acc += sct.messageLength) {

                    for (int size, length = arr.length, offset = 0; length > 0; length -= size, offset += size) {

                        size = in.read(arr, offset, length);

                        Control.checkTrue(size != -1, //
                                "Invalid data");
                    }

                    for (int j = 0; j < 256; j++) {
                        out.write((byte) j);
                    }

                    Control.checkTrue(Arrays.equals(arr, createMessage(acc, sct.messageLength).array()), //
                            "Invalid data");
                }

                Control.close(in);

                success.set(true);
            }
        };

        r.start();

        return new Verifier() {

            @Override
            public void sync() {

                loop: for (;;) {

                    try {

                        r.join();

                        break loop;

                    } catch (InterruptedException e) {

                        continue loop;
                    }
                }

                Control.checkTrue(success.get(), //
                        "Transport failed");
            }
        };
    }

    /**
     * Creates a sender {@link Verifier}.
     * 
     * @exception IOException
     *                when something goes awry.
     */
    protected Verifier createSender(int index, //
            int bufferSize, final InetSocketAddress remoteAddress) throws IOException {

        final SynchronousManagedConnection sender = new SynchronousManagedConnection( //
                String.format("s-%d", index), this.scm) //
                .setBufferSize(bufferSize);

        if (this.useSsl) {
            sender.setFilterFactory(clientSslFilterFactory);
        }

        final AtomicBoolean success = new AtomicBoolean(false);

        final Thread s = new CoreThread("Sender Thread") {

            @Override
            protected void doRun() throws Exception {

                SynchronousConnectionTest sct = SynchronousConnectionTest.this;

                sender.init(InitializationType.CONNECT, remoteAddress).get();

                InputStream in = sender.getInputStream();
                OutputStream out = sender.getOutputStream();

                ByteBuffer header = (ByteBuffer) ByteBuffer.allocate(8).putLong(0xCAFEBABEDEADBEEFL).flip();

                for (int i = 0; i < 8; i++) {
                    out.write(header.get());
                }

                for (int i = 0, n = sct.nMessages, acc = 0; i < n; i++, acc += sct.messageLength) {

                    ByteBuffer bb = createMessage(acc, sct.messageLength);

                    out.write(bb.array(), 0, bb.capacity());

                    for (int j = 0; j < 256; j++) {
                        Control.checkTrue(in.read() == j, //
                                "Invalid data");
                    }
                }

                Control.checkTrue(in.read() == -1, //
                        "Invalid data");

                success.set(true);
            }
        };

        s.start();

        return new Verifier() {

            @Override
            public void sync() {

                loop: for (;;) {

                    try {

                        s.join();

                        break loop;

                    } catch (InterruptedException e) {

                        continue loop;
                    }
                }

                Control.checkTrue(success.get(), //
                        "Transport failed");
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
    }
}

/**
 * This file is part of the Shared Scientific Toolbox in Java ("this library"). <br />
 * <br />
 * Copyright (C) 2008 Roy Liu <br />
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

import shared.net.ConnectionManager;
import shared.net.SynchronousManagedConnection;
import shared.net.filter.SSLFilterFactory;
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
     * The server {@link SSLFilterFactory}.
     */
    final protected static SSLFilterFactory<SynchronousManagedConnection> ServerSSLFilterFactory = //
    AllNetTests.createServerSSLFilterFactory();

    /**
     * The client {@link SSLFilterFactory}.
     */
    final protected static SSLFilterFactory<SynchronousManagedConnection> ClientSSLFilterFactory = //
    AllNetTests.createClientSSLFilterFactory();

    final InetSocketAddress remoteAddress;
    final long delay;
    final int messageLength;
    final int nmessages;
    final int nconnections;
    final boolean useSSL;

    ConnectionManager rcm, scm;

    /**
     * Default constructor.
     */
    public SynchronousConnectionTest(Properties p) {

        this.remoteAddress = new InetSocketAddress(p.getProperty("remote"), Integer.parseInt(p.getProperty("port")));
        this.delay = Long.parseLong(p.getProperty("delay"));
        this.messageLength = Integer.parseInt(p.getProperty("message_length"));
        this.nmessages = Integer.parseInt(p.getProperty("nmessages"));
        this.nconnections = Integer.parseInt(p.getProperty("nsync_connections"));
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
        String hostName = this.remoteAddress.getHostName();

        List<Verifier> verifiers = new ArrayList<Verifier>();

        for (int i = 0, n = this.nconnections, port = basePort; i < n; i++, port++) {
            verifiers.add(createReceiver(i, bufferSize, new InetSocketAddress(port)));
        }

        Control.sleep(this.delay);

        for (int i = 0, n = this.nconnections, port = basePort; i < n; i++, port++) {
            verifiers.add(createSender(i, bufferSize, new InetSocketAddress(hostName, port)));
        }

        // Reverse the verifier list so that we synchronize on senders first and detect any errors that may
        // arise.
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
    protected Verifier createReceiver(final int index, //
            int bufferSize, final InetSocketAddress localAddress) throws IOException {

        final SynchronousManagedConnection receiver = new SynchronousManagedConnection( //
                String.format("r-%d", index), this.rcm) //
                .setBufferSize(bufferSize);

        if (this.useSSL) {
            receiver.setFilterFactory(ServerSSLFilterFactory);
        }

        final AtomicBoolean success = new AtomicBoolean(false);

        final Thread r = new CoreThread("Receiver Thread") {

            @Override
            protected void runUnchecked() throws Exception {

                SynchronousConnectionTest sct = SynchronousConnectionTest.this;

                try {

                    receiver.accept(localAddress).get();

                } catch (Exception e) {

                    throw new RuntimeException(e);
                }

                InputStream in = receiver.getInputStream();
                OutputStream out = receiver.getOutputStream();

                ByteBuffer header = ByteBuffer.allocate(8);

                for (int i = 0; i < 8; i++) {
                    header.put((byte) in.read());
                }

                Control.checkTrue(((ByteBuffer) header.flip()).getLong() == 0xCAFEBABEDEADBEEFL, //
                        "Invalid data");

                byte[] arr = new byte[sct.messageLength << 3];

                for (int i = 0, n = sct.nmessages, acc = 0; i < n; i++, acc += sct.messageLength) {

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
    protected Verifier createSender(final int index, //
            int bufferSize, final InetSocketAddress remoteAddress) throws IOException {

        final SynchronousManagedConnection sender = new SynchronousManagedConnection( //
                String.format("s-%d", index), this.scm) //
                .setBufferSize(bufferSize);

        if (this.useSSL) {
            sender.setFilterFactory(ClientSSLFilterFactory);
        }

        final AtomicBoolean success = new AtomicBoolean(false);

        final Thread s = new CoreThread("Sender Thread") {

            @Override
            protected void runUnchecked() throws Exception {

                SynchronousConnectionTest sct = SynchronousConnectionTest.this;

                try {

                    sender.connect(remoteAddress).get();

                } catch (Exception e) {

                    throw new RuntimeException(e);
                }

                InputStream in = sender.getInputStream();
                OutputStream out = sender.getOutputStream();

                ByteBuffer header = (ByteBuffer) ByteBuffer.allocate(8) //
                        .putLong(0xCAFEBABEDEADBEEFL).flip();

                for (int i = 0; i < 8; i++) {
                    out.write(header.get());
                }

                for (int i = 0, n = sct.nmessages, acc = 0; i < n; i++, acc += sct.messageLength) {

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

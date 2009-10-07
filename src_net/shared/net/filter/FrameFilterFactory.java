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

import java.nio.ByteBuffer;
import java.util.Queue;

import shared.net.Connection;
import shared.util.Control;

/**
 * An implementation of {@link FilterFactory} for {@code null}-delimited {@code byte} frames.
 * 
 * @param <C>
 *            the {@link Connection} type.
 * @author Roy Liu
 */
public class FrameFilterFactory<C extends Connection> implements FilterFactory<ByteBuffer, ByteBuffer, C> {

    final int minimumSize;
    final int maximumSize;

    /**
     * Default constructor.
     */
    public FrameFilterFactory(int minimumSize, int maximumSize) {

        this.minimumSize = minimumSize;
        this.maximumSize = maximumSize;
    }

    /**
     * Alternate constructor.
     */
    public FrameFilterFactory(int maximumSize) {
        this(maximumSize, maximumSize);
    }

    /**
     * Alternate constructor.
     */
    public FrameFilterFactory() {
        this(0, Integer.MAX_VALUE);
    }

    public Filter<ByteBuffer, ByteBuffer> newFilter(final C connection) {

        final FrameFilterFactory<C> xmlFF = FrameFilterFactory.this;

        return new Filter<ByteBuffer, ByteBuffer>() {

            ByteBuffer frameBuffer = ByteBuffer.allocate(xmlFF.minimumSize);

            public void getInbound(Queue<ByteBuffer> in, Queue<ByteBuffer> out) {

                assert !Thread.holdsLock(connection);

                loop: for (ByteBuffer bb = null; (bb = in.peek()) != null;) {

                    int save = bb.position();

                    byte b;

                    for (b = -1; bb.hasRemaining() && (b = bb.get()) != 0;) {
                    }

                    if (b == 0) {

                        int size = bb.position() - save - 1;

                        ensureCapacity(size);

                        this.frameBuffer.put(bb.array(), save, size);
                        bb.position(save + size + 1);

                        this.frameBuffer.flip();
                        out.add((ByteBuffer) ByteBuffer.allocate(this.frameBuffer.remaining()) //
                                .put(this.frameBuffer).flip());
                        this.frameBuffer.compact();

                        assert (this.frameBuffer.limit() == this.frameBuffer.capacity()) //
                                && (this.frameBuffer.position() == 0);

                        if (this.frameBuffer.capacity() > xmlFF.minimumSize) {
                            this.frameBuffer = ByteBuffer.allocate(xmlFF.minimumSize);
                        }

                        // Continue the loop and see if anything more can be read.
                        continue loop;

                    } else {

                        int size = bb.position() - save;

                        ensureCapacity(size);

                        this.frameBuffer.put(bb.array(), save, size);
                        bb.position(save + size);

                        in.remove();
                    }
                }
            }

            public void getOutbound(Queue<ByteBuffer> in, Queue<ByteBuffer> out) {

                assert Thread.holdsLock(connection);

                for (ByteBuffer bb = null; (bb = in.poll()) != null;) {

                    int save = bb.position();

                    for (; bb.hasRemaining() && (bb.get() != 0);) {
                    }

                    Control.checkTrue(bb.position() == bb.limit(), //
                            "Buffer must contain all non-zero byte values");

                    bb.position(save);

                    out.add(bb);
                    out.add((ByteBuffer) ByteBuffer.allocate(1).put((byte) 0).flip());
                }
            }

            void ensureCapacity(int len) {

                Control.checkTrue(len <= xmlFF.maximumSize - this.frameBuffer.position(), //
                        "Maximum message size exceeded");

                if (len > this.frameBuffer.remaining()) {

                    assert (this.frameBuffer.limit() == this.frameBuffer.capacity());

                    this.frameBuffer = ByteBuffer.allocate(Math.min( //
                            xmlFF.maximumSize, (this.frameBuffer.position() + len) << 1)) //
                            .put((ByteBuffer) this.frameBuffer.flip());
                }
            }
        };
    }
}

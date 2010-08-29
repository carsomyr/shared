/**
 * <p>
 * Copyright (C) 2009 Roy Liu<br />
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
public class FrameFilterFactory<C extends Connection> //
        implements FilterFactory<Filter<ByteBuffer, ByteBuffer>, ByteBuffer, ByteBuffer, C> {

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

    @Override
    public Filter<ByteBuffer, ByteBuffer> newFilter(final C connection) {

        final FrameFilterFactory<C> fFF = FrameFilterFactory.this;

        return new Filter<ByteBuffer, ByteBuffer>() {

            ByteBuffer frameBuffer = ByteBuffer.allocate(fFF.minimumSize);

            @Override
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

                        if (this.frameBuffer.capacity() > fFF.minimumSize) {
                            this.frameBuffer = ByteBuffer.allocate(fFF.minimumSize);
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

            @Override
            public void getOutbound(Queue<ByteBuffer> in, Queue<ByteBuffer> out) {

                assert Thread.holdsLock(connection);

                for (ByteBuffer bb; (bb = in.poll()) != null;) {

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

                Control.checkTrue(len <= fFF.maximumSize - this.frameBuffer.position(), //
                        "Maximum message size exceeded");

                if (len > this.frameBuffer.remaining()) {

                    assert (this.frameBuffer.limit() == this.frameBuffer.capacity());

                    this.frameBuffer = ByteBuffer.allocate(Math.min( //
                            fFF.maximumSize, (this.frameBuffer.position() + len) << 1)) //
                            .put((ByteBuffer) this.frameBuffer.flip());
                }
            }
        };
    }
}

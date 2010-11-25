/**
 * <p>
 * Copyright (c) 2009 Roy Liu<br>
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
import shared.net.ConnectionHandler;

/**
 * An implementation of {@link FilterFactory} for {@code null}-delimited {@code byte} frames.
 * 
 * @author Roy Liu
 */
public class FrameFilterFactory //
        implements FilterFactory<Filter<ByteBuffer, ByteBuffer>, ByteBuffer, ByteBuffer, ConnectionHandler<?>> {

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
    public Filter<ByteBuffer, ByteBuffer> newFilter(final ConnectionHandler<?> handler) {

        final FrameFilterFactory fff = FrameFilterFactory.this;

        return new Filter<ByteBuffer, ByteBuffer>() {

            ByteBuffer frameBuffer = ByteBuffer.allocate(fff.minimumSize);

            @Override
            public void applyInbound(Queue<ByteBuffer> inputs, Queue<ByteBuffer> outputs) {

                Connection conn = handler.getConnection();
                assert !Thread.holdsLock(conn.getLock()) && conn.isManagerThread();

                for (ByteBuffer bb = null; (bb = inputs.peek()) != null;) {

                    int save = bb.position();

                    byte b = -1;

                    for (; bb.hasRemaining() && (b = bb.get()) != 0;) {
                    }

                    if (b == 0) {

                        int size = bb.position() - save - 1;

                        ensureCapacity(size);

                        this.frameBuffer.put(bb.array(), save, size);
                        bb.position(save + size + 1);

                        this.frameBuffer.flip();
                        outputs.add((ByteBuffer) ByteBuffer.allocate(this.frameBuffer.remaining()) //
                                .put(this.frameBuffer).flip());
                        this.frameBuffer.compact();

                        assert (this.frameBuffer.limit() == this.frameBuffer.capacity()) //
                                && (this.frameBuffer.position() == 0);

                        if (this.frameBuffer.capacity() > fff.minimumSize) {
                            this.frameBuffer = ByteBuffer.allocate(fff.minimumSize);
                        }

                    } else {

                        int size = bb.position() - save;

                        ensureCapacity(size);

                        this.frameBuffer.put(bb.array(), save, size);
                        bb.position(save + size);

                        inputs.remove();
                    }
                }
            }

            @Override
            public void applyOutbound(Queue<ByteBuffer> inputs, Queue<ByteBuffer> outputs) {

                assert Thread.holdsLock(handler.getConnection().getLock());

                for (ByteBuffer bb; (bb = inputs.poll()) != null;) {

                    int save = bb.position();

                    for (; bb.hasRemaining() && (bb.get() != 0);) {
                    }

                    if (bb.position() != bb.limit()) {
                        throw new IllegalArgumentException("Buffer must contain all nonzero byte values");
                    }

                    bb.position(save);

                    outputs.add(bb);
                    outputs.add((ByteBuffer) ByteBuffer.allocate(1).put((byte) 0).flip());
                }
            }

            /**
             * Ensures that the underlying frame buffer has at least the given number of {@code byte}s.
             */
            protected void ensureCapacity(int len) {

                if (len > fff.maximumSize - this.frameBuffer.position()) {
                    throw new IllegalStateException("Maximum message size exceeded");
                }

                if (len > this.frameBuffer.remaining()) {

                    assert (this.frameBuffer.limit() == this.frameBuffer.capacity());

                    this.frameBuffer = ByteBuffer.allocate(Math.min( //
                            fff.maximumSize, (this.frameBuffer.position() + len) << 1)) //
                            .put((ByteBuffer) this.frameBuffer.flip());
                }
            }
        };
    }
}

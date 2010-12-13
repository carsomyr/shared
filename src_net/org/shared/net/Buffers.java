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

package org.shared.net;

import java.nio.Buffer;
import java.nio.ByteBuffer;

/**
 * A static utility class for {@link Buffer} manipulations.
 * 
 * @author Roy Liu
 */
public class Buffers {

    /**
     * Appends the contents of one buffer onto the end of another.
     * 
     * @param dst
     *            the destination {@link ByteBuffer}.
     * @param src
     *            the source {@link ByteBuffer}, which must be in the ready-to-read mode.
     * @param shift
     *            the power of two overallocation factor.
     * @return the destination {@link ByteBuffer}, or a copy with capacity equal to the overallocation factor times the
     *         total amount of data.
     */
    final public static ByteBuffer append(ByteBuffer dst, ByteBuffer src, int shift) {

        if (src.remaining() > dst.remaining()) {
            dst = ByteBuffer.allocate((dst.position() + src.remaining()) << shift) //
                    .put((ByteBuffer) dst.flip());
        }

        return dst.put(src);
    }

    /**
     * A facade for {@link #append(ByteBuffer, ByteBuffer, int)}.
     */
    final public static ByteBuffer append(ByteBuffer dst, ByteBuffer src) {
        return append(dst, src, 0);
    }

    /**
     * A facade for {@link #append(ByteBuffer, ByteBuffer, int)}.
     */
    final public static ByteBuffer resize(ByteBuffer src, int size) {
        return append(ByteBuffer.allocate(size), src, 0);
    }

    // Dummy constructor.
    Buffers() {
    }
}

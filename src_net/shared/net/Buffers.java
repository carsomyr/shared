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

package shared.net;

import java.nio.Buffer;
import java.nio.ByteBuffer;

/**
 * A collection of static utility methods for {@link Buffer} manipulations.
 * 
 * @author Roy Liu
 */
public class Buffers {

    /**
     * An empty {@link ByteBuffer}.
     */
    final public static ByteBuffer EmptyBuffer = ByteBuffer.allocate(0);

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

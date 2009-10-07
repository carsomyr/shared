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

package shared.codec;

import static java.util.zip.Deflater.BEST_COMPRESSION;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterOutputStream;

/**
 * A collection of convenience wrappers for the static methods found in {@link Deflater} and {@link Inflater}.
 * 
 * @author Roy Liu
 */
public class Compression {

    /**
     * Compresses the given {@code byte} array.
     * 
     * @param array
     *            the {@code byte} array.
     * @return the compressed {@code byte} array.
     */
    final public static byte[] deflate(byte[] array) {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DeflaterOutputStream deflaterOut = new DeflaterOutputStream(out, new Deflater(BEST_COMPRESSION));

        try {

            deflaterOut.write(array);
            deflaterOut.finish();

        } catch (IOException e) {

            throw new RuntimeException(e);
        }

        return out.toByteArray();
    }

    /**
     * Decompresses the given {@code byte} array.
     * 
     * @param array
     *            the {@code byte} array.
     * @return the decompressed {@code byte} array.
     */
    final public static byte[] inflate(byte[] array) {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        InflaterOutputStream inflaterOut = new InflaterOutputStream(out);

        try {

            inflaterOut.write(array);
            inflaterOut.finish();

        } catch (IOException e) {

            throw new RuntimeException(e);
        }

        return out.toByteArray();
    }

    // Dummy constructor.
    Compression() {
    }
}

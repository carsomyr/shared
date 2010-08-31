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

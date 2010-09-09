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

package shared.test.util;

import java.nio.ByteOrder;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import shared.codec.Base64;
import shared.codec.Compression;
import shared.codec.Hex;
import shared.util.Arithmetic;

/**
 * A class of unit tests for I/O codecs.
 * 
 * @author Roy Liu
 */
public class CodecTest {

    /**
     * Default constructor.
     */
    public CodecTest() {
    }

    /**
     * Tests reading and writing with the {@link Base64} codec.
     */
    @Test
    public void testBase64() {

        int len = 1024;

        byte[] byteValues = Arithmetic.nextBytes(len);

        int[] intValues = new int[len];

        for (int i = 0; i < len; i++) {
            intValues[i] = Arithmetic.nextInt();
        }

        double[] doubleValues = new double[len];

        for (int i = 0; i < len; i++) {
            doubleValues[i] = Arithmetic.nextDouble(1.0);
        }

        long[] longValues = new long[len];

        for (int i = 0; i < len; i++) {
            longValues[i] = Arithmetic.nextLong();
        }

        Assert.assertTrue(Arrays.equals(Base64.base64ToBytes(Base64.bytesToBase64(byteValues)), byteValues));
        Assert.assertTrue(Arrays.equals(Base64.base64ToDoubles(Base64.doublesToBase64(doubleValues)), doubleValues));
        Assert.assertTrue(Arrays.equals(Base64.base64ToInts(Base64.intsToBase64(intValues)), intValues));
        Assert.assertTrue(Arrays.equals(Base64.base64ToLongs(Base64.longsToBase64(longValues)), longValues));

        Assert.assertTrue(Arrays.equals(Base64.base64ToDoubles(Base64.doublesToBase64( //
                doubleValues, ByteOrder.BIG_ENDIAN), ByteOrder.BIG_ENDIAN), doubleValues));
        Assert.assertTrue(Arrays.equals(Base64.base64ToInts(Base64.intsToBase64( //
                intValues, ByteOrder.BIG_ENDIAN), ByteOrder.BIG_ENDIAN), intValues));
        Assert.assertTrue(Arrays.equals(Base64.base64ToLongs(Base64.longsToBase64( //
                longValues, ByteOrder.BIG_ENDIAN), ByteOrder.BIG_ENDIAN), longValues));

        Assert.assertTrue(Arrays.equals(Base64.base64ToDoubles(Base64.doublesToBase64( //
                doubleValues, ByteOrder.LITTLE_ENDIAN), ByteOrder.LITTLE_ENDIAN), doubleValues));
        Assert.assertTrue(Arrays.equals(Base64.base64ToInts(Base64.intsToBase64( //
                intValues, ByteOrder.LITTLE_ENDIAN), ByteOrder.LITTLE_ENDIAN), intValues));
        Assert.assertTrue(Arrays.equals(Base64.base64ToLongs(Base64.longsToBase64( //
                longValues, ByteOrder.LITTLE_ENDIAN), ByteOrder.LITTLE_ENDIAN), longValues));
    }

    /**
     * Tests reading and writing with the {@link Hex} codec.
     */
    @Test
    public void testHex() {

        int len = 1024;

        byte[] byteValues = Arithmetic.nextBytes(len);

        Assert.assertTrue(Arrays.equals(Hex.hexToBytes(Hex.bytesToHex(byteValues)), byteValues));
    }

    /**
     * Tests compression and decompression with the {@link Compression} codec.
     */
    @Test
    public void testCompress() {

        for (int i = 0; i < 256; i++) {

            byte[] byteValues = Arithmetic.nextBytes(Arithmetic.nextInt(1024) + 1);
            Assert.assertTrue(Arrays.equals(Compression.inflate(Compression.deflate(byteValues)), byteValues));
        }
    }
}

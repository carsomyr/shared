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

package shared.test.util;

import static java.nio.ByteOrder.BIG_ENDIAN;
import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static org.junit.Assert.assertTrue;
import static shared.codec.Base64.base64ToBytes;
import static shared.codec.Base64.base64ToDoubles;
import static shared.codec.Base64.base64ToInts;
import static shared.codec.Base64.base64ToLongs;
import static shared.codec.Base64.bytesToBase64;
import static shared.codec.Base64.doublesToBase64;
import static shared.codec.Base64.intsToBase64;
import static shared.codec.Base64.longsToBase64;
import static shared.codec.Compression.deflate;
import static shared.codec.Compression.inflate;
import static shared.codec.Hex.bytesToHex;
import static shared.codec.Hex.hexToBytes;

import java.util.Arrays;

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

        assertTrue(Arrays.equals(base64ToBytes(bytesToBase64(byteValues)), byteValues));
        assertTrue(Arrays.equals(base64ToDoubles(doublesToBase64(doubleValues)), doubleValues));
        assertTrue(Arrays.equals(base64ToInts(intsToBase64(intValues)), intValues));
        assertTrue(Arrays.equals(base64ToLongs(longsToBase64(longValues)), longValues));

        assertTrue(Arrays.equals( //
                base64ToDoubles(doublesToBase64(doubleValues, BIG_ENDIAN), BIG_ENDIAN), //
                doubleValues));
        assertTrue(Arrays.equals( //
                base64ToInts(intsToBase64(intValues, BIG_ENDIAN), BIG_ENDIAN), //
                intValues));
        assertTrue(Arrays.equals( //
                base64ToLongs(longsToBase64(longValues, BIG_ENDIAN), BIG_ENDIAN), //
                longValues));

        assertTrue(Arrays.equals( //
                base64ToDoubles(doublesToBase64(doubleValues, LITTLE_ENDIAN), LITTLE_ENDIAN), //
                doubleValues));
        assertTrue(Arrays.equals( //
                base64ToInts(intsToBase64(intValues, LITTLE_ENDIAN), LITTLE_ENDIAN), //
                intValues));
        assertTrue(Arrays.equals( //
                base64ToLongs(longsToBase64(longValues, LITTLE_ENDIAN), LITTLE_ENDIAN), //
                longValues));
    }

    /**
     * Tests reading and writing with the {@link Hex} codec.
     */
    @Test
    public void testHex() {

        int len = 1024;

        byte[] byteValues = Arithmetic.nextBytes(len);

        assertTrue(Arrays.equals(hexToBytes(bytesToHex(byteValues)), byteValues));
    }

    /**
     * Tests compression and decompression with the {@link Compression} codec.
     */
    @Test
    public void testCompress() {

        for (int i = 0; i < 256; i++) {

            byte[] byteValues = Arithmetic.nextBytes(Arithmetic.nextInt(1024) + 1);
            assertTrue(Arrays.equals(inflate(deflate(byteValues)), byteValues));
        }
    }
}

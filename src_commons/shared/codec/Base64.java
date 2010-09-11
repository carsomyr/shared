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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import shared.util.Control;

/**
 * A collection of convenience wrappers for the static methods found in <a
 * href="http://commons.apache.org/codec/">Apache Commons Codec</a>'s {@code Base64} class.
 * 
 * @author Roy Liu
 */
public class Base64 {

    /**
     * Converts the given base {@code 64} string into a {@code byte} array.
     * 
     * @param data
     *            the data.
     * @return the {@code byte} array.
     */
    final public static byte[] base64ToBytes(String data) {
        return org.apache.commons.codec.binary.Base64.decodeBase64(data);
    }

    /**
     * Converts the given {@code byte} array into a base {@code 64} string.
     * 
     * @param data
     *            the data.
     * @return the base {@code 64} string.
     */
    final public static String bytesToBase64(byte[] data) {
        return org.apache.commons.codec.binary.Base64.encodeBase64String(data);
    }

    /**
     * Converts the given base {@code 64} string into a {@code double} array.
     * 
     * @param data
     *            the data.
     * @param byteOrder
     *            the Endianness.
     * @return the {@code double} array.
     */
    final public static double[] base64ToDoubles(String data, ByteOrder byteOrder) {

        ByteBuffer bb = ByteBuffer.wrap(base64ToBytes(data)).order(byteOrder);

        int len = bb.remaining() >>> 3;

        Control.checkTrue(len << 3 == bb.remaining(), //
                "Invalid data size");

        double[] res = new double[len];

        for (int i = 0; i < len; i++) {
            res[i] = bb.getDouble();
        }

        return res;
    }

    /**
     * Converts the given base {@code 64} string into a {@code double} array.
     * 
     * @param data
     *            the data.
     * @return the {@code double} array.
     */
    final public static double[] base64ToDoubles(String data) {
        return base64ToDoubles(data, ByteOrder.nativeOrder());
    }

    /**
     * Converts the given {@code double} array into a base {@code 64} string.
     * 
     * @param values
     *            the values.
     * @param byteOrder
     *            the Endianness.
     * @return the base {@code 64} string.
     */
    final public static String doublesToBase64(double[] values, ByteOrder byteOrder) {

        int len = values.length;

        ByteBuffer bb = ByteBuffer.allocate(len << 3).order(byteOrder);

        for (double value : values) {
            bb.putDouble(value);
        }

        return bytesToBase64(bb.array());
    }

    /**
     * Converts the given {@code double} array into a base {@code 64} string.
     * 
     * @param values
     *            the values.
     * @return the base {@code 64} string.
     */
    final public static String doublesToBase64(double[] values) {
        return doublesToBase64(values, ByteOrder.nativeOrder());
    }

    /**
     * Converts the given base {@code 64} string into an {@code int} array.
     * 
     * @param data
     *            the data.
     * @param byteOrder
     *            the Endianness.
     * @return the {@code int} array.
     */
    final public static int[] base64ToInts(String data, ByteOrder byteOrder) {

        ByteBuffer bb = ByteBuffer.wrap(base64ToBytes(data)).order(byteOrder);

        int len = bb.remaining() >>> 2;

        Control.checkTrue(len << 2 == bb.remaining(), //
                "Invalid data size");

        int[] res = new int[len];

        for (int i = 0; i < len; i++) {
            res[i] = bb.getInt();
        }

        return res;
    }

    /**
     * Converts the given base {@code 64} string into an {@code int} array.
     * 
     * @param data
     *            the data.
     * @return the {@code int} array.
     */
    final public static int[] base64ToInts(String data) {
        return base64ToInts(data, ByteOrder.nativeOrder());
    }

    /**
     * Converts the given {@code int} array into a base {@code 64} string.
     * 
     * @param values
     *            the values.
     * @param byteOrder
     *            the Endianness.
     * @return the base {@code 64} string.
     */
    final public static String intsToBase64(int[] values, ByteOrder byteOrder) {

        int len = values.length;

        ByteBuffer bb = ByteBuffer.allocate(len << 2).order(byteOrder);

        for (int value : values) {
            bb.putInt(value);
        }

        return bytesToBase64(bb.array());
    }

    /**
     * Converts the given {@code int} array into a base {@code 64} string.
     * 
     * @param values
     *            the values.
     * @return the base {@code 64} string.
     */
    final public static String intsToBase64(int[] values) {
        return intsToBase64(values, ByteOrder.nativeOrder());
    }

    /**
     * Converts the given base {@code 64} string into a {@code long} array.
     * 
     * @param data
     *            the data.
     * @param byteOrder
     *            the Endianness.
     * @return the {@code long} array.
     */
    final public static long[] base64ToLongs(String data, ByteOrder byteOrder) {

        ByteBuffer bb = ByteBuffer.wrap(base64ToBytes(data)).order(byteOrder);

        int len = bb.remaining() >>> 3;

        Control.checkTrue(len << 3 == bb.remaining(), //
                "Invalid data size");

        long[] res = new long[len];

        for (int i = 0; i < len; i++) {
            res[i] = bb.getLong();
        }

        return res;
    }

    /**
     * Converts the given base {@code 64} string into a {@code long} array.
     * 
     * @param data
     *            the data.
     * @return the {@code long} array.
     */
    final public static long[] base64ToLongs(String data) {
        return base64ToLongs(data, ByteOrder.nativeOrder());
    }

    /**
     * Converts the given {@code long} array into a base {@code 64} string.
     * 
     * @param values
     *            the values.
     * @param byteOrder
     *            the Endianness.
     * @return the base {@code 64} string.
     */
    final public static String longsToBase64(long[] values, ByteOrder byteOrder) {

        int len = values.length;

        ByteBuffer bb = ByteBuffer.allocate(len << 3).order(byteOrder);

        for (long value : values) {
            bb.putLong(value);
        }

        return bytesToBase64(bb.array());
    }

    /**
     * Converts the given {@code long} array into a base {@code 64} string.
     * 
     * @param values
     *            the values.
     * @return the base {@code 64} string.
     */
    final public static String longsToBase64(long[] values) {
        return longsToBase64(values, ByteOrder.nativeOrder());
    }

    // Dummy constructor.
    Base64() {
    }
}

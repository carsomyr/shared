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

import static org.apache.commons.codec.binary.Base64.decodeBase64;
import static org.apache.commons.codec.binary.Base64.encodeBase64;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import shared.util.Control;

/**
 * A collection of convenience wrappers for the static methods found in <a
 * href="http://commons.apache.org/codec/">Apache Commons Codec</a>'s {@link org.apache.commons.codec.binary.Base64}
 * class.
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
        return decodeBase64(data.getBytes());
    }

    /**
     * Converts the given {@code byte} array into a base {@code 64} string.
     * 
     * @param data
     *            the data.
     * @return the base {@code 64} string.
     */
    final public static String bytesToBase64(byte[] data) {
        return new String(encodeBase64(data));
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

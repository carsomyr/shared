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

import static org.apache.commons.codec.binary.Hex.decodeHex;
import static org.apache.commons.codec.binary.Hex.encodeHex;

import org.apache.commons.codec.DecoderException;

/**
 * A collection of convenience wrappers for the static methods found in <a
 * href="http://commons.apache.org/codec/">Apache Commons Codec</a>'s {@link org.apache.commons.codec.binary.Hex} class.
 * 
 * @author Roy Liu
 */
public class Hex {

    /**
     * Converts the given hex string into a {@code byte} array.
     * 
     * @param data
     *            the data.
     * @return the {@code byte} array.
     */
    final public static byte[] hexToBytes(String data) {

        try {

            return decodeHex(data.toCharArray());

        } catch (DecoderException e) {

            throw new RuntimeException(e);
        }
    }

    /**
     * Converts the given {@code byte} array into a hex string.
     * 
     * @param data
     *            the data.
     * @return the hex string.
     */
    final public static String bytesToHex(byte[] data) {
        return new String(encodeHex(data));
    }

    // Dummy constructor.
    Hex() {
    }
}

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

package org.shared.array.kernel;

/**
 * Contains constant values pertaining to the reading and writing of MATLAB's MAT-file format.
 * 
 * @apiviz.exclude
 * @author Roy Liu
 */
public class MatlabIo {

    /**
     * A value such that observation in its reversed form necessitates conversion from Little-Endian values to
     * Big-Endian values, and vice versa.
     */
    final public static short ENDIANNESS = 0x4D49;

    /**
     * MATLAB's value for a signed {@code byte}.
     */
    final public static int MATLAB_MI_INT8 = 1;

    /**
     * MATLAB's value for an unsigned {@code byte}.
     */
    final public static int MATLAB_MI_UINT8 = 2;

    /**
     * MATLAB's value for a signed {@code short}.
     */
    final public static int MATLAB_MI_INT16 = 3;

    /**
     * MATLAB's value for an unsigned {@code short}.
     */
    final public static int MATLAB_MI_UINT16 = 4;

    /**
     * MATLAB's value for a signed {@code int}.
     */
    final public static int MATLAB_MI_INT32 = 5;

    /**
     * MATLAB's value for an unsigned {@code int}.
     */
    final public static int MATLAB_MI_UINT32 = 6;

    /**
     * MATLAB's value for a {@code float}.
     */
    final public static int MATLAB_MI_SINGLE = 7;

    /**
     * MATLAB's value for a {@code double}.
     */
    final public static int MATLAB_MI_DOUBLE = 9;

    /**
     * MATLAB's value for an array of {@code double}s.
     */
    final public static int MATLAB_MX_DOUBLE_CLASS = 6;

    /**
     * MATLAB's value for an array of {@code float}s.
     */
    final public static int MATLAB_MX_SINGLE_CLASS = 7;

    /**
     * MATLAB's value for an array of signed {@code byte}s.
     */
    final public static int MATLAB_MX_INT8_CLASS = 8;

    /**
     * MATLAB's value for an array of unsigned {@code byte}s.
     */
    final public static int MATLAB_MX_UINT8_CLASS = 9;

    /**
     * MATLAB's value for an array of signed {@code short}s.
     */
    final public static int MATLAB_MX_INT16_CLASS = 10;

    /**
     * MATLAB's value for an array of unsigned {@code short}s.
     */
    final public static int MATLAB_MX_UINT16_CLASS = 11;

    /**
     * MATLAB's value for an array of signed {@code int}s.
     */
    final public static int MATLAB_MX_INT32_CLASS = 12;

    /**
     * MATLAB's value for an array of unsigned {@code int}s.
     */
    final public static int MATLAB_MX_UINT32_CLASS = 13;

    /**
     * MATLAB's value for a matrix.
     */
    final public static int MATLAB_MI_MATRIX = 14;

    /**
     * An enumeration of MATLAB data types.
     */
    public enum DataType {

        /**
         * Denotes a signed {@code byte}.
         */
        MI_INT8 {

            @Override
            public int getMatlabValue() {
                return MATLAB_MI_INT8;
            }

            @Override
            public int sizeOf() {
                return 1;
            }
        }, //

        /**
         * Denotes an unsigned {@code byte}.
         */
        MI_UINT8 {

            @Override
            public int getMatlabValue() {
                return MATLAB_MI_UINT8;
            }

            @Override
            public int sizeOf() {
                return 1;
            }
        }, //

        /**
         * Denotes a signed {@code short}.
         */
        MI_INT16 {

            @Override
            public int getMatlabValue() {
                return MATLAB_MI_INT16;
            }

            @Override
            public int sizeOf() {
                return 2;
            }
        }, //

        /**
         * Denotes an unsigned {@code short}.
         */
        MI_UINT16 {

            @Override
            public int getMatlabValue() {
                return MATLAB_MI_UINT16;
            }

            @Override
            public int sizeOf() {
                return 2;
            }
        }, //

        /**
         * Denotes a signed {@code int}.
         */
        MI_INT32 {

            @Override
            public int getMatlabValue() {
                return MATLAB_MI_INT32;
            }

            @Override
            public int sizeOf() {
                return 4;
            }
        }, //

        /**
         * Denotes an unsigned {@code int}.
         */
        MI_UINT32 {

            @Override
            public int getMatlabValue() {
                return MATLAB_MI_UINT32;
            }

            @Override
            public int sizeOf() {
                return 4;
            }
        }, //

        /**
         * Denotes a {@code float}.
         */
        MI_SINGLE {

            @Override
            public int getMatlabValue() {
                return MATLAB_MI_SINGLE;
            }

            @Override
            public int sizeOf() {
                return 4;
            }
        }, //

        /**
         * Denotes a {@code double}.
         */
        MI_DOUBLE {

            @Override
            public int getMatlabValue() {
                return MATLAB_MI_DOUBLE;
            }

            @Override
            public int sizeOf() {
                return 8;
            }
        };

        /**
         * Gets the physical MATLAB value.
         */
        abstract public int getMatlabValue();

        /**
         * Gets the size of this type in {@code byte}s.
         */
        abstract public int sizeOf();
    }

    /**
     * An enumeration of MATLAB object types.
     */
    public enum ObjectType {

        /**
         * Denotes a MATLAB array of {@code double}s.
         */
        MX_DOUBLE_CLASS {

            @Override
            public int getMatlabValue() {
                return MATLAB_MX_DOUBLE_CLASS;
            }
        }, //

        /**
         * Denotes a MATLAB array of {@code float}s.
         */
        MX_SINGLE_CLASS {

            @Override
            public int getMatlabValue() {
                return MATLAB_MX_SINGLE_CLASS;
            }
        }, //

        /**
         * Denotes a MATLAB array of signed {@code byte}s.
         */
        MX_INT8_CLASS {

            @Override
            public int getMatlabValue() {
                return MATLAB_MX_INT8_CLASS;
            }
        }, //

        /**
         * Denotes a MATLAB array of unsigned {@code byte}s.
         */
        MX_UINT8_CLASS {

            @Override
            public int getMatlabValue() {
                return MATLAB_MX_UINT8_CLASS;
            }
        }, //

        /**
         * Denotes a MATLAB array of signed {@code short}s.
         */
        MX_INT16_CLASS {

            @Override
            public int getMatlabValue() {
                return MATLAB_MX_INT16_CLASS;
            }
        }, //

        /**
         * Denotes a MATLAB array of unsigned {@code short}s.
         */
        MX_UINT16_CLASS {

            @Override
            public int getMatlabValue() {
                return MATLAB_MX_UINT16_CLASS;
            }
        }, //

        /**
         * Denotes a MATLAB array of signed {@code int}s.
         */
        MX_INT32_CLASS {

            @Override
            public int getMatlabValue() {
                return MATLAB_MX_INT32_CLASS;
            }
        }, //

        /**
         * Denotes a MATLAB array of unsigned {@code int}s.
         */
        MX_UINT32_CLASS {

            @Override
            public int getMatlabValue() {
                return MATLAB_MX_UINT32_CLASS;
            }
        }, //

        /**
         * Denotes a MATLAB matrix.
         */
        MI_MATRIX {

            @Override
            public int getMatlabValue() {
                return MATLAB_MI_MATRIX;
            }
        };

        /**
         * Gets the physical MATLAB value.
         */
        abstract public int getMatlabValue();
    }

    // Dummy constructor.
    MatlabIo() {
    }
}

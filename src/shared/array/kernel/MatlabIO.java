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

package shared.array.kernel;

/**
 * Contains constant values pertaining to the reading and writing of Matlab's "mat" file format.
 * 
 * @apiviz.exclude
 * @author Roy Liu
 */
public class MatlabIO {

    /**
     * A value such that observation in its reversed form necessitates conversion from Little-Endian values to
     * Big-Endian values, and vice versa.
     */
    final public static short ENDIANNESS = 0x4D49;

    /**
     * Matlab's value for a signed {@code byte}.
     */
    final public static int MATLAB_MI_INT8 = 1;

    /**
     * Matlab's value for an unsigned {@code byte}.
     */
    final public static int MATLAB_MI_UINT8 = 2;

    /**
     * Matlab's value for a signed {@code short}.
     */
    final public static int MATLAB_MI_INT16 = 3;

    /**
     * Matlab's value for an unsigned {@code short}.
     */
    final public static int MATLAB_MI_UINT16 = 4;

    /**
     * Matlab's value for a signed {@code int}.
     */
    final public static int MATLAB_MI_INT32 = 5;

    /**
     * Matlab's value for an unsigned {@code int}.
     */
    final public static int MATLAB_MI_UINT32 = 6;

    /**
     * Matlab's value for a {@code float}.
     */
    final public static int MATLAB_MI_SINGLE = 7;

    /**
     * Matlab's value for a {@code double}.
     */
    final public static int MATLAB_MI_DOUBLE = 9;

    /**
     * Matlab's value for an array of {@code double}s.
     */
    final public static int MATLAB_MX_DOUBLE_CLASS = 6;

    /**
     * Matlab's value for an array of {@code float}s.
     */
    final public static int MATLAB_MX_SINGLE_CLASS = 7;

    /**
     * Matlab's value for an array of signed {@code byte}s.
     */
    final public static int MATLAB_MX_INT8_CLASS = 8;

    /**
     * Matlab's value for an array of unsigned {@code byte}s.
     */
    final public static int MATLAB_MX_UINT8_CLASS = 9;

    /**
     * Matlab's value for an array of signed {@code short}s.
     */
    final public static int MATLAB_MX_INT16_CLASS = 10;

    /**
     * Matlab's value for an array of unsigned {@code short}s.
     */
    final public static int MATLAB_MX_UINT16_CLASS = 11;

    /**
     * Matlab's value for an array of signed {@code int}s.
     */
    final public static int MATLAB_MX_INT32_CLASS = 12;

    /**
     * Matlab's value for an array of unsigned {@code int}s.
     */
    final public static int MATLAB_MX_UINT32_CLASS = 13;

    /**
     * Matlab's value for a matrix.
     */
    final public static int MATLAB_MI_MATRIX = 14;

    /**
     * An enumeration of Matlab data types.
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
         * Gets the physical Matlab value.
         */
        abstract public int getMatlabValue();

        /**
         * Gets the size of this type in {@code byte}s.
         */
        abstract public int sizeOf();
    }

    /**
     * An enumeration of Matlab object types.
     */
    public enum ObjectType {

        /**
         * Denotes a Matlab array of {@code double}s.
         */
        MX_DOUBLE_CLASS {

            @Override
            public int getMatlabValue() {
                return MATLAB_MX_DOUBLE_CLASS;
            }
        }, //

        /**
         * Denotes a Matlab array of {@code float}s.
         */
        MX_SINGLE_CLASS {

            @Override
            public int getMatlabValue() {
                return MATLAB_MX_SINGLE_CLASS;
            }
        }, //

        /**
         * Denotes a Matlab array of signed {@code byte}s.
         */
        MX_INT8_CLASS {

            @Override
            public int getMatlabValue() {
                return MATLAB_MX_INT8_CLASS;
            }
        }, //

        /**
         * Denotes a Matlab array of unsigned {@code byte}s.
         */
        MX_UINT8_CLASS {

            @Override
            public int getMatlabValue() {
                return MATLAB_MX_UINT8_CLASS;
            }
        }, //

        /**
         * Denotes a Matlab array of signed {@code short}s.
         */
        MX_INT16_CLASS {

            @Override
            public int getMatlabValue() {
                return MATLAB_MX_INT16_CLASS;
            }
        }, //

        /**
         * Denotes a Matlab array of unsigned {@code short}s.
         */
        MX_UINT16_CLASS {

            @Override
            public int getMatlabValue() {
                return MATLAB_MX_UINT16_CLASS;
            }
        }, //

        /**
         * Denotes a Matlab array of signed {@code int}s.
         */
        MX_INT32_CLASS {

            @Override
            public int getMatlabValue() {
                return MATLAB_MX_INT32_CLASS;
            }
        }, //

        /**
         * Denotes a Matlab array of unsigned {@code int}s.
         */
        MX_UINT32_CLASS {

            @Override
            public int getMatlabValue() {
                return MATLAB_MX_UINT32_CLASS;
            }
        }, //

        /**
         * Denotes a Matlab matrix.
         */
        MI_MATRIX {

            @Override
            public int getMatlabValue() {
                return MATLAB_MI_MATRIX;
            }
        };

        /**
         * Gets the physical Matlab value.
         */
        abstract public int getMatlabValue();
    }

    // Dummy constructor.
    MatlabIO() {
    }
}

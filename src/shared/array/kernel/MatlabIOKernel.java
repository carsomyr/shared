/**
 * <p>
 * Copyright (C) 2008 Roy Liu<br />
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

package shared.array.kernel;

import static shared.Constants.MAJOR_VERSION;
import static shared.Constants.MINOR_VERSION;
import static shared.array.kernel.MatlabIO.ENDIANNESS;
import static shared.array.kernel.MatlabIO.MATLAB_MI_DOUBLE;
import static shared.array.kernel.MatlabIO.MATLAB_MI_INT16;
import static shared.array.kernel.MatlabIO.MATLAB_MI_INT32;
import static shared.array.kernel.MatlabIO.MATLAB_MI_INT8;
import static shared.array.kernel.MatlabIO.MATLAB_MI_MATRIX;
import static shared.array.kernel.MatlabIO.MATLAB_MI_SINGLE;
import static shared.array.kernel.MatlabIO.MATLAB_MI_UINT16;
import static shared.array.kernel.MatlabIO.MATLAB_MI_UINT32;
import static shared.array.kernel.MatlabIO.MATLAB_MI_UINT8;
import static shared.array.kernel.MatlabIO.MATLAB_MX_DOUBLE_CLASS;
import static shared.array.kernel.MatlabIO.MATLAB_MX_INT16_CLASS;
import static shared.array.kernel.MatlabIO.MATLAB_MX_INT32_CLASS;
import static shared.array.kernel.MatlabIO.MATLAB_MX_INT8_CLASS;
import static shared.array.kernel.MatlabIO.MATLAB_MX_SINGLE_CLASS;
import static shared.array.kernel.MatlabIO.MATLAB_MX_UINT16_CLASS;
import static shared.array.kernel.MatlabIO.MATLAB_MX_UINT32_CLASS;
import static shared.array.kernel.MatlabIO.MATLAB_MX_UINT8_CLASS;
import static shared.array.kernel.MatlabIO.DataType.MI_DOUBLE;
import static shared.array.kernel.MatlabIO.DataType.MI_INT32;
import static shared.array.kernel.MatlabIO.DataType.MI_INT8;
import static shared.array.kernel.MatlabIO.DataType.MI_UINT32;
import static shared.array.kernel.MatlabIO.ObjectType.MI_MATRIX;
import static shared.array.kernel.MatlabIO.ObjectType.MX_DOUBLE_CLASS;
import static shared.array.kernel.MatlabIO.ObjectType.MX_INT32_CLASS;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import shared.array.Array;
import shared.array.Array.IndexingOrder;
import shared.array.ComplexArray;
import shared.array.IntegerArray;
import shared.array.RealArray;
import shared.array.kernel.MatlabIO.DataType;
import shared.util.Control;

/**
 * An implementation of {@link ArrayIOKernel} that reads and writes Matlab "mat" files.
 * 
 * @apiviz.composedOf shared.array.kernel.MatlabIOKernel.DoubleArrayDataElement
 * @apiviz.composedOf shared.array.kernel.MatlabIOKernel.IntArrayDataElement
 * @apiviz.composedOf shared.array.kernel.MatlabIOKernel.MatrixDataElement
 * @apiviz.owns shared.array.kernel.MatlabIO.DataType
 * @apiviz.owns shared.array.kernel.MatlabIO.ObjectType
 * @author Roy Liu
 */
public class MatlabIOKernel implements ArrayIOKernel {

    /**
     * A {@link RealArray} counter local to the current thread.
     */
    final protected static ThreadLocal<Integer> RealArrayCountLocal = new ThreadLocal<Integer>();

    /**
     * A {@link ComplexArray} counter local to the current thread.
     */
    final protected static ThreadLocal<Integer> ComplexArrayCountLocal = new ThreadLocal<Integer>();

    /**
     * An {@link IntegerArray} counter local to the current thread.
     */
    final protected static ThreadLocal<Integer> IntegerArrayCountLocal = new ThreadLocal<Integer>();

    /**
     * Default constructor.
     */
    public MatlabIOKernel() {
    }

    @Override
    public <T extends Array<T, E>, E> byte[] getBytes(T array) {

        final Integer count;
        final String name;

        final Object arrayObj = array;

        if (arrayObj instanceof RealArray) {

            count = RealArrayCountLocal.get();

            if (count != null) {

                name = String.format("ra_%d", count);

                RealArrayCountLocal.set(count + 1);

            } else {

                name = "ra";
            }

            return getBytes((RealArray) arrayObj, name);

        } else if (arrayObj instanceof ComplexArray) {

            count = ComplexArrayCountLocal.get();

            if (count != null) {

                name = String.format("ca_%d", count);

                ComplexArrayCountLocal.set(count + 1);

            } else {

                name = "ca";
            }

            return getBytes((ComplexArray) arrayObj, name);

        } else if (arrayObj instanceof IntegerArray) {

            count = IntegerArrayCountLocal.get();

            if (count != null) {

                name = String.format("ia_%d", count);

                IntegerArrayCountLocal.set(count + 1);

            } else {

                name = "ia";
            }

            return getBytes((IntegerArray) arrayObj, name);

        } else {

            throw new IllegalArgumentException("Invalid array type");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Array<T, ?>> T parse(byte[] data) {
        return (T) new MatrixDataElement(ByteBuffer.wrap(data)).get();
    }

    /**
     * Gets the binary representation of the "mat" file header.
     */
    final public static byte[] getHeaderBytes(String text) {

        ByteBuffer bb = ByteBuffer.allocate(128);

        int len = text.length();

        Control.checkTrue(len <= 124, //
                "The descriptive text is too long");

        bb.put(text.getBytes());

        for (int i = 0, n = 124 - len; i < n; i++) {
            bb.put((byte) ' ');
        }

        bb.putShort((short) 0x0100);
        bb.putShort(ENDIANNESS);

        return bb.array();
    }

    /**
     * Converts the given {@link RealArray} into {@code byte}s.
     */
    final public static byte[] getBytes(RealArray array, String name) {

        // Force column-major indexing.
        if (array.order() == IndexingOrder.FAR) {
            array = array.reverseOrder();
        }

        int[] dims = array.dims();
        double[] values = array.values();

        int totalSize = getDataElementSize(MI_UINT32, 2) //
                + getDataElementSize(MI_INT32, dims.length) //
                + getDataElementSize(MI_INT8, name.length()) //
                + getDataElementSize(MI_DOUBLE, values.length);

        ByteBuffer bb = ByteBuffer.allocate(totalSize + 8);

        bb.putInt(MI_MATRIX.getMatlabValue());
        bb.putInt(totalSize);

        writeDataElementHeader(bb, MI_UINT32, 2);
        bb.putInt(MX_DOUBLE_CLASS.getMatlabValue());
        bb.putInt(0);

        writeDataElementHeader(bb, MI_INT32, dims.length);
        writeDataElementBody(bb, dims);

        writeDataElementHeader(bb, MI_INT8, name.length());
        writeDataElementBody(bb, name.getBytes());

        writeDataElementHeader(bb, MI_DOUBLE, values.length);
        writeDataElementBody(bb, values);

        return bb.array();
    }

    /**
     * Converts the given {@link ComplexArray} into {@code byte}s.
     */
    final public static byte[] getBytes(ComplexArray complexArray, String name) {

        RealArray array = new RealArray(complexArray.values(), complexArray.order(), complexArray.dims()) //
                .reverseOrder();

        int[] dims = array.dims();
        double[] values = array.values();

        int totalSize = getDataElementSize(MI_UINT32, 2) //
                + getDataElementSize(MI_INT32, dims.length - 1) //
                + getDataElementSize(MI_INT8, name.length()) //
                + 2 * getDataElementSize(MI_DOUBLE, values.length >>> 1);

        ByteBuffer bb = ByteBuffer.allocate(totalSize + 8);

        bb.putInt(MI_MATRIX.getMatlabValue());
        bb.putInt(totalSize);

        writeDataElementHeader(bb, MI_UINT32, 2);
        bb.putInt(0x00000800 | MX_DOUBLE_CLASS.getMatlabValue());
        bb.putInt(0);

        writeDataElementHeader(bb, MI_INT32, dims.length - 1);
        writeDataElementBody(bb, Arrays.copyOf(dims, dims.length - 1));

        writeDataElementHeader(bb, MI_INT8, name.length());
        writeDataElementBody(bb, name.getBytes());

        writeDataElementHeader(bb, MI_DOUBLE, values.length >>> 1);
        writeDataElementBody(bb, Arrays.copyOfRange(values, 0, values.length >>> 1));

        writeDataElementHeader(bb, MI_DOUBLE, values.length >>> 1);
        writeDataElementBody(bb, Arrays.copyOfRange(values, values.length >>> 1, values.length));

        return bb.array();
    }

    /**
     * Converts the given {@link IntegerArray} into {@code byte}s.
     */
    final public static byte[] getBytes(IntegerArray array, String name) {

        // Force column-major indexing.
        if (array.order() == IndexingOrder.FAR) {
            array = array.reverseOrder();
        }

        int[] dims = array.dims();
        int[] values = array.values();

        int totalSize = getDataElementSize(MI_UINT32, 2) //
                + getDataElementSize(MI_INT32, dims.length) //
                + getDataElementSize(MI_INT8, name.length()) //
                + getDataElementSize(MI_INT32, values.length);

        ByteBuffer bb = ByteBuffer.allocate(totalSize + 8);

        bb.putInt(MI_MATRIX.getMatlabValue());
        bb.putInt(totalSize);

        writeDataElementHeader(bb, MI_UINT32, 2);
        bb.putInt(MX_INT32_CLASS.getMatlabValue());
        bb.putInt(0);

        writeDataElementHeader(bb, MI_INT32, dims.length);
        writeDataElementBody(bb, dims);

        writeDataElementHeader(bb, MI_INT8, name.length());
        writeDataElementBody(bb, name.getBytes());

        writeDataElementHeader(bb, MI_INT32, values.length);
        writeDataElementBody(bb, values);

        return bb.array();
    }

    /**
     * Converts the given {@link Array}s into "mat" format.
     * 
     * @param <T>
     *            the {@link Array} type.
     * @param <E>
     *            the {@link Array} element type.
     */
    public <T extends Array<T, E>, E> byte[] getMatBytes(T... arrays) {

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        RealArrayCountLocal.set(0);
        ComplexArrayCountLocal.set(0);
        IntegerArrayCountLocal.set(0);

        try {

            out.write(getHeaderBytes(String.format("MATLAB 5.0 MAT-file, " //
                    + "Platform: Shared Scientific Toolbox in Java %d.%02d", //
                    MAJOR_VERSION, MINOR_VERSION)));

            for (T array : arrays) {
                out.write(getBytes(array));
            }

        } catch (IOException e) {

            throw new RuntimeException(e);

        } finally {

            RealArrayCountLocal.set(null);
            ComplexArrayCountLocal.set(null);
            IntegerArrayCountLocal.set(null);
        }

        return out.toByteArray();
    }

    /**
     * Parses a series of {@link Array}s from "mat" format.
     */
    public Array<?, ?>[] parseMat(byte[] data) {

        ByteBuffer bb = ByteBuffer.wrap(data);
        bb.position(126);

        if (bb.getShort() == (short) ((ENDIANNESS >>> 8) | (ENDIANNESS << 8))) {
            bb.order(bb.order().equals(ByteOrder.BIG_ENDIAN) ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);
        }

        bb.position(124);

        Control.checkTrue(bb.getShort() == 0x0100, //
                "Invalid version");

        Control.checkTrue(bb.getShort() == ENDIANNESS, //
                "Invalid Endian indicator");

        List<Array<?, ?>> arrays = new ArrayList<Array<?, ?>>();

        for (; bb.hasRemaining();) {
            arrays.add(new MatrixDataElement(bb).get());
        }

        return arrays.toArray(new Array<?, ?>[] {});
    }

    /**
     * Writes the data element header, given the {@link DataType} and the number of elements.
     */
    final protected static void writeDataElementHeader(ByteBuffer bb, DataType type, int nElts) {

        int nBytes = type.sizeOf() * nElts;

        if (nBytes > 4 || nBytes == 0) {

            bb.putInt(type.getMatlabValue());
            bb.putInt(nBytes);

        } else {

            bb.putShort((short) nBytes);
            bb.putShort((short) type.getMatlabValue());
        }
    }

    /**
     * Writes the data element body.
     */
    final protected static void writeDataElementBody(ByteBuffer bb, byte[] arr) {

        bb.put(arr);

        if (arr.length > 4) {

            for (int i = 0, n = (8 - arr.length % 8) % 8; i < n; i++) {
                bb.put((byte) 0);
            }

        } else if (arr.length >= 1) {

            for (int i = 0, n = 4 - arr.length; i < n; i++) {
                bb.put((byte) 0);
            }
        }
    }

    /**
     * Writes the data element body.
     */
    final protected static void writeDataElementBody(ByteBuffer bb, int[] arr) {

        for (int i : arr) {
            bb.putInt(i);
        }

        if (arr.length > 1) {

            for (int i = 0, n = (2 - arr.length % 2) % 2; i < n; i++) {
                bb.putInt(0);
            }

        } else if (arr.length >= 1) {

            for (int i = 0, n = 1 - arr.length; i < n; i++) {
                bb.putInt(0);
            }
        }
    }

    /**
     * Writes the data element body.
     */
    final protected static void writeDataElementBody(ByteBuffer bb, double[] arr) {

        for (double r : arr) {
            bb.putDouble(r);
        }
    }

    /**
     * Gets the data element size, given the {@link DataType} and the number of elements.
     */
    final protected static int getDataElementSize(DataType type, int nElts) {

        int nBytes = type.sizeOf() * nElts;
        return (nBytes > 4) ? (8 + nBytes + (8 - nBytes % 8) % 8) : 8;
    }

    /**
     * A Matlab data element.
     * 
     * @param <T>
     *            the result type.
     */
    abstract protected static class DataElement<T> {

        final int initialPosition;
        final int type;
        final int size;
        final ByteBuffer bb;

        /**
         * Default constructor.
         */
        protected DataElement(ByteBuffer bb) {

            this.initialPosition = bb.position();

            int tag = bb.getInt();
            bb.position(bb.position() - 4);

            if ((tag & 0xFFFF0000) == 0) {

                this.type = bb.getInt();
                this.size = bb.getInt();

            } else {

                this.size = bb.getShort();
                this.type = bb.getShort();
            }

            this.bb = bb;
        }

        /**
         * Reads padding up to an eight {@code byte} alignment.
         */
        protected void readPadding() {

            for (int i = 0, n = (8 - (this.bb.position() - this.initialPosition) % 8) % 8; i < n; i++) {
                this.bb.get();
            }
        }

        /**
         * Gets the result.
         */
        abstract protected T get();
    }

    /**
     * A Matlab data element that is an array of {@code int}s.
     */
    protected static class IntArrayDataElement extends DataElement<int[]> {

        final int[] values;

        /**
         * Default constructor.
         */
        protected IntArrayDataElement(ByteBuffer bb) {
            super(bb);

            switch (this.type) {

            case MATLAB_MI_INT8:

                this.values = new int[this.size];

                for (int i = 0, n = this.values.length; i < n; i++) {
                    this.values[i] = bb.get();
                }

                break;

            case MATLAB_MI_UINT8:

                this.values = new int[this.size];

                for (int i = 0, n = this.values.length; i < n; i++) {
                    this.values[i] = (bb.get() & 0x000000FF);
                }

                break;

            case MATLAB_MI_INT16:

                Control.checkTrue(this.size % 2 == 0, //
                        "Invalid size");

                this.values = new int[this.size >>> 1];

                for (int i = 0, n = this.values.length; i < n; i++) {
                    this.values[i] = bb.getShort();
                }

                break;

            case MATLAB_MI_UINT16:

                Control.checkTrue(this.size % 2 == 0, //
                        "Invalid size");

                this.values = new int[this.size >>> 1];

                for (int i = 0, n = this.values.length; i < n; i++) {
                    this.values[i] = (bb.getShort() & 0x0000FFFF);
                }

                break;

            case MATLAB_MI_INT32:
            case MATLAB_MI_UINT32:

                Control.checkTrue(this.size % 4 == 0, //
                        "Invalid size");

                this.values = new int[this.size >>> 2];

                for (int i = 0, n = this.values.length; i < n; i++) {
                    this.values[i] = bb.getInt();
                }

                break;

            default:
                throw new UnsupportedOperationException("Unknown data type");
            }

            readPadding();
        }

        @Override
        protected int[] get() {
            return this.values;
        }
    }

    /**
     * A Matlab data element that is an array of {@code double}s.
     */
    protected static class DoubleArrayDataElement extends DataElement<double[]> {

        final double[] values;

        /**
         * Default constructor.
         */
        protected DoubleArrayDataElement(ByteBuffer bb) {
            super(bb);

            switch (this.type) {

            case MATLAB_MI_SINGLE:

                Control.checkTrue(this.size % 4 == 0, //
                        "Invalid size");

                this.values = new double[this.size >>> 2];

                for (int i = 0, n = this.values.length; i < n; i++) {
                    this.values[i] = bb.getFloat();
                }

                break;

            case MATLAB_MI_DOUBLE:

                Control.checkTrue(this.size % 8 == 0, //
                        "Invalid size");

                this.values = new double[this.size >>> 3];

                for (int i = 0, n = this.values.length; i < n; i++) {
                    this.values[i] = bb.getDouble();
                }

                break;

            case MATLAB_MI_INT8:

                this.values = new double[this.size];

                for (int i = 0, n = this.values.length; i < n; i++) {
                    this.values[i] = bb.get();
                }

                break;

            case MATLAB_MI_UINT8:

                this.values = new double[this.size];

                for (int i = 0, n = this.values.length; i < n; i++) {
                    this.values[i] = (bb.get() & 0x000000FF);
                }

                break;

            case MATLAB_MI_INT16:

                Control.checkTrue(this.size % 2 == 0, //
                        "Invalid size");

                this.values = new double[this.size >>> 1];

                for (int i = 0, n = this.values.length; i < n; i++) {
                    this.values[i] = bb.getShort();
                }

                break;

            case MATLAB_MI_UINT16:

                Control.checkTrue(this.size % 2 == 0, //
                        "Invalid size");

                this.values = new double[this.size >>> 1];

                for (int i = 0, n = this.values.length; i < n; i++) {
                    this.values[i] = (bb.getShort() & 0x0000FFFF);
                }

                break;

            case MATLAB_MI_INT32:
            case MATLAB_MI_UINT32:

                Control.checkTrue(this.size % 4 == 0, //
                        "Invalid size");

                this.values = new double[this.size >>> 2];

                for (int i = 0, n = this.values.length; i < n; i++) {
                    this.values[i] = bb.getInt();
                }

                break;

            default:
                throw new UnsupportedOperationException("Unknown data type");
            }

            readPadding();
        }

        @Override
        protected double[] get() {
            return this.values;
        }
    }

    /**
     * A Matlab matrix data element.
     */
    protected static class MatrixDataElement extends DataElement<Array<?, ?>> {

        final Array<?, ?> array;

        /**
         * Default constructor.
         */
        protected MatrixDataElement(ByteBuffer bb) {
            super(bb);

            int savePosition = bb.position();

            Control.checkTrue(this.type == MATLAB_MI_MATRIX, //
                    "Data element must be a Matlab matrix");

            int arrayType = new IntArrayDataElement(bb).get()[0];
            int[] dims = new IntArrayDataElement(bb).get();

            // Read in the array name and forget it.
            new IntArrayDataElement(bb);

            switch (arrayType & 0x000000FF) {

            case MATLAB_MX_DOUBLE_CLASS:
            case MATLAB_MX_SINGLE_CLASS:

                // The regular case.
                if ((arrayType & 0x00000800) != 0x00000800) {

                    this.array = new RealArray(new DoubleArrayDataElement(bb).get(), //
                            IndexingOrder.NEAR, dims).reverseOrder();

                }
                // The complex case.
                else {

                    double[] reals = new DoubleArrayDataElement(bb).get();
                    double[] complexes = new DoubleArrayDataElement(bb).get();

                    int len = Control.checkEquals(reals.length, complexes.length, //
                            "Number of real values must equal number of complex values");

                    double[] composite = new double[2 * len];

                    System.arraycopy(reals, 0, composite, 0, len);
                    System.arraycopy(complexes, 0, composite, len, len);

                    int[] newDims = Arrays.copyOf(dims, dims.length + 1);
                    newDims[newDims.length - 1] = 2;

                    RealArray tmpArray = new RealArray(composite, IndexingOrder.NEAR, newDims);
                    this.array = new ComplexArray(tmpArray.reverseOrder().values(), newDims);
                }

                break;

            case MATLAB_MX_INT8_CLASS:
            case MATLAB_MX_UINT8_CLASS:
            case MATLAB_MX_INT16_CLASS:
            case MATLAB_MX_UINT16_CLASS:
            case MATLAB_MX_INT32_CLASS:
            case MATLAB_MX_UINT32_CLASS:
                this.array = new IntegerArray(new IntArrayDataElement(bb).get(), //
                        IndexingOrder.NEAR, dims).reverseOrder();
                break;

            default:
                throw new IllegalArgumentException("Invalid Matlab matrix type");
            }

            Control.checkTrue(this.size == bb.position() - savePosition, //
                    "Actual data element size does not match expected");
        }

        @Override
        protected Array<?, ?> get() {
            return this.array;
        }
    }
}

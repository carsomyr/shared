/**
 * <p>
 * Copyright (C) 2007 Roy Liu<br />
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

package shared.array;

import static shared.array.ArrayBase.DEFAULT_ORDER;
import static shared.array.ArrayBase.FFTService;
import static shared.array.ArrayBase.FIELD_PRECISION;
import static shared.array.ArrayBase.FIELD_WIDTH;
import static shared.array.ArrayBase.formatEmptyArray;
import static shared.array.ArrayBase.formatRescale;
import static shared.array.ArrayBase.formatSlice;

import java.util.Arrays;
import java.util.Formatter;

import shared.array.kernel.MappingOps;
import shared.util.Arithmetic;
import shared.util.Control;

/**
 * An abstract base class for multidimensional arrays of real and complex values.
 * 
 * @apiviz.uses shared.array.ArrayBase
 * @param <T>
 *            the base parameterization.
 * @param <U>
 *            the "up" parameterization representing the forwards FFT type.
 * @param <D>
 *            the "down" parameterization representing the backwards FFT type.
 * @param <E>
 *            the array element type.
 * @author Roy Liu
 */
abstract public class AbstractArray<T extends AbstractArray<T, U, D, E>, U extends AbstractArray<U, ?, ?, ?>, D extends AbstractArray<D, ?, ?, ?>, E>
        extends ProtoArray<T, double[], E> {

    /**
     * An extra bit of information to help {@link #rifft()} correctly size destination arrays.
     */
    final protected int parity;

    /**
     * A value for {@link #parity} indicating that this array was not the result of {@link #rfft()}.
     */
    final protected static int INVALID_PARITY = -1;

    /**
     * Default constructor.
     */
    protected AbstractArray(double[] values, int parity, IndexingOrder order, int[] dims, int[] strides) {
        super(values, order, dims, strides);

        this.parity = parity;
    }

    /**
     * Creates an instance of the base type.
     */
    abstract protected T wrap(int parity, IndexingOrder order, int[] dims, int[] strides);

    /**
     * Creates an instance of the "up" type after a forwards FFT.
     */
    abstract protected U wrapUp(int parity, IndexingOrder order, int[] dims, int[] strides);

    /**
     * Creates an instance of the "down" type after a backwards FFT.
     */
    abstract protected D wrapDown(int parity, IndexingOrder order, int[] dims, int[] strides);

    /**
     * Gets the value at the given logical index.
     */
    public double get(int... s) {
        return this.values[physical(s)];
    }

    /**
     * Sets the value at the given logical index.
     */
    public void set(double value, int... s) {
        this.values[physical(s)] = value;
    }

    @Override
    public double[] values() {
        return this.values;
    }

    @Override
    protected T wrap(IndexingOrder order, int[] dims, int[] strides) {
        return wrap(this.parity, order, dims, strides);
    }

    @Override
    public String toString() {

        double[] values = this.values;
        int[] dims = this.dims;
        int[] strides = this.strides;

        int ndims = dims.length;
        int nrows = (ndims == 1) ? 1 : size(ndims - 2);
        int ncols = size(ndims - 1);
        int sliceSize = nrows * ncols;

        int exponent = (int) Math.log10(Arithmetic.max( //
                Arithmetic.max(values), Math.abs(Arithmetic.min(values)), 1e-128));

        Formatter f = new Formatter();

        if (values.length == 0) {

            formatEmptyArray(f, dims);

            return f.toString();
        }

        String format = String.format("%%%d.%df", FIELD_WIDTH, FIELD_PRECISION);

        values = formatRescale(f, exponent, values);

        int[] indices = MappingOps.assignMappingIndices(Arithmetic.product(dims), //
                dims, strides);

        strides = IndexingOrder.FAR.strides(dims);

        if (ndims <= 2) {

            f.format("%n");

            formatSlice(f, format, //
                    values, indices, 0, nrows, ncols, false);

            return f.toString();
        }

        for (int offset = 0, m = values.length; offset < m; offset += sliceSize) {

            f.format("%n[slice (");

            for (int i = 0, n = ndims - 2, offsetAcc = offset; i < n; offsetAcc %= strides[i], i++) {
                f.format("%d, ", offsetAcc / strides[i]);
            }

            f.format(":, :)]%n");

            formatSlice(f, format, //
                    values, indices, offset, nrows, ncols, false);
        }

        return f.toString();
    }

    /**
     * Computes the reduced FFT of this array.
     */
    protected U rfft() {

        int[] newDims = rfftDimensions();

        // Very important: Derive the original size from the parity.
        U dst = wrapUp(this.dims[this.dims.length - 1] % 2, DEFAULT_ORDER, newDims, this.order.strides(newDims));

        FFTService.rfft(this.dims, values(), dst.values());

        return dst;
    }

    /**
     * Computes the reduced IFFT of this array.
     */
    protected D rifft() {

        int[] newDims = rifftDimensions();

        D dst = wrapDown(INVALID_PARITY, DEFAULT_ORDER, newDims, this.order.strides(newDims));

        FFTService.rifft(dst.dims, values(), dst.values());

        return dst;
    }

    /**
     * Computes the full FFT of this array.
     */
    protected T fft() {
        return transform(+1);
    }

    /**
     * Computes the full IFFT of this array.
     */
    protected T ifft() {
        return transform(-1);
    }

    /**
     * Gets the reduced FFT dimensions.
     */
    protected int[] rfftDimensions() {

        int ndims = this.dims.length;
        int lastDimSize = (this.dims[ndims - 1] / 2 + 1);

        int[] dimsModified = Arrays.copyOf(this.dims, ndims + 1);
        dimsModified[ndims - 1] = lastDimSize;
        dimsModified[ndims] = 2;

        return dimsModified;
    }

    /**
     * Gets the reduced IFFT dimensions.
     */
    protected int[] rifftDimensions() {

        Control.checkTrue(this.parity != INVALID_PARITY, //
                "Array must have valid parity");

        int ndims = this.dims.length;
        int lastDimSize = (this.dims[ndims - 2] - 1) * 2 + this.parity;

        int[] dimsModified = Arrays.copyOf(this.dims, ndims - 1);
        dimsModified[ndims - 2] = lastDimSize;

        return dimsModified;
    }

    /**
     * Checks that this array has storage order {@link ArrayBase#DEFAULT_ORDER}.
     */
    protected void checkMatrixOrder() {
        Control.checkTrue(this.order == DEFAULT_ORDER, //
                "Array must have row major indexing");
    }

    /**
     * Checks that this array's parity is {@link #INVALID_PARITY}.
     */
    protected void checkInvalidParity() {
        Control.checkTrue(this.parity == INVALID_PARITY, //
                "Array must have invalid parity");
    }

    /**
     * Checks that two {@link AbstractArray}s have the same size and underlying {@link Array.IndexingOrder}.
     * 
     * @param b
     *            the {@link AbstractArray} to compare to.
     * @return the common {@link #parity} value.
     */
    protected int checkShape(T b) {

        AbstractArray<?, ?, ?, ?> a = this;

        final int parity = Control.checkEquals(a.parity, b.parity, //
                "Parity mismatch");

        Control.checkTrue(a.order == b.order, //
                "Indexing orders do not match");

        Control.checkTrue(Arrays.equals(a.dims, b.dims), //
                "Dimensions do not match");

        return parity;
    }

    /**
     * Transforms this array in the forwards/backwards FFT direction.
     * 
     * @param direction
     *            the FFT direction.
     * @return the transformed array.
     */
    @SuppressWarnings("unchecked")
    protected T transform(int direction) {

        checkInvalidParity();

        T a = (T) this;

        T res = wrap(INVALID_PARITY, DEFAULT_ORDER, a.dims, a.strides);

        switch (direction) {

        case +1:
            FFTService.fft(Arrays.copyOf(a.dims, a.dims.length - 1), a.values, res.values);
            break;

        case -1:
            FFTService.ifft(Arrays.copyOf(a.dims, a.dims.length - 1), a.values, res.values);
            break;

        default:
            throw new IllegalArgumentException("Direction not recognized");
        }

        return res;
    }
}

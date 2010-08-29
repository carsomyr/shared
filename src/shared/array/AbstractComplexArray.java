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
import static shared.array.ArrayBase.FIELD_PRECISION;
import static shared.array.ArrayBase.FIELD_WIDTH;
import static shared.array.ArrayBase.OpKernel;
import static shared.array.ArrayBase.formatEmptyArray;
import static shared.array.ArrayBase.formatRescale;
import static shared.array.ArrayBase.formatSlice;

import java.util.Arrays;
import java.util.Formatter;

import shared.array.kernel.ArrayKernel;
import shared.array.kernel.MappingOps;
import shared.util.Arithmetic;
import shared.util.Control;

/**
 * An abstract base class for arrays of complex values.
 * 
 * @apiviz.owns shared.array.AbstractComplexArray.Complex
 * @param <C>
 *            the complex array type.
 * @param <R>
 *            the real array type.
 * @author Roy Liu
 */
abstract public class AbstractComplexArray<C extends AbstractComplexArray<C, R>, R extends AbstractRealArray<R, C>>
        extends AbstractArray<C, C, R, AbstractComplexArray.Complex> {

    /**
     * Default constructor.
     */
    protected AbstractComplexArray(double[] values, int parity, int[] dims, int[] strides) {
        super(values, parity, DEFAULT_ORDER, dims, strides);
    }

    /**
     * Shifts the entries of this array to the zero frequency component.
     */
    public C fftShift() {
        return fftShift(+1);
    }

    /**
     * Undoes the effects of {@link #fftShift()}.
     */
    public C ifftShift() {
        return fftShift(-1);
    }

    /**
     * Depending on the direction, performs an {@link #fftShift()} or {@link #ifftShift()} of this array.
     * 
     * @param direction
     *            the shift direction.
     * @return the shifted array.
     */
    protected C fftShift(int direction) {

        checkInvalidParity();

        int nDims = this.dims.length;
        int[] shift = new int[nDims];

        for (int i = 0, n = nDims - 1; i < n; i++) {
            shift[i] = direction * (this.dims[i] / 2);
        }

        return shift(shift);
    }

    @Override
    public String toString() {

        double[] values = this.values;
        int[] dims = this.dims;
        int[] strides = this.strides;

        int nDims = dims.length;
        int nRows = (nDims == 2) ? 1 : size(nDims - 3);
        int nCols = size(nDims - 2);
        int sliceSize = nRows * nCols * 2;

        int exponent = (int) Math.log10(Arithmetic.max( //
                Arithmetic.max(values), Math.abs(Arithmetic.min(values)), 1e-128));

        Formatter f = new Formatter();

        if (values.length == 0) {

            formatEmptyArray(f, dims);

            return f.toString();
        }

        String format = String.format("%%%d.%df +%%%d.%dfi", //
                FIELD_WIDTH, FIELD_PRECISION, FIELD_WIDTH, FIELD_PRECISION);

        values = formatRescale(f, exponent, values);

        int[] indices = MappingOps.assignMappingIndices(Arithmetic.product(dims), //
                dims, strides);

        strides = IndexingOrder.FAR.strides(dims);

        if (nDims <= 3) {

            f.format("%n");

            formatSlice(f, format, //
                    values, indices, 0, nRows, nCols, true);

            return f.toString();
        }

        for (int offset = 0, m = values.length; offset < m; offset += sliceSize) {

            f.format("%n[slice (");

            for (int i = 0, n = nDims - 3, offsetAcc = offset; i < n; offsetAcc %= strides[i], i++) {
                f.format("%d, ", offsetAcc / strides[i]);
            }

            f.format(":, :)]%n");

            formatSlice(f, format, //
                    values, indices, offset, nRows, nCols, true);
        }

        return f.toString();
    }

    @Override
    protected C wrap(Complex value, IndexingOrder order, int[] dims, int[] strides) {
        return wrap(order, dims, strides).uFill(value.re, value.im);
    }

    @Override
    protected C wrapUp(int parity, IndexingOrder order, int[] dims, int[] strides) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int[] rifftDimensions() {
        return super.rifftDimensions();
    }

    @Override
    public R rifft() {
        return super.rifft();
    }

    @Override
    public C fft() {
        return super.fft();
    }

    @Override
    public C ifft() {
        return super.ifft();
    }

    @Override
    public C subarray(int... bounds) {

        Control.checkTrue((bounds[bounds.length - 1] - bounds[bounds.length - 2]) == 2, //
                "Invalid subarray bounds for complex-valued array");

        return super.subarray(bounds);
    }

    @Override
    public C tile(int... repetitions) {

        Control.checkTrue(repetitions[repetitions.length - 1] == 1, //
                "Invalid tile repetitions for complex-valued array");

        return super.tile(repetitions);
    }

    @Override
    public C transpose(int... permutation) {

        Control.checkTrue(permutation[permutation.length - 1] == permutation.length - 1, //
                "Invalid transpose permutation for complex-valued array");

        return super.transpose(permutation);
    }

    @Override
    public C reshape(int... dims) {

        Control.checkTrue(dims[dims.length - 1] == 2, //
                "Invalid reshape dimensions for complex-valued array");

        return super.reshape(dims);
    }

    @Override
    public C reverseOrder() {
        throw new UnsupportedOperationException("Cannot reverse storage orders of complex-valued arrays");
    }

    @Override
    public Class<Complex> getComponentType() {
        return Complex.class;
    }

    /**
     * Creates an {@link AbstractRealArray} from the complex magnitudes of this array's elements.
     */
    public R torAbs() {
        return applyKernelComplexToRealOperation(ArrayKernel.CTOR_ABS);
    }

    /**
     * Creates an {@link AbstractRealArray} from the real parts of this array's elements.
     */
    public R torRe() {
        return applyKernelComplexToRealOperation(ArrayKernel.CTOR_RE);
    }

    /**
     * Creates an {@link AbstractRealArray} from the imaginary parts of this array's elements.
     */
    public R torIm() {
        return applyKernelComplexToRealOperation(ArrayKernel.CTOR_IM);
    }

    /**
     * Computes the elementwise addition.
     */
    public C eAdd(C array) {
        return applyKernelElementwiseOperation(array, ArrayKernel.CE_ADD);
    }

    /**
     * Computes the elementwise subtraction.
     */
    public C eSub(C array) {
        return applyKernelElementwiseOperation(array, ArrayKernel.CE_SUB);
    }

    /**
     * Computes the elementwise multiplication.
     */
    public C eMul(C array) {
        return applyKernelElementwiseOperation(array, ArrayKernel.CE_MUL);
    }

    /**
     * Computes the elementwise division.
     */
    public C eDiv(C array) {
        return applyKernelElementwiseOperation(array, ArrayKernel.CE_DIV);
    }

    /**
     * Computes the left elementwise addition.
     */
    public C lAdd(C array) {
        return applyKernelLeftElementwiseOperation(array, ArrayKernel.CE_ADD);
    }

    /**
     * Computes the left elementwise subtraction.
     */
    public C lSub(C array) {
        return applyKernelLeftElementwiseOperation(array, ArrayKernel.CE_SUB);
    }

    /**
     * Computes the left elementwise multiplication.
     */
    public C lMul(C array) {
        return applyKernelLeftElementwiseOperation(array, ArrayKernel.CE_MUL);
    }

    /**
     * Computes the left elementwise division.
     */
    public C lDiv(C array) {
        return applyKernelLeftElementwiseOperation(array, ArrayKernel.CE_DIV);
    }

    /**
     * Mutatively exponentiates the elements to the base {@link Math#E}.
     */
    public C uExp() {
        return applyKernelComplexUnaryOperation(Double.NaN, Double.NaN, ArrayKernel.CU_EXP);
    }

    /**
     * Mutatively randomizes the elements.
     */
    public C uRnd(double aRe, double aIm) {
        return applyKernelComplexUnaryOperation(aRe, aIm, ArrayKernel.CU_RND);
    }

    /**
     * Mutatively takes the complex conjugates of the elements.
     */
    public C uConj() {
        return applyKernelComplexUnaryOperation(Double.NaN, Double.NaN, ArrayKernel.CU_CONJ);
    }

    /**
     * Mutatively takes the cosine of the elements.
     */
    public C uCos() {
        return applyKernelComplexUnaryOperation(Double.NaN, Double.NaN, ArrayKernel.CU_COS);
    }

    /**
     * Mutatively takes the sine of the elements.
     */
    public C uSin() {
        return applyKernelComplexUnaryOperation(Double.NaN, Double.NaN, ArrayKernel.CU_SIN);
    }

    /**
     * Mutatively adds the argument to the elements.
     */
    public C uAdd(double aRe, double aIm) {
        return applyKernelComplexUnaryOperation(aRe, aIm, ArrayKernel.CU_ADD);
    }

    /**
     * Mutatively multiplies the elements by the argument.
     */
    public C uMul(double aRe, double aIm) {
        return applyKernelComplexUnaryOperation(aRe, aIm, ArrayKernel.CU_MUL);
    }

    /**
     * Mutatively fills this array with the argument.
     */
    public C uFill(double aRe, double aIm) {
        return applyKernelComplexUnaryOperation(aRe, aIm, ArrayKernel.CU_FILL);
    }

    /**
     * Mutatively shuffles this array.
     */
    public C uShuffle() {
        return applyKernelComplexUnaryOperation(Double.NaN, Double.NaN, ArrayKernel.CU_SHUFFLE);
    }

    /**
     * Computes the sum over the elements.
     */
    public double[] aSum() {
        return applyKernelComplexAccumulatorOperation(ArrayKernel.CA_SUM);
    }

    /**
     * Computes the product over the elements.
     */
    public double[] aProd() {
        return applyKernelComplexAccumulatorOperation(ArrayKernel.CA_PROD);
    }

    /**
     * Computes the mean over the elements.
     */
    public double[] aMean() {

        int len = values().length / 2;

        double[] res = aSum();
        res[0] /= len;
        res[1] /= len;

        return res;
    }

    /**
     * Supports the a* series of operations.
     */
    protected double[] applyKernelComplexAccumulatorOperation(int type) {
        return OpKernel.caOp(type, this.values);
    }

    /**
     * Supports the tor* series of operations.
     */
    @SuppressWarnings("unchecked")
    protected R applyKernelComplexToRealOperation(int type) {

        checkInvalidParity();

        C a = (C) this;

        int[] newDims = Arrays.copyOf(a.dims, a.dims.length - 1);

        R res = wrapDown(INVALID_PARITY, a.order, newDims, a.order.strides(newDims));

        OpKernel.convert(type, a.values, true, res.values, false);

        return res;
    }

    /**
     * Supports the e* series of operations.
     */
    @SuppressWarnings("unchecked")
    protected C applyKernelElementwiseOperation(C b, int type) {

        int parity = checkShape(b);

        C a = (C) this;

        C res = wrap(parity, a.order, a.dims, a.strides);

        OpKernel.eOp(type, a.values, b.values, res.values, true);

        return res;
    }

    /**
     * Supports the l* series of operations.
     */
    @SuppressWarnings("unchecked")
    protected C applyKernelLeftElementwiseOperation(C b, int type) {

        checkShape(b);

        C a = (C) this;

        OpKernel.eOp(type, a.values, b.values, a.values, false);

        return a;
    }

    /**
     * Supports the u* series of operations.
     */
    @SuppressWarnings("unchecked")
    protected C applyKernelComplexUnaryOperation(double aRe, double aIm, int type) {

        OpKernel.cuOp(type, aRe, aIm, this.values);

        return (C) this;
    }

    /**
     * A representation of complex numbers.
     */
    @SuppressWarnings("serial")
    public static class Complex extends Number implements Comparable<Complex> {

        /**
         * The real part.
         */
        final public double re;

        /**
         * The imaginary part.
         */
        final public double im;

        /**
         * Default constructor.
         * 
         * @param re
         *            the real part.
         * @param im
         *            the imaginary part.
         */
        public Complex(double re, double im) {

            this.re = re;
            this.im = im;
        }

        /**
         * Calculates the complex magnitude.
         * 
         * @return the magnitude.
         */
        public double magnitude() {
            return Math.sqrt(this.re * this.re + this.im * this.im);
        }

        /**
         * Checks if the real and imaginary parts are equal.
         */
        @Override
        public boolean equals(Object o) {

            if (!(o instanceof Complex)) {
                return false;
            }

            Complex c = (Complex) o;
            return (this.re == c.re) && (this.im == c.im);
        }

        /**
         * Fulfills the {@link Object#hashCode()} contract.
         */
        @Override
        public int hashCode() {
            return (int) (Double.doubleToRawLongBits(this.re) ^ Double.doubleToRawLongBits(this.im));
        }

        /**
         * Does a comparison on the basis of magnitude.
         */
        @Override
        public int compareTo(Complex b) {
            return Double.compare(magnitude(), b.magnitude());
        }

        /**
         * Gets the {@code double} value.
         */
        @Override
        public double doubleValue() {
            return magnitude();
        }

        /**
         * Gets the {@code float} value.
         */
        @Override
        public float floatValue() {
            return new Double(magnitude()).floatValue();
        }

        /**
         * Gets the {@code int} value.
         */
        @Override
        public int intValue() {
            return new Double(magnitude()).intValue();
        }

        /**
         * Gets the {@code long} value.
         */
        @Override
        public long longValue() {
            return new Double(magnitude()).longValue();
        }

        /**
         * Gets the {@code byte} value.
         */
        @Override
        public byte byteValue() {
            return new Double(magnitude()).byteValue();
        }

        /**
         * Creates a human-readable representation of this number.
         */
        @Override
        public String toString() {
            return String.format("%f + %fi", this.re, this.im);
        }
    }
}

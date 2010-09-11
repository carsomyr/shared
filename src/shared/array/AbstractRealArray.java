/**
 * <p>
 * Copyright (c) 2007 Roy Liu<br>
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
import static shared.array.ArrayBase.OpKernel;

import java.util.Arrays;

import shared.array.kernel.ArrayKernel;

/**
 * An abstract base class for arrays of real values.
 * 
 * @apiviz.has shared.array.AbstractRealArray.RealMap - - - argument
 * @apiviz.has shared.array.AbstractRealArray.RealReduce - - - argument
 * @param <R>
 *            the real array type.
 * @param <C>
 *            the complex array type.
 * @author Roy Liu
 */
abstract public class AbstractRealArray<R extends AbstractRealArray<R, C>, C extends AbstractComplexArray<C, R>>
        extends AbstractArray<R, C, R, Double> {

    /**
     * Default constructor.
     */
    protected AbstractRealArray(double[] values, IndexingOrder order, int[] dims, int[] strides) {
        super(values, INVALID_PARITY, order, dims, strides);
    }

    @Override
    protected R wrap(Double value, IndexingOrder order, int[] dims, int[] strides) {
        return wrap(order, dims, strides).uFill(value);
    }

    @Override
    protected R wrapDown(int parity, IndexingOrder order, int[] dims, int[] strides) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int[] rfftDimensions() {
        return super.rfftDimensions();
    }

    @Override
    public C rfft() {
        return super.rfft();
    }

    @Override
    public Class<Double> getComponentType() {
        return Double.class;
    }

    /**
     * Creates an {@link AbstractComplexArray} with the real parts set to this array's elements.
     */
    public C tocRe() {
        return applyKernelRealToComplexOperation(ArrayKernel.R_TO_C_RE);
    }

    /**
     * Creates an {@link AbstractComplexArray} with the imaginary parts set to this array's elements.
     */
    public C tocIm() {
        return applyKernelRealToComplexOperation(ArrayKernel.R_TO_C_IM);
    }

    /**
     * Mutatively exponentiates the elements to the base {@link Math#E}.
     */
    public R uExp() {
        return applyKernelRealUnaryOperation(Double.NaN, ArrayKernel.RU_EXP);
    }

    /**
     * Mutatively takes the cosine of the elements.
     */
    public R uCos() {
        return applyKernelRealUnaryOperation(Double.NaN, ArrayKernel.RU_COS);
    }

    /**
     * Mutatively takes the sine of the elements.
     */
    public R uSin() {
        return applyKernelRealUnaryOperation(Double.NaN, ArrayKernel.RU_SIN);
    }

    /**
     * Mutatively takes the arctangent of the elements.
     */
    public R uAtan() {
        return applyKernelRealUnaryOperation(Double.NaN, ArrayKernel.RU_ATAN);
    }

    /**
     * Mutatively randomizes the elements.
     */
    public R uRnd(double a) {
        return applyKernelRealUnaryOperation(a, ArrayKernel.RU_RND);
    }

    /**
     * Mutatively takes the natural logarithm of the elements.
     */
    public R uLog() {
        return applyKernelRealUnaryOperation(Double.NaN, ArrayKernel.RU_LOG);
    }

    /**
     * Mutatively takes the absolute value of the elements.
     */
    public R uAbs() {
        return applyKernelRealUnaryOperation(Double.NaN, ArrayKernel.RU_ABS);
    }

    /**
     * Mutatively takes the elements to the power of the argument.
     */
    public R uPow(double a) {
        return applyKernelRealUnaryOperation(a, ArrayKernel.RU_POW);
    }

    /**
     * Mutatively adds the argument to the elements.
     */
    public R uAdd(double a) {
        return applyKernelRealUnaryOperation(a, ArrayKernel.RU_ADD);
    }

    /**
     * Mutatively multiplies the elements by the argument.
     */
    public R uMul(double a) {
        return applyKernelRealUnaryOperation(a, ArrayKernel.RU_MUL);
    }

    /**
     * Mutatively takes the square root of the elements.
     */
    public R uSqrt() {
        return applyKernelRealUnaryOperation(Double.NaN, ArrayKernel.RU_SQRT);
    }

    /**
     * Mutatively squares the elements.
     */
    public R uSqr() {
        return applyKernelRealUnaryOperation(Double.NaN, ArrayKernel.RU_SQR);
    }

    /**
     * Mutatively takes the multiplicative inverse of the elements.
     */
    public R uInv(double a) {
        return applyKernelRealUnaryOperation(a, ArrayKernel.RU_INV);
    }

    /**
     * Mutatively fills this array with the argument.
     */
    public R uFill(double a) {
        return applyKernelRealUnaryOperation(a, ArrayKernel.RU_FILL);
    }

    /**
     * Mutatively shuffles this array.
     */
    public R uShuffle() {
        return applyKernelRealUnaryOperation(Double.NaN, ArrayKernel.RU_SHUFFLE);
    }

    /**
     * Computes the elementwise addition.
     */
    public R eAdd(R array) {
        return applyKernelElementwiseOperation(array, ArrayKernel.RE_ADD);
    }

    /**
     * Computes the elementwise subtraction.
     */
    public R eSub(R array) {
        return applyKernelElementwiseOperation(array, ArrayKernel.RE_SUB);
    }

    /**
     * Computes the elementwise multiplication.
     */
    public R eMul(R array) {
        return applyKernelElementwiseOperation(array, ArrayKernel.RE_MUL);
    }

    /**
     * Computes the elementwise division.
     */
    public R eDiv(R array) {
        return applyKernelElementwiseOperation(array, ArrayKernel.RE_DIV);
    }

    /**
     * Computes the elementwise maximum.
     */
    public R eMax(R array) {
        return applyKernelElementwiseOperation(array, ArrayKernel.RE_MAX);
    }

    /**
     * Computes the elementwise minimum.
     */
    public R eMin(R array) {
        return applyKernelElementwiseOperation(array, ArrayKernel.RE_MIN);
    }

    /**
     * Computes the left elementwise addition.
     */
    public R lAdd(R array) {
        return applyKernelLeftElementwiseOperation(array, ArrayKernel.RE_ADD);
    }

    /**
     * Computes the left elementwise subtraction.
     */
    public R lSub(R array) {
        return applyKernelLeftElementwiseOperation(array, ArrayKernel.RE_SUB);
    }

    /**
     * Computes the left elementwise multiplication.
     */
    public R lMul(R array) {
        return applyKernelLeftElementwiseOperation(array, ArrayKernel.RE_MUL);
    }

    /**
     * Computes the left elementwise division.
     */
    public R lDiv(R array) {
        return applyKernelLeftElementwiseOperation(array, ArrayKernel.RE_DIV);
    }

    /**
     * Computes the left elementwise maximum.
     */
    public R lMax(R array) {
        return applyKernelLeftElementwiseOperation(array, ArrayKernel.RE_MAX);
    }

    /**
     * Computes the left elementwise minimum.
     */
    public R lMin(R array) {
        return applyKernelLeftElementwiseOperation(array, ArrayKernel.RE_MIN);
    }

    /**
     * Computes the sum over the elements.
     */
    public double aSum() {
        return applyKernelRealAccumulatorOperation(ArrayKernel.RA_SUM);
    }

    /**
     * Computes the product over the elements.
     */
    public double aProd() {
        return applyKernelRealAccumulatorOperation(ArrayKernel.RA_PROD);
    }

    /**
     * Computes the maximum over the elements.
     */
    public double aMax() {
        return applyKernelRealAccumulatorOperation(ArrayKernel.RA_MAX);
    }

    /**
     * Computes the variance over the elements.
     */
    public double aVar() {
        return applyKernelRealAccumulatorOperation(ArrayKernel.RA_VAR);
    }

    /**
     * Computes the entropy over the elements.
     */
    public double aEnt() {
        return applyKernelRealAccumulatorOperation(ArrayKernel.RA_ENT);
    }

    /**
     * Computes the minimum over the elements.
     */
    public double aMin() {
        return applyKernelRealAccumulatorOperation(ArrayKernel.RA_MIN);
    }

    /**
     * Computes the mean over the elements.
     */
    public double aMean() {
        return aSum() / values().length;
    }

    /**
     * Computes the sum along the given dimensions.
     */
    public R rSum(int... opDims) {
        return applyKernelRealReduceOperation(ArrayKernel.RR_SUM, opDims);
    }

    /**
     * Computes the product along the given dimensions.
     */
    public R rProd(int... opDims) {
        return applyKernelRealReduceOperation(ArrayKernel.RR_PROD, opDims);
    }

    /**
     * Computes the maximum along the given dimensions.
     */
    public R rMax(int... opDims) {
        return applyKernelRealReduceOperation(ArrayKernel.RR_MAX, opDims);
    }

    /**
     * Computes the minimum along the given dimensions.
     */
    public R rMin(int... opDims) {
        return applyKernelRealReduceOperation(ArrayKernel.RR_MIN, opDims);
    }

    /**
     * Computes the mean along the given dimensions.
     */
    public R rMean(int... opDims) {

        int acc = 1;

        for (int dim : opDims) {
            acc *= size(dim);
        }

        return rSum(opDims).uMul(1.0 / acc);
    }

    /**
     * Computes the variance along the given dimensions.
     */
    public R rVar(int... opDims) {
        return applyKernelRealReduceOperation(ArrayKernel.RR_VAR, opDims);
    }

    /**
     * Finds the maximum values along the given dimension.
     */
    public IntegerArray iMax(int dim) {
        return applyKernelRealIndexOperation(ArrayKernel.RI_MAX, dim);
    }

    /**
     * Finds the minimum values along the given dimension.
     */
    public IntegerArray iMin(int dim) {
        return applyKernelRealIndexOperation(ArrayKernel.RI_MIN, dim);
    }

    /**
     * Finds all zeros along the given dimension.
     */
    public IntegerArray iZero(int dim) {
        return applyKernelRealIndexOperation(ArrayKernel.RI_ZERO, dim);
    }

    /**
     * Finds all greater-than-zeros along the given dimension.
     */
    public IntegerArray iGZero(int dim) {
        return applyKernelRealIndexOperation(ArrayKernel.RI_GZERO, dim);
    }

    /**
     * Finds all less-than-zeros along the given dimension.
     */
    public IntegerArray iLZero(int dim) {
        return applyKernelRealIndexOperation(ArrayKernel.RI_LZERO, dim);
    }

    /**
     * Sorts along the given dimension.
     */
    public IntegerArray iSort(int dim) {
        return applyKernelRealIndexOperation(ArrayKernel.RI_SORT, dim);
    }

    /**
     * Equates to calling {@link #iMax(int)} with argument {@code -1}.
     */
    public IntegerArray iMax() {
        return applyKernelRealIndexOperation(ArrayKernel.RI_MAX, -1);
    }

    /**
     * Equates to calling {@link #iMin(int)} with argument {@code -1}.
     */
    public IntegerArray iMin() {
        return applyKernelRealIndexOperation(ArrayKernel.RI_MIN, -1);
    }

    /**
     * Equates to calling {@link #iZero(int)} with argument {@code -1}.
     */
    public IntegerArray iZero() {
        return applyKernelRealIndexOperation(ArrayKernel.RI_ZERO, -1);
    }

    /**
     * Equates to calling {@link #iGZero(int)} with argument {@code -1}.
     */
    public IntegerArray iGZero() {
        return applyKernelRealIndexOperation(ArrayKernel.RI_GZERO, -1);
    }

    /**
     * Equates to calling {@link #iLZero(int)} with argument {@code -1}.
     */
    public IntegerArray iLZero() {
        return applyKernelRealIndexOperation(ArrayKernel.RI_LZERO, -1);
    }

    /**
     * Equates to calling {@link #iSort(int)} with argument {@code -1}.
     */
    public IntegerArray iSort() {
        return applyKernelRealIndexOperation(ArrayKernel.RI_SORT, -1);
    }

    /**
     * Takes the sum along the given dimensions.
     */
    public R dSum(int... opDims) {
        return applyKernelRealDimensionOperation(ArrayKernel.RD_SUM, opDims);
    }

    /**
     * Takes the product along the given dimensions.
     */
    public R dProd(int... opDims) {
        return applyKernelRealDimensionOperation(ArrayKernel.RD_PROD, opDims);
    }

    /**
     * Supports the a* series of operations.
     */
    protected double applyKernelRealAccumulatorOperation(int type) {
        return OpKernel.raOp(type, this.values);
    }

    /**
     * Supports the e* series of operations.
     */
    @SuppressWarnings("unchecked")
    protected R applyKernelElementwiseOperation(R b, int type) {

        checkShape(b);

        R a = (R) this;

        R res = wrap(INVALID_PARITY, a.order, a.dims, a.strides);

        OpKernel.eOp(type, a.values, b.values, res.values, false);

        return res;
    }

    /**
     * Supports the l* series of operations.
     */
    @SuppressWarnings("unchecked")
    protected R applyKernelLeftElementwiseOperation(R b, int type) {

        checkShape(b);

        R a = (R) this;

        OpKernel.eOp(type, a.values, b.values, a.values, false);

        return a;
    }

    /**
     * Supports the toc* series of operations.
     */
    @SuppressWarnings("unchecked")
    protected C applyKernelRealToComplexOperation(int type) {

        checkMatrixOrder();

        R a = (R) this;

        int[] newDims = Arrays.copyOf(a.dims, a.dims.length + 1);
        newDims[newDims.length - 1] = 2;

        C res = wrapUp(INVALID_PARITY, DEFAULT_ORDER, newDims, a.order.strides(newDims));

        OpKernel.convert(type, a.values, false, res.values, true);

        return res;
    }

    /**
     * Supports the u* series of operations.
     */
    @SuppressWarnings("unchecked")
    protected R applyKernelRealUnaryOperation(double a, int type) {

        OpKernel.ruOp(type, a, this.values);

        return (R) this;
    }

    /**
     * Supports the r* series of operations.
     */
    @SuppressWarnings("unchecked")
    protected R applyKernelRealReduceOperation(int type, int[] opDims) {

        R a = (R) this;

        int[] newDims = a.dims.clone();

        for (int dim : opDims) {

            // In case the dimension is 0.
            newDims[dim] = Math.min(a.dims[dim], 1);
        }

        R res = wrap(INVALID_PARITY, a.order, newDims, a.order.strides(newDims));

        OpKernel.rrOp(type, //
                a.values, a.dims, a.strides, //
                res.values, res.dims, res.strides, //
                opDims);

        return res;
    }

    /**
     * Supports the i* series of operations.
     */
    @SuppressWarnings("unchecked")
    protected IntegerArray applyKernelRealIndexOperation(int type, int dim) {

        R a = (R) this;

        IntegerArray res = new IntegerArray(a.order, a.dims);

        OpKernel.riOp(type, //
                a.values, a.dims, a.strides, res.values, //
                dim);

        return res;
    }

    /**
     * Supports the d* series of operations.
     */
    @SuppressWarnings("unchecked")
    protected R applyKernelRealDimensionOperation(int type, int[] opDims) {

        R a = (R) this;

        R res = wrap(INVALID_PARITY, a.order, a.dims, a.strides);

        OpKernel.rdOp(type, //
                a.values, a.dims, a.strides, res.values, //
                opDims);

        return res;
    }

    /**
     * Mutatively maps the elements.
     * 
     * @param rm
     *            the {@link RealMap}.
     */
    @SuppressWarnings("unchecked")
    public R map(RealMap rm) {

        R a = (R) this;

        int[] logical = new int[a.dims.length];

        for (int i = 0, n = a.values.length; i < n; i++) {
            a.values[i] = rm.apply(a.values[i], ArrayBase.logical(i, a.strides, logical));
        }

        return a;
    }

    /**
     * Reduces the elements.
     * 
     * @param rr
     *            the {@link RealReduce}.
     * @return the reduction result.
     */
    @SuppressWarnings("unchecked")
    public double reduce(RealReduce rr) {

        R a = (R) this;

        int[] logical = new int[a.dims.length];

        for (int i = 0, n = a.values.length; i < n; i++) {
            rr.apply(a.values[i], ArrayBase.logical(i, a.strides, logical));
        }

        return rr.get();
    }

    /**
     * Defines an elementwise "map" operation over real-valued multidimensional arrays.
     */
    public interface RealMap {

        /**
         * Applies the mapping.
         * 
         * @param value
         *            the input value.
         * @param logical
         *            the current array logical index.
         * @return the output value.
         */
        public double apply(double value, int[] logical);
    }

    /**
     * Defines an elementwise "reduce" operation over real-valued multidimensional arrays.
     */
    public interface RealReduce {

        /**
         * Applies the reduction.
         * 
         * @param value
         *            the input value.
         * @param logical
         *            the current array logical index.
         */
        public void apply(double value, int[] logical);

        /**
         * Gets the reduction result.
         * 
         * @return the reduction result.
         */
        public double get();
    }
}

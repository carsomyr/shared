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
import static shared.array.ArrayBase.IOKernel;
import static shared.array.ArrayBase.OpKernel;
import shared.util.Arithmetic;
import shared.util.Control;

/**
 * A multidimensional complex array class.
 * 
 * @author Roy Liu
 */
public class ComplexArray extends AbstractComplexArray<ComplexArray, RealArray> implements
        Matrix<ComplexArray, AbstractComplexArray.Complex> {

    /**
     * Default constructor.
     */
    public ComplexArray(int... dims) {
        this(0, dims.clone());
    }

    /**
     * Internal constructor with a distinctive signature.
     */
    protected ComplexArray(int unused, int[] dims) {
        super(new double[Arithmetic.product(dims)], INVALID_PARITY, dims, DEFAULT_ORDER.strides(dims));

        Control.checkTrue(dims.length >= 2 && dims[dims.length - 1] == 2);
    }

    /**
     * Alternate constructor.
     */
    public ComplexArray(double[] values, int... dims) {
        this(0, values, ArrayBase.inferDimensions(dims, values.length, true));
    }

    /**
     * Alternate constructor.
     */
    public ComplexArray(ComplexArray array) {
        this(0, array.values.clone(), array.dims);
    }

    /**
     * Internal constructor with a distinctive signature.
     */
    protected ComplexArray(int unused, double[] values, int[] dims) {
        super(values, INVALID_PARITY, dims, DEFAULT_ORDER.strides(dims));

        Control.checkTrue(dims.length >= 2 && dims[dims.length - 1] == 2 //
                && values.length == Arithmetic.product(dims));
    }

    /**
     * Internal constructor for package use only.
     */
    protected ComplexArray(int parity, int[] dims, int[] strides) {
        super(new double[Arithmetic.product(dims)], parity, dims, strides);
    }

    /**
     * Internal constructor for package use only.
     */
    protected ComplexArray(double[] values, int parity, int[] dims) {
        super(values, parity, dims, DEFAULT_ORDER.strides(dims));
    }

    @Override
    protected ComplexArray wrap(int parity, IndexingOrder order, int[] dims, int[] strides) {
        return new ComplexArray(parity, dims, strides);
    }

    @Override
    protected RealArray wrapDown(int parity, IndexingOrder order, int[] dims, int[] strides) {
        return new RealArray(order, dims, strides);
    }

    @Override
    public ComplexArray mMul(ComplexArray b) {

        ComplexArray a = this;

        Control.checkTrue(Control.checkEquals(a.dims.length, b.dims.length, //
                "Dimensionality mismatch") == 3, //
                "Arrays must have exactly three dimensions");

        // Matrices are already in matrix order.
        ComplexArray res = new ComplexArray(a.dims[0], b.dims[1], 2);

        OpKernel.mul(a.values, b.values, a.dims[0], b.dims[1], res.values, true);

        return res;
    }

    @Override
    public ComplexArray mDiag() {

        ComplexArray a = this;

        Control.checkTrue(a.dims.length == 3, //
                "Array must have exactly three dimensions");

        int n = Control.checkEquals(a.dims[0], a.dims[1], //
                "Dimensionality mismatch");

        // Matrices are already in matrix order.
        ComplexArray res = new ComplexArray(n, 1, 2);

        OpKernel.diag(a.values, res.values, n, true);

        return res;
    }

    @Override
    public ComplexArray mTranspose() {

        ComplexArray a = this;

        Control.checkTrue(a.dims.length == 3, //
                "Array must have exactly three dimensions");

        return transpose(1, 0, 2);
    }

    @Override
    public byte[] getBytes() {
        return IOKernel.getBytes(this);
    }

    /**
     * Gets the singleton value from this array.
     */
    public double[] singleton() {

        Control.checkTrue(this.values.length == 2, //
                "Array must contain exactly one value");

        return this.values.clone();
    }

    /**
     * Parses an array from {@code byte}s.
     */
    final public static ComplexArray parse(byte[] data) {
        return IOKernel.parse(data);
    }

    @Override
    public ComplexArray[] mSVD() {
        throw new UnsupportedOperationException(
                "Complex matrices currently do not support singular value decompositions");
    }

    @Override
    public ComplexArray[] mEigs() {
        throw new UnsupportedOperationException("Complex matrices currently do not support eigenvalue decompositions");
    }

    @Override
    public ComplexArray mInvert() {
        throw new UnsupportedOperationException("Complex matrices currently do not support inverses");
    }
}

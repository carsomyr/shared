/**
 * This file is part of the Shared Scientific Toolbox in Java ("this library"). <br />
 * <br />
 * Copyright (C) 2009 Roy Liu <br />
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

package shared.array.sparse;

import static shared.array.ArrayBase.DEFAULT_ORDER;
import static shared.array.ArrayBase.FIELD_PRECISION;
import static shared.array.ArrayBase.FIELD_WIDTH;
import static shared.array.ArrayBase.OpKernel;
import static shared.array.ArrayBase.formatRescale;
import static shared.array.ArrayBase.formatSparseArray;

import java.util.Formatter;

import shared.array.ArrayBase;
import shared.array.RealArray;
import shared.util.Arithmetic;
import shared.util.Arrays;
import shared.util.Control;

/**
 * A sparse real array class.
 * 
 * @author Roy Liu
 */
public class RealSparseArray extends ProtoSparseArray<RealSparseArray, double[], Double, RealArray> {

    /**
     * An empty array.
     */
    final protected static double[] Empty = new double[] {};

    /**
     * Default constructor.
     */
    public RealSparseArray(int... dims) {
        this(0, dims.clone());
    }

    /**
     * Internal constructor with a distinctive signature.
     */
    protected RealSparseArray(int unused, int[] dims) {
        super(new SparseArrayState<double[]>(Empty, dims), //
                dims, DEFAULT_ORDER.strides(dims), createDimensionOffsets(dims));

        Control.checkTrue(dims.length > 0);
    }

    /**
     * Alternate constructor.
     */
    public RealSparseArray(RealSparseArray array) {
        this(array.state, array.dims, array.strides, array.dimOffsets);
    }

    /**
     * Internal constructor for package use only.
     */
    protected RealSparseArray(SparseArrayState<double[]> state, int[] dims, int[] strides, int[] dimOffsets) {
        super(state, dims, strides, dimOffsets);
    }

    public Class<Double> getComponentType() {
        return Double.class;
    }

    @Override
    protected RealSparseArray wrap(Double value, int[] dims, int[] strides, int[] dimOffsets) {

        final SparseArrayState<double[]> state;

        if (value == null) {

            state = new SparseArrayState<double[]>(Empty, dims);

        } else {

            double[] values = Arrays.newArray(Arithmetic.product(dims), value.doubleValue());

            state = OpKernel.insertSparse( //
                    Empty, dims, strides, dimOffsets, EmptyIndices, //
                    values, Arithmetic.range(values.length));
        }

        return new RealSparseArray(state, dims, strides, dimOffsets);
    }

    @Override
    protected RealSparseArray wrap(SparseArrayState<double[]> state, int[] dims, int[] strides, int[] dimOffsets) {
        return new RealSparseArray(state, dims, strides, dimOffsets);
    }

    @Override
    protected int length(double[] values) {
        return values.length;
    }

    @Override
    protected double[] empty() {
        return Empty;
    }

    @Override
    public RealArray toDense() {

        RealSparseArray src = this;

        RealArray dst = new RealArray(src.order(), src.dims);

        SparseArrayState<double[]> srcState = src.state;

        double[] srcValues = srcState.values;
        int[] srcIndices = srcState.indices;

        double[] dstValues = dst.values();

        for (int i = 0, n = srcValues.length; i < n; i++) {
            dstValues[srcIndices[i]] = srcValues[i];
        }

        return dst;
    }

    @Override
    public String toString() {

        SparseArrayState<double[]> state = this.state;

        double[] values = state.values;
        int[] indices = state.indices;
        int[] dims = this.dims;
        int[] strides = this.strides;

        int exponent = (int) Math.log10(Arithmetic.max( //
                Arithmetic.max(values), Math.abs(Arithmetic.min(values)), 1e-128));

        int maxIndex = Arithmetic.max(dims);
        int exponentIndex = maxIndex > 0 ? (int) Math.log10(maxIndex) : 0;

        Formatter f = new Formatter();

        if (values.length == 0) {

            ArrayBase.formatEmptyArray(f, dims);

            return f.toString();
        }

        String valueFormat = String.format("%%%d.%df", FIELD_WIDTH, FIELD_PRECISION);
        String indexFormat = String.format("%%%dd", exponentIndex + 2);

        formatSparseArray(f, valueFormat, indexFormat, formatRescale(f, exponent, values), indices, strides);

        return f.toString();
    }
}

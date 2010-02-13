/**
 * This file is part of the Shared Scientific Toolbox in Java ("this library"). <br />
 * <br />
 * Copyright (C) 2007 Roy Liu <br />
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

package shared.array.jni;

import shared.array.kernel.ArrayKernel;
import shared.array.sparse.SparseArrayState;
import shared.metaclass.Library;
import shared.util.Control;

/**
 * A native implementation of {@link ArrayKernel}.
 * 
 * @author Roy Liu
 */
public class NativeArrayKernel implements ArrayKernel {

    /**
     * Default constructor. Checks the validity of native bindings.
     */
    public NativeArrayKernel() {
        Control.checkTrue(Library.isInitialized(), //
                "Could not instantiate native bindings -- Linking failed");
    }

    //

    final public native void randomize();

    final public native void derandomize();

    //

    final public native void map(int[] bounds, //
            Object srcV, int[] srcD, int[] srcS, //
            Object dstV, int[] dstD, int[] dstS);

    final public native void slice( //
            int[] slices, //
            Object srcV, int[] srcD, int[] srcS, //
            Object dstV, int[] dstD, int[] dstS);

    //

    final public native void rrOp(int type, //
            double[] srcV, int[] srcD, int[] srcS, //
            double[] dstV, int[] dstD, int[] dstS, //
            int... opDims);

    final public native void riOp(int type, //
            double[] srcV, int[] srcD, int[] srcS, int[] dstV, //
            int dim);

    final public native void rdOp(int type, //
            double[] srcV, int[] srcD, int[] srcS, double[] dstV, //
            int... opDims);

    //

    final public native double raOp(int type, double[] srcV);

    final public native double[] caOp(int type, double[] srcV);

    final public native void ruOp(int type, double a, double[] srcV);

    final public native void cuOp(int type, double aRe, double aIm, double[] srcV);

    final public native void iuOp(int type, int a, int[] srcV);

    final public native void eOp(int type, Object lhsV, Object rhsV, Object dstV, boolean isComplex);

    final public native void convert(int type, //
            Object srcV, boolean isSrcComplex, //
            Object dstV, boolean isDstComplex);

    //

    final public native void mul(double[] lhsV, double[] rhsV, int lr, int rc, double[] dstV, boolean isComplex);

    final public native void diag(double[] srcV, double[] dstV, int size, boolean isComplex);

    //

    final public native void svd(double[] srcV, int srcStrideRow, int srcStrideCol, //
            double[] uV, double[] sV, double[] vV, //
            int nrows, int ncols);

    final public native void eigs(double[] srcV, double[] vecV, double[] valV, int size);

    final public native void invert(double[] srcV, double[] dstV, int size);

    //

    final public native int[] find(int[] srcV, int[] srcD, int[] srcS, int[] logical);

    //

    final public native <V> SparseArrayState<V> insertSparse( //
            V oldV, int[] oldD, int[] oldS, int[] oldDO, int[] oldI, //
            V newV, int[] newLI);

    final public native <V> SparseArrayState<V> sliceSparse(int[] slices, //
            V srcV, int[] srcD, int[] srcS, int[] srcDO, //
            int[] srcI, int[] srcIO, int[] srcII, //
            V dstV, int[] dstD, int[] dstS, int[] dstDO, //
            int[] dstI, int[] dstIO, int[] dstII);
}

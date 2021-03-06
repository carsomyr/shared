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

package org.shared.array.jni;

import org.shared.array.kernel.ArrayKernel;
import org.shared.array.sparse.SparseArrayState;
import org.shared.metaclass.Library;
import org.shared.util.Control;

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

    @Override
    final public native void randomize();

    @Override
    final public native void derandomize();

    //

    @Override
    final public native void map(int[] bounds, //
            Object srcV, int[] srcD, int[] srcS, //
            Object dstV, int[] dstD, int[] dstS);

    @Override
    final public native void slice( //
            int[] slices, //
            Object srcV, int[] srcD, int[] srcS, //
            Object dstV, int[] dstD, int[] dstS);

    //

    @Override
    final public native void rrOp(int type, //
            double[] srcV, int[] srcD, int[] srcS, //
            double[] dstV, int[] dstD, int[] dstS, //
            int... opDims);

    @Override
    final public native void riOp(int type, //
            double[] srcV, int[] srcD, int[] srcS, int[] dstV, //
            int dim);

    @Override
    final public native void rdOp(int type, //
            double[] srcV, int[] srcD, int[] srcS, double[] dstV, //
            int... opDims);

    //

    @Override
    final public native double raOp(int type, double[] srcV);

    @Override
    final public native double[] caOp(int type, double[] srcV);

    @Override
    final public native void ruOp(int type, double a, double[] srcV);

    @Override
    final public native void cuOp(int type, double aRe, double aIm, double[] srcV);

    @Override
    final public native void iuOp(int type, int a, int[] srcV);

    @Override
    final public native void eOp(int type, Object lhsV, Object rhsV, Object dstV, boolean complex);

    @Override
    final public native void convert(int type, //
            Object srcV, boolean isSrcComplex, //
            Object dstV, boolean isDstComplex);

    //

    @Override
    final public native void mul(double[] lhsV, double[] rhsV, int lr, int rc, double[] dstV, boolean complex);

    @Override
    final public native void diag(double[] srcV, double[] dstV, int size, boolean complex);

    //

    @Override
    final public native void svd(double[] srcV, int srcStrideRow, int srcStrideCol, //
            double[] uV, double[] sV, double[] vV, //
            int nRows, int nCols);

    @Override
    final public native void eigs(double[] srcV, double[] vecV, double[] valV, int size);

    @Override
    final public native void invert(double[] srcV, double[] dstV, int size);

    //

    @Override
    final public native int[] find(int[] srcV, int[] srcD, int[] srcS, int[] logical);

    //

    @Override
    final public native <V> SparseArrayState<V> insertSparse( //
            V oldV, int[] oldD, int[] oldS, int[] oldDo, int[] oldI, //
            V newV, int[] newLi);

    @Override
    final public native <V> SparseArrayState<V> sliceSparse(int[] slices, //
            V srcV, int[] srcD, int[] srcS, int[] srcDo, //
            int[] srcI, int[] srcIo, int[] srcIi, //
            V dstV, int[] dstD, int[] dstS, int[] dstDo, //
            int[] dstI, int[] dstIo, int[] dstIi);
}

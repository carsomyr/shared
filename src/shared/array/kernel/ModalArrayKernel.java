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

package shared.array.kernel;

import shared.array.jni.NativeArrayKernel;
import shared.array.sparse.SparseArrayState;
import shared.util.Services;

/**
 * An implementation of {@link ArrayKernel} that has JNI and pure Java bindings.
 * 
 * @apiviz.owns shared.array.kernel.JavaArrayKernel
 * @author Roy Liu
 */
public class ModalArrayKernel implements ArrayKernel {

    volatile ArrayKernel opKernel;

    /**
     * Default constructor. Tries to create an underlying {@link NativeArrayKernel}. Failing that, creates an underlying
     * {@link JavaArrayKernel}.
     */
    public ModalArrayKernel() {

        this.opKernel = Services.createService(ArrayKernel.class);

        if (this.opKernel == null) {
            this.opKernel = new JavaArrayKernel();
        }
    }

    /**
     * Attempts to use the {@link ArrayKernel} obtained from {@link Services#createService(Class)}.
     * 
     * @return {@code true} if and only if an implementation could be obtained without resorting to the default kernel.
     */
    public boolean useNative() {

        this.opKernel = Services.createService(ArrayKernel.class);

        if (this.opKernel == null) {

            this.opKernel = new JavaArrayKernel();

            return false;

        } else {

            return true;
        }
    }

    /**
     * Uses the underlying {@link JavaArrayKernel}.
     */
    public void useJava() {
        this.opKernel = new JavaArrayKernel();
    }

    //

    @Override
    public void randomize() {
        this.opKernel.randomize();
    }

    @Override
    public void derandomize() {
        this.opKernel.derandomize();
    }

    //

    @Override
    public void map(int[] bounds, //
            Object srcV, int[] srcD, int[] srcS, //
            Object dstV, int[] dstD, int[] dstS) {
        this.opKernel.map(bounds, srcV, srcD, srcS, dstV, dstD, dstS);
    }

    @Override
    public void slice( //
            int[] slices, //
            Object srcV, int[] srcD, int[] srcS, //
            Object dstV, int[] dstD, int[] dstS) {
        this.opKernel.slice(slices, srcV, srcD, srcS, dstV, dstD, dstS);
    }

    //

    @Override
    public void rrOp(int type, //
            double[] srcV, int[] srcD, int[] srcS, //
            double[] dstV, int[] dstD, int[] dstS, //
            int... opDims) {
        this.opKernel.rrOp(type, srcV, srcD, srcS, dstV, dstD, dstS, opDims);
    }

    @Override
    public void riOp(int type, //
            double[] srcV, int[] srcD, int[] srcS, int[] dstV, //
            int dim) {
        this.opKernel.riOp(type, srcV, srcD, srcS, dstV, dim);
    }

    @Override
    public void rdOp(int type, //
            double[] srcV, int[] srcD, int[] srcS, double[] dstV, //
            int... opDims) {
        this.opKernel.rdOp(type, srcV, srcD, srcS, dstV, opDims);
    }

    @Override
    public double raOp(int type, double[] srcV) {
        return this.opKernel.raOp(type, srcV);
    }

    @Override
    public double[] caOp(int type, double[] srcV) {
        return this.opKernel.caOp(type, srcV);
    }

    @Override
    public void ruOp(int type, double a, double[] srcV) {
        this.opKernel.ruOp(type, a, srcV);
    }

    @Override
    public void cuOp(int type, double aRe, double aIm, double[] srcV) {
        this.opKernel.cuOp(type, aRe, aIm, srcV);
    }

    @Override
    public void iuOp(int type, int a, int[] srcV) {
        this.opKernel.iuOp(type, a, srcV);
    }

    @Override
    public void eOp(int type, Object lhsV, Object rhsV, Object dstV, boolean complex) {
        this.opKernel.eOp(type, lhsV, rhsV, dstV, complex);
    }

    @Override
    public void convert(int type, //
            Object srcV, boolean isSrcComplex, //
            Object dstV, boolean isDstComplex) {
        this.opKernel.convert(type, srcV, isSrcComplex, dstV, isDstComplex);
    }

    //

    @Override
    public void mul(double[] lhsV, double[] rhsV, int lr, int rc, double[] dstV, boolean complex) {
        this.opKernel.mul(lhsV, rhsV, lr, rc, dstV, complex);
    }

    @Override
    public void diag(double[] srcV, double[] dstV, int size, boolean complex) {
        this.opKernel.diag(srcV, dstV, size, complex);
    }

    //

    @Override
    public void svd(double[] srcV, int srcStrideRow, int srcStrideCol, //
            double[] uV, double[] sV, double[] vV, //
            int nRows, int nCols) {
        this.opKernel.svd(srcV, srcStrideRow, srcStrideCol, uV, sV, vV, nRows, nCols);
    }

    @Override
    public void eigs(double[] srcV, double[] vecV, double[] valV, int size) {
        this.opKernel.eigs(srcV, vecV, valV, size);
    }

    @Override
    public void invert(double[] srcV, double[] dstV, int size) {
        this.opKernel.invert(srcV, dstV, size);
    }

    //

    @Override
    public int[] find(int[] srcV, int[] srcD, int[] srcS, int[] logical) {
        return this.opKernel.find(srcV, srcD, srcS, logical);
    }

    //

    @Override
    public <V> SparseArrayState<V> insertSparse( //
            V oldV, int[] oldD, int[] oldS, int[] oldDO, int[] oldI, //
            V newV, int[] newI) {
        return this.opKernel.insertSparse(oldV, oldD, oldS, oldDO, oldI, newV, newI);
    }

    @Override
    public <V> SparseArrayState<V> sliceSparse(int[] slices, //
            V srcV, int[] srcD, int[] srcS, int[] srcDO, //
            int[] srcI, int[] srcIO, int[] srcII, //
            V dstV, int[] dstD, int[] dstS, int[] dstDO, //
            int[] dstI, int[] dstIO, int[] dstII) {
        return this.opKernel.sliceSparse(slices, //
                srcV, srcD, srcS, srcDO, //
                srcI, srcIO, srcII, //
                dstV, dstD, dstS, dstDO, //
                dstI, dstIO, dstII);
    }
}

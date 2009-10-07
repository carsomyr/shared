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
     * Uses the underlying {@link NativeArrayKernel} obtained from {@link Services#createService(Class)}.
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

    public void randomize() {
        this.opKernel.randomize();
    }

    public void derandomize() {
        this.opKernel.derandomize();
    }

    //

    public void map(int[] bounds, //
            Object srcV, int[] srcD, int[] srcS, //
            Object dstV, int[] dstD, int[] dstS) {
        this.opKernel.map(bounds, srcV, srcD, srcS, dstV, dstD, dstS);
    }

    public void slice( //
            int[] slices, //
            Object srcV, int[] srcD, int[] srcS, //
            Object dstV, int[] dstD, int[] dstS) {
        this.opKernel.slice(slices, srcV, srcD, srcS, dstV, dstD, dstS);
    }

    //

    public void rrOp(int type, //
            double[] srcV, int[] srcD, int[] srcS, //
            double[] dstV, int[] dstD, int[] dstS, //
            int... selectedDims) {
        this.opKernel.rrOp(type, srcV, srcD, srcS, dstV, dstD, dstS, selectedDims);
    }

    public void riOp(int type, //
            double[] srcV, int[] srcD, int[] srcS, int[] dstV, //
            int dim) {
        this.opKernel.riOp(type, srcV, srcD, srcS, dstV, dim);
    }

    public void rdOp(int type, //
            double[] srcV, int[] srcD, int[] srcS, double[] dstV, //
            int... selectedDims) {
        this.opKernel.rdOp(type, srcV, srcD, srcS, dstV, selectedDims);
    }

    public double raOp(int type, double[] srcV) {
        return this.opKernel.raOp(type, srcV);
    }

    public double[] caOp(int type, double[] srcV) {
        return this.opKernel.caOp(type, srcV);
    }

    public void ruOp(int type, double a, double[] srcV) {
        this.opKernel.ruOp(type, a, srcV);
    }

    public void cuOp(int type, double aRe, double aIm, double[] srcV) {
        this.opKernel.cuOp(type, aRe, aIm, srcV);
    }

    public void iuOp(int type, int a, int[] srcV) {
        this.opKernel.iuOp(type, a, srcV);
    }

    public void eOp(int type, Object lhsV, Object rhsV, Object dstV, boolean isComplex) {
        this.opKernel.eOp(type, lhsV, rhsV, dstV, isComplex);
    }

    public void convert(int type, //
            Object srcV, boolean isSrcComplex, //
            Object dstV, boolean isDstComplex) {
        this.opKernel.convert(type, srcV, isSrcComplex, dstV, isDstComplex);
    }

    //

    public void mul(double[] lhsV, double[] rhsV, int lr, int rc, double[] dstV, boolean isComplex) {
        this.opKernel.mul(lhsV, rhsV, lr, rc, dstV, isComplex);
    }

    public void diag(double[] srcV, double[] dstV, int size, boolean isComplex) {
        this.opKernel.diag(srcV, dstV, size, isComplex);
    }

    //

    public void svd(double[] srcV, int srcStrideRow, int srcStrideCol, //
            double[] uV, double[] sV, double[] vV, //
            int nrows, int ncols) {
        this.opKernel.svd(srcV, srcStrideRow, srcStrideCol, uV, sV, vV, nrows, ncols);
    }

    public void eigs(double[] srcV, double[] vecV, double[] valV, int size) {
        this.opKernel.eigs(srcV, vecV, valV, size);
    }

    public void invert(double[] srcV, double[] dstV, int size) {
        this.opKernel.invert(srcV, dstV, size);
    }

    //

    public int[] find(int[] srcV, int[] srcD, int[] srcS, int[] logical) {
        return this.opKernel.find(srcV, srcD, srcS, logical);
    }

    //

    public <V> SparseArrayState<V> insertSparse( //
            V oldV, int[] oldD, int[] oldS, int[] oldDO, int[] oldI, //
            V newV, int[] newI) {
        return this.opKernel.insertSparse(oldV, oldD, oldS, oldDO, oldI, newV, newI);
    }

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

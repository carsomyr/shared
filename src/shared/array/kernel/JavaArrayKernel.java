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

import shared.array.sparse.SparseArrayState;
import shared.util.Arithmetic;

/**
 * A pure Java implementation of {@link ArrayKernel}.
 * 
 * @apiviz.uses shared.array.kernel.DimensionOps
 * @apiviz.uses shared.array.kernel.ElementOps
 * @apiviz.uses shared.array.kernel.IndexOps
 * @apiviz.uses shared.array.kernel.LinearAlgebraOps
 * @apiviz.uses shared.array.kernel.MappingOps
 * @apiviz.uses shared.array.kernel.MatrixOps
 * @apiviz.uses shared.array.kernel.SparseOps
 * @author Roy Liu
 */
public class JavaArrayKernel implements ArrayKernel {

    /**
     * Default constructor.
     */
    public JavaArrayKernel() {
    }

    //

    @Override
    public void randomize() {
        Arithmetic.randomize();
    }

    @Override
    public void derandomize() {
        Arithmetic.derandomize();
    }

    //

    @Override
    public void map(int[] bounds, //
            Object srcV, int[] srcD, int[] srcS, //
            Object dstV, int[] dstD, int[] dstS) {
        MappingOps.map(bounds, srcV, srcD, srcS, dstV, dstD, dstS);
    }

    @Override
    public void slice( //
            int[] slices, //
            Object srcV, int[] srcD, int[] srcS, //
            Object dstV, int[] dstD, int[] dstS) {
        MappingOps.slice(slices, srcV, srcD, srcS, dstV, dstD, dstS);
    }

    //

    @Override
    public void rrOp(int type, //
            double[] srcV, int[] srcD, int[] srcS, //
            double[] dstV, int[] dstD, int[] dstS, //
            int... opDims) {
        DimensionOps.rrOp(type, srcV, srcD, srcS, dstV, dstD, dstS, opDims);
    }

    @Override
    public void riOp(int type, //
            double[] srcV, int[] srcD, int[] srcS, int[] dstV, //
            int dim) {
        DimensionOps.riOp(type, srcV, srcD, srcS, dstV, dim);
    }

    @Override
    public void rdOp(int type, //
            double[] srcV, int[] srcD, int[] srcS, double[] dstV, //
            int... opDims) {
        DimensionOps.rdOp(type, srcV, srcD, srcS, dstV, opDims);
    }

    //

    @Override
    public double raOp(int type, double[] srcV) {
        return ElementOps.raOp(type, srcV);
    }

    @Override
    public double[] caOp(int type, double[] srcV) {
        return ElementOps.caOp(type, srcV);
    }

    @Override
    public void ruOp(int type, double a, double[] srcV) {
        ElementOps.ruOp(type, a, srcV);
    }

    @Override
    public void cuOp(int type, double aRe, double aIm, double[] srcV) {
        ElementOps.cuOp(type, aRe, aIm, srcV);
    }

    @Override
    public void iuOp(int type, int a, int[] srcV) {
        ElementOps.iuOp(type, a, srcV);
    }

    @Override
    public void eOp(int type, Object lhsV, Object rhsV, Object dstV, boolean complex) {
        ElementOps.eOp(type, lhsV, rhsV, dstV, complex);
    }

    @Override
    public void convert(int type, Object srcV, boolean isSrcComplex, Object dstV, boolean isDstComplex) {
        ElementOps.convert(type, srcV, isSrcComplex, dstV, isDstComplex);
    }

    //

    @Override
    public void mul(double[] lhsV, double[] rhsV, int lr, int rc, double[] dstV, boolean complex) {
        MatrixOps.mul(lhsV, rhsV, lr, rc, dstV, complex);
    }

    @Override
    public void diag(double[] srcV, double[] dstV, int size, boolean complex) {
        MatrixOps.diag(srcV, dstV, size, complex);
    }

    //

    @Override
    public void svd(double[] srcV, int srcStrideRow, int srcStrideCol, //
            double[] uV, double[] sV, double[] vV, //
            int nRows, int nCols) {
        LinearAlgebraOps.svd(srcV, srcStrideRow, srcStrideCol, uV, sV, vV, nRows, nCols);
    }

    @Override
    public void eigs(double[] srcV, double[] vecV, double[] valV, int size) {
        LinearAlgebraOps.eigs(srcV, vecV, valV, size);
    }

    @Override
    public void invert(double[] srcV, double[] dstV, int size) {
        LinearAlgebraOps.invert(srcV, dstV, size);
    }

    //

    @Override
    public int[] find(int[] srcV, int[] srcD, int[] srcS, int[] logical) {
        return IndexOps.find(srcV, srcD, srcS, logical);
    }

    //

    @Override
    public <V> SparseArrayState<V> insertSparse( //
            V oldV, int[] oldD, int[] oldS, int[] oldDO, int[] oldI, //
            V newV, int[] newI) {
        return SparseOps.insert(oldV, oldD, oldS, oldDO, oldI, newV, newI);
    }

    @Override
    public <V> SparseArrayState<V> sliceSparse(int[] slices, //
            V srcV, int[] srcD, int[] srcS, int[] srcDO, //
            int[] srcI, int[] srcIO, int[] srcII, //
            V dstV, int[] dstD, int[] dstS, int[] dstDO, //
            int[] dstI, int[] dstIO, int[] dstII) {
        return SparseOps.slice(slices, //
                srcV, srcD, srcS, srcDO, //
                srcI, srcIO, srcII, //
                dstV, dstD, dstS, dstDO, //
                dstI, dstIO, dstII);
    }
}

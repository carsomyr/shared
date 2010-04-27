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

package shared.array.kernel;

import shared.array.Array;
import shared.array.Matrix;
import shared.array.Array.IndexingOrder;
import shared.array.sparse.SparseArrayState;
import shared.util.Service;

/**
 * A provider of operations on {@link Array}s.
 * 
 * @author Roy Liu
 */
public interface ArrayKernel extends Service {

    /** Real reduce sum. */
    final public static int RR_SUM = 0;

    /** Real reduce product. */
    final public static int RR_PROD = 1;

    /** Real reduce maximum. */
    final public static int RR_MAX = 2;

    /** Real reduce minimum. */
    final public static int RR_MIN = 3;

    /** Real reduce variance. */
    final public static int RR_VAR = 4;

    //

    /** Real index maximum. */
    final public static int RI_MAX = 0;

    /** Real index minimum. */
    final public static int RI_MIN = 1;

    /** Real index find zeros. */
    final public static int RI_ZERO = 2;

    /** Real index find greater-than-zeros. */
    final public static int RI_GZERO = 3;

    /** Real index find less-than-zeros. */
    final public static int RI_LZERO = 4;

    /** Real index sort. */
    final public static int RI_SORT = 5;

    //

    /** Real dimension sum. */
    final public static int RD_SUM = 0;

    /** Real dimension product. */
    final public static int RD_PROD = 1;

    //

    /** Real accumulator sum. */
    final public static int RA_SUM = 0;

    /** Real accumulator product. */
    final public static int RA_PROD = 1;

    /** Real accumulator variance. */
    final public static int RA_VAR = 2;

    /** Real accumulator maximum. */
    final public static int RA_MAX = 3;

    /** Real accumulator minimum. */
    final public static int RA_MIN = 4;

    /** Real accumulator entropy. */
    final public static int RA_ENT = 5;

    //

    /** Complex accumulator sum. */
    final public static int CA_SUM = 0;

    /** Complex accumulator product. */
    final public static int CA_PROD = 1;

    //

    /** Real unary addition. */
    final public static int RU_ADD = 0;

    /** Real unary multiplication. */
    final public static int RU_MUL = 1;

    /** Real unary absolute value. */
    final public static int RU_ABS = 2;

    /** Real unary power. */
    final public static int RU_POW = 3;

    /** Real unary exponentiation. */
    final public static int RU_EXP = 4;

    /** Real unary randomization. */
    final public static int RU_RND = 5;

    /** Real unary natural logarithm. */
    final public static int RU_LOG = 6;

    /** Real unary square root. */
    final public static int RU_SQRT = 7;

    /** Real unary square. */
    final public static int RU_SQR = 8;

    /** Real unary inverse. */
    final public static int RU_INV = 9;

    /** Real unary cosine. */
    final public static int RU_COS = 10;

    /** Real unary sine. */
    final public static int RU_SIN = 11;

    /** Real unary arctangent. */
    final public static int RU_ATAN = 12;

    /** Real unary fill. */
    final public static int RU_FILL = 13;

    /** Real unary shuffle. */
    final public static int RU_SHUFFLE = 14;

    //

    /** Complex unary addition. */
    final public static int CU_ADD = 0;

    /** Complex unary multiplication. */
    final public static int CU_MUL = 1;

    /** Complex unary exponentiation. */
    final public static int CU_EXP = 2;

    /** Complex unary randomization. */
    final public static int CU_RND = 3;

    /** Complex unary conjugation. */
    final public static int CU_CONJ = 4;

    /** Complex unary cosine. */
    final public static int CU_COS = 5;

    /** Complex unary sine. */
    final public static int CU_SIN = 6;

    /** Complex unary fill. */
    final public static int CU_FILL = 7;

    /** Complex unary shuffle. */
    final public static int CU_SHUFFLE = 8;

    //

    /** Integer unary addition. */
    final public static int IU_ADD = 0;

    /** Integer unary multiplication. */
    final public static int IU_MUL = 1;

    /** Integer unary fill. */
    final public static int IU_FILL = 2;

    /** Integer unary shuffle. */
    final public static int IU_SHUFFLE = 3;

    //

    /** Real elementwise addition. */
    final public static int RE_ADD = 0;

    /** Real elementwise subtraction. */
    final public static int RE_SUB = 1;

    /** Real elementwise multiplication. */
    final public static int RE_MUL = 2;

    /** Real elementwise division. */
    final public static int RE_DIV = 3;

    /** Real elementwise maximum. */
    final public static int RE_MAX = 4;

    /** Real elementwise minimum. */
    final public static int RE_MIN = 5;

    //

    /** Integer elementwise addition. */
    final public static int IE_ADD = 0;

    /** Integer elementwise subtraction. */
    final public static int IE_SUB = 1;

    /** Integer elementwise multiplication. */
    final public static int IE_MUL = 2;

    /** Integer elementwise maximum. */
    final public static int IE_MAX = 3;

    /** Integer elementwise minimum. */
    final public static int IE_MIN = 4;

    //

    /** Complex elementwise addition. */
    final public static int CE_ADD = 0;

    /** Complex elementwise subtraction. */
    final public static int CE_SUB = 1;

    /** Complex elementwise multiplication. */
    final public static int CE_MUL = 2;

    /** Complex elementwise division. */
    final public static int CE_DIV = 3;

    //

    /** Complex to real conversion by complex magnitudes. */
    final public static int CTOR_ABS = 0;

    /** Complex to real conversion by real part. */
    final public static int CTOR_RE = 1;

    /** Complex to real conversion by imaginary part. */
    final public static int CTOR_IM = 2;

    //

    /** Real to complex conversion by real part. */
    final public static int RTOC_RE = 0;

    /** Real to complex conversion by imaginary part. */
    final public static int RTOC_IM = 1;

    //

    /** Integer to real conversion by up-casting. */
    final public static int ITOR = 0;

    //

    /**
     * Seeds the underlying source of randomness with the current time.
     */
    public void randomize();

    /**
     * Seeds the underlying source of randomness with a constant.
     */
    public void derandomize();

    /**
     * Performs a mapping operation.
     * 
     * @param bounds
     *            the mapping bounds.
     * @param srcV
     *            the source values.
     * @param srcD
     *            the source dimensions.
     * @param srcS
     *            the source strides.
     * @param dstV
     *            the destination values.
     * @param dstD
     *            the destination dimensions.
     * @param dstS
     *            the destination strides.
     */
    public void map( //
            int[] bounds, //
            Object srcV, int[] srcD, int[] srcS, //
            Object dstV, int[] dstD, int[] dstS);

    /**
     * Performs a slicing operation.
     * 
     * @param slices
     *            the slicing specification.
     * @param srcV
     *            the source values.
     * @param srcD
     *            the source dimensions.
     * @param srcS
     *            the source strides.
     * @param dstV
     *            the destination values.
     * @param dstD
     *            the destination dimensions.
     * @param dstS
     *            the destination strides.
     */
    public void slice( //
            int[] slices, //
            Object srcV, int[] srcD, int[] srcS, //
            Object dstV, int[] dstD, int[] dstS);

    //

    /**
     * Performs a real reduce operation.
     * 
     * @param type
     *            the operation type.
     * @param srcV
     *            the source values.
     * @param srcD
     *            the source dimensions.
     * @param srcS
     *            the source strides.
     * @param dstV
     *            the destination values.
     * @param dstD
     *            the destination dimensions.
     * @param dstS
     *            the destination strides.
     * @param opDims
     *            the dimensions of interest.
     */
    public void rrOp(int type, //
            double[] srcV, int[] srcD, int[] srcS, //
            double[] dstV, int[] dstD, int[] dstS, //
            int... opDims);

    /**
     * Performs a real index operation.
     * 
     * @param type
     *            the operation type.
     * @param srcV
     *            the source values.
     * @param srcD
     *            the source dimensions.
     * @param srcS
     *            the source strides.
     * @param dstV
     *            the destination values.
     * @param dim
     *            the dimension of interest.
     */
    public void riOp(int type, //
            double[] srcV, int[] srcD, int[] srcS, //
            int[] dstV, //
            int dim);

    /**
     * Performs a real dimension operation.
     * 
     * @param type
     *            the operation type.
     * @param srcV
     *            the source values.
     * @param srcD
     *            the source dimensions.
     * @param srcS
     *            the source strides.
     * @param dstV
     *            the destination values.
     * @param opDims
     *            the dimensions of interest.
     */
    public void rdOp(int type, //
            double[] srcV, int[] srcD, int[] srcS, double[] dstV, //
            int... opDims);

    //

    /**
     * Performs a real accumulator operation.
     * 
     * @param type
     *            the operation type.
     * @param srcV
     *            the array.
     * @return the accumulated result.
     */
    public double raOp(int type, double[] srcV);

    /**
     * Performs a complex accumulator operation.
     * 
     * @param type
     *            the operation type.
     * @param srcV
     *            the array.
     * @return the accumulated result.
     */
    public double[] caOp(int type, double[] srcV);

    /**
     * Applies a real unary operation.
     * 
     * @param type
     *            the operation type.
     * @param a
     *            the argument, if any.
     * @param srcV
     *            the array.
     */
    public void ruOp(int type, double a, double[] srcV);

    /**
     * Applies a complex unary operation.
     * 
     * @param type
     *            the operation type.
     * @param aRe
     *            the real part of the argument, if any.
     * @param aIm
     *            the imaginary part of the argument, if any.
     * @param srcV
     *            the array.
     */
    public void cuOp(int type, double aRe, double aIm, double[] srcV);

    /**
     * Applies an integer unary operation.
     * 
     * @param type
     *            the operation type.
     * @param a
     *            the argument, if any.
     * @param srcV
     *            the array.
     */
    public void iuOp(int type, int a, int[] srcV);

    /**
     * Applies a binary operation.
     * 
     * @param type
     *            the operation type.
     * @param lhsV
     *            the left hand side values.
     * @param rhsV
     *            the right hand side values.
     * @param dstV
     *            the destination values.
     * @param isComplex
     *            whether the operation is complex-valued.
     */
    public void eOp(int type, Object lhsV, Object rhsV, Object dstV, boolean isComplex);

    /**
     * Performs a conversion operation.
     * 
     * @param type
     *            the operation type.
     * @param srcV
     *            the source values.
     * @param isSrcComplex
     *            whether the source is complex-valued.
     * @param dstV
     *            the destination values.
     * @param isDstComplex
     *            whether the destination is complex-valued.
     */
    public void convert(int type, //
            Object srcV, boolean isSrcComplex, //
            Object dstV, boolean isDstComplex);

    //

    /**
     * Multiplies two {@link Matrix}s. They are assumed to have storage order {@link IndexingOrder#FAR}.
     * 
     * @param lhsV
     *            the left hand side values.
     * @param rhsV
     *            the right hand side values.
     * @param lr
     *            the row count of the result.
     * @param rc
     *            the column count of the result.
     * @param dstV
     *            the destination values.
     * @param isComplex
     *            whether the operation is complex-valued.
     */
    public void mul(double[] lhsV, double[] rhsV, int lr, int rc, double[] dstV, boolean isComplex);

    /**
     * Gets the diagonal of a {@link Matrix}.
     * 
     * @param srcV
     *            source values.
     * @param dstV
     *            destination values.
     * @param size
     *            the matrix size.
     * @param isComplex
     *            whether the operation is complex-valued.
     */
    public void diag(double[] srcV, double[] dstV, int size, boolean isComplex);

    //

    /**
     * Computes the singular value decomposition of a {@link Matrix}.
     * 
     * @param srcV
     *            the source values.
     * @param srcStrideRow
     *            the source row stride.
     * @param srcStrideCol
     *            the source column stride.
     * @param uV
     *            the input vectors.
     * @param sV
     *            the gain controls.
     * @param vV
     *            the output vectors.
     * @param nRows
     *            the number of rows.
     * @param nCols
     *            the number of columns.
     */
    public void svd(double[] srcV, int srcStrideRow, int srcStrideCol, //
            double[] uV, double[] sV, double[] vV, //
            int nRows, int nCols);

    /**
     * Computes the eigenvectors and eigenvalues of a {@link Matrix}.
     * 
     * @param srcV
     *            the source values.
     * @param vecV
     *            the eigenvectors.
     * @param valV
     *            the eigenvalues.
     * @param size
     *            the matrix size.
     */
    public void eigs(double[] srcV, double[] vecV, double[] valV, int size);

    /**
     * Computes the inverse of a {@link Matrix}.
     * 
     * @param srcV
     *            the source values.
     * @param dstV
     *            the destination values.
     * @param size
     *            the matrix size.
     */
    public void invert(double[] srcV, double[] dstV, int size);

    //

    /**
     * Extracts the valid indices along a dimension anchored at the given logical index.
     * 
     * @param srcV
     *            the source values.
     * @param srcD
     *            the source dimensions.
     * @param srcS
     *            the source strides.
     * @param logical
     *            the logical index.
     * @return the valid indices.
     */
    public int[] find(int[] srcV, int[] srcD, int[] srcS, int[] logical);

    //

    /**
     * Inserts elements into a sparse array.
     * 
     * @param oldV
     *            the old values.
     * @param oldD
     *            the old dimensions.
     * @param oldS
     *            the old strides.
     * @param oldDO
     *            the old dimension offsets.
     * @param oldI
     *            the old physical indices. Invariant: Sorted in ascending order, and does not contain duplicates.
     * @param newV
     *            the new values.
     * @param newI
     *            the new physical indices, which need not be sorted in ascending order.
     * @param <V>
     *            the storage array type.
     * @return the {@link SparseArrayState}.
     */
    public <V> SparseArrayState<V> insertSparse( //
            V oldV, int[] oldD, int[] oldS, int[] oldDO, int[] oldI, //
            V newV, int[] newI);

    /**
     * Slices one sparse array into another.
     * 
     * @param slices
     *            the slicing specification.
     * @param srcV
     *            the source values.
     * @param srcD
     *            the source dimensions.
     * @param srcS
     *            the source strides.
     * @param srcDO
     *            the source dimension offsets.
     * @param srcI
     *            the source physical indices. Invariant: Sorted in ascending order, and does not contain duplicates.
     * @param srcIO
     *            the source indirection offsets.
     * @param srcII
     *            the source indirections.
     * @param dstV
     *            the destination values.
     * @param dstD
     *            the destination dimensions.
     * @param dstS
     *            the destination strides.
     * @param dstDO
     *            the destination dimension offsets.
     * @param dstI
     *            the destination physical indices. Invariant: Sorted in ascending order, and does not contain
     *            duplicates.
     * @param dstIO
     *            the destination indirection offsets.
     * @param dstII
     *            the destination indirections.
     * @param <V>
     *            the storage array type.
     * @return the {@link SparseArrayState}.
     */
    public <V> SparseArrayState<V> sliceSparse(int[] slices, //
            V srcV, int[] srcD, int[] srcS, int[] srcDO, //
            int[] srcI, int[] srcIO, int[] srcII, //
            V dstV, int[] dstD, int[] dstS, int[] dstDO, //
            int[] dstI, int[] dstIO, int[] dstII);
}

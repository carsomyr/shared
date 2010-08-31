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

import static shared.array.kernel.ArrayKernel.RD_PROD;
import static shared.array.kernel.ArrayKernel.RD_SUM;
import static shared.array.kernel.ArrayKernel.RI_GZERO;
import static shared.array.kernel.ArrayKernel.RI_LZERO;
import static shared.array.kernel.ArrayKernel.RI_MAX;
import static shared.array.kernel.ArrayKernel.RI_MIN;
import static shared.array.kernel.ArrayKernel.RI_SORT;
import static shared.array.kernel.ArrayKernel.RI_ZERO;
import static shared.array.kernel.ArrayKernel.RR_MAX;
import static shared.array.kernel.ArrayKernel.RR_MIN;
import static shared.array.kernel.ArrayKernel.RR_PROD;
import static shared.array.kernel.ArrayKernel.RR_SUM;
import static shared.array.kernel.ArrayKernel.RR_VAR;

import java.util.Arrays;

import shared.util.Arithmetic;
import shared.util.Control;

/**
 * A class for dimension operations in pure Java.
 * 
 * @apiviz.has shared.array.kernel.DimensionOps.RealDimensionOperation - - - argument
 * @apiviz.has shared.array.kernel.DimensionOps.RealIndexOperation - - - argument
 * @apiviz.has shared.array.kernel.DimensionOps.RealReduceOperation - - - argument
 * @apiviz.uses shared.array.kernel.PermutationEntry
 * @author Roy Liu
 */
public class DimensionOps {

    /**
     * Defines real reduce operations.
     */
    protected interface RealReduceOperation {

        /**
         * Performs a real reduce operation.
         */
        public void op(double[] working, int[] workingIndices, int size, int stride);
    }

    final static RealReduceOperation RRSumOp = new RealReduceOperation() {

        @Override
        public void op(double[] working, int[] workingIndices, int size, int stride) {

            for (int workingIndex : workingIndices) {

                for (int j = 1, offset = workingIndex + stride; j < size; j++, offset += stride) {
                    working[workingIndex] += working[offset];
                }
            }
        }
    };

    final static RealReduceOperation RRProdOp = new RealReduceOperation() {

        @Override
        public void op(double[] working, int[] workingIndices, int size, int stride) {

            for (int workingIndex : workingIndices) {

                for (int j = 1, offset = workingIndex + stride; j < size; j++, offset += stride) {
                    working[workingIndex] *= working[offset];
                }
            }
        }
    };

    final static RealReduceOperation RRMaxOp = new RealReduceOperation() {

        @Override
        public void op(double[] working, int[] workingIndices, int size, int stride) {

            for (int workingIndex : workingIndices) {

                for (int j = 1, offset = workingIndex + stride; j < size; j++, offset += stride) {
                    working[workingIndex] = Math.max(working[offset], working[workingIndex]);
                }
            }
        }
    };

    final static RealReduceOperation RRMinOp = new RealReduceOperation() {

        @Override
        public void op(double[] working, int[] workingIndices, int size, int stride) {

            for (int workingIndex : workingIndices) {

                for (int j = 1, offset = workingIndex + stride; j < size; j++, offset += stride) {
                    working[workingIndex] = Math.min(working[offset], working[workingIndex]);
                }
            }
        }
    };

    final static RealReduceOperation RRVarOp = new RealReduceOperation() {

        @Override
        public void op(double[] working, int[] workingIndices, int size, int stride) {

            for (int workingIndex : workingIndices) {

                double mean = 0.0;

                for (int j = 0, offset = workingIndex; j < size; j++, offset += stride) {
                    mean += working[offset];
                }

                mean /= size;

                for (int j = 0, offset = workingIndex; j < size; j++, offset += stride) {

                    double diff = working[offset] - mean;
                    working[offset] = diff * diff;
                }

                for (int j = 1, offset = workingIndex + stride; j < size; j++, offset += stride) {
                    working[workingIndex] += working[offset];
                }

                working[workingIndex] /= size;
            }
        }
    };

    /**
     * Defines real index operations.
     */
    protected interface RealIndexOperation {

        /**
         * Performs a real index operation.
         */
        public void op(double[] srcV, int[] srcIndices, int[] dstV, int size, int stride);
    }

    final static RealIndexOperation RIMaxOp = new RealIndexOperation() {

        @Override
        public void op(double[] srcV, int[] srcIndices, int[] dstV, int size, int stride) {

            if (srcIndices != null) {

                int maxStride = stride * size;

                for (int i = 0, nIndices = srcIndices.length; i < nIndices; i++) {

                    double acc = -Double.MAX_VALUE;

                    for (int offset = 0; offset < maxStride; offset += stride) {
                        acc = Math.max(acc, srcV[srcIndices[i] + offset]);
                    }

                    int count = 0;

                    for (int offset = 0; offset < maxStride; offset += stride) {

                        if (srcV[srcIndices[i] + offset] == acc) {

                            dstV[srcIndices[i] + count] = offset / stride;
                            count += stride;
                        }
                    }

                    for (int offset = count; offset < maxStride; offset += stride) {
                        dstV[srcIndices[i] + offset] = -1;
                    }
                }

            } else {

                double maxValue = Arithmetic.max(srcV);

                for (int i = 0, n = srcV.length; i < n; i++) {
                    dstV[i] = (srcV[i] == maxValue) ? 1 : 0;
                }
            }
        }
    };

    final static RealIndexOperation RIMinOp = new RealIndexOperation() {

        @Override
        public void op(double[] srcV, int[] srcIndices, int[] dstV, int size, int stride) {

            if (srcIndices != null) {

                int maxStride = stride * size;

                for (int i = 0, nIndices = srcIndices.length; i < nIndices; i++) {

                    double acc = Double.MAX_VALUE;

                    for (int offset = 0; offset < maxStride; offset += stride) {
                        acc = Math.min(acc, srcV[srcIndices[i] + offset]);
                    }

                    int count = 0;

                    for (int offset = 0; offset < maxStride; offset += stride) {

                        if (srcV[srcIndices[i] + offset] == acc) {

                            dstV[srcIndices[i] + count] = offset / stride;
                            count += stride;
                        }
                    }

                    for (int offset = count; offset < maxStride; offset += stride) {
                        dstV[srcIndices[i] + offset] = -1;
                    }
                }

            } else {

                double minValue = Arithmetic.min(srcV);

                for (int i = 0, n = srcV.length; i < n; i++) {
                    dstV[i] = (srcV[i] == minValue) ? 1 : 0;
                }
            }
        }
    };

    final static RealIndexOperation RIZeroOp = new RealIndexOperation() {

        @Override
        public void op(double[] srcV, int[] srcIndices, int[] dstV, int size, int stride) {

            if (srcIndices != null) {

                int maxStride = stride * size;

                for (int i = 0, nIndices = srcIndices.length; i < nIndices; i++) {

                    int count = 0;

                    for (int offset = 0; offset < maxStride; offset += stride) {

                        if (srcV[srcIndices[i] + offset] == 0.0) {

                            dstV[srcIndices[i] + count] = offset / stride;
                            count += stride;
                        }
                    }

                    for (int offset = count; offset < maxStride; offset += stride) {
                        dstV[srcIndices[i] + offset] = -1;
                    }
                }

            } else {

                for (int i = 0, n = srcV.length; i < n; i++) {
                    dstV[i] = (srcV[i] == 0.0) ? 1 : 0;
                }
            }
        }
    };

    final static RealIndexOperation RIGZeroOp = new RealIndexOperation() {

        @Override
        public void op(double[] srcV, int[] srcIndices, int[] dstV, int size, int stride) {

            if (srcIndices != null) {

                int maxStride = stride * size;

                for (int i = 0, nIndices = srcIndices.length; i < nIndices; i++) {

                    int count = 0;

                    for (int offset = 0; offset < maxStride; offset += stride) {

                        if (srcV[srcIndices[i] + offset] > 0.0) {

                            dstV[srcIndices[i] + count] = offset / stride;
                            count += stride;
                        }
                    }

                    for (int offset = count; offset < maxStride; offset += stride) {
                        dstV[srcIndices[i] + offset] = -1;
                    }
                }

            } else {

                for (int i = 0, n = srcV.length; i < n; i++) {
                    dstV[i] = (srcV[i] > 0.0) ? 1 : 0;
                }
            }
        }
    };

    final static RealIndexOperation RILZeroOp = new RealIndexOperation() {

        @Override
        public void op(double[] srcV, int[] srcIndices, int[] dstV, int size, int stride) {

            if (srcIndices != null) {

                int maxStride = stride * size;

                for (int i = 0, nIndices = srcIndices.length; i < nIndices; i++) {

                    int count = 0;

                    for (int offset = 0; offset < maxStride; offset += stride) {

                        if (srcV[srcIndices[i] + offset] < 0.0) {

                            dstV[srcIndices[i] + count] = offset / stride;
                            count += stride;
                        }
                    }

                    for (int offset = count; offset < maxStride; offset += stride) {
                        dstV[srcIndices[i] + offset] = -1;
                    }
                }

            } else {

                for (int i = 0, n = srcV.length; i < n; i++) {
                    dstV[i] = (srcV[i] < 0.0) ? 1 : 0;
                }
            }
        }
    };

    final static RealIndexOperation RISortOp = new RealIndexOperation() {

        @Override
        public void op(double[] srcV, int[] srcIndices, int[] dstV, int size, int stride) {

            Double[] srcVBoxed = shared.util.Arrays.box(srcV);

            PermutationEntry.iSort(srcVBoxed, srcIndices, dstV, size, stride);

            for (int i = 0, n = srcV.length; i < n; i++) {
                srcV[i] = srcVBoxed[i];
            }
        }
    };

    /**
     * Defines real dimension operations.
     */
    protected interface RealDimensionOperation {

        /**
         * Performs a real dimension operation.
         */
        public void op(double[] srcV, int[] srcD, int[] srcS, double[] dstV, int[] opDims);
    }

    final static RealDimensionOperation RDSumOp = new RealDimensionOperation() {

        @Override
        public void op(double[] srcV, int[] srcD, int[] srcS, double[] dstV, int[] opDims) {

            int len = Control.checkEquals(srcV.length, dstV.length);
            int nDims = Control.checkEquals(srcD.length, srcS.length);

            boolean[] indicator = new boolean[nDims];

            for (int dim : opDims) {
                indicator[dim] = true;
            }

            int[] srcIndices = MappingOps.assignMappingIndices(len, srcD, srcS);

            //

            System.arraycopy(srcV, 0, dstV, 0, len);

            for (int dim = 0, indexBlockIncrement = len; dim < nDims; indexBlockIncrement /= srcD[dim++]) {

                if (!indicator[dim]) {
                    continue;
                }

                int size = srcD[dim];
                int stride = srcS[dim];

                for (int lower = 0, upper = indexBlockIncrement / size; //
                lower < len; //
                lower += indexBlockIncrement, upper += indexBlockIncrement) {

                    for (int indexIndex = lower; indexIndex < upper; indexIndex++) {

                        double acc = 0.0;

                        for (int k = 0, physical = srcIndices[indexIndex]; k < size; k++, physical += stride) {

                            acc += dstV[physical];
                            dstV[physical] = acc;
                        }
                    }
                }
            }
        }
    };

    final static RealDimensionOperation RDProdOp = new RealDimensionOperation() {

        @Override
        public void op(double[] srcV, int[] srcD, int[] srcS, double[] dstV, int[] opDims) {

            int len = Control.checkEquals(srcV.length, dstV.length);
            int nDims = Control.checkEquals(srcD.length, srcS.length);

            boolean[] indicator = new boolean[nDims];

            for (int dim : opDims) {
                indicator[dim] = true;
            }

            int[] srcIndices = MappingOps.assignMappingIndices(len, srcD, srcS);

            //

            System.arraycopy(srcV, 0, dstV, 0, len);

            for (int dim = 0, indexBlockIncrement = len; dim < nDims; indexBlockIncrement /= srcD[dim++]) {

                if (!indicator[dim]) {
                    continue;
                }

                int size = srcD[dim];
                int stride = srcS[dim];

                for (int lower = 0, upper = indexBlockIncrement / size; //
                lower < len; //
                lower += indexBlockIncrement, upper += indexBlockIncrement) {

                    for (int indexIndex = lower; indexIndex < upper; indexIndex++) {

                        double acc = 1.0;

                        for (int k = 0, physical = srcIndices[indexIndex]; k < size; k++, physical += stride) {

                            acc *= dstV[physical];
                            dstV[physical] = acc;
                        }
                    }
                }
            }
        }
    };

    /**
     * Assigns base indices when excluding a dimension.
     * 
     * @param nIndices
     *            the number of indices.
     * @param srcD
     *            the dimensions.
     * @param srcS
     *            the strides.
     * @param dim
     *            the dimension to exclude.
     * @return the base physical indices.
     */
    final public static int[] assignBaseIndices(int nIndices, int[] srcD, int[] srcS, int dim) {

        int nDims = Control.checkEquals(srcD.length, srcS.length, //
                "Invalid arguments");

        int[] dModified = new int[nDims - 1];
        int[] sModified = new int[nDims - 1];

        System.arraycopy(srcD, 0, dModified, 0, dim);
        System.arraycopy(srcD, dim + 1, dModified, dim, (nDims - 1) - dim);

        System.arraycopy(srcS, 0, sModified, 0, dim);
        System.arraycopy(srcS, dim + 1, sModified, dim, (nDims - 1) - dim);

        return MappingOps.assignMappingIndices(nIndices, dModified, sModified);
    }

    /**
     * Dimension reduce operations in support of
     * {@link JavaArrayKernel#rrOp(int, double[], int[], int[], double[], int[], int[], int...)}.
     */
    final public static void rrOp(int type, //
            double[] srcV, int[] srcD, int[] srcS, //
            double[] dstV, int[] dstD, int[] dstS, //
            int[] opDims) {

        int srcLen = MappingOps.checkDimensions(srcV.length, srcD, srcS);
        int dstLen = MappingOps.checkDimensions(dstV.length, dstD, dstS);

        final RealReduceOperation op;

        switch (type) {

        case RR_SUM:
            op = RRSumOp;
            break;

        case RR_PROD:
            op = RRProdOp;
            break;

        case RR_MAX:
            op = RRMaxOp;
            break;

        case RR_MIN:
            op = RRMinOp;
            break;

        case RR_VAR:
            op = RRVarOp;
            break;

        default:
            throw new IllegalArgumentException();
        }

        Arrays.sort(opDims);

        int nOpDims = opDims.length;
        int nDims = Control.checkEquals(srcD.length, dstD.length, //
                "Dimensionality mismatch");

        for (int i = 1; i < nOpDims; i++) {
            Control.checkTrue(opDims[i - 1] != opDims[i], //
                    "Duplicate operating dimensions are not allowed");
        }

        int acc = dstLen;

        for (int i = 0; i < nOpDims; i++) {

            int dim = opDims[i];

            Control.checkTrue(dim >= 0 && dim < nDims, //
                    "Invalid dimension");

            Control.checkTrue(dstD[dim] <= 1, //
                    "Operating dimensions must have singleton or zero length");

            acc *= srcD[dim];
        }

        Control.checkTrue(acc == srcLen, //
                "Invalid arguments");

        if (srcLen == 0) {
            return;
        }

        double[] workingV = srcV.clone();
        int[] workingD = srcD.clone();

        acc = srcLen;

        for (int i = 0; i < nOpDims; i++) {

            int dim = opDims[i];

            acc /= srcD[dim];

            op.op(workingV, assignBaseIndices(acc, workingD, srcS, dim), //
                    workingD[dim], srcS[dim]);

            workingD[dim] = 1;
        }

        MappingOps.assign( //
                workingV, MappingOps.assignMappingIndices(dstLen, dstD, srcS), //
                dstV, MappingOps.assignMappingIndices(dstLen, dstD, dstS));
    }

    /**
     * Dimension index operations in support of {@link ArrayKernel#riOp(int, double[], int[], int[], int[], int)}.
     */
    final public static void riOp(int type, //
            double[] srcV, int[] srcD, int[] srcS, int[] dstV, //
            int dim) {

        int srcLen = MappingOps.checkDimensions(srcV.length, srcD, srcS);

        final RealIndexOperation op;

        switch (type) {

        case RI_MAX:
            op = RIMaxOp;
            break;

        case RI_MIN:
            op = RIMinOp;
            break;

        case RI_ZERO:
            op = RIZeroOp;
            break;

        case RI_GZERO:
            op = RIGZeroOp;
            break;

        case RI_LZERO:
            op = RILZeroOp;
            break;

        case RI_SORT:
            op = RISortOp;
            break;

        default:
            throw new IllegalArgumentException();
        }

        Control.checkTrue(srcLen == dstV.length, //
                "Invalid arguments");

        if (srcLen == 0) {
            return;
        }

        if (dim != -1) {

            op.op(srcV, assignBaseIndices(srcLen / srcD[dim], srcD, srcS, dim), //
                    dstV, srcD[dim], srcS[dim]);

        } else {

            op.op(srcV, null, dstV, -1, -1);
        }
    }

    /**
     * Dimension operations in support of {@link ArrayKernel#rdOp(int, double[], int[], int[], double[], int...)}.
     */
    final public static void rdOp(int type, //
            double[] srcV, int[] srcD, int[] srcS, double[] dstV, //
            int[] opDims) {

        int srcLen = MappingOps.checkDimensions(srcV.length, srcD, srcS);

        final RealDimensionOperation op;

        switch (type) {

        case RD_SUM:
            op = RDSumOp;
            break;

        case RD_PROD:
            op = RDProdOp;
            break;

        default:
            throw new IllegalArgumentException();
        }

        int nDims = Control.checkEquals(srcD.length, srcS.length, //
                "Dimensionality mismatch");

        for (int dim : opDims) {
            Control.checkTrue(dim >= 0 && dim < nDims, //
                    "Invalid dimension");
        }

        if (srcLen == 0) {
            return;
        }

        op.op(srcV, srcD, srcS, dstV, opDims);
    }

    // Dummy constructor.
    DimensionOps() {
    }
}

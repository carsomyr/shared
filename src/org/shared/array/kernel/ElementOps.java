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

package org.shared.array.kernel;

import static org.shared.array.kernel.ArrayKernel.CA_PROD;
import static org.shared.array.kernel.ArrayKernel.CA_SUM;
import static org.shared.array.kernel.ArrayKernel.CE_ADD;
import static org.shared.array.kernel.ArrayKernel.CE_DIV;
import static org.shared.array.kernel.ArrayKernel.CE_MUL;
import static org.shared.array.kernel.ArrayKernel.CE_SUB;
import static org.shared.array.kernel.ArrayKernel.CU_ADD;
import static org.shared.array.kernel.ArrayKernel.CU_CONJ;
import static org.shared.array.kernel.ArrayKernel.CU_COS;
import static org.shared.array.kernel.ArrayKernel.CU_EXP;
import static org.shared.array.kernel.ArrayKernel.CU_FILL;
import static org.shared.array.kernel.ArrayKernel.CU_MUL;
import static org.shared.array.kernel.ArrayKernel.CU_RND;
import static org.shared.array.kernel.ArrayKernel.CU_SHUFFLE;
import static org.shared.array.kernel.ArrayKernel.CU_SIN;
import static org.shared.array.kernel.ArrayKernel.C_TO_R_ABS;
import static org.shared.array.kernel.ArrayKernel.C_TO_R_IM;
import static org.shared.array.kernel.ArrayKernel.C_TO_R_RE;
import static org.shared.array.kernel.ArrayKernel.IE_ADD;
import static org.shared.array.kernel.ArrayKernel.IE_MAX;
import static org.shared.array.kernel.ArrayKernel.IE_MIN;
import static org.shared.array.kernel.ArrayKernel.IE_MUL;
import static org.shared.array.kernel.ArrayKernel.IE_SUB;
import static org.shared.array.kernel.ArrayKernel.IU_ADD;
import static org.shared.array.kernel.ArrayKernel.IU_FILL;
import static org.shared.array.kernel.ArrayKernel.IU_MUL;
import static org.shared.array.kernel.ArrayKernel.IU_SHUFFLE;
import static org.shared.array.kernel.ArrayKernel.RA_ENT;
import static org.shared.array.kernel.ArrayKernel.RA_MAX;
import static org.shared.array.kernel.ArrayKernel.RA_MIN;
import static org.shared.array.kernel.ArrayKernel.RA_PROD;
import static org.shared.array.kernel.ArrayKernel.RA_SUM;
import static org.shared.array.kernel.ArrayKernel.RA_VAR;
import static org.shared.array.kernel.ArrayKernel.RE_ADD;
import static org.shared.array.kernel.ArrayKernel.RE_DIV;
import static org.shared.array.kernel.ArrayKernel.RE_MAX;
import static org.shared.array.kernel.ArrayKernel.RE_MIN;
import static org.shared.array.kernel.ArrayKernel.RE_MUL;
import static org.shared.array.kernel.ArrayKernel.RE_SUB;
import static org.shared.array.kernel.ArrayKernel.RU_ABS;
import static org.shared.array.kernel.ArrayKernel.RU_ADD;
import static org.shared.array.kernel.ArrayKernel.RU_ATAN;
import static org.shared.array.kernel.ArrayKernel.RU_COS;
import static org.shared.array.kernel.ArrayKernel.RU_EXP;
import static org.shared.array.kernel.ArrayKernel.RU_FILL;
import static org.shared.array.kernel.ArrayKernel.RU_INV;
import static org.shared.array.kernel.ArrayKernel.RU_LOG;
import static org.shared.array.kernel.ArrayKernel.RU_MUL;
import static org.shared.array.kernel.ArrayKernel.RU_POW;
import static org.shared.array.kernel.ArrayKernel.RU_RND;
import static org.shared.array.kernel.ArrayKernel.RU_SHUFFLE;
import static org.shared.array.kernel.ArrayKernel.RU_SIN;
import static org.shared.array.kernel.ArrayKernel.RU_SQR;
import static org.shared.array.kernel.ArrayKernel.RU_SQRT;
import static org.shared.array.kernel.ArrayKernel.R_TO_C_IM;
import static org.shared.array.kernel.ArrayKernel.R_TO_C_RE;

import org.shared.util.Arithmetic;
import org.shared.util.Control;

/**
 * A class for elementwise operations in pure Java.
 * 
 * @apiviz.has org.shared.array.kernel.ElementOps.ComplexAccumulatorOperation - - - argument
 * @apiviz.has org.shared.array.kernel.ElementOps.ComplexBinaryOperation - - - argument
 * @apiviz.has org.shared.array.kernel.ElementOps.ComplexToRealOperation - - - argument
 * @apiviz.has org.shared.array.kernel.ElementOps.IntegerBinaryOperation - - - argument
 * @apiviz.has org.shared.array.kernel.ElementOps.RealAccumulatorOperation - - - argument
 * @apiviz.has org.shared.array.kernel.ElementOps.RealBinaryOperation - - - argument
 * @apiviz.has org.shared.array.kernel.ElementOps.RealToComplexOperation - - - argument
 * @author Roy Liu
 */
public class ElementOps {

    /**
     * Defines real binary operations.
     */
    protected interface RealBinaryOperation {

        /**
         * Performs a real binary operation.
         */
        public double op(double a, double b);
    }

    final static RealBinaryOperation reAddOp = new RealBinaryOperation() {

        @Override
        public double op(double a, double b) {
            return a + b;
        }
    };

    final static RealBinaryOperation reSubOp = new RealBinaryOperation() {

        @Override
        public double op(double a, double b) {
            return a - b;
        }
    };

    final static RealBinaryOperation reMulOp = new RealBinaryOperation() {

        @Override
        public double op(double a, double b) {
            return a * b;
        }
    };

    final static RealBinaryOperation reDivOp = new RealBinaryOperation() {

        @Override
        public double op(double a, double b) {
            return a / b;
        }
    };

    final static RealBinaryOperation reMaxOp = new RealBinaryOperation() {

        @Override
        public double op(double a, double b) {
            return Math.max(a, b);
        }
    };

    final static RealBinaryOperation reMinOp = new RealBinaryOperation() {

        @Override
        public double op(double a, double b) {
            return Math.min(a, b);
        }
    };

    /**
     * Defines complex binary operations.
     */
    public interface ComplexBinaryOperation {

        /**
         * Performs a complex binary operation.
         */
        public void op(double[] res, double aRe, double aIm, double bRe, double bIm);
    }

    final static ComplexBinaryOperation ceAddOp = new ComplexBinaryOperation() {

        @Override
        public void op(double[] res, double aRe, double aIm, double bRe, double bIm) {

            res[0] = aRe + bRe;
            res[1] = aIm + bIm;
        }
    };

    final static ComplexBinaryOperation ceSubOp = new ComplexBinaryOperation() {

        @Override
        public void op(double[] res, double aRe, double aIm, double bRe, double bIm) {

            res[0] = aRe - bRe;
            res[1] = aIm - bIm;
        }
    };

    final static ComplexBinaryOperation ceMulOp = new ComplexBinaryOperation() {

        @Override
        public void op(double[] res, double aRe, double aIm, double bRe, double bIm) {

            res[0] = aRe * bRe - aIm * bIm;
            res[1] = aRe * bIm + bRe * aIm;
        }
    };

    final static ComplexBinaryOperation ceDivOp = new ComplexBinaryOperation() {

        @Override
        public void op(double[] res, double aRe, double aIm, double bRe, double bIm) {

            res[0] = (aRe * bRe + aIm * bIm) / (bRe * bRe + bIm * bIm);
            res[1] = (aIm * bRe - aRe * bIm) / (bRe * bRe + bIm * bIm);
        }
    };

    /**
     * Defines integer binary operations.
     */
    protected interface IntegerBinaryOperation {

        /**
         * Performs an integer binary operation.
         */
        public int op(int a, int b);
    }

    final static IntegerBinaryOperation ieAddOp = new IntegerBinaryOperation() {

        @Override
        public int op(int a, int b) {
            return a + b;
        }
    };

    final static IntegerBinaryOperation ieSubOp = new IntegerBinaryOperation() {

        @Override
        public int op(int a, int b) {
            return a - b;
        }
    };

    final static IntegerBinaryOperation ieMulOp = new IntegerBinaryOperation() {

        @Override
        public int op(int a, int b) {
            return a * b;
        }
    };

    final static IntegerBinaryOperation ieMaxOp = new IntegerBinaryOperation() {

        @Override
        public int op(int a, int b) {
            return Math.max(a, b);
        }
    };

    final static IntegerBinaryOperation ieMinOp = new IntegerBinaryOperation() {

        @Override
        public int op(int a, int b) {
            return Math.min(a, b);
        }
    };

    /**
     * Defines complex-to-real operations.
     */
    public interface ComplexToRealOperation {

        /**
         * Performs a complex-to-real operation.
         */
        public double op(double aRe, double aIm);
    }

    final static ComplexToRealOperation cToRAbsOp = new ComplexToRealOperation() {

        @Override
        public double op(double aRe, double aIm) {
            return Math.sqrt(aRe * aRe + aIm * aIm);
        }
    };

    final static ComplexToRealOperation cToRReOp = new ComplexToRealOperation() {

        @Override
        public double op(double aRe, double aIm) {
            return aRe;
        }
    };

    final static ComplexToRealOperation cToRImOp = new ComplexToRealOperation() {

        @Override
        public double op(double aRe, double aIm) {
            return aIm;
        }
    };

    /**
     * Defines real-to-complex operations.
     */
    protected interface RealToComplexOperation {

        /**
         * Performs a real-to-complex operation.
         */
        public void op(double[] res, double a);
    }

    final static RealToComplexOperation rToCReOp = new RealToComplexOperation() {

        @Override
        public void op(double[] res, double a) {

            res[0] = a;
            res[1] = 0;
        }
    };

    final static RealToComplexOperation rToCImOp = new RealToComplexOperation() {

        @Override
        public void op(double[] res, double a) {

            res[0] = 0;
            res[1] = a;
        }
    };

    final static RealBinaryOperation ruAbsOp = new RealBinaryOperation() {

        @Override
        public double op(double a, double v) {
            return Math.abs(v);
        }
    };

    final static RealBinaryOperation ruPowOp = new RealBinaryOperation() {

        @Override
        public double op(double a, double v) {
            return Math.pow(v, a);
        }
    };

    final static RealBinaryOperation ruExpOp = new RealBinaryOperation() {

        @Override
        public double op(double a, double v) {
            return Math.exp(v);
        }
    };

    final static RealBinaryOperation ruRndOp = new RealBinaryOperation() {

        @Override
        public double op(double a, double v) {
            return Arithmetic.nextDouble(a);
        }
    };

    final static RealBinaryOperation ruLogOp = new RealBinaryOperation() {

        @Override
        public double op(double a, double v) {
            return Math.log(v);
        }
    };

    final static RealBinaryOperation ruSqrtOp = new RealBinaryOperation() {

        @Override
        public double op(double a, double v) {
            return Math.sqrt(v);
        }
    };

    final static RealBinaryOperation ruSqrOp = new RealBinaryOperation() {

        @Override
        public double op(double a, double v) {
            return v * v;
        }
    };

    final static RealBinaryOperation ruInvOp = new RealBinaryOperation() {

        @Override
        public double op(double a, double v) {
            return a / v;
        }
    };

    final static RealBinaryOperation ruCosOp = new RealBinaryOperation() {

        @Override
        public double op(double a, double v) {
            return Math.cos(v);
        }
    };

    final static RealBinaryOperation ruSinOp = new RealBinaryOperation() {

        @Override
        public double op(double a, double v) {
            return Math.sin(v);
        }
    };

    final static RealBinaryOperation ruAtanOp = new RealBinaryOperation() {

        @Override
        public double op(double a, double v) {
            return Math.atan(v);
        }
    };

    final static RealBinaryOperation ruFillOp = new RealBinaryOperation() {

        @Override
        public double op(double a, double v) {
            return a;
        }
    };

    final static ComplexBinaryOperation cuExpOp = new ComplexBinaryOperation() {

        @Override
        public void op(double[] res, double aRe, double aIm, double vRe, double vIm) {

            res[0] = Math.exp(vRe) * Math.cos(vIm);
            res[1] = Math.exp(vRe) * Math.sin(vIm);
        }
    };

    final static ComplexBinaryOperation cuRndOp = new ComplexBinaryOperation() {

        @Override
        public void op(double[] res, double aRe, double aIm, double vRe, double vIm) {

            res[0] = Arithmetic.nextDouble(aRe);
            res[1] = Arithmetic.nextDouble(aIm);
        }
    };

    final static ComplexBinaryOperation cuConjOp = new ComplexBinaryOperation() {

        @Override
        public void op(double[] res, double aRe, double aIm, double vRe, double vIm) {

            res[0] = vRe;
            res[1] = -vIm;
        }
    };

    final static ComplexBinaryOperation cuCosOp = new ComplexBinaryOperation() {

        @Override
        public void op(double[] res, double aRe, double aIm, double vRe, double vIm) {

            ceMulOp.op(res, 0.0, 1.0, vRe, vIm);
            cuExpOp.op(res, Double.NaN, Double.NaN, res[0], res[1]);

            double xRe = res[0];
            double xIm = res[1];

            ceMulOp.op(res, 0.0, -1.0, vRe, vIm);
            cuExpOp.op(res, Double.NaN, Double.NaN, res[0], res[1]);

            double yRe = res[0];
            double yIm = res[1];

            ceAddOp.op(res, xRe, xIm, yRe, yIm);
            ceDivOp.op(res, res[0], res[1], 2.0, 0.0);
        }
    };

    final static ComplexBinaryOperation cuSinOp = new ComplexBinaryOperation() {

        @Override
        public void op(double[] res, double aRe, double aIm, double vRe, double vIm) {

            ceMulOp.op(res, 0.0, 1.0, vRe, vIm);
            cuExpOp.op(res, Double.NaN, Double.NaN, res[0], res[1]);

            double xRe = res[0];
            double xIm = res[1];

            ceMulOp.op(res, 0.0, -1.0, vRe, vIm);
            cuExpOp.op(res, Double.NaN, Double.NaN, res[0], res[1]);

            double yRe = res[0];
            double yIm = res[1];

            ceSubOp.op(res, xRe, xIm, yRe, yIm);
            ceDivOp.op(res, res[0], res[1], 0.0, 2.0);
        }
    };

    final static ComplexBinaryOperation cuFillOp = new ComplexBinaryOperation() {

        @Override
        public void op(double[] res, double aRe, double aIm, double vRe, double vIm) {

            res[0] = aRe;
            res[1] = aIm;
        }
    };

    final static IntegerBinaryOperation iuFillOp = new IntegerBinaryOperation() {

        @Override
        public int op(int a, int v) {
            return a;
        }
    };

    /**
     * Defines real accumulator operations.
     */
    protected interface RealAccumulatorOperation {

        /**
         * Performs a real accumulator operation.
         */
        public double op(double[] srcV);
    }

    final static RealAccumulatorOperation raSumOp = new RealAccumulatorOperation() {

        @Override
        public double op(double[] srcV) {
            return Arithmetic.sum(srcV);
        }
    };

    final static RealAccumulatorOperation raProdOp = new RealAccumulatorOperation() {

        @Override
        public double op(double[] srcV) {
            return Arithmetic.product(srcV);
        }
    };

    final static RealAccumulatorOperation raMaxOp = new RealAccumulatorOperation() {

        @Override
        public double op(double[] srcV) {
            return Arithmetic.max(srcV);
        }
    };

    final static RealAccumulatorOperation raMinOp = new RealAccumulatorOperation() {

        @Override
        public double op(double[] srcV) {
            return Arithmetic.min(srcV);
        }
    };

    final static RealAccumulatorOperation raVarOp = new RealAccumulatorOperation() {

        @Override
        public double op(double[] srcV) {
            return Arithmetic.variance(srcV);
        }
    };

    final static RealAccumulatorOperation raEntOp = new RealAccumulatorOperation() {

        @Override
        public double op(double[] srcV) {
            return Arithmetic.entropy(srcV);
        }
    };

    /**
     * Defines complex accumulator operations.
     */
    protected interface ComplexAccumulatorOperation {

        /**
         * Performs a complex accumulator operation.
         */
        public double[] op(double[] srcV);
    }

    final static ComplexAccumulatorOperation caSumOp = new ComplexAccumulatorOperation() {

        @Override
        public double[] op(double[] srcV) {

            double[] res = new double[2];

            for (int i = 0, n = srcV.length; i < n; i += 2) {
                ceAddOp.op(res, res[0], res[1], srcV[i], srcV[i + 1]);
            }

            return res;
        }
    };

    final static ComplexAccumulatorOperation caProdOp = new ComplexAccumulatorOperation() {

        @Override
        public double[] op(double[] srcV) {

            double[] res = new double[] { 1.0, 0.0 };

            for (int i = 0, n = srcV.length; i < n; i += 2) {
                ceMulOp.op(res, res[0], res[1], srcV[i], srcV[i + 1]);
            }

            return res;
        }
    };

    /**
     * A real accumulator operation in support of {@link JavaArrayKernel#raOp(int, double[])}.
     */
    final public static double raOp(int type, double[] srcV) {

        final RealAccumulatorOperation op;

        switch (type) {

        case RA_SUM:
            op = raSumOp;
            break;

        case RA_PROD:
            op = raProdOp;
            break;

        case RA_ENT:
            op = raEntOp;
            break;

        case RA_VAR:
            op = raVarOp;
            break;

        case RA_MAX:
            op = raMaxOp;
            break;

        case RA_MIN:
            op = raMinOp;
            break;

        default:
            throw new IllegalArgumentException();
        }

        return op.op(srcV);
    }

    /**
     * A complex accumulator operation in support of {@link JavaArrayKernel#caOp(int, double[])}.
     */
    final public static double[] caOp(int type, double[] srcV) {

        final ComplexAccumulatorOperation op;

        switch (type) {

        case CA_SUM:
            op = caSumOp;
            break;

        case CA_PROD:
            op = caProdOp;
            break;

        default:
            throw new IllegalArgumentException();
        }

        Control.checkTrue(srcV.length % 2 == 0);

        return op.op(srcV);
    }

    /**
     * A real unary elementwise operation in support of {@link JavaArrayKernel#ruOp(int, double, double[])}.
     */
    final public static void ruOp(int type, double a, double[] srcV) {

        final RealBinaryOperation op;

        switch (type) {

        case RU_ADD:
            op = reAddOp;
            break;

        case RU_MUL:
            op = reMulOp;
            break;

        case RU_EXP:
            op = ruExpOp;
            break;

        case RU_LOG:
            op = ruLogOp;
            break;

        case RU_POW:
            op = ruPowOp;
            break;

        case RU_RND:
            op = ruRndOp;
            break;

        case RU_ABS:
            op = ruAbsOp;
            break;

        case RU_SQRT:
            op = ruSqrtOp;
            break;

        case RU_SQR:
            op = ruSqrOp;
            break;

        case RU_INV:
            op = ruInvOp;
            break;

        case RU_COS:
            op = ruCosOp;
            break;

        case RU_SIN:
            op = ruSinOp;
            break;

        case RU_ATAN:
            op = ruAtanOp;
            break;

        case RU_FILL:
            op = ruFillOp;
            break;

        case RU_SHUFFLE:
            Arithmetic.shuffle(srcV);
            return;

        default:
            throw new IllegalArgumentException();
        }

        for (int i = 0, n = srcV.length; i < n; i++) {
            srcV[i] = op.op(a, srcV[i]);
        }
    }

    /**
     * A complex unary elementwise operation in support of {@link JavaArrayKernel#cuOp(int, double, double, double[])}.
     */
    final public static void cuOp(int type, double aRe, double aIm, double[] srcV) {

        double[] tmp = new double[2];

        int n = srcV.length;

        final ComplexBinaryOperation op;

        switch (type) {

        case CU_ADD:
            op = ceAddOp;
            break;

        case CU_MUL:
            op = ceMulOp;
            break;

        case CU_EXP:
            op = cuExpOp;
            break;

        case CU_RND:
            op = cuRndOp;
            break;

        case CU_CONJ:
            op = cuConjOp;
            break;

        case CU_COS:
            op = cuCosOp;
            break;

        case CU_SIN:
            op = cuSinOp;
            break;

        case CU_FILL:
            op = cuFillOp;
            break;

        case CU_SHUFFLE:

            for (int i = n / 2; i > 1; i--) {

                int index = Arithmetic.nextInt(i);

                double tmpRe = srcV[2 * (i - 1)];
                double tmpIm = srcV[2 * (i - 1) + 1];

                srcV[2 * (i - 1)] = srcV[2 * index];
                srcV[2 * (i - 1) + 1] = srcV[2 * index + 1];

                srcV[2 * index] = tmpRe;
                srcV[2 * index + 1] = tmpIm;
            }

            return;

        default:
            throw new IllegalArgumentException();
        }

        Control.checkTrue(n % 2 == 0);

        for (int i = 0; i < n; i += 2) {

            op.op(tmp, aRe, aIm, srcV[i], srcV[i + 1]);

            srcV[i] = tmp[0];
            srcV[i + 1] = tmp[1];
        }
    }

    /**
     * An integer unary elementwise operation in support of {@link JavaArrayKernel#iuOp(int, int, int[])}.
     */
    final public static void iuOp(int type, int a, int[] srcV) {

        final IntegerBinaryOperation op;

        switch (type) {

        case IU_ADD:
            op = ieAddOp;
            break;

        case IU_MUL:
            op = ieMulOp;
            break;

        case IU_FILL:
            op = iuFillOp;
            break;

        case IU_SHUFFLE:
            Arithmetic.shuffle(srcV);
            return;

        default:
            throw new IllegalArgumentException();
        }

        for (int i = 0, n = srcV.length; i < n; i++) {
            srcV[i] = op.op(a, srcV[i]);
        }
    }

    /**
     * A binary elementwise operation in support of {@link JavaArrayKernel#eOp(int, Object, Object, Object, boolean)}.
     */
    final public static void eOp(int type, Object lhs, Object rhs, Object dst, boolean complex) {

        if (lhs instanceof double[] && rhs instanceof double[] && dst instanceof double[]) {

            final double[] lhsV = (double[]) lhs;
            final double[] rhsV = (double[]) rhs;
            final double[] dstV = (double[]) dst;

            if (complex) {

                final ComplexBinaryOperation op;

                switch (type) {

                case CE_ADD:
                    op = ceAddOp;
                    break;

                case CE_SUB:
                    op = ceSubOp;
                    break;

                case CE_MUL:
                    op = ceMulOp;
                    break;

                case CE_DIV:
                    op = ceDivOp;
                    break;

                default:
                    throw new IllegalArgumentException();
                }

                double[] tmp = new double[2];

                int n = Control.checkEquals(Control.checkEquals(lhsV.length, rhsV.length), dstV.length);

                Control.checkTrue(n % 2 == 0);

                for (int i = 0; i < n; i += 2) {

                    op.op(tmp, lhsV[i], lhsV[i + 1], rhsV[i], rhsV[i + 1]);

                    dstV[i] = tmp[0];
                    dstV[i + 1] = tmp[1];
                }

            } else {

                final RealBinaryOperation op;

                switch (type) {

                case RE_ADD:
                    op = reAddOp;
                    break;

                case RE_SUB:
                    op = reSubOp;
                    break;

                case RE_MUL:
                    op = reMulOp;
                    break;

                case RE_DIV:
                    op = reDivOp;
                    break;

                case RE_MAX:
                    op = reMaxOp;
                    break;

                case RE_MIN:
                    op = reMinOp;
                    break;

                default:
                    throw new IllegalArgumentException();
                }

                for (int i = 0, n = Control.checkEquals(Control.checkEquals(lhsV.length, rhsV.length), dstV.length); //
                i < n; //
                i++) {
                    dstV[i] = op.op(lhsV[i], rhsV[i]);
                }
            }

        } else if (lhs instanceof int[] && rhs instanceof int[] && dst instanceof int[]) {

            final int[] lhsV = (int[]) lhs;
            final int[] rhsV = (int[]) rhs;
            final int[] dstV = (int[]) dst;

            final IntegerBinaryOperation op;

            switch (type) {

            case IE_ADD:
                op = ieAddOp;
                break;

            case IE_SUB:
                op = ieSubOp;
                break;

            case IE_MUL:
                op = ieMulOp;
                break;

            case IE_MAX:
                op = ieMaxOp;
                break;

            case IE_MIN:
                op = ieMinOp;
                break;

            default:
                throw new IllegalArgumentException();
            }

            for (int i = 0, n = Control.checkEquals(Control.checkEquals(lhsV.length, rhsV.length), dstV.length); //
            i < n; //
            i++) {
                dstV[i] = op.op(lhsV[i], rhsV[i]);
            }

        } else {

            throw new IllegalArgumentException("Invalid array types");
        }
    }

    /**
     * A type conversion operation in support of {@link JavaArrayKernel#convert(int, Object, boolean, Object, boolean)}.
     */
    final public static void convert(int type, Object srcV, boolean isSrcComplex, Object dstV, boolean isDstComplex) {

        if (srcV instanceof double[] && !isSrcComplex && dstV instanceof double[] && isDstComplex) {

            double[] srcV_d = (double[]) srcV;
            double[] dstV_d = (double[]) dstV;

            final RealToComplexOperation op;

            switch (type) {

            case R_TO_C_RE:
                op = rToCReOp;
                break;

            case R_TO_C_IM:
                op = rToCImOp;
                break;

            default:
                throw new IllegalArgumentException();
            }

            double[] tmp = new double[2];

            for (int i = 0, j = 0, n = Control.checkEquals(srcV_d.length * 2, dstV_d.length) / 2; //
            i < n; //
            i++, j += 2) {

                op.op(tmp, srcV_d[i]);

                dstV_d[j] = tmp[0];
                dstV_d[j + 1] = tmp[1];
            }

        } else if (srcV instanceof double[] && isSrcComplex && dstV instanceof double[] && !isDstComplex) {

            double[] srcV_d = (double[]) srcV;
            double[] dstV_d = (double[]) dstV;

            final ComplexToRealOperation op;

            switch (type) {

            case C_TO_R_ABS:
                op = cToRAbsOp;
                break;

            case C_TO_R_RE:
                op = cToRReOp;
                break;

            case C_TO_R_IM:
                op = cToRImOp;
                break;

            default:
                throw new IllegalArgumentException();
            }

            for (int i = 0, j = 0, n = Control.checkEquals(srcV_d.length, dstV_d.length * 2); //
            i < n; //
            i += 2, j++) {
                dstV_d[j] = op.op(srcV_d[i], srcV_d[i + 1]);
            }

        } else if (srcV instanceof int[] && !isSrcComplex && dstV instanceof double[] && !isDstComplex) {

            int[] srcV_i = (int[]) srcV;
            double[] dstV_d = (double[]) dstV;

            for (int i = 0, n = Control.checkEquals(srcV_i.length, dstV_d.length); i < n; i++) {
                dstV_d[i] = srcV_i[i];
            }

        } else {

            throw new IllegalArgumentException("Invalid array types");
        }
    }

    // Dummy constructor.
    ElementOps() {
    }
}

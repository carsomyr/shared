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

import static shared.array.kernel.ArrayKernel.CA_PROD;
import static shared.array.kernel.ArrayKernel.CA_SUM;
import static shared.array.kernel.ArrayKernel.CE_ADD;
import static shared.array.kernel.ArrayKernel.CE_DIV;
import static shared.array.kernel.ArrayKernel.CE_MUL;
import static shared.array.kernel.ArrayKernel.CE_SUB;
import static shared.array.kernel.ArrayKernel.CTOR_ABS;
import static shared.array.kernel.ArrayKernel.CTOR_IM;
import static shared.array.kernel.ArrayKernel.CTOR_RE;
import static shared.array.kernel.ArrayKernel.CU_ADD;
import static shared.array.kernel.ArrayKernel.CU_CONJ;
import static shared.array.kernel.ArrayKernel.CU_COS;
import static shared.array.kernel.ArrayKernel.CU_EXP;
import static shared.array.kernel.ArrayKernel.CU_FILL;
import static shared.array.kernel.ArrayKernel.CU_MUL;
import static shared.array.kernel.ArrayKernel.CU_RND;
import static shared.array.kernel.ArrayKernel.CU_SHUFFLE;
import static shared.array.kernel.ArrayKernel.CU_SIN;
import static shared.array.kernel.ArrayKernel.IE_ADD;
import static shared.array.kernel.ArrayKernel.IE_MAX;
import static shared.array.kernel.ArrayKernel.IE_MIN;
import static shared.array.kernel.ArrayKernel.IE_MUL;
import static shared.array.kernel.ArrayKernel.IE_SUB;
import static shared.array.kernel.ArrayKernel.IU_ADD;
import static shared.array.kernel.ArrayKernel.IU_FILL;
import static shared.array.kernel.ArrayKernel.IU_MUL;
import static shared.array.kernel.ArrayKernel.IU_SHUFFLE;
import static shared.array.kernel.ArrayKernel.RA_ENT;
import static shared.array.kernel.ArrayKernel.RA_MAX;
import static shared.array.kernel.ArrayKernel.RA_MIN;
import static shared.array.kernel.ArrayKernel.RA_PROD;
import static shared.array.kernel.ArrayKernel.RA_SUM;
import static shared.array.kernel.ArrayKernel.RA_VAR;
import static shared.array.kernel.ArrayKernel.RE_ADD;
import static shared.array.kernel.ArrayKernel.RE_DIV;
import static shared.array.kernel.ArrayKernel.RE_MAX;
import static shared.array.kernel.ArrayKernel.RE_MIN;
import static shared.array.kernel.ArrayKernel.RE_MUL;
import static shared.array.kernel.ArrayKernel.RE_SUB;
import static shared.array.kernel.ArrayKernel.RTOC_IM;
import static shared.array.kernel.ArrayKernel.RTOC_RE;
import static shared.array.kernel.ArrayKernel.RU_ABS;
import static shared.array.kernel.ArrayKernel.RU_ADD;
import static shared.array.kernel.ArrayKernel.RU_ATAN;
import static shared.array.kernel.ArrayKernel.RU_COS;
import static shared.array.kernel.ArrayKernel.RU_EXP;
import static shared.array.kernel.ArrayKernel.RU_FILL;
import static shared.array.kernel.ArrayKernel.RU_INV;
import static shared.array.kernel.ArrayKernel.RU_LOG;
import static shared.array.kernel.ArrayKernel.RU_MUL;
import static shared.array.kernel.ArrayKernel.RU_POW;
import static shared.array.kernel.ArrayKernel.RU_RND;
import static shared.array.kernel.ArrayKernel.RU_SHUFFLE;
import static shared.array.kernel.ArrayKernel.RU_SIN;
import static shared.array.kernel.ArrayKernel.RU_SQR;
import static shared.array.kernel.ArrayKernel.RU_SQRT;
import shared.util.Arithmetic;
import shared.util.Control;

/**
 * A class for elementwise operations in pure Java.
 * 
 * @apiviz.has shared.array.kernel.ElementOps.RealBinaryOperation - - - argument
 * @apiviz.has shared.array.kernel.ElementOps.RealAccumulatorOperation - - - argument
 * @apiviz.has shared.array.kernel.ElementOps.RealToComplexOperation - - - argument
 * @apiviz.has shared.array.kernel.ElementOps.IntegerBinaryOperation - - - argument
 * @apiviz.has shared.array.kernel.ElementOps.ComplexBinaryOperation - - - argument
 * @apiviz.has shared.array.kernel.ElementOps.ComplexAccumulatorOperation - - - argument
 * @apiviz.has shared.array.kernel.ElementOps.ComplexToRealOperation - - - argument
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

    final static RealBinaryOperation REAddOp = new RealBinaryOperation() {

        public double op(double a, double b) {
            return a + b;
        }
    };

    final static RealBinaryOperation RESubOp = new RealBinaryOperation() {

        public double op(double a, double b) {
            return a - b;
        }
    };

    final static RealBinaryOperation REMulOp = new RealBinaryOperation() {

        public double op(double a, double b) {
            return a * b;
        }
    };

    final static RealBinaryOperation REDivOp = new RealBinaryOperation() {

        public double op(double a, double b) {
            return a / b;
        }
    };

    final static RealBinaryOperation REMaxOp = new RealBinaryOperation() {

        public double op(double a, double b) {
            return Math.max(a, b);
        }
    };

    final static RealBinaryOperation REMinOp = new RealBinaryOperation() {

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

    final static ComplexBinaryOperation CEAddOp = new ComplexBinaryOperation() {

        public void op(double[] res, double aRe, double aIm, double bRe, double bIm) {

            res[0] = aRe + bRe;
            res[1] = aIm + bIm;
        }
    };

    final static ComplexBinaryOperation CESubOp = new ComplexBinaryOperation() {

        public void op(double[] res, double aRe, double aIm, double bRe, double bIm) {

            res[0] = aRe - bRe;
            res[1] = aIm - bIm;
        }
    };

    final static ComplexBinaryOperation CEMulOp = new ComplexBinaryOperation() {

        public void op(double[] res, double aRe, double aIm, double bRe, double bIm) {

            res[0] = aRe * bRe - aIm * bIm;
            res[1] = aRe * bIm + bRe * aIm;
        }
    };

    final static ComplexBinaryOperation CEDivOp = new ComplexBinaryOperation() {

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

    final static IntegerBinaryOperation IEAddOp = new IntegerBinaryOperation() {

        public int op(int a, int b) {
            return a + b;
        }
    };

    final static IntegerBinaryOperation IESubOp = new IntegerBinaryOperation() {

        public int op(int a, int b) {
            return a - b;
        }
    };

    final static IntegerBinaryOperation IEMulOp = new IntegerBinaryOperation() {

        public int op(int a, int b) {
            return a * b;
        }
    };

    final static IntegerBinaryOperation IEMaxOp = new IntegerBinaryOperation() {

        public int op(int a, int b) {
            return Math.max(a, b);
        }
    };

    final static IntegerBinaryOperation IEMinOp = new IntegerBinaryOperation() {

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

    final static ComplexToRealOperation CTORAbsOp = new ComplexToRealOperation() {

        public double op(double aRe, double aIm) {
            return Math.sqrt(aRe * aRe + aIm * aIm);
        }
    };

    final static ComplexToRealOperation CTORReOp = new ComplexToRealOperation() {

        public double op(double aRe, double aIm) {
            return aRe;
        }
    };

    final static ComplexToRealOperation CTORImOp = new ComplexToRealOperation() {

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

    final static RealToComplexOperation RTOCReOp = new RealToComplexOperation() {

        public void op(double[] res, double a) {

            res[0] = a;
            res[1] = 0;
        }
    };

    final static RealToComplexOperation RTOCImOp = new RealToComplexOperation() {

        public void op(double[] res, double a) {

            res[0] = 0;
            res[1] = a;
        }
    };

    final static RealBinaryOperation RUAbsOp = new RealBinaryOperation() {

        public double op(double a, double v) {
            return Math.abs(v);
        }
    };

    final static RealBinaryOperation RUPowOp = new RealBinaryOperation() {

        public double op(double a, double v) {
            return Math.pow(v, a);
        }
    };

    final static RealBinaryOperation RUExpOp = new RealBinaryOperation() {

        public double op(double a, double v) {
            return Math.exp(v);
        }
    };

    final static RealBinaryOperation RURndOp = new RealBinaryOperation() {

        public double op(double a, double v) {
            return Arithmetic.nextDouble(a);
        }
    };

    final static RealBinaryOperation RULogOp = new RealBinaryOperation() {

        public double op(double a, double v) {
            return Math.log(v);
        }
    };

    final static RealBinaryOperation RUSqrtOp = new RealBinaryOperation() {

        public double op(double a, double v) {
            return Math.sqrt(v);
        }
    };

    final static RealBinaryOperation RUSqrOp = new RealBinaryOperation() {

        public double op(double a, double v) {
            return v * v;
        }
    };

    final static RealBinaryOperation RUInvOp = new RealBinaryOperation() {

        public double op(double a, double v) {
            return a / v;
        }
    };

    final static RealBinaryOperation RUCosOp = new RealBinaryOperation() {

        public double op(double a, double v) {
            return Math.cos(v);
        }
    };

    final static RealBinaryOperation RUSinOp = new RealBinaryOperation() {

        public double op(double a, double v) {
            return Math.sin(v);
        }
    };

    final static RealBinaryOperation RUAtanOp = new RealBinaryOperation() {

        public double op(double a, double v) {
            return Math.atan(v);
        }
    };

    final static RealBinaryOperation RUFillOp = new RealBinaryOperation() {

        public double op(double a, double v) {
            return a;
        }
    };

    final static ComplexBinaryOperation CUExpOp = new ComplexBinaryOperation() {

        public void op(double[] res, double aRe, double aIm, double vRe, double vIm) {

            res[0] = Math.exp(vRe) * Math.cos(vIm);
            res[1] = Math.exp(vRe) * Math.sin(vIm);
        }
    };

    final static ComplexBinaryOperation CURndOp = new ComplexBinaryOperation() {

        public void op(double[] res, double aRe, double aIm, double vRe, double vIm) {

            res[0] = Arithmetic.nextDouble(aRe);
            res[1] = Arithmetic.nextDouble(aIm);
        }
    };

    final static ComplexBinaryOperation CUConjOp = new ComplexBinaryOperation() {

        public void op(double[] res, double aRe, double aIm, double vRe, double vIm) {

            res[0] = vRe;
            res[1] = -vIm;
        }
    };

    final static ComplexBinaryOperation CUCosOp = new ComplexBinaryOperation() {

        public void op(double[] res, double aRe, double aIm, double vRe, double vIm) {

            CEMulOp.op(res, 0.0, 1.0, vRe, vIm);
            CUExpOp.op(res, Double.NaN, Double.NaN, res[0], res[1]);

            double xRe = res[0];
            double xIm = res[1];

            CEMulOp.op(res, 0.0, -1.0, vRe, vIm);
            CUExpOp.op(res, Double.NaN, Double.NaN, res[0], res[1]);

            double yRe = res[0];
            double yIm = res[1];

            CEAddOp.op(res, xRe, xIm, yRe, yIm);
            CEDivOp.op(res, res[0], res[1], 2.0, 0.0);
        }
    };

    final static ComplexBinaryOperation CUSinOp = new ComplexBinaryOperation() {

        public void op(double[] res, double aRe, double aIm, double vRe, double vIm) {

            CEMulOp.op(res, 0.0, 1.0, vRe, vIm);
            CUExpOp.op(res, Double.NaN, Double.NaN, res[0], res[1]);

            double xRe = res[0];
            double xIm = res[1];

            CEMulOp.op(res, 0.0, -1.0, vRe, vIm);
            CUExpOp.op(res, Double.NaN, Double.NaN, res[0], res[1]);

            double yRe = res[0];
            double yIm = res[1];

            CESubOp.op(res, xRe, xIm, yRe, yIm);
            CEDivOp.op(res, res[0], res[1], 0.0, 2.0);
        }
    };

    final static ComplexBinaryOperation CUFillOp = new ComplexBinaryOperation() {

        public void op(double[] res, double aRe, double aIm, double vRe, double vIm) {

            res[0] = aRe;
            res[1] = aIm;
        }
    };

    final static IntegerBinaryOperation IUFillOp = new IntegerBinaryOperation() {

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

    final static RealAccumulatorOperation RASumOp = new RealAccumulatorOperation() {

        public double op(double[] srcV) {
            return Arithmetic.sum(srcV);
        }
    };

    final static RealAccumulatorOperation RAProdOp = new RealAccumulatorOperation() {

        public double op(double[] srcV) {
            return Arithmetic.product(srcV);
        }
    };

    final static RealAccumulatorOperation RAMaxOp = new RealAccumulatorOperation() {

        public double op(double[] srcV) {
            return Arithmetic.max(srcV);
        }
    };

    final static RealAccumulatorOperation RAMinOp = new RealAccumulatorOperation() {

        public double op(double[] srcV) {
            return Arithmetic.min(srcV);
        }
    };

    final static RealAccumulatorOperation RAVarOp = new RealAccumulatorOperation() {

        public double op(double[] srcV) {
            return Arithmetic.variance(srcV);
        }
    };

    final static RealAccumulatorOperation RAEntOp = new RealAccumulatorOperation() {

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

    final static ComplexAccumulatorOperation CASumOp = new ComplexAccumulatorOperation() {

        public double[] op(double[] srcV) {

            double[] res = new double[2];

            for (int i = 0, n = srcV.length; i < n; i += 2) {
                CEAddOp.op(res, res[0], res[1], srcV[i], srcV[i + 1]);
            }

            return res;
        }
    };

    final static ComplexAccumulatorOperation CAProdOp = new ComplexAccumulatorOperation() {

        public double[] op(double[] srcV) {

            double[] res = new double[] { 1.0, 0.0 };

            for (int i = 0, n = srcV.length; i < n; i += 2) {
                CEMulOp.op(res, res[0], res[1], srcV[i], srcV[i + 1]);
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
            op = RASumOp;
            break;

        case RA_PROD:
            op = RAProdOp;
            break;

        case RA_ENT:
            op = RAEntOp;
            break;

        case RA_VAR:
            op = RAVarOp;
            break;

        case RA_MAX:
            op = RAMaxOp;
            break;

        case RA_MIN:
            op = RAMinOp;
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
            op = CASumOp;
            break;

        case CA_PROD:
            op = CAProdOp;
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
            op = REAddOp;
            break;

        case RU_MUL:
            op = REMulOp;
            break;

        case RU_EXP:
            op = RUExpOp;
            break;

        case RU_LOG:
            op = RULogOp;
            break;

        case RU_POW:
            op = RUPowOp;
            break;

        case RU_RND:
            op = RURndOp;
            break;

        case RU_ABS:
            op = RUAbsOp;
            break;

        case RU_SQRT:
            op = RUSqrtOp;
            break;

        case RU_SQR:
            op = RUSqrOp;
            break;

        case RU_INV:
            op = RUInvOp;
            break;

        case RU_COS:
            op = RUCosOp;
            break;

        case RU_SIN:
            op = RUSinOp;
            break;

        case RU_ATAN:
            op = RUAtanOp;
            break;

        case RU_FILL:
            op = RUFillOp;
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
            op = CEAddOp;
            break;

        case CU_MUL:
            op = CEMulOp;
            break;

        case CU_EXP:
            op = CUExpOp;
            break;

        case CU_RND:
            op = CURndOp;
            break;

        case CU_CONJ:
            op = CUConjOp;
            break;

        case CU_COS:
            op = CUCosOp;
            break;

        case CU_SIN:
            op = CUSinOp;
            break;

        case CU_FILL:
            op = CUFillOp;
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
            op = IEAddOp;
            break;

        case IU_MUL:
            op = IEMulOp;
            break;

        case IU_FILL:
            op = IUFillOp;
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
    final public static void eOp(int type, Object lhs, Object rhs, Object dst, boolean isComplex) {

        if (lhs instanceof double[] && rhs instanceof double[] && dst instanceof double[]) {

            final double[] lhsV = (double[]) lhs;
            final double[] rhsV = (double[]) rhs;
            final double[] dstV = (double[]) dst;

            if (isComplex) {

                final ComplexBinaryOperation op;

                switch (type) {

                case CE_ADD:
                    op = CEAddOp;
                    break;

                case CE_SUB:
                    op = CESubOp;
                    break;

                case CE_MUL:
                    op = CEMulOp;
                    break;

                case CE_DIV:
                    op = CEDivOp;
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
                    op = REAddOp;
                    break;

                case RE_SUB:
                    op = RESubOp;
                    break;

                case RE_MUL:
                    op = REMulOp;
                    break;

                case RE_DIV:
                    op = REDivOp;
                    break;

                case RE_MAX:
                    op = REMaxOp;
                    break;

                case RE_MIN:
                    op = REMinOp;
                    break;

                default:
                    throw new IllegalArgumentException();
                }

                for (int i = 0, n = Control.checkEquals(Control.checkEquals(lhsV.length, rhsV.length), dstV.length); //
                i < n; i++) {
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
                op = IEAddOp;
                break;

            case IE_SUB:
                op = IESubOp;
                break;

            case IE_MUL:
                op = IEMulOp;
                break;

            case IE_MAX:
                op = IEMaxOp;
                break;

            case IE_MIN:
                op = IEMinOp;
                break;

            default:
                throw new IllegalArgumentException();
            }

            for (int i = 0, n = Control.checkEquals(Control.checkEquals(lhsV.length, rhsV.length), dstV.length); //
            i < n; i++) {
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

            case RTOC_RE:
                op = RTOCReOp;
                break;

            case RTOC_IM:
                op = RTOCImOp;
                break;

            default:
                throw new IllegalArgumentException();
            }

            double[] tmp = new double[2];

            for (int i = 0, j = 0, n = Control.checkEquals(srcV_d.length * 2, dstV_d.length) / 2; //
            i < n; i++, j += 2) {

                op.op(tmp, srcV_d[i]);

                dstV_d[j] = tmp[0];
                dstV_d[j + 1] = tmp[1];
            }

        } else if (srcV instanceof double[] && isSrcComplex && dstV instanceof double[] && !isDstComplex) {

            double[] srcV_d = (double[]) srcV;
            double[] dstV_d = (double[]) dstV;

            final ComplexToRealOperation op;

            switch (type) {

            case CTOR_ABS:
                op = CTORAbsOp;
                break;

            case CTOR_RE:
                op = CTORReOp;
                break;

            case CTOR_IM:
                op = CTORImOp;
                break;

            default:
                throw new IllegalArgumentException();
            }

            for (int i = 0, j = 0, n = Control.checkEquals(srcV_d.length, dstV_d.length * 2); //
            i < n; i += 2, j++) {
                dstV_d[j] = op.op(srcV_d[i], srcV_d[i + 1]);
            }

        } else if (srcV instanceof int[] && !isSrcComplex && dstV instanceof double[] && !isDstComplex) {

            int[] srcV_i = (int[]) srcV;
            double[] dstV_d = (double[]) dstV;

            for (int i = 0, n = Control.checkEquals(srcV_i.length, dstV_d.length); i < n; i++) {
                dstV_d[i] = srcV_i[i];
            }

        } else {

            throw new IllegalArgumentException("Invalid arguments");
        }
    }

    // Dummy constructor.
    ElementOps() {
    }
}

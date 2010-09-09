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

package shared.test.array;

import static shared.array.ArrayBase.OpKernel;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import shared.array.jni.NativeArrayKernel;
import shared.array.kernel.ArrayKernel;
import shared.array.kernel.JavaArrayKernel;
import shared.array.kernel.ModalArrayKernel;
import shared.test.Tests;

/**
 * A class of unit tests for {@link NativeArrayKernel} and {@link JavaArrayKernel}, depending on which is selected by
 * {@link ModalArrayKernel}.
 * 
 * @author Roy Liu
 */
public class ArrayKernelTest {

    /**
     * Default constructor.
     */
    public ArrayKernelTest() {
    }

    /**
     * Tests all accumulator, unary, binary, and conversion operations.
     */
    @Test
    public void testOperations() {

        ArrayKernel kernel = OpKernel;

        double[] v;

        //

        Assert.assertTrue(Math.abs(kernel.raOp(ArrayKernel.RA_SUM, //
                new double[] { 1.0, -0.5, 0.25, -0.125 }) - 0.625) < 1e-12);

        Assert.assertTrue(Math.abs(kernel.raOp(ArrayKernel.RA_PROD, //
                new double[] { 1.0, 2.0, 3.0, 4.0 }) - 24.0) < 1e-12);

        Assert.assertTrue(Math.abs(kernel.raOp(ArrayKernel.RA_MAX, //
                new double[] { 1.0, 2.0, 1.0 }) - 2.0) < 1e-12);

        Assert.assertTrue(Math.abs(kernel.raOp(ArrayKernel.RA_MIN, //
                new double[] { 1.0, 2.0, 1.0 }) - 1.0) < 1e-12);

        Assert.assertTrue(Math.abs(kernel.raOp(ArrayKernel.RA_VAR, //
                new double[] { 0.0, 2.0, 4.0 }) - 8.0 / 3.0) < 1e-12);

        Assert.assertTrue(Math.abs(kernel.raOp(ArrayKernel.RA_ENT, //
                new double[] { 1, 0.5, 0.5 }) - 1.03972077) < 1e-8);

        //

        Assert.assertTrue(Tests.equals(kernel.caOp(ArrayKernel.CA_SUM, //
                new double[] { 0.0, 1.0, 1.0, 0.0, 0.5, 0.5 }), //
                new double[] { 1.5, 1.5 }));

        Assert.assertTrue(Tests.equals(kernel.caOp(ArrayKernel.CA_PROD, //
                new double[] { 1.0, 0.5, 0.5, 0.25, 0.25, 0.5 }), //
                new double[] { -0.15625, 0.3125 }));

        //

        kernel.ruOp(ArrayKernel.RU_ADD, 2, v = new double[] { 0, -1, 1, 0, -1, 1, 0, -1, 1 });
        Assert.assertTrue(Tests.equals(v, new double[] { 2, 1, 3, 2, 1, 3, 2, 1, 3 }));

        kernel.ruOp(ArrayKernel.RU_MUL, 2, v = new double[] { 0, -1, 1, 0, -1, 1, 0, -1, 1 });
        Assert.assertTrue(Tests.equals(v, new double[] { 0, -2, 2, 0, -2, 2, 0, -2, 2 }));

        kernel.ruOp(ArrayKernel.RU_EXP, Double.NaN, v = new double[] { 0, -1, 1, 0, -1, 1 });
        Assert.assertTrue(Tests.equals(v, new double[] { Math.exp(0), Math.exp(-1), Math.exp(1), //
                Math.exp(0), Math.exp(-1), Math.exp(1) }));

        kernel.ruOp(ArrayKernel.RU_COS, Double.NaN, //
                v = new double[] { 0, 1, 2, 3 });
        Assert.assertTrue(Tests.equals(v, new double[] {
                //
                1.0, 0.5403023058681398, -0.4161468365471424, -0.9899924966004454 }));

        kernel.ruOp(ArrayKernel.RU_SIN, Double.NaN, //
                v = new double[] { 0, 1, 2, 3 });
        Assert.assertTrue(Tests.equals(v, new double[] {
                //
                0.0, 0.8414709848078965, 0.9092974268256817, 0.1411200080598672 }));

        kernel.ruOp(ArrayKernel.RU_ATAN, Double.NaN, //
                v = new double[] { 0, 1, 2, 3 });
        Assert.assertTrue(Tests.equals(v, new double[] {
                //
                0.0, 0.7853981633974483, 1.1071487177940904, 1.2490457723982544 }));

        kernel.ruOp(ArrayKernel.RU_LOG, Double.NaN, v = new double[] {
                //
                Math.E, Math.E * Math.E, Math.E * Math.E * Math.E, //
                Math.E, Math.E * Math.E, Math.E * Math.E * Math.E });
        Assert.assertTrue(Tests.equals(v, new double[] { 1, 2, 3, 1, 2, 3 }));

        kernel.ruOp(ArrayKernel.RU_ABS, Double.NaN, v = new double[] { 0, -1, 1, 0, -1, 1, 0, -1, 1 });
        Assert.assertTrue(Tests.equals(v, new double[] { 0, 1, 1, 0, 1, 1, 0, 1, 1 }));

        kernel.ruOp(ArrayKernel.RU_POW, 1.0 / 3.0, v = new double[] { 1, 8, 64, 1, 8, 64, 1, 8, 64 });
        Assert.assertTrue(Tests.equals(v, new double[] { 1, 2, 4, 1, 2, 4, 1, 2, 4 }));

        kernel.ruOp(ArrayKernel.RU_SQRT, Double.NaN, v = new double[] { 1, 4, 16, 1, 4, 16, 1, 4, 16 });
        Assert.assertTrue(Tests.equals(v, new double[] { 1, 2, 4, 1, 2, 4, 1, 2, 4 }));

        kernel.ruOp(ArrayKernel.RU_SQR, Double.NaN, v = new double[] { 1, 2, 4, 1, 2, 4, 1, 2, 4 });
        Assert.assertTrue(Tests.equals(v, new double[] { 1, 4, 16, 1, 4, 16, 1, 4, 16 }));

        kernel.ruOp(ArrayKernel.RU_INV, 0.5, v = new double[] { 1, 2, 4, 1, 2, 4, 1, 2, 4 });
        Assert.assertTrue(Tests.equals(v, new double[] { 0.5, 0.25, 0.125, 0.5, 0.25, 0.125, 0.5, 0.25, 0.125 }));

        kernel.ruOp(ArrayKernel.RU_FILL, 0.5, v = new double[] { 1, 2, 3, 4, 5, 6, 7, 8 });
        Assert.assertTrue(Tests.equals(v, new double[] { 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5 }));

        kernel.ruOp(ArrayKernel.RU_SHUFFLE, Double.NaN, //
                v = new double[] { 1, 2, 3, 4, 5, 6, 7, 8 });
        Arrays.sort(v);
        Assert.assertTrue(Tests.equals(v, new double[] { 1, 2, 3, 4, 5, 6, 7, 8 }));

        int nSamples = 1 << 16;
        int nBins = 1 << 2;
        int samplesPerBin = nSamples / nBins;

        kernel.ruOp(ArrayKernel.RU_RND, Math.PI, v = new double[nSamples]);
        kernel.riOp(ArrayKernel.RI_SORT, v, new int[] { nSamples }, new int[] { 1 }, new int[nSamples], 0);

        for (int i = 0, binOffset = 0; i < nBins; i++, binOffset += samplesPerBin) {

            double acc = 0.0;

            for (int j = 0; j < samplesPerBin; j++) {
                acc += v[binOffset + j];
            }

            Assert.assertTrue(Math.abs(acc / (Math.PI * samplesPerBin) - (i + 0.5) / nBins) < 0.005);
        }

        //

        kernel.cuOp(ArrayKernel.CU_ADD, 0.5, -0.5, v = new double[] { 0, 0, -1, 1, 1, -1, 0, 0, -1, 1, 1, -1 });
        Assert.assertTrue(Tests.equals(v, //
                new double[] { 0.5, -0.5, -0.5, 0.5, 1.5, -1.5, 0.5, -0.5, -0.5, 0.5, 1.5, -1.5 }));

        kernel.cuOp(ArrayKernel.CU_MUL, 0.5, -0.5, v = new double[] { 0, 0, -1, 1, 1, -1, 0, 0, -1, 1, 1, -1 });
        Assert.assertTrue(Tests.equals(v, new double[] { 0, 0, 0, 1, 0, -1, 0, 0, 0, 1, 0, -1 }));

        kernel.cuOp(ArrayKernel.CU_EXP, Double.NaN, Double.NaN, //
                v = new double[] { 0, 0, -1, 1, 1, -1, 0, 0, -1, 1, 1, -1 });
        Assert.assertTrue(Tests.equals(v, new double[] {
                //
                1.0, 0.0, //
                0.19876611034641298, 0.3095598756531122, //
                1.4686939399158851, -2.2873552871788423, //
                1.0, 0.0, //
                0.19876611034641298, 0.3095598756531122, //
                1.4686939399158851, -2.2873552871788423 }));

        kernel.cuOp(ArrayKernel.CU_CONJ, Double.NaN, Double.NaN, //
                v = new double[] { 1, 2, -1, 1, 1, -1, 1, 2, -1, 1, 1, -1 });
        Assert.assertTrue(Tests.equals(v, new double[] { 1, -2, -1, -1, 1, 1, 1, -2, -1, -1, 1, 1 }));

        kernel.cuOp(ArrayKernel.CU_COS, Double.NaN, Double.NaN, //
                v = new double[] { 0, 0, 1, 0, 0, 1, 1, 1 });
        Assert.assertTrue(Tests.equals(v, new double[] {
                //
                1.0, 0.0, //
                0.5403023058681398, 0.0, //
                1.543080634815244, 0.0, //
                0.8337300251311491, -0.9888977057628653 }));

        kernel.cuOp(ArrayKernel.CU_SIN, Double.NaN, Double.NaN, //
                v = new double[] { 0, 0, 1, 0, 0, 1, 1, 1 });
        Assert.assertTrue(Tests.equals(v, new double[] {
                //
                0.0, 0.0, //
                0.8414709848078965, 0.0, //
                0.0, 1.1752011936438016, //
                1.2984575814159776, 0.6349639147847362 }));

        kernel.cuOp(ArrayKernel.CU_FILL, -1, 1, v = new double[] { 1, 0, 0, 2, 3, 0, 0, 4 });
        Assert.assertTrue(Tests.equals(v, new double[] { -1, 1, -1, 1, -1, 1, -1, 1 }));

        kernel.cuOp(ArrayKernel.CU_SHUFFLE, Double.NaN, Double.NaN, //
                v = new double[] { 1, 1, 3, 3, 2, 2, 4, 4 });
        Arrays.sort(v);
        Assert.assertTrue(Tests.equals(v, new double[] { 1, 1, 2, 2, 3, 3, 4, 4 }));

        kernel.cuOp(ArrayKernel.CU_RND, Math.E, Math.PI, v = new double[nSamples << 1]);

        double[] vRe = new double[nSamples];
        double[] vIm = new double[nSamples];

        for (int i = 0, offset = 0; i < nSamples; i++, offset += 2) {

            vRe[i] = v[offset];
            vIm[i] = v[offset + 1];
        }

        Arrays.sort(vRe);
        Arrays.sort(vIm);

        for (int i = 0, binOffset = 0; i < nBins; i++, binOffset += samplesPerBin) {

            double accRe = 0.0;
            double accIm = 0.0;

            for (int j = 0; j < samplesPerBin; j++) {

                accRe += vRe[binOffset + j];
                accIm += vIm[binOffset + j];
            }

            Assert.assertTrue(Math.abs(accRe / (Math.E * samplesPerBin) - (i + 0.5) / nBins) < 0.005);
            Assert.assertTrue(Math.abs(accIm / (Math.PI * samplesPerBin) - (i + 0.5) / nBins) < 0.005);
        }

        //

        int[] iv;

        kernel.iuOp(ArrayKernel.IU_ADD, 2, iv = new int[] { 1, 2, 3, 4, 5, 6, 7, 8 });
        Assert.assertTrue(Arrays.equals(iv, new int[] { 3, 4, 5, 6, 7, 8, 9, 10 }));

        kernel.iuOp(ArrayKernel.IU_MUL, 2, iv = new int[] { 1, 2, 3, 4, 5, 6, 7, 8 });
        Assert.assertTrue(Arrays.equals(iv, new int[] { 2, 4, 6, 8, 10, 12, 14, 16 }));

        kernel.iuOp(ArrayKernel.IU_FILL, 2, iv = new int[] { 1, 2, 3, 4, 5, 6, 7, 8 });
        Assert.assertTrue(Arrays.equals(iv, new int[] { 2, 2, 2, 2, 2, 2, 2, 2 }));

        kernel.iuOp(ArrayKernel.IU_SHUFFLE, Integer.MIN_VALUE, //
                iv = new int[] { 1, 2, 3, 4, 5, 6, 7, 8 });
        Arrays.sort(iv);
        Assert.assertTrue(Arrays.equals(iv, new int[] { 1, 2, 3, 4, 5, 6, 7, 8 }));

        //

        kernel.eOp(ArrayKernel.RE_ADD, //
                new double[] { 1, -1, -1 }, //
                new double[] { -2, 1, 2 }, v = new double[3], false);
        Assert.assertTrue(Tests.equals(v, new double[] { -1, 0, 1 }));

        kernel.eOp(ArrayKernel.RE_SUB, //
                new double[] { 1, -1, -1 }, //
                new double[] { -2, 1, 2 }, v = new double[3], false);
        Assert.assertTrue(Tests.equals(v, new double[] { 3, -2, -3 }));

        kernel.eOp(ArrayKernel.RE_MUL, //
                new double[] { 1, -1, -1 }, //
                new double[] { -2, 1, 2 }, v = new double[3], false);
        Assert.assertTrue(Tests.equals(v, new double[] { -2, -1, -2 }));

        kernel.eOp(ArrayKernel.RE_DIV, //
                new double[] { 1, -1, -1 }, //
                new double[] { -2, 1, 2 }, v = new double[3], false);
        Assert.assertTrue(Tests.equals(v, new double[] { -0.5, -1.0, -0.5 }));

        kernel.eOp(ArrayKernel.RE_MAX, //
                new double[] { 1, -1, -1 }, //
                new double[] { -2, 1, 2 }, v = new double[3], false);
        Assert.assertTrue(Tests.equals(v, new double[] { 1, 1, 2 }));

        kernel.eOp(ArrayKernel.RE_MIN, //
                new double[] { 1, -1, -1 }, //
                new double[] { -2, 1, 2 }, v = new double[3], false);
        Assert.assertTrue(Tests.equals(v, new double[] { -2, -1, -1 }));

        //

        kernel.eOp(ArrayKernel.IE_ADD, //
                new int[] { 1, -1, -1 }, //
                new int[] { -2, 1, 2 }, iv = new int[3], false);
        Assert.assertTrue(Arrays.equals(iv, new int[] { -1, 0, 1 }));

        kernel.eOp(ArrayKernel.IE_SUB, //
                new int[] { 1, -1, -1 }, //
                new int[] { -2, 1, 2 }, iv = new int[3], false);
        Assert.assertTrue(Arrays.equals(iv, new int[] { 3, -2, -3 }));

        kernel.eOp(ArrayKernel.IE_MUL, //
                new int[] { 1, -1, -1 }, //
                new int[] { -2, 1, 2 }, iv = new int[3], false);
        Assert.assertTrue(Arrays.equals(iv, new int[] { -2, -1, -2 }));

        kernel.eOp(ArrayKernel.IE_MAX, //
                new int[] { 1, -1, -1 }, //
                new int[] { -2, 1, 2 }, iv = new int[3], false);
        Assert.assertTrue(Arrays.equals(iv, new int[] { 1, 1, 2 }));

        kernel.eOp(ArrayKernel.IE_MIN, //
                new int[] { 1, -1, -1 }, //
                new int[] { -2, 1, 2 }, iv = new int[3], false);
        Assert.assertTrue(Arrays.equals(iv, new int[] { -2, -1, -1 }));

        //

        kernel.eOp(ArrayKernel.CE_ADD, //
                new double[] { 1, -1, -1, 1, 0, 1, 1, -1, -1, 1, 0, 1 }, //
                new double[] { -2, 1, 2, -1, 1, 0, -2, 1, 2, -1, 1, 0 }, v = new double[12], true);
        Assert.assertTrue(Tests.equals(v, new double[] { -1, 0, 1, 0, 1, 1, -1, 0, 1, 0, 1, 1 }));

        kernel.eOp(ArrayKernel.CE_SUB, //
                new double[] { 1, -1, -1, 1, 0, 1, 1, -1, -1, 1, 0, 1 }, //
                new double[] { -2, 1, 2, -1, 1, 0, -2, 1, 2, -1, 1, 0 }, v = new double[12], true);
        Assert.assertTrue(Tests.equals(v, new double[] { 3, -2, -3, 2, -1, 1, 3, -2, -3, 2, -1, 1 }));

        kernel.eOp(ArrayKernel.CE_MUL, //
                new double[] { 1, -1, -1, 1, 0, 1, 1, -1, -1, 1, 0, 1 }, //
                new double[] { -2, 1, 2, -1, 1, 0, -2, 1, 2, -1, 1, 0 }, v = new double[12], true);
        Assert.assertTrue(Tests.equals(v, new double[] { -1, 3, -1, 3, 0, 1, -1, 3, -1, 3, 0, 1 }));

        kernel.eOp(ArrayKernel.CE_DIV, //
                new double[] { 1, -1, -1, 1, 0, 1, 1, -1, -1, 1, 0, 1 }, //
                new double[] { -2, 1, 2, -1, 1, 0, -2, 1, 2, -1, 1, 0 }, v = new double[12], true);
        Assert.assertTrue(Tests.equals(v, new double[] { -0.6, 0.2, -0.6, 0.2, 0, 1, -0.6, 0.2, -0.6, 0.2, 0, 1 }));

        //

        kernel.convert(ArrayKernel.C_TO_R_RE, //
                new double[] { 0.0, 1.0, 1.0, -1.0, 0.5, -0.25 }, true, v = new double[3], false);
        Assert.assertTrue(Tests.equals(v, new double[] { 0.0, 1.0, 0.5 }));

        kernel.convert(ArrayKernel.C_TO_R_IM, //
                new double[] { 0.0, 1.0, 1.0, -1.0, 0.5, -0.25 }, true, v = new double[3], false);
        Assert.assertTrue(Tests.equals(v, new double[] { 1.0, -1.0, -0.25 }));

        kernel.convert(ArrayKernel.C_TO_R_ABS, //
                new double[] { 0.0, 1.0, 1.0, -1.0, 0.5, -0.25 }, true, v = new double[3], false);
        Assert.assertTrue(Tests.equals(v, new double[] { 1.0, Math.sqrt(2), Math.sqrt(1.0 / 4.0 + 1.0 / 16.0) }));

        //

        kernel.convert(ArrayKernel.R_TO_C_RE, new double[] { 1, 2, 3, 4, 5, 6 }, false, v = new double[12], true);
        Assert.assertTrue(Tests.equals(v, new double[] { 1, 0, 2, 0, 3, 0, 4, 0, 5, 0, 6, 0 }));

        kernel.convert(ArrayKernel.R_TO_C_IM, new double[] { 1, 2, 3, 4, 5, 6 }, false, v = new double[12], true);
        Assert.assertTrue(Tests.equals(v, new double[] { 0, 1, 0, 2, 0, 3, 0, 4, 0, 5, 0, 6 }));

        //

        kernel.convert(ArrayKernel.I_TO_R, new int[] { 1, 2, 3, 4, 5, 6 }, false, v = new double[6], false);
        Assert.assertTrue(Tests.equals(v, new double[] { 1, 2, 3, 4, 5, 6 }));
    }
}

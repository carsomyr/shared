/**
 * <p>
 * Copyright (c) 2008 Roy Liu<br>
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

package shared.array;

import static shared.util.Control.LineSeparator;

import java.lang.reflect.Array;
import java.util.Formatter;

import shared.array.Array.IndexingOrder;
import shared.array.kernel.ModalArrayIOKernel;
import shared.array.kernel.ModalArrayKernel;
import shared.fft.ModalFFTService;
import shared.util.Arithmetic;
import shared.util.Control;

/**
 * A static utility class for common {@link Array} operations.
 * 
 * @author Roy Liu
 */
public class ArrayBase {

    /**
     * The array operations kernel.
     */
    final public static ModalArrayKernel OpKernel = new ModalArrayKernel();

    /**
     * The array I/O kernel.
     */
    final public static ModalArrayIOKernel IOKernel = new ModalArrayIOKernel();

    /**
     * A global service providing access to FFT operations.
     */
    final public static ModalFFTService FFTService = new ModalFFTService();

    /**
     * The default storage order shall be {@link IndexingOrder#FAR}.
     */
    final public static IndexingOrder DEFAULT_ORDER = IndexingOrder.FAR;

    /**
     * The field width to use when printing.
     */
    public static int FIELD_WIDTH = 8;

    /**
     * The field precision to use when printing.
     */
    public static int FIELD_PRECISION = 2;

    /**
     * Canonicalizes the alternate slicing specification.
     */
    final public static int[] canonicalizeSlices(int[][] srcSlices, int[] srcDims, int[][] dstSlices, int[] dstDims) {

        int nDims = srcSlices.length;

        Control.checkTrue(nDims == dstSlices.length //
                && nDims == srcDims.length //
                && nDims == dstDims.length, //
                "Dimensionality mismatch");

        int nSlices = 0;

        for (int dim = 0; dim < nDims; dim++) {
            nSlices += Control.checkEquals(srcSlices[dim].length, dstSlices[dim].length, //
                    "Dimension mismatch");
        }

        int[] slices = new int[3 * nSlices];

        for (int dim = 0, offset = 0; dim < nDims; dim++) {

            int[] srcSlice = srcSlices[dim];
            int[] dstSlice = dstSlices[dim];

            for (int j = 0, m = srcSlice.length; j < m; j++, offset += 3) {

                slices[offset] = srcSlice[j];
                slices[offset + 1] = dstSlice[j];
                slices[offset + 2] = dim;
            }
        }

        return slices;
    }

    /**
     * Canonicalizes the alternate slicing specification.
     */
    final public static int[] canonicalizeSlices(int[] srcDims, int[] dstDims, int[][] dstSlices) {

        int nDims = dstSlices.length;

        Control.checkTrue(nDims == srcDims.length //
                && nDims == dstDims.length, //
                "Dimensionality mismatch");

        int nSlices = 0;

        for (int dim = 0; dim < nDims; dim++) {
            nSlices += Control.checkEquals(srcDims[dim], dstSlices[dim].length, //
                    "Dimension mismatch");
        }

        int[] slices = new int[3 * nSlices];

        for (int dim = 0, offset = 0; dim < nDims; dim++) {

            int[] dstSlice = dstSlices[dim];

            for (int j = 0, m = dstSlice.length; j < m; j++, offset += 3) {

                slices[offset] = j;
                slices[offset + 1] = dstSlice[j];
                slices[offset + 2] = dim;
            }
        }

        return slices;
    }

    /**
     * Canonicalizes the alternate slicing specification.
     */
    final public static int[] canonicalizeSlices(int nSlices, int[] srcDims, int[][] srcSlices) {

        int[] slices = new int[3 * nSlices];

        for (int dim = 0, offset = 0, nDims = srcDims.length; dim < nDims; dim++) {

            int[] srcSlice = srcSlices[dim];

            for (int j = 0, m = srcSlice.length; j < m; j++, offset += 3) {

                slices[offset] = srcSlice[j];
                slices[offset + 1] = j;
                slices[offset + 2] = dim;
            }
        }

        return slices;
    }

    /**
     * Creates a slicing specification for {@link shared.array.Array#reverse(int...)}.
     */
    final public static int[] createReverseSlices(int[] srcDims, int[] opDims) {

        int nDims = srcDims.length;
        int nIndices = opDims.length;

        int nSlices = Arithmetic.sum(srcDims);
        int[] slices = new int[3 * nSlices];

        boolean[] sentinel = new boolean[nDims];

        for (int i = 0; i < nIndices; i++) {
            sentinel[opDims[i]] = true;
        }

        for (int dim = 0, offset = 0; dim < nDims; dim++) {

            int dimSize = srcDims[dim];

            if (sentinel[dim]) {

                for (int j = 0, k = dimSize - 1; j < dimSize; j++, k--, offset += 3) {

                    slices[offset] = j;
                    slices[offset + 1] = k;
                    slices[offset + 2] = dim;
                }

            } else {

                for (int j = 0; j < dimSize; j++, offset += 3) {

                    slices[offset] = j;
                    slices[offset + 1] = j;
                    slices[offset + 2] = dim;
                }
            }
        }

        return slices;
    }

    /**
     * Sets the formatting parameters {@link #FIELD_WIDTH} and {@link #FIELD_PRECISION}.
     * 
     * @param width
     *            the number width.
     * @param precision
     *            the number precision.
     */
    final public static void format(int width, int precision) {

        Control.checkTrue(width >= precision + 4 && precision >= 1, //
                "Invalid formatting parameters");

        FIELD_WIDTH = width;
        FIELD_PRECISION = precision;
    }

    /**
     * Formats a two-dimensional slice of an array for display.
     * 
     * @param f
     *            the target {@link Formatter}.
     * @param format
     *            the formatting string.
     * @param values
     *            the values.
     * @param indices
     *            the physical indices.
     * @param offset
     *            the physical index offset.
     * @param nRows
     *            the number of rows in the slice.
     * @param nCols
     *            the number of columns in the slice.
     * @param isComplex
     *            whether the values are complex.
     */
    final public static void formatSlice(Formatter f, String format, //
            Object values, int[] indices, //
            int offset, int nRows, int nCols, boolean isComplex) {

        if (!isComplex) {

            for (int j = 0, k = 0; j < nRows; j++) {

                for (int i = 0; i < nCols; i++, k++) {
                    f.format(format, Array.get(values, indices[offset + k]));
                }

                f.format(LineSeparator);
            }

        } else {

            for (int j = 0, k = 0; j < nRows; j++) {

                for (int i = 0; i < nCols; i++, k += 2) {
                    f.format(format, //
                            Array.get(values, indices[offset + k]), //
                            Array.get(values, indices[offset + k + 1]));
                }

                f.format(LineSeparator);
            }
        }
    }

    /**
     * Formats an empty array message.
     * 
     * @param f
     *            the target {@link Formatter}.
     * @param dims
     *            the dimensions.
     */
    final public static void formatEmptyArray(Formatter f, int[] dims) {

        int nDims = dims.length;

        f.format(LineSeparator).format("[empty (");

        for (int i = 0, n = nDims - 1; i < n; i++) {
            f.format("%d, ", dims[i]);
        }

        f.format("%d)]", dims[nDims - 1]).format(LineSeparator);
    }

    /**
     * Formats a potential rescale message if the given exponent is too large or too small.
     * 
     * @param f
     *            the target {@link Formatter}.
     * @param exponent
     *            the exponent.
     * @param values
     *            the values.
     * @return the potentially rescaled values.
     */
    final public static double[] formatRescale(Formatter f, int exponent, double[] values) {

        if (FIELD_WIDTH < exponent + FIELD_PRECISION + 4 || exponent < 0) {

            f.format("%s[rescale 10^%d]%s", //
                    LineSeparator, exponent, LineSeparator);

            double scale = Math.pow(10.0, -exponent);

            double[] res = values.clone();

            for (int i = 0, n = res.length; i < n; i++) {
                res[i] *= scale;
            }

            return res;

        } else {

            return values;
        }
    }

    /**
     * Formats a sparse array for display.
     * 
     * @param f
     *            the target {@link Formatter}.
     * @param valueFormat
     *            the formatting string for values.
     * @param indexFormat
     *            the formatting string for logical indices.
     * @param values
     *            the values.
     * @param indices
     *            the physical indices.
     * @param strides
     *            the strides.
     */
    final public static void formatSparseArray(Formatter f, String valueFormat, String indexFormat, //
            Object values, int[] indices, int[] strides) {

        int nDims = strides.length;

        for (int i = 0, n = Control.checkEquals(Array.getLength(values), indices.length); i < n; i++) {

            int physical = indices[i];

            f.format("(");

            for (int j = 0, m = nDims - 1; j < m; j++) {

                f.format(indexFormat, physical / strides[j]);
                f.format(",");

                physical %= strides[j];
            }

            f.format(indexFormat, physical / strides[nDims - 1]);
            f.format(")");
            f.format(valueFormat, Array.get(values, i));
            f.format(LineSeparator);
        }
    }

    /**
     * Computes the logical index from the given physical index and strides.
     * 
     * @param physical
     *            the physical index.
     * @param strides
     *            the strides.
     * @param logical
     *            the logical index.
     * @return the logical index.
     */
    final public static int[] logical(int physical, int[] strides, int[] logical) {

        for (int dim = 0, nDims = Control.checkEquals(strides.length, logical.length); dim < nDims; dim++) {

            logical[dim] = physical / strides[dim];
            physical %= strides[dim];
        }

        return logical;
    }

    /**
     * Infers dimensions from the backing array length if the number of declared dimensions is {@code 0}.
     * 
     * @param dims
     *            the declared dimensions.
     * @param len
     *            the array length.
     * @param isComplex
     *            whether the array contains complex values.
     * @return the inferred dimensions.
     */
    final public static int[] inferDimensions(int[] dims, int len, boolean isComplex) {
        return (dims.length > 0) ? dims.clone() : (isComplex ? new int[] {
                Control.checkEquals(len, (len >>> 1) << 1) >>> 1, 2 } : new int[] { len });
    }

    // Dummy constructor.
    ArrayBase() {
    }
}

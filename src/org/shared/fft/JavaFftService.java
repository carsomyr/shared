/**
 * <p>
 * Copyright (c) 2008 The Regents of the University of California<br>
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

package org.shared.fft;

import java.util.Arrays;

import org.shared.array.ComplexArray;
import org.shared.array.RealArray;
import org.shared.util.Arithmetic;
import org.shared.util.Control;

/**
 * An {@link FftService} implementation in pure Java.
 * 
 * @apiviz.uses org.shared.fft.FftOps
 * @author Roy Liu
 */
public class JavaFftService implements FftService {

    /**
     * Default constructor.
     */
    public JavaFftService() {
    }

    @Override
    public void rfft(int[] dims, double[] in, double[] out) {

        ComplexArray proxyIn = new RealArray(in, dims).tocRe();
        ComplexArray proxyOut = proxyIn.clone();

        FftOps.fft(+1, dims, proxyIn.values(), proxyOut.values());

        proxyOut = fullToReduced(proxyOut);

        System.arraycopy(proxyOut.values(), 0, out, 0, //
                Control.checkEquals(proxyOut.values().length, out.length));
    }

    @Override
    public void rifft(int[] dims, double[] in, double[] out) {

        int nDims = dims.length;
        int[] subdims = Arrays.copyOf(dims, nDims + 1);

        subdims[nDims - 1] = (subdims[nDims - 1] >>> 1) + 1;
        subdims[nDims] = 2;

        ComplexArray proxyIn = reducedToFull(new ComplexArray(in, subdims), dims);
        ComplexArray proxyOut = proxyIn.clone();

        FftOps.fft(-1, dims, proxyIn.values(), proxyOut.values());

        RealArray realProxyOut = proxyOut.torRe();

        System.arraycopy(realProxyOut.values(), 0, out, 0, //
                Control.checkEquals(realProxyOut.values().length, out.length));
    }

    @Override
    public void fft(int[] dims, double[] in, double[] out) {
        FftOps.fft(+1, dims, in, out);
    }

    @Override
    public void ifft(int[] dims, double[] in, double[] out) {
        FftOps.fft(-1, dims, in, out);
    }

    @Override
    public void setHint(String name, String value) {
        throw new IllegalArgumentException("Unknown hint");
    }

    @Override
    public String getHint(String name) {
        throw new IllegalArgumentException("Unknown hint");
    }

    /**
     * Derives a full {@link ComplexArray} from a reduced, half-complex {@link ComplexArray}.
     * 
     * @param reduced
     *            the reduced array.
     * @param logicalDims
     *            the logical {@link RealArray} dimensions.
     * @return the full array.
     */
    public ComplexArray reducedToFull(ComplexArray reduced, int[] logicalDims) {

        int[] reducedDims = reduced.dims();
        int nDims = reducedDims.length;

        int[] fullDims = Arrays.copyOf(logicalDims, nDims);
        fullDims[nDims - 1] = 2;

        ComplexArray full = new ComplexArray(fullDims);

        //

        int[] mapBounds = new int[3 * nDims];

        for (int dim = 0; dim < nDims; dim++) {

            mapBounds[3 * dim] = 0;
            mapBounds[3 * dim + 1] = 0;
            mapBounds[3 * dim + 2] = reducedDims[dim];
        }

        reduced.map(full, mapBounds);

        //

        int[] slices = new int[3 * Arithmetic.sum(reducedDims)];
        int[] sliceOffsets = new int[nDims];

        for (int dim = 0, offset = 0; dim < nDims; dim++) {

            sliceOffsets[dim] = offset;

            for (int j = 0, m = reducedDims[dim]; j < m; j++, offset += 3) {

                slices[offset] = j;
                slices[offset + 1] = j;
                slices[offset + 2] = dim;
            }
        }

        for (int dim = nDims - 2; dim >= 0; dim--) {

            int sliceOffset = sliceOffsets[dim];

            for (int j = 1, m = reducedDims[dim], offset = sliceOffset + 3; j < m; j++, offset += 3) {

                slices[offset] = j;
                slices[offset + 1] = m - j;
                slices[offset + 2] = dim;
            }

            reduced = reduced.splice(new ComplexArray(reducedDims), slices);

            for (int j = 1, m = reducedDims[dim], offset = sliceOffset + 3; j < m; j++, offset += 3) {

                slices[offset] = j;
                slices[offset + 1] = j;
                slices[offset + 2] = dim;
            }
        }

        //

        mapBounds[3 * (nDims - 2)] = 2 * reducedDims[nDims - 2] - fullDims[nDims - 2];
        mapBounds[3 * (nDims - 2) + 1] = reducedDims[nDims - 2];
        mapBounds[3 * (nDims - 2) + 2] = fullDims[nDims - 2] - reducedDims[nDims - 2];

        return reduced.uConj().map(full, mapBounds);
    }

    /**
     * Derives a reduced, half-complex {@link ComplexArray} from a full {@link ComplexArray}.
     * 
     * @param full
     *            the full array.
     * @return the reduced array.
     */
    public ComplexArray fullToReduced(ComplexArray full) {

        int[] fullDims = full.dims();
        int nDims = fullDims.length;

        int[] subbounds = new int[nDims << 1];

        for (int dim = 0, offset = 0; dim < nDims; dim++, offset += 2) {
            subbounds[offset + 1] = fullDims[dim];
        }

        subbounds[((nDims - 2) << 1) + 1] >>>= 1;
        subbounds[((nDims - 2) << 1) + 1]++;

        return full.subarray(subbounds);
    }
}

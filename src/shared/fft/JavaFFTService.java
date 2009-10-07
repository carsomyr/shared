/**
 * This file is part of the Shared Scientific Toolbox in Java ("this library"). <br />
 * <br />
 * Copyright (C) 2008 Roy Liu, The Regents of the University of California <br />
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

package shared.fft;

import java.util.Arrays;

import shared.array.ComplexArray;
import shared.array.RealArray;
import shared.util.Arithmetic;
import shared.util.Control;

/**
 * An {@link FFTService} implementation in pure Java.
 * 
 * @apiviz.uses shared.fft.FFTOps
 * @author Roy Liu
 */
public class JavaFFTService implements FFTService {

    /**
     * Default constructor.
     */
    public JavaFFTService() {
    }

    public void rfft(int[] dims, double[] in, double[] out) {

        ComplexArray proxyIn = new RealArray(in, dims).tocRe();
        ComplexArray proxyOut = proxyIn.clone();

        FFTOps.fft(+1, dims, proxyIn.values(), proxyOut.values());

        proxyOut = fullToReduced(proxyOut);

        System.arraycopy(proxyOut.values(), 0, out, 0, //
                Control.checkEquals(proxyOut.values().length, out.length));
    }

    public void rifft(int[] dims, double[] in, double[] out) {

        int ndims = dims.length;
        int[] subdims = Arrays.copyOf(dims, ndims + 1);

        subdims[ndims - 1] = (subdims[ndims - 1] >>> 1) + 1;
        subdims[ndims] = 2;

        ComplexArray proxyIn = reducedToFull(new ComplexArray(in, subdims), dims);
        ComplexArray proxyOut = proxyIn.clone();

        FFTOps.fft(-1, dims, proxyIn.values(), proxyOut.values());

        RealArray realProxyOut = proxyOut.torRe();

        System.arraycopy(realProxyOut.values(), 0, out, 0, //
                Control.checkEquals(realProxyOut.values().length, out.length));
    }

    public void fft(int[] dims, double[] in, double[] out) {
        FFTOps.fft(+1, dims, in, out);
    }

    public void ifft(int[] dims, double[] in, double[] out) {
        FFTOps.fft(-1, dims, in, out);
    }

    public void setHint(String name, String value) {
        throw new IllegalArgumentException("Unknown hint");
    }

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

        int[] reducedDims = reduced.dimensions();
        int ndims = reducedDims.length;

        int[] fullDims = Arrays.copyOf(logicalDims, ndims);
        fullDims[ndims - 1] = 2;

        ComplexArray full = new ComplexArray(fullDims);

        //

        int[] mapBounds = new int[3 * ndims];

        for (int dim = 0; dim < ndims; dim++) {

            mapBounds[3 * dim] = 0;
            mapBounds[3 * dim + 1] = 0;
            mapBounds[3 * dim + 2] = reducedDims[dim];
        }

        reduced.map(full, mapBounds);

        //

        int[] slices = new int[3 * Arithmetic.sum(reducedDims)];
        int[] sliceOffsets = new int[ndims];

        for (int dim = 0, offset = 0; dim < ndims; dim++) {

            sliceOffsets[dim] = offset;

            for (int j = 0, m = reducedDims[dim]; j < m; j++, offset += 3) {

                slices[offset] = j;
                slices[offset + 1] = j;
                slices[offset + 2] = dim;
            }
        }

        for (int dim = ndims - 2; dim >= 0; dim--) {

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

        mapBounds[3 * (ndims - 2)] = 2 * reducedDims[ndims - 2] - fullDims[ndims - 2];
        mapBounds[3 * (ndims - 2) + 1] = reducedDims[ndims - 2];
        mapBounds[3 * (ndims - 2) + 2] = fullDims[ndims - 2] - reducedDims[ndims - 2];

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

        int[] fullDims = full.dimensions();
        int ndims = fullDims.length;

        int[] subbounds = new int[ndims << 1];

        for (int dim = 0, offset = 0; dim < ndims; dim++, offset += 2) {
            subbounds[offset + 1] = fullDims[dim];
        }

        subbounds[((ndims - 2) << 1) + 1] >>>= 1;
        subbounds[((ndims - 2) << 1) + 1]++;

        return full.subarray(subbounds);
    }
}

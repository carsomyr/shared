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

package org.shared.image.filter;

import org.shared.array.RealArray;
import org.shared.fft.Cacheable;

/**
 * An implementation of LoG filters.
 * 
 * @apiviz.uses org.shared.image.filter.Filters
 * @author Roy Liu
 */
public class LaplacianOfGaussian extends RealArray implements Cacheable {

    final double scale;
    final int hashCode, supportRadius;

    @Override
    public String toString() {
        return String.format("LoG[%d, %4.2f]", //
                this.supportRadius, this.scale);
    }

    /**
     * Fulfills the {@link #equals(Object)} contract.
     */
    @Override
    public boolean equals(Object o) {

        if (!(o instanceof LaplacianOfGaussian)) {
            return false;
        }

        LaplacianOfGaussian g = (LaplacianOfGaussian) o;
        return this.supportRadius == g.supportRadius && this.scale == g.scale;
    }

    /**
     * Fulfills the {@link #hashCode()} contract.
     */
    @Override
    public int hashCode() {
        return this.hashCode;
    }

    /**
     * Default constructor.
     * 
     * @param supportRadius
     *            the support radius.
     * @param scale
     *            the scale.
     */
    public LaplacianOfGaussian(int supportRadius, double scale) {
        super(2 * supportRadius + 1, 2 * supportRadius + 1);

        this.supportRadius = supportRadius;
        this.scale = scale;

        this.hashCode = new Integer(supportRadius).hashCode() ^ new Double(scale).hashCode();

        double sigma = (scale * supportRadius) / 3.0d;

        RealArray ptsMatrix = Filters.createPointSupport(supportRadius);

        RealArray ptsY = ptsMatrix.subarray(0, 1, 0, ptsMatrix.size(1)) //
                .reshape(2 * supportRadius + 1, 2 * supportRadius + 1);
        RealArray ptsX = ptsMatrix.subarray(1, 2, 0, ptsMatrix.size(1)) //
                .reshape(2 * supportRadius + 1, 2 * supportRadius + 1);

        ptsX.clone().uSqr().eAdd(ptsY.clone().uSqr()) //
                .uAdd(-2.0 * sigma * sigma) //
                .uMul(1.0 / (sigma * sigma * sigma * sigma)) //
                .eMul(new DerivativeOfGaussian(supportRadius, 0.0, 1.0, 0)) //
                .map(this, 0, 0, size(0), 0, 0, size(1));

        uAdd(-aMean());
    }
}

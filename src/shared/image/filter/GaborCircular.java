/**
 * <p>
 * Copyright (c) 2007 The Regents of the University of California<br>
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

package shared.image.filter;

import shared.array.ComplexArray;
import shared.array.RealArray;
import shared.fft.Cacheable;

/**
 * An implementation of circular, complex-valued Gabor filters.
 * 
 * @apiviz.uses shared.image.filter.Filters
 * @author Roy Liu
 */
public class GaborCircular extends ComplexArray implements Cacheable {

    final double scale, frequency;
    final int hashCode, supportRadius;

    @Override
    public String toString() {
        return String.format("GaborCircular[%d, %4.2f, %4.2f]", //
                this.supportRadius, this.scale, this.frequency);
    }

    /**
     * Fulfills the {@link #equals(Object)} contract.
     */
    @Override
    public boolean equals(Object o) {

        if (!(o instanceof GaborCircular)) {
            return false;
        }

        GaborCircular g = (GaborCircular) o;
        return this.supportRadius == g.supportRadius //
                && this.scale == g.scale //
                && this.frequency == g.frequency;
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
     * @param frequency
     *            the frequency.
     */
    public GaborCircular(int supportRadius, double scale, double frequency) {
        super(2 * supportRadius + 1, 2 * supportRadius + 1, 2);

        this.supportRadius = supportRadius;
        this.scale = scale;
        this.frequency = frequency;

        this.hashCode = new Integer(supportRadius).hashCode() //
                ^ new Double(scale).hashCode() //
                ^ new Double(frequency).hashCode();

        RealArray ptsMatrix = Filters.createPointSupport(supportRadius);
        RealArray ptsY = ptsMatrix.subarray(0, 1, 0, ptsMatrix.size(1)) //
                .reshape(2 * supportRadius + 1, 2 * supportRadius + 1);
        RealArray ptsX = ptsMatrix.subarray(1, 2, 0, ptsMatrix.size(1)) //
                .reshape(2 * supportRadius + 1, 2 * supportRadius + 1);

        final double k = scale * supportRadius;

        final double a = -4.0d / (k * k);

        RealArray ptsRe = ((ptsX.clone().uPow(2.0)).eAdd(ptsY.clone().uPow(2.0))).uMul(a);

        final double b = 2.0d * Math.PI * frequency / k;

        RealArray ptsIm = ((ptsX.clone().uPow(2.0)).eAdd(ptsY.clone().uPow(2.0))).uPow(0.5).uMul(b);

        (ptsRe.tocRe()).eAdd(ptsIm.tocIm()).uExp().map(this, 0, 0, size(0), 0, 0, size(1), 0, 0, 2);
    }
}

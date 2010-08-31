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

import shared.array.RealArray;
import shared.fft.Cacheable;

/**
 * An implementation of DooG filters up to {@code 0}th and {@code 1}st derivatives.
 * 
 * @apiviz.uses shared.image.filter.Filters
 * @author Roy Liu
 */
public class DerivativeOfGaussian extends RealArray implements Cacheable {

    final double theta, scale;
    final int ord, hashCode, supportRadius;

    @Override
    public String toString() {
        return String.format("DooG[%d, %4.2f, %4.2f, %d]", //
                this.supportRadius, this.theta, this.scale, this.ord);
    }

    /**
     * Fulfills the {@link #equals(Object)} contract.
     */
    @Override
    public boolean equals(Object o) {

        if (!(o instanceof DerivativeOfGaussian)) {
            return false;
        }

        DerivativeOfGaussian g = (DerivativeOfGaussian) o;
        return this.supportRadius == g.supportRadius //
                && this.theta == g.theta //
                && this.scale == g.scale //
                && this.ord == g.ord;
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
     * @param theta
     *            the orientation.
     * @param scale
     *            the scale.
     * @param ord
     *            the order of the derivative.
     */
    public DerivativeOfGaussian(int supportRadius, double theta, double scale, int ord) {
        super(2 * supportRadius + 1, 2 * supportRadius + 1);

        this.supportRadius = supportRadius;
        this.theta = theta;
        this.scale = scale;
        this.ord = ord;

        this.hashCode = new Integer(supportRadius).hashCode() //
                ^ new Double(theta).hashCode() //
                ^ new Double(scale).hashCode() //
                ^ new Integer(ord).hashCode();

        double sigma = (scale * supportRadius) / 3.0d;

        RealArray ptsMatrix = Filters.createRotationMatrix(theta) //
                .mMul(Filters.createPointSupport(supportRadius));

        RealArray ptsY = ptsMatrix.subarray(0, 1, 0, ptsMatrix.size(1)) //
                .reshape(2 * supportRadius + 1, 2 * supportRadius + 1);
        RealArray ptsX = ptsMatrix.subarray(1, 2, 0, ptsMatrix.size(1)) //
                .reshape(2 * supportRadius + 1, 2 * supportRadius + 1);

        RealArray gX = derivative(sigma, ptsX, ord);
        RealArray gY = derivative(sigma, ptsY, 0);

        gX.eMul(gY).map(this, 0, 0, size(0), 0, 0, size(1));

        switch (ord) {

        case 0:
            uMul(1.0 / aSum());
            break;

        case 1:
            uAdd(-aMean());
            break;
        }
    }

    /**
     * Calculates a Gaussian derivative to the specified order.
     */
    final protected static RealArray derivative(double sigma, RealArray support, int order) {

        double variance = sigma * sigma;

        // Get a working copy.
        RealArray g = support.eMul(support) //
                .uMul(-1.0 / (2 * variance)).uExp() //
                .uMul(1.0 / Math.sqrt(2 * Math.PI * variance));

        switch (order) {

        case 0:
            return g;

        case 1:
            return g.eMul(support.clone().uMul(-1.0d / variance));

        default:
            throw new IllegalArgumentException("Order of derivative can only be zero or one");
        }
    }
}

/**
 * This file is part of the Shared Scientific Toolbox in Java ("this library"). <br />
 * <br />
 * Copyright (C) 2007 Roy Liu, The Regents of the University of California <br />
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
     * Fulfills the {@link Object#equals(Object)} contract.
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
     * Fulfills the {@link Object#hashCode()} contract.
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

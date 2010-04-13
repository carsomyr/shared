/**
 * This file is part of the Shared Scientific Toolbox in Java ("this library"). <br />
 * <br />
 * Copyright (C) 2008 The Regents of the University of California <br />
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
 * An implementation of LoG filters.
 * 
 * @apiviz.uses shared.image.filter.Filters
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
     * Fulfills the {@link Object#equals(Object)} contract.
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

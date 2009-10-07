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
     * Fulfills the {@link Object#equals(Object)} contract.
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

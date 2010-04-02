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
 * An implementation of oriented, complex-valued Gabor filters.
 * 
 * @apiviz.uses shared.image.filter.Filters
 * @author Roy Liu
 */
public class Gabor extends ComplexArray implements Cacheable {

    final double theta, scale, frequency, elongation;
    final int hashCode, supportRadius;

    @Override
    public String toString() {
        return String.format("Gabor[%d, %4.2f, %4.2f, %4.2f, %4.2f]", //
                this.supportRadius, this.theta, this.scale, this.frequency, this.elongation);
    }

    /**
     * Fulfills the {@link Object#equals(Object)} contract.
     */
    @Override
    public boolean equals(Object o) {

        if (!(o instanceof Gabor)) {
            return false;
        }

        Gabor g = (Gabor) o;
        return this.supportRadius == g.supportRadius //
                && this.theta == g.theta //
                && this.scale == g.scale //
                && this.frequency == g.frequency //
                && this.elongation == g.elongation;
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
     * @param frequency
     *            the frequency.
     * @param elongation
     *            the elongation.
     */
    public Gabor(int supportRadius, //
            double theta, double scale, //
            double frequency, double elongation) {
        super(2 * supportRadius + 1, 2 * supportRadius + 1, 2);

        this.supportRadius = supportRadius;
        this.elongation = elongation;
        this.theta = theta;
        this.scale = scale;
        this.frequency = frequency;

        this.hashCode = new Integer(supportRadius).hashCode() //
                ^ new Double(theta).hashCode() //
                ^ new Double(scale).hashCode() //
                ^ new Double(frequency).hashCode() //
                ^ new Double(elongation).hashCode();

        RealArray ptsMatrix = Filters.createRotationMatrix(theta) //
                .mMul(Filters.createPointSupport(supportRadius));

        RealArray ptsY = ptsMatrix.subarray(0, 1, 0, ptsMatrix.size(1)) //
                .reshape(2 * supportRadius + 1, 2 * supportRadius + 1);
        RealArray ptsX = ptsMatrix.subarray(1, 2, 0, ptsMatrix.size(1)) //
                .reshape(2 * supportRadius + 1, 2 * supportRadius + 1);

        final double k = scale * supportRadius / elongation;

        final double a = -4.0d / (k * k);

        RealArray ptsRe = ((ptsX.clone().uPow(2.0).uMul(1.0 / (elongation * elongation))) //
                .eAdd(ptsY.clone().uPow(2.0))) //
                .uMul(a);

        final double b = 2.0d * Math.PI * frequency / k;

        RealArray ptsIm = ptsY.clone().uMul(b);

        (ptsRe.tocRe()).eAdd(ptsIm.tocIm()).uExp().map(this, 0, 0, size(0), 0, 0, size(1), 0, 0, 2);
    }
}

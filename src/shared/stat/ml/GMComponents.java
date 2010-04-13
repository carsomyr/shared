/**
 * This file is part of the Shared Scientific Toolbox in Java ("this library"). <br />
 * <br />
 * Copyright (C) 2007 The Regents of the University of California <br />
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

package shared.stat.ml;

import shared.array.RealArray;

/**
 * A data structure containing information on Gaussian mixture components.
 * 
 * @author Roy Liu
 */
public class GMComponents {

    /**
     * The weights.
     */
    final public RealArray weights;

    /**
     * The centers.
     */
    final public RealArray centers;

    /**
     * The (diagonal) covariances.
     */
    final public RealArray covariances;

    /**
     * The likelihood. Updates every time one obtains a new likelihood from the <tt>M</tt> step.
     */
    public double likelihood;

    /**
     * The number of rounds so far.
     */
    public int nrounds;

    /**
     * Default constructor.
     * 
     * @param ncomponents
     *            the number of components.
     * @param ndims
     *            the dimensionality expected of the data.
     */
    public GMComponents(int ncomponents, int ndims) {

        this.weights = new RealArray(ncomponents, 1);
        this.centers = new RealArray(ncomponents, ndims);
        this.covariances = new RealArray(ncomponents, ndims);

        this.likelihood = Double.NaN;
        this.nrounds = -1;
    }

    /**
     * Regularizes the covariances by setting a floor on what they can be.
     * 
     * @param regularization
     *            the regularization hint.
     */
    public void regularize(double regularization) {

        double[] values = this.covariances.values();

        // Apply regularization.
        for (int i = 0, n = values.length; i < n; i++) {
            values[i] = Math.max(values[i], regularization);
        }
    }

    /**
     * Creates a human-readable representation of the mixture components.
     */
    @Override
    public String toString() {
        return String.format("%n" //
                + "centers =%n" //
                + "%s%n" //
                + "sqrt(covariances) =%n" //
                + "%s%n" //
                + "weights =%n" //
                + "%s%n" //
                + "likelihood = %4.4e%n%n" //
                + "nrounds = %d%n", //
                this.centers, this.covariances.clone().uSqrt(), this.weights, this.likelihood, this.nrounds);
    }
}

/**
 * <p>
 * Copyright (C) 2007 The Regents of the University of California<br />
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
    public int nRounds;

    /**
     * Default constructor.
     * 
     * @param nComps
     *            the number of components.
     * @param nDims
     *            the dimensionality expected of the data.
     */
    public GMComponents(int nComps, int nDims) {

        this.weights = new RealArray(nComps, 1);
        this.centers = new RealArray(nComps, nDims);
        this.covariances = new RealArray(nComps, nDims);

        this.likelihood = Double.NaN;
        this.nRounds = -1;
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
                + "n_rounds = %d%n", //
                this.centers, this.covariances.clone().uSqrt(), this.weights, this.likelihood, this.nRounds);
    }
}

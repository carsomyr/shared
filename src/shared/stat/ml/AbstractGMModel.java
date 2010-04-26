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
 * An abstract base class for Gaussian mixture models characterized by their initialization and update rules.
 * 
 * @apiviz.owns shared.stat.ml.GMComponents
 * @author Roy Liu
 */
abstract public class AbstractGMModel {

    /**
     * Default constructor.
     */
    protected AbstractGMModel() {
    }

    /**
     * Trains the model.
     * 
     * @param points
     *            the input.
     * @param ncomps
     *            the number of components.
     * @param regularization
     *            the regularization hint.
     * @param delta
     *            the change in likelihood required for termination.
     * @return the mixture components trained.
     */
    public GMComponents train(RealArray points, int ncomps, double regularization, double delta) {

        // Convert to a list of points.
        GMComponents gmc = initialize(points, ncomps, regularization);

        int nrounds = 0;

        for (double currentLL = -Double.MAX_VALUE / 4.0, previousLL = 2.0 * currentLL; //
        currentLL - previousLL > delta;) {

            previousLL = currentLL;
            update(gmc, computePosterior(gmc, points), points, regularization);
            currentLL = gmc.likelihood;

            nrounds++;

            gmc.nrounds = nrounds;
        }

        return gmc;
    }

    /**
     * Computes the posterior distribution and updates the mean likelihood of the points.
     * 
     * @param gmc
     *            the mixture components.
     * @param points
     *            the input.
     * @return the posterior distribution <tt>p(components | points)</tt>.
     */
    public RealArray computePosterior(GMComponents gmc, RealArray points) {

        int ncomps = gmc.weights.size(0);

        // The log of the weighted densities has dimensions (ncomps, npoints).
        RealArray logWeightedDensities = computeLogWeightedDensities(gmc, points);
        RealArray rowMax = logWeightedDensities.rMax(0);

        // Compute the normalized posterior for numerical stability.
        RealArray posterior = logWeightedDensities.eSub(rowMax.tile(ncomps, 1)).uExp();
        RealArray rowSum = posterior.rSum(0);

        // Normalize the posterior.
        posterior = posterior.lDiv(rowSum.tile(ncomps, 1));

        // Calculate the total log-likelihood.
        gmc.likelihood = (rowSum.clone().uAdd(1e-64).uLog()).lAdd(rowMax).aMean();

        return posterior;
    }

    /**
     * Performs the update rule.
     * 
     * @param gmc
     *            the mixture components.
     * @param posterior
     *            the posterior distribution <tt>p(components | points)</tt>.
     * @param points
     *            the input.
     * @param regularization
     *            the regularization hint.
     */
    abstract protected void update(GMComponents gmc, RealArray posterior, RealArray points, double regularization);

    /**
     * Initializes the mixture components.
     * 
     * @param points
     *            the input.
     * @param ncomps
     *            the number of components.
     * @param regularization
     *            the regularization hint.
     * @return the initial mixture components.
     */
    abstract protected GMComponents initialize(RealArray points, int ncomps, double regularization);

    /**
     * Computes the log of the weighted densities.
     * 
     * @param gmc
     *            the mixture components.
     * @param points
     *            the input.
     * @return the log of <tt>p(points | components)</tt>.
     */
    abstract protected RealArray computeLogWeightedDensities(GMComponents gmc, RealArray points);
}

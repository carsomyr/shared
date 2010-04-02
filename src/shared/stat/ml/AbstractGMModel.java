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

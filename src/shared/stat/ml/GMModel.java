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

import java.util.List;

import shared.array.RealArray;
import shared.util.Control;

/**
 * An implementation of {@link AbstractGMModel} using EM updates on GMM's with diagonal covariances. Inspired by <a
 * href="http://www.cs.ucsd.edu/~saul/">Lawrence Saul</a>'s EM code.
 * 
 * @apiviz.uses shared.stat.ml.KMeans
 * @author Roy Liu
 */
public class GMModel extends AbstractGMModel {

    /**
     * Default constructor.
     */
    public GMModel() {
    }

    @Override
    public GMComponents initialize(RealArray input, int ncomps, double regularization) {

        int ndims = input.size(1);

        GMComponents gmc = new GMComponents(ncomps, ndims);

        List<RealArray> clusters = KMeans.cluster(ncomps, input);

        // Assign the covariances to be the global covariance.
        RealArray globalSum = input.rSum(0);
        RealArray globalMean = input.rMean(0);

        (globalSum.clone().uSqr().uMul(1.0 / input.size(0))) //
                .lSub(globalMean.eMul(globalMean)).uAdd(regularization) //
                .tile(ncomps, 1) //
                .map(gmc.covariances, 0, 0, ncomps, 0, 0, ndims);

        // Assign the means to be the cluster means.
        for (int i = 0; i < ncomps; i++) {
            clusters.get(i).rMean(0).map(gmc.centers, 0, i, 1, 0, 0, ndims);
        }

        // Every component starts out as equally likely.
        gmc.weights.uFill(1.0 / ncomps);

        return gmc;
    }

    @Override
    public void update(GMComponents gmc, RealArray posterior, RealArray points, double regularization) {

        int ncomps = gmc.weights.size(0);
        int ndims = points.size(1);
        int npoints = Control.checkEquals(points.size(0), posterior.size(1));

        RealArray colSum = posterior.rSum(1);

        // The normalization has dimensions (ncomps, ndims).
        RealArray nrmp = colSum.tile(1, ndims).uAdd(1e-64);

        // Update the means.

        posterior.mMul(points).lDiv(nrmp) //
                .map(gmc.centers, 0, 0, ncomps, 0, 0, ndims);

        // Update the covariances.

        posterior.mMul(points.clone().uSqr()).lDiv(nrmp) //
                .lSub(gmc.centers.clone().uSqr()) //
                .map(gmc.covariances, 0, 0, ncomps, 0, 0, ndims);

        gmc.regularize(regularization);

        // Update the weights.

        colSum.map(gmc.weights, 0, 0, ncomps, 0, 0, 1).uMul(1.0 / npoints);
    }

    @Override
    public RealArray computeLogWeightedDensities(GMComponents gmc, RealArray points) {

        int npoints = points.size(0);

        RealArray iCov = gmc.covariances.clone().uInv(1.0);

        RealArray nrmTiled = //
        (gmc.centers.clone().uSqr().lMul(iCov)) //
                .lAdd(gmc.covariances.clone().uLog()) //
                .rSum(1).tile(1, npoints);

        RealArray pointsT = points.transpose(1, 0);

        RealArray logWeightsTiled = gmc.weights.tile(1, npoints).uAdd(1e-64).uLog();

        RealArray exponent = (iCov.mMul(pointsT.clone().uSqr())) //
                .lAdd(gmc.centers.eMul(iCov).mMul(pointsT).uMul(-2.0)) //
                .lAdd(nrmTiled).uMul(-0.5);

        return exponent.lAdd(logWeightsTiled);
    }
}

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
    public GMComponents initialize(RealArray input, int nComps, double regularization) {

        int nDims = input.size(1);

        GMComponents gmc = new GMComponents(nComps, nDims);

        List<RealArray> clusters = KMeans.cluster(nComps, input);

        // Assign the covariances to be the global covariance.
        RealArray globalSum = input.rSum(0);
        RealArray globalMean = input.rMean(0);

        (globalSum.clone().uSqr().uMul(1.0 / input.size(0))) //
                .lSub(globalMean.eMul(globalMean)).uAdd(regularization) //
                .tile(nComps, 1) //
                .map(gmc.covariances, 0, 0, nComps, 0, 0, nDims);

        // Assign the means to be the cluster means.
        for (int i = 0; i < nComps; i++) {
            clusters.get(i).rMean(0).map(gmc.centers, 0, i, 1, 0, 0, nDims);
        }

        // Every component starts out as equally likely.
        gmc.weights.uFill(1.0 / nComps);

        return gmc;
    }

    @Override
    public void update(GMComponents gmc, RealArray posterior, RealArray points, double regularization) {

        int nComps = gmc.weights.size(0);
        int nDims = points.size(1);
        int nPoints = Control.checkEquals(points.size(0), posterior.size(1));

        RealArray colSum = posterior.rSum(1);

        // The normalization has dimensions (nComps, nDims).
        RealArray nrmp = colSum.tile(1, nDims).uAdd(1e-64);

        // Update the means.

        posterior.mMul(points).lDiv(nrmp) //
                .map(gmc.centers, 0, 0, nComps, 0, 0, nDims);

        // Update the covariances.

        posterior.mMul(points.clone().uSqr()).lDiv(nrmp) //
                .lSub(gmc.centers.clone().uSqr()) //
                .map(gmc.covariances, 0, 0, nComps, 0, 0, nDims);

        gmc.regularize(regularization);

        // Update the weights.

        colSum.map(gmc.weights, 0, 0, nComps, 0, 0, 1).uMul(1.0 / nPoints);
    }

    @Override
    public RealArray computeLogWeightedDensities(GMComponents gmc, RealArray points) {

        int nPoints = points.size(0);

        RealArray iCov = gmc.covariances.clone().uInv(1.0);

        RealArray nrmTiled = (gmc.centers.clone().uSqr().lMul(iCov)) //
                .lAdd(gmc.covariances.clone().uLog()) //
                .rSum(1).tile(1, nPoints);

        RealArray pointsT = points.transpose(1, 0);

        RealArray logWeightsTiled = gmc.weights.tile(1, nPoints).uAdd(1e-64).uLog();

        RealArray exponent = (iCov.mMul(pointsT.clone().uSqr())) //
                .lAdd(gmc.centers.eMul(iCov).mMul(pointsT).uMul(-2.0)) //
                .lAdd(nrmTiled).uMul(-0.5);

        return exponent.lAdd(logWeightsTiled);
    }
}

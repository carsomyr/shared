/**
 * <p>
 * Copyright (c) 2007 The Regents of the University of California<br>
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

package shared.stat.plot;

import shared.array.RealArray;

/**
 * A representation of precision-recall plots.
 * 
 * @author Roy Liu
 */
public class PrecisionRecall extends ErrorDistribution {

    /**
     * The <code>x</code>-axis range.
     */
    final protected static double[] XRange = new double[] { 1.0, 0.0 };

    final double[] yrange;

    /**
     * Default constructor.
     * 
     * @see ErrorDistribution#ErrorDistribution(double[][], boolean[][])
     */
    public PrecisionRecall(double[][] confidencesArray, boolean[][] outcomesArray) {
        super(confidencesArray, outcomesArray);

        double precisionMin = 1.0;

        for (RealArray dataset : this.datasets) {
            precisionMin = Math.min(precisionMin, dataset.subarray(0, dataset.size(0), 1, 2).aMin());
        }

        this.yrange = new double[] { precisionMin, 1.0 };
    }

    /**
     * Alternate constructor.
     */
    public PrecisionRecall(double[] confidences, boolean[] outcomes) {
        this(new double[][] { confidences }, new boolean[][] { outcomes });
    }

    @Override
    public String getTitle() {
        return "Precision-Recall";
    }

    @Override
    protected void initDataset(RealArray dataset, boolean[] outcomes) {

        int nExamples = dataset.size(0);

        for (int i = 0, nCorrect = 0; i < nExamples; i++) {

            if (outcomes[i]) {
                nCorrect++;
            }

            dataset.set(i / (double) nExamples, i, 0);
            dataset.set((i > 0) ? nCorrect / (double) i : 1.0, i, 1);
        }
    }

    @Override
    protected double[] getXAxisRange() {
        return XRange;
    }

    @Override
    protected double[] getYAxisRange() {
        return this.yrange;
    }

    @Override
    protected String getXAxisTitle() {
        return "recall";
    }

    @Override
    protected String getYAxisTitle() {
        return "precision";
    }
}

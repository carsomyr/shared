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

package org.shared.stat.plot;

import org.shared.array.RealArray;

/**
 * A representation of ROC (receiver operating characteristic) plots.
 * 
 * @author Roy Liu
 */
public class Roc extends ErrorDistribution {

    /**
     * The <code>x</code>-axis range.
     */
    final protected static double[] xRange = new double[] { 0.0, 1.0 };

    /**
     * The <code>y</code>-axis range.
     */
    final protected static double[] yRange = new double[] { 0.0, 1.0 };

    /**
     * Default constructor.
     * 
     * @see ErrorDistribution#ErrorDistribution(double[][], boolean[][])
     */
    public Roc(double[][] confidencesArray, boolean[][] outcomesArray) {
        super(confidencesArray, outcomesArray);
    }

    /**
     * Alternate constructor.
     */
    public Roc(double[] confidences, boolean[] outcomes) {
        this(new double[][] { confidences }, new boolean[][] { outcomes });
    }

    @Override
    public String getTitle() {
        return "ROC";
    }

    @Override
    protected void initDataset(RealArray dataset, boolean[] outcomes) {

        int nExamples = dataset.size(0);

        int nTrue = 0;
        int nFalse = 0;

        for (int i = 0; i < nExamples; i++) {

            if (outcomes[i]) {

                nTrue++;

            } else {

                nFalse++;
            }

            dataset.set(nFalse, i, 0);
            dataset.set(nTrue, i, 1);
        }

        if (nFalse > 0) {

            for (int i = 0; i < nExamples; i++) {
                dataset.set(dataset.get(i, 0) / nFalse, i, 0);
            }

        } else {

            for (int i = 0; i < nExamples; i++) {
                dataset.set(0.0, i, 0);
            }
        }

        if (nTrue > 0) {

            for (int i = 0; i < nExamples; i++) {
                dataset.set(dataset.get(i, 1) / nTrue, i, 1);
            }

        } else {

            for (int i = 0; i < nExamples; i++) {
                dataset.set(1.0, i, 1);
            }
        }
    }

    @Override
    protected double[] getXAxisRange() {
        return xRange;
    }

    @Override
    protected double[] getYAxisRange() {
        return yRange;
    }

    @Override
    protected String getXAxisTitle() {
        return "false rate";
    }

    @Override
    protected String getYAxisTitle() {
        return "true rate";
    }
}

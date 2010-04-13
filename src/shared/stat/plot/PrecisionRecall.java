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

package shared.stat.plot;

import shared.array.RealArray;

/**
 * A representation of precision-recall plots.
 * 
 * @author Roy Liu
 */
public class PrecisionRecall extends ErrorDistribution {

    /**
     * The <tt>x</tt>-axis range.
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

    public String getTitle() {
        return "Precision-Recall";
    }

    @Override
    protected void initDataset(RealArray dataset, boolean[] outcomes) {

        int nexamples = dataset.size(0);

        for (int i = 0, ncorrect = 0; i < nexamples; i++) {

            if (outcomes[i]) {
                ncorrect++;
            }

            dataset.set(i / (double) nexamples, i, 0);
            dataset.set((i > 0) ? ncorrect / (double) i : 1.0, i, 1);
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

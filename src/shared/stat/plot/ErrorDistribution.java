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
import shared.stat.plot.Plot.AxisType;
import shared.util.Arrays;
import shared.util.Control;

/**
 * A base class for {@link PrecisionRecall} and {@link ROC}.
 * 
 * @author Roy Liu
 */
abstract public class ErrorDistribution implements Plottable {

    /**
     * The datasets.
     */
    final protected RealArray[] datasets;

    /**
     * The data titles.
     */
    final protected String[] dataTitles;

    /**
     * The {@link DataStyle}s.
     */
    final protected DataStyle[] dataStyles;

    /**
     * Default constructor.
     * 
     * @param confidencesArray
     *            the array of prediction confidences.
     * @param outcomesArray
     *            the array of prediction outcomes.
     */
    public ErrorDistribution(double[][] confidencesArray, boolean[][] outcomesArray) {

        int nclasses = Control.checkEquals(confidencesArray.length, outcomesArray.length);

        this.datasets = new RealArray[nclasses];

        for (int i = 0; i < nclasses; i++) {

            int nexamples = Control.checkEquals(confidencesArray[i].length, outcomesArray[i].length);

            double[] confidences = new double[nexamples];
            boolean[] outcomes = new boolean[nexamples];

            RealArray dataset = new RealArray(nexamples, 2);

            int[] perm = new RealArray(confidencesArray[i]).clone().iSort(0).reverse(0).values();

            for (int j = 0; j < nexamples; j++) {

                confidences[j] = confidencesArray[i][perm[j]];
                outcomes[j] = outcomesArray[i][perm[j]];
            }

            initDataset(dataset, outcomes);

            this.datasets[i] = dataset;
        }

        this.dataTitles = PlotBase.createDefaultTitles(nclasses);
        this.dataStyles = Arrays.newArray(DataStyle.class, nclasses, DataStyle.Lines);
    }

    /**
     * Gets the AUC's (area under curves).
     */
    public double[] getAUCs() {

        double[] aucs = new double[this.datasets.length];

        for (int i = 0, n = this.datasets.length; i < n; i++) {

            RealArray dataset = this.datasets[i];

            RealArray xarr = dataset.subarray(0, dataset.size(0), 0, 1);
            RealArray yarr = dataset.subarray(0, dataset.size(0), 1, 2);

            aucs[i] = xarr.eSub(xarr.shift(1, 0)).eMul(yarr.eAdd(yarr.shift(1, 0))).uMul(0.5) //
                    .subarray(1, dataset.size(0), 0, dataset.size(1)).rSum(0).get(0, 0);
        }

        return aucs;
    }

    public RealArray[] getDatasets() {
        return this.datasets;
    }

    public String[] getDataTitles() {
        return this.dataTitles;
    }

    public DataStyle[] getDataStyles() {
        return this.dataStyles;
    }

    public boolean getPropertyEnabled(String property) {
        return (property.equals("legend") || property.equals("grid"));
    }

    public String getAxisTitle(AxisType axisType) {

        switch (axisType) {

        case X:
            return getXAxisTitle();

        case Y:
            return getYAxisTitle();

        default:
            throw new IllegalArgumentException("Invalid axis type");
        }
    }

    public double[] getAxisRange(AxisType axisType) {

        switch (axisType) {

        case X:
            return getXAxisRange();

        case Y:
            return getYAxisRange();

        default:
            throw new IllegalArgumentException("Invalid axis type");
        }
    }

    /**
     * Initializes the given dataset.
     * 
     * @param dataset
     *            the dataset.
     * @param outcomes
     *            the prediction outcomes.
     */
    abstract protected void initDataset(RealArray dataset, boolean[] outcomes);

    /**
     * Gets the <tt>x</tt>-axis title.
     */
    abstract protected String getXAxisTitle();

    /**
     * Gets the <tt>y</tt>-axis title.
     */
    abstract protected String getYAxisTitle();

    /**
     * Gets the <tt>x</tt>-axis range.
     */
    abstract protected double[] getXAxisRange();

    /**
     * Gets the <tt>y</tt>-axis range.
     */
    abstract protected double[] getYAxisRange();
}

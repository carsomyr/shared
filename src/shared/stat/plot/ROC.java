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
 * A representation of ROC (receiver operating characteristic) plots.
 * 
 * @author Roy Liu
 */
public class ROC extends ErrorDistribution {

    /**
     * The <tt>x</tt>-axis range.
     */
    final protected static double[] XRange = new double[] { 0.0, 1.0 };

    /**
     * The <tt>y</tt>-axis range.
     */
    final protected static double[] YRange = new double[] { 0.0, 1.0 };

    /**
     * Default constructor.
     * 
     * @see ErrorDistribution#ErrorDistribution(double[][], boolean[][])
     */
    public ROC(double[][] confidencesArray, boolean[][] outcomesArray) {
        super(confidencesArray, outcomesArray);
    }

    /**
     * Alternate constructor.
     */
    public ROC(double[] confidences, boolean[] outcomes) {
        this(new double[][] { confidences }, new boolean[][] { outcomes });
    }

    public String getTitle() {
        return "ROC";
    }

    @Override
    protected void initDataset(RealArray dataset, boolean[] outcomes) {

        int nexamples = dataset.size(0);

        int ntrue = 0;
        int nfalse = 0;

        for (int i = 0; i < nexamples; i++) {

            if (outcomes[i]) {

                ntrue++;

            } else {

                nfalse++;
            }

            dataset.set(nfalse, i, 0);
            dataset.set(ntrue, i, 1);
        }

        if (nfalse > 0) {

            for (int i = 0; i < nexamples; i++) {
                dataset.set(dataset.get(i, 0) / nfalse, i, 0);
            }

        } else {

            for (int i = 0; i < nexamples; i++) {
                dataset.set(0.0, i, 0);
            }
        }

        if (ntrue > 0) {

            for (int i = 0; i < nexamples; i++) {
                dataset.set(dataset.get(i, 1) / ntrue, i, 1);
            }

        } else {

            for (int i = 0; i < nexamples; i++) {
                dataset.set(1.0, i, 1);
            }
        }
    }

    @Override
    protected double[] getXAxisRange() {
        return XRange;
    }

    @Override
    protected double[] getYAxisRange() {
        return YRange;
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

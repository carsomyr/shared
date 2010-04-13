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

/**
 * A representation of histograms.
 * 
 * @author Roy Liu
 */
public class Histogram implements Plottable {

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
     * The <tt>x</tt> range.
     */
    final protected double[] xrange;

    /**
     * The <tt>y</tt> range.
     */
    final protected double[] yrange;

    /**
     * Default constructor.
     * 
     * @param min
     *            the range minimum.
     * @param max
     *            the range maximum.
     * @param nbins
     *            the number of bins.
     * @param valuesArray
     *            the array of values.
     */
    public Histogram(double min, double max, int nbins, double[]... valuesArray) {

        int nclasses = valuesArray.length;

        this.datasets = new RealArray[nclasses];

        double increment = (max - min) / nbins;
        double maxCount = 0.0;

        for (int i = 0, n = valuesArray.length; i < n; i++) {

            RealArray dataset = new RealArray(nbins, 2);

            for (int bin = 0; bin < nbins; bin++) {
                dataset.set(increment * (bin + 0.5) + min, bin, 0);
            }

            for (double value : valuesArray[i]) {

                int bin = Math.max(0, Math.min(nbins - 1, (int) ((value - min) / increment)));
                dataset.set(dataset.get(bin, 1) + 1.0d, bin, 1);
            }

            maxCount = Math.max(maxCount, dataset.subarray(0, nbins, 1, 2).aMax());

            this.datasets[i] = dataset;
        }

        this.xrange = new double[] { min, max };
        this.yrange = new double[] { 0, maxCount };

        this.dataTitles = PlotBase.createDefaultTitles(nclasses);
        this.dataStyles = shared.util.Arrays.newArray(DataStyle.class, nclasses, DataStyle.Bars);
    }

    public String getTitle() {
        return "Histogram";
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
        return false;
    }

    public double[] getAxisRange(AxisType axisType) {

        switch (axisType) {

        case X:
            return this.xrange;

        case Y:
            return this.yrange;

        default:
            throw new IllegalArgumentException("Invalid axis type");
        }
    }

    public String getAxisTitle(AxisType axisType) {

        switch (axisType) {

        case X:
            return "bins";

        case Y:
            return "counts";

        default:
            throw new IllegalArgumentException("Invalid axis type");
        }
    }
}

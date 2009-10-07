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

package shared.stat.plot;

import shared.array.RealArray;
import shared.stat.plot.Plot.AxisType;

/**
 * A representation of scatter plots.
 * 
 * @author Roy Liu
 */
public class Scatter implements Plottable {

    /**
     * The generic axis range.
     */
    final protected static double[] AxisRange = new double[] { 0.0, 1.0 };

    final RealArray[] datasets;
    final String[] dataTitles;
    final DataStyle[] dataStyles;

    /**
     * Default constructor.
     * 
     * @param datasets
     *            the datasets.
     */
    public Scatter(RealArray... datasets) {

        int nclasses = datasets.length;

        // Check for uniform dimensionality.
        PlotBase.inferDimensionality(datasets);

        this.datasets = datasets;
        this.dataTitles = PlotBase.createDefaultTitles(nclasses);
        this.dataStyles = shared.util.Arrays.newArray(DataStyle.class, nclasses, DataStyle.Points);
    }

    public RealArray[] getDatasets() {
        return this.datasets;
    }

    public String getTitle() {
        return "Scatter Plot";
    }

    public boolean getPropertyEnabled(String property) {
        return property.equals("legend");
    }

    public double[] getAxisRange(AxisType axisType) {
        return AxisRange;
    }

    public String getAxisTitle(AxisType axisType) {
        return axisType.toString();
    }

    public String[] getDataTitles() {
        return this.dataTitles;
    }

    public DataStyle[] getDataStyles() {
        return this.dataStyles;
    }
}

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
import org.shared.stat.plot.Plot.AxisType;

/**
 * A representation of scatter plots.
 * 
 * @author Roy Liu
 */
public class Scatter implements Plottable {

    /**
     * The generic axis range.
     */
    final protected static double[] axisRange = new double[] { 0.0, 1.0 };

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

        int nClasses = datasets.length;

        // Check for uniform dimensionality.
        PlotBase.inferDimensionality(datasets);

        this.datasets = datasets;
        this.dataTitles = PlotBase.createDefaultTitles(nClasses);
        this.dataStyles = org.shared.util.Arrays.newArray(DataStyle.class, nClasses, DataStyle.points);
    }

    @Override
    public RealArray[] getDatasets() {
        return this.datasets;
    }

    @Override
    public String getTitle() {
        return "Scatter Plot";
    }

    @Override
    public boolean isPropertyEnabled(String property) {
        return property.equals("legend");
    }

    @Override
    public double[] getAxisRange(AxisType axisType) {
        return axisRange;
    }

    @Override
    public String getAxisTitle(AxisType axisType) {
        return axisType.toString();
    }

    @Override
    public String[] getDataTitles() {
        return this.dataTitles;
    }

    @Override
    public DataStyle[] getDataStyles() {
        return this.dataStyles;
    }
}

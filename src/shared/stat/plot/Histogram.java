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
     * The <code>x</code> range.
     */
    final protected double[] xrange;

    /**
     * The <code>y</code> range.
     */
    final protected double[] yrange;

    /**
     * Default constructor.
     * 
     * @param min
     *            the range minimum.
     * @param max
     *            the range maximum.
     * @param nBins
     *            the number of bins.
     * @param valuesArray
     *            the array of values.
     */
    public Histogram(double min, double max, int nBins, double[]... valuesArray) {

        int nClasses = valuesArray.length;

        this.datasets = new RealArray[nClasses];

        double increment = (max - min) / nBins;
        double maxCount = 0.0;

        for (int i = 0, n = valuesArray.length; i < n; i++) {

            RealArray dataset = new RealArray(nBins, 2);

            for (int bin = 0; bin < nBins; bin++) {
                dataset.set(increment * (bin + 0.5) + min, bin, 0);
            }

            for (double value : valuesArray[i]) {

                int bin = Math.max(0, Math.min(nBins - 1, (int) ((value - min) / increment)));
                dataset.set(dataset.get(bin, 1) + 1.0d, bin, 1);
            }

            maxCount = Math.max(maxCount, dataset.subarray(0, nBins, 1, 2).aMax());

            this.datasets[i] = dataset;
        }

        this.xrange = new double[] { min, max };
        this.yrange = new double[] { 0, maxCount };

        this.dataTitles = PlotBase.createDefaultTitles(nClasses);
        this.dataStyles = shared.util.Arrays.newArray(DataStyle.class, nClasses, DataStyle.Bars);
    }

    @Override
    public String getTitle() {
        return "Histogram";
    }

    @Override
    public RealArray[] getDatasets() {
        return this.datasets;
    }

    @Override
    public String[] getDataTitles() {
        return this.dataTitles;
    }

    @Override
    public DataStyle[] getDataStyles() {
        return this.dataStyles;
    }

    @Override
    public boolean getPropertyEnabled(String property) {
        return false;
    }

    @Override
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

    @Override
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

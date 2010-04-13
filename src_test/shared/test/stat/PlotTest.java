/**
 * This file is part of the Shared Scientific Toolbox in Java ("this library"). <br />
 * <br />
 * Copyright (C) 2009 The Regents of the University of California <br />
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

package shared.test.stat;

import static org.junit.Assert.assertTrue;
import static shared.test.Demo.DemoDir;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.junit.BeforeClass;
import org.junit.Test;

import shared.array.RealArray;
import shared.array.AbstractRealArray.RealMap;
import shared.stat.plot.DataStyle;
import shared.stat.plot.GnuplotContext;
import shared.stat.plot.Histogram;
import shared.stat.plot.PrecisionRecall;
import shared.stat.plot.ProbabilityDistribution;
import shared.stat.plot.ROC;
import shared.stat.plot.Scatter;
import shared.stat.plot.DataStyle.DataStyleType;
import shared.stat.plot.Plot.AxisScaleType;
import shared.stat.plot.Plot.AxisType;
import shared.stat.plot.ProbabilityDistribution.Mode;
import shared.test.Demo;
import shared.util.Arithmetic;
import shared.util.Control;

/**
 * A plotting demo.
 * 
 * @author Roy Liu
 */
public class PlotTest {

    /**
     * Creates the demo directory if it doesn't already exist in the file system.
     */
    @BeforeClass
    final public static void initClass() {
        Demo.createDemoDir();
    }

    /**
     * Default constructor.
     */
    public PlotTest() {
    }

    /**
     * Tests plotting operations.
     * 
     * @throws IOException
     *             when something goes awry.
     */
    @Test
    public void testPlot() throws IOException {

        final int size = 31;

        RealArray a2D = new RealArray(384, 2);
        RealArray a3D = new RealArray(size * size, 3);

        GnuplotContext gpc = new GnuplotContext();

        // 2D random scatter plot that highlights logarithmic scaling for the x-axis.

        Arithmetic.derandomize();

        gpc.addPlot(a2D.clone().uRnd(1.5).uAdd(0.5), a2D.clone().uRnd(1.5).uAdd(0.5)) //
                .setAxis(AxisType.X, "log(X)", 0.5, 2.0, AxisScaleType.LOG) //
                .setAxis(AxisType.Y, "Y", 0.5, 2.0, AxisScaleType.NORMAL) //
                .setDataStyles( //
                        new DataStyle(DataStyleType.POINTS) //
                                .setLineColor("blue") //
                                .setPointStyle("4"), //
                        new DataStyle(DataStyleType.POINTS) //
                                .setLineColor("gold") //
                                .setPointStyle("4")) //
                .setTitle("2D Scatter Plot") //
                .setPanelLocation(0, 0);

        // 2D random line graph.

        Arithmetic.derandomize();

        RealArray a2D1 = new RealArray(32, 2).uRnd(4.0).uAdd(-2.0);
        RealArray a2D2 = new RealArray(32, 2).uRnd(4.0).uAdd(-2.0);

        RealArray a2D1Sorted = a2D1.clone();
        RealArray a2D2Sorted = a2D2.clone();

        a2D1Sorted.iSort(0);
        a2D2Sorted.iSort(0);

        a2D1Sorted.map(a2D1, 0, 0, a2D1.size(0), 0, 0, 1);
        a2D2Sorted.map(a2D2, 0, 0, a2D2.size(0), 0, 0, 1);

        gpc.addPlot(new Scatter(a2D1, a2D2)) //
                .setAxisRange(AxisType.X, -2.0, 2.0) //
                .setAxisRange(AxisType.Y, -2.0, 2.0) //
                .setPropertyEnabled("grid", true) //
                .setDataStyles( //
                        new DataStyle(DataStyleType.LINESPOINTS) //
                                .setLineColor("blue") //
                                .setLineStyle("2") //
                                .setLineSize(4.0) //
                                .setPointStyle("2") //
                                .setPointSize(1.0), //
                        new DataStyle(DataStyleType.POINTS) //
                                .setLineColor("gold") //
                                .setLineStyle("1") //
                                .setLineSize(1.0) //
                                .setPointStyle("1") //
                                .setPointSize(2.0)) //
                .setTitle("Line Plot") //
                .setPanelLocation(0, 1);

        // 3D mesh plot.

        a3D.map(new RealMap() {

            public double apply(double value, int[] logical) {

                int xcoord = (size >>> 1) - (logical[0] / size);
                int ycoord = (size >>> 1) - (logical[0] % size);

                switch (logical[1]) {

                case 0:
                    return xcoord;

                case 1:
                    return ycoord;

                case 2:
                    return (xcoord * ycoord) / Math.sqrt(Math.abs(xcoord * ycoord) + 1);

                default:
                    throw new IllegalStateException("Invalid logical index");
                }
            }
        });

        gpc.addPlot(new Scatter(a3D)) //
                .setAxisRange(AxisType.X, -(size >>> 1), (size >>> 1)) //
                .setAxisRange(AxisType.Y, -(size >>> 1), (size >>> 1)) //
                .setAxisRange(AxisType.Z, 1.5 * -(size >>> 1), 1.5 * (size >>> 1)) //
                .setPropertyEnabled("mesh", true) //
                .setPropertyEnabled("colormap", true) //
                .setPropertyEnabled("legend", false) //
                .setDataStyles(DataStyle.Surface) //
                .setTitle("3D Mesh Plot") //
                .setViewport(60.0, 15.0) //
                .setPanelLocation(1, 0) //
                .setPanelSize(1, 2);

        int nvalues = 1024;

        double[] values = new double[nvalues];

        for (int i = 0; i < nvalues; i++) {
            values[i] = Arithmetic.nextGaussian(2.0);
        }

        // Random histogram.

        gpc.addPlot(new Histogram(-6.0, 6.0, 24, values)) //
                .setPanelLocation(0, 2);

        // Random probability distribution.

        gpc.addPlot(new ProbabilityDistribution(Mode.PDF, -6.0, 6.0, 24, values)) //
                .setPanelLocation(1, 2);
        gpc.addPlot(new ProbabilityDistribution(Mode.CDF, -6.0, 6.0, 24, values)) //
                .setPanelLocation(2, 2);

        // Random ROC curve.

        Arithmetic.derandomize();

        boolean[] outcomes = new boolean[nvalues];

        for (int i = 0; i < nvalues; i++) {

            values[i] = Arithmetic.nextDouble(1.0);
            outcomes[i] = (Arithmetic.nextDouble(1.0) < (1.0 - values[i]) * 0.25) ? false : true;
        }

        ROC roc = new ROC(values, outcomes);

        gpc.addPlot(roc) //
                .setTitle(String.format("%s (auc = %.4f)", roc.getTitle(), roc.getAUCs()[0])) //
                .setPanelLocation(2, 0);

        // Random precision-recall curve.

        PrecisionRecall pr = new PrecisionRecall(values, outcomes);

        gpc.addPlot(pr) //
                .setTitle(String.format("%s (auc = %.4f)", pr.getTitle(), pr.getAUCs()[0])) //
                .setPanelLocation(2, 1);

        //

        File epsFile = new File(DemoDir, "gnuplot.eps");
        File pngFile = new File(DemoDir, "gnuplot.png");
        File svgFile = new File(DemoDir, "gnuplot.svg");

        Control.delete(epsFile);
        Control.delete(pngFile);
        Control.delete(svgFile);

        gpc.setOutputFormat("eps").setFont("Helvetica", 8).toFile(epsFile);

        //

        URL url = Thread.currentThread().getContextClassLoader().getResource("font/Vera.ttf");

        File file = new File(DemoDir, "Vera.ttf");

        Control.transfer(url.openStream(), file);

        Control.beginEnvironment().put("GDFONTPATH", file.getParent());

        try {

            gpc.setOutputFormat("png").setFont("Vera", 8).toFile(pngFile);

        } finally {

            Control.endEnvironment();
        }

        Control.delete(file);

        //

        gpc.setOutputFormat("svg").setFont("Helvetica", 8).toFile(svgFile);

        assertTrue(epsFile.exists());
        assertTrue(pngFile.exists());
        assertTrue(svgFile.exists());
    }
}

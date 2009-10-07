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

import java.util.Arrays;

import shared.array.RealArray;
import shared.stat.plot.Plot.AxisType;

/**
 * A representation of probability distributions.
 * 
 * @apiviz.owns shared.stat.plot.ProbabilityDistribution.Mode
 * @author Roy Liu
 */
public class ProbabilityDistribution extends Histogram {

    /**
     * An enumeration of probability distribution modes.
     */
    public enum Mode {

        /**
         * Indicates a cumulative density function.
         */
        CDF {

            @Override
            protected void initDistribution(RealArray d) {
                d.eDiv(d.rSum(0).tile(d.size(0), 1)).dSum(0) //
                        .map(d, 0, 0, d.size(0), 1, 1, 1);
            }
        }, //

        /**
         * Indicates a probability density function.
         */
        PDF {

            @Override
            protected void initDistribution(RealArray d) {
                d.eDiv(d.rSum(0).tile(d.size(0), 1)) //
                        .map(d, 0, 0, d.size(0), 1, 1, 1);
            }
        };

        /**
         * Initializes the probability distribution.
         */
        abstract protected void initDistribution(RealArray distribution);
    }

    final Mode mode;

    /**
     * Default constructor.
     * 
     * @param mode
     *            the display mode.
     * @param min
     *            the range minimum.
     * @param max
     *            the range maximum.
     * @param nintervals
     *            the number of sampling intervals.
     */
    public ProbabilityDistribution(Mode mode, double min, double max, int nintervals, double[] values) {
        super(min, max, nintervals, values);

        double maxDensity = 0.0;

        for (RealArray dataset : this.datasets) {

            mode.initDistribution(dataset);

            maxDensity = Math.max(maxDensity, dataset.subarray(0, dataset.size(0), 1, 2).aMax());
        }

        Arrays.fill(this.dataStyles, DataStyle.Lines);

        this.yrange[0] = 0.0;
        this.yrange[1] = (mode == Mode.PDF) ? maxDensity : 1.0;

        this.mode = mode;
    }

    /**
     * Alternate constructor.
     * 
     * @param min
     *            the range minimum.
     * @param max
     *            the range maximum.
     * @param nintervals
     *            the number of sampling intervals.
     */
    public ProbabilityDistribution(double min, double max, int nintervals, double[] values) {
        this(Mode.CDF, min, max, nintervals, values);
    }

    @Override
    public String getTitle() {

        switch (this.mode) {

        case PDF:
            return "Probability Density Function";

        case CDF:
            return "Cumulative Density Function";

        default:
            throw new IllegalArgumentException("Invalid mode");
        }
    }

    @Override
    public String getAxisTitle(AxisType axisType) {

        switch (axisType) {

        case X:
            return axisType.toString();

        case Y:

            switch (this.mode) {

            case PDF:
                return "density";

            case CDF:
                return "cumulative density";

            default:
                throw new IllegalArgumentException("Invalid mode");
            }

        default:
            throw new IllegalArgumentException("Invalid axis type");
        }
    }
}

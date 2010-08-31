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
     * @param nIntervals
     *            the number of sampling intervals.
     */
    public ProbabilityDistribution(Mode mode, double min, double max, int nIntervals, double[] values) {
        super(min, max, nIntervals, values);

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
     * @param nIntervals
     *            the number of sampling intervals.
     */
    public ProbabilityDistribution(double min, double max, int nIntervals, double[] values) {
        this(Mode.CDF, min, max, nIntervals, values);
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

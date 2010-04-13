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

package shared.stat.plot;

import shared.array.RealArray;
import shared.util.Control;

/**
 * A collection of useful static methods for plotting.
 */
public class PlotBase {

    /**
     * Infers the dimensionality of the given datasets.
     * 
     * @param datasets
     *            the datasets.
     * @return the dimensionality.
     */
    final public static int inferDimensionality(RealArray... datasets) {

        int nclasses = datasets.length;

        Control.checkTrue(nclasses > 0, //
                "Please specify some datasets");

        for (int i = 0; i < nclasses; i++) {
            Control.checkTrue(datasets[i].ndims() == 2, //
                    "Invalid arguments");
        }

        int ndims = datasets[0].size(1);

        for (int i = 1; i < nclasses; i++) {
            Control.checkTrue(ndims == datasets[i].size(1), //
                    "Dimensionality mismatch");
        }

        return ndims;
    }

    /**
     * Creates a default array of titles.
     * 
     * @param length
     *            the desired length.
     * @return an array consisting of {@code 1}, {@code 2}, <tt>...</tt>, {@code length}.
     */
    final public static String[] createDefaultTitles(int length) {

        String[] res = new String[length];

        for (int i = 0; i < length; i++) {
            res[i] = String.format("class %d", i + 1);
        }

        return res;
    }

    // Dummy constructor.
    PlotBase() {
    }
}

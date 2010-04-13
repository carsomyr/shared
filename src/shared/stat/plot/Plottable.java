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
import shared.stat.plot.Plot.AxisType;

/**
 * Defines something as suitable for having its values plotted.
 */
public interface Plottable {

    /**
     * Gets the datasets.
     */
    public RealArray[] getDatasets();

    /**
     * Gets the title.
     */
    public String getTitle();

    /**
     * Gets the axis title.
     * 
     * @param axisType
     *            the {@link AxisType}.
     */
    public String getAxisTitle(AxisType axisType);

    /**
     * Gets the axis range.
     * 
     * @param axisType
     *            the {@link AxisType}.
     */
    public double[] getAxisRange(AxisType axisType);

    /**
     * Gets the data titles.
     */
    public String[] getDataTitles();

    /**
     * Gets the {@link DataStyle}s.
     */
    public DataStyle[] getDataStyles();

    /**
     * Gets whether the given property is enabled.
     * 
     * @param property
     *            the given property.
     * @return whether the given property is enabled.
     */
    public boolean getPropertyEnabled(String property);
}

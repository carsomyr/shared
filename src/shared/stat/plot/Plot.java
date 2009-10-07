/**
 * This file is part of the Shared Scientific Toolbox in Java ("this library"). <br />
 * <br />
 * Copyright (C) 2009 Roy Liu <br />
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

/**
 * Defines a plotting abstraction.
 * 
 * @apiviz.owns shared.stat.plot.Plottable
 * @apiviz.owns shared.stat.plot.Plot.AxisType
 * @apiviz.owns shared.stat.plot.Plot.AxisScaleType
 * @apiviz.owns shared.stat.plot.DataStyle
 * @param <T>
 *            the parameterization lower bounded by {@link Plot} itself.
 */
public interface Plot<T extends Plot<T>> {

    /**
     * An enumeration of axis types.
     */
    public enum AxisType {

        /**
         * Indicates the <tt>x</tt>-axis.
         */
        X, //

        /**
         * Indicates the <tt>y</tt>-axis.
         */
        Y, //

        /**
         * Indicates the <tt>z</tt>-axis.
         */
        Z;
    };

    /**
     * An enumeration of axis scale types.
     */
    public enum AxisScaleType {

        /**
         * Indicates a normal scale.
         */
        NORMAL, //

        /**
         * Indicates a logarithmic scale.
         */
        LOG;
    };

    /**
     * Sets the title.
     * 
     * @param title
     *            the title.
     */
    public T setTitle(String title);

    /**
     * Sets the axis information.
     * 
     * @param axisType
     *            the {@link AxisType}.
     * @param axisTitle
     *            the axis title.
     * @param lower
     *            the lower bound.
     * @param upper
     *            the upper bound.
     * @param axisScaleType
     *            the {@link AxisScaleType}.
     */
    public T setAxis(AxisType axisType, String axisTitle, double lower, double upper, AxisScaleType axisScaleType);

    /**
     * Sets the axis title.
     * 
     * @param axisType
     *            the {@link AxisType}.
     * @param axisTitle
     *            the axis title.
     */
    public T setAxisTitle(AxisType axisType, String axisTitle);

    /**
     * Sets the axis range.
     * 
     * @param axisType
     *            the {@link AxisType}.
     * @param lower
     *            the lower bound.
     * @param upper
     *            the upper bound.
     */
    public T setAxisRange(AxisType axisType, double lower, double upper);

    /**
     * Sets the {@link AxisScaleType}.
     * 
     * @param axisType
     *            the {@link AxisType}.
     * @param axisScaleType
     *            the {@link AxisScaleType}.
     */
    public T setAxisScale(AxisType axisType, AxisScaleType axisScaleType);

    /**
     * Sets the data titles.
     * 
     * @param dataTitles
     *            the data titles.
     */
    public T setDataTitles(String... dataTitles);

    /**
     * Sets the {@link DataStyle}s.
     * 
     * @param dataStyles
     *            the {@link DataStyle}s.
     */
    public T setDataStyles(DataStyle... dataStyles);

    /**
     * Sets the viewport.
     * 
     * @param viewportParameters
     *            the viewport parameters.
     */
    public T setViewport(double... viewportParameters);

    /**
     * Sets whether the given property is enabled.
     * 
     * @param isPropertyEnabled
     *            whether the given property is enabled.
     */
    public T setPropertyEnabled(String property, boolean isPropertyEnabled);

    /**
     * Sets the panel location.
     * 
     * @param panelX
     *            the panel <tt>x</tt>-coordinate.
     * @param panelY
     *            the panel <tt>y</tt>-coordinate.
     */
    public T setPanelLocation(int panelX, int panelY);

    /**
     * Sets the panel size.
     * 
     * @param panelWidth
     *            the panel width.
     * @param panelHeight
     *            the panel height.
     */
    public T setPanelSize(int panelWidth, int panelHeight);
}

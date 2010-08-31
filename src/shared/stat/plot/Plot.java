/**
 * <p>
 * Copyright (c) 2009 The Regents of the University of California<br>
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

/**
 * Defines a plotting abstraction.
 * 
 * @apiviz.owns shared.stat.plot.DataStyle
 * @apiviz.owns shared.stat.plot.Plot.AxisType
 * @apiviz.owns shared.stat.plot.Plot.AxisScaleType
 * @apiviz.owns shared.stat.plot.Plottable
 * @param <T>
 *            the parameterization lower bounded by {@link Plot} itself.
 */
public interface Plot<T extends Plot<T>> {

    /**
     * An enumeration of axis types.
     */
    public enum AxisType {

        /**
         * Indicates the <code>x</code>-axis.
         */
        X, //

        /**
         * Indicates the <code>y</code>-axis.
         */
        Y, //

        /**
         * Indicates the <code>z</code>-axis.
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
     *            the panel <code>x</code>-coordinate.
     * @param panelY
     *            the panel <code>y</code>-coordinate.
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

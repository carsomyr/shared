/**
 * <p>
 * Copyright (C) 2009 The Regents of the University of California<br />
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

import shared.event.EnumType;
import shared.util.Control;

/**
 * A container class for dataset rendering style information.
 * 
 * @apiviz.owns shared.stat.plot.DataStyle.DataStyleType
 * @author Roy Liu
 */
public class DataStyle implements EnumType<DataStyle.DataStyleType> {

    /**
     * An enumeration of {@link DataStyle} types.
     */
    public enum DataStyleType {

        /**
         * Indicates lines.
         */
        LINES, //

        /**
         * Indicates points.
         */
        POINTS, //

        /**
         * Indicates lines and points.
         */
        LINESPOINTS, //

        /**
         * Indicates bars.
         */
        BARS, //

        /**
         * Indicates that the data induces a surface.
         */
        SURFACE;
    };

    /**
     * The default style for {@link DataStyleType#LINES}.
     */
    final public static DataStyle Lines = new DataStyle(DataStyleType.LINES);

    /**
     * The default style for {@link DataStyleType#POINTS}.
     */
    final public static DataStyle Points = new DataStyle(DataStyleType.POINTS);

    /**
     * The default style for {@link DataStyleType#LINESPOINTS}.
     */
    final public static DataStyle LinesPoints = new DataStyle(DataStyleType.LINESPOINTS);

    /**
     * The default style for {@link DataStyleType#BARS}.
     */
    final public static DataStyle Bars = new DataStyle(DataStyleType.BARS);

    /**
     * The default style for {@link DataStyleType#SURFACE}.
     */
    final public static DataStyle Surface = new DataStyle(DataStyleType.SURFACE);

    final DataStyleType type;

    String lineStyle;
    String lineColor;

    Double lineSize;

    String pointStyle;
    String pointColor;

    Double pointSize;

    /**
     * Default constructor.
     */
    public DataStyle(DataStyleType type) {

        this.type = type;

        Control.checkTrue(type != null, //
                "Invalid arguments");
    }

    @Override
    public DataStyleType getType() {
        return this.type;
    }

    /**
     * Gets the line style.
     */
    public String getLineStyle() {
        return this.lineStyle;
    }

    /**
     * Sets the line style.
     */
    public DataStyle setLineStyle(String lineStyle) {

        this.lineStyle = lineStyle;

        return this;
    }

    /**
     * Gets the line color.
     */
    public String getLineColor() {
        return this.lineColor;
    }

    /**
     * Sets the line color.
     */
    public DataStyle setLineColor(String lineColor) {

        this.lineColor = lineColor;

        return this;
    }

    /**
     * Gets the line size.
     */
    public Double getLineSize() {
        return this.lineSize;
    }

    /**
     * Sets the line size.
     */
    public DataStyle setLineSize(Double lineSize) {

        this.lineSize = lineSize;

        return this;
    }

    /**
     * Gets the point style.
     */
    public String getPointStyle() {
        return this.pointStyle;
    }

    /**
     * Sets the point style.
     */
    public DataStyle setPointStyle(String pointStyle) {

        this.pointStyle = pointStyle;

        return this;
    }

    /**
     * Gets the point color.
     */
    public String getPointColor() {
        return this.pointColor;
    }

    /**
     * Sets the point color.
     */
    public DataStyle setPointColor(String pointColor) {

        this.pointColor = pointColor;

        return this;
    }

    /**
     * Gets the point size.
     */
    public Double getPointSize() {
        return this.pointSize;
    }

    /**
     * Sets the point size.
     */
    public DataStyle setPointSize(Double pointSize) {

        this.pointSize = pointSize;

        return this;
    }
}

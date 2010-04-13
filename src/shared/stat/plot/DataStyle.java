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

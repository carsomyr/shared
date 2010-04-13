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

import java.io.File;
import java.io.IOException;

import shared.array.RealArray;

/**
 * Defines a context for creating {@link Plot}s.
 * 
 * @apiviz.owns shared.stat.plot.Plot
 * @param <T>
 *            the parameterization lower bounded by {@link PlotContext} itself.
 * @param <P>
 *            the {@link Plot} type.
 */
public interface PlotContext<T extends PlotContext<T, P>, P extends Plot<P>> {

    /**
     * Creates a {@link Plot} at the given coordinates.
     * 
     * @param plottable
     *            the {@link Plottable}.
     */
    public P addPlot(Plottable plottable);

    /**
     * Creates a {@link Plot} at the given coordinates.
     * 
     * @param datasets
     *            the datasets.
     */
    public P addPlot(RealArray... datasets);

    /**
     * Exports to a file.
     * 
     * @param file
     *            the file.
     * @throws IOException
     *             when something goes awry.
     */
    public void toFile(File file) throws IOException;

    /**
     * Sets the font and size.
     * 
     * @param fontName
     *            the font name.
     * @param fontSize
     *            the font size.
     */
    public T setFont(String fontName, int fontSize);

    /**
     * Sets the output format.
     * 
     * @param outputFormat
     *            the output format.
     */
    public T setOutputFormat(String outputFormat);

    /**
     * Sets the output size.
     * 
     * @param outputWidth
     *            the output width.
     * @param outputHeight
     *            the output height.
     */
    public T setOutputSize(int outputWidth, int outputHeight);
}

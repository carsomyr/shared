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

package org.shared.stat.plot;

import java.io.File;
import java.io.IOException;

import org.shared.array.RealArray;

/**
 * Defines a context for creating {@link Plot}s.
 * 
 * @apiviz.owns org.shared.stat.plot.Plot
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

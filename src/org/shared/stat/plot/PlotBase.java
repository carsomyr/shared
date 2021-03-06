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

import org.shared.array.RealArray;
import org.shared.util.Control;

/**
 * A static utility class for plotting.
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

        int nClasses = datasets.length;

        Control.checkTrue(nClasses > 0, //
                "Please specify some datasets");

        for (int i = 0; i < nClasses; i++) {
            Control.checkTrue(datasets[i].nDims() == 2, //
                    "Invalid arguments");
        }

        int nDims = datasets[0].size(1);

        for (int i = 1; i < nClasses; i++) {
            Control.checkTrue(nDims == datasets[i].size(1), //
                    "Dimensionality mismatch");
        }

        return nDims;
    }

    /**
     * Creates a default array of titles.
     * 
     * @param length
     *            the desired length.
     * @return an array consisting of {@code 1}, {@code 2}, <code>...</code>, {@code length}.
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

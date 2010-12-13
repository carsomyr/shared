/**
 * <p>
 * Copyright (c) 2007-2010 The Regents of the University of California<br>
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

package org.shared.image.filter;

import org.shared.array.AbstractArray;
import org.shared.array.ComplexArray;
import org.shared.array.RealArray;

/**
 * A static utility class in support of two-dimensional filters.
 * 
 * @author Roy Liu
 */
public class Filters {

    /**
     * Mutatively normalizes a {@link RealArray} by its <code>L1</code> norm or, alternatively, a {@link ComplexArray}
     * by its complex magnitudes.
     */
    final public static <T extends AbstractArray<?, ?, ?, ?>> void normalize(T array) {

        if (array instanceof RealArray) {

            RealArray m = (RealArray) array;

            m = m.uAdd(-m.aMean());
            m.uMul(1.0 / m.clone().uAbs().aSum());

        } else if (array instanceof ComplexArray) {

            ComplexArray c = (ComplexArray) array;

            double[] mean = c.aMean();

            c.uAdd(-mean[0], -mean[1]);
            c.uMul(1.0 / c.torAbs().aSum(), 0.0);

        } else {

            throw new IllegalArgumentException("Array type not recognized");
        }
    }

    /**
     * Creates the <code>{@code 2}&#x00D7;{@code 2}</code> rotation matrix<br>
     * <code style="white-space: pre;">
     * -cos(&#x03B8;) -sin(&#x03B8;)
     * -sin(&#x03B8;)  cos(&#x03B8;)</code>.
     * 
     * @param theta
     *            the angle of rotation.
     * @return the rotation matrix.
     */
    final public static RealArray createRotationMatrix(double theta) {

        RealArray res = new RealArray(2, 2);

        res.set(-Math.cos(theta), 0, 0);
        res.set(-Math.sin(theta), 0, 1);
        res.set(-Math.sin(theta), 1, 0);
        res.set(Math.cos(theta), 1, 1);

        return res;
    }

    /**
     * Creates a pair of <code>x</code>- and <code>y</code>-axis aligned DooG kernels for gradient calculations.
     * 
     * @param supportRadius
     *            the kernel support radius.
     * @return the pair of kernels.
     */
    final public static RealArray[] createGradientKernels(int supportRadius) {

        RealArray[] gk = new RealArray[2];

        normalize(gk[0] = new DerivativeOfGaussian(supportRadius, 0.0, 1.0, 1));
        normalize(gk[1] = new DerivativeOfGaussian(supportRadius, 3.0 * Math.PI / 2.0, 1.0, 1));

        return gk;
    }

    /**
     * Creates a point support matrix. The top row consists of <code>x</code>-coordinates, and the bottom row consists
     * of <code>y</code>-coordinates. The points range in a square where the origin is at the center.
     * 
     * @param supportRadius
     *            the support radius.
     * @return the point support matrix.
     */
    final public static RealArray createPointSupport(int supportRadius) {

        int support = supportRadius * 2 + 1;

        RealArray pts = new RealArray(2, support * support);

        for (int i = 0, n = support * support; i < n; i++) {

            pts.set((i / support) - supportRadius, 0, i);
            pts.set((i % support) - supportRadius, 1, i);
        }

        return pts;
    }

    // Dummy constructor.
    Filters() {
    }
}

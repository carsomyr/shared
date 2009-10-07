/**
 * This file is part of the Shared Scientific Toolbox in Java ("this library"). <br />
 * <br />
 * Copyright (C) 2007 Roy Liu, The Regents of the University of California <br />
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

package shared.image.filter;

import shared.array.AbstractArray;
import shared.array.ComplexArray;
import shared.array.RealArray;

/**
 * A collection of static methods in support of two-dimensional filters.
 * 
 * @author Roy Liu
 */
public class Filters {

    /**
     * Mutatively normalizes a {@link RealArray} by its <tt>L1</tt> norm or, alternatively, a {@link ComplexArray} by
     * its complex magnitudes.
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
     * Creates the <tt>2x2</tt> rotation matrix <br />
     * <tt>-cos(theta) -sin(theta)</tt> <br />
     * <tt>-sin(theta) cos(theta)</tt>. <br />
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
     * Creates a pair of <tt>x</tt> and <tt>y</tt> axis aligned DooG kernels for gradient calculations.
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
     * Creates a point support matrix. The top row consists of <tt>x</tt> coordinates, and the bottom row consists of
     * <tt>y</tt> coordinates. The points range in a square where the origin is at the center.
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

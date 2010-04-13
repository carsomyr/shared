/**
 * This file is part of the Shared Scientific Toolbox in Java ("this library"). <br />
 * <br />
 * Copyright (C) 2008 The Regents of the University of California <br />
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

package shared.image.kernel;

import shared.util.Service;

/**
 * A provider of image processing operations.
 * 
 * @author Roy Liu
 */
public interface ImageKernel extends Service {

    /**
     * Creates an integral image.
     * 
     * @param srcV
     *            the source values.
     * @param srcD
     *            the source dimensions.
     * @param srcS
     *            the source strides.
     * @param dstV
     *            the destination values.
     * @param dstD
     *            the destination dimensions.
     * @param dstS
     *            the destination strides.
     */
    public void createIntegralImage( //
            double[] srcV, int[] srcD, int[] srcS, //
            double[] dstV, int[] dstD, int[] dstS);

    /**
     * Creates an integral histogram.
     * 
     * @param srcV
     *            the source values.
     * @param srcD
     *            the source dimensions.
     * @param srcS
     *            the source strides.
     * @param memV
     *            the class memberships.
     * @param dstV
     *            the destination values.
     * @param dstD
     *            the destination dimensions.
     * @param dstS
     *            the destination strides.
     */
    public void createIntegralHistogram( //
            double[] srcV, int[] srcD, int[] srcS, int[] memV, //
            double[] dstV, int[] dstD, int[] dstS);
}

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

/**
 * A pure Java implementation of {@link ImageKernel}.
 * 
 * @apiviz.uses shared.image.kernel.ImageOps
 * @author Roy Liu
 */
public class JavaImageKernel implements ImageKernel {

    /**
     * Default constructor.
     */
    public JavaImageKernel() {
    }

    public void createIntegralImage( //
            double[] srcV, int[] srcD, int[] srcS, //
            double[] dstV, int[] dstD, int[] dstS) {
        ImageOps.createIntegralImage(srcV, srcD, srcS, dstV, dstD, dstS);
    }

    public void createIntegralHistogram( //
            double[] srcV, int[] srcD, int[] srcS, int[] memV, //
            double[] dstV, int[] dstD, int[] dstS) {
        ImageOps.createIntegralHistogram(srcV, srcD, srcS, memV, dstV, dstD, dstS);
    }
}

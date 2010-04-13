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

import shared.image.jni.NativeImageKernel;
import shared.util.Services;

/**
 * An implementation of {@link ImageKernel} that has JNI and pure Java bindings.
 * 
 * @apiviz.owns shared.image.kernel.JavaImageKernel
 * @author Roy Liu
 */
public class ModalImageKernel implements ImageKernel {

    volatile ImageKernel imKernel;

    /**
     * Default constructor. Tries to create an underlying {@link NativeImageKernel}. Failing that, creates an underlying
     * {@link JavaImageKernel}.
     */
    public ModalImageKernel() {

        this.imKernel = Services.createService(ImageKernel.class);

        if (this.imKernel == null) {
            this.imKernel = new JavaImageKernel();
        }
    }

    /**
     * Uses the underlying {@link NativeImageKernel} obtained from {@link Services#createService(Class)}.
     */
    public boolean useNative() {

        this.imKernel = Services.createService(ImageKernel.class);

        if (this.imKernel == null) {

            this.imKernel = new JavaImageKernel();

            return false;

        } else {

            return true;
        }
    }

    /**
     * Uses the underlying {@link JavaImageKernel}.
     */
    public void useJava() {
        this.imKernel = new JavaImageKernel();
    }

    public void createIntegralImage( //
            double[] srcV, int[] srcD, int[] srcS, //
            double[] dstV, int[] dstD, int[] dstS) {
        this.imKernel.createIntegralImage(srcV, srcD, srcS, dstV, dstD, dstS);
    }

    public void createIntegralHistogram( //
            double[] srcV, int[] srcD, int[] srcS, int[] memV, //
            double[] dstV, int[] dstD, int[] dstS) {
        this.imKernel.createIntegralHistogram(srcV, srcD, srcS, memV, dstV, dstD, dstS);
    }
}

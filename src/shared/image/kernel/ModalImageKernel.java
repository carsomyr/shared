/**
 * <p>
 * Copyright (c) 2008 The Regents of the University of California<br>
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
     * Attempts to use the {@link ImageKernel} obtained from {@link Services#createService(Class)}.
     * 
     * @return {@code true} if and only if an implementation could be obtained without resorting to the default kernel.
     */
    public boolean useRegisteredKernel() {

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

    @Override
    public void createIntegralImage( //
            double[] srcV, int[] srcD, int[] srcS, //
            double[] dstV, int[] dstD, int[] dstS) {
        this.imKernel.createIntegralImage(srcV, srcD, srcS, dstV, dstD, dstS);
    }

    @Override
    public void createIntegralHistogram( //
            double[] srcV, int[] srcD, int[] srcS, int[] memV, //
            double[] dstV, int[] dstD, int[] dstS) {
        this.imKernel.createIntegralHistogram(srcV, srcD, srcS, memV, dstV, dstD, dstS);
    }
}

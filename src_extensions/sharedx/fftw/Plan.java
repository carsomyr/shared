/**
 * <p>
 * Copyright (C) 2006 The Regents of the University of California<br />
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

package sharedx.fftw;

import java.util.Arrays;

/**
 * The Java peer to <a href="http://www.fftw.org/">FFTW3</a> <tt>plan</tt> structures.
 * 
 * @author Roy Liu
 */
public class Plan extends PlanKey {

    // The pointer to the native peer.
    final byte[] memory;

    /**
     * Default constructor.
     * 
     * @param type
     *            the transform type.
     * @param dims
     *            the logical dimensions of the transform.
     * @param mode
     *            the mode of the transform.
     */
    public Plan(int type, int[] dims, int mode) {
        super(type, dims.clone(), mode);

        this.memory = create(this.type, this.dims, this.mode);
    }

    /**
     * Creates a human-readable representation of this plan.
     */
    @Override
    public String toString() {
        return String.format("%s[%s, %s, %s]", //
                Plan.class.getSimpleName(), //
                FFTWService.typeToString(this.type), Arrays.toString(this.dims), //
                FFTWService.modeToString(this.mode));
    }

    /**
     * Real-to-half-complex (RFFT).
     */
    final public static int R2C = 0;

    /**
     * Half-complex-to-real (RIFFT).
     */
    final public static int C2R = 1;

    /**
     * Complex-to-complex forward (FFT).
     */
    final public static int FORWARD = 2;

    /**
     * Complex-to-complex backward (IFFT).
     */
    final public static int BACKWARD = 3;

    /**
     * FFTW execution flag. No precomputation.
     */
    final public static int FFTW_ESTIMATE = 0;

    /**
     * FFTW execution flag. A little bit of precomputation.
     */
    final public static int FFTW_MEASURE = 1;

    /**
     * FFTW execution flag. Heavy precomputation.
     */
    final public static int FFTW_PATIENT = 2;

    /**
     * FFTW execution flag. Extreme precomputation.
     */
    final public static int FFTW_EXHAUSTIVE = 3;

    /**
     * Exports learned wisdom to a string.
     * 
     * @return the wisdom.
     */
    final public native static String exportWisdom();

    /**
     * Imports wisdom from a string.
     * 
     * @param wisdom
     *            the source of wisdom.
     */
    final public native static void importWisdom(String wisdom);

    /**
     * Performs an out-of-place transform.
     * 
     * @param in
     *            the input array.
     * @param out
     *            the output array.
     */
    final public native void transform(double[] in, double[] out);

    /**
     * Creates a pointer to the native peer.
     * 
     * @param type
     *            the transform type.
     * @param dims
     *            the dimensions.
     * @param mode
     *            the transform mode.
     * @return a pointer to the native peer.
     */
    final protected native byte[] create(int type, int[] dims, int mode);

    /**
     * Destroys the native peer.
     */
    final protected native void destroy();

    // The finalizer guardian for the native peer.
    final Object reaper = new Object() {

        @Override
        protected void finalize() {
            Plan.this.destroy();
        }
    };
}

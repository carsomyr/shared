/**
 * This file is part of the Shared Scientific Toolbox in Java ("this library"). <br />
 * <br />
 * Copyright (C) 2006 The Regents of the University of California <br />
 * <br />
 * This library is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License, version 2, as published by the Free Software Foundation. <br />
 * <br />
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. <br />
 * <br />
 * You should have received a copy of the GNU General Public License along with this library. If not, see <a
 * href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
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
     * Complex-to-complex forwards (FFT).
     */
    final public static int FORWARD = 2;

    /**
     * Complex-to-complex backwards (IFFT).
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

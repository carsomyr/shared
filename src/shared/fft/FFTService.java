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

package shared.fft;

import shared.util.Service;

/**
 * Defines a {@link Service} for FFT operations.
 * 
 * @author Roy Liu
 */
public interface FFTService extends Service {

    /**
     * Computes a reduced forwards transform.
     * 
     * @param dims
     *            the dimensions of the transform.
     * @param in
     *            the input array.
     * @param out
     *            the output array.
     */
    public void rfft(int[] dims, double[] in, double[] out);

    /**
     * Computes a reduced backwards transform.
     * 
     * @param dims
     *            the dimensions of the transform.
     * @param in
     *            the input array.
     * @param out
     *            the output array.
     */
    public void rifft(int[] dims, double[] in, double[] out);

    /**
     * Computes a forwards transform.
     * 
     * @param dims
     *            the dimensions of the transform.
     * @param in
     *            the input array.
     * @param out
     *            the output array.
     */
    public void fft(int[] dims, double[] in, double[] out);

    /**
     * Computes a backwards transform.
     * 
     * @param dims
     *            the dimensions of the transform.
     * @param in
     *            the input array.
     * @param out
     *            the output array.
     */
    public void ifft(int[] dims, double[] in, double[] out);

    /**
     * Sets the value of the given hint.
     * 
     * @param name
     *            the hint name.
     * @param value
     *            the value.
     */
    public void setHint(String name, String value);

    /**
     * Gets the value of the given hint.
     * 
     * @param name
     *            the hint name.
     * @return the hint value.
     */
    public String getHint(String name);
}

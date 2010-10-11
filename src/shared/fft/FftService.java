/**
 * <p>
 * Copyright (c) 2007 The Regents of the University of California<br>
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

package shared.fft;

import shared.util.Service;

/**
 * Defines a {@link Service} for FFT operations.
 * 
 * @author Roy Liu
 */
public interface FftService extends Service {

    /**
     * Computes a reduced forward transform.
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
     * Computes a reduced backward transform.
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
     * Computes a forward transform.
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
     * Computes a backward transform.
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

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

package org.shared.image.filter;

import org.shared.array.RealArray;
import org.shared.fft.Cacheable;

/**
 * A mean low-pass filter.
 * 
 * @author Roy Liu
 */
public class Mean extends RealArray implements Cacheable {

    final int hashCode;
    final int width;
    final int height;

    @Override
    public String toString() {
        return String.format("Mean[%d, %d]", //
                this.width, this.height);
    }

    /**
     * Fulfills the {@link #equals(Object)} contract.
     */
    @Override
    public boolean equals(Object o) {

        if (!(o instanceof Mean)) {
            return false;
        }

        Mean m = (Mean) o;
        return this.width == m.width //
                && this.height == m.height;
    }

    /**
     * Fulfills the {@link #hashCode()} contract.
     */
    @Override
    public int hashCode() {
        return this.hashCode;
    }

    /**
     * Default constructor.
     * 
     * @param width
     *            the width.
     * @param height
     *            the height.
     */
    public Mean(int width, int height) {
        super(width, height);

        this.hashCode = new Integer(width).hashCode() //
                ^ new Integer(height).hashCode();

        this.width = width;
        this.height = height;

        uFill(1.0 / (width * height));
    }
}

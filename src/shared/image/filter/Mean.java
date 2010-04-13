/**
 * This file is part of the Shared Scientific Toolbox in Java ("this library"). <br />
 * <br />
 * Copyright (C) 2007 The Regents of the University of California <br />
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

import shared.array.RealArray;
import shared.fft.Cacheable;

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
     * Fulfills the {@link Object#equals(Object)} contract.
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
     * Fulfills the {@link Object#hashCode()} contract.
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

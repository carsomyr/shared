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
 * A base class for {@link Plan} suitable for hashing.
 * 
 * @author Roy Liu
 */
public class PlanKey {

    final int type, mode;
    final int[] dims;

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
    public PlanKey(int type, int[] dims, int mode) {

        this.type = type;
        this.mode = mode;
        this.dims = dims;
    }

    /**
     * Fulfills the {@link Object#equals(Object)} contract.
     */
    @Override
    public boolean equals(Object o) {

        if (!(o instanceof PlanKey)) {
            return false;
        }

        PlanKey p = (PlanKey) o;

        return this.type == p.type //
                && this.mode == p.mode //
                && Arrays.equals(this.dims, p.dims);
    }

    /**
     * Fulfills the {@link Object#hashCode()} contract.
     */
    @Override
    public int hashCode() {
        return this.type ^ this.mode ^ Arrays.hashCode(this.dims);
    }
}

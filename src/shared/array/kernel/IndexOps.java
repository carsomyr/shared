/**
 * This file is part of the Shared Scientific Toolbox in Java ("this library"). <br />
 * <br />
 * Copyright (C) 2009 Roy Liu <br />
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

package shared.array.kernel;

import shared.util.Arithmetic;
import shared.util.Control;

/**
 * A class for indexing operations in pure Java.
 * 
 * @author Roy Liu
 */
public class IndexOps {

    /**
     * An operation in support of {@link JavaArrayKernel#find(int[], int[], int[], int[])}.
     */
    final public static int[] find(int[] srcV, int[] srcD, int[] srcS, int[] logical) {

        int ndims = srcD.length;

        Control.checkTrue(ndims == srcS.length //
                && ndims == logical.length);

        MappingOps.checkDimensions(srcV.length, srcD, srcS);

        int activeDim = Arithmetic.indexOf(logical, -1);

        Control.checkTrue(Arithmetic.count(logical, -1) == 1, //
                "The dimension marker must be unique");

        int offset = 0;

        for (int dim = 0; dim < ndims; dim++) {

            if (dim != activeDim) {

                int index = logical[dim];

                offset += index * srcS[dim];

                Control.checkTrue(index >= 0 && index < srcD[dim], //
                        "Invalid index");
            }
        }

        int upper = 0;
        int size = srcD[activeDim];
        int stride = srcS[activeDim];

        for (int i = 0, physical = offset; i < size; i++, physical += stride) {

            if (srcV[physical] >= 0) {
                upper++;
            }
        }

        int[] res = new int[upper];

        for (int i = 0, physical = offset; i < upper; i++, physical += stride) {
            res[i] = srcV[physical];
        }

        return res;
    }

    // Dummy constructor.
    IndexOps() {
    }
}

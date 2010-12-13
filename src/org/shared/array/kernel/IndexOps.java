/**
 * <p>
 * Copyright (c) 2009 Roy Liu<br>
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

package org.shared.array.kernel;

import org.shared.util.Arithmetic;
import org.shared.util.Control;

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

        int nDims = srcD.length;

        Control.checkTrue(nDims == srcS.length //
                && nDims == logical.length);

        MappingOps.checkDimensions(srcV.length, srcD, srcS);

        int activeDim = Arithmetic.indexOf(logical, -1);

        Control.checkTrue(Arithmetic.count(logical, -1) == 1, //
                "The dimension marker must be unique");

        int offset = 0;

        for (int dim = 0; dim < nDims; dim++) {

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

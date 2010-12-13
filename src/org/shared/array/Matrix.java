/**
 * <p>
 * Copyright (c) 2007 Roy Liu<br>
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

package org.shared.array;

/**
 * Defines functionality that can be expected from all matrices.
 * 
 * @param <T>
 *            the parameterization lower bounded by {@link Matrix} itself.
 * @param <E>
 *            the element type.
 * @author Roy Liu
 */
public interface Matrix<T extends Matrix<T, E>, E> extends Array<T, E> {

    /**
     * Multiplies two matrices.
     * 
     * @param rhs
     *            the right hand side.
     * @return the multiplication result.
     */
    public T mMul(T rhs);

    /**
     * Gets the diagonal of this matrix as a column vector.
     * 
     * @return the diagonal.
     */
    public T mDiag();

    /**
     * Transposes this matrix.
     * 
     * @return the transposed matrix.
     */
    public T mTranspose();

    /**
     * Inverts this matrix.
     * 
     * @return the inverted matrix.
     */
    public T mInvert();

    /**
     * Gets the singular value decomposition of this matrix.
     * 
     * @return the matrices U, S, and V.
     */
    public T[] mSvd();

    /**
     * Gets the eigenvectors and eigenvalues of this matrix.
     * 
     * @return the eigenvectors and eigenvalues.
     */
    public T[] mEigs();
}

/**
 * This file is part of the Shared Scientific Toolbox in Java ("this library"). <br />
 * <br />
 * Copyright (C) 2007 Roy Liu <br />
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

package shared.array;

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
     * @return the matrices "U", "S", and "V".
     */
    public T[] mSVD();

    /**
     * Gets the eigenvectors and eigenvalues of this matrix.
     * 
     * @return the eigenvectors and eigenvalues.
     */
    public T[] mEigs();
}

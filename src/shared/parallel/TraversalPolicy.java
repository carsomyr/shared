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

package shared.parallel;

/**
 * Defines a total ordering and depth assignment to a collection of {@link Traversable}s via
 * {@link Traversable#setOrder(int)} and {@link Traversable#setDepth(int)}. The modifications serve as a hint to
 * {@link Engine} and do not affect the correctness of the calculation.
 * 
 * @apiviz.owns shared.parallel.Traversable
 * @param <V>
 *            the node type.
 * @param <E>
 *            the edge type.
 * @author Roy Liu
 */
public interface TraversalPolicy<V extends Traversable<V, E>, E extends Edge<V>> {

    /**
     * Makes a total ordering and depth assignment to all nodes reachable from the given node.
     * 
     * @param root
     *            the given node.
     * @return the number of nodes visited.
     */
    public int assign(V root);
}

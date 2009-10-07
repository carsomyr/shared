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

import java.util.List;

/**
 * Defines a graph node as part of some total ordering on graph nodes.
 * 
 * @param <V>
 *            the node type.
 * @param <E>
 *            the edge type.
 * @author Roy Liu
 */
public interface Traversable<V extends Traversable<V, E>, E extends Edge<V>> extends Comparable<V> {

    /**
     * Gets the order.
     */
    public int getOrder();

    /**
     * Sets the order.
     */
    public void setOrder(int order);

    /**
     * Gets the depth.
     */
    public int getDepth();

    /**
     * Sets the depth.
     */
    public void setDepth(int depth);

    /**
     * Gets the incoming edges.
     */
    public List<? extends E> getIn();

    /**
     * Gets the outgoing edges.
     */
    public List<? extends E> getOut();

    /**
     * Creates a human-readable representation of this node.
     */
    @Override
    public String toString();
}

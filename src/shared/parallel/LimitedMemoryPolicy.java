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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import shared.parallel.Engine.EngineNode;
import shared.util.Control;

/**
 * An implementation of {@link TraversalPolicy} that attempts to conserve memory usage during parallel execution.
 * 
 * @apiviz.composedOf shared.parallel.LimitedMemoryPolicy.PolicyNode
 * @param <V>
 *            the node type.
 * @param <E>
 *            the edge type.
 * @author Roy Liu
 */
public class LimitedMemoryPolicy<V extends Traversable<V, E>, E extends Edge<V>> implements TraversalPolicy<V, E> {

    /**
     * Default constructor.
     */
    public LimitedMemoryPolicy() {
    }

    public int assign(V root) {
        return assignDepthFirst(longestPath( //
                root, //
                new LinkedHashMap<Traversable<V, E>, PolicyNode>()), //
                new LinkedHashSet<PolicyNode>(), //
                new LinkedHashSet<PolicyNode>(), 0 //
        );
    }

    /**
     * Sorts a {@link PolicyNode}'s children in decreasing order of maximum distance from the source.
     */
    final protected PolicyNode longestPath(Traversable<V, E> curr, Map<Traversable<V, E>, PolicyNode> visitedMap) {

        // Visit the current node by adding a map entry.
        PolicyNode currNode = new PolicyNode(curr);

        visitedMap.put(curr, currNode);

        for (Edge<V> edge : curr.getIn()) {

            V child = edge.getU();

            PolicyNode node = visitedMap.get(child);

            if (node == null) {
                node = longestPath(child, visitedMap);
            }

            currNode.maxDistance = Math.max(currNode.maxDistance, node.maxDistance);
            currNode.children.add(node);
        }

        // Sort in order of decreasing distance.
        Collections.sort(currNode.children);
        Collections.reverse(currNode.children);

        curr.setDepth(currNode.maxDistance++);

        return currNode;
    }

    /**
     * A recursive subroutine for assigning traversal orders while traversing the {@link PolicyNode} graph in a
     * depth-first manner.
     */
    final protected int assignDepthFirst(PolicyNode node, //
            Set<PolicyNode> partiallyVisited, //
            Set<PolicyNode> completelyVisited, //
            int dfsCtr) {

        // Partially visit the current node.
        partiallyVisited.add(node);

        for (PolicyNode child : node.children) {

            Control.checkTrue(!partiallyVisited.contains(child), //
                    "Dependency cycle detected");

            if (!completelyVisited.contains(child)) {
                dfsCtr = assignDepthFirst(child, partiallyVisited, completelyVisited, dfsCtr);
            }
        }

        // Completely visited the current node.
        partiallyVisited.remove(node);
        completelyVisited.add(node);

        node.handle.setOrder(dfsCtr);

        return dfsCtr + 1;
    }

    /**
     * An {@link EngineNode} proxy class that aids in the computation of traversal orderings.
     */
    protected class PolicyNode implements Comparable<PolicyNode> {

        final Traversable<V, E> handle;
        final List<PolicyNode> children;

        int maxDistance;

        /**
         * Default constructor.
         */
        protected PolicyNode(Traversable<V, E> handle) {

            this.handle = handle;
            this.children = new ArrayList<PolicyNode>();

            this.maxDistance = 0;
        }

        /**
         * Compares maximum distance from the source.
         */
        public int compareTo(PolicyNode rhs) {
            return this.maxDistance - rhs.maxDistance;
        }

        /**
         * Delegates to the {@link Traversable}'s {@link Traversable#toString()} method.
         */
        @Override
        public String toString() {
            return this.handle.toString();
        }
    }
}

/**
 * <p>
 * Copyright (C) 2007 Roy Liu<br />
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

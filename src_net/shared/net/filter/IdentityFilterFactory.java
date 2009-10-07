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

package shared.net.filter;

import java.util.Queue;

import shared.net.Connection;

/**
 * An implementation of {@link FilterFactory} that creates identity filters, which simply pull values off of the input
 * queue and push them onto the output queue.
 * 
 * @param <C>
 *            the {@link Connection} type.
 * @author Roy Liu
 */
public class IdentityFilterFactory<T, C extends Connection> implements FilterFactory<T, T, C>, Filter<T, T> {

    /**
     * The global {@link IdentityFilterFactory} instance.
     */
    final protected static IdentityFilterFactory<?, ?> Instance = new IdentityFilterFactory<Object, Connection>();

    /**
     * Gets the global instance.
     */
    @SuppressWarnings("unchecked")
    final public static <T, C extends Connection> FilterFactory<T, T, C> getInstance() {
        return (IdentityFilterFactory<T, C>) Instance;
    }

    /**
     * Default constructor.
     */
    public IdentityFilterFactory() {
    }

    public void getInbound(Queue<T> in, Queue<T> out) {

        for (T inbound = null; (inbound = in.poll()) != null;) {
            out.add(inbound);
        }
    }

    public void getOutbound(Queue<T> in, Queue<T> out) {

        for (T outbound = null; (outbound = in.poll()) != null;) {
            out.add(outbound);
        }
    }

    public Filter<T, T> newFilter(C connection) {
        return this;
    }
}

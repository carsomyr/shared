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

/**
 * Defines a networking I/O filter.
 * 
 * @param <I>
 *            the nominal input type.
 * @param <O>
 *            the nominal output type.
 * @author Roy Liu
 */
public interface Filter<I, O> {

    /**
     * Filters inbound traffic.
     * 
     * @param in
     *            the input {@link Queue}.
     * @param out
     *            the output {@link Queue}.
     */
    public void getInbound(Queue<I> in, Queue<O> out);

    /**
     * Filters outbound traffic.
     * 
     * @param in
     *            the input {@link Queue}.
     * @param out
     *            the output {@link Queue}.
     */
    public void getOutbound(Queue<O> in, Queue<I> out);
}

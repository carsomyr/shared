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

import java.nio.ByteBuffer;
import java.util.Queue;

import shared.net.Connection;

/**
 * Defines a {@link Connection} that uses {@link Filter}s to process inbound and outbound data.
 * 
 * @apiviz.owns shared.net.filter.FilterFactory
 * @param <C>
 *            the parameterization lower bounded by {@link FilteredConnection} itself.
 * @param <T>
 *            the {@link Filter} output type.
 * @author Roy Liu
 */
public interface FilteredConnection<C extends FilteredConnection<C, T>, T> extends Connection {

    /**
     * Sets the {@link FilterFactory} from which appropriate {@link Filter}s will be derived.
     * 
     * @param filterFactory
     *            the {@link FilterFactory}.
     */
    public C setFilterFactory(FilterFactory<ByteBuffer, T, ? super C> filterFactory);

    /**
     * Sends the given outbound value.
     * 
     * @param outbound
     *            the outbound value.
     * @return the number of units remaining to be written.
     */
    public int sendOutbound(T outbound);

    /**
     * Receives the given inbound values.
     * 
     * @param inbounds
     *            the inbound values.
     */
    public void onReceiveInbound(Queue<T> inbounds);

    /**
     * On successful binding.
     * 
     * @param inbounds
     *            the inbound values.
     */
    public void onBindInbound(Queue<T> inbounds);

    /**
     * On user-requested close.
     * 
     * @param inbounds
     *            the inbound values.
     */
    public void onCloseInbound(Queue<T> inbounds);

    /**
     * On end-of-stream.
     * 
     * @param inbounds
     *            the inbound values.
     */
    public void onEOSInbound(Queue<T> inbounds);
}

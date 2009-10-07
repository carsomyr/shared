/**
 * This file is part of the Shared Scientific Toolbox in Java ("this library"). <br />
 * <br />
 * Copyright (C) 2005 Roy Liu <br />
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

package shared.net;

import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Defines a container for {@link Socket} information that would otherwise be hidden by the {@link Connection}
 * interface.
 * 
 * @param <T>
 *            the parameterization lower bounded by {@link SocketInformation} itself.
 * @author Roy Liu
 */
public interface SocketInformation<T extends SocketInformation<T>> {

    /**
     * Gets the local address.
     */
    public InetSocketAddress getLocalAddress();

    /**
     * Gets the remote address.
     */
    public InetSocketAddress getRemoteAddress();

    /**
     * Gets the read/write buffer size.
     */
    public int getBufferSize();

    /**
     * Sets the read/write buffer size.
     */
    public T setBufferSize(int size);

    /**
     * Gets the error, if any, that occurred.
     */
    public Throwable getError();

    /**
     * Checks if this connection has been submitted.
     */
    public boolean isSubmitted();

    /**
     * Checks if this connection has been bound.
     */
    public boolean isBound();

    /**
     * Checks if this connection has been closed.
     */
    public boolean isClosed();
}

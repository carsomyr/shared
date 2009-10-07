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

package shared.util;

import java.util.concurrent.FutureTask;

/**
 * A subclass of {@link FutureTask} specially geared toward asynchronous request-response patterns.
 * 
 * @param <T>
 *            the result type.
 * @author Roy Liu
 */
public class RequestFuture<T> extends FutureTask<T> {

    /**
     * Default constructor.
     */
    public RequestFuture() {
        super(Control.NullRunnable, null);
    }

    /**
     * Gives public visibility to the {@link FutureTask#set(Object)} method.
     */
    @Override
    public void set(T v) {
        super.set(v);
    }

    /**
     * Gives public visibility to the {@link FutureTask#setException(Throwable)} method.
     */
    @Override
    public void setException(Throwable t) {
        super.setException(t);
    }
}

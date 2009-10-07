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

package shared.event;

import java.io.Closeable;

/**
 * Defines an originator of {@link Event}s.
 * 
 * @param <T>
 *            the parameterization lower bounded by {@link Event} itself.
 * @param <S>
 *            the {@link Source} enumeration type.
 * @author Roy Liu
 */
public interface Source<T extends Event<T, ?, S>, S extends Enum<S>> extends SourceLocal<T>, SourceRemote<T>,
        EnumType<S>, Closeable {

    /**
     * Gets the {@link Handler} that will process incoming {@link Event}s.
     */
    public Handler<T> getHandler();

    /**
     * Sets the {@link Handler} that will process incoming {@link Event}s.
     */
    public void setHandler(Handler<T> handler);

    /**
     * Overrides {@link Closeable#close()} so that it doesn't throw an exception.
     */
    public void close();
}

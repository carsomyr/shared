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

import shared.net.Connection;

/**
 * Defines a factory for creating {@link Filter}s.
 * 
 * @apiviz.owns shared.net.filter.Filter
 * @param <F>
 *            the {@link Filter} type.
 * @param <I>
 *            the nominal input type.
 * @param <O>
 *            the nominal output type.
 * @param <C>
 *            the {@link Connection} type.
 * @author Roy Liu
 */
public interface FilterFactory<F extends Filter<I, O>, I, O, C extends Connection> {

    /**
     * Creates a new {@link Filter}.
     */
    public F newFilter(C connection);
}

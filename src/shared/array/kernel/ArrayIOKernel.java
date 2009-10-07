/**
 * This file is part of the Shared Scientific Toolbox in Java ("this library"). <br />
 * <br />
 * Copyright (C) 2008 Roy Liu <br />
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

package shared.array.kernel;

import shared.array.Array;
import shared.util.Service;

/**
 * A provider of {@link Array} I/O operations.
 * 
 * @author Roy Liu
 */
public interface ArrayIOKernel extends Service {

    /**
     * Converts an {@link Array} into {@code byte}s.
     * 
     * @param <T>
     *            the {@link Array} type.
     * @param <E>
     *            the {@link Array} element type.
     * @param array
     *            the {@link Array}.
     * @return the {@code byte}s.
     */
    public <T extends Array<T, E>, E> byte[] getBytes(T array);

    /**
     * Parses an {@link Array} from {@code byte}s.
     * 
     * @param data
     *            the {@code byte}s.
     * @param <T>
     *            the inferred {@link Array} type.
     * @return the {@link Array}.
     */
    public <T extends Array<T, ?>> T parse(byte[] data);
}

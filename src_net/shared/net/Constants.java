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

package shared.net;

/**
 * Contains constant values.
 * 
 * @author Roy Liu
 */
public class Constants {

    /**
     * The default backlog size for newly accepted connections.
     */
    final public static int DEFAULT_BACKLOG_SIZE = 64;

    /**
     * The default read/write buffer size for sockets.
     */
    final public static int DEFAULT_BUFFER_SIZE = 1 << 16;

    // Dummy constructor.
    Constants() {
    }
}

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

package shared.metaclass;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

/**
 * Defines a registry for looking up class path resources.
 * 
 * @author Roy Liu
 */
public interface ResourceRegistry {

    /**
     * Gets a resource {@link URL} corresponding to the given pathname.
     * 
     * @param pathname
     *            the resource pathname.
     * @return a resource {@link URL} or {@code null} if not found.
     */
    public URL getResource(String pathname);

    /**
     * Gets all resource {@link URL}s corresponding to the given pathname.
     * 
     * @param pathname
     *            the resource pathname.
     * @return an {@link Enumeration} of resource {@link URL}s.
     * @throws IOException
     *             when something goes awry.
     */
    public Enumeration<URL> getResources(String pathname) throws IOException;

    /**
     * Gets an {@link InputStream} to the resource at the given pathname.
     * 
     * @param pathname
     *            the resource pathname.
     * @return an {@link InputStream} to the resource or {@code null} if not found.
     */
    public InputStream getResourceAsStream(String pathname);
}

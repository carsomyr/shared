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

package shared.metaclass;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;

/**
 * An implementation of {@link ResourceRegistry} that consults the file system for resources.
 * 
 * @author Roy Liu
 */
public class FileSystemRegistry implements ResourceRegistry {

    final File folder;

    /**
     * Default constructor.
     */
    public FileSystemRegistry(File folder) {
        this.folder = folder;
    }

    public URL getResource(String pathname) {

        File resourceFile = new File(this.folder, pathname);

        if (resourceFile.exists()) {

            try {

                return resourceFile.toURI().toURL();

            } catch (MalformedURLException e) {

                // Ah well.
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public Enumeration<URL> getResources(String pathname) {

        URL url = getResource(pathname);
        return Collections.enumeration((url != null) ? Collections.singleton(url) : Collections.EMPTY_LIST);
    }

    public InputStream getResourceAsStream(String pathname) {

        URL url = getResource(pathname);

        if (url != null) {

            try {

                return url.openStream();

            } catch (IOException e) {

                // Ah well.
            }
        }

        return null;
    }

    /**
     * Creates a human-readable representation of this registry.
     */
    @Override
    public String toString() {

        URL url = null;

        try {

            url = this.folder.toURI().toURL();

        } catch (MalformedURLException e) {

            // Ah well.
        }

        return String.format("%s[url = \"%s\"]", getClass().getSimpleName(), url);
    }
}

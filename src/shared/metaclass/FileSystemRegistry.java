/**
 * <p>
 * Copyright (c) 2009 Roy Liu<br>
 * All rights reserved.
 * </p>
 * <p>
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * </p>
 * <ul>
 * <li>Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.</li>
 * <li>Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution.</li>
 * <li>Neither the name of the author nor the names of any contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.</li>
 * </ul>
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * </p>
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

    @Override
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
    @Override
    public Enumeration<URL> getResources(String pathname) {

        URL url = getResource(pathname);
        return Collections.enumeration((url != null) ? Collections.singleton(url) : Collections.EMPTY_LIST);
    }

    @Override
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

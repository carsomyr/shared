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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;

import shared.util.Control;
import shared.util.TemporaryFiles;

/**
 * An implementation of {@link ResourceRegistry} that consults {@link JarFile}s and {@link JarInputStream}s for
 * resources.
 * 
 * @author Roy Liu
 */
public class JarRegistry implements ResourceRegistry {

    final Map<String, byte[]> dataMap;
    final Map<String, File> fileMap;

    /**
     * Default constructor.
     * 
     * @throws IOException
     *             when something goes awry.
     */
    public JarRegistry(JarFile jf) throws IOException {
        this(createDataMap(jf));
    }

    /**
     * Alternate constructor.
     * 
     * @throws IOException
     *             when something goes awry.
     */
    public JarRegistry(JarInputStream jis) throws IOException {
        this(createDataMap(jis));
    }

    /**
     * Alternate constructor.
     */
    public JarRegistry() {
        throw new UnsupportedOperationException();
    }

    /**
     * Internal constructor.
     */
    protected JarRegistry(Map<String, byte[]> dataMap) {

        this.dataMap = dataMap;
        this.fileMap = new HashMap<String, File>();
    }

    /**
     * Gets the {@link Map} of resource pathnames to {@code byte} arrays.
     */
    public Map<String, byte[]> getDataMap() {
        return this.dataMap;
    }

    public URL getResource(String pathname) {

        final byte[] data = this.dataMap.get(pathname);

        if (data == null) {
            return null;
        }

        synchronized (this) {

            File f = this.fileMap.get(pathname);

            try {

                if (f != null) {
                    return f.toURI().toURL();
                }

                f = File.createTempFile(pathname.replace("/", "_").concat("_"), "");

                this.fileMap.put(pathname, f);
                TemporaryFiles.deleteOnExit(f);

                Control.transfer(data, f);

                return f.toURI().toURL();

            } catch (IOException e) {

                // Return empty-handed if, for some reason, the temporary file could not be created.
                return null;
            }
        }
    }

    @SuppressWarnings("unchecked")
    public Enumeration<URL> getResources(String pathname) {

        URL url = getResource(pathname);
        return Collections.enumeration((url != null) ? Collections.singleton(url) : Collections.EMPTY_LIST);
    }

    public InputStream getResourceAsStream(String pathname) {

        byte[] data = this.dataMap.get(pathname);

        if (data == null) {
            return null;
        }

        return new ByteArrayInputStream(data);
    }

    /**
     * Gets the string "Jar Registry".
     */
    @Override
    public String toString() {
        return String.format("JarRegistry[size = %d]", this.dataMap.size());
    }

    /**
     * Creates a mapping of pathnames to data from a {@link JarInputStream}.
     * 
     * @throws IOException
     *             when something goes awry.
     */
    final protected static Map<String, byte[]> createDataMap(JarInputStream jis) throws IOException {

        HashMap<String, byte[]> map = new HashMap<String, byte[]>();

        for (JarEntry je = null; (je = jis.getNextJarEntry()) != null;) {
            map.put(je.getName(), Control.getBytes(jis));
        }

        return map;
    }

    /**
     * Creates a mapping of pathnames to data from a {@link JarFile}.
     * 
     * @throws IOException
     *             when something goes awry.
     */
    final protected static Map<String, byte[]> createDataMap(JarFile jf) throws IOException {

        HashMap<String, byte[]> map = new HashMap<String, byte[]>();

        for (Enumeration<JarEntry> jes = jf.entries(); jes.hasMoreElements();) {

            JarEntry je = jes.nextElement();
            map.put(je.getName(), Control.getBytes(jf.getInputStream(je)));
        }

        return map;
    }

    // A finalizer guardian for deleting outstanding files.
    final Object fileReaper = new Object() {

        @Override
        protected void finalize() {

            JarRegistry jr = JarRegistry.this;

            synchronized (jr) {

                for (File f : jr.fileMap.values()) {

                    Control.delete(f);
                    TemporaryFiles.undelete(f);
                }
            }
        }
    };
}

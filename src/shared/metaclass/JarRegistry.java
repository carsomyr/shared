/**
 * <p>
 * Copyright (c) 2008 Roy Liu<br>
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

/**
 * An implementation of {@link ResourceRegistry} that consults {@link JarFile}s and {@link JarInputStream}s for
 * resources.
 * 
 * @apiviz.uses shared.metaclass.MetaclassBase
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

    @Override
    public URL getResource(String pathname) {
        return MetaclassBase.getResourceAsTemporaryFile(this, pathname);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Enumeration<URL> getResources(String pathname) {

        URL url = getResource(pathname);
        return Collections.enumeration((url != null) ? Collections.singleton(url) : Collections.EMPTY_LIST);
    }

    @Override
    public InputStream getResourceAsStream(String pathname) {

        byte[] data = this.dataMap.get(pathname);

        if (data == null) {
            return null;
        }

        return new ByteArrayInputStream(data);
    }

    /**
     * Creates a human-readable representation of this registry.
     */
    @Override
    public String toString() {
        return String.format("%s[size = %d]", getClass().getSimpleName(), this.dataMap.size());
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
            map.put(je.getName(), MetaclassBase.getBytes(jis));
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
            map.put(je.getName(), MetaclassBase.getBytes(jf.getInputStream(je)));
        }

        return map;
    }
}

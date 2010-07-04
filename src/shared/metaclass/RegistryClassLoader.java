/**
 * <p>
 * Copyright (C) 2008 Roy Liu<br />
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

import static shared.metaclass.MetaclassBase.close;
import static shared.metaclass.MetaclassBase.getBytes;
import static shared.metaclass.MetaclassBase.getResourceAsTemporaryFile;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.ByteBuffer;
import java.security.SecureClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * A subclass of {@link SecureClassLoader} that derives classes and resource {@link URL}s from a registry delegation
 * chain.
 * 
 * @apiviz.composedOf shared.metaclass.RegistryClassLoader.PrefixNode
 * @apiviz.owns shared.metaclass.JarRegistry
 * @apiviz.owns shared.metaclass.FileSystemRegistry
 * @apiviz.has shared.metaclass.Policy - - - argument
 * @apiviz.uses shared.metaclass.Library
 * @apiviz.uses shared.metaclass.MetaclassBase
 * @author Roy Liu
 */
public class RegistryClassLoader extends SecureClassLoader implements ResourceRegistry {

    final Set<ResourceRegistry> delegates;
    final Set<String> classNames;
    final PrefixNode root;
    final Class<? extends Annotation> policyClass;
    final Method recursiveMethod, includesMethod, loadMethod, loadLibraryMethod;

    /**
     * Default constructor.
     */
    public RegistryClassLoader() {
        this(Thread.currentThread().getContextClassLoader());
    }

    /**
     * Alternate constructor.
     */
    @SuppressWarnings("unchecked")
    public RegistryClassLoader(ClassLoader parent) {
        super(parent);

        this.classNames = new HashSet<String>();
        this.root = new PrefixNode();

        this.delegates = new LinkedHashSet<ResourceRegistry>();

        try {

            this.policyClass = (Class<? extends Annotation>) loadClass("shared.metaclass.Policy", true);
            this.recursiveMethod = this.policyClass.getDeclaredMethod("recursive");
            this.includesMethod = this.policyClass.getDeclaredMethod("includes");

            // Force loading of shared.metaclass.Library by this class loader to effectively make native
            // libraries exclusive to it and not the parent class loader.
            Class<?> clazz = findClass("shared.metaclass.Library");
            this.loadMethod = clazz.getDeclaredMethod("load", String.class);
            this.loadLibraryMethod = clazz.getDeclaredMethod("loadLibrary", String.class);

        } catch (ClassNotFoundException e) {

            throw new RuntimeException(e);

        } catch (NoSuchMethodException e) {

            throw new RuntimeException(e);
        }
    }

    /**
     * Attempts to derive a class first from this class loader's parent, and then from its {@link #findClass(String)}
     * method.
     * 
     * @throws ClassNotFoundException
     *             when the class could not be found.
     */
    @Override
    protected Class<?> loadClass(String className, boolean resolve) throws ClassNotFoundException {

        Class<?> clazz = findLoadedClass(className);

        if (clazz != null) {
            return clazz;
        }

        String[] prefixes = className.split("\\.", -1);
        prefixes = Arrays.copyOf(prefixes, prefixes.length - 1);

        // Check for truncated class name membership to deal with inner classes.
        int index = className.indexOf("$");
        String truncatedClassName = (index >= 0) ? className.substring(0, index) : className;

        if (!this.root.containsPrefixes(prefixes, 0, false) && !this.classNames.contains(truncatedClassName)) {

            return super.loadClass(className, resolve);

        } else {

            clazz = findClass(className);
        }

        if (resolve) {
            resolveClass(clazz);
        }

        return clazz;
    }

    /**
     * Attempts to derive a class from the registry delegation chain.
     * 
     * @throws ClassNotFoundException
     *             when the class could not be found.
     */
    @Override
    protected Class<?> findClass(String className) throws ClassNotFoundException {

        InputStream in = getResourceAsStream(className.replace(".", "/").concat(".class"));

        if (in == null) {
            throw new ClassNotFoundException(String.format("Class '%s' not found", className));
        }

        try {

            return defineClass(className, //
                    ByteBuffer.wrap(getBytes(in)), //
                    RegistryClassLoader.class.getProtectionDomain());

        } catch (IOException e) {

            throw new RuntimeException(e);

        } finally {

            close(in);
        }
    }

    @Override
    protected URL findResource(String pathname) {

        for (ResourceRegistry delegate : this.delegates) {

            URL url = delegate.getResource(pathname);

            if (url != null) {
                return url;
            }
        }

        return null;
    }

    @Override
    protected Enumeration<URL> findResources(String pathname) throws IOException {

        List<URL> urls = new ArrayList<URL>();

        for (ResourceRegistry delegate : this.delegates) {
            urls.addAll(Collections.list(delegate.getResources(pathname)));
        }

        return Collections.enumeration(urls);
    }

    @Override
    public InputStream getResourceAsStream(String pathname) {

        ClassLoader parent = getParent();
        InputStream in = (parent != null) ? parent.getResourceAsStream(pathname) //
                : getSystemResourceAsStream(pathname);

        if (in != null) {
            return in;
        }

        for (ResourceRegistry delegate : this.delegates) {

            in = delegate.getResourceAsStream(pathname);

            if (in != null) {
                return in;
            }
        }

        return null;
    }

    /**
     * Attempts to load a native library first from the class path, and then from the system's dynamic linker.
     */
    public void loadLibrary(String libraryName) {

        String pathname = libraryName.replace(".", "/");

        int index = pathname.lastIndexOf("/");

        String librarySymbolicName = pathname.substring(index + 1);
        pathname = pathname.substring(0, index + 1).concat(System.mapLibraryName(librarySymbolicName));

        URL url = getResource(pathname);

        // Attempt to load from a file, if found.
        if (url != null) {

            String filename = //
            (url.getProtocol().equals("file") ? url : getResourceAsTemporaryFile(this, pathname)).getPath();

            try {

                this.loadMethod.invoke(null, filename);

            } catch (IllegalAccessException e) {

                throw new RuntimeException(e);

            } catch (InvocationTargetException e) {

                throw new RuntimeException(e);
            }
        }
        // Fall back on the system's dynamic linker for resolution.
        else {

            try {

                this.loadLibraryMethod.invoke(null, librarySymbolicName);

            } catch (IllegalAccessException e) {

                throw new RuntimeException(e);

            } catch (InvocationTargetException e) {

                throw new RuntimeException(String.format("Library '%s' not found " //
                        + "and dynamic linker resolution of '%s' failed", pathname, librarySymbolicName), e);
            }
        }
    }

    /**
     * Adds a registry to the delegation chain.
     */
    public RegistryClassLoader addRegistry(ResourceRegistry registry) {

        this.delegates.add(registry);

        return this;
    }

    /**
     * Adds a class which requires, for linking purposes, resources exclusive to this class loader.
     */
    public RegistryClassLoader addClass(String className) {

        Class<?> clazz = findLoadedClass(className);

        try {

            if (clazz == null) {
                clazz = findClass(className);
            }

        } catch (ClassNotFoundException e) {

            throw new RuntimeException(e);
        }

        this.classNames.add(className);

        return this;
    }

    /**
     * Adds a package whose classes require, for linking purposes, resources exclusive to this class loader.
     */
    public RegistryClassLoader addPackage(String packageName) {

        if (packageName == null || packageName.length() == 0) {
            throw new IllegalArgumentException("Invalid package name");
        }

        String className = packageName.concat(".package-info");
        String[] prefixes = packageName.split("\\.", -1);

        Class<?> clazz = findLoadedClass(className);

        try {

            if (clazz == null) {
                clazz = findClass(className);
            }

        } catch (ClassNotFoundException e) {

            throw new RuntimeException(e);
        }

        Annotation policy = clazz.getAnnotation(this.policyClass);

        if (policy == null) {
            throw new IllegalArgumentException("Package is not annotated with a class loading policy");
        }

        final String[] includes;

        try {

            includes = (String[]) this.includesMethod.invoke(policy);

        } catch (IllegalAccessException e) {

            throw new RuntimeException(e);

        } catch (InvocationTargetException e) {

            throw new RuntimeException(e);
        }

        for (String include : includes) {

            String[] split = include.split("#", -1);

            switch (split.length) {

            case 1:
                addPackage(split[0]);
                break;

            case 2:
                addClass(!split[0].equals("") ? String.format("%s.%s", split[0], split[1]) : split[1]);
                break;

            default:
                throw new IllegalArgumentException("Invalid include syntax");
            }
        }

        this.root.addPrefixes(prefixes, 0, policy);

        return this;
    }

    /**
     * Creates a human-readable representation of this class loader.
     */
    @Override
    public String toString() {

        Formatter f = new Formatter();

        f.format("%s(%s)%n%n", //
                RegistryClassLoader.class.getName(), getParent());

        for (ResourceRegistry delegate : this.delegates) {
            f.format("%s%n", delegate.toString());
        }

        f.format("%n%s", this.root.toString());

        return f.toString();
    }

    /**
     * A node class for policy hierarchy lookups.
     */
    protected class PrefixNode {

        final Map<String, PrefixNode> prefixMap;

        Annotation policy;

        /**
         * Default constructor.
         */
        protected PrefixNode() {

            this.prefixMap = new HashMap<String, PrefixNode>();
            this.policy = null;
        }

        /**
         * Adds the given series of prefixes to the policy hierarchy.
         */
        protected void addPrefixes(String[] prefixes, int index, Annotation policy) {

            if (index < prefixes.length) {

                String currentPrefix = prefixes[index];

                for (Entry<String, PrefixNode> entry : this.prefixMap.entrySet()) {

                    String prefix = entry.getKey();
                    PrefixNode node = entry.getValue();

                    if (currentPrefix.equals(prefix)) {

                        node.addPrefixes(prefixes, index + 1, policy);

                        return;
                    }
                }

                PrefixNode node = new PrefixNode();
                node.addPrefixes(prefixes, index + 1, policy);

                this.prefixMap.put(currentPrefix, node);

            } else {

                this.policy = policy;
            }
        }

        /**
         * Checks if the given series of prefixes is contained in the policy hierarchy.
         */
        protected boolean containsPrefixes(String[] prefixes, int index, boolean recursive) {

            if (index < prefixes.length) {

                String currentPrefix = prefixes[index];

                for (Entry<String, PrefixNode> entry : this.prefixMap.entrySet()) {

                    String prefix = entry.getKey();
                    PrefixNode node = entry.getValue();

                    if (currentPrefix.equals(prefix)) {

                        if (node.policy != null) {

                            try {

                                recursive = (Boolean) RegistryClassLoader.this.recursiveMethod.invoke(node.policy);

                            } catch (IllegalAccessException e) {

                                throw new RuntimeException(e);

                            } catch (InvocationTargetException e) {

                                throw new RuntimeException(e);
                            }
                        }

                        return node.containsPrefixes(prefixes, index + 1, recursive);
                    }
                }

                return recursive;

            } else {

                // Accept if the prefix search terminates at a policy node.
                return (this.policy != null) || recursive;
            }
        }

        /**
         * Delegates to {@link PrefixNode#format(Formatter, String)}.
         */
        @Override
        public String toString() {

            Formatter f = new Formatter();
            format(f, "");

            return f.toString();
        }

        /**
         * Gets a string representation of this node's descendants.
         */
        protected void format(Formatter f, String indent) {

            for (Entry<String, PrefixNode> entry : this.prefixMap.entrySet()) {

                String prefix = entry.getKey();
                PrefixNode node = entry.getValue();

                f.format("%s%s%s%n", indent, prefix, //
                        (node.policy != null) ? ":".concat(node.policy.toString()) : "");

                node.format(f, indent.concat("\t"));
            }
        }
    }
}

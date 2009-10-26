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

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.ByteBuffer;
import java.security.SecureClassLoader;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.Map.Entry;

/**
 * A subclass of {@link SecureClassLoader} that derives classes and resource {@link URL}s from a registry delegation
 * chain.
 * 
 * @apiviz.composedOf shared.metaclass.RegistryClassLoader.PrefixNode
 * @apiviz.owns shared.metaclass.JarRegistry
 * @apiviz.owns shared.metaclass.FileSystemRegistry
 * @apiviz.has shared.metaclass.Policy - - - argument
 * @apiviz.uses shared.metaclass.Library
 * @author Roy Liu
 */
public class RegistryClassLoader extends SecureClassLoader implements ResourceRegistry {

    /**
     * The bulk transfer size for {@link InputStream}s and {@link OutputStream}s.
     */
    final protected static int BULK_TRANSFER_SIZE = 1 << 8;

    /**
     * A weak mapping from {@link ResourceRegistry}s to {@link TemporaryFileSet}s.
     */
    final protected static Map<ResourceRegistry, TemporaryFileSet> RRToTFSMap = new WeakHashMap<ResourceRegistry, TemporaryFileSet>();

    /**
     * A weak set of {@link TemporaryFileSet}s.
     */
    final protected static Set<TemporaryFileSet> TFSs = //
    Collections.newSetFromMap(new WeakHashMap<TemporaryFileSet, Boolean>());

    static {

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

            public void run() {

                Set<TemporaryFileSet> reapSets = new HashSet<TemporaryFileSet>();
                reapSets.addAll(RRToTFSMap.values());
                reapSets.addAll(TFSs);

                for (TemporaryFileSet files : reapSets) {
                    files.run();
                }
            }

        }, "Temporary File Deletion Hook"));
    }

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
    public URL getResource(String pathname) {

        ClassLoader parent = getParent();
        URL url = (parent != null) ? parent.getResource(pathname) //
                : getSystemResource(pathname);

        if (url != null) {
            return url;
        }

        for (ResourceRegistry delegate : this.delegates) {

            url = delegate.getResource(pathname);

            if (url != null) {
                return url;
            }
        }

        return null;
    }

    @Override
    public Enumeration<URL> getResources(String pathname) throws IOException {

        ClassLoader parent = getParent();
        List<URL> urls = Collections.list((parent != null) ? parent.getResources(pathname) //
                : getSystemResources(pathname));

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
     * A {@link Closeable#close()} convenience wrapper.
     */
    final protected static void close(Closeable closeable) {

        if (closeable != null) {

            try {

                closeable.close();

            } catch (IOException e) {

                // Do nothing.
            }
        }
    }

    /**
     * Transfers the contents of one stream into another.
     * 
     * @param in
     *            the {@link InputStream}.
     * @param out
     *            the {@link OutputStream}.
     * @throws IOException
     *             when something goes awry.
     */
    final protected static void transfer(InputStream in, OutputStream out) throws IOException {

        byte[] transferBuf = new byte[BULK_TRANSFER_SIZE];

        for (int size; (size = in.read(transferBuf)) >= 0;) {
            out.write(transferBuf, 0, size);
        }

        out.flush();
    }

    /**
     * Gets a {@code byte} array from the given stream.
     * 
     * @param in
     *            the {@link InputStream}.
     * @return the {@code byte} array.
     * @throws IOException
     *             when something goes awry.
     */
    final protected static byte[] getBytes(InputStream in) throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        transfer(in, out);

        return out.toByteArray();
    }

    /**
     * Gets a resource from the given registry as a temporary file.
     */
    final protected static URL getResourceAsTemporaryFile(ResourceRegistry registry, String pathname) {

        InputStream in = registry.getResourceAsStream(pathname);

        if (in == null) {
            return null;
        }

        try {

            File f = File.createTempFile(pathname.replace("/", "_").concat("_"), "");

            synchronized (TemporaryFileSet.class) {

                TemporaryFileSet files = RRToTFSMap.get(registry);

                if (files == null) {

                    files = new TemporaryFileSet();
                    RRToTFSMap.put(registry, files);
                    TFSs.add(files);
                }

                files.add(f);
            }

            FileOutputStream out = new FileOutputStream(f);

            try {

                transfer(in, out);

            } finally {

                close(out);
            }

            return f.toURI().toURL();

        } catch (IOException e) {

            throw new RuntimeException(e);

        } finally {

            close(in);
        }
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

    /**
     * A subclass of {@link LinkedHashSet} that holds temporary files and doubles as a deletion hook.
     */
    @SuppressWarnings("serial")
    protected static class TemporaryFileSet extends LinkedHashSet<File> implements Runnable {

        /**
         * Default constructor.
         */
        protected TemporaryFileSet() {
        }

        /**
         * Deletes all contained temporary files.
         */
        public void run() {

            for (File f : this) {
                f.delete();
            }
        }

        // A finalizer guardian for the set.
        final Object guardian = new Object() {

            @Override
            public void finalize() {
                run();
            }
        };
    }
}

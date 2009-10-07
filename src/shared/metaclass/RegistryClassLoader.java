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

import static shared.util.Control.NullRunnable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
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
import java.util.Map.Entry;

import shared.util.Control;
import shared.util.Finalizable;

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
public class RegistryClassLoader extends SecureClassLoader implements ResourceRegistry,
        Finalizable<RegistryClassLoader> {

    final Set<ResourceRegistry> delegates;
    final Set<String> classNames;
    final PrefixNode root;
    final Class<? extends Annotation> policyClass;
    final Method recursiveMethod, includesMethod, loadMethod, loadLibraryMethod;

    volatile Runnable finalizer;

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
        this.finalizer = NullRunnable;

        try {

            this.policyClass = (Class<? extends Annotation>) loadClass("shared.metaclass.Policy", true);
            this.recursiveMethod = this.policyClass.getDeclaredMethod("recursive");
            this.includesMethod = this.policyClass.getDeclaredMethod("includes");

            // Force loading of shared.metaclass.Library by this class loader to effectively make native
            // libraries exclusive to it and not the parent class loader.
            Class<?> clazz = findClass("shared.metaclass.Library");
            this.loadMethod = clazz.getDeclaredMethod("load", File.class);
            this.loadLibraryMethod = clazz.getDeclaredMethod("loadLibrary", String.class);

        } catch (Exception e) {

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

        final String[] prefixes;

        if (className.contains(".")) {

            String[] split = className.split("\\.", -1);
            prefixes = Arrays.copyOf(split, split.length - 1);

        } else {

            prefixes = new String[] {};
        }

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

        byte[] classData = getClassBytes(className);

        if (classData == null) {
            throw new ClassNotFoundException(String.format("Class '%s' not found", className));
        }

        return defineClass(className, //
                ByteBuffer.wrap(classData), //
                RegistryClassLoader.class.getProtectionDomain());
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

    public RegistryClassLoader setFinalizer(Runnable finalizer) {

        Control.checkTrue(finalizer != null, //
                "Finalizer must be non-null");

        this.finalizer = finalizer;

        return this;
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

        Control.checkTrue(packageName != null && !packageName.equals(""), //
                "Invalid package name");

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

        Control.checkTrue(policy != null, //
                "Package is not annotated with a class loading policy");

        final String[] includes;

        try {

            includes = (String[]) this.includesMethod.invoke(policy);

        } catch (Exception e) {

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
     * Loads a native library from the given file.
     */
    public RegistryClassLoader load(File libFile) {

        try {

            this.loadMethod.invoke(null, libFile);

        } catch (Exception e) {

            throw new RuntimeException(e);
        }

        return this;
    }

    /**
     * Loads a native library from dynamic linker resolution of the given name.
     */
    public RegistryClassLoader loadLibrary(String libName) {

        try {

            this.loadLibraryMethod.invoke(null, libName);

        } catch (Exception e) {

            throw new RuntimeException(e);
        }

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
     * Gets bytecodes associated with the given class name.
     */
    protected byte[] getClassBytes(String className) {

        String pathname = className.replace(".", "/").concat(".class");
        ClassLoader parent = getParent();

        byte[] classBytes = getBytes((parent != null) ? parent.getResourceAsStream(pathname) //
                : getSystemResourceAsStream(pathname));

        if (classBytes != null) {
            return classBytes;
        }

        for (ResourceRegistry delegate : this.delegates) {

            classBytes = getBytes(delegate.getResourceAsStream(pathname));

            if (classBytes != null) {
                return classBytes;
            }
        }

        return null;
    }

    /**
     * Gets a {@code byte} array from the given {@link InputStream}.
     */
    final protected static byte[] getBytes(InputStream in) {

        if (in == null) {
            return null;
        }

        final byte[] classBytes;

        try {

            classBytes = Control.getBytes(in);

        } catch (IOException e) {

            throw new RuntimeException(e);
        }

        return (classBytes != null && classBytes.length > 0) ? classBytes : null;
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

                                recursive = (Boolean) RegistryClassLoader.this.recursiveMethod //
                                        .invoke(node.policy);

                            } catch (Exception e) {

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

    // A finalizer guardian for the class loader.
    final Object reaper = new Object() {

        @Override
        protected void finalize() {
            RegistryClassLoader.this.finalizer.run();
        }
    };
}

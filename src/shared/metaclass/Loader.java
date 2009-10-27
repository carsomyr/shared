/**
 * This file is part of the Shared Scientific Toolbox in Java ("this library"). <br />
 * <br />
 * Copyright (C) 2007 Roy Liu <br />
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
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarInputStream;

/**
 * A facility providing a way for programs to take advantage of Jars and native libraries found on the class path. To
 * use it, one annotates a target class with resource descriptions specifying the Jars and native libraries to be
 * loaded.
 * 
 * @apiviz.owns shared.metaclass.RegistryClassLoader
 * @apiviz.has shared.metaclass.Loader.LoadableResources - - - argument
 * @apiviz.has shared.metaclass.Loader.EntryPoint - - - argument
 * @author Roy Liu
 */
public class Loader {

    /**
     * An annotation in support of resource loading.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface LoadableResources {

        /**
         * Gets the resource descriptions.
         */
        public String[] resources();

        /**
         * Gets the names of packages whose classes require said resources for linking.
         */
        public String[] packages();
    }

    /**
     * A marker annotation for the entry point of a program after resource acquisition.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface EntryPoint {
    }

    /**
     * The entry point for {@link Loader} when invoked as a Java process.
     * 
     * @exception Exception
     *                when something goes awry.
     */
    public static void main(String[] args) throws Exception {

        if (args.length == 0) {
            throw new IllegalArgumentException("Please specify a class to load");
        }

        run(args[0], Arrays.copyOfRange(args, 1, args.length), true);
    }

    /**
     * Delegates to {@link #run(String, Object, boolean)} with the class loader delegation flag set to {@code true}.
     * 
     * @exception Exception
     *                when something goes awry.
     */
    public static void run(String targetName, Object invocationArg) throws Exception {
        run(targetName, invocationArg, true);
    }

    /**
     * The entry point for {@link Loader} when invoked programmatically.
     * 
     * @param targetName
     *            the target class.
     * @param invocationArg
     *            the invocation argument.
     * @param useDelegation
     *            whether to use the class loader gotten by {@link Thread#getContextClassLoader()} or {@code null} as
     *            the underlying {@link RegistryClassLoader}'s parent.
     * @throws Exception
     *             when something goes awry.
     */
    @SuppressWarnings("unchecked")
    public static void run(String targetName, Object invocationArg, boolean useDelegation) throws Exception {

        Thread currentThread = Thread.currentThread();
        ClassLoader cl = currentThread.getContextClassLoader();

        // Retrieve the annotations WITHOUT initializing the class, which may depend on stuff yet to be loaded.
        LoadableResources resources = Class.forName(targetName, false, cl) //
                .getAnnotation(LoadableResources.class);

        if (resources == null) {
            throw new IllegalArgumentException("Resource annotations not declared");
        }

        //

        Map<String, List<String>> resourceNamesMap = new HashMap<String, List<String>>();

        for (String type : new String[] { "jar", "folder", "native" }) {
            resourceNamesMap.put(type, new ArrayList<String>());
        }

        for (String resource : resources.resources()) {

            int index = resource.indexOf(":");

            if (index == -1) {
                throw new IllegalArgumentException("Invalid resource description");
            }

            String resourceType = resource.substring(0, index);
            String resourceName = resource.substring(index + 1);

            List<String> resourceNames = resourceNamesMap.get(resourceType);

            if (resourceNames == null) {
                throw new IllegalArgumentException("Invalid resource type");
            }

            resourceNames.add(resourceName);
        }

        //

        RegistryClassLoader bootstrapCL = new RegistryClassLoader(useDelegation ? cl : null);

        for (String packageName : resources.packages()) {
            bootstrapCL.addPackage(packageName);
        }

        //

        for (String resourceName : resourceNamesMap.get("jar")) {

            String pathname = resourceName.replace(".", "/").concat(".jar");
            InputStream in = cl.getResourceAsStream(pathname);

            if (in == null) {
                throw new IllegalArgumentException(String.format("Jar '%s' not found", pathname));
            }

            bootstrapCL.addRegistry(new JarRegistry(new JarInputStream(in)));
        }

        //

        for (String pathname : resourceNamesMap.get("folder")) {

            File folder = new File(pathname);

            if (!folder.isDirectory()) {
                throw new IllegalArgumentException(String.format("Folder '%s' is not a directory", pathname));
            }

            bootstrapCL.addRegistry(new FileSystemRegistry(folder));
        }

        //

        for (String resourceName : resourceNamesMap.get("native")) {
            bootstrapCL.loadLibrary(resourceName);
        }

        //

        Class<? extends Annotation> entryPointClass = //
        (Class<? extends Annotation>) Class.forName(EntryPoint.class.getName(), true, bootstrapCL);

        Class<?> targetClass = Class.forName(targetName, false, bootstrapCL);

        Method method = null;

        // Find any annotated methods.
        for (Method m : targetClass.getMethods()) {

            if (m.isAnnotationPresent(entryPointClass)) {

                if (method != null) {
                    throw new IllegalArgumentException("Duplicate entry points");
                }

                method = m;
            }
        }

        if (method == null) {
            throw new IllegalArgumentException("Annotated entry point method not found");
        }

        try {

            currentThread.setContextClassLoader(bootstrapCL);

            method.invoke(null, invocationArg);

        } finally {

            currentThread.setContextClassLoader(cl);
        }
    }

    // Dummy constructor.
    Loader() {
    }
}

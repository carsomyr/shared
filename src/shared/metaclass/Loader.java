/**
 * <p>
 * Copyright (c) 2007 Roy Liu<br>
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
 * use it, one annotates a target class with resource descriptions specifying the Jars and native libraries to load.
 * 
 * @apiviz.owns shared.metaclass.RegistryClassLoader
 * @apiviz.has shared.metaclass.Loader.EntryPoint - - - argument
 * @apiviz.has shared.metaclass.Loader.LoadableResources - - - argument
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
         * The resource descriptions.
         */
        public String[] resources();

        /**
         * The names of packages whose classes require said resources for linking.
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
     * Delegates to {@link #run(String, Object, boolean)} with the class loader delegation option set to {@code true}.
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

        RegistryClassLoader bootstrapCl = new RegistryClassLoader(useDelegation ? cl : null);

        for (String packageName : resources.packages()) {
            bootstrapCl.addPackage(packageName);
        }

        //

        for (String resourceName : resourceNamesMap.get("jar")) {

            String pathname = resourceName.replace(".", "/").concat(".jar");
            InputStream in = cl.getResourceAsStream(pathname);

            if (in == null) {
                throw new IllegalArgumentException(String.format("Jar \"%s\" not found", pathname));
            }

            bootstrapCl.addRegistry(new JarRegistry(new JarInputStream(in)));
        }

        //

        for (String pathname : resourceNamesMap.get("folder")) {

            File folder = new File(pathname);

            if (!folder.isDirectory()) {
                throw new IllegalArgumentException(String.format("Folder \"%s\" is not a directory", pathname));
            }

            bootstrapCl.addRegistry(new FileSystemRegistry(folder));
        }

        //

        for (String resourceName : resourceNamesMap.get("native")) {
            bootstrapCl.loadLibrary(resourceName);
        }

        //

        Class<? extends Annotation> entryPointClass = //
        (Class<? extends Annotation>) Class.forName(EntryPoint.class.getName(), true, bootstrapCl);

        Class<?> targetClass = Class.forName(targetName, false, bootstrapCl);

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

            currentThread.setContextClassLoader(bootstrapCl);

            method.invoke(null, invocationArg);

        } finally {

            currentThread.setContextClassLoader(cl);
        }
    }

    // Dummy constructor.
    Loader() {
    }
}

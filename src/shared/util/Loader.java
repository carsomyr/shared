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

package shared.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarInputStream;

import shared.metaclass.FileSystemRegistry;
import shared.metaclass.JarRegistry;
import shared.metaclass.RegistryClassLoader;
import shared.util.LoadableResources.Resource;

/**
 * A facility providing a way for programs to take advantage of Jars and native libraries found on the class path. To
 * use it, one annotates a target class with {@link LoadableResources.Resource}s specifying the Jars and native
 * libraries to be loaded, as well as the {@link Service}s to be registered.
 * 
 * @apiviz.has shared.util.LoadableResources - - - argument
 * @apiviz.has shared.util.Loader.EntryPoint - - - argument
 * @apiviz.uses shared.util.TemporaryFiles
 * @author Roy Liu
 */
public class Loader {

    /**
     * A marker annotation for the entry point of a program <i>after</i> {@link Loader} has acquired the required
     * resources.
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

        Control.checkTrue(args.length > 0, //
                "Please specify class to load");

        start(args[0], Arrays.copyOfRange(args, 1, args.length), true);
    }

    /**
     * Delegates to {@link #start(String, Object, boolean)} with the class loader delegation flag set to {@code true}.
     * 
     * @exception Exception
     *                when something goes awry.
     */
    public static void start(String targetName, Object invocationArg) throws Exception {
        start(targetName, invocationArg, true);
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
    public static void start(String targetName, Object invocationArg, boolean useDelegation) throws Exception {

        List<Resource> jars = new ArrayList<Resource>();
        List<Resource> folders = new ArrayList<Resource>();
        List<Resource> libraries = new ArrayList<Resource>();

        //

        Thread currentThread = Thread.currentThread();
        ClassLoader cl = currentThread.getContextClassLoader();

        // Retrieve the annotations WITHOUT initializing the class, which may depend on stuff yet to be loaded.
        LoadableResources resources = Class.forName(targetName, false, cl) //
                .getAnnotation(LoadableResources.class);

        Control.checkTrue(resources != null, //
                "Resource annotations not declared");

        for (Resource resource : resources.resources()) {

            switch (resource.type()) {

            case NATIVE:
                libraries.add(resource);
                break;

            case JAR:
                jars.add(resource);
                break;

            case FILE_SYSTEM:
                folders.add(resource);
                break;

            default:
                throw new AssertionError("Control should never reach here");
            }
        }

        //

        final List<File> cleanups = new ArrayList<File>();

        RegistryClassLoader bootstrapCL = new RegistryClassLoader(useDelegation ? cl : null) //
                .setFinalizer(new Runnable() {

                    public void run() {

                        for (File f : cleanups) {

                            Control.delete(f);
                            TemporaryFiles.undelete(f);
                        }
                    }
                });

        for (Resource jar : jars) {

            String pathname = String.format("%s/%s.jar", jar.path(), jar.name());

            InputStream in = cl.getResourceAsStream(pathname);

            Control.checkTrue(in != null, //
                    String.format("Resource '%s' not found", pathname));

            bootstrapCL.addRegistry(new JarRegistry(new JarInputStream(in)));
        }

        //

        for (Resource folder : folders) {

            Control.checkTrue(folder.name().equals(""), //
                    "Folders do not have names");

            bootstrapCL.addRegistry(new FileSystemRegistry(new File(folder.path())));
        }

        //

        for (String packageName : resources.packages()) {
            bootstrapCL.addPackage(packageName);
        }

        //

        for (Resource library : libraries) {

            String libName = library.name();
            String pathname = String.format("%s/%s", //
                    library.path(), System.mapLibraryName(libName));

            URL url = cl.getResource(pathname);

            // Attempt to load from a file, if found.
            if (url != null) {

                final File f;

                if (url.getProtocol().equals("file")) {

                    try {

                        f = new File(url.toURI());

                    } catch (URISyntaxException e) {

                        throw new RuntimeException(e);
                    }

                } else {

                    InputStream in = cl.getResourceAsStream(pathname);

                    Control.checkTrue(in != null, //
                            String.format("Resource '%s' not found", pathname));

                    try {

                        f = File.createTempFile(pathname.replace("/", "_").concat("_"), "");

                        cleanups.add(f);
                        TemporaryFiles.deleteOnExit(f);

                        Control.transfer(in, f);

                    } catch (IOException e) {

                        throw new RuntimeException(e);

                    } finally {

                        Control.close(in);
                    }
                }

                bootstrapCL.load(f);
            }
            // Fall back on the dynamic linker for resolution.
            else {

                try {

                    bootstrapCL.loadLibrary(libName);

                } catch (RuntimeException e) {

                    throw new RuntimeException(String.format("The native library resource '%s' was not found " //
                            + "and dynamic linker resolution of '%s' failed", pathname, libName), e);
                }
            }
        }

        //

        Class<? extends Annotation> entryPointClass = //
        (Class<? extends Annotation>) Class.forName(EntryPoint.class.getName(), true, bootstrapCL);

        Class<?> targetClass = Class.forName(targetName, false, bootstrapCL);

        Method method = null;

        // Find any annotated methods.
        for (Method m : targetClass.getMethods()) {

            if (m.isAnnotationPresent(entryPointClass)) {

                Control.checkTrue(method == null, //
                        "Duplicate entry points");

                method = m;
            }
        }

        Control.checkTrue(method != null, //
                "Annotated entry point method not found");

        try {

            currentThread.setContextClassLoader(bootstrapCL);

            method.invoke(null, invocationArg);

        } finally {

            currentThread.setContextClassLoader(cl);
        }
    }

    /**
     * Registers a {@link Service}.
     * 
     * @param <A>
     *            the service type.
     * @param serviceSpec
     *            the service to register.
     */
    @SuppressWarnings("unchecked")
    public static <A extends Service> void registerService(String[] serviceSpec) {

        final Class<A> specClass;
        final Class<? extends A> implClass;

        try {

            specClass = (Class<A>) Class.forName(serviceSpec[0]);
            implClass = (Class<? extends A>) Class.forName(serviceSpec[1]);

        } catch (ClassNotFoundException e) {

            throw new RuntimeException(e);
        }

        Control.checkTrue(Service.class.isAssignableFrom(specClass), //
                "Specification class is not marked as a service");

        Control.checkTrue(specClass.isAssignableFrom(implClass), //
                "Specification class is not a superclass of the implementation class");

        Services.registerService(specClass, implClass);
    }

    // Dummy constructor.
    Loader() {
    }
}

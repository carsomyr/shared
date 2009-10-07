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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation in support of {@link Loader} resource loading.
 * 
 * @apiviz.owns shared.util.LoadableResources.Resource
 * @author Roy Liu
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface LoadableResources {

    /**
     * An enumeration of the types of loadable resources.
     */
    public enum ResourceType {

        /**
         * Denotes a native library.
         */
        NATIVE, //

        /**
         * Denotes a Jar file.
         */
        JAR, //

        /**
         * Denotes a file system folder.
         */
        FILE_SYSTEM;
    }

    /**
     * Gets the resource descriptions.
     */
    public Resource[] resources();

    /**
     * Gets the names of packages whose classes require said resources for linking.
     */
    public String[] packages();

    /**
     * An annotation describing the whereabouts and type of a resource.
     * 
     * @apiviz.owns shared.util.LoadableResources.ResourceType
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Resource {

        /**
         * Gets the resource type.
         */
        public ResourceType type();

        /**
         * Gets the resource name.
         */
        public String name();

        /**
         * Gets the resource path.
         */
        public String path();
    }
}

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

/**
 * A class for interacting with dynamically linked native code.
 * 
 * @author Roy Liu
 */
public class Library {

    /**
     * A flag set on load of the native library.
     */
    protected static boolean INITIALIZED = false;

    /**
     * Checks to see if the native library has been loaded.
     */
    final public static boolean isInitialized() {
        return INITIALIZED;
    }

    /**
     * Loads a native library from the given filename.
     */
    final public static void load(String filename) {
        System.load(filename);
    }

    /**
     * Loads a native library from dynamic linker resolution of the given name.
     */
    final public static void loadLibrary(String libName) {
        System.loadLibrary(libName);
    }

    // Dummy constructor.
    Library() {
    }
}

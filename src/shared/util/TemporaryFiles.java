/**
 * This file is part of the Shared Scientific Toolbox in Java ("this library"). <br />
 * <br />
 * Copyright (C) 2009 Roy Liu <br />
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
import java.util.HashSet;
import java.util.Set;

/**
 * A class whose static methods allow (de)registration of files for deletion upon JVM shutdown.
 * 
 * @author Roy Liu
 */
public class TemporaryFiles {

    /**
     * The set of temporary files under consideration for deletion.
     */
    protected static Set<File> Deletions = new HashSet<File>();

    static {

        Runtime.getRuntime().addShutdownHook(new CoreThread("Temporary File Deletion Hook") {

            @Override
            protected void runUnchecked() {

                synchronized (TemporaryFiles.class) {

                    for (File deletion : Deletions) {
                        Control.delete(deletion);
                    }

                    Deletions = null;
                }
            }
        });
    }

    /**
     * Adds the given file for deletion and serves as an alternative to {@link File#deleteOnExit()}.
     * 
     * @param file
     *            the file.
     * @return the file.
     */
    final public static File deleteOnExit(File file) {

        synchronized (TemporaryFiles.class) {

            if (Deletions != null) {

                Deletions.add(file);

                return file;

            } else {

                throw new IllegalStateException("Shutdown imminent");
            }
        }
    }

    /**
     * Removes the given file from consideration for deletion.
     * 
     * @param file
     *            the file.
     * @return whether the file was under consideration for deletion.
     */
    final public static boolean undelete(File file) {

        synchronized (TemporaryFiles.class) {

            if (Deletions != null) {

                return Deletions.remove(file);

            } else {

                throw new IllegalStateException("Shutdown imminent");
            }
        }
    }

    // Dummy constructor.
    TemporaryFiles() {
    }
}

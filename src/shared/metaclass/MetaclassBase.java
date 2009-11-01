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

package shared.metaclass;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * A collection of useful static methods and infrastructure.
 * 
 * @apiviz.composedOf shared.metaclass.MetaclassBase.TemporaryFileSet
 * @author Roy Liu
 */
public class MetaclassBase {

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

    /**
     * A {@link Closeable#close()} convenience wrapper.
     */
    final public static void close(Closeable closeable) {

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
    final public static void transfer(InputStream in, OutputStream out) throws IOException {

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
    final public static byte[] getBytes(InputStream in) throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        transfer(in, out);

        return out.toByteArray();
    }

    /**
     * Gets a resource from the given registry as a temporary file.
     */
    final public static URL getResourceAsTemporaryFile(ResourceRegistry registry, String pathname) {

        InputStream in = registry.getResourceAsStream(pathname);

        if (in == null) {
            return null;
        }

        try {

            File f = File.createTempFile(pathname.replace("/", "_").concat("_"), "");

            synchronized (MetaclassBase.class) {

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
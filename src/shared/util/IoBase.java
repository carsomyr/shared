/**
 * <p>
 * Copyright (c) 2010 Roy Liu<br>
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

package shared.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A static utility class for I/O operations.
 * 
 * @author Roy Liu
 */
public class IoBase {

    /**
     * The bulk transfer size for {@link InputStream}s and {@link OutputStream}s.
     */
    final protected static int BULK_TRANSFER_SIZE = 1 << 16;

    /**
     * A null {@link OutputStream} to which writes have no effect.
     */
    final public static OutputStream nullOutputStream = new OutputStream() {

        @Override
        public void write(int b) throws IOException {
        }
    };

    /**
     * A null {@link InputStream} that has nothing to read.
     */
    final public static InputStream nullInputStream = new InputStream() {

        @Override
        public int read() throws IOException {
            return -1;
        }
    };

    /**
     * A mapping of environment variables local to the current thread in support of {@link #beginEnvironment()} and
     * {@link #endEnvironment()}.
     */
    final protected static ThreadLocal<Map<String, String>> environmentLocal = new ThreadLocal<Map<String, String>>();

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
     * Deletes a file or directory. If a directory, then recursively deletes the contents while not following symbolic
     * links.
     * 
     * @param f
     *            the file or directory.
     * @return {@code true} if and only if the file or directory was deleted.
     */
    final public static boolean delete(File f) {

        if (f.isDirectory()) {

            // We don't recurse down symlinks.
            if (isSymbolicLink(f)) {
                return false;
            }

            for (File file : f.listFiles()) {
                delete(file);
            }
        }

        return f.delete();
    }

    /**
     * Copies a file or directory. If a directory, then recursively copies the contents while not following symbolic
     * links.
     * 
     * @param src
     *            the source file or directory.
     * @param dst
     *            the destination file or directory.
     * @return {@code true} if and only if the copy operation went smoothly.
     */
    final public static boolean copy(File src, File dst) {

        // We don't operate on symlinks.
        if (isSymbolicLink(src)) {
            return true;
        }

        if (src.isDirectory()) {

            if (dst.exists() || !dst.mkdirs()) {
                return false;
            }

            for (File file : src.listFiles()) {

                if (!copy(file, new File(dst, file.getName()))) {
                    return false;
                }
            }

            return true;

        } else if (src.isFile()) {

            try {

                transfer(src, dst);

            } catch (IOException e) {

                return false;
            }

            if (!dst.setReadable(src.canRead()) //
                    || !dst.setWritable(src.canWrite()) //
                    || !dst.setExecutable(src.canExecute())) {
                return false;
            }

            return true;
        }

        return true;
    }

    /**
     * Gets whether the given file is a symbolic link.
     */
    final public static boolean isSymbolicLink(File f) {

        File parent = f.getParentFile();

        try {

            // The file path itself could contain symlinked directories.
            File normalized = (parent != null) ? new File(parent.getCanonicalFile(), f.getName()) : f;
            return !normalized.getCanonicalFile().equals(normalized.getAbsoluteFile());

        } catch (IOException e) {

            throw new RuntimeException(e);
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
     * Transfers the contents of a file into a stream.
     * 
     * @param inFile
     *            the input {@link File}.
     * @param out
     *            the {@link OutputStream}.
     * @throws IOException
     *             when something goes awry.
     */
    final public static void transfer(File inFile, OutputStream out) throws IOException {

        InputStream in = new FileInputStream(inFile);

        try {

            transfer(in, out);

        } finally {

            close(in);
        }
    }

    /**
     * Transfers the contents of a stream into a file.
     * 
     * @param in
     *            the {@link InputStream}.
     * @param outFile
     *            the output {@link File}.
     * @param append
     *            whether this operation overwrites the file, or merely appends onto the end.
     * @throws IOException
     *             when something goes awry.
     */
    final public static void transfer(InputStream in, File outFile, boolean append) throws IOException {

        OutputStream out = new FileOutputStream(outFile, append);

        try {

            transfer(in, out);

        } finally {

            close(out);
        }
    }

    /**
     * Transfers the contents of a file into a file.
     * 
     * @param inFile
     *            the input {@link File}.
     * @param outFile
     *            the output {@link File}.
     * @param append
     *            whether this operation overwrites the file, or merely appends onto the end.
     * @throws IOException
     *             when something goes awry.
     */
    final public static void transfer(File inFile, File outFile, boolean append) throws IOException {

        InputStream in = new FileInputStream(inFile);

        try {

            transfer(in, outFile, append);

        } finally {

            close(in);
        }
    }

    /**
     * Transfers the contents of a stream into a file without appending.
     * 
     * @param in
     *            the {@link InputStream}.
     * @param outFile
     *            the output {@link File}.
     * @throws IOException
     *             when something goes awry.
     */
    final public static void transfer(InputStream in, File outFile) throws IOException {
        transfer(in, outFile, false);
    }

    /**
     * Transfers the contents of a file into a file without appending.
     * 
     * @param inFile
     *            the input {@link File}.
     * @param outFile
     *            the output {@link File}.
     * @throws IOException
     *             when something goes awry.
     */
    final public static void transfer(File inFile, File outFile) throws IOException {

        InputStream in = new FileInputStream(inFile);

        try {

            transfer(in, outFile, false);

        } finally {

            close(in);
        }
    }

    /**
     * Transfers the contents of a {@code byte} array into a stream.
     * 
     * @param bytes
     *            the {@code byte} array.
     * @param out
     *            the {@link OutputStream}.
     * @throws IOException
     *             when something goes awry.
     */
    final public static void transfer(byte[] bytes, OutputStream out) throws IOException {

        InputStream in = new ByteArrayInputStream(bytes);

        try {

            transfer(in, out);

        } finally {

            close(in);
        }
    }

    /**
     * Transfers the contents of a {@code byte} array into a file.
     * 
     * @param bytes
     *            the {@code byte} array.
     * @param outFile
     *            the output {@link File}.
     * @param append
     *            whether this operation overwrites the file, or merely appends onto the end.
     * @throws IOException
     *             when something goes awry.
     */
    final public static void transfer(byte[] bytes, File outFile, boolean append) throws IOException {

        InputStream in = new ByteArrayInputStream(bytes);

        try {

            transfer(in, outFile, append);

        } finally {

            close(in);
        }
    }

    /**
     * Transfers the contents of a {@code byte} array into a file without appending.
     * 
     * @param bytes
     *            the {@code byte} array.
     * @param outFile
     *            the output {@link File}.
     * @throws IOException
     *             when something goes awry.
     */
    final public static void transfer(byte[] bytes, File outFile) throws IOException {

        InputStream in = new ByteArrayInputStream(bytes);

        try {

            transfer(in, outFile, false);

        } finally {

            close(in);
        }
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
     * Gets a {@code byte} array from the given file.
     * 
     * @param inFile
     *            the input {@link File}.
     * @return the {@code byte} array.
     * @throws IOException
     *             when something goes awry.
     */
    final public static byte[] getBytes(File inFile) throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        transfer(inFile, out);

        return out.toByteArray();
    }

    /**
     * Forks and waits on a new {@link Process}.
     * 
     * @param parentIn
     *            the {@link InputStream} that contains the child's input.
     * @param parentOut
     *            the {@link OutputStream} that receives the child's output.
     * @param parentError
     *            the {@link OutputStream} that receives the child's error output.
     * @param execArgs
     *            the command and arguments to execute.
     * @return the return code of the child process.
     * @throws IOException
     *             when something goes awry.
     */
    final public static int execAndWaitFor( //
            final InputStream parentIn, //
            final OutputStream parentOut, //
            final OutputStream parentError, //
            String... execArgs) throws IOException {

        ProcessBuilder pb = new ProcessBuilder(execArgs);

        Map<String, String> env = environmentLocal.get();

        if (env != null) {
            pb.environment().putAll(env);
        }

        final AtomicReference<IOException> childThreadException = new AtomicReference<IOException>(null);

        Process p = null;

        try {

            p = pb.start();

            final OutputStream out = p.getOutputStream();
            final InputStream in = p.getInputStream();
            final InputStream error = p.getErrorStream();

            Thread t1 = new Thread("Process Writer") {

                @Override
                public void run() {

                    try {

                        transfer(parentIn, out);

                    } catch (IOException e) {

                        childThreadException.compareAndSet(null, e);

                    } finally {

                        // Close the child's input.
                        close(out);
                    }
                }
            };

            t1.start();

            Thread t2 = new Thread("Process Reader") {

                @Override
                public void run() {

                    try {

                        transfer(in, parentOut);

                    } catch (IOException e) {

                        childThreadException.compareAndSet(null, e);
                    }
                }

            };

            t2.start();

            Thread t3 = new Thread("Process Error Reader") {

                @Override
                public void run() {

                    try {

                        transfer(error, parentError);

                    } catch (IOException e) {

                        childThreadException.compareAndSet(null, e);
                    }
                }

            };

            t3.start();

            retry: for (;;) {

                try {

                    int rc = p.waitFor();

                    // Join with the thread that writes to the child's input.
                    t1.join();

                    // Join with the thread that reads from the child's output.
                    t2.join();

                    // Join with the thread that reads from the child's error output.
                    t3.join();

                    IOException ex = childThreadException.get();

                    if (ex != null) {
                        throw ex;
                    }

                    return rc;

                } catch (InterruptedException e) {

                    continue retry;
                }
            }

        } finally {

            if (p != null) {
                p.destroy();
            }
        }
    }

    /**
     * Forks and waits on a new {@link Process}.
     * 
     * @param execArgs
     *            the command and arguments to execute.
     * @return the return code of the child process.
     * @throws IOException
     *             when something goes awry.
     */
    final public static int execAndWaitFor(String... execArgs) throws IOException {
        return execAndWaitFor(nullInputStream, nullOutputStream, nullOutputStream, execArgs);
    }

    /**
     * Forks and waits on a new {@link Process}.
     * 
     * @param parentIn
     *            the {@link InputStream} that contains the child's input.
     * @param execArgs
     *            the command and arguments to execute.
     * @return the return code of the child process.
     * @throws IOException
     *             when something goes awry.
     */
    final public static int execAndWaitFor(InputStream parentIn, String... execArgs) throws IOException {
        return execAndWaitFor(parentIn, nullOutputStream, nullOutputStream, execArgs);
    }

    /**
     * Forks and waits on a new {@link Process}.
     * 
     * @param parentOut
     *            the {@link OutputStream} that receives the child's output.
     * @param execArgs
     *            the command and arguments to execute.
     * @return the return code of the child process.
     * @throws IOException
     *             when something goes awry.
     */
    final public static int execAndWaitFor(OutputStream parentOut, String... execArgs) throws IOException {
        return execAndWaitFor(nullInputStream, parentOut, parentOut, execArgs);
    }

    /**
     * Forks and waits on a new {@link Process}.
     * 
     * @param parentIn
     *            the {@link InputStream} that contains the child's input.
     * @param parentOut
     *            the {@link OutputStream} that receives the child's output.
     * @param execArgs
     *            the command and arguments to execute.
     * @return the return code of the child process.
     * @throws IOException
     *             when something goes awry.
     */
    final public static int execAndWaitFor(InputStream parentIn, OutputStream parentOut, String... execArgs) //
            throws IOException {
        return execAndWaitFor(parentIn, parentOut, parentOut, execArgs);
    }

    /**
     * Begins building the mapping of environment variables for {@link Thread#currentThread()}.
     */
    final public static Map<String, String> beginEnvironment() {

        if (environmentLocal.get() != null) {
            throw new IllegalStateException("Must call endEnvironment() before beginEnvironment()");
        }

        Map<String, String> env = new HashMap<String, String>();

        environmentLocal.set(env);

        return env;
    }

    /**
     * Ends building the mapping of environment variables for {@link Thread#currentThread()}.
     */
    final public static void endEnvironment() {

        if (environmentLocal.get() == null) {
            throw new IllegalStateException("Must call beginEnvironment() before endEnvironment()");
        }

        environmentLocal.set(null);
    }

    // Dummy constructor.
    IoBase() {
    }
}

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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * A collection of useful static methods for control flow.
 * 
 * @author Roy Liu
 */
public class Control {

    /**
     * The bulk transfer size for {@link InputStream}s and {@link OutputStream}s.
     */
    final protected static int BULK_TRANSFER_SIZE = 1 << 16;

    /**
     * A null {@link OutputStream} to which writes have no effect.
     */
    final public static OutputStream NullOutputStream = new OutputStream() {

        @Override
        public void write(int b) throws IOException {
        }
    };

    /**
     * A null {@link InputStream} that has nothing to read.
     */
    final public static InputStream NullInputStream = new InputStream() {

        @Override
        public int read() throws IOException {
            return -1;
        }
    };

    /**
     * A null {@link Runnable} that has an empty {@link Runnable#run()} method.
     */
    final public static Runnable NullRunnable = new Runnable() {

        public void run() {
        }
    };

    /**
     * A timestamp local to the current thread in support of {@link #tick()} and {@link #tock()}.
     */
    final protected static ThreadLocal<Long> TimeStampLocal = new ThreadLocal<Long>();

    /**
     * A mapping of environment variables local to the current thread in support of {@link #beginEnvironment()} and
     * {@link #endEnvironment()}.
     */
    final protected static ThreadLocal<Map<String, String>> EnvironmentLocal = new ThreadLocal<Map<String, String>>();

    /**
     * An implementation of {@link EntityResolver} that finds external entities on the class path of the current
     * thread's context class loader.
     */
    final public static EntityResolver ClasspathResolver = new EntityResolver() {

        public InputSource resolveEntity(String publicId, String systemId) {

            String classpathStr = "classpath://";

            if (!systemId.startsWith(classpathStr)) {
                return null;
            }

            InputStream in = Thread.currentThread().getContextClassLoader() //
                    .getResourceAsStream(systemId.substring(classpathStr.length()));
            return (in != null) ? new InputSource(in) : null;
        }
    };

    /**
     * An implementation of {@link ErrorHandler} that immediately throws any {@link SAXException} passed to it,
     * regardless of severity.
     */
    final public static ErrorHandler DefaultErrorHandler = new ErrorHandler() {

        public void error(SAXParseException exception) throws SAXException {
            throw exception;
        }

        public void fatalError(SAXParseException exception) throws SAXException {
            throw exception;
        }

        public void warning(SAXParseException exception) throws SAXException {
            throw exception;
        }
    };

    /**
     * A {@link DocumentBuilder} local to the current thread.
     */
    final protected static ThreadLocal<DocumentBuilder> BuilderLocal = new ThreadLocal<DocumentBuilder>() {

        @Override
        protected DocumentBuilder initialValue() {

            final DocumentBuilder db;

            try {

                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

                dbf.setValidating(true);
                dbf.setIgnoringElementContentWhitespace(true);

                dbf.setFeature("http://apache.org/xml/features/validation/dynamic", true);

                db = dbf.newDocumentBuilder();
                db.setEntityResolver(ClasspathResolver);
                db.setErrorHandler(DefaultErrorHandler);

            } catch (ParserConfigurationException e) {

                throw new RuntimeException(e);
            }

            return db;
        }
    };

    /**
     * A {@link Transformer} local to the current thread.
     */
    final protected static ThreadLocal<Transformer> TransformerLocal = new ThreadLocal<Transformer>() {

        @Override
        protected Transformer initialValue() {

            try {

                return TransformerFactory.newInstance().newTransformer();

            } catch (TransformerConfigurationException e) {

                throw new RuntimeException(e);
            }
        }
    };

    /**
     * The system-dependent line separator.
     */
    final public static String LineSeparator = System.getProperty("line.separator");

    /**
     * A {@link Closeable#close()} convenience wrapper.
     */
    final public static void close(Closeable closeable) {

        if (closeable != null) {

            try {

                closeable.close();

            } catch (IOException e) {
            }
        }
    }

    /**
     * Sleeps for the given number of milliseconds.
     */
    final public static void sleep(long millis) {

        try {

            Thread.sleep(millis);

        } catch (InterruptedException e) {
        }
    }

    /**
     * Creates an unmodifiable {@link Iterator} from a possibly modifiable one.
     * 
     * @param <T>
     *            the parameterization.
     * @param itr
     *            the original {@link Iterator}.
     * @return an unmodifiable {@link Iterator} view.
     */
    final public static <T> Iterator<T> unmodifiableIterator(final Iterator<T> itr) {

        return new Iterator<T>() {

            public boolean hasNext() {
                return itr.hasNext();
            }

            public T next() {
                return itr.next();
            }

            public void remove() {
                throw new UnsupportedOperationException("Iterator is unmodifiable");
            }
        };
    }

    /**
     * Starts the timer for {@link Thread#currentThread()}.
     */
    final public static void tick() {

        Control.checkTrue(TimeStampLocal.get() == null, //
                "Must call tock() before tick()");

        TimeStampLocal.set(System.currentTimeMillis());
    }

    /**
     * Stops the timer for {@link Thread#currentThread()}.
     * 
     * @return the (wall clock) time elapsed.
     */
    final public static long tock() {

        Long timeStamp = TimeStampLocal.get();

        Control.checkTrue(timeStamp != null, //
                "Must call tick() before tock()");

        TimeStampLocal.set(null);

        return System.currentTimeMillis() - timeStamp;
    }

    /**
     * Deletes a file or directory. If a directory, then recursively deletes the contents while not following symbolic
     * links.
     * 
     * @param f
     *            the file or directory.
     * @return whether the file or directory was deleted.
     */
    final public static boolean delete(File f) {

        if (f.isDirectory()) {

            try {

                File absoluteFile = f.getAbsoluteFile();
                File canonicalFile = f.getCanonicalFile();
                File absoluteParent = absoluteFile.getParentFile().getCanonicalFile();
                File canonicalParent = canonicalFile.getParentFile();

                // The file is a symbolic link if and only if the names don't match and/or the parent
                // directories don't match.
                if (!absoluteParent.equals(canonicalParent) //
                        || !absoluteFile.getName().equals(canonicalFile.getName())) {
                    return false;
                }

                for (File file : f.listFiles()) {
                    delete(file);
                }

            } catch (IOException e) {

                return false;
            }
        }

        return f.delete();
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

        transfer(in, out);

        close(in);
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

        transfer(in, out);

        close(out);
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

        transfer(in, outFile, append);

        close(in);
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

        transfer(in, outFile, false);

        close(in);
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

        transfer(in, out);

        close(in);
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

        transfer(in, outFile, append);

        close(in);
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

        transfer(in, outFile, false);

        close(in);
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
     * Checks if a condition is {@code true}.
     */
    final public static void checkTrue(boolean value) {
        ensureTrue(value, false, "Check failed");
    }

    /**
     * Asserts that a condition is {@code true}.
     */
    final public static void assertTrue(boolean value) {
        ensureTrue(value, true, "Assertion failed");
    }

    /**
     * Checks if two {@code int}s are equal.
     * 
     * @param a
     *            the first argument.
     * @param b
     *            the second argument.
     * @return the equality value, if success.
     */
    final public static int checkEquals(int a, int b) {
        return checkEquals(a, b, "Check failed");
    }

    /**
     * Checks if two objects are equal.
     * 
     * @param <T>
     *            the object parameterization.
     * @param a
     *            the first argument.
     * @param b
     *            the second argument.
     * @return the equality value, if success.
     */
    final public static <T> T checkEquals(T a, T b) {
        return checkEquals(a, b, "Check failed");
    }

    /**
     * Checks if a condition is {@code true}.
     * 
     * @param value
     *            the truth value to check.
     * @param message
     *            the failure message.
     */
    final public static void checkTrue(boolean value, String message) {
        ensureTrue(value, false, message);
    }

    /**
     * Checks if two {@code int}s are equal.
     * 
     * @param a
     *            the first argument.
     * @param b
     *            the second argument.
     * @param message
     *            the failure message.
     * @return the equality value, if success.
     */
    final public static int checkEquals(int a, int b, String message) {

        final int n;

        ensureTrue((n = a) == b, false, message);

        return n;
    }

    /**
     * Checks if two objects are equal.
     * 
     * @param <T>
     *            the object parameterization.
     * @param a
     *            the first argument.
     * @param b
     *            the second argument.
     * @param message
     *            the failure message.
     * @return the equality value, if success.
     */
    final public static <T> T checkEquals(T a, T b, String message) {

        final T val;

        ensureTrue(((val = a) != null) ? a.equals(b) : (b == null), false, message);

        return val;
    }

    /**
     * An internal check/assert method that backs the likes of {@link #checkTrue(boolean)} and
     * {@link #assertTrue(boolean)}.
     * 
     * @param value
     *            the truth value.
     * @param isAssertion
     *            if a failed check results in an {@link AssertionError}.
     * @param message
     *            the failure message.
     * @throws RuntimeException
     *             when a check fails.
     * @throws AssertionError
     *             when an assertion fails.
     */
    final protected static void ensureTrue(boolean value, boolean isAssertion, String message) //
            throws RuntimeException, AssertionError {

        if (!value) {

            if (isAssertion) {

                throw new AssertionError(message);

            } else {

                throw new RuntimeException(message);
            }
        }
    }

    /**
     * Rethrows the given exception. Checked exceptions are wrapped with a {@link RuntimeException}.
     * 
     * @param t
     *            the exception.
     * @throws RuntimeException
     *             when a {@link RuntimeException} has occurred.
     * @throws Error
     *             when an {@link Error} has occurred.
     */
    final public static void rethrow(Throwable t) throws RuntimeException, Error {

        if (t instanceof RuntimeException) {

            throw (RuntimeException) t;

        } else if (t instanceof Error) {

            throw (Error) t;

        } else {

            throw new RuntimeException(t);
        }
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
    final public static int execAndWaitFor(final InputStream parentIn, //
            final OutputStream parentOut, final OutputStream parentError, String... execArgs) throws IOException {

        ProcessBuilder pb = new ProcessBuilder(execArgs);

        Process p = null;

        final AtomicReference<IOException> childThreadException = new AtomicReference<IOException>(null);

        try {

            Map<String, String> env = EnvironmentLocal.get();

            if (env != null) {
                pb.environment().putAll(env);
            }

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
                        Control.close(out);
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
        return execAndWaitFor(NullInputStream, NullOutputStream, NullOutputStream, execArgs);
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
        return execAndWaitFor(parentIn, NullOutputStream, NullOutputStream, execArgs);
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
        return execAndWaitFor(NullInputStream, parentOut, parentOut, execArgs);
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
    final public static int execAndWaitFor(InputStream parentIn, OutputStream parentOut, String... execArgs)
            throws IOException {
        return execAndWaitFor(parentIn, parentOut, parentOut, execArgs);
    }

    /**
     * Begins building the mapping of environment variables for {@link Thread#currentThread()}.
     */
    final public static Map<String, String> beginEnvironment() {

        Control.checkTrue(EnvironmentLocal.get() == null, //
                "Must call endEnvironment() before beginEnvironment()");

        Map<String, String> env = new HashMap<String, String>();

        EnvironmentLocal.set(env);

        return env;
    }

    /**
     * Ends building the mapping of environment variables for {@link Thread#currentThread()}.
     */
    final public static void endEnvironment() {

        Control.checkTrue(EnvironmentLocal.get() != null, //
                "Must call beginEnvironment() before endEnvironment()");

        EnvironmentLocal.set(null);
    }

    /**
     * Creates a pool of daemon worker threads.
     * 
     * @param ncoreThreads
     *            the number of core threads.
     * @param nmaxThreads
     *            the maximum number of threads.
     * @param workQueue
     *            the {@link BlockingQueue} to which work is submitted.
     * @param h
     *            the handler to invoke when a worker thread dies horribly.
     * @return the pool.
     */
    final public static ThreadPoolExecutor createPool(int ncoreThreads, int nmaxThreads, //
            BlockingQueue<Runnable> workQueue, final UncaughtExceptionHandler h) {

        return new ThreadPoolExecutor(ncoreThreads, nmaxThreads, //
                (ncoreThreads < nmaxThreads) ? 60000L : 0L, //
                TimeUnit.MILLISECONDS, workQueue, //

                new ThreadFactory() {

                    volatile int threadCount = 0;

                    public Thread newThread(Runnable r) {

                        Thread t = new Thread(r, String.format("Thread Pool Worker #%d", this.threadCount++));
                        t.setDaemon(true);
                        t.setUncaughtExceptionHandler(h);

                        return t;
                    }
                } //
        );
    }

    /**
     * Creates a pool of daemon worker threads.
     * 
     * @param ncoreThreads
     *            the number of core threads.
     * @return the pool.
     */
    final public static ThreadPoolExecutor createPool(int ncoreThreads) {
        return createPool(ncoreThreads, ncoreThreads, //
                new LinkedBlockingQueue<Runnable>(), null);
    }

    /**
     * Creates a pool of daemon worker threads of size {@link Runtime#availableProcessors()}.
     * 
     * @return the pool.
     */
    final public static ThreadPoolExecutor createPool() {

        int ncoreThreads = Runtime.getRuntime().availableProcessors();
        return createPool(ncoreThreads, ncoreThreads, //
                new LinkedBlockingQueue<Runnable>(), null);
    }

    /**
     * Creates a {@link Document}.
     */
    final public static Document createDocument() {
        return BuilderLocal.get().newDocument();
    }

    /**
     * Creates a {@link Document} from the given {@code byte} array.
     */
    final public static Document createDocument(byte[] array) {
        return createDocument(new ByteArrayInputStream(array));
    }

    /**
     * Creates a {@link Document} from the given string.
     */
    final public static Document createDocument(String data) {
        return createDocument(new ByteArrayInputStream(data.getBytes()));
    }

    /**
     * Creates a {@link Document} from the given file.
     */
    final public static Document createDocument(File file) {

        final FileInputStream fin;

        try {

            fin = new FileInputStream(file);

        } catch (IOException e) {

            throw new RuntimeException(e);
        }

        return createDocument(fin);
    }

    /**
     * Creates a {@link Document} from the given {@link InputStream}.
     */
    final public static Document createDocument(InputStream in) {

        try {

            return BuilderLocal.get().parse(in);

        } catch (Exception e) {

            throw new RuntimeException(e);
        }
    }

    /**
     * Converts the given {@link Document} to a string.
     */
    final public static String toString(Node node) {

        StringWriter sw = new StringWriter();

        try {

            TransformerLocal.get().transform(new DOMSource(node), new StreamResult(sw));

        } catch (TransformerException e) {

            throw new RuntimeException(e);
        }

        return sw.toString();
    }

    // Dummy constructor.
    Control() {
    }
}

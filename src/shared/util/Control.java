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

package shared.util;

import java.util.Iterator;

/**
 * A static utility class for control flow.
 * 
 * @author Roy Liu
 */
public class Control {

    /**
     * A timestamp local to the current thread in support of {@link #tick()} and {@link #tock()}.
     */
    final protected static ThreadLocal<Long> timestampLocal = new ThreadLocal<Long>();

    /**
     * Sleeps for the given number of milliseconds.
     */
    final public static void sleep(long millis) {

        try {

            Thread.sleep(millis);

        } catch (InterruptedException e) {

            // Do nothing.
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

            @Override
            public boolean hasNext() {
                return itr.hasNext();
            }

            @Override
            public T next() {
                return itr.next();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Iterator is unmodifiable");
            }
        };
    }

    /**
     * Starts the timer for {@link Thread#currentThread()}.
     */
    final public static void tick() {

        checkTrue(timestampLocal.get() == null, //
                "Must call tock() before tick()");

        timestampLocal.set(System.currentTimeMillis());
    }

    /**
     * Stops the timer for {@link Thread#currentThread()}.
     * 
     * @return the (wall clock) time elapsed.
     */
    final public static long tock() {

        Long timestamp = timestampLocal.get();

        checkTrue(timestamp != null, //
                "Must call tick() before tock()");

        timestampLocal.set(null);

        return System.currentTimeMillis() - timestamp;
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

    // Dummy constructor.
    Control() {
    }
}

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

/**
 * A thread convenience class that has a {@code try}-{@code catch}-{@code finally} pattern built into its
 * {@link Runnable#run()} method.
 * 
 * @author Roy Liu
 */
abstract public class CoreThread extends Thread {

    /**
     * Default constructor.
     * 
     * @param name
     *            the name of this thread.
     */
    public CoreThread(String name) {
        super(name);
    }

    /**
     * Houses the {@code try}-{@code catch}-{@code finally} pattern.
     */
    @Override
    public void run() {

        try {

            doRun();

        } catch (Throwable t) {

            doCatch(t);

        } finally {

            doFinally();
        }
    }

    /**
     * Runs the main body of code and passes any uncaught exceptions up to the {@link #run()} method.
     * 
     * @throws Exception
     *             the exception to pass upwards.
     */
    abstract protected void doRun() throws Exception;

    /**
     * Runs the exception handler. The default behavior is to rethrow the given exception if unchecked and to throw a
     * {@link RuntimeException} wrapping it if checked.
     */
    protected void doCatch(Throwable t) {

        if (t instanceof RuntimeException) {

            throw (RuntimeException) t;

        } else if (t instanceof Exception) {

            throw new RuntimeException(t);

        } else if (t instanceof Error) {

            throw (Error) t;

        } else {

            throw new AssertionError("Control should never reach here");
        }
    }

    /**
     * Runs the finalizer. Guaranteed to execute on termination of {@link #doRun()}. The default behavior is to do
     * nothing.
     */
    protected void doFinally() {
    }
}

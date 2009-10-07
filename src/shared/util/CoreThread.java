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

            runUnchecked();

        } catch (Throwable t) {

            runCatch(t);

        } finally {

            runFinalizer();
        }
    }

    /**
     * Runs the main body of code and passes any uncaught exceptions up to the {@link Runnable#run()} method.
     * 
     * @throws Exception
     *             the deferred exception.
     */
    abstract protected void runUnchecked() throws Exception;

    /**
     * Runs the exception handler. The default behavior is wrap the given {@link Throwable} in a
     * {@link RuntimeException} and throw that.
     */
    protected void runCatch(Throwable t) {
        Control.rethrow(t);
    }

    /**
     * Runs the finalizer. Guaranteed to execute on termination of {@link #runUnchecked()}. The default behavior is to
     * do nothing.
     */
    protected void runFinalizer() {
    }
}

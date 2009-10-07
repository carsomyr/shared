/**
 * This file is part of the Shared Scientific Toolbox in Java ("this library"). <br />
 * <br />
 * Copyright (C) 2005 Roy Liu <br />
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

package shared.event;

import java.io.Closeable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import shared.util.Control;
import shared.util.CoreThread;
import shared.util.Finalizable;

/**
 * A base class for a thread repetitively going through the event processing motions {@link BlockingQueue#take()}
 * (fetch), {@link Event#getSource()} (see who it's from), {@link Source#getHandler()} (see how the source wishes to
 * process events), and {@link Handler#handle(Object)} (process the event).
 * 
 * @apiviz.composedOf shared.event.Processor.ProcessorThread
 * @param <T>
 *            the parameterization lower bounded by {@link Event} itself.
 * @author Roy Liu
 */
public class Processor<T extends Event<T, ?, ?>> implements SourceLocal<T>, Finalizable<Processor<T>>, Closeable {

    final ProcessorThread<T> thread;

    /**
     * Default constructor.
     */
    public Processor(String name) {
        this.thread = new ProcessorThread<T>(name);
    }

    /**
     * Shuts down the internal thread.
     */
    public void close() {

        this.thread.run = false;
        this.thread.interrupt();
    }

    public void onLocal(T evt) {
        this.thread.eq.add(evt);
    }

    public Processor<T> setFinalizer(Runnable finalizer) {

        Control.checkTrue(finalizer != null, //
                "Finalizer must be non-null");

        this.thread.finalizer = finalizer;

        return this;
    }

    /**
     * Gets the name of the underlying {@link ProcessorThread}.
     */
    @Override
    public String toString() {
        return this.thread.toString();
    }

    /**
     * Checks that {@link Thread#currentThread()} is indeed this processor's internal thread. Helpful for enforcing a
     * serial event processing model.
     */
    public void checkCurrentThread() {
        Control.checkTrue(Thread.currentThread() == this.thread, //
                "Expected call from within processor thread");
    }

    /**
     * A worker thread class for processing events.
     * 
     * @param <T>
     *            the parameterization lower bounded by {@link Event} itself.
     */
    final protected static class ProcessorThread<T extends Event<T, ?, ?>> extends CoreThread {

        final BlockingQueue<T> eq;

        volatile Runnable finalizer;
        volatile boolean run;

        /**
         * Default constructor.
         */
        protected ProcessorThread(String name) {
            super(name);

            this.eq = new LinkedBlockingQueue<T>();

            this.finalizer = Control.NullRunnable;
            this.run = true;

            setDaemon(true);
            start();
        }

        /**
         * Runs the event processing loop.
         */
        @Override
        protected void runUnchecked() {

            for (; this.run;) {

                try {

                    T evt = this.eq.take();
                    evt.getSource().getHandler().handle(evt);

                } catch (InterruptedException e) {
                }
            }
        }

        @Override
        protected void runFinalizer() {
            this.finalizer.run();
        }
    }

    // A finalizer guardian for the processor thread.
    final Object threadReaper = new Object() {

        @Override
        protected void finalize() {
            Control.close(Processor.this);
        }
    };
}

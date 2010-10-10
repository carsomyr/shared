/**
 * <p>
 * Copyright (c) 2005 Roy Liu<br>
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
 *            the {@link Event} type.
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
    @Override
    public void close() {

        this.thread.run = false;
        this.thread.interrupt();
    }

    @Override
    public void onLocal(T evt) {
        this.thread.eq.add(evt);
    }

    @Override
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
                "Expected call from within the processor thread");
    }

    /**
     * A worker thread class for processing events.
     * 
     * @param <T>
     *            the {@link Event} type.
     */
    protected static class ProcessorThread<T extends Event<T, ?, ?>> extends CoreThread {

        /**
         * A null {@link Runnable} that has an empty {@link Runnable#run()} method.
         */
        final protected static Runnable NullRunnable = new Runnable() {

            @Override
            public void run() {
            }
        };

        final BlockingQueue<T> eq;

        volatile Runnable finalizer;
        volatile boolean run;

        /**
         * Default constructor.
         */
        protected ProcessorThread(String name) {
            super(name);

            this.eq = new LinkedBlockingQueue<T>();

            this.finalizer = NullRunnable;
            this.run = true;

            setDaemon(true);
            start();
        }

        /**
         * Runs the event processing loop.
         */
        @Override
        protected void doRun() {

            loop: for (; this.run;) {

                final T evt;

                try {

                    evt = this.eq.take();

                } catch (InterruptedException e) {

                    continue loop;
                }

                evt.getSource().getHandler().handle(evt);
            }
        }

        @Override
        protected void doFinally() {
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

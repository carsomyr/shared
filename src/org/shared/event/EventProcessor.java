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

package org.shared.event;

import java.io.Closeable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A thread going through the event processing motions {@link BlockingQueue#take()} (fetch), {@link Event#getSource()}
 * (see who it's from), {@link Source#getHandler()} (see how the source wishes to process events), and
 * {@link Handler#handle(Object)} (process the event).
 * 
 * @param <T>
 *            the {@link Event} type.
 * @param <E>
 *            the {@link Event} enumeration type.
 * @param <S>
 *            the {@link Source} enumeration type.
 * @author Roy Liu
 */
abstract public class EventProcessor<T extends Event<T, E, S>, E extends Enum<E>, S extends Enum<S>> extends Thread //
        implements Source<T, S>, Handler<T>, Closeable {

    final BlockingQueue<T> eq;

    volatile boolean run;

    /**
     * Default constructor.
     */
    public EventProcessor(String name) {
        super(name);

        this.eq = new LinkedBlockingQueue<T>();

        this.run = true;
    }

    @Override
    public void onLocal(T evt) {
        this.eq.add(evt);
    }

    /**
     * Stops this thread.
     */
    @Override
    public void close() {

        this.run = false;
        interrupt();
    }

    /**
     * Creates a human-readable representation of this thread.
     */
    @Override
    public String toString() {
        return getName();
    }

    /**
     * Checks that {@link Thread#currentThread()} is indeed this thread. Helpful for enforcing a serial event processing
     * model.
     */
    public void checkCurrentThread() {

        if (Thread.currentThread() != this) {
            throw new IllegalStateException("Expected call from within the processor thread");
        }
    }

    /**
     * Runs the event processing loop.
     */
    @Override
    public void run() {

        try {

            loop: for (; this.run;) {

                final T evt;

                try {

                    evt = this.eq.take();

                } catch (InterruptedException e) {

                    continue loop;
                }

                evt.getSource().getHandler().handle(evt);
            }

        } finally {

            doFinally();
        }
    }

    /**
     * Runs the exception handler, should anything go awry in the processing loop. Rethrows the given exception by
     * default.
     */
    protected void doCatch(Throwable t) {

        if (t instanceof RuntimeException) {

            throw (RuntimeException) t;

        } else if (t instanceof Error) {

            throw (Error) t;

        } else {

            throw new AssertionError("Control should never reach here");
        }
    }

    /**
     * Runs the finalizer, which executes on termination of the processing loop and possibly exception handler. Does
     * nothing by default.
     */
    protected void doFinally() {
        // Do nothing.
    }

    @Override
    public Handler<T> getHandler() {
        return this;
    }

    @Override
    public void onRemote(T evt) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setHandler(Handler<T> handler) {
        throw new UnsupportedOperationException();
    }
}

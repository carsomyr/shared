/**
 * <p>
 * Copyright (C) 2007-2010 Roy Liu<br />
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

import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A utility class for reaping {@link Reference}s after their referents have been garbage collected.
 * 
 * @apiviz.composedOf shared.util.ReferenceReaper.ReaperThread
 * @apiviz.has shared.util.ReferenceReaper.ReferenceType - - - argument
 * @param <T>
 *            the referent type.
 * @author Roy Liu
 */
public class ReferenceReaper<T> {

    /**
     * An enumeration of {@link Reference} types.
     */
    public enum ReferenceType {

        /**
         * Indicates a {@link WeakReference}.
         */
        WEAK, //

        /**
         * Indicates a {@link SoftReference}.
         */
        SOFT, //

        /**
         * Indicates a {@link PhantomReference}.
         */
        PHANTOM;
    }

    final ReaperThread<T> thread;

    /**
     * Default constructor.
     */
    public ReferenceReaper() {
        this.thread = new ReaperThread<T>();
    }

    /**
     * Wraps the given object in a {@link Reference} of the requested type.
     * 
     * @param type
     *            the type of {@link Reference} desired.
     * @param referent
     *            the object being referred to.
     * @param cleanup
     *            the cleanup action.
     * @return the wrapping {@link Reference}.
     */
    public Reference<T> wrap(ReferenceType type, T referent, Runnable cleanup) {

        final Reference<T> ref;

        switch (type) {

        case WEAK:

            ref = new WeakReference<T>(referent, this.thread.rq) {

                @Override
                public String toString() {
                    return String.valueOf(get());
                }
            };

            break;

        case SOFT:

            ref = new SoftReference<T>(referent, this.thread.rq) {

                @Override
                public String toString() {
                    return String.valueOf(get());
                }
            };

            break;

        case PHANTOM:

            ref = new PhantomReference<T>(referent, this.thread.rq) {

                @Override
                public String toString() {
                    return String.valueOf(get());
                }
            };

            break;

        default:
            throw new IllegalArgumentException("Invalid reference type");
        }

        this.thread.map.put(ref, cleanup);

        return ref;
    }

    /**
     * A worker thread class for reaping {@link Reference}s.
     * 
     * @param <T>
     *            the referent type.
     */
    final protected static class ReaperThread<T> extends CoreThread {

        final ReferenceQueue<T> rq;
        final ConcurrentMap<Reference<T>, Runnable> map;

        volatile boolean run;

        /**
         * Default constructor.
         */
        protected ReaperThread() {
            super("Reaper");

            this.rq = new ReferenceQueue<T>();
            this.map = new ConcurrentHashMap<Reference<T>, Runnable>();

            this.run = true;

            setDaemon(true);
            start();
        }

        /**
         * Runs the reference reaping loop.
         */
        @Override
        public void runUnchecked() {

            loop: for (; this.run;) {

                final Reference<? extends T> ref;

                try {

                    ref = this.rq.remove();

                } catch (InterruptedException e) {

                    continue loop;
                }

                try {

                    this.map.remove(ref).run();

                } catch (Throwable t) {

                    // In case the cleanup stub acts up.
                }
            }
        }
    }

    // A finalizer guardian for the reaper thread.
    final Object threadReaper = new Object() {

        @Override
        protected void finalize() {

            ReferenceReaper.this.thread.run = false;
            ReferenceReaper.this.thread.interrupt();
        }
    };
}

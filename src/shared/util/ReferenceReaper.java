/**
 * This file is part of the Shared Scientific Toolbox in Java ("this library"). <br />
 * <br />
 * Copyright (C) 2007 Roy Liu, The Regents of the University of California <br />
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

import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A utility class for reaping {@link SoftReference}s and {@link WeakReference}s.
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
     * Wraps the given object in some sort of {@link Reference}.
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

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

package shared.parallel;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Formatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import shared.util.Control;

/**
 * An execution engine class for pushing data through the guts of some parallel computation given as a directed, acyclic
 * graph.
 * 
 * @apiviz.composedOf shared.parallel.Engine.EngineEdge
 * @apiviz.composedOf shared.parallel.Engine.EngineNode
 * @apiviz.composedOf shared.parallel.Engine.ThrowableReferenceHandler
 * @apiviz.owns shared.parallel.TraversalPolicy
 * @param <T>
 *            the input type.
 * @author Roy Liu
 */
public class Engine<T> {

    final Semaphore guard, notifier;
    final ThrowableReferenceHandler exceptionRef;
    final Calculator<? super Object, ? extends T> startCalculator;
    final Calculator<? super Object, ? extends Object> stopCalculator;
    final Map<Calculator<?, ?>, EngineNode<?, ?>> nodeMap;
    final TraversalPolicy<EngineNode<?, ?>, EngineEdge<?>> policy;
    final ThreadPoolExecutor executor;

    boolean valid;

    T engineInput;

    /**
     * Alternate constructor. Creates an engine with the number of threads set to {@link Runtime#availableProcessors()}
     * and {@link LimitedMemoryPolicy} for its {@link TraversalPolicy}.
     */
    public Engine() {
        this(Runtime.getRuntime().availableProcessors());
    }

    /**
     * Alternate constructor. Creates an engine with {@link LimitedMemoryPolicy} for its {@link TraversalPolicy}.
     * 
     * @param nThreads
     *            the number of threads.
     */
    public Engine(int nThreads) {
        this(nThreads, new LimitedMemoryPolicy<EngineNode<?, ?>, EngineEdge<?>>());
    }

    /**
     * Default constructor.
     * 
     * @param nThreads
     *            the number of threads.
     * @param policy
     *            the {@link TraversalPolicy} to apply when ordering nodes.
     */
    public Engine(int nThreads, TraversalPolicy<EngineNode<?, ?>, EngineEdge<?>> policy) {

        this.exceptionRef = new ThrowableReferenceHandler();
        this.executor = Control.createPool(nThreads, nThreads, //
                new PriorityBlockingQueue<Runnable>(), //
                this.exceptionRef);

        this.policy = policy;

        this.guard = new Semaphore(1);
        this.notifier = new Semaphore(0);

        this.nodeMap = new LinkedHashMap<Calculator<?, ?>, EngineNode<?, ?>>();

        // The start calculation that merely propagates the engine's input.
        this.startCalculator = new Calculator<Object, T>() {

            @Override
            public T calculate(List<? extends Handle<? extends Object>> inputVector) {
                return Engine.this.engineInput;
            }

            @Override
            public String toString() {
                return "Start ()";
            }
        };

        this.nodeMap.put(this.startCalculator, //
                new EngineNode<Object, T>(this.startCalculator, false));

        // The token stop calculation that releases the semaphore guard.
        this.stopCalculator = new Calculator<Object, Object>() {

            @Override
            public T calculate(List<? extends Handle<? extends Object>> inputVector) {
                return null;
            }

            @Override
            public String toString() {
                return "Stop ()";
            }
        };

        this.nodeMap.put(this.stopCalculator, //

                new EngineNode<Object, Object>(this.stopCalculator, false) {

                    @Override
                    public void run() {

                        try {

                            super.run();

                        } finally {

                            // Make sure that the notifier semaphore is released no matter what.
                            Engine.this.notifier.release();
                        }
                    }
                });

        this.valid = false;
        this.engineInput = null;
    }

    /**
     * Gets the initial {@link Calculator}, which does nothing aside from repeating input given to it.
     */
    public Calculator<? super Object, ? extends T> getInput() {
        return this.startCalculator;
    }

    /**
     * Adds a non-output {@link Calculator} along with its dependencies.
     * 
     * @see #add(Calculator, boolean, Collection)
     */
    public <I, O> void add( //
            Calculator<I, O> calc, //
            Calculator<?, ? extends I>... calcDeps //
    ) {
        add(calc, false, Arrays.asList(calcDeps));
    }

    /**
     * Adds an output {@link Calculator} along with its dependencies.
     * 
     * @return a {@link Handle} from which potential output can be retrieved.
     * @see #add(Calculator, boolean, Collection)
     */
    public <I, O> Handle<O> addOutput( //
            Calculator<I, O> calc, //
            Calculator<?, ? extends I>... calcDeps //
    ) {
        return add(calc, true, Arrays.asList(calcDeps));
    }

    /**
     * Adds a {@link Calculator} along with its dependencies.
     * 
     * @param <I>
     *            the {@link Calculator} input type.
     * @param <O>
     *            the {@link Calculator} output type.
     * @param calc
     *            the {@link Calculator}.
     * @param hasOutput
     *            whether this node has observable output.
     * @param calcDeps
     *            the dependencies.
     * @return a {@link Handle} from which potential output can be retrieved.
     */
    @SuppressWarnings("unchecked")
    public <I, O> Handle<O> add( //
            Calculator<I, O> calc, boolean hasOutput, //
            Collection<? extends Calculator<?, ? extends I>> calcDeps //
    ) {

        EngineNode<I, O> node = new EngineNode<I, O>(calc, hasOutput);

        Control.checkTrue(calcDeps.size() > 0, //
                "Please specify some dependencies");

        try {

            Control.checkTrue(this.guard.tryAcquire(), //
                    "Operation in progress");

            // The ordering is no longer valid.
            invalidate();

            // Check everything before inserts.
            Control.checkTrue(!this.nodeMap.containsKey(calc), //
                    "Node already exists");

            for (Calculator<?, ? extends I> calcDep : calcDeps) {
                Control.checkTrue(this.nodeMap.containsKey(calcDep), //
                        "Node doesn't exist");
            }

            this.nodeMap.put(calc, node);

            for (Calculator<?, ? extends I> calcDep : calcDeps) {
                addEdge((EngineNode<?, ? extends I>) this.nodeMap.get(calcDep), node);
            }

        } finally {

            this.guard.release();
        }

        return node.hasOutput ? node : null;
    }

    /**
     * Executes with the given input.
     * 
     * @param engineInput
     *            the input.
     */
    public void execute(T engineInput) {

        try {

            Control.checkTrue(this.guard.tryAcquire(), //
                    "Operation in progress");

            validate();

            // Reset reference counts in preparation for the new computation.
            for (EngineNode<?, ?> node : this.nodeMap.values()) {

                // Invariant: Both reference counts should have reached zero.
                Control.assertTrue(node.outRefCount.getAndSet(node.outputs.size()) == 0 //
                        && node.inRefCount.getAndSet(node.inputs.size()) == 0);

                node.set(null);
            }

            // The queue size and the number of permits should be zero.
            Control.assertTrue(this.notifier.availablePermits() == 0 //
                    && this.executor.getQueue().isEmpty());

            // Prime the priority queue with a single element -- the input calculator.
            this.engineInput = engineInput;
            this.executor.execute(this.nodeMap.get(this.startCalculator));

            // Try to acquire a number of permits equal to the number of nodes, thus guaranteeing termination of the
            // computation upon return.
            this.notifier.acquireUninterruptibly();

            Throwable t = this.exceptionRef.getAndSet(null);

            // If the calculation internally encountered a problem.
            if (t != null) {
                Control.rethrow(t);
            }

        } finally {

            this.guard.release();
        }
    }

    /**
     * Outputs human-readable directives for <a href="http://www.graphviz.org/">Dot</a> graph generation.
     */
    @Override
    public String toString() {

        try {

            Control.checkTrue(this.guard.tryAcquire(), //
                    "Operation in progress");

            validate();

            EngineNode<?, ?> root = this.nodeMap.get(this.startCalculator);
            EngineNode<?, ?> sink = this.nodeMap.get(this.stopCalculator);

            Formatter f = new Formatter();

            f.format("%n/* Begin Node Specification */%n%n");
            f.format("\"%s (%d)\" [shape = diamond, color = green];%n", root, root.order);
            f.format("\"%s (%d)\" [shape = diamond, color = green];%n", sink, sink.order);

            List<EngineNode<?, ?>> nodes = new ArrayList<EngineNode<?, ?>>(this.nodeMap.values());
            Collections.sort(nodes);

            for (EngineNode<?, ?> node : nodes) {

                if (node.hasOutput) {

                    f.format("\"%s (%d)\" [shape = octagon, color = red];%n", node, node.order);

                } else if (!node.equals(root) && !node.equals(sink)) {

                    f.format("\"%s (%d)\" [shape = rectangle, color = blue];%n", node, node.order);
                }
            }

            f.format("%n/* Begin Edge Specification */%n%n");

            for (EngineNode<?, ?> node : nodes) {

                for (EngineEdge<?> edge : node.outputs) {

                    for (int i = 0, n = node.depth; i < n; i++) {
                        f.format("\t");
                    }

                    f.format("\"%s (%d)\" -> \"%s (%d)\"%n", //
                            edge.getU(), edge.getU().order, //
                            edge.getV(), edge.getV().order);
                }
            }

            return f.toString();

        } finally {

            this.guard.release();
        }
    }

    /**
     * Computes a traversal ordering over the current configuration of {@link EngineNode}s.
     */
    @SuppressWarnings("unchecked")
    protected void validate() {

        if (!this.valid) {

            Collection<EngineNode<?, ?>> nodes = this.nodeMap.values();

            EngineNode<? super Object, ? extends Object> sink = //
            (EngineNode<? super Object, ? extends Object>) this.nodeMap.get(this.stopCalculator);

            // Unlink the output node. It had better be the case that a child knows about the output node.
            for (EngineEdge<?> edge : sink.inputs) {
                Control.assertTrue(edge.getU().outputs.remove(edge));
            }

            sink.inputs.clear();

            // Attach everything that's a sink to the fake sink.
            for (EngineNode<?, ?> node : nodes) {

                if (node.outputs.size() == 0 && node != sink) {
                    addEdge(node, sink);
                }
            }

            // Assign priority order according to the traversal policy.
            Control.assertTrue(this.policy.assign(sink) == nodes.size());

            this.valid = true;
        }
    }

    /**
     * Invalidates the current traversal ordering.
     */
    protected void invalidate() {
        this.valid = false;
    }

    /**
     * Links two {@link EngineNode}s by an {@link EngineEdge}.
     * 
     * @param <O>
     *            the output-input type.
     */
    protected <O> void addEdge(EngineNode<?, ? extends O> u, EngineNode<? super O, ?> v) {

        EngineEdge<O> e = new EngineEdge<O>(u, v);

        u.outputs.add(e);
        v.inputs.add(e);
    }

    /**
     * A computation node that is part of some topology of nodes.
     * 
     * @apiviz.owns shared.parallel.Calculator
     * @param <I>
     *            the input type.
     * @param <O>
     *            the output type.
     */
    protected class EngineNode<I, O> implements Handle<O>, Runnable, Traversable<EngineNode<?, ?>, EngineEdge<?>> {

        final Calculator<? super I, ? extends O> calculator;
        final List<EngineEdge<? extends I>> inputs;
        final List<EngineEdge<? extends I>> inputsReadOnly;
        final List<EngineEdge<? super O>> outputs;
        final List<EngineEdge<? super O>> outputsReadOnly;

        final AtomicInteger outRefCount, inRefCount;

        final boolean hasOutput;

        int order, depth;

        O value;

        /**
         * Default constructor.
         */
        public EngineNode(Calculator<? super I, ? extends O> calculator, boolean hasOutput) {

            this.calculator = calculator;
            this.hasOutput = hasOutput;

            this.inputs = new ArrayList<EngineEdge<? extends I>>();
            this.inputsReadOnly = Collections.unmodifiableList(this.inputs);
            this.outputs = new ArrayList<EngineEdge<? super O>>();
            this.outputsReadOnly = Collections.unmodifiableList(this.outputs);

            this.outRefCount = new AtomicInteger(0);
            this.inRefCount = new AtomicInteger(0);

            this.order = (this.depth = -1);

            this.value = null;
        }

        /**
         * Compares traversal orders to determine priority of execution.
         */
        @Override
        public int compareTo(EngineNode<?, ?> node) {
            return this.order - node.order;
        }

        @Override
        public O get() {
            return this.value;
        }

        @Override
        public void set(O value) {
            this.value = value;
        }

        @Override
        public int getOrder() {
            return this.order;
        }

        @Override
        public void setOrder(int order) {
            this.order = order;
        }

        @Override
        public int getDepth() {
            return this.depth;
        }

        @Override
        public void setDepth(int depth) {
            this.depth = depth;
        }

        @Override
        public List<EngineEdge<? extends I>> getIn() {
            return this.inputsReadOnly;
        }

        @Override
        public List<EngineEdge<? super O>> getOut() {
            return this.outputsReadOnly;
        }

        @Override
        public String toString() {
            return this.calculator.toString();
        }

        /**
         * Executes the {@link Calculator#calculate(List)} method associated with this node.
         */
        @Override
        public void run() {

            try {

                this.value = this.calculator.calculate(this.inputsReadOnly);

            } catch (Throwable t) {

                Engine.this.exceptionRef.compareAndSet(null, t);

                Control.rethrow(t);

            } finally {

                for (int i = 0, n = this.inputs.size(), val; i < n; i++) {

                    EngineNode<?, ? extends I> node = this.inputs.get(i).getU();

                    // The node has all of its outputs observed; free its value.
                    if ((val = node.outRefCount.decrementAndGet()) == 0) {

                        if (!node.hasOutput) {
                            node.set(null);
                        }

                    } else {

                        Control.assertTrue(val > 0);
                    }
                }

                for (int i = 0, n = this.outputs.size(), val; i < n; i++) {

                    EngineNode<? super O, ?> node = this.outputs.get(i).getV();

                    // The node in question has all of its inputs accounted for; insert it according to its traversal
                    // order.
                    if ((val = node.inRefCount.decrementAndGet()) == 0) {

                        Engine.this.executor.execute(node);

                    } else {

                        Control.assertTrue(val > 0);
                    }
                }
            }
        }
    }

    /**
     * An output-input relationship between two {@link Engine.EngineNode}s.
     * 
     * @param <O>
     *            the output-input type.
     */
    protected class EngineEdge<O> implements Handle<O>, Edge<EngineNode<?, ?>> {

        final EngineNode<?, ? extends O> u;
        final EngineNode<? super O, ?> v;

        /**
         * Default constructor.
         */
        public EngineEdge(EngineNode<?, ? extends O> u, EngineNode<? super O, ?> v) {

            this.u = u;
            this.v = v;
        }

        @Override
        public EngineNode<?, ? extends O> getU() {
            return this.u;
        }

        @Override
        public EngineNode<? super O, ?> getV() {
            return this.v;
        }

        /**
         * Delegates to the start {@link Engine.EngineNode}'s {@link #get()} method.
         */
        @Override
        public O get() {
            return this.u.get();
        }

        @Override
        public void setU(EngineNode<?, ?> node) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setV(EngineNode<?, ?> node) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void set(O output) {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * A subclass of {@link AtomicReference} that doubles as an {@link UncaughtExceptionHandler}. It is set whenever an
     * uncaught {@link Throwable} occurs.
     */
    @SuppressWarnings("serial")
    protected static class ThrowableReferenceHandler extends AtomicReference<Throwable> //
            implements UncaughtExceptionHandler {

        /**
         * Default constructor.
         */
        protected ThrowableReferenceHandler() {
        }

        /**
         * Sets this reference.
         */
        @Override
        public void uncaughtException(Thread thread, Throwable throwable) {

            // Set the exception only if one hasn't already occurred.
            compareAndSet(null, throwable);
        }
    }

    // A finalizer guardian for the thread pool.
    final Object poolReaper = new Object() {

        @Override
        protected void finalize() {
            Engine.this.executor.shutdownNow();
        }
    };
}

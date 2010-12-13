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

package org.shared.test.parallel;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.shared.parallel.Calculator;
import org.shared.parallel.Engine;
import org.shared.parallel.Handle;

/**
 * A class of unit tests for {@link Engine}.
 * 
 * @author Roy Liu
 */
public class EngineTest {

    static class Subtractor implements Calculator<Integer, Integer> {

        @Override
        public Integer calculate(List<? extends Handle<? extends Integer>> inputVector) {
            return inputVector.get(0).get().intValue() - inputVector.get(1).get().intValue();
        }

        @Override
        public String toString() {
            return "-";
        }
    }

    static class Multiplier implements Calculator<Integer, Integer> {

        @Override
        public Integer calculate(List<? extends Handle<? extends Integer>> inputVector) {
            return inputVector.get(0).get().intValue() * inputVector.get(1).get().intValue();
        }

        @Override
        public String toString() {
            return "x";
        }
    }

    static class Doubler implements Calculator<Integer, Integer> {

        @Override
        public Integer calculate(List<? extends Handle<? extends Integer>> inputVector) {
            return inputVector.get(0).get().intValue() << 1;
        }

        @Override
        public String toString() {
            return "2x";
        }
    }

    static class Repeater implements Calculator<Integer, Integer> {

        @Override
        public Integer calculate(List<? extends Handle<? extends Integer>> inputVector) {
            return inputVector.get(0).get();
        }

        @Override
        public String toString() {
            return "I";
        }
    }

    static class Summer implements Calculator<Integer, Integer> {

        @Override
        public Integer calculate(List<? extends Handle<? extends Integer>> inputVector) {

            int sum = 0;

            for (int i = 0; i < inputVector.size(); i++) {
                sum += inputVector.get(i).get().intValue();
            }

            return sum;
        }

        @Override
        public String toString() {
            return "+";
        }
    }

    static class Outputter implements Calculator<Integer, Integer> {

        @Override
        public Integer calculate(List<? extends Handle<? extends Integer>> inputVector) {
            return inputVector.get(0).get();
        }

        @Override
        public String toString() {
            return "O";
        }
    }

    static class Thrower implements Calculator<Integer, Integer> {

        @Override
        public Integer calculate(List<? extends Handle<? extends Integer>> inputVector) {
            throw new IllegalStateException("This exception was artificially induced");
        }

        @Override
        public String toString() {
            return Thrower.class.getSimpleName();
        }
    }

    Engine<Integer> engine;

    /**
     * Default constructor.
     */
    public EngineTest() {
    }

    /**
     * Initializes the underlying {@link Engine}.
     */
    @Before
    public void init() {
        this.engine = new Engine<Integer>();
    }

    /**
     * Makes the underlying {@link Engine} eligible for garbage collection.
     */
    @After
    public void destroy() {
        this.engine = null;
    }

    /**
     * Tests a basic schema.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testExecute1() {

        Calculator<Integer, Integer> r1 = new Repeater();
        Calculator<Integer, Integer> d1 = new Doubler();
        Calculator<Integer, Integer> s2 = new Summer();
        Calculator<Integer, Integer> s1 = new Subtractor();
        Calculator<Integer, Integer> m1 = new Multiplier();
        Calculator<Integer, Integer> a2 = new Summer();
        Calculator<Integer, Integer> d2 = new Doubler();

        Calculator<Integer, Integer> o1 = new Outputter();

        this.engine.add(r1, this.engine.getInput());
        this.engine.add(d1, this.engine.getInput());

        this.engine.add(s2, r1, d1);
        this.engine.add(s1, r1, s2);
        this.engine.add(m1, d1, s2);
        this.engine.add(a2, s1, m1);
        this.engine.add(d2, a2);

        Handle<Integer> ref = this.engine.addOutput(o1, d2);

        this.engine.execute(1);

        Assert.assertEquals(new Integer(8), ref.get());
    }

    /**
     * Tests a parallel schema.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testExecute2() {

        Calculator<Integer, Integer> r1 = new Repeater();
        Calculator<Integer, Integer> r2 = new Repeater();
        Calculator<Integer, Integer> r3 = new Repeater();
        Calculator<Integer, Integer> r4 = new Repeater();
        Calculator<Integer, Integer> u1 = new Summer();
        Calculator<Integer, Integer> d1 = new Doubler();
        Calculator<Integer, Integer> d2 = new Doubler();
        Calculator<Integer, Integer> r5 = new Repeater();

        Calculator<Integer, Integer> o1 = new Outputter();

        this.engine.add(r1, this.engine.getInput());
        this.engine.add(r2, this.engine.getInput());
        this.engine.add(r3, this.engine.getInput());
        this.engine.add(r4, this.engine.getInput());
        this.engine.add(r5, this.engine.getInput());

        this.engine.add(u1, r1, r2, r3, r4);
        this.engine.add(d1, r5);
        this.engine.add(d2, u1, d1);

        Handle<Integer> ref = this.engine.addOutput(o1, d2);

        this.engine.execute(1);

        Assert.assertEquals(new Integer(8), ref.get());
    }

    /**
     * Tests the case where {@link Engine#add(Calculator, Calculator...)} calls are intermingled with
     * {@link Engine#execute(Object)} calls.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testExecute3() {

        this.engine.execute(0);

        Calculator<Integer, Integer> r1 = new Repeater();
        Calculator<Integer, Integer> r2 = new Repeater();
        Calculator<Integer, Integer> r3 = new Repeater();
        Calculator<Integer, Integer> r4 = new Repeater();
        Calculator<Integer, Integer> r5 = new Repeater();
        Calculator<Integer, Integer> r6 = new Repeater();

        Calculator<Integer, Integer> o1 = new Outputter();
        Calculator<Integer, Integer> o2 = new Outputter();
        Calculator<Integer, Integer> o3 = new Outputter();

        this.engine.add(r1, this.engine.getInput());
        this.engine.execute(0);

        this.engine.add(r2, this.engine.getInput());
        this.engine.add(r3, this.engine.getInput());
        this.engine.execute(0);

        this.engine.add(r4, this.engine.getInput());
        this.engine.add(r5, this.engine.getInput());
        this.engine.add(r6, this.engine.getInput());
        this.engine.execute(0);

        Handle<Integer> ref1 = this.engine.addOutput(o1, r1);
        this.engine.execute(1);
        Assert.assertEquals(new Integer(1), ref1.get());

        Handle<Integer> ref2 = this.engine.addOutput(o2, r2);
        this.engine.execute(2);
        Assert.assertEquals(new Integer(2), ref2.get());

        Handle<Integer> ref3 = this.engine.addOutput(o3, r4);
        this.engine.execute(3);
        Assert.assertEquals(new Integer(3), ref3.get());
    }

    /**
     * Tests a degenerate tree schema.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testExecute4() {

        this.engine.execute(0);

        Calculator<Integer, Integer> a1 = new Summer();
        Calculator<Integer, Integer> a2 = new Summer();
        Calculator<Integer, Integer> a3 = new Summer();
        Calculator<Integer, Integer> a4 = new Summer();

        Calculator<Integer, Integer> r1 = new Repeater();
        Calculator<Integer, Integer> r2 = new Repeater();
        Calculator<Integer, Integer> r3 = new Repeater();
        Calculator<Integer, Integer> r4 = new Repeater();
        Calculator<Integer, Integer> r5 = new Repeater();

        Calculator<Integer, Integer> o = new Outputter();

        this.engine.add(r1, this.engine.getInput());
        this.engine.add(r2, this.engine.getInput());
        this.engine.add(r3, this.engine.getInput());
        this.engine.add(r4, this.engine.getInput());
        this.engine.add(r5, this.engine.getInput());

        this.engine.add(a1, r1, r5);
        this.engine.add(a2, r2, a1);
        this.engine.add(a3, a2, r3);
        this.engine.add(a4, r4, a3);

        Handle<Integer> ref = this.engine.addOutput(o, a4);

        this.engine.execute(1);

        Assert.assertEquals(new Integer(5), ref.get());
    }

    /**
     * Tests that an {@link IllegalStateException} is thrown.
     */
    @SuppressWarnings("unchecked")
    @Test(expected = IllegalStateException.class)
    public void testThrowException() {

        Engine<Integer> engine = new Engine<Integer>();
        engine.addOutput(new Thrower(), engine.getInput());
        engine.execute(1);
    }
}

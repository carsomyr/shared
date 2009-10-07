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

package shared.test;

import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import shared.util.Control;

/**
 * Contains useful support routines for <a href="http://www.junit.org/">JUnit</a> testing purposes.
 * 
 * @author Roy Liu
 */
public class Tests {

    /**
     * The instance used for logging.
     */
    final protected static Logger Log = LoggerFactory.getLogger(Tests.class);

    /**
     * Runs a batch of <a href="http://www.junit.org/">JUnit</a> tests.
     * 
     * @param name
     *            the name of this run.
     * @param suites
     *            the test suites to run.
     */
    final public static void runTests(final String name, Class<?>... suites) {

        JUnitCore juc = new JUnitCore();

        juc.addListener(new RunListener() {

            @Override
            public void testRunStarted(Description description) throws Exception {
                Log.debug(String.format("Running '%s'.", name));
            }

            @Override
            public void testStarted(Description description) throws Exception {
                Control.tick();
            }

            @Override
            public void testFinished(Description description) throws Exception {
                Log.debug(String.format("\t%s\t(%d ms)", description, Control.tock()));
            }

            @Override
            public void testFailure(Failure failure) throws Exception {
                Log.debug(String.format("\tException: %s", //
                        failure.getDescription()), failure.getException());
            }

            @Override
            public void testIgnored(Description description) throws Exception {
                Log.debug(String.format("\tIgnored: %s", description));
            }

            @Override
            public void testRunFinished(Result result) throws Exception {
                Log.debug(String.format("Results (total, failures) = (%d, %d).", //
                        result.getRunCount(), result.getFailureCount()));
            }
        });
        juc.run(suites);
    }

    /**
     * Tests if two arrays are equal within a small error tolerance.
     * 
     * @param aVal
     *            the left hand side.
     * @param bVal
     *            the right hand side.
     * @return whether the two are equal.
     */
    final public static boolean equals(double[] aVal, double[] bVal) {

        final int len = Control.checkEquals(aVal.length, bVal.length, //
                "Array length mismatch");

        for (int i = 0; i < len; i++) {

            if (Math.abs(aVal[i] - bVal[i]) > 1e-8) {
                return false;
            }
        }

        return true;
    }

    // Dummy constructor.
    Tests() {
    }
}

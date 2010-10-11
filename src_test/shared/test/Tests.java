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
 * A static utility class for <a href="http://www.junit.org/">JUnit</a> testing purposes.
 * 
 * @author Roy Liu
 */
public class Tests {

    /**
     * The instance used for logging.
     */
    final public static Logger log = LoggerFactory.getLogger(Tests.class);

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
                log.debug(String.format("Running \"%s\".", name));
            }

            @Override
            public void testStarted(Description description) throws Exception {
                Control.tick();
            }

            @Override
            public void testFinished(Description description) throws Exception {
                log.debug(String.format("    %s (%d ms)", description, Control.tock()));
            }

            @Override
            public void testFailure(Failure failure) throws Exception {
                log.debug(String.format("    Exception: %s", //
                        failure.getDescription()), failure.getException());
            }

            @Override
            public void testIgnored(Description description) throws Exception {
                log.debug(String.format("    Ignored: %s", description));
            }

            @Override
            public void testRunFinished(Result result) throws Exception {
                log.debug(String.format("Results (total, failures) = (%d, %d).", //
                        result.getRunCount(), result.getFailureCount()));
            }
        });
        juc.run(suites);
    }

    /**
     * Gets whether two arrays are equal within a small error tolerance.
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

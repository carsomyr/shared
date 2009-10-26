/**
 * This file is part of the Shared Scientific Toolbox in Java ("this library"). <br />
 * <br />
 * Copyright (C) 2007 Roy Liu, The Regents of the University of California <br />
 * <br />
 * This library is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License, version 2, as published by the Free Software Foundation. <br />
 * <br />
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. <br />
 * <br />
 * You should have received a copy of the GNU General Public License along with this library. If not, see <a
 * href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 */

package sharedx.test;

import shared.array.ArrayBase;
import shared.log.Logging;
import shared.metaclass.Loader;
import shared.metaclass.RegistryClassLoader;
import shared.metaclass.Loader.EntryPoint;
import shared.metaclass.Loader.LoadableResources;
import shared.test.Tests;
import shared.test.fft.AllFFTTests;
import shared.util.Control;

/**
 * Contains extension tests for the SST.
 * 
 * @apiviz.owns sharedx.test.BenchmarkJava
 * @apiviz.owns sharedx.test.BenchmarkNative
 * @author Roy Liu
 */
@LoadableResources(resources = {
//
        "jar:lib.junit", //
        "jar:lib.log4j", //
        "jar:lib.slf4j-api", //
        "jar:lib.slf4j-log4j12" //
}, //
//
packages = {
//
"sharedx.test" //
})
public class AllX {

    /**
     * Delegates to {@link Loader#start(String, Object)}.
     * 
     * @throws Exception
     *             when something goes awry.
     */
    public static void main(String[] args) throws Exception {
        Loader.start("sharedx.test.AllX", null);
    }

    /**
     * The entry point.
     */
    @EntryPoint
    public static void entryPoint(String[] args) {

        Logging.configureLog4J("shared/log4j.xml");
        Logging.configureLog4J("shared/test/log4j.xml");

        try {

            ((RegistryClassLoader) Thread.currentThread().getContextClassLoader()).loadLibrary("libx.sstx");

        } catch (RuntimeException e) {

            Tests.Log.debug("Requisite native library not found. Skipping tests.");

            return;
        }

        Control.checkTrue(ArrayBase.OpKernel.useNative() && ArrayBase.FFTService.useProvider(), //
                "Could not link native library");

        Tests.runTests("Extension Module Tests", //
                AllFFTTests.class, //
                BenchmarkJava.class, //
                BenchmarkNative.class);
    }

    // Dummy constructor.
    AllX() {
    }
}

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
import shared.test.Tests;
import shared.test.fft.AllFFTTests;
import shared.util.Control;
import shared.util.LoadableResources;
import shared.util.Loader;
import shared.util.LoadableResources.Resource;
import shared.util.LoadableResources.ResourceType;
import shared.util.Loader.EntryPoint;

/**
 * Contains extension tests for the SST.
 * 
 * @apiviz.owns sharedx.test.BenchmarkJava
 * @apiviz.owns sharedx.test.BenchmarkNative
 * @author Roy Liu
 */
@LoadableResources(resources = {
//
        @Resource(type = ResourceType.JAR, path = "lib", name = "junit"), //
        @Resource(type = ResourceType.JAR, path = "lib", name = "log4j"), //
        @Resource(type = ResourceType.JAR, path = "lib", name = "slf4j-api"), //
        @Resource(type = ResourceType.JAR, path = "lib", name = "slf4j-log4j12"), //
        @Resource(type = ResourceType.NATIVE, path = "libx", name = "sstx") //
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

        Control.checkTrue(ArrayBase.OpKernel.useNative() && ArrayBase.FFTService.useProvider(), //
                "Could not link native library");

        Logging.configureLog4J("shared/log4j.xml");
        Logging.configureLog4J("shared/test/log4j.xml");

        Tests.runTests("Extension Module Tests", //
                AllFFTTests.class, //
                BenchmarkJava.class, //
                BenchmarkNative.class);
    }

    // Dummy constructor.
    AllX() {
    }
}

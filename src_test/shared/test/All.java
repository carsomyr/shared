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

import shared.array.ArrayBase;
import shared.image.kernel.ImageOps;
import shared.log.Logging;
import shared.metaclass.Loader;
import shared.metaclass.Loader.EntryPoint;
import shared.metaclass.Loader.LoadableResources;
import shared.test.array.AllArrayOperationTests;
import shared.test.array.AllArrayUtilTests;
import shared.test.fft.AllFFTTests;
import shared.test.image.AllImageTests;
import shared.test.net.AllNetTests;
import shared.test.parallel.AllParallelTests;
import shared.test.stat.AllMLTests;
import shared.test.stat.AllStatUtilTests;
import shared.test.util.AllUtilTests;

/**
 * Contains pure Java tests for the SST.
 * 
 * @apiviz.uses shared.test.Tests
 * @author Roy Liu
 */
@LoadableResources(resources = {
//
        "jar:lib.commons-codec", //
        "jar:lib.junit", //
        "jar:lib.log4j", //
        "jar:lib.slf4j-api", //
        "jar:lib.slf4j-log4j12" //
}, //
//
packages = {
//
"shared.test" //
})
public class All {

    /**
     * Delegates to {@link Loader#start(String, Object)}.
     * 
     * @throws Exception
     *             when something goes awry.
     */
    public static void main(String[] args) throws Exception {
        Loader.start("shared.test.All", null);
    }

    /**
     * The entry point.
     */
    @EntryPoint
    public static void entryPoint(String[] args) {

        ArrayBase.OpKernel.useJava();
        ArrayBase.IOKernel.useMatlabIO();
        ArrayBase.FFTService.useJava();
        ImageOps.ImKernel.useJava();

        Logging.configureLog4J("shared/log4j.xml");
        Logging.configureLog4J("shared/net/log4j.xml");
        Logging.configureLog4J("shared/test/log4j.xml");

        Tests.runTests("Pure Java Tests", //
                AllUtilTests.class, //
                AllArrayOperationTests.class, //
                AllArrayUtilTests.class, //
                AllFFTTests.class, //
                AllImageTests.class, //
                AllParallelTests.class, //
                AllMLTests.class, //
                AllStatUtilTests.class, //
                AllNetTests.class);
    }

    // Dummy constructor.
    All() {
    }
}

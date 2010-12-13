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

package org.shared.test;

import org.shared.array.ArrayBase;
import org.shared.image.kernel.ImageOps;
import org.shared.log.Logging;
import org.shared.metaclass.Loader;
import org.shared.metaclass.Loader.EntryPoint;
import org.shared.metaclass.Loader.LoadableResources;
import org.shared.test.array.AllArrayOperationTests;
import org.shared.test.array.AllArrayUtilTests;
import org.shared.test.fft.AllFftTests;
import org.shared.test.image.AllImageTests;
import org.shared.test.net.AllNetTests;
import org.shared.test.parallel.AllParallelTests;
import org.shared.test.stat.AllMlTests;
import org.shared.test.stat.AllStatUtilTests;
import org.shared.test.util.AllUtilTests;

/**
 * Contains pure Java tests for the SST.
 * 
 * @apiviz.uses org.shared.test.Tests
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
"org.shared.test" //
})
public class All {

    /**
     * Delegates to {@link Loader#run(String, Object)}.
     * 
     * @throws Exception
     *             when something goes awry.
     */
    public static void main(String[] args) throws Exception {
        Loader.run("org.shared.test.All", null);
    }

    /**
     * The program entry point.
     */
    @EntryPoint
    public static void main0(String[] args) {

        ArrayBase.opKernel.useJava();
        ArrayBase.ioKernel.useMatlabIo();
        ArrayBase.fftService.useJava();
        ImageOps.imKernel.useJava();

        Logging.configureLog4J("org/shared/log4j.xml");
        Logging.configureLog4J("org/shared/net/log4j.xml");
        Logging.configureLog4J("org/shared/test/log4j.xml");

        Tests.runTests("Pure Java Tests", //
                AllUtilTests.class, //
                AllArrayOperationTests.class, //
                AllArrayUtilTests.class, //
                AllFftTests.class, //
                AllImageTests.class, //
                AllParallelTests.class, //
                AllMlTests.class, //
                AllStatUtilTests.class, //
                AllNetTests.class);
    }

    // Dummy constructor.
    All() {
    }
}

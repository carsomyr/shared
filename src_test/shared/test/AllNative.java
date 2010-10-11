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

import shared.array.ArrayBase;
import shared.image.kernel.ImageOps;
import shared.log.Logging;
import shared.metaclass.Loader;
import shared.metaclass.Loader.EntryPoint;
import shared.metaclass.Loader.LoadableResources;
import shared.metaclass.RegistryClassLoader;
import shared.test.array.AllArrayOperationTests;
import shared.test.image.AllImageTests;
import shared.test.stat.AllMlTests;
import shared.util.Control;

/**
 * Contains native library tests for the SST.
 * 
 * @apiviz.uses shared.test.Tests
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
        "shared.test", //
        "shared.array.jni", //
        "shared.image.jni" //
})
public class AllNative {

    /**
     * Delegates to {@link Loader#run(String, Object)}.
     * 
     * @throws Exception
     *             when something goes awry.
     */
    public static void main(String[] args) throws Exception {
        Loader.run("shared.test.AllNative", null);
    }

    /**
     * The program entry point.
     */
    @EntryPoint
    public static void main0(String[] args) {

        Logging.configureLog4J("shared/log4j.xml");
        Logging.configureLog4J("shared/test/log4j.xml");

        try {

            ((RegistryClassLoader) Thread.currentThread().getContextClassLoader()).loadLibrary("lib.sst");

        } catch (RuntimeException e) {

            Tests.log.debug("Requisite native library not found. Skipping tests.");

            return;
        }

        Control.checkTrue(ArrayBase.opKernel.useRegisteredKernel() && ImageOps.imKernel.useRegisteredKernel(), //
                "Could not link native library");

        ArrayBase.ioKernel.useMatlabIo();
        ArrayBase.fftService.useJava();

        Tests.runTests("Native Tests", //
                AllArrayOperationTests.class, //
                AllMlTests.class, //
                AllImageTests.class);
    }

    // Dummy constructor.
    AllNative() {
    }
}

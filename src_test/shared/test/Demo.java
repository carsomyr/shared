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

import java.io.File;

import shared.log.Logging;
import shared.metaclass.Loader;
import shared.metaclass.Loader.EntryPoint;
import shared.metaclass.Loader.LoadableResources;
import shared.test.net.ClientServerTest;
import shared.test.stat.PlotTest;

/**
 * Contains demonstrations of the SST's capabilities.
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
"shared.test" //
})
public class Demo {

    /**
     * The demo directory.
     */
    final public static File DemoDir = new File("demo");

    /**
     * Delegates to {@link Loader#run(String, Object)}.
     * 
     * @throws Exception
     *             when something goes awry.
     */
    public static void main(String[] args) throws Exception {
        Loader.run("shared.test.Demo", null);
    }

    /**
     * The program entry point.
     */
    @EntryPoint
    public static void main0(String[] args) {

        Logging.configureLog4J("shared/log4j.xml");
        Logging.configureLog4J("shared/net/log4j.xml");
        Logging.configureLog4J("shared/test/log4j.xml");
        Logging.configureLog4J("shared/test/net/log4j.xml");

        Tests.runTests("Demonstrations", //
                PlotTest.class, //
                ClientServerTest.class);
    }

    /**
     * Creates the demo directory.
     */
    final public static void createDemoDir() {

        if (!DemoDir.exists()) {
            DemoDir.mkdirs();
        }
    }

    // Dummy constructor.
    Demo() {
    }
}

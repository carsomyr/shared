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

import java.io.File;

import shared.log.Logging;
import shared.test.net.ClientServerTest;
import shared.test.stat.PlotTest;
import shared.util.LoadableResources;
import shared.util.Loader;
import shared.util.LoadableResources.Resource;
import shared.util.LoadableResources.ResourceType;
import shared.util.Loader.EntryPoint;

/**
 * Contains demonstrations of the SST's capabilities.
 * 
 * @apiviz.uses shared.test.Tests
 * @author Roy Liu
 */
@LoadableResources(resources = {
//
        @Resource(type = ResourceType.JAR, path = "lib", name = "junit"), //
        @Resource(type = ResourceType.JAR, path = "lib", name = "log4j"), //
        @Resource(type = ResourceType.JAR, path = "lib", name = "slf4j-api"), //
        @Resource(type = ResourceType.JAR, path = "lib", name = "slf4j-log4j12") //
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
     * Delegates to {@link Loader#start(String, Object)}.
     * 
     * @throws Exception
     *             when something goes awry.
     */
    public static void main(String[] args) throws Exception {
        Loader.start("shared.test.Demo", null);
    }

    /**
     * The entry point.
     */
    @EntryPoint
    public static void entryPoint(String[] args) {

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
/**
 * This file is part of the Shared Scientific Toolbox in Java ("this library"). <br />
 * <br />
 * Copyright (C) 2008 Roy Liu <br />
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

package shared.log;

import org.apache.log4j.xml.DOMConfigurator;

import shared.util.Control;

/**
 * A collection of static methods for setting up logging frameworks.
 * 
 * @author Roy Liu
 */
public class Logging {

    /**
     * Configures <a href="http://logging.apache.org/log4j/">Log4J</a> from an XML file found on the class path of
     * {@link Thread#getContextClassLoader()}.
     * 
     * @param pathname
     *            the class pathname.
     */
    final public static void configureLog4J(String pathname) {

        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        DOMConfigurator.configure( //
                Control.createDocument(cl.getResourceAsStream(pathname)).getDocumentElement());
    }

    // Dummy constructor.
    Logging() {
    }
}

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

package shared;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Properties;

/**
 * Contains SST constant values.
 * 
 * @author Roy Liu
 */
public class Constants {

    /**
     * The major version of the software.
     */
    final public static int MAJOR_VERSION;

    /**
     * The minor version of the software.
     */
    final public static int MINOR_VERSION;

    static {

        Properties p = new Properties();

        try {

            p.load(Constants.class.getClassLoader().getResourceAsStream("shared/project.properties"));

        } catch (IOException e) {

            throw new RuntimeException(e);
        }

        BigDecimal version = new BigDecimal(p.getProperty("build.version"));

        int versionAsInt = version.movePointRight(2).intValue();

        MAJOR_VERSION = versionAsInt / 100;
        MINOR_VERSION = versionAsInt % 100;
    }

    // Dummy constructor.
    Constants() {
    }
}

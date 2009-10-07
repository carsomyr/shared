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

package shared.cli;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import shared.cli.CLIOptions.CLIOption;
import shared.util.Control;

/**
 * An interpreter for {@link CLIOptions} annotations, from which <a href="http://commons.apache.org/cli/">Apache Commons
 * CLI</a> {@link Options} objects are derived.
 * 
 * @apiviz.has shared.cli.CLIOptions - - - argument
 * @author Roy Liu
 */
public class CLI {

    /**
     * Creates a {@link CommandLine}.
     * 
     * @param clazz
     *            the class annotated with {@link CLIOptions}.
     * @throws ParseException
     *             when the command line arguments couldn't be parsed.
     */
    public static CommandLine createCommandLine(Class<?> clazz, String[] args) throws ParseException {
        return new PosixParser().parse(createOptions(clazz), args);
    }

    /**
     * Creates a help string.
     * 
     * @param clazz
     *            the class annotated with {@link CLIOptions}.
     */
    public static String createHelp(Class<?> clazz) {

        StringWriter sw = new StringWriter();
        new HelpFormatter().printHelp(new PrintWriter(sw), //
                80, clazz.getName(), //
                "", createOptions(clazz), 4, 4, "", true);

        return sw.toString();
    }

    /**
     * Creates a set of {@link Option}s.
     * 
     * @param clazz
     *            the class annotated with {@link CLIOptions}.
     */
    protected static Options createOptions(Class<?> clazz) {

        CLIOptions cliOptions = clazz.getAnnotation(CLIOptions.class);

        Control.checkTrue(cliOptions != null, //
                "Please provide command line argument specifications");

        Options options = new Options();

        for (CLIOption cliOption : cliOptions.options()) {

            boolean optionalArgs = cliOption.optionalArgs();
            int numArgs = cliOption.numArgs();

            if (!optionalArgs) {

                if (numArgs >= 0) {

                    OptionBuilder.hasArgs(numArgs);

                } else {

                    Control.checkTrue(numArgs == -1, //
                            "Invalid number of arguments");

                    OptionBuilder.hasArgs();
                }

            } else {

                if (numArgs >= 0) {

                    OptionBuilder.hasOptionalArgs(numArgs);

                } else {

                    Control.checkTrue(numArgs == -1, //
                            "Invalid number of arguments");

                    OptionBuilder.hasOptionalArgs();
                }
            }

            OptionBuilder.isRequired(cliOption.requiredOpt());

            String longOpt = cliOption.longOpt();
            OptionBuilder.withLongOpt(longOpt.length() > 0 ? longOpt : null);

            String argName = cliOption.argName();
            OptionBuilder.withArgName(argName.length() > 0 ? argName : null);

            String description = cliOption.description();
            OptionBuilder.withDescription(description.length() > 0 ? description : null);

            char valueSeparator = cliOption.valueSeparator();

            if (valueSeparator != '\u0000') {
                OptionBuilder.withValueSeparator(valueSeparator);
            }

            //

            String shortOpt = cliOption.opt();

            if (shortOpt.length() > 0) {

                options.addOption(OptionBuilder.create(shortOpt));

            } else {

                options.addOption(OptionBuilder.create());
            }
        }

        return options;
    }

    // Dummy constructor.
    CLI() {
    }
}

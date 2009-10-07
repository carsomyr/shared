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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/**
 * An annotation class for specifying <a href="http://commons.apache.org/cli/">Apache Commons CLI</a> {@link Options}
 * objects.
 * 
 * @apiviz.owns shared.cli.CLIOptions.CLIOption
 * @author Roy Liu
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CLIOptions {

    /**
     * Gets the {@link CLIOption}s.
     */
    public CLIOption[] options();

    /**
     * An annotation for a single {@link Option}.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface CLIOption {

        /**
         * The short {@link Option} name.
         */
        public String opt() default "";

        /**
         * The long {@link Option} name.
         */
        public String longOpt() default "";

        /**
         * Whether arguments are optional.
         */
        public boolean optionalArgs() default false;

        /**
         * The minimum number of arguments that can be expected. Use {@code -1} to denote an unlimited number.
         */
        public int numArgs() default 1;

        /**
         * Whether the {@link Option} is required.
         */
        public boolean requiredOpt() default false;

        /**
         * The argument name.
         */
        public String argName() default "";

        /**
         * The {@link Option} description.
         */
        public String description() default "";

        /**
         * The {@link Option} value separator.
         */
        public char valueSeparator() default '\u0000';
    }
}

/**
 * <p>
 * Copyright (C) 2008 Roy Liu<br />
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

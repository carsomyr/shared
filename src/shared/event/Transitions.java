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

package shared.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation class for specifying finite state machine transitions. Inspired by Apache MINA's <a
 * href="http://mina.apache.org/introduction-to-mina-statemachine.html">StateMachine</a> construct.
 * 
 * @apiviz.owns shared.event.Transitions.Transition
 * @author Roy Liu
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Transitions {

    /**
     * Gets the {@link Transition}s.
     */
    public Transition[] transitions();

    /**
     * An annotation for a single transition.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Transition {

        /**
         * The current state.
         */
        String currentState();

        /**
         * The {@link Event} type.
         */
        String eventType();

        /**
         * The next state.
         */
        String nextState() default "";

        /**
         * The group name.
         */
        String group() default "";
    }
}

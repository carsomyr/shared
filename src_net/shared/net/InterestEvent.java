/**
 * This file is part of the Shared Scientific Toolbox in Java ("this library"). <br />
 * <br />
 * Copyright (C) 2005 Roy Liu <br />
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

package shared.net;

import shared.event.Event;
import shared.event.Source;

/**
 * An internal event class for {@link AbstractManagedConnection} and {@link ConnectionManager}.
 * 
 * @apiviz.owns shared.net.InterestEvent.InterestEventType
 * @param <T>
 *            the argument type.
 * @author Roy Liu
 */
public class InterestEvent<T> implements Event<InterestEvent<?>, InterestEvent.InterestEventType, SourceType> {

    /**
     * An enumeration of {@link InterestEvent} types.
     */
    public enum InterestEventType {

        /**
         * Denotes a connection connect request.
         */
        CONNECT, //

        /**
         * Denotes a connection accept request.
         */
        ACCEPT, //

        /**
         * Denotes a connection dispatch.
         */
        DISPATCH, //

        /**
         * Denotes a connection registration request.
         */
        REGISTER, //

        /**
         * Denotes a connection operation interest change request.
         */
        OP, //

        /**
         * Denotes an asynchronous execution request.
         */
        EXECUTE, //

        /**
         * Denotes a connection close request.
         */
        CLOSE, //

        /**
         * Denotes a connection error.
         */
        ERROR, //

        /**
         * Denotes a manager thread shutdown request.
         */
        SHUTDOWN, //

        /**
         * Denotes a query for the list of bound addresses.
         */
        QUERY_BOUND_ADDRESSES, //

        /**
         * Denotes a query for the list of connections.
         */
        QUERY_CONNECTIONS;
    }

    final InterestEventType type;
    final T argument;
    final Source<InterestEvent<?>, SourceType> source;

    /**
     * Default constructor.
     */
    protected InterestEvent(InterestEventType type, T argument, Source<InterestEvent<?>, SourceType> source) {

        this.type = type;
        this.argument = argument;
        this.source = source;
    }

    /**
     * Alternate constructor.
     */
    protected InterestEvent(InterestEventType type, Source<InterestEvent<?>, SourceType> source) {
        this(type, null, source);
    }

    public InterestEventType getType() {
        return this.type;
    }

    public Source<InterestEvent<?>, SourceType> getSource() {
        return this.source;
    }

    /**
     * Gets the argument.
     */
    protected T getArgument() {
        return this.argument;
    }
}

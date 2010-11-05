/**
 * <p>
 * Copyright (c) 2005 Roy Liu<br>
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

package shared.net.nio;

import shared.event.Event;
import shared.event.Source;
import shared.net.SourceType;

/**
 * An internal event class for {@link NioConnection} and {@link NioManager}.
 * 
 * @apiviz.owns shared.net.nio.NioEvent.NioEventType
 * @param <T>
 *            the argument type.
 * @author Roy Liu
 */
public class NioEvent<T> implements Event<NioEvent<?>, NioEvent.NioEventType, SourceType> {

    /**
     * An enumeration of {@link NioEvent} types.
     */
    public enum NioEventType {

        /**
         * Denotes a connection connect request.
         */
        CONNECT, //

        /**
         * Denotes a connection accept request.
         */
        ACCEPT, //

        /**
         * Denotes a connection registration request.
         */
        REGISTER, //

        /**
         * Denotes a connection dispatch notification.
         */
        DISPATCH, //

        /**
         * Denotes a connection operation interest change request.
         */
        OP, //

        /**
         * Denotes a connection close request.
         */
        CLOSE, //

        /**
         * Denotes a connection error notification.
         */
        ERROR, //

        /**
         * Denotes a request to execute code on the manager thread.
         */
        INVOKE, //

        /**
         * Denotes a request to get the list of bound addresses.
         */
        GET_BOUND_ADDRESSES, //

        /**
         * Denotes a request to get the list of connections.
         */
        GET_CONNECTIONS, //

        /**
         * Denotes a request to get the listen backlog size.
         */
        GET_BACKLOG_SIZE, //

        /**
         * Denotes a request to set the listen backlog size.
         */
        SET_BACKLOG_SIZE, //

        /**
         * Denotes a request to shut down the manager thread.
         */
        SHUTDOWN;
    }

    final NioEventType type;
    final T argument;
    final Source<NioEvent<?>, SourceType> source;

    /**
     * Default constructor.
     */
    protected NioEvent(NioEventType type, T argument, Source<NioEvent<?>, SourceType> source) {

        this.type = type;
        this.argument = argument;
        this.source = source;
    }

    /**
     * Alternate constructor.
     */
    protected NioEvent(NioEventType type, Source<NioEvent<?>, SourceType> source) {
        this(type, null, source);
    }

    @Override
    public NioEventType getType() {
        return this.type;
    }

    @Override
    public Source<NioEvent<?>, SourceType> getSource() {
        return this.source;
    }

    /**
     * Gets the argument.
     */
    protected T getArgument() {
        return this.argument;
    }
}

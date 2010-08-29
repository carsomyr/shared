/**
 * <p>
 * Copyright (C) 2010 Roy Liu<br />
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

package shared.net.filter;

import shared.event.Event;
import shared.event.Source;
import shared.net.Connection;
import shared.net.SourceType;

/**
 * An event class for conveying out-of-band information that doesn't belong in data, like {@link Connection} closure.
 * 
 * @apiviz.owns shared.net.filter.OOBEvent.OOBEventType
 * @author Roy Liu
 */
public class OOBEvent implements Event<OOBEvent, OOBEvent.OOBEventType, SourceType> {

    /**
     * An enumeration of {@link OOBEvent} types.
     */
    public enum OOBEventType {

        /**
         * Denotes a custom event type.
         */
        CUSTOM, //

        /**
         * Denotes a connection bind.
         */
        BIND, //

        /**
         * Denotes a connection closure by end-of-stream.
         */
        CLOSING_EOS, //

        /**
         * Denotes a connection closure by user request.
         */
        CLOSING_USER, //

        /**
         * Denotes a connection closure by error.
         */
        CLOSING_ERROR;
    }

    final OOBEventType type;
    final Source<OOBEvent, SourceType> source;

    /**
     * Default constructor.
     */
    public OOBEvent(OOBEventType type, Source<OOBEvent, SourceType> source) {

        this.type = type;
        this.source = source;
    }

    @Override
    public OOBEventType getType() {
        return this.type;
    }

    @Override
    public Source<OOBEvent, SourceType> getSource() {
        return this.source;
    }

    /**
     * Creates a human-readable representation of this event.
     */
    @Override
    public String toString() {
        return String.format("%s[%s, %s]", //
                OOBEvent.class.getSimpleName(), this.type, this.source);
    }
}

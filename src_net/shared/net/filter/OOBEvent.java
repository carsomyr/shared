/**
 * This file is part of the Shared Scientific Toolbox in Java ("this library"). <br />
 * <br />
 * Copyright (C) 2010 Roy Liu <br />
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
         * Denotes a connection closure induced by an end-of-stream.
         */
        CLOSE_EOS, //

        /**
         * Denotes a connection closure initiated by the user.
         */
        CLOSE_USER;
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

    public OOBEventType getType() {
        return this.type;
    }

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

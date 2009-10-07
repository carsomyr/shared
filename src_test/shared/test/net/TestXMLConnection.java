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

package shared.test.net;

import static shared.net.SourceType.CONNECTION;
import static shared.test.net.TestXMLEvent.TestXMLEventType.END_OF_STREAM;

import org.w3c.dom.Element;

import shared.event.SourceLocal;
import shared.net.ConnectionManager;
import shared.net.SourceType;
import shared.net.XMLConnection;
import shared.test.net.TestXMLEvent.ErrorXMLEvent;

/**
 * A subclass of {@link XMLConnection} for testing purposes.
 * 
 * @apiviz.has shared.test.net.TestXMLEvent - - - event
 * @author Roy Liu
 */
public class TestXMLConnection extends XMLConnection<TestXMLConnection, TestXMLEvent, SourceType> {

    final SourceLocal<TestXMLEvent> receiver;

    /**
     * Default constructor.
     * 
     * @param receiver
     *            the local receiver to which events will be forwarded.
     * @see XMLConnection#XMLConnection(String, Enum, int, ConnectionManager)
     */
    public TestXMLConnection(String name, int minimumSize, int maximumSize, ConnectionManager manager,
            SourceLocal<TestXMLEvent> receiver) {
        super(name, CONNECTION, minimumSize, maximumSize, manager);

        this.receiver = receiver;
    }

    public void onLocal(TestXMLEvent evt) {
        this.receiver.onLocal(evt);
    }

    @Override
    protected void onError(Throwable error) {
        onLocal(new ErrorXMLEvent(error, this));
    }

    @Override
    protected TestXMLEvent parse(Element rootElement) {
        return (rootElement == null) ? new TestXMLEvent(END_OF_STREAM, this) //
                : TestXMLEvent.parse(rootElement, this);
    }
}

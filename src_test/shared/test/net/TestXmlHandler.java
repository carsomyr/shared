/**
 * <p>
 * Copyright (c) 2008 Roy Liu<br>
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

package shared.test.net;

import static shared.net.SourceType.CONNECTION;
import static shared.test.net.TestXmlEvent.TestXmlEventType.END_OF_STREAM;

import org.w3c.dom.Element;

import shared.event.SourceLocal;
import shared.net.SourceType;
import shared.net.handler.XmlHandler;
import shared.net.nio.NioManager;
import shared.test.net.TestXmlEvent.ErrorXmlEvent;

/**
 * A subclass of {@link XmlHandler} for testing purposes.
 * 
 * @apiviz.has shared.test.net.TestXmlEvent - - - event
 * @author Roy Liu
 */
public class TestXmlHandler extends XmlHandler<TestXmlHandler, TestXmlEvent, SourceType> {

    final SourceLocal<TestXmlEvent> receiver;

    /**
     * Default constructor.
     * 
     * @param receiver
     *            the local receiver to which events will be forwarded.
     */
    public TestXmlHandler(String name, int minimumSize, int maximumSize, //
            NioManager manager, SourceLocal<TestXmlEvent> receiver) {
        super(name, CONNECTION, minimumSize, maximumSize, manager);

        this.receiver = receiver;
    }

    @Override
    public void onLocal(TestXmlEvent evt) {
        this.receiver.onLocal(evt);
    }

    @Override
    protected void onError() {
        onLocal(new ErrorXmlEvent(getException(), this));
    }

    @Override
    protected TestXmlEvent parse(Element rootElement) {
        return (rootElement == null) ? new TestXmlEvent(END_OF_STREAM, this) //
                : TestXmlEvent.parse(rootElement, this);
    }
}

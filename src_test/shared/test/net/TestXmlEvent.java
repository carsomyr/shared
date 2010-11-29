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

import static shared.test.net.TestXmlEvent.TestXmlEventType.DATA;
import static shared.test.net.TestXmlEvent.TestXmlEventType.ERROR;
import static shared.test.net.TestXmlEvent.TestXmlEventType.SEQUENCE;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import shared.codec.Codecs;
import shared.event.Source;
import shared.event.XmlEvent;
import shared.net.SourceType;
import shared.net.filter.XmlFilterFactory;
import shared.test.net.AbstractTestVerifier.AbstractReceiverVerifier;
import shared.test.net.AbstractTestVerifier.AbstractSenderVerifier;
import shared.test.net.AbstractTestVerifier.DataEventDefinition;
import shared.test.net.AbstractTestVerifier.ErrorEventDefinition;
import shared.test.net.AbstractTestVerifier.SequenceEventDefinition;

/**
 * A base class of all XML-derived events for testing purposes.
 * 
 * @apiviz.owns shared.test.net.TestXmlEvent.TestXmlEventType
 * @author Roy Liu
 */
public class TestXmlEvent implements XmlEvent<TestXmlEvent, TestXmlEvent.TestXmlEventType, SourceType> {

    /**
     * An enumeration of {@link TestXmlEvent} types.
     */
    public enum TestXmlEventType {

        /**
         * Indicates that fresh data awaits parsing.
         */
        DATA {

            @Override
            protected TestXmlEvent parse(Node contentNode, Source<TestXmlEvent, SourceType> source) {
                return new DataXmlEvent(contentNode, source);
            }
        }, //

        /**
         * Indicates a sequence number for verifying internal connection state.
         */
        SEQUENCE {

            @Override
            protected TestXmlEvent parse(Node contentNode, Source<TestXmlEvent, SourceType> source) {
                return new SequenceXmlEvent(contentNode, source);
            }
        }, //

        /**
         * Indicates that an error has occurred.
         */
        ERROR, //

        /**
         * Indicates that an end-of-stream has been reached.
         */
        END_OF_STREAM;

        /**
         * Parses a {@link TestXmlEvent} from the given DOM {@link Node}.
         */
        protected TestXmlEvent parse(Node contentNode, Source<TestXmlEvent, SourceType> source) {
            throw new UnsupportedOperationException("Parse method not defined");
        }
    }

    final TestXmlEventType type;
    final Source<TestXmlEvent, SourceType> source;

    /**
     * Default constructor.
     */
    protected TestXmlEvent(TestXmlEventType type, Source<TestXmlEvent, SourceType> source) {

        this.type = type;
        this.source = source;
    }

    /**
     * Transfers the contents of this event into the given DOM {@link Node}.
     */
    protected void getContents(Node contentNode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Element toDom() {

        Document doc = XmlFilterFactory.newDocument();

        Element rootElement = doc.createElement(XmlEvent.class.getName());

        rootElement.appendChild(doc.createElement("type")) //
                .setTextContent(getType().toString());

        getContents(rootElement.appendChild(doc.createElement("content")));

        return rootElement;
    }

    @Override
    public Source<TestXmlEvent, SourceType> getSource() {
        return this.source;
    }

    @Override
    public TestXmlEventType getType() {
        return this.type;
    }

    /**
     * Parses a {@link TestXmlEvent} from the given root DOM {@link Element}.
     */
    final public static TestXmlEvent parse(Element rootElement, Source<TestXmlEvent, SourceType> source) {

        NodeList children = rootElement.getChildNodes();
        return TestXmlEventType.valueOf(children.item(0).getTextContent()).parse(children.item(1), source);
    }

    /**
     * A subclass of {@link TestXmlEvent} for carrying errors.
     */
    protected static class ErrorXmlEvent extends TestXmlEvent implements ErrorEventDefinition {

        final Throwable exception;

        /**
         * Default constructor.
         */
        public ErrorXmlEvent(Throwable exception, Source<TestXmlEvent, SourceType> source) {
            super(ERROR, source);

            this.exception = exception;
        }

        @Override
        public Throwable getException() {
            return this.exception;
        }
    }

    /**
     * A subclass of {@link TestXmlEvent} for holding sequence numbers.
     */
    protected static class SequenceXmlEvent extends TestXmlEvent implements SequenceEventDefinition {

        final long seqNo;
        final int nMessages;

        /**
         * Default constructor.
         */
        public SequenceXmlEvent(long seqNo, int nMessages, Source<TestXmlEvent, SourceType> source) {
            super(SEQUENCE, source);

            this.seqNo = seqNo;
            this.nMessages = nMessages;
        }

        /**
         * Alternate constructor.
         */
        public SequenceXmlEvent(Node contentNode, Source<TestXmlEvent, SourceType> source) {
            super(SEQUENCE, source);

            NodeList children = contentNode.getChildNodes();

            this.seqNo = Long.parseLong(children.item(0).getTextContent());
            this.nMessages = Integer.parseInt(children.item(1).getTextContent());
        }

        @Override
        protected void getContents(Node contentNode) {

            Document doc = contentNode.getOwnerDocument();

            contentNode.appendChild(doc.createElement("seqNo")) //
                    .setTextContent(Long.toString(this.seqNo));
            contentNode.appendChild(doc.createElement("nmessages")) //
                    .setTextContent(Integer.toString(this.nMessages));
        }

        @Override
        public long getSeqNo() {
            return this.seqNo;
        }

        @Override
        public int nMessages() {
            return this.nMessages;
        }
    }

    /**
     * A subclass of {@link TestXmlEvent} for carrying data.
     */
    protected static class DataXmlEvent extends TestXmlEvent implements DataEventDefinition {

        final byte[] data;

        /**
         * Default constructor.
         */
        public DataXmlEvent(byte[] data, Source<TestXmlEvent, SourceType> source) {
            super(DATA, source);

            this.data = data;
        }

        /**
         * Alternate constructor.
         */
        public DataXmlEvent(Node contentNode, Source<TestXmlEvent, SourceType> source) {
            super(DATA, source);

            this.data = Codecs.base64ToBytes(contentNode.getFirstChild().getTextContent());
        }

        @Override
        protected void getContents(Node contentNode) {

            Document doc = contentNode.getOwnerDocument();

            contentNode.appendChild(doc.createElement("data")) //
                    .setTextContent(Codecs.bytesToBase64(this.data));
        }

        @Override
        public byte[] getData() {
            return this.data;
        }
    }

    /**
     * An instantiation of {@link AbstractReceiverVerifier}.
     */
    protected static class ReceiverXmlVerifier extends AbstractReceiverVerifier<TestXmlEvent> {

        /**
         * Default constructor.
         */
        protected ReceiverXmlVerifier() {
        }

        @Override
        protected TestXmlEvent createSequenceEvent(long seqNo, int nMessages) {
            return new SequenceXmlEvent(seqNo, nMessages, null);
        }
    }

    /**
     * An instantiation of {@link AbstractSenderVerifier}.
     */
    protected static class SenderXmlVerifier extends AbstractSenderVerifier<TestXmlEvent> {

        /**
         * Default constructor.
         */
        protected SenderXmlVerifier(long seqNo, int nMessages, int meanMessageSize) {
            super(seqNo, nMessages, meanMessageSize);
        }

        @Override
        protected TestXmlEvent createDataEvent(byte[] data) {
            return new DataXmlEvent(data, null);
        }
    }
}

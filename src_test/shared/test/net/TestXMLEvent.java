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

import static shared.test.net.TestXMLEvent.TestXMLEventType.DATA;
import static shared.test.net.TestXMLEvent.TestXMLEventType.ERROR;
import static shared.test.net.TestXMLEvent.TestXMLEventType.SEQUENCE;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import shared.codec.Base64;
import shared.event.Source;
import shared.event.XMLEvent;
import shared.net.SourceType;
import shared.test.net.AbstractTestVerifier.AbstractReceiverVerifier;
import shared.test.net.AbstractTestVerifier.AbstractSenderVerifier;
import shared.test.net.AbstractTestVerifier.DataEventDefinition;
import shared.test.net.AbstractTestVerifier.ErrorEventDefinition;
import shared.test.net.AbstractTestVerifier.SequenceEventDefinition;
import shared.util.Control;

/**
 * A base class of all XML-derived events for testing purposes.
 * 
 * @apiviz.owns shared.test.net.TestXMLEvent.TestXMLEventType
 * @author Roy Liu
 */
public class TestXMLEvent extends XMLEvent<TestXMLEvent, TestXMLEvent.TestXMLEventType, SourceType> {

    /**
     * An enumeration of {@link TestXMLEvent} types.
     */
    public enum TestXMLEventType {

        /**
         * Indicates that an error has occurred.
         */
        ERROR {

            @Override
            protected TestXMLEvent parseXML(Node rootNode, Source<TestXMLEvent, SourceType> source) {
                throw new IllegalArgumentException();
            }
        }, //

        /**
         * Indicates that an end-of-stream has been reached.
         */
        END_OF_STREAM {

            @Override
            protected TestXMLEvent parseXML(Node rootNode, Source<TestXMLEvent, SourceType> source) {
                throw new IllegalArgumentException();
            }
        }, //

        /**
         * Indicates that fresh data awaits parsing.
         */
        DATA {

            @Override
            protected TestXMLEvent parseXML(Node rootNode, Source<TestXMLEvent, SourceType> source) {
                return new DataXMLEvent(rootNode, source);
            }
        }, //

        /**
         * Indicates a sequence number for verifying internal connection state.
         */
        SEQUENCE {

            @Override
            protected TestXMLEvent parseXML(Node rootNode, Source<TestXMLEvent, SourceType> source) {
                return new SequenceXMLEvent(rootNode, source);
            }
        };

        /**
         * Given a root DOM {@link Node}, parses a {@link TestXMLEvent} from this type.
         */
        abstract protected TestXMLEvent parseXML(Node rootNode, Source<TestXMLEvent, SourceType> source);
    }

    final Source<TestXMLEvent, SourceType> source;

    /**
     * Default constructor.
     */
    protected TestXMLEvent(TestXMLEventType type, Source<TestXMLEvent, SourceType> source) {
        super(type);

        this.source = source;
    }

    /**
     * Transfers the contents of this event into the given DOM {@link Node}.
     */
    protected void getContents(Node contentNode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Element toDOM() {

        Document doc = Control.createDocument();

        Element rootElement = doc.createElement(XMLEvent.class.getName());

        rootElement.appendChild(doc.createElement("type")) //
                .setTextContent(getType().toString());

        getContents(rootElement.appendChild(doc.createElement("content")));

        return rootElement;
    }

    public Source<TestXMLEvent, SourceType> getSource() {
        return this.source;
    }

    /**
     * Parses a {@link TestXMLEvent} from the given root DOM {@link Node}.
     */
    final public static TestXMLEvent parse(Node rootNode, Source<TestXMLEvent, SourceType> source) {

        NodeList children = rootNode.getChildNodes();

        return TestXMLEventType.valueOf(children.item(0).getTextContent()).parseXML(children.item(1), source);
    }

    /**
     * A subclass of {@link TestXMLEvent} for carrying errors.
     */
    final protected static class ErrorXMLEvent extends TestXMLEvent implements ErrorEventDefinition {

        final Throwable error;

        /**
         * Default constructor.
         */
        public ErrorXMLEvent(Throwable error, Source<TestXMLEvent, SourceType> source) {
            super(ERROR, source);

            this.error = error;
        }

        public Throwable getError() {
            return this.error;
        }
    }

    /**
     * A subclass of {@link TestXMLEvent} for holding sequence numbers.
     */
    final protected static class SequenceXMLEvent extends TestXMLEvent implements SequenceEventDefinition {

        final long seqNo;
        final int nmessages;

        /**
         * Default constructor.
         */
        public SequenceXMLEvent(long seqNo, int nmessages, Source<TestXMLEvent, SourceType> source) {
            super(SEQUENCE, source);

            this.seqNo = seqNo;
            this.nmessages = nmessages;
        }

        /**
         * Alternate constructor.
         */
        public SequenceXMLEvent(Node contentNode, Source<TestXMLEvent, SourceType> source) {
            super(SEQUENCE, source);

            NodeList children = contentNode.getChildNodes();

            this.seqNo = Long.parseLong(children.item(0).getTextContent());
            this.nmessages = Integer.parseInt(children.item(1).getTextContent());
        }

        @Override
        protected void getContents(Node contentNode) {

            Document doc = contentNode.getOwnerDocument();

            contentNode.appendChild(doc.createElement("seqNo")) //
                    .setTextContent(Long.toString(this.seqNo));

            contentNode.appendChild(doc.createElement("nmessages")) //
                    .setTextContent(Integer.toString(this.nmessages));
        }

        public long getSeqNo() {
            return this.seqNo;
        }

        public int nmessages() {
            return this.nmessages;
        }
    }

    /**
     * A subclass of {@link TestXMLEvent} for carrying data.
     */
    final protected static class DataXMLEvent extends TestXMLEvent implements DataEventDefinition {

        final byte[] data;

        /**
         * Default constructor.
         */
        public DataXMLEvent(byte[] data, Source<TestXMLEvent, SourceType> source) {
            super(DATA, source);

            this.data = data;
        }

        /**
         * Alternate constructor.
         */
        public DataXMLEvent(Node contentNode, Source<TestXMLEvent, SourceType> source) {
            super(DATA, source);

            this.data = Base64.base64ToBytes(contentNode.getFirstChild().getTextContent());
        }

        @Override
        protected void getContents(Node contentNode) {

            Document doc = contentNode.getOwnerDocument();

            contentNode.appendChild(doc.createElement("data")) //
                    .setTextContent(Base64.bytesToBase64(this.data));
        }

        public byte[] getData() {
            return this.data;
        }
    }

    /**
     * An instantiation of {@link AbstractReceiverVerifier}.
     */
    final protected static class ReceiverXMLVerifier extends AbstractReceiverVerifier<TestXMLEvent> {

        /**
         * Default constructor.
         */
        protected ReceiverXMLVerifier() {
        }

        @Override
        protected TestXMLEvent createSequenceEvent(long seqNo, int nmessages) {
            return new SequenceXMLEvent(seqNo, nmessages, null);
        }
    }

    /**
     * An instantiation of {@link AbstractSenderVerifier}.
     */
    final protected static class SenderXMLVerifier extends AbstractSenderVerifier<TestXMLEvent> {

        /**
         * Default constructor.
         */
        protected SenderXMLVerifier(long seqNo, int nmessages, int meanMessageSize) {
            super(seqNo, nmessages, meanMessageSize);
        }

        @Override
        protected TestXMLEvent createDataEvent(byte[] data) {
            return new DataXMLEvent(data, null);
        }
    }
}

/**
 * <p>
 * Copyright (c) 2009 Roy Liu<br>
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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Queue;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import shared.net.ConnectionHandler;

/**
 * An implementation of {@link FilterFactory} for reading and writing XML DOM {@link Element}s.
 * 
 * @author Roy Liu
 */
public class XmlFilterFactory //
        implements FilterFactory<Filter<ByteBuffer, Element>, ByteBuffer, Element, ConnectionHandler<?>>, //
        Filter<ByteBuffer, Element> {

    /**
     * The global instance.
     */
    final protected static XmlFilterFactory instance = new XmlFilterFactory();

    /**
     * A {@link DocumentBuilder} local to the current thread.
     */
    final protected static ThreadLocal<DocumentBuilder> builderLocal = new ThreadLocal<DocumentBuilder>() {

        @Override
        protected DocumentBuilder initialValue() {

            try {

                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                dbf.setValidating(true);
                dbf.setFeature("http://apache.org/xml/features/validation/dynamic", true);

                DocumentBuilder db = dbf.newDocumentBuilder();
                db.setErrorHandler(new ErrorHandler() {

                    @Override
                    public void error(SAXParseException exception) throws SAXException {
                        throw exception;
                    }

                    @Override
                    public void fatalError(SAXParseException exception) throws SAXException {
                        throw exception;
                    }

                    @Override
                    public void warning(SAXParseException exception) throws SAXException {
                        throw exception;
                    }
                });

                return db;

            } catch (ParserConfigurationException e) {

                throw new RuntimeException(e);
            }
        }
    };

    /**
     * A {@link Transformer} local to the current thread.
     */
    final protected static ThreadLocal<Transformer> transformerLocal = new ThreadLocal<Transformer>() {

        @Override
        protected Transformer initialValue() {

            try {

                return TransformerFactory.newInstance().newTransformer();

            } catch (TransformerConfigurationException e) {

                throw new RuntimeException(e);
            }
        }
    };

    /**
     * Gets the global instance.
     */
    final public static XmlFilterFactory getInstance() {
        return instance;
    }

    /**
     * Creates a new XML {@link Filter}.
     */
    final public static Filter<ByteBuffer, Element> newFilter() {
        return instance;
    }

    /**
     * Creates a new {@link Document}.
     */
    final public static Document newDocument() {
        return builderLocal.get().newDocument();
    }

    /**
     * Default constructor.
     */
    public XmlFilterFactory() {
    }

    @Override
    public void applyInbound(Queue<ByteBuffer> inputs, Queue<Element> outputs) {

        for (ByteBuffer bb; (bb = inputs.poll()) != null;) {

            int save = bb.position();
            int size = bb.remaining();

            assert (bb.limit() == bb.capacity());

            InputStream in = new ByteArrayInputStream((size == bb.capacity()) ? bb.array() //
                    : Arrays.copyOfRange(bb.array(), save, save + size));

            final Document doc;

            try {

                doc = builderLocal.get().parse(in);

            } catch (RuntimeException e) {

                throw e;

            } catch (Exception e) {

                throw new RuntimeException(e);
            }

            outputs.add(doc.getDocumentElement());

            bb.position(save + size);
        }
    }

    @Override
    public void applyOutbound(Queue<Element> inputs, Queue<ByteBuffer> outputs) {

        for (Element elt; (elt = inputs.poll()) != null;) {

            StringWriter sw = new StringWriter();

            try {

                transformerLocal.get().transform(new DOMSource(elt), new StreamResult(sw));

            } catch (TransformerException e) {

                throw new RuntimeException(e);
            }

            outputs.add(ByteBuffer.wrap(sw.toString().getBytes()));
        }
    }

    @Override
    public Filter<ByteBuffer, Element> newFilter(ConnectionHandler<?> handler) {
        return newFilter();
    }
}

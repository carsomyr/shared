/**
 * <p>
 * Copyright (c) 2010 Roy Liu<br>
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

package shared.util;

import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Node;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * A static utility class for reading and writing XML.
 * 
 * @author Roy Liu
 */
public class XmlBase {

    /**
     * An implementation of {@link EntityResolver} that finds external entities on the class path of the current
     * thread's context class loader.
     */
    final public static EntityResolver classpathResolver = new EntityResolver() {

        @Override
        public InputSource resolveEntity(String publicId, String systemId) {

            final URI uri;

            try {

                uri = new URI(systemId);

            } catch (URISyntaxException e) {

                throw new RuntimeException(e);
            }

            if (!uri.getScheme().equals("classpath")) {
                return null;
            }

            InputStream in = Thread.currentThread().getContextClassLoader() //
                    .getResourceAsStream(uri.getSchemeSpecificPart());
            return (in != null) ? new InputSource(in) : null;
        }
    };

    /**
     * An implementation of {@link ErrorHandler} that immediately throws any {@link SAXException} passed to it,
     * regardless of severity.
     */
    final public static ErrorHandler strictErrorHandler = new ErrorHandler() {

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
     * Converts the given DOM {@link Node} to a string.
     */
    final public static String toString(Node node) {

        StringWriter sw = new StringWriter();

        try {

            transformerLocal.get().transform(new DOMSource(node), new StreamResult(sw));

        } catch (TransformerException e) {

            throw new RuntimeException(e);
        }

        return sw.toString();
    }

    // Dummy constructor.
    XmlBase() {
    }
}

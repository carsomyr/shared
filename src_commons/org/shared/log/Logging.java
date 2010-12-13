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

package org.shared.log;

import static org.shared.util.XmlBase.classpathResolver;
import static org.shared.util.XmlBase.strictErrorHandler;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.xml.DOMConfigurator;
import org.w3c.dom.Document;

/**
 * A static utility class for setting up logging frameworks.
 * 
 * @author Roy Liu
 */
public class Logging {

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
                db.setErrorHandler(strictErrorHandler);
                db.setEntityResolver(classpathResolver);

                return db;

            } catch (ParserConfigurationException e) {

                throw new RuntimeException(e);
            }
        }
    };

    /**
     * Configures <a href="http://logging.apache.org/log4j/">Log4J</a> from an XML file found on the class path of
     * {@link Thread#getContextClassLoader()}.
     * 
     * @param pathname
     *            the class pathname.
     */
    final public static void configureLog4J(String pathname) {

        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(pathname);

        final Document doc;

        try {

            doc = builderLocal.get().parse(in);

        } catch (RuntimeException e) {

            throw e;

        } catch (Exception e) {

            throw new RuntimeException(e);
        }

        DOMConfigurator.configure(doc.getDocumentElement());
    }

    // Dummy constructor.
    Logging() {
    }
}

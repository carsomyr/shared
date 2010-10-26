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

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Queue;

import org.w3c.dom.Element;

import shared.net.ConnectionHandler;
import shared.util.Control;

/**
 * An implementation of {@link FilterFactory} for reading and writing XML DOM {@link Element}s.
 * 
 * @param <H>
 *            the {@link ConnectionHandler} type.
 * @author Roy Liu
 */
public class XmlFilterFactory<H extends ConnectionHandler<?>> //
        implements FilterFactory<Filter<ByteBuffer, Element>, ByteBuffer, Element, H>, Filter<ByteBuffer, Element> {

    /**
     * The global instance.
     */
    final protected static XmlFilterFactory<?> instance = new XmlFilterFactory<ConnectionHandler<?>>();

    /**
     * Gets the global instance.
     * 
     * @param <H>
     *            the {@link ConnectionHandler} type.
     */
    @SuppressWarnings("unchecked")
    final public static <H extends ConnectionHandler<?>> XmlFilterFactory<H> getInstance() {
        return (XmlFilterFactory<H>) instance;
    }

    /**
     * Creates a new XML {@link Filter}.
     */
    final public static Filter<ByteBuffer, Element> newFilter() {
        return instance;
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

            byte[] array = (size == bb.capacity()) ? bb.array() //
                    : Arrays.copyOfRange(bb.array(), save, save + size);
            outputs.add(Control.parse(array).getDocumentElement());

            bb.position(save + size);
        }
    }

    @Override
    public void applyOutbound(Queue<Element> inputs, Queue<ByteBuffer> outputs) {

        for (Element elt; (elt = inputs.poll()) != null;) {
            outputs.add(ByteBuffer.wrap(Control.toString(elt).getBytes()));
        }
    }

    @Override
    public Filter<ByteBuffer, Element> newFilter(H handler) {
        return newFilter();
    }
}

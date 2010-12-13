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

package org.shared.net.handler;

import java.nio.ByteBuffer;
import java.util.Queue;

import org.shared.event.Handler;
import org.shared.event.Source;
import org.shared.event.XmlEvent;
import org.shared.net.Connection;
import org.shared.net.SourceType;
import org.shared.net.filter.ChainFilterFactory;
import org.shared.net.filter.Filter;
import org.shared.net.filter.FilterFactory;
import org.shared.net.filter.FrameFilterFactory;
import org.shared.net.filter.XmlFilterFactory;
import org.w3c.dom.Element;

/**
 * A null-terminated packet protocol for reading and writing {@link XmlEvent}s. Programmers will have to implement
 * {@link #parse(Element)} for system-specific behavior.
 * 
 * @apiviz.owns org.shared.net.SourceType
 * @param <H>
 *            the parameterization lower bounded by {@link XmlHandler} itself.
 * @param <C>
 *            the {@link Connection} type.
 * @param <T>
 *            the {@link XmlEvent} type.
 * @param <S>
 *            the {@link Source} enumeration type.
 * @author Roy Liu
 */
abstract public class XmlHandler<H extends XmlHandler<H, C, T, S>, C extends Connection, T extends XmlEvent<T, ?, S>, S extends Enum<S>> //
        extends AbstractFilteredHandler<H, C, T> //
        implements Source<T, S>, FilterFactory<Filter<T, Element>, T, Element, H> {

    final S type;

    Handler<T> handler;

    /**
     * Default constructor.
     * 
     * @param name
     *            the name of this connection.
     * @param type
     *            the {@link SourceType}.
     * @param minimumSize
     *            the minimum message buffer size.
     * @param maximumSize
     *            the maximum message buffer size.
     */
    public XmlHandler(String name, S type, int minimumSize, int maximumSize) {
        super(name);

        this.type = type;

        this.handler = new Handler<T>() {

            @Override
            public void handle(T evt) {
                throw new UnsupportedOperationException("The handler is not initialized");
            }
        };

        setFilterFactory(new ChainFilterFactory<ByteBuffer, ByteBuffer, H>() //
                .add(new FrameFilterFactory(minimumSize, maximumSize)) //
                .add(XmlFilterFactory.getInstance()) //
                .add(this));
    }

    /**
     * Alternate constructor.
     */
    public XmlHandler(String name, S type, int maximumSize) {
        this(name, type, maximumSize, maximumSize);
    }

    /**
     * Parses an {@link XmlEvent} from the given root DOM {@link Element}.
     * 
     * @param rootElement
     *            the root DOM {@link Element}.
     * @return the {@link XmlEvent}.
     */
    abstract protected T parse(Element rootElement);

    /**
     * Creates an end-of-stream {@link XmlEvent}.
     * 
     * @return the {@link XmlEvent}.
     */
    abstract protected T createEos();

    /**
     * Creates an error {@link XmlEvent}.
     * 
     * @return the {@link XmlEvent}.
     */
    abstract protected T createError();

    @Override
    public S getType() {
        return this.type;
    }

    @Override
    public Handler<T> getHandler() {
        return this.handler;
    }

    @Override
    public void setHandler(Handler<T> handler) {
        this.handler = handler;
    }

    @Override
    public void onRemote(T evt) {
        send(evt);
    }

    @Override
    public void onReceive(Queue<T> inputs) {

        for (T evt; (evt = inputs.poll()) != null;) {
            onLocal(evt);
        }
    }

    @Override
    public void onClosing(ClosingType type, Queue<T> inputs) {

        switch (type) {

        case EOS:

            onLocal(createEos());

            if (!inputs.isEmpty()) {
                throw new IllegalArgumentException("No more events can remain in queue");
            }

            break;

        case ERROR:
            onLocal(createError());
            break;

        case USER:
            // Do nothing.
            break;

        default:
            throw new IllegalArgumentException("Invalid closing type");
        }
    }

    @Override
    public Filter<T, Element> newFilter(final H handler) {

        return new Filter<T, Element>() {

            @Override
            public void applyInbound(Queue<Element> inputs, Queue<T> outputs) {

                Connection conn = handler.getConnection();
                assert !Thread.holdsLock(conn.getLock()) && conn.isManagerThread();

                for (Element elt; (elt = inputs.poll()) != null;) {
                    outputs.add(parse(elt));
                }
            }

            @Override
            public void applyOutbound(Queue<T> inputs, Queue<Element> outputs) {

                assert Thread.holdsLock(handler.getConnection().getLock());

                for (T evt; (evt = inputs.poll()) != null;) {
                    outputs.add(evt.toDom());
                }
            }
        };
    }

    @Override
    public void onBind(Queue<T> inputs) {
        // Do nothing.
    }

    @Override
    public void onClose() {
        // Do nothing.
    }
}

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

package shared.net;

import java.nio.ByteBuffer;
import java.util.Queue;

import org.w3c.dom.Element;

import shared.event.Handler;
import shared.event.Source;
import shared.event.XmlEvent;
import shared.net.filter.ChainFilterFactory;
import shared.net.filter.Filter;
import shared.net.filter.FilterFactory;
import shared.net.filter.FilteredManagedConnection;
import shared.net.filter.FrameFilterFactory;
import shared.net.filter.XmlFilterFactory;
import shared.util.Control;

/**
 * A null-terminated packet protocol for reading and writing {@link XmlEvent}s. Programmers will have to implement
 * {@link #parse(Element)} for system-specific behavior.
 * 
 * @apiviz.owns shared.net.SourceType
 * @param <C>
 *            the parameterization lower bounded by {@link XmlConnection} itself.
 * @param <T>
 *            the {@link XmlEvent} type.
 * @param <S>
 *            the {@link Source} enumeration type.
 * @author Roy Liu
 */
abstract public class XmlConnection<C extends XmlConnection<C, T, S>, T extends XmlEvent<T, ?, S>, S extends Enum<S>> //
        extends FilteredManagedConnection<C, T> //
        implements Source<T, S>, FilterFactory<Filter<Element, T>, Element, T, C> {

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
     * @param manager
     *            the {@link ConnectionManager} with which this connection will be registered.
     */
    public XmlConnection(String name, S type, int minimumSize, int maximumSize, ConnectionManager manager) {
        super(name, manager);

        this.type = type;

        this.handler = new Handler<T>() {

            @Override
            public void handle(T evt) {
                throw new UnsupportedOperationException("The handler is not initialized");
            }
        };

        setFilterFactory(new ChainFilterFactory<ByteBuffer, ByteBuffer, C>() //
                .add(new FrameFilterFactory<C>(minimumSize, maximumSize)) //
                .add(XmlFilterFactory.getInstance()) //
                .add(this));
    }

    /**
     * Alternate constructor.
     */
    public XmlConnection(String name, S type, int maximumSize, ConnectionManager manager) {
        this(name, type, maximumSize, maximumSize, manager);
    }

    /**
     * Alternate constructor.
     */
    public XmlConnection(String name, S type, int minimumSize, int maximumSize) {
        this(name, type, minimumSize, maximumSize, ConnectionManager.getInstance());
    }

    /**
     * Alternate constructor.
     */
    public XmlConnection(String name, S type, int maximumSize) {
        this(name, type, maximumSize, maximumSize, ConnectionManager.getInstance());
    }

    /**
     * Parses an {@link XmlEvent} from the given root DOM {@link Element}.
     * 
     * @param rootElement
     *            the root DOM {@link Element}. A {@code null} value signifies an end-of-stream.
     * @return the {@link XmlEvent}.
     */
    abstract protected T parse(Element rootElement);

    /**
     * On error.
     */
    abstract protected void onError();

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
        sendOutbound(evt);
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

            onLocal(parse(null));

            Control.checkTrue(inputs.isEmpty(), //
                    "No more events can remain in queue");

            break;

        case ERROR:
            onError();
            break;

        case USER:
            // Do nothing.
            break;

        default:
            throw new IllegalArgumentException("Invalid closing type");
        }
    }

    @Override
    public Filter<Element, T> newFilter(final C connection) {

        return new Filter<Element, T>() {

            @Override
            public void applyInbound(Queue<Element> inputs, Queue<T> outputs) {

                assert !Thread.holdsLock(connection);

                for (Element elt; (elt = inputs.poll()) != null;) {
                    outputs.add(parse(elt));
                }
            }

            @Override
            public void applyOutbound(Queue<T> inputs, Queue<Element> outputs) {

                assert Thread.holdsLock(connection);

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
}

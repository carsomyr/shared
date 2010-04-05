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

package shared.net;

import java.nio.ByteBuffer;
import java.util.Queue;

import org.w3c.dom.Element;

import shared.event.Handler;
import shared.event.Source;
import shared.event.XMLEvent;
import shared.net.filter.ChainFilterFactory;
import shared.net.filter.Filter;
import shared.net.filter.FilterFactory;
import shared.net.filter.FilteredManagedConnection;
import shared.net.filter.FrameFilterFactory;
import shared.net.filter.XMLFilterFactory;
import shared.util.Control;

/**
 * A null-terminated packet protocol for reading and writing {@link XMLEvent}s. Programmers will have to implement
 * {@link #parse(Element)} for system-specific behavior.
 * 
 * @apiviz.owns shared.net.SourceType
 * @param <C>
 *            the parameterization lower bounded by {@link XMLConnection} itself.
 * @param <T>
 *            the parameterization lower bounded by {@link XMLEvent} itself.
 * @param <S>
 *            the {@link Source} enumeration type.
 * @author Roy Liu
 */
abstract public class XMLConnection<C extends XMLConnection<C, T, S>, T extends XMLEvent<T, ?, S>, S extends Enum<S>> //
        extends FilteredManagedConnection<C, T> //
        implements Source<T, S>, FilterFactory<Filter<Element, T>, Element, T, C> {

    final S type;

    int mark;

    Throwable error;

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
    public XMLConnection(String name, S type, int minimumSize, int maximumSize, ConnectionManager manager) {
        super(name, manager);

        this.type = type;

        this.mark = 0;
        this.error = null;
        this.handler = new Handler<T>() {

            public void handle(T evt) {
                throw new IllegalStateException("The handler is not initialized");
            }
        };

        setFilterFactory(new ChainFilterFactory<ByteBuffer, ByteBuffer, C>() //
                .add(new FrameFilterFactory<C>(minimumSize, maximumSize)) //
                .add(XMLFilterFactory.getInstance()) //
                .add(this));
    }

    /**
     * Alternate constructor.
     * 
     * @see #XMLConnection(String, Enum, int, int, ConnectionManager)
     */
    public XMLConnection(String name, S type, int maximumSize, ConnectionManager manager) {
        this(name, type, maximumSize, maximumSize, manager);
    }

    /**
     * Alternate constructor.
     * 
     * @see #XMLConnection(String, Enum, int, int, ConnectionManager)
     */
    public XMLConnection(String name, S type, int minimumSize, int maximumSize) {
        this(name, type, minimumSize, maximumSize, ConnectionManager.getInstance());
    }

    /**
     * Alternate constructor.
     * 
     * @see #XMLConnection(String, Enum, int, int, ConnectionManager)
     */
    public XMLConnection(String name, S type, int maximumSize) {
        this(name, type, maximumSize, maximumSize, ConnectionManager.getInstance());
    }

    /**
     * Parses the given root DOM {@link Element}.
     * 
     * @param rootElement
     *            the root DOM {@link Element}. A {@code null} value signifies an end-of-stream.
     * @return the parsed event.
     */
    abstract protected T parse(Element rootElement);

    /**
     * On error.
     * 
     * @param error
     *            the error.
     */
    abstract protected void onError(Throwable error);

    public S getType() {
        return this.type;
    }

    public Handler<T> getHandler() {
        return this.handler;
    }

    public void setHandler(Handler<T> handler) {
        this.handler = handler;
    }

    public void onRemote(T evt) {
        sendOutbound(evt);
    }

    public void onReceiveInbound(Queue<T> evts) {

        for (T evt; (evt = evts.poll()) != null;) {
            onLocal(evt);
        }
    }

    public void onCloseInbound(Queue<T> evts) {
        onClose();
    }

    public void onEOSInbound(Queue<T> evts) {

        onLocal(parse(null));

        Control.checkTrue(evts.isEmpty(), //
                "No more events can remain in queue");
    }

    public void onError(Throwable error, ByteBuffer bb) {

        this.error = error;

        onError(error);
    }

    public Filter<Element, T> newFilter(final C connection) {

        return new Filter<Element, T>() {

            public void getInbound(Queue<Element> in, Queue<T> out) {

                assert !Thread.holdsLock(connection);

                for (Element inbound; (inbound = in.poll()) != null;) {
                    out.add(parse(inbound));
                }
            }

            public void getOutbound(Queue<T> in, Queue<Element> out) {

                assert Thread.holdsLock(connection);

                for (T outbound; (outbound = in.poll()) != null;) {
                    out.add(outbound.toDOM());
                }
            }
        };
    }

    public void onBindInbound(Queue<T> inbounds) {
        // Do nothing.
    }
}

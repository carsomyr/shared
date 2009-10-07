/**
 * This file is part of the Shared Scientific Toolbox in Java ("this library"). <br />
 * <br />
 * Copyright (C) 2009 Roy Liu <br />
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

package shared.net.filter;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Queue;

import org.w3c.dom.Element;

import shared.net.Connection;
import shared.util.Control;

/**
 * An implementation of {@link FilterFactory} for reading and writing XML DOM {@link Element}s.
 * 
 * @param <C>
 *            the {@link Connection} type.
 * @author Roy Liu
 */
public class XMLFilterFactory<C extends Connection> implements FilterFactory<ByteBuffer, Element, C> {

    /**
     * The global instance.
     */
    final protected static XMLFilterFactory<?> Instance = new XMLFilterFactory<Connection>();

    /**
     * Gets the global instance.
     * 
     * @param <C>
     *            the {@link Connection} type.
     */
    @SuppressWarnings("unchecked")
    final public static <C extends Connection> XMLFilterFactory<C> getInstance() {
        return (XMLFilterFactory<C>) Instance;
    }

    /**
     * Default constructor.
     */
    public XMLFilterFactory() {
    }

    public Filter<ByteBuffer, Element> newFilter(final C connection) {

        return new Filter<ByteBuffer, Element>() {

            public void getInbound(Queue<ByteBuffer> in, Queue<Element> out) {

                assert !Thread.holdsLock(connection);

                for (ByteBuffer bb = null; (bb = in.poll()) != null;) {

                    int save = bb.position();
                    int size = bb.remaining();

                    assert (bb.limit() == bb.capacity());

                    byte[] array = (size == bb.capacity()) ? bb.array() //
                            : Arrays.copyOfRange(bb.array(), save, save + size);
                    out.add(Control.createDocument(array).getDocumentElement());

                    bb.position(save + size);
                }
            }

            public void getOutbound(Queue<Element> in, Queue<ByteBuffer> out) {

                assert Thread.holdsLock(connection);

                for (Element elt = null; (elt = in.poll()) != null;) {
                    out.add(ByteBuffer.wrap(Control.toString(elt).getBytes()));
                }
            }
        };
    }
}

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

package org.shared.net.handler;

import java.nio.ByteBuffer;
import java.util.Queue;

import org.shared.net.Connection;
import org.shared.net.ConnectionHandler;
import org.shared.net.filter.Filter;
import org.shared.net.filter.FilterFactory;

/**
 * Defines a {@link ConnectionHandler} that uses {@link Filter}s to process inbound and outbound data.
 * 
 * @apiviz.owns org.shared.net.filter.FilterFactory
 * @param <H>
 *            the parameterization lower bounded by {@link FilteredHandler} itself.
 * @param <C>
 *            the {@link Connection} type.
 * @param <T>
 *            the {@link Filter} inbound type.
 * @author Roy Liu
 */
public interface FilteredHandler<H extends FilteredHandler<H, C, T>, C extends Connection, T> //
        extends ConnectionHandler<C> {

    /**
     * Sets the {@link FilterFactory} from which appropriate {@link Filter}s will be derived.
     * 
     * @param filterFactory
     *            the {@link FilterFactory}.
     */
    public H setFilterFactory(FilterFactory<? extends Filter<T, ByteBuffer>, T, ByteBuffer, ? super H> filterFactory);

    /**
     * Sends the given output value to the remote host.
     * 
     * @param output
     *            the output value.
     * @return the number of units remaining to be written.
     */
    public int send(T output);

    /**
     * On binding.
     * 
     * @param inputs
     *            the input {@link Queue}.
     */
    public void onBind(Queue<T> inputs);

    /**
     * On receipt of data.
     * 
     * @param inputs
     *            the input {@link Queue}.
     */
    public void onReceive(Queue<T> inputs);

    /**
     * On closure.
     * 
     * @param type
     *            the {@link org.shared.net.ConnectionHandler.ClosingType}.
     * @param inputs
     *            the input {@link Queue}.
     * @see org.shared.net.ConnectionHandler.ClosingType
     */
    public void onClosing(ClosingType type, Queue<T> inputs);
}

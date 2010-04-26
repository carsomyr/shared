/**
 * <p>
 * Copyright (C) 2009 Roy Liu<br />
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
import java.util.Queue;

import shared.net.Connection;

/**
 * Defines a {@link Connection} that uses {@link Filter}s to process inbound and outbound data.
 * 
 * @apiviz.owns shared.net.filter.FilterFactory
 * @param <C>
 *            the parameterization lower bounded by {@link FilteredConnection} itself.
 * @param <T>
 *            the {@link Filter} output type.
 * @author Roy Liu
 */
public interface FilteredConnection<C extends FilteredConnection<C, T>, T> extends Connection {

    /**
     * Sets the {@link FilterFactory} from which appropriate {@link Filter}s will be derived.
     * 
     * @param filterFactory
     *            the {@link FilterFactory}.
     */
    public C setFilterFactory(FilterFactory<? extends Filter<ByteBuffer, T>, ByteBuffer, T, ? super C> filterFactory);

    /**
     * Sends the given outbound value.
     * 
     * @param outbound
     *            the outbound value.
     * @return the number of units remaining to be written.
     */
    public int sendOutbound(T outbound);

    /**
     * Receives the given inbound values.
     * 
     * @param inbounds
     *            the inbound values.
     */
    public void onReceiveInbound(Queue<T> inbounds);

    /**
     * On successful binding.
     * 
     * @param inbounds
     *            the inbound values.
     */
    public void onBindInbound(Queue<T> inbounds);

    /**
     * On user-requested close.
     * 
     * @param inbounds
     *            the inbound values.
     */
    public void onCloseInbound(Queue<T> inbounds);

    /**
     * On end-of-stream.
     * 
     * @param inbounds
     *            the inbound values.
     */
    public void onEOSInbound(Queue<T> inbounds);
}

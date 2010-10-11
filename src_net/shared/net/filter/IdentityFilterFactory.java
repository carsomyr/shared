/**
 * <p>
 * Copyright (c) 2009-2010 Roy Liu<br>
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

import java.util.Queue;

import shared.net.Connection;

/**
 * An implementation of {@link FilterFactory} that creates identity filters, which simply pull values off of the input
 * queue and push them onto the output queue.
 * 
 * @param <T>
 *            the inbound and outbound type.
 * @param <C>
 *            the {@link Connection} type.
 * @author Roy Liu
 */
public class IdentityFilterFactory<T, C extends Connection> //
        implements FilterFactory<OobFilter<T, T>, T, T, C>, OobFilter<T, T> {

    /**
     * The global {@link IdentityFilterFactory} instance.
     */
    final protected static IdentityFilterFactory<?, ?> instance = new IdentityFilterFactory<Object, Connection>();

    /**
     * Gets the global instance.
     * 
     * @param <T>
     *            the inbound and outbound type.
     * @param <C>
     *            the {@link Connection} type.
     */
    @SuppressWarnings("unchecked")
    final public static <T, C extends Connection> IdentityFilterFactory<T, C> getInstance() {
        return (IdentityFilterFactory<T, C>) instance;
    }

    /**
     * Creates a new identity {@link Filter}.
     * 
     * @param <T>
     *            the input and output type.
     */
    @SuppressWarnings("unchecked")
    final public static <T> OobFilter<T, T> newFilter() {
        return (OobFilter<T, T>) instance;
    }

    /**
     * Default constructor.
     */
    public IdentityFilterFactory() {
    }

    @Override
    public void applyInbound(Queue<T> inputs, Queue<T> outputs) {
        Filters.transfer(inputs, outputs);
    }

    @Override
    public void applyOutbound(Queue<T> inputs, Queue<T> outputs) {
        Filters.transfer(inputs, outputs);
    }

    @Override
    public void applyInboundOob( //
            Queue<T> inputs, Queue<OobEvent> inputEvts, //
            Queue<T> outputs, Queue<OobEvent> outputEvts) {

        Filters.transfer(inputEvts, outputEvts);
        applyInbound(inputs, outputs);
    }

    @Override
    public void applyOutboundOob( //
            Queue<T> inputs, Queue<OobEvent> inputEvts, //
            Queue<T> outputs, Queue<OobEvent> outputEvts) {

        Filters.transfer(inputEvts, outputEvts);
        applyOutbound(inputs, outputs);
    }

    @Override
    public OobFilter<T, T> newFilter(C connection) {
        return newFilter();
    }
}

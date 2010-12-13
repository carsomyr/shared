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

package org.shared.net.filter;

import java.util.Queue;

/**
 * Defines a {@link Filter} that requires special processing on {@link OobEvent}s.
 * 
 * @param <I>
 *            the inbound type.
 * @param <O>
 *            the outbound type.
 * @author Roy Liu
 */
public interface OobFilter<I, O> extends Filter<I, O> {

    /**
     * Processes an {@link OobEvent} as it would apply to the inbound direction.
     * 
     * @param inputs
     *            the input event {@link Queue}.
     * @param outputs
     *            the output event {@link Queue}.
     */
    public void applyInboundOob(Queue<OobEvent> inputs, Queue<OobEvent> outputs);

    /**
     * Processes an {@link OobEvent} as it would apply to the outbound direction.
     * 
     * @param inputs
     *            the input event {@link Queue}.
     * @param outputs
     *            the output event {@link Queue}.
     */
    public void applyOutboundOob(Queue<OobEvent> inputs, Queue<OobEvent> outputs);
}

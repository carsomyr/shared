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

package org.shared.net;

import java.nio.ByteBuffer;

/**
 * Defines a handler for {@link Connection} callbacks.
 * 
 * @apiviz.has org.shared.net.ConnectionHandler.ClosingType - - - argument
 * @param <C>
 *            the {@link Connection} type.
 * @author Roy Liu
 */
public interface ConnectionHandler<C extends Connection> {

    /**
     * An enumeration of the ways in which a connection may be closed.
     */
    public static enum ClosingType {

        /**
         * Denotes closure by user request.
         */
        USER, //

        /**
         * Denotes closure by end-of-stream.
         */
        EOS, //

        /**
         * Denotes closure by error.
         */
        ERROR;
    }

    /**
     * On binding.
     */
    public void onBind();

    /**
     * On receipt of data.
     * 
     * @param bb
     *            the {@link ByteBuffer} containing data.
     */
    public void onReceive(ByteBuffer bb);

    /**
     * On closure.
     * 
     * @param type
     *            the {@link ClosingType}.
     * @param bb
     *            the {@link ByteBuffer} containing data. It must be completely drained, as this is the final callback.
     * @see ClosingType
     */
    public void onClosing(ClosingType type, ByteBuffer bb);

    /**
     * On completion of closure.
     */
    public void onClose();

    /**
     * Gets the {@link Connection} associated with this handler.
     */
    public C getConnection();

    /**
     * Sets the {@link Connection} associated with this handler.
     * 
     * @param conn
     *            the {@link Connection}.
     */
    public void setConnection(C conn);

    /**
     * Creates a human-readable representation of this handler.
     */
    @Override
    public String toString();
}

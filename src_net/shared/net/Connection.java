/**
 * <p>
 * Copyright (c) 2005 Roy Liu<br>
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

import java.io.Closeable;
import java.nio.ByteBuffer;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

/**
 * Defines a managed connection.
 * 
 * @author Roy Liu
 */
public interface Connection extends Closeable, Executor {

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
     * An enumeration of the ways in which a connection may be initialized.
     */
    public static enum InitializationType {

        /**
         * Denotes connecting to a remote address.
         */
        CONNECT, //

        /**
         * Denotes accepting from a local address.
         */
        ACCEPT, //

        /**
         * Denotes registration of an existing, unmanaged connection.
         */
        REGISTER;
    }

    /**
     * An enumeration of the managed operations manipulable by the user.
     */
    public static enum OperationType {

        /**
         * Denotes managed reads.
         */
        READ, //

        /**
         * Denotes managed writes.
         */
        WRITE;
    }

    // Callback methods are not necessarily thread-safe, and thus must execute on internal threads.

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

    // User methods must be thread-safe, and thus may execute on any thread.

    /**
     * Initializes this connection.
     * 
     * @param type
     *            the {@link InitializationType}.
     * @param argument
     *            the argument.
     * @param <R>
     *            the result type.
     * @param <T>
     *            the argument type.
     * @return a completion {@link Future} for the result.
     * @see InitializationType
     */
    public <R, T> Future<R> init(InitializationType type, T argument);

    /**
     * Sends data to the remote host.
     * 
     * @param bb
     *            the {@link ByteBuffer} containing data. It must be in ready-to-read mode.
     * @return the number of {@code byte}s remaining in this connection's write buffer; is greater than {@code 0} if and
     *         only if not all {@code byte}s could be written out to the network, and have to be stored locally for the
     *         time being.
     */
    public int send(ByteBuffer bb);

    /**
     * Enables/disables various managed operations:
     * <ul>
     * <li>{@link OperationType#READ} -- The {@link #onReceive(ByteBuffer)} callback is enabled/disabled. With argument
     * {@code true}, this method shall guarantee that {@link #onReceive(ByteBuffer)} will subsequently be called at
     * least once.</li>
     * <li>{@link OperationType#WRITE} -- Managed writes of data buffered by the {@link #send(ByteBuffer)} method are
     * enabled/disabled.</li>
     * </ul>
     * 
     * @param type
     *            the {@link OperationType}.
     * @param enabled
     *            whether the given operation is enabled or disabled.
     * @see OperationType
     */
    public void setEnabled(OperationType type, boolean enabled);

    /**
     * Gets the exception that occurred.
     */
    public Throwable getException();

    /**
     * Sets the exception and triggers connection closure.
     * 
     * @param exception
     *            the exception.
     * @see ClosingType#ERROR
     */
    public void setException(Throwable exception);

    /**
     * Executes the given code snippet on this connection's manager thread.
     * 
     * @param r
     *            the code snippet to execute.
     */
    @Override
    public void execute(Runnable r);

    /**
     * Closes this connection.
     */
    @Override
    public void close();

    /**
     * Creates a human-readable representation of this connection.
     */
    @Override
    public String toString();
}

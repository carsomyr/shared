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

package org.shared.array.kernel;

import org.shared.array.Array;
import org.shared.util.Services;

/**
 * An implementation of {@link ArrayIoKernel} that is a gateway to multiple I/O schemes.
 * 
 * @apiviz.owns org.shared.array.kernel.MatlabIoKernel
 * @author Roy Liu
 */
public class ModalArrayIoKernel implements ArrayIoKernel {

    volatile ArrayIoKernel opKernel;

    /**
     * Default constructor.
     */
    public ModalArrayIoKernel() {

        this.opKernel = Services.createService(ArrayIoKernel.class);

        if (this.opKernel == null) {
            this.opKernel = new MatlabIoKernel();
        }
    }

    /**
     * Attempts to use the underlying {@link MatlabIoKernel}.
     */
    public void useMatlabIo() {
        this.opKernel = new MatlabIoKernel();
    }

    @Override
    public <T extends Array<T, ?>> byte[] getBytes(T array) {
        return this.opKernel.getBytes(array);
    }

    @Override
    public <T extends Array<T, ?>> T parse(byte[] data) {
        return this.opKernel.parse(data);
    }
}

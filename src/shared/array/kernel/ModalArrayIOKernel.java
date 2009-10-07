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

package shared.array.kernel;

import shared.array.Array;
import shared.util.Services;

/**
 * An implementation of {@link ArrayIOKernel} that is a gateway to multiple I/O schemes.
 * 
 * @apiviz.owns shared.array.kernel.MatlabIOKernel
 * @author Roy Liu
 */
public class ModalArrayIOKernel implements ArrayIOKernel {

    volatile ArrayIOKernel opKernel;

    /**
     * Default constructor.
     */
    public ModalArrayIOKernel() {

        this.opKernel = Services.createService(ArrayIOKernel.class);

        if (this.opKernel == null) {
            this.opKernel = new MatlabIOKernel();
        }
    }

    /**
     * Attempts to use the underlying {@link MatlabIOKernel}.
     */
    public void useMatlabIO() {
        this.opKernel = new MatlabIOKernel();
    }

    public <T extends Array<T, E>, E> byte[] getBytes(T array) {
        return this.opKernel.getBytes(array);
    }

    public <T extends Array<T, ?>> T parse(byte[] data) {
        return this.opKernel.parse(data);
    }
}

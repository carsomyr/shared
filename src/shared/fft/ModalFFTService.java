/**
 * This file is part of the Shared Scientific Toolbox in Java ("this library"). <br />
 * <br />
 * Copyright (C) 2008 The Regents of the University of California <br />
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

package shared.fft;

import shared.util.Services;

/**
 * An {@link FFTService} backed by a service provider as well as a pure Java implementation.
 * 
 * @apiviz.owns shared.fft.JavaFFTService
 * @author Roy Liu
 */
public class ModalFFTService implements FFTService {

    volatile FFTService service;

    /**
     * Default constructor. Tries to obtain a service provider. Failing that, falls back to {@link JavaFFTService}.
     */
    public ModalFFTService() {

        this.service = Services.createService(FFTService.class);

        if (this.service == null) {
            this.service = new JavaFFTService();
        }
    }

    /**
     * Uses the service provider obtained from {@link Services#createService(Class)}.
     */
    public boolean useProvider() {

        this.service = Services.createService(FFTService.class);

        if (this.service == null) {

            this.service = new JavaFFTService();

            return false;

        } else {

            return true;
        }
    }

    /**
     * Uses {@link JavaFFTService}.
     */
    public void useJava() {
        this.service = new JavaFFTService();
    }

    public void rfft(int[] dims, double[] in, double[] out) {
        this.service.rfft(dims, in, out);
    }

    public void rifft(int[] dims, double[] in, double[] out) {
        this.service.rifft(dims, in, out);
    }

    public void fft(int[] dims, double[] in, double[] out) {
        this.service.fft(dims, in, out);
    }

    public void ifft(int[] dims, double[] in, double[] out) {
        this.service.ifft(dims, in, out);
    }

    public void setHint(String name, String value) {
        this.service.setHint(name, value);
    }

    public String getHint(String name) {
        return this.service.getHint(name);
    }
}

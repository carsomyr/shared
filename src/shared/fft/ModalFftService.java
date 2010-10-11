/**
 * <p>
 * Copyright (c) 2008 The Regents of the University of California<br>
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

package shared.fft;

import shared.util.Services;

/**
 * An {@link FftService} backed by a service provider as well as a pure Java implementation.
 * 
 * @apiviz.owns shared.fft.JavaFftService
 * @author Roy Liu
 */
public class ModalFftService implements FftService {

    volatile FftService service;

    /**
     * Default constructor. Tries to obtain a service provider. Failing that, falls back to {@link JavaFftService}.
     */
    public ModalFftService() {

        this.service = Services.createService(FftService.class);

        if (this.service == null) {
            this.service = new JavaFftService();
        }
    }

    /**
     * Attempts to use the {@link FftService} obtained from {@link Services#createService(Class)}.
     * 
     * @return {@code true} if and only if an implementation could be obtained without resorting to the default service.
     */
    public boolean useRegisteredService() {

        this.service = Services.createService(FftService.class);

        if (this.service == null) {

            this.service = new JavaFftService();

            return false;

        } else {

            return true;
        }
    }

    /**
     * Uses {@link JavaFftService}.
     */
    public void useJava() {
        this.service = new JavaFftService();
    }

    @Override
    public void rfft(int[] dims, double[] in, double[] out) {
        this.service.rfft(dims, in, out);
    }

    @Override
    public void rifft(int[] dims, double[] in, double[] out) {
        this.service.rifft(dims, in, out);
    }

    @Override
    public void fft(int[] dims, double[] in, double[] out) {
        this.service.fft(dims, in, out);
    }

    @Override
    public void ifft(int[] dims, double[] in, double[] out) {
        this.service.ifft(dims, in, out);
    }

    @Override
    public void setHint(String name, String value) {
        this.service.setHint(name, value);
    }

    @Override
    public String getHint(String name) {
        return this.service.getHint(name);
    }
}

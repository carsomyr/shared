/**
 * <p>
 * Copyright (c) 2007-2010 The Regents of the University of California<br>
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

package sharedx.fftw;

import java.lang.ref.Reference;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import shared.fft.FFTService;
import shared.util.ReferenceReaper;
import shared.util.ReferenceReaper.ReferenceType;

/**
 * An <a href="http://www.fftw.org/">FFTW3</a>-backed service provider ascribing to {@link FFTService}.
 * 
 * @apiviz.owns sharedx.fftw.Plan
 * @author Roy Liu
 */
public class FFTWService implements FFTService {

    int mode;

    final ConcurrentMap<PlanKey, Reference<Plan>> planMap;
    final ReferenceReaper<Plan> rr;

    /**
     * Default constructor.
     */
    public FFTWService() {

        this.planMap = new ConcurrentHashMap<PlanKey, Reference<Plan>>();
        this.rr = new ReferenceReaper<Plan>();

        this.mode = Plan.FFTW_MEASURE;
    }

    @Override
    public void rfft(int[] dims, double[] in, double[] out) {
        transform(Plan.R_TO_C, dims, this.mode, in, out);
    }

    @Override
    public void rifft(int[] dims, double[] in, double[] out) {
        transform(Plan.C_TO_R, dims, this.mode, in, out);
    }

    @Override
    public void fft(int[] dims, double[] in, double[] out) {
        transform(Plan.FORWARD, dims, this.mode, in, out);
    }

    @Override
    public void ifft(int[] dims, double[] in, double[] out) {
        transform(Plan.BACKWARD, dims, this.mode, in, out);
    }

    @Override
    public void setHint(String name, String value) {

        if (name.equals("wisdom")) {

            Plan.importWisdom(value);

        } else if (name.equals("mode")) {

            if (value.equals("estimate")) {

                this.mode = Plan.FFTW_ESTIMATE;

            } else if (value.equals("measure")) {

                this.mode = Plan.FFTW_MEASURE;

            } else if (value.equals("patient")) {

                this.mode = Plan.FFTW_PATIENT;

            } else if (value.equals("exhaustive")) {

                this.mode = Plan.FFTW_EXHAUSTIVE;

            } else {

                throw new IllegalArgumentException("Invalid execution mode");
            }

        } else {

            throw new IllegalArgumentException("Unknown hint");
        }
    }

    @Override
    public String getHint(String name) {

        if (name.equals("wisdom")) {

            return Plan.exportWisdom();

        } else if (name.equals("mode")) {

            return modeToString(this.mode);

        } else {

            throw new IllegalArgumentException("Unknown hint");
        }
    }

    /**
     * Delegates to {@link ConcurrentHashMap#toString()}.
     */
    @Override
    public String toString() {
        return this.planMap.toString();
    }

    /**
     * Gets a string representation of the execution mode.
     */
    final protected static String modeToString(int mode) {

        switch (mode) {

        case Plan.FFTW_ESTIMATE:
            return "estimate";

        case Plan.FFTW_MEASURE:
            return "measure";

        case Plan.FFTW_PATIENT:
            return "patient";

        case Plan.FFTW_EXHAUSTIVE:
            return "exhaustive";

        default:
            throw new IllegalArgumentException("Invalid execution mode");
        }
    }

    /**
     * Gets a string representation of the FFT type.
     */
    final protected static String typeToString(int mode) {

        switch (mode) {

        case Plan.R_TO_C:
            return "r2c";

        case Plan.C_TO_R:
            return "c2r";

        case Plan.FORWARD:
            return "forward";

        case Plan.BACKWARD:
            return "backward";

        default:
            throw new IllegalArgumentException("Invalid execution mode");
        }
    }

    /**
     * Performs a transform of the given type and dimensions on the given input/output arrays. If a cached transform
     * doesn't exist, a new one is created and cached. Computation time initially depends on the amount of
     * precomputation desired.
     * 
     * @param type
     *            the kind of transform.
     * @param dims
     *            the dimensions of the transform.
     * @param mode
     *            the transform mode.
     * @param in
     *            the input array.
     * @param out
     *            the output array.
     */
    protected void transform(int type, int[] dims, int mode, double[] in, double[] out) {

        final PlanKey key = new PlanKey(type, dims, mode);

        Reference<Plan> ref = this.planMap.get(key);

        Plan plan = (ref != null) ? ref.get() : null;

        if (plan == null) {

            plan = new Plan(type, dims, mode);

            this.planMap.put(key, this.rr.wrap(ReferenceType.SOFT, plan, new Runnable() {

                @Override
                public void run() {
                    FFTWService.this.planMap.remove(key);
                }
            }));
        }

        plan.transform(in, out);
    }
}

/**
 * This file is part of the Shared Scientific Toolbox in Java ("this library"). <br />
 * <br />
 * Copyright (C) 2007 Roy Liu, The Regents of the University of California <br />
 * <br />
 * This library is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License, version 2, as published by the Free Software Foundation. <br />
 * <br />
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. <br />
 * <br />
 * You should have received a copy of the GNU General Public License along with this library. If not, see <a
 * href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
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

    public void rfft(int[] dims, double[] in, double[] out) {
        transform(Plan.R2C, dims, this.mode, in, out);
    }

    public void rifft(int[] dims, double[] in, double[] out) {
        transform(Plan.C2R, dims, this.mode, in, out);
    }

    public void fft(int[] dims, double[] in, double[] out) {
        transform(Plan.FORWARD, dims, this.mode, in, out);
    }

    public void ifft(int[] dims, double[] in, double[] out) {
        transform(Plan.BACKWARD, dims, this.mode, in, out);
    }

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

        case Plan.R2C:
            return "r2c";

        case Plan.C2R:
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
            this.rr.register(ReferenceType.SOFT, plan, this.planMap, key);
        }

        plan.transform(in, out);
    }
}

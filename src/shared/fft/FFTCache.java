/**
 * This file is part of the Shared Scientific Toolbox in Java ("this library"). <br />
 * <br />
 * Copyright (C) 2007 Roy Liu, The Regents of the University of California <br />
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

import java.lang.ref.Reference;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import shared.array.AbstractArray;
import shared.util.ReferenceReaper;
import shared.util.ReferenceReaper.ReferenceType;

/**
 * A class for thread-safe caching of padded and FFT'd convolution kernels.
 * 
 * @apiviz.composedOf shared.fft.FFTCache.CacheKey
 * @param <C>
 *            the complex array type.
 * @param <R>
 *            the real array type.
 * @author Roy Liu
 */
abstract public class FFTCache<C extends AbstractArray<C, C, R, ?>, R extends AbstractArray<R, C, R, ?>> {

    final ConcurrentMap<CacheKey, Reference<C>> kernelMap;
    final ReferenceReaper<C> rr;

    /**
     * Default constructor.
     */
    public FFTCache() {

        this.kernelMap = new ConcurrentHashMap<CacheKey, Reference<C>>();
        this.rr = new ReferenceReaper<C>();
    }

    /**
     * Delegates to {@link ConcurrentHashMap#toString()}.
     */
    @Override
    public String toString() {
        return this.kernelMap.toString();
    }

    /**
     * Attempts to retrieve a cached, padded, and FFT'd companion of the given {@link AbstractArray}.
     * 
     * @param tag
     *            the query key.
     * @param dims
     *            the size of the cached result.
     * @param <A>
     *            the query array type.
     * @return the cached result, or a newly created and inserted one.
     */
    public <A extends AbstractArray<?, C, ?, ?>> C get(A tag, int... dims) {

        final CacheKey key = new CacheKey(tag, dims);

        Reference<C> ref = this.kernelMap.get(key);

        C kernel = (ref != null) ? ref.get() : null;

        if (kernel == null) {

            kernel = createCacheable(tag, dims);

            this.kernelMap.put(key, this.rr.wrap(ReferenceType.SOFT, kernel, new Runnable() {

                public void run() {
                    FFTCache.this.kernelMap.remove(key);
                }
            }));
        }

        return kernel;
    }

    /**
     * Creates an instance of the complex array type.
     * 
     * @param array
     *            the query key.
     * @param dims
     *            the size of the cached result.
     * @param <A>
     *            the query array type.
     * @return an instance of the complex array type.
     */
    abstract protected <A extends AbstractArray<?, C, ?, ?>> C createCacheable(A array, int[] dims);

    /**
     * A lookup key class for cached kernels.
     */
    protected class CacheKey {

        final AbstractArray<?, C, ?, ?> tag;
        final int[] dims;

        /**
         * Default constructor.
         */
        protected CacheKey(AbstractArray<?, C, ?, ?> tag, int[] dims) {

            this.tag = tag;
            this.dims = dims;
        }

        /**
         * Computes the <tt>xor</tt> of the hash codes of the array and the padded dimensions.
         */
        @Override
        public int hashCode() {
            return this.tag.hashCode() ^ Arrays.hashCode(this.dims);
        }

        /**
         * Checks for equality on the array and the padded dimensions.
         */
        @Override
        public boolean equals(Object o) {

            CacheKey key = (CacheKey) o;
            return key.tag.equals(this.tag) && Arrays.equals(key.dims, this.dims);
        }
    }
}

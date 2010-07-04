/**
 * <p>
 * Copyright (C) 2007-2010 The Regents of the University of California<br />
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
        @SuppressWarnings("unchecked")
        @Override
        public boolean equals(Object o) {

            CacheKey key = (CacheKey) o;
            return key.tag.equals(this.tag) && Arrays.equals(key.dims, this.dims);
        }
    }
}

/**
 * <p>
 * Copyright (c) 2007-2010 Roy Liu<br>
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

package shared.util;

import java.lang.ref.Reference;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import shared.util.ReferenceReaper.ReferenceType;

/**
 * A global registry of various {@link Service}s and their implementing classes.
 * 
 * @apiviz.owns shared.util.Service
 * @author Roy Liu
 */
public class Services {

    /**
     * A global instance.
     */
    final protected static Services Instance = new Services();

    final ConcurrentMap<Class<? extends Service>, Reference<Class<? extends Service>>> serviceMap;
    final ReferenceReaper<Class<? extends Service>> rr;

    /**
     * Default constructor.
     */
    protected Services() {

        this.serviceMap = new ConcurrentHashMap<Class<? extends Service>, Reference<Class<? extends Service>>>();
        this.rr = new ReferenceReaper<Class<? extends Service>>();
    }

    /**
     * Creates an implementing class instance from the given specification superclass.
     * 
     * @param <A>
     *            the implementing class type, which is a lower bound on the superclass type.
     * @param specClass
     *            the specification superclass.
     * @return an instance of the implementing class.
     */
    @SuppressWarnings("unchecked")
    public static <A extends Service> A createService(Class<? super A> specClass) {

        try {

            Reference<Class<? extends Service>> ref = Instance.serviceMap.get(specClass);

            if (ref == null) {
                return null;
            }

            Class<? extends Service> implClass = ref.get();

            if (implClass == null) {
                return null;
            }

            return ((Class<? extends A>) implClass).newInstance();

        } catch (Exception e) {

            throw new RuntimeException(e);
        }
    }

    /**
     * Registers an implementing class with the given specification superclass.
     * 
     * @param <A>
     *            the superclass type, which is an upper bound on the implementation class type.
     * @param specClass
     *            the specification superclass.
     * @param implClass
     *            the implementing class.
     */
    public static <A extends Service> void registerService(final Class<A> specClass, Class<? extends A> implClass) {

        Instance.serviceMap.put(specClass, Instance.rr.wrap(ReferenceType.WEAK, implClass, new Runnable() {

            @Override
            public void run() {
                Instance.serviceMap.remove(specClass);
            }
        }));
    }
}

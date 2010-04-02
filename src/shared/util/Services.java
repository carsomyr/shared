/**
 * This file is part of the Shared Scientific Toolbox in Java ("this library"). <br />
 * <br />
 * Copyright (C) 2007 Roy Liu <br />
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

            public void run() {
                Instance.serviceMap.remove(specClass);
            }
        }));
    }
}

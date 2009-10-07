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

package shared.event;

/**
 * A state-based {@link Processor} subclass that is a {@link Source} in itself.
 * 
 * @apiviz.owns shared.event.StateTable
 * @param <T>
 *            the parameterization lower bounded by {@link Event} itself.
 * @param <E>
 *            the {@link Event} enumeration type.
 * @param <S>
 *            the {@link Source} enumeration type.
 * @author Roy Liu
 */
abstract public class StateProcessor<T extends Event<T, E, S>, E extends Enum<E>, S extends Enum<S>> extends
        Processor<T> implements Source<T, S>, Handler<T> {

    /**
     * Default constructor.
     */
    public StateProcessor(String name) {
        super(name);
    }

    public Handler<T> getHandler() {
        return this;
    }

    public void onRemote(T evt) {
        throw new UnsupportedOperationException();
    }

    public void setHandler(Handler<T> handler) {
        throw new UnsupportedOperationException();
    }
}

/**
 * This file is part of the Shared Scientific Toolbox in Java ("this library"). <br />
 * <br />
 * Copyright (C) 2005 Roy Liu <br />
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
 * Defines functionality that can be expected from all events.
 * 
 * @apiviz.owns shared.event.Source
 * @param <T>
 *            the parameterization lower bounded by {@link Event} itself.
 * @param <E>
 *            the {@link Event} enumeration type.
 * @param <S>
 *            the {@link Source} enumeration type.
 * @author Roy Liu
 */
public interface Event<T extends Event<T, E, S>, E extends Enum<E>, S extends Enum<S>> extends EnumType<E> {

    /**
     * Gets the {@link Source} from which this event originated.
     */
    public Source<T, S> getSource();

    /**
     * Gets the {@link Event} type.
     */
    public E getType();
}

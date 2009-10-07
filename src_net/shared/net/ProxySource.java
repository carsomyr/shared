/**
 * This file is part of the Shared Scientific Toolbox in Java ("this library"). <br />
 * <br />
 * Copyright (C) 2009 Roy Liu <br />
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

package shared.net;

import shared.event.Handler;
import shared.event.Source;

/**
 * An event {@link Source} that represents its {@link AbstractManagedConnection} parent.
 * 
 * @author Roy Liu
 */
public class ProxySource<C extends AbstractManagedConnection<C>> //
        implements Source<InterestEvent<?>, SourceType> {

    final C connection;

    /**
     * Default constructor.
     */
    protected ProxySource(C connection) {
        this.connection = connection;
    }

    /**
     * Gets the parent connection.
     */
    protected C getConnection() {
        return this.connection;
    }

    public void onLocal(InterestEvent<?> evt) {
        this.connection.getThread().onLocal(evt);
    }

    public void close() {
        throw new UnsupportedOperationException();
    }

    public SourceType getType() {
        throw new UnsupportedOperationException();
    }

    public Handler<InterestEvent<?>> getHandler() {
        throw new UnsupportedOperationException();
    }

    public void setHandler(Handler<InterestEvent<?>> handler) {
        throw new UnsupportedOperationException();
    }

    public void onRemote(InterestEvent<?> evt) {
        throw new UnsupportedOperationException();
    }
}

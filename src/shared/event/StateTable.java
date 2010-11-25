/**
 * <p>
 * Copyright (c) 2005 Roy Liu<br>
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

package shared.event;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import shared.event.Transitions.Transition;

/**
 * A finite state machine class.
 * 
 * @apiviz.composedOf shared.event.StateTable.StateHandler
 * @apiviz.has shared.event.Event - - - event
 * @apiviz.has shared.event.Transitions - - - argument
 * @param <X>
 *            the state enumeration type.
 * @param <Y>
 *            the {@link Event} enumeration type.
 * @param <Z>
 *            the {@link Event} type.
 * @author Roy Liu
 */
public class StateTable<X extends Enum<X>, Y extends Enum<Y>, Z extends Event<Z, Y, ?>> {

    /**
     * An array of all four wildcard combinations.
     */
    final protected static String[] wildcardCombinations = new String[] { "**", "* ", " *", "  " };

    final int nStates;
    final int nEvents;
    final StateHandler[] backingArray;

    /**
     * Default constructor.
     */
    @SuppressWarnings({ "unchecked" })
    public StateTable(Object target, Class<X> stateClass, Class<Y> eventTypeClass, String group) {

        this.nStates = stateClass.getEnumConstants().length;
        this.nEvents = eventTypeClass.getEnumConstants().length;
        this.backingArray = new StateTable.StateHandler[this.nStates * this.nEvents];

        int stride = this.nEvents;

        final Map<String, List<StateHandler>> handlersMap = new HashMap<String, List<StateHandler>>();

        for (String str : wildcardCombinations) {
            handlersMap.put(str, new ArrayList<StateHandler>());
        }

        for (Class<?> clazz = target.getClass(); //
        clazz != null && !clazz.getName().startsWith("java.") && !clazz.getName().startsWith("javax."); //
        clazz = clazz.getSuperclass()) {

            outerLoop: for (Field field : clazz.getDeclaredFields()) {

                Transitions ts = field.getAnnotation(Transitions.class);
                Transition t = field.getAnnotation(Transition.class);

                if (ts != null && t != null) {
                    throw new IllegalArgumentException("Transition and Transitions annotations " //
                            + "cannot occur simultaneously");
                }

                final Transition[] transitions;

                if (ts != null) {

                    transitions = ts.transitions();

                } else if (t != null) {

                    transitions = new Transition[] { t };

                } else {

                    continue outerLoop;
                }

                final Object obj;

                field.setAccessible(true);

                try {

                    obj = field.get(target);

                } catch (IllegalAccessException e) {

                    throw new RuntimeException(e);

                } finally {

                    field.setAccessible(false);
                }

                if (!(obj instanceof Handler<?>)) {
                    throw new IllegalArgumentException("Field does not reference an event handler");
                }

                final Handler<Z> handler = (Handler<Z>) obj;
                final String name = field.getName();

                innerLoop: for (Transition transition : transitions) {

                    if (!transition.group().equals(group)) {
                        continue innerLoop;
                    }

                    X currentState = !transition.currentState().equals("*") ? Enum.valueOf( //
                            stateClass, transition.currentState()) : null;
                    Y eventType = !transition.eventType().equals("*") ? Enum.valueOf( //
                            eventTypeClass, transition.eventType()) : null;

                    final StateHandler stateHandler;

                    if (!transition.nextState().equals("")) {

                        final X nextState = Enum.valueOf(stateClass, transition.nextState());

                        stateHandler = new StateHandler(currentState, eventType) {

                            @Override
                            public void handle(EnumStatus<X> stateObj, Z evt) {

                                handler.handle(evt);
                                stateObj.setStatus(nextState);
                            }

                            @Override
                            public String toString() {
                                return String.format("%s -> %s : %s", //
                                        super.toString(), nextState, name);
                            }
                        };

                    } else {

                        stateHandler = new StateHandler(currentState, eventType) {

                            @Override
                            public void handle(EnumStatus<X> stateObj, Z evt) {
                                handler.handle(evt);
                            }

                            @Override
                            public String toString() {
                                return String.format("%s : %s", //
                                        super.toString(), name);
                            }
                        };
                    }

                    final String key;

                    if (currentState == null && eventType == null) {

                        key = "**";

                    } else if (currentState == null && eventType != null) {

                        key = "* ";

                    } else if (currentState != null && eventType == null) {

                        key = " *";

                    } else {

                        key = "  ";
                    }

                    handlersMap.get(key).add(stateHandler);
                }
            }
        }

        int[] rowRange = new int[this.nStates];

        for (int row = 0, nRows = rowRange.length; row < nRows; row++) {
            rowRange[row] = row;
        }

        int[] colRange = new int[this.nEvents];

        for (int col = 0, nCols = colRange.length; col < nCols; col++) {
            colRange[col] = col;
        }

        for (String key : wildcardCombinations) {

            for (StateHandler stateHandler : handlersMap.get(key)) {

                int[] rows = (stateHandler.state != null) ? new int[] { stateHandler.state.ordinal() } //
                        : rowRange;
                int[] cols = (stateHandler.eventType != null) ? new int[] { stateHandler.eventType.ordinal() } //
                        : colRange;

                for (int row : rows) {

                    for (int col : cols) {
                        this.backingArray[stride * row + col] = stateHandler;
                    }
                }
            }
        }
    }

    /**
     * Alternate constructor.
     */
    public StateTable(Object target, Class<X> stateClass, Class<Y> eventTypeClass) {
        this(target, stateClass, eventTypeClass, "");
    }

    /**
     * Creates a human-readable representation of this table.
     */
    @Override
    public String toString() {

        Formatter f = new Formatter();

        int nRows = this.nStates;
        int nCols = this.nEvents;
        int stride = this.nEvents;

        for (int row = 0; row < nRows; row++) {

            for (int col = 0; col < nCols; col++) {

                StateHandler stateHandler = this.backingArray[stride * row + col];

                if (stateHandler != null) {
                    f.format("%s%n", stateHandler);
                }
            }
        }

        return f.toString();
    }

    /**
     * Looks up and handles an {@link Event} based on the current state and the event type.
     * 
     * @param stateObj
     *            the {@link EnumStatus} object.
     * @param evt
     *            the {@link Event}.
     */
    public void lookup(EnumStatus<X> stateObj, Z evt) {

        int stride = this.nEvents;
        StateHandler handler = this.backingArray[stride * stateObj.getStatus().ordinal() + evt.getType().ordinal()];

        if (handler != null) {
            handler.handle(stateObj, evt);
        }
    }

    /**
     * Defines an {@link Event} handler that may mutate {@link EnumStatus} objects.
     */
    abstract protected class StateHandler {

        /**
         * The state.
         */
        final protected X state;

        /**
         * The event type.
         */
        final protected Y eventType;

        /**
         * Default constructor.
         */
        protected StateHandler(X state, Y eventType) {

            this.state = state;
            this.eventType = eventType;
        }

        /**
         * Creates a human-readable representation of this handler.
         */
        @Override
        public String toString() {
            return String.format("(%s, %s)", //
                    (this.state != null) ? this.state : "*", //
                    (this.eventType != null) ? this.eventType : "*");
        }

        /**
         * Handles an {@link Event}. May optionally mutate the given {@link EnumStatus} object.
         */
        abstract protected void handle(EnumStatus<X> state, Z evt);
    }
}

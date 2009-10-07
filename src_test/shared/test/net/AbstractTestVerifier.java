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

package shared.test.net;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.Semaphore;

import shared.event.EnumStatus;
import shared.event.Event;
import shared.event.Handler;
import shared.event.SourceLocal;
import shared.event.StateTable;
import shared.event.Transitions;
import shared.event.Transitions.Transition;
import shared.net.SourceType;
import shared.test.net.TestXMLEvent.TestXMLEventType;
import shared.util.Arithmetic;
import shared.util.Control;

/**
 * An implementation of {@link SourceLocal} that serves as a base class for checking the contents of incoming events
 * against their expected values.
 * 
 * @apiviz.owns shared.test.net.AbstractTestVerifier.VerifierStatus
 * @author Roy Liu
 */
abstract public class AbstractTestVerifier<T extends Event<T, TestXMLEventType, SourceType>> //
        implements Verifier, SourceLocal<T>, EnumStatus<AbstractTestVerifier.VerifierStatus> {

    /**
     * An enumeration of {@link Verifier} states.
     */
    public enum VerifierStatus {

        /**
         * Indicates that the {@link Verifier} has not yet been initialized.
         */
        VIRGIN, //

        /**
         * Indicates that the {@link Verifier} presides over an active connection.
         */
        RUN, //

        /**
         * Indicates a successful run.
         */
        SUCCESS, //

        /**
         * Indicates a failed run.
         */
        FAILURE;
    }

    /**
     * Defines data events.
     */
    public interface DataEventDefinition {

        /**
         * Gets the data.
         */
        public byte[] getData();
    }

    /**
     * Defines sequence events.
     */
    public interface SequenceEventDefinition {

        /**
         * Gets the sequence number.
         */
        public long getSeqNo();

        /**
         * Gets the number of messages.
         */
        public int nmessages();
    }

    /**
     * Defines error events.
     */
    public interface ErrorEventDefinition {

        /**
         * Gets the cause of the error.
         */
        public Throwable getError();
    }

    final Semaphore semaphore;

    long seqNo;
    long seqNoForward;
    int nmessages;

    VerifierStatus status;

    /**
     * Default constructor.
     */
    protected AbstractTestVerifier(long seqNo, int nmessages) {

        this.semaphore = new Semaphore(0);

        this.seqNo = seqNo;
        this.seqNoForward = seqNo + 1;
        this.nmessages = nmessages;

        this.status = VerifierStatus.VIRGIN;
    }

    public VerifierStatus getStatus() {
        return this.status;
    }

    public void setStatus(VerifierStatus status) {
        this.status = status;
    }

    public void sync() {

        this.semaphore.acquireUninterruptibly();

        Control.checkTrue(this.status == VerifierStatus.SUCCESS, //
                "Transport failed");
    }

    /**
     * Creates a block of data for transport.
     */
    final protected static byte[] createData(long seqNo, int messageSize) {

        ByteBuffer bb = ByteBuffer.allocate(4 + (messageSize << 3));

        bb.putInt(messageSize);

        for (int i = 0; i < messageSize; i++) {
            bb.putLong(seqNo);
        }

        return bb.array();
    }

    /**
     * A subclass of {@link AbstractTestVerifier} that maintains receiver state.
     */
    abstract protected static class AbstractReceiverVerifier<T extends Event<T, TestXMLEventType, SourceType>> extends
            AbstractTestVerifier<T> {

        final StateTable<VerifierStatus, TestXMLEventType, T> fsm;

        @Transition(currentState = "VIRGIN", eventType = "SEQUENCE", nextState = "RUN")
        final Handler<T> handleVirginToRun = new Handler<T>() {

            public void handle(T evt) {

                AbstractReceiverVerifier<T> arv = AbstractReceiverVerifier.this;

                SequenceEventDefinition sEvt = (SequenceEventDefinition) evt;

                arv.seqNo = sEvt.getSeqNo();
                arv.nmessages = sEvt.nmessages();

                evt.getSource().onRemote(evt);
            }
        };

        @Transition(currentState = "RUN", eventType = "DATA")
        final Handler<T> handleData = new Handler<T>() {

            public void handle(T evt) {

                AbstractReceiverVerifier<T> arv = AbstractReceiverVerifier.this;

                byte[] received = ((DataEventDefinition) evt).getData();

                Control.checkTrue(Arrays.equals(received, createData( //
                        arv.seqNo, (received.length - 4) >>> 3)), //
                        "Received data does not match expected data");

                evt.getSource().onRemote(createSequenceEvent( //
                        arv.seqNo++, //
                        arv.nmessages--));
            }
        };

        @Transitions(transitions = {
        //
                @Transition(currentState = "*", eventType = "ERROR"), //
                @Transition(currentState = "*", eventType = "END_OF_STREAM") //
        })
        final Handler<T> handleEOS = new Handler<T>() {

            public void handle(T evt) {

                AbstractReceiverVerifier<T> arv = AbstractReceiverVerifier.this;

                arv.status = (arv.nmessages == 0) ? VerifierStatus.SUCCESS : VerifierStatus.FAILURE;
                arv.semaphore.release();
            }
        };

        /**
         * Default constructor.
         */
        protected AbstractReceiverVerifier() {
            super(Long.MIN_VALUE, Integer.MIN_VALUE);

            this.fsm = new StateTable<VerifierStatus, TestXMLEventType, T>(this, //
                    VerifierStatus.class, TestXMLEventType.class);
        }

        public void onLocal(T evt) {
            this.fsm.lookup(this, evt);
        }

        /**
         * Creates a sequence event.
         */
        abstract protected T createSequenceEvent(long seqNo, int nmessages);
    }

    /**
     * A subclass of {@link AbstractTestVerifier} that maintains sender state.
     */
    abstract protected static class AbstractSenderVerifier<T extends Event<T, TestXMLEventType, SourceType>> extends
            AbstractTestVerifier<T> {

        final StateTable<VerifierStatus, TestXMLEventType, T> fsm;

        @Transition(currentState = "VIRGIN", eventType = "SEQUENCE", nextState = "RUN")
        final Handler<T> handleVirginToRun = new Handler<T>() {

            public void handle(T evt) {

                AbstractSenderVerifier<T> asv = AbstractSenderVerifier.this;

                SequenceEventDefinition sEvt = (SequenceEventDefinition) evt;

                Control.checkTrue(sEvt.getSeqNo() == asv.seqNo //
                        && sEvt.nmessages() == asv.nmessages, //
                        "Invalid sequence event");

                evt.getSource().onRemote(createDataEvent( //
                        createData(asv.seqNo, 0)));

                asv.status = VerifierStatus.RUN;
            }
        };

        @Transition(currentState = "RUN", eventType = "SEQUENCE")
        final Handler<T> handleSequence = new Handler<T>() {

            public void handle(T evt) {

                AbstractSenderVerifier<T> asv = AbstractSenderVerifier.this;

                SequenceEventDefinition sEvt = (SequenceEventDefinition) evt;

                Control.checkTrue(sEvt.getSeqNo() == asv.seqNo++ //
                        && sEvt.nmessages() == asv.nmessages--, //
                        "Invalid sequence event");

                if (asv.nmessages == 0) {

                    // We finished sending everything.
                    Control.close(evt.getSource());

                    asv.status = VerifierStatus.SUCCESS;
                    asv.semaphore.release();

                } else if (asv.seqNo == asv.seqNoForward) {

                    // Otherwise, create a data event.
                    int len = Arithmetic.nextInt(asv.meanMessageSize << 1);

                    // Simulate bursty behavior by transmitting events as either singletons or
                    // pairs.
                    for (int i = 0, n = Math.min(asv.nmessages, Arithmetic.nextInt(2) + 1); i < n; i++, asv.seqNoForward++) {
                        evt.getSource().onRemote( //
                                createDataEvent(createData(asv.seqNo + i, len)));
                    }
                }
            }
        };

        @Transitions(transitions = {
        //
                @Transition(currentState = "*", eventType = "ERROR"), //
                @Transition(currentState = "*", eventType = "END_OF_STREAM") //
        })
        final Handler<T> handleEOS = new Handler<T>() {

            public void handle(T evt) {

                AbstractSenderVerifier<T> asv = AbstractSenderVerifier.this;

                asv.status = VerifierStatus.FAILURE;
                asv.semaphore.release();
            }
        };

        final int meanMessageSize;

        /**
         * Default constructor.
         */
        protected AbstractSenderVerifier(long seqNo, int nmessages, int meanMessageSize) {
            super(seqNo, nmessages);

            this.meanMessageSize = meanMessageSize;

            this.fsm = new StateTable<VerifierStatus, TestXMLEventType, T>(this, //
                    VerifierStatus.class, TestXMLEventType.class);
        }

        public void onLocal(T evt) {
            this.fsm.lookup(this, evt);
        }

        /**
         * Creates a data event.
         */
        abstract protected T createDataEvent(byte[] data);
    }
}
/**
 * <p>
 * Copyright (c) 2008 Roy Liu<br>
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

package org.shared.test.net;

import static org.shared.test.net.AllNetTests.randomSource;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.FutureTask;

import org.shared.event.EnumStatus;
import org.shared.event.Event;
import org.shared.event.Handler;
import org.shared.event.SourceLocal;
import org.shared.event.StateTable;
import org.shared.event.Transitions;
import org.shared.event.Transitions.Transition;
import org.shared.net.SourceType;
import org.shared.test.net.TestXmlEvent.TestXmlEventType;

/**
 * An implementation of {@link SourceLocal} that serves as a base class for checking the contents of incoming events
 * against their expected values.
 * 
 * @apiviz.owns org.shared.test.net.AbstractTestVerifier.VerifierStatus
 * @author Roy Liu
 */
abstract public class AbstractTestVerifier<T extends Event<T, TestXmlEventType, SourceType>> //
        extends FutureTask<Object> //
        implements SourceLocal<T>, EnumStatus<AbstractTestVerifier.VerifierStatus> {

    /**
     * A null {@link Runnable} that has an empty {@link Runnable#run()} method.
     */
    final protected static Runnable nullRunnable = new Runnable() {

        @Override
        public void run() {
        }
    };

    /**
     * An enumeration of verifier states.
     */
    public enum VerifierStatus {

        /**
         * Indicates that the verifier has not yet been initialized.
         */
        VIRGIN, //

        /**
         * Indicates that the verifier presides over an active connection.
         */
        RUN, //

        /**
         * Indicates that the run has finished.
         */
        FINISHED;
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
        public int nMessages();
    }

    /**
     * Defines error events.
     */
    public interface ErrorEventDefinition {

        /**
         * Gets the cause of the error.
         */
        public Throwable getException();
    }

    long seqNo;
    long seqNoForward;
    int nMessages;

    VerifierStatus status;

    /**
     * Default constructor.
     */
    protected AbstractTestVerifier(long seqNo, int nMessages) {
        super(nullRunnable, null);

        this.seqNo = seqNo;
        this.seqNoForward = seqNo + 1;
        this.nMessages = nMessages;

        this.status = VerifierStatus.VIRGIN;
    }

    @Override
    public VerifierStatus getStatus() {
        return this.status;
    }

    @Override
    public void setStatus(VerifierStatus status) {
        this.status = status;
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
    abstract protected static class AbstractReceiverVerifier<T extends Event<T, TestXmlEventType, SourceType>> //
            extends AbstractTestVerifier<T> {

        final StateTable<VerifierStatus, TestXmlEventType, T> fsm;

        @Transition(currentState = "VIRGIN", eventType = "SEQUENCE", nextState = "RUN")
        final Handler<T> handleVirginToRun = new Handler<T>() {

            @Override
            public void handle(T evt) {

                AbstractReceiverVerifier<T> arv = AbstractReceiverVerifier.this;

                SequenceEventDefinition sEvt = (SequenceEventDefinition) evt;

                arv.seqNo = sEvt.getSeqNo();
                arv.nMessages = sEvt.nMessages();

                evt.getSource().onRemote(evt);
            }
        };

        @Transition(currentState = "RUN", eventType = "DATA")
        final Handler<T> handleData = new Handler<T>() {

            @Override
            public void handle(T evt) {

                AbstractReceiverVerifier<T> arv = AbstractReceiverVerifier.this;

                byte[] received = ((DataEventDefinition) evt).getData();

                if (!Arrays.equals(received, createData(arv.seqNo, (received.length - 4) >>> 3))) {
                    throw new IllegalStateException("Received data does not match expected data");
                }

                evt.getSource().onRemote(createSequenceEvent( //
                        arv.seqNo++, //
                        arv.nMessages--));
            }
        };

        @Transitions(transitions = {
                //
                @Transition(currentState = "*", eventType = "ERROR"), //
                @Transition(currentState = "*", eventType = "END_OF_STREAM") //
        })
        final Handler<T> handleEos = new Handler<T>() {

            @Override
            public void handle(T evt) {

                AbstractReceiverVerifier<T> arv = AbstractReceiverVerifier.this;

                arv.setStatus(VerifierStatus.FINISHED);

                if (arv.nMessages == 0) {

                    arv.set(null);

                } else {

                    arv.setException(new IllegalStateException("Transport failed"));
                }
            }
        };

        /**
         * Default constructor.
         */
        protected AbstractReceiverVerifier() {
            super(Long.MIN_VALUE, Integer.MIN_VALUE);

            this.fsm = new StateTable<VerifierStatus, TestXmlEventType, T>(this, //
                    VerifierStatus.class, TestXmlEventType.class);
        }

        @Override
        public void onLocal(T evt) {
            this.fsm.lookup(this, evt);
        }

        /**
         * Creates a sequence event.
         */
        abstract protected T createSequenceEvent(long seqNo, int nMessages);
    }

    /**
     * A subclass of {@link AbstractTestVerifier} that maintains sender state.
     */
    abstract protected static class AbstractSenderVerifier<T extends Event<T, TestXmlEventType, SourceType>> //
            extends AbstractTestVerifier<T> {

        final StateTable<VerifierStatus, TestXmlEventType, T> fsm;

        @Transition(currentState = "VIRGIN", eventType = "SEQUENCE", nextState = "RUN")
        final Handler<T> handleVirginToRun = new Handler<T>() {

            @Override
            public void handle(T evt) {

                AbstractSenderVerifier<T> asv = AbstractSenderVerifier.this;

                SequenceEventDefinition sEvt = (SequenceEventDefinition) evt;

                if (sEvt.getSeqNo() != asv.seqNo || sEvt.nMessages() != asv.nMessages) {
                    throw new IllegalStateException("Invalid sequence event");
                }

                evt.getSource().onRemote(createDataEvent( //
                        createData(asv.seqNo, 0)));

                asv.status = VerifierStatus.RUN;
            }
        };

        @Transition(currentState = "RUN", eventType = "SEQUENCE")
        final Handler<T> handleSequence = new Handler<T>() {

            @Override
            public void handle(T evt) {

                AbstractSenderVerifier<T> asv = AbstractSenderVerifier.this;

                SequenceEventDefinition sEvt = (SequenceEventDefinition) evt;

                if (sEvt.getSeqNo() != asv.seqNo++ || sEvt.nMessages() != asv.nMessages--) {
                    throw new IllegalStateException("Invalid sequence event");
                }

                if (asv.nMessages == 0) {

                    // We finished sending everything.
                    evt.getSource().close();

                    asv.setStatus(VerifierStatus.FINISHED);
                    asv.set(null);

                } else if (asv.seqNo == asv.seqNoForward) {

                    // Otherwise, create a data event.
                    int len = randomSource.nextInt(asv.meanMessageSize << 1);

                    // Simulate bursty behavior by transmitting events as either singletons or pairs.
                    for (int i = 0, n = Math.min(asv.nMessages, randomSource.nextInt(2) + 1); //
                    i < n; //
                    i++, asv.seqNoForward++) {
                        evt.getSource().onRemote(createDataEvent(createData(asv.seqNo + i, len)));
                    }
                }
            }
        };

        @Transitions(transitions = {
                //
                @Transition(currentState = "*", eventType = "ERROR"), //
                @Transition(currentState = "*", eventType = "END_OF_STREAM") //
        })
        final Handler<T> handleEos = new Handler<T>() {

            @Override
            public void handle(T evt) {

                AbstractSenderVerifier<T> asv = AbstractSenderVerifier.this;

                asv.setStatus(VerifierStatus.FINISHED);
                asv.setException(new IllegalStateException("Transport failed"));
            }
        };

        final int meanMessageSize;

        /**
         * Default constructor.
         */
        protected AbstractSenderVerifier(long seqNo, int nMessages, int meanMessageSize) {
            super(seqNo, nMessages);

            this.meanMessageSize = meanMessageSize;

            this.fsm = new StateTable<VerifierStatus, TestXmlEventType, T>(this, //
                    VerifierStatus.class, TestXmlEventType.class);
        }

        @Override
        public void onLocal(T evt) {
            this.fsm.lookup(this, evt);
        }

        /**
         * Creates a data event.
         */
        abstract protected T createDataEvent(byte[] data);
    }
}

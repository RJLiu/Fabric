/*
 *  Copyright 2016,2017 DTCC, Fujitsu Australia Software Technology, IBM - All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.hyperledger.fabric.sdk;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperledger.fabric.protos.common.Common.Block;
import org.hyperledger.fabric.sdk.exception.InvalidProtocolBufferRuntimeException;

import static org.hyperledger.fabric.protos.peer.PeerEvents.Event;

/**
 * A wrapper for the Block returned in an Event
 *
 * @see Block
 */
public class BlockEvent extends BlockInfo {
    private static final Log logger = LogFactory.getLog(BlockEvent.class);

    /**
     * Get eventhub that received the event
     *
     * @return eventhub that received the event
     */

    public EventHub getEventHub() {
        return eventHub;
    }

    private final EventHub eventHub;
    private final Event event;

    public Event getEvent() {
        return event;
    }

    /**
     * creates a BlockEvent object by parsing the input Block and retrieving its constituent Transactions
     *
     * @param eventHub a Hyperledger Fabric Block message
     * @throws InvalidProtocolBufferException
     * @see Block
     */
    BlockEvent(EventHub eventHub, Event event) throws InvalidProtocolBufferException {
        super(event.getBlock());
        this.eventHub = eventHub;
        this.event = event;
    }

    TransactionEvent getTransactionEvent(int index) throws InvalidProtocolBufferException {

        return new TransactionEvent((TansactionEnvelopeInfo) getEnvelopeInfo(index), index);
    }

    public class TransactionEvent extends BlockInfo.TansactionEnvelopeInfo {
        TransactionEvent(TansactionEnvelopeInfo tansactionEnvelopeInfo, int index) {
            super(tansactionEnvelopeInfo.getTransactionDeserializer(), index);
        }

        EventHub getEventHub() {

            return BlockEvent.this.getEventHub();
        }
    }

    List<TransactionEvent> getTransactionEventsList() {

        ArrayList<TransactionEvent> ret = new ArrayList<TransactionEvent>(getEnvelopCount());
        for (TransactionEvent transactionEvent : getTransactionEvents()) {
            ret.add(transactionEvent);
        }

        return ret;

    }

    public Iterable<TransactionEvent> getTransactionEvents() {

        return new TransactionEventIterable();

    }

    class TransactionEventIterator implements Iterator<TransactionEvent> {
        int ci = 0;
        final int max;

        TransactionEventIterator() {
            max = getEnvelopCount();

        }

        @Override
        public boolean hasNext() {
            return ci < max;

        }

        @Override
        public TransactionEvent next() {

            try {
                return getTransactionEvent(ci++);
            } catch (InvalidProtocolBufferException e) {
                throw new InvalidProtocolBufferRuntimeException(e);
            }

        }

    }

    class TransactionEventIterable implements Iterable<TransactionEvent> {

        @Override
        public Iterator<TransactionEvent> iterator() {
            return new TransactionEventIterator();
        }
    }

} // BlockEvent
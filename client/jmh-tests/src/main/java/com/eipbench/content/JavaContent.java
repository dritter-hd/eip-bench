package com.eipbench.content;

import com.fasterxml.jackson.databind.JsonNode;
import com.eipbench.content.Content;
import com.eipbench.states.fast.FastJsonMessageBatchSet;
import com.eipbench.content.OrderMessageSet;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Setup;

import java.util.Collection;
import java.util.Iterator;

public class JavaContent extends Content {
    private Iterator<Collection<JsonNode>> iterator;
    private Collection<JsonNode> message;

    public FastJsonMessageBatchSet fastJson = new FastJsonMessageBatchSet("order-messages-json", OrderMessageSet.MESSAGE_SET_FILE, 1500000);

    @Setup(Level.Trial)
    public void parse() {
        if (!offHeapMessages) {
            messageSet.parse(batchSize, msgScaleLevel);
        } else {
            fastJson.load(batchSize);
        }
    }

    @Setup(Level.Iteration)
    public void prepareIterator() {
        if (!offHeapMessages) {
            iterator = messageSet.getJsonMessageListIterator();
        } else {
            fastJson.reset();
        }
    }

    @Setup(Level.Invocation)
    public void prepareMessage() {
        if (!offHeapMessages) {
            if (!iterator.hasNext()) {
                iterator = messageSet.getJsonMessageListIterator();
            }

            message = iterator.next();
        } else {
            message = fastJson.getNextBatch();
        }
    }

    public Collection<JsonNode> getMessage() {
        return message;
    }
}

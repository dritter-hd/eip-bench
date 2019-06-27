package com.eipbench.states.fast;

import com.fasterxml.jackson.databind.JsonNode;
import com.eipbench.states.Content;
import com.eipbench.generator.OrderMessageSet;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Setup;

import java.util.Collection;

public class FastJavaContent extends Content {
    private FastJsonMessageBatchSet messageSet = new FastJsonMessageBatchSet("order-messages-json", OrderMessageSet.MESSAGE_SET_FILE, 1500000);

    private Collection<JsonNode> message;

    @Setup(Level.Trial)
    public void load() {
        messageSet.load(batchSize);
    }

    @Setup(Level.Iteration)
    public void reset() {
        messageSet.reset();
    }

    @Setup(Level.Invocation)
    public void prepareMessage() {
        message = messageSet.getNextBatch();
    }

    public Collection<JsonNode> getMessage() {
        return message;
    }
}

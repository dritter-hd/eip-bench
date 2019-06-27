package com.eipbench.states.fast;

import com.fasterxml.jackson.databind.JsonNode;
import net.openhft.chronicle.map.ChronicleMap;
import net.openhft.chronicle.map.ChronicleMapBuilder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class FastJsonMessageBatchSet extends FastMessageBatchSet {
    private final int totalMessageCount;
    ChronicleMap<Integer, ObjectNodeBatch> batches;

    public FastJsonMessageBatchSet(String name, File allMessagesJsonFile, int totalMessageCount) {
        super(name, allMessagesJsonFile);
        this.totalMessageCount = totalMessageCount;
    }

    protected void createBatchMap(int batchSize, File mmapFile) throws IOException {
        batches = ChronicleMapBuilder.of(Integer.class, ObjectNodeBatch.class)
                .entries(totalMessageCount/batchSize)
                .averageValueSize(800*batchSize)
                .createPersistedTo(mmapFile);
    }

    protected Batcher<JsonNode> createBatcher(final int batchSize) {
        return new Batcher<JsonNode>(batchSize) {
            @Override
            protected void addBatch(ArrayList<JsonNode> currentBatch) {
                ObjectNodeBatch objectNodeBatch = new ObjectNodeBatch();
                objectNodeBatch.setObjectNode(currentBatch);
                batches.put(allBatchCounter, objectNodeBatch);
            }

            @Override
            protected JsonNode convertMessage(JsonNode message) {
                return message;
            }
        };
    }

    private ObjectNodeBatch batch = new ObjectNodeBatch();
    public ArrayList<JsonNode> getNextBatch() {
        return batches.getUsing(iterate(),batch).getObjectNode();
    }
}

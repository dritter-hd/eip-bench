package com.eipbench.states.fast;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;

public abstract class Batcher<T> implements InlineMessageParser.MessageHandler {
    private final int batchSize;
    int batchCounter = 0;
    int allBatchCounter = 0;
    ArrayList<T> currentBatch;

    public Batcher(int batchSize) {
        this.batchSize = batchSize;
        currentBatch = new ArrayList<>(batchSize);
    }

    @Override
    public void handle(JsonNode message) {
        if (batchCounter >= batchSize) {
            /*ObjectNodeBatch objectNodeBatch = new ObjectNodeBatch();
            objectNodeBatch.setObjectNode(currentBatch);
            batches.put(allBatchCounter, objectNodeBatch);*/
            addBatch(currentBatch);

            currentBatch = new ArrayList<>(batchSize);
            batchCounter = 0;

            allBatchCounter++;
        }

        batchCounter++;

        currentBatch.add(convertMessage(message));
    }

    protected abstract void addBatch(ArrayList<T> currentBatch);
    protected abstract T convertMessage(JsonNode message);

    public int getAllBatchCounter() {
        return allBatchCounter;
    }
}

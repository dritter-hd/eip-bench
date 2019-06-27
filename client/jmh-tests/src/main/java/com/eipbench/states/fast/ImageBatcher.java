package com.eipbench.states.fast;

import java.util.ArrayList;
import java.util.List;

public abstract class ImageBatcher<T> {
    private final int batchSize;
    int batchCounter = 0;
    int allBatchCounter = 0;
    ArrayList<T> currentBatch;

    public ImageBatcher(int batchSize) {
        this.batchSize = batchSize;
        currentBatch = new ArrayList<>(batchSize);
    }

    public void handle(byte[] message) {
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

    protected abstract void addBatch(List<T> currentBatch);
    protected abstract T convertMessage(byte[] message);

    public int getAllBatchCounter() {
        return allBatchCounter;
    }
}

package com.eipbench.states.fast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public abstract class FastMessageBatchSet {
    private static final File BASEDIR = new File(System.getProperty("java.io.tmpdir") + File.separator + "eipbench");
    public static final File CACHE_DIR = new File(BASEDIR, "cache");

    private int batchSize;
    private int batchCount;
    private int iteratorCounter = 0;

    private final String name;
    private final File mmapFile;
    private final File mmapPropsFile;
    private final File jsonAllMessagesFile;

    public FastMessageBatchSet(String name, File jsonAllMessagesFile) {
        this.name = name;
        this.jsonAllMessagesFile = jsonAllMessagesFile;
        this.mmapFile = new File(CACHE_DIR, name + ".mmap");
        this.mmapPropsFile =  new File(CACHE_DIR, name + ".properties");
    }

    public void load(int batchSize) {
        try {
            inlineLoad(batchSize, false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void parseAndLoad(int batchSize) {
        try {
            inlineLoad(batchSize, true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void inlineLoad(int batchSize, boolean reparse) throws IOException {
        System.out.println("Loading message batch sets");
        createMmapFile();

        createBatchMap(batchSize, mmapFile);

        if (!mmapPropsFile.exists() || reparse ) {
            System.out.println("Parsing batch sets");
            InlineMessageParser inlineMessageParser = new InlineMessageParser();
            Batcher<?> jsonBatcher = createBatcher(batchSize);
            inlineMessageParser.parse(jsonAllMessagesFile, jsonBatcher);

            this.batchSize = batchSize;
            this.batchCount = jsonBatcher.getAllBatchCounter();
            System.out.println("Done with " + batchSize + " x " + this.batchCount);

            writeProperties(batchSize);
        } else {
            loadProperties();
            if (this.batchSize != batchSize) {
                System.out.println("Rebuilding message cache since batch size does not match (current: " + this.batchSize + "/requested:" + batchSize + ")");
                mmapFile.delete();
                inlineLoad(batchSize, true);
            }
        }
    }

    protected abstract Batcher<?> createBatcher(int batchSize);
    protected abstract void createBatchMap(int batchSize, File mmapFile) throws IOException;

    private void createMmapFile() throws IOException {
        if (!mmapFile.exists()) {
            CACHE_DIR.mkdirs();
            mmapFile.createNewFile();
        }
    }

    private void writeProperties(int batchSize) throws IOException {
        Properties properties = new Properties();
        properties.setProperty("batch.count", String.valueOf(this.batchCount));
        properties.setProperty("batch.size", String.valueOf(batchSize));
        try (FileOutputStream out = new FileOutputStream(mmapPropsFile)) {
            properties.store(out, null);
        }
    }

    private void loadProperties() throws IOException {
        Properties properties = new Properties();
        try (FileInputStream in = new FileInputStream(mmapPropsFile)) {
            properties.load(in);
        }

        this.batchSize = Integer.parseInt(properties.getProperty("batch.size", "1"));
        this.batchCount = Integer.parseInt(properties.getProperty("batch.count", "0"));
    }

    protected int iterate() {
        if (iteratorCounter >= batchCount) {
            iteratorCounter = 0;
        }

        return iteratorCounter++;
    }

    public void reset() {
        iteratorCounter = 0;
    }
}

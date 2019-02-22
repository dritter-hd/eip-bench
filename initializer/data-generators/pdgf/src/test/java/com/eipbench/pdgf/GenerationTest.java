package com.eipbench.pdgf;

import org.junit.After;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class GenerationTest {
    private static final Logger log = LoggerFactory.getLogger(GenerationTest.class);
    private final TpchDataGenerator gen = new TpchDataGenerator();

    @After
    public void cleanup() throws IOException {
        log.debug("Cleanup working dir: {}", gen.getWorkingDir().getAbsolutePath());
        /*org.apache.commons.io.FileUtils.deleteDirectory(gen.getWorkingDir()); Cleanup disabled for generation reuse by other modules*/
    }

    @Test
    public void testGeneration() throws Exception {
        log.debug("Preparing working dir: {}", gen.getWorkingDir());
        gen.prepareWorkingDir();
        gen.generate();
    }
}

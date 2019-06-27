package com.eipbench.states.java;

import org.openjdk.jmh.annotations.Setup;

public class JavaBaseline extends JavaFixture {
        @Setup
        public void setup() throws Exception {
            endpoint = context.getEndpoint(BASELINE);
            if (endpoint == null) {
                throw new RuntimeException("Endpoint not found");
            }
            producer = context.createProducerTemplate();
        }
}

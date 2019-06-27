package com.eipbench.camel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class JacksonJsonProcessor implements Processor {
    private final ObjectMapper om = new ObjectMapper();

    @Override
    public void process(final Exchange exchange) throws Exception {
        final JsonNode tree = om.readTree(exchange.getIn().getBody(String.class));
        exchange.getIn().setBody(tree);
    }
}

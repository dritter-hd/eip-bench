package com.eipbench.camel;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.converter.stream.InputStreamCache;

import java.io.InputStream;

public class SerializeJsonToDatalogProgram implements Processor {
    JsonDatalogDataFormat jsonDatalogDataFormat = new JsonDatalogDataFormat();

    public SerializeJsonToDatalogProgram() {
        // Meta facts are slowing down performance tests
        jsonDatalogDataFormat.setSkipMetaFactGeneration(true);
    }
    
    @Override
    public void process(Exchange exchange) throws Exception {
        if (exchange.getIn().getBody() instanceof InputStreamCache) {
            try {
                final Object dp = jsonDatalogDataFormat.unmarshal(exchange, exchange.getIn().getBody(InputStream.class));
                exchange.getIn().setBody(dp);
            } catch (final Exception e) {
                throw new IllegalStateException("Camel Converter Issue: ", e);
            }
        } else {
            final Object dp = jsonDatalogDataFormat.unmarshal(exchange, exchange.getIn().getBody(JsonNode.class));
            exchange.getIn().setBody(dp);
        }
    }
}

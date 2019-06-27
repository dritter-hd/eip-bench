package com.eipbench.camel;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.converter.stream.InputStreamCache;

import java.io.InputStream;

public class SerializeCsvToDatalogProgram implements Processor {
    private final CsvDatalogDataFormat formatter = new CsvDatalogDataFormat(",", "name");
    
    @Override
    public void process(final Exchange exchange) throws Exception {
        if (exchange.getIn().getBody() instanceof InputStreamCache) {
            try {
                final Object dp = formatter.unmarshal(exchange, exchange.getIn().getBody(InputStream.class));
                exchange.getIn().setBody(dp);
            } catch (final Exception e) {
                throw new IllegalStateException("Camel Converter Issue: ", e);
            }
        }       
    }
}

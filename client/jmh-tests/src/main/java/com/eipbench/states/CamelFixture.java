package com.eipbench.states;

import com.eipbench.content.Constants;
import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

@State(Scope.Benchmark)
public class CamelFixture {
    public static final String BASELINE = "direct:baseline";
    protected final CamelContext context = new DefaultCamelContext();
    private final String DELIMITER = ":";

    protected Endpoint endpoint;
    protected ProducerTemplate producer;

    public Endpoint getEndpoint() {
        return endpoint;
    }

    public ProducerTemplate getProducer() {
        return producer;
    }

    protected Endpoint getEndpoint(Constants.TYPE type) {
        final Endpoint endpoint = context.getEndpoint("direct:" + type.buildUriPart(Constants.FORMAT.JSON, DELIMITER));
        if (endpoint == null) {
            throw new RuntimeException("Endpoint not found");
        }
        return endpoint;
    }

    protected RouteBuilder getBaselineRoute() {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from(BASELINE).process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        //do nothing
                    }
                });
            }
        };
    }

}
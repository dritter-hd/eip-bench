package com.eipbench.camel;

import com.eipbench.content.Constants;
import com.eipbench.content.Constants.*;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.MulticastDefinition;
import org.apache.camel.model.OnExceptionDefinition;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.RouteDefinition;

public abstract class RouteBuilderHelper extends RouteBuilder {
    protected static final String DELIMITER = ":";
    protected static final String ID_DELIMITER = "_";

    private Constants.TYPE lastFromType = null; // Warning, not THREAD SAFE
    private Constants.FORMAT lastFromFormat = null;

    protected final static Processor VOID = new Processor() {
        @Override
        public void process(final Exchange exchange) throws Exception {
        }
    };
    
    protected final static Processor IMG_VOID = new Processor() {
        @Override
        public void process(final Exchange exchange) throws Exception {
            // final Object body = exchange.getIn().getBody();
            // new ImageConsumerByDisplay().consume(exchange);
        }
    };
    
    protected final static Processor OCR_VOID = new Processor() {
        @Override
        public void process(final Exchange exchange) throws Exception {
            // final Object body = exchange.getIn().getBody();
            // new ImageConsumerByDisplay().consume(exchange);
        }
    };
    
    protected final static Processor IMG_SHOW_VOID = new Processor() {
        @Override
        public void process(final Exchange exchange) throws Exception {
            //final Object body = exchange.getIn().getBody();
            //new ImageConsumerByDisplay().consume(exchange);
        }
    };

    protected RouteDefinition from(String uri, TYPE type, FORMAT format) {
        // simplify code
        lastFromType = type;
        lastFromFormat = format;
        String newUri = buildUri(uri,lastFromType, lastFromFormat);
        RouteDefinition routeDef = super.from(newUri);
        return routeDef;
    }

    protected RouteDefinition from(String uri, TYPE type) {
        // simplify code
        return from(uri,type,FORMAT.JSON);
    }

    protected String buildUri(String subUri, TYPE type, FORMAT format) {
        return "direct:" + subUri + type.buildUriPart(format, DELIMITER);
    }

    public String buildUri(String subUri, TYPE type) {
        return buildUri(subUri,type,FORMAT.JSON);
    }

    // must call custom from Method to use this !
    protected String buildId(String subId) {
        return lastFromType.buildId(subId, ID_DELIMITER);
    }

    // must call custom from Method to use this !
    protected String buildUri(String subUri) {
        return buildUri(subUri,lastFromType,lastFromFormat);
    }

    private ProcessorDefinition addProcessor(ProcessorDefinition processorDefinition, Processor processor) {
        if (processor != null) {
            processorDefinition = processorDefinition.process(processor);
        }
        return processorDefinition;
    }

    private ProcessorDefinition addSkipDuplicate(ProcessorDefinition processorDefinition, boolean skipDuplicate) {
        if (!skipDuplicate) {
            processorDefinition = processorDefinition.filter(property(Exchange.DUPLICATE_MESSAGE).isEqualTo(true)).process(VOID)
                    .id(buildId("duplicate")).stop().end();
        }
        return processorDefinition;
    }

    protected void registerMc(TYPE type, int branchNo, boolean pp) {
        MulticastDefinition rd = from("", type).multicast();
        if (pp) {
            rd.parallelProcessing();
        }
        for (int i = 1; i <= branchNo; i++) {
            rd.to(buildUri(""+i));
            from(""+i, type).process(VOID).id(buildId(""+i));
        }
    }

    protected void registerLb(TYPE loadBalancerType, TYPE targetType) {
        from("", loadBalancerType).loadBalance().weighted(true, "1,0").to(buildUri("",targetType),buildUri("",targetType));
    }

    protected void registerALO(final TYPE type, final int maxRedeliver, final Processor processor, final boolean originalMessage, final boolean handled) {
        final OnExceptionDefinition rd = from("", type).onException(Exception.class).maximumRedeliveries(maxRedeliver);
        if (originalMessage) {
            rd.useOriginalMessage();
        }
        rd.handled(handled).end().process(processor).onException(Exception.class).handled(handled).end().process(VOID);
    }
}

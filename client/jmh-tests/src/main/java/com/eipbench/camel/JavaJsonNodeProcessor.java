package com.eipbench.camel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import java.util.ArrayList;
import java.util.List;

public abstract class JavaJsonNodeProcessor implements Processor {
    protected final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void process(final Exchange exchange) throws Exception {
        final List<JsonNode> jsonNodeList = exchange.getIn().getMandatoryBody(List.class);
        final List<JsonNode> result = new ArrayList<>();

        for (JsonNode jsonNode : jsonNodeList) {
            processNode(jsonNode, result);
        }

        exchange.getIn().setBody(result);
    }

    public abstract void processNode(final JsonNode jsonNode, final List<JsonNode> result);

}

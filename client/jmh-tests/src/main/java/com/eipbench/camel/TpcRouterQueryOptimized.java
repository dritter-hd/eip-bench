package com.eipbench.camel;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.camel.Exchange;
import org.apache.camel.Expression;
import org.apache.camel.InvalidPayloadException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TpcRouterQueryOptimized implements Expression {
    private final String path;
    protected List<Operator> operators;

    public TpcRouterQueryOptimized(final String path, final String key, final String value) {
        this.path = path;
    }

    public TpcRouterQueryOptimized(String path, Operator... operator) {
        this.path = path;
        operators = Arrays.asList(operator);
    }

    protected void checkJsonNode(JsonNode objectNode, List<JsonNode> jsonObjList) {
        if (matches(objectNode)) {
            jsonObjList.add(objectNode);
        }
    }

    @Override
    public <T> T evaluate(Exchange exchange, Class<T> type) {
        try {
            final List<JsonNode> nodeList = exchange.getIn().getMandatoryBody(List.class);

            final List<JsonNode> jsonObjList = new ArrayList<JsonNode>();

            /*final JsonNode nodeArray = jsonNode.at(path);*/

            /*for (JsonNode objectNode : nodeArray) {*/

            for (JsonNode objectNode : nodeList) {
                checkJsonNode(objectNode, jsonObjList);
            }
            /*}*/
            return exchange.getContext().getTypeConverter().convertTo(type, jsonObjList);
        } catch (InvalidPayloadException e) {
            throw new RuntimeException("Exception occured during processing of exchange", e);
        }
    }

    protected boolean matches(JsonNode objectNode) {
        for (Operator operator : operators) {
            if (!operator.eval(objectNode)) {
                return false;
            }
        }
        return true;
    }
}
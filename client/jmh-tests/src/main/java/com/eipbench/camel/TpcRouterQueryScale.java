package com.eipbench.camel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.camel.Exchange;
import org.apache.camel.Expression;
import org.apache.camel.InvalidPayloadException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class TpcRouterQueryScale implements Expression {
    private final String path;
    protected List<Operator> operators;
    protected List<Operator> operatorsSubObject = Arrays.asList(new OperatorEquals("C_ADDRESS", "does-not-match"));

    public TpcRouterQueryScale(final String path, final String key, final String value) {
        this.path = path;
    }

    public TpcRouterQueryScale(final String path, final Operator... operator) {
        this.path = path;
        operators = Arrays.asList(operator);
    }

    protected void checkJsonNode(JsonNode objectNode, List<JsonNode> jsonObjList) {
        if (matches(objectNode)) {
            jsonObjList.add(objectNode);
        }
    }

    protected void checkSubJsonNode(final JsonNode objectNode, final List<JsonNode> jsonObjList) {
        if (matchesSubObject(objectNode)) {
            jsonObjList.add(objectNode);
        }
    }
    
    @Override
    public <T> T evaluate(Exchange exchange, Class<T> type) {
        try {
            final List<JsonNode> nodeList = exchange.getIn().getMandatoryBody(List.class);
            final List<JsonNode> intermediate = new ArrayList<JsonNode>();
            final List<JsonNode> jsonObjList = new ArrayList<JsonNode>();
            
            for (JsonNode objectNode : nodeList) {
                checkJsonNode(objectNode, intermediate);
            }

            for (JsonNode objectNode : nodeList) {
                final ArrayNode customers = (ArrayNode) objectNode.get("CUSTOMERS");
                if (customers != null) {
                    final Iterator<JsonNode> iterator = customers.iterator();
                    while (iterator.hasNext()) {
                        final JsonNode next = iterator.next();
                        checkSubJsonNode(next, jsonObjList);
                    }
                }
            }
            return exchange.getContext().getTypeConverter().convertTo(type, jsonObjList);
        } catch (final InvalidPayloadException e) {
            throw new RuntimeException("Exception occured during processing of exchange", e);
        }
    }

    protected boolean matches(final JsonNode objectNode) {
        boolean matches = false;
        for (Operator operator : operators) {
            if (operator.eval(objectNode)) {
                matches = true;
            }
        }
        return matches;
    }
    
    protected boolean matchesSubObject(final JsonNode objectNode) {
        boolean matches = true;
        for (Operator operator : operatorsSubObject) {
            if (!operator.eval(objectNode)) {
                matches = false;
            }
        }
        return matches;
    }
}

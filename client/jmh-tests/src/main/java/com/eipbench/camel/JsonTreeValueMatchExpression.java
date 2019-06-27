package com.eipbench.camel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.camel.Exchange;
import org.apache.camel.Expression;
import org.apache.camel.InvalidPayloadException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JsonTreeValueMatchExpression implements Expression {
    private final String path;

    private List<Operator> operators;

    public JsonTreeValueMatchExpression(final String path, final String key, final String value) {
        this.path = path;
    }

    public JsonTreeValueMatchExpression(String path, Operator... operator) {
        this.path = path;

        operators = Arrays.asList(operator);
    }

    @Override
    public <T> T evaluate(Exchange exchange, Class<T> type) {
        try {
            final JsonNode jsonNode = exchange.getIn().getMandatoryBody(JsonNode.class);

            final List<JsonNode> jsonObjList = new ArrayList<JsonNode>();

            final JsonNode nodeArray = jsonNode.at(path);

            for (JsonNode objectNode : nodeArray) {
                if(objectNode.isObject()) {
                    ObjectNode subObjectNode = (ObjectNode) objectNode;

                    if (matches(objectNode)) {
                        jsonObjList.add(subObjectNode);
                    }
                }
            }

            return exchange.getContext().getTypeConverter().convertTo(type, jsonObjList);
        } catch (InvalidPayloadException e) {
            throw new RuntimeException("Exception occured during processing of exchange", e);
        }
    }

    private boolean matches(JsonNode objectNode) {
        for (Operator operator : operators) {
            if (!operator.eval(objectNode)) {
                return false;
            }
        }
        return true;
    }
}

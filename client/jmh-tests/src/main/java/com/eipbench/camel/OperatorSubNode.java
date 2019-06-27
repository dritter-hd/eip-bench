package com.eipbench.camel;

import com.fasterxml.jackson.databind.JsonNode;

public class OperatorSubNode implements Operator {
    private final String key;
    private final Operator operator;

    public OperatorSubNode(String key, Operator operator) {
        this.key = key;
        this.operator = operator;
    }

    @Override
    public boolean eval(JsonNode node) {
        JsonNode jsonnode = node.get(key);
        return  operator.eval(jsonnode);
    }
}

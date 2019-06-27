package com.eipbench.camel;

import com.fasterxml.jackson.databind.JsonNode;

public class OperatorEquals implements Operator {
    private final String key;
    private final Object value;

    public OperatorEquals(String key, Object value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public boolean eval(JsonNode node) {
        return node.get(key).asText().equals(value);
    }
}

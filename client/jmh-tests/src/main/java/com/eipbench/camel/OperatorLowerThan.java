package com.eipbench.camel;

import com.fasterxml.jackson.databind.JsonNode;

public class OperatorLowerThan implements Operator {
    private final String key;
    private final Number value;

    public OperatorLowerThan(final String key, final Number value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public boolean eval(final JsonNode node) {
        return node.get(key).asDouble() < value.doubleValue();
    }
}
